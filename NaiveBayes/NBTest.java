import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 */

/**
 * @author Jiachen Li (jiachenl)
 *
 */
public class NBTest {

    /**
     * @param args
     */

    static private final float smooth = 1.0f;

    public static void main(String[] args) throws IOException {
        
        Map<String, Map<String, Float>> map = new HashMap<>();
        map.put("CCAT", new HashMap<String, Float>()); // Corporate/Industrial
        map.put("ECAT", new HashMap<String, Float>()); // Economics
        map.put("GCAT", new HashMap<String, Float>()); // Government/Social
        map.put("MCAT", new HashMap<String, Float>()); // Markets
        Set<String> wordset = new HashSet<>();
        Map<String, Float> tmpMap = new HashMap<>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split("\t");
            if (seg.length != 2) {
                System.out.println("invalid: " + line);
                continue;
            }
            // 1. Y=y; 2. Y=*; 3. Y=y,W=w; 4. Y=y,W=*;
            if (seg[0].contains(",")) { // case 3,4
                String[] pair = seg[0].split(",");
                map.get(pair[0].substring(2)).put(pair[1].substring(2), Float.valueOf(seg[1]));
                if (!wordset.contains(pair[1].substring(2))) {
                    wordset.add(pair[1].substring(2));
                }
            } else { // case 1,2
                tmpMap.put(seg[0].substring(2), Float.valueOf(seg[1]));
            }          
        }
        br.close();
        wordset.remove("*");
        
        // process Naive Bayes parameters
        for (String key : map.keySet()) {
            Map<String, Float> mp = map.get(key);
            for (String feat : mp.keySet()) {
                if (feat.equals("*")) {
                    continue;
                }
                float p = (mp.get(feat) + smooth) / (mp.get("*") + smooth * wordset.size());
                mp.put(feat, p);
            }
            float prior = (tmpMap.get(key) + smooth) / (tmpMap.get("*") + smooth * map.size());
            mp.put(key, prior);
            mp.put("*", smooth / (smooth * wordset.size()));
        }
        
        // load test data
        List<String> labels = new ArrayList<>();
        List<String> feats = new ArrayList<>();
        br = new BufferedReader(new InputStreamReader(new FileInputStream(
                args[0])));
        int testSize = 0, acc = 0;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            // process input sample
            // get the label
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                System.out.println("invalid test: " + line);
                continue;
            }
            testSize++;
            
            String[] tags = seg[0].trim().split(",");
            for (String tag : tags) {
                if (tag.endsWith("CAT")) {
                    labels.add(tag);
                }
            }
            
            // evaluate
            feats = tokenizeDoc(seg[1]);
            String label = "none";
            double logProb = - Double.MAX_VALUE;
            
            for (String tag : map.keySet()) {
                Map<String, Float> mp = map.get(tag);
                double s = Math.log(mp.get(tag));
                
                for (int i = 0; i < feats.size(); i++) {
                    String feat = feats.get(i);
                    if (!wordset.contains(feat)) {
                        continue;
                    }
                    if (mp.containsKey(feat)) {
                        s += Math.log(mp.get(feat));
                    } else {
                        s += Math.log(mp.get("*"));
                    }
                }
                
                if (s > logProb) {
                    logProb = s;
                    label = tag;
                }
            }
            
            System.out.println(label + "\t" + logProb);
            
            // check accuracy
            for (int i = 0; i < labels.size(); i++) {
                if (label.equals(labels.get(i))) {
                    acc++;
                    break;
                }
            }
            
            labels.clear();
            feats.clear();
        }
        br.close();
        
        System.out.println("Test Accuracy: " + (double)acc / testSize);
    }

    private static ArrayList<String> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        ArrayList<String> tokens = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\W", "");
            if (words[i].length() > 0) {
                tokens.add(words[i]);
            }
        }
        return tokens;
    }

}
