import java.awt.print.Pageable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Efficient Approximate PageRank
 * 
 * @author jiacli
 *
 */
public class ApproxPageRank {

    /**
     * @param args
     */
    private String inputPath;
    private String seed;
    private double alpha;
    private double epsilon;

    private HashMap<String, Double> p;
    private HashMap<String, String[]> cacheMap;
    private HashSet<String> toCache;

    public ApproxPageRank(String[] args) throws IOException {
        inputPath = args[0];
        seed = args[1];
        alpha = Double.parseDouble(args[2]);
        epsilon = Double.parseDouble(args[3]);

        p = new HashMap<>();
        cacheMap = new HashMap<>();
        toCache = new HashSet<>();

        // p.put(seed, 0.0);
        toCache.add(seed);
        cachePages();
    }

    BufferedReader br;

    private void cachePages() throws IOException {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(
                inputPath)));
        String line;
        while ((line = br.readLine()) != null) {
            String[] page = line.split("\t");
            if (toCache.contains(page[0])) {
                cacheMap.put(page[0], Arrays.copyOfRange(page, 1, page.length));
                toCache.remove(page[0]);
                if (toCache.isEmpty()) {
                    br.close();
                    return;
                }
            }
        }
        br.close();
        if (!toCache.isEmpty()) {
            toCache.clear();
        }
    }

    private void calcPageRank() throws IOException {
        HashMap<String, Double> r = new HashMap<>();
        r.put(seed, 1.0);
        
        boolean needUpdate = true;
        while (needUpdate) {
            needUpdate = false;

            boolean needPush = true;
            // push the node in cache
            while (needPush) {
                needPush = false;

                for (Map.Entry<String, String[]> entry : cacheMap.entrySet()) {
                    String page = entry.getKey();
                    String[] neighbor = entry.getValue();
                    int d = neighbor.length;
                    double ratio = r.get(page) / d;
                    if (ratio <= epsilon) {
                        continue;
                    }
                    needPush = true;
                    double p_score = p.containsKey(page) ? p.get(page) : 0.0;
                    double r_score = r.get(page);

                    // PUSH operation
                    // p' = p + \alpha * r_u
                    p.put(page, p_score + alpha * r_score);
                    // r' = r - r_u + (1 - \alpha) * r_u * (I +D^{-1} * A) / 2
                    double score = (1 - alpha) * r_score / 2;
                    r.put(page, score);
                    for (String node : neighbor) {
                        double node_score = r.containsKey(node) ? r.get(node)
                                : 0.0;
                        r.put(node, node_score + score / d);
                    }
                }
            }

            // check the pages that need to be cached
            for (Entry<String, Double> e : r.entrySet()) {
                if (e.getValue() > epsilon && !cacheMap.containsKey(e.getKey())) {
                    toCache.add(e.getKey());
                }
            }
            cachePages();

            // check whether need to update
            for (Entry<String, String[]> e : cacheMap.entrySet()) {
                if (r.get(e.getKey()) / e.getValue().length > epsilon) {
                    needUpdate = true;
                    break;
                }
            }
        }
        r.clear();
        toCache.clear();
    }

    private HashSet<String> buildSubgraph() {

        HashSet<String> S = new HashSet<>();
        HashSet<String> S_star = new HashSet<>();
        ArrayList<String> temp = new ArrayList<>();

        S.add(seed);
        S_star.add(seed);

        // sort the PageRank vector
        List<Map.Entry<String, Double>> pr = new ArrayList<>(p.entrySet());
        Collections.sort(pr, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                    Map.Entry<String, Double> o2) {
                double v1 = o1.getValue();
                double v2 = o2.getValue();
                if (v1 < v2) {
                    return 1;
                } else if (v1 > v2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        
        // initial parameters
        int volume = cacheMap.get(seed).length;
        int boundary = getBoundary(S);
        double cond = (double) boundary / volume;
        double cond_star = cond;
        
        for (int i = 0; i < pr.size(); i++) {
            String page = pr.get(i).getKey();
            if (seed.equals(page)) {
                continue;
            }
            
            S.add(page);
            temp.add(page);
            
            // re-calculate conductance
            volume += cacheMap.get(page).length;
            boundary = getBoundary(S);
            cond = (double) boundary / volume;
            
            if (cond < cond_star) {
                cond_star = cond;
                S_star.addAll(temp);
                temp.clear();
            }
        }
        S.clear();
        temp.clear();
        
        return S_star;
    }
    
    private int  getBoundary(HashSet<String> S) {
        int count = 0;
        for (String node : S) {
            String[] neighbors = cacheMap.get(node);
            for (String str : neighbors) {
                if (!S.contains(str)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public void printPR(HashSet<String> S) {
        for (String node : S) {
            System.out.println(node + "\t" + Math.max(1.0, Math.log(p.get(node) / epsilon)));
        }
    }

    public static void main(String[] args) throws IOException {

        ApproxPageRank apr = new ApproxPageRank(args);

        // calculate PageRank
        apr.calcPageRank();

        // build low-conductance subgraph
        HashSet<String> S = apr.buildSubgraph();
        
        // output results
        apr.printPR(S);
    }
}
