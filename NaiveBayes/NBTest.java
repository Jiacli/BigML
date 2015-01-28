import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author JK
 *
 */
public class NBTest {

    static final int STARHASH = "*".hashCode();
    static final float SMOOTH = 1.0f;
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        Map<String, IntObjectHashMap<Float>> model = new HashMap<>();
        Map<String, Float> prior = new HashMap<>();
        buildModelWithNeededWords(model, prior);
        
//        int all = 0, correct = 0;
        
        // predict using NB model
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0])));
        List<Integer> tokens = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            tokenizeDoc(line, tokens);
//            String allLabel = tokenizeDoc(line, tokens);
            
            // predict
            String label = "";
            float score = - Float.MAX_VALUE;
            for (String tag : model.keySet()) {
                IntObjectHashMap<Float> map = model.get(tag);
                float s = prior.get(tag);
                
                for (int i = 0; i < tokens.size(); i++) {
                    int wordhash = tokens.get(i).hashCode();
                    boolean valid = false;
                    for (String key : model.keySet()) {
                        if (model.get(key).containsKey(wordhash)) {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        continue;
                    }                   
                    
                    if (map.containsKey(wordhash)) {
                        s += map.get(wordhash);
                    } else {
                        s += prior.get("#");
                    }
                }
                
                if (s > score) {
                    score = s;
                    label = tag;
                }
            }
            System.out.println(label + "\t" + score);
            tokens.clear();
            
//            // acc
//            all++;
//            String[] labels = allLabel.split(",");
//            for (String tag : labels) {
//                if (label.equals(tag)) {
//                    correct++;
//                    break;
//                }
//            }
            
        }
        br.close();
        
//        System.out.println("acc: " + ((float)correct) / all);
    }
    
    private static void buildModelWithNeededWords(Map<String, IntObjectHashMap<Float>> model,
            Map<String, Float> prior) throws IOException {        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        // Collect needed parameters
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split(" ");
            if (seg.length == 3) { // label wordhash count
                int wordhash = Integer.parseInt(seg[1]);
//                if (!wordSet.contains(wordhash)) {
//                    boolean valid = false;
//                    for (String key : model.keySet()) {
//                        if (model.get(key).containsKey(wordhash)) {
//                            valid = true;
//                            break;
//                        }
//                    }
//                    if (!valid) {
//                        continue;
//                    }
//                }
                
                float count = Float.parseFloat(seg[2]);
                
                if (!model.containsKey(seg[0])) {
                    IntObjectHashMap<Float> map = new IntObjectHashMap<>();
                    map.put(wordhash, count);
                    model.put(seg[0], map);
                } else {
                    model.get(seg[0]).put(wordhash, count);
                }
                //wordSet.remove(wordhash);
            } else if (seg.length == 2) {
                prior.put(seg[0], Float.parseFloat(seg[1]));
            }
        }
        br.close();
        //wordSet.clear();
        
        // update model parameters
        float featsize = prior.get("#");
        for (String label : model.keySet()) {
            IntObjectHashMap<Float> map = model.get(label);
            for (int hash : map.keySet()) {
                if (hash == STARHASH) {
                    continue;
                }
                float p = (map.get(hash) + SMOOTH) / (map.get(STARHASH) + SMOOTH * featsize);
                map.put(hash, (float)Math.log(p));
                //wordSet.add(hash); // rebuild the wordSet
            }
            float pr = prior.get(label) / prior.get("*");
            prior.put(label, (float)Math.log(pr));
        }
        prior.put("#", (float)Math.log(SMOOTH / (SMOOTH * featsize)));        
    }
    
//    private static Set<Integer> getTestWordSet(String filename) throws IOException {
//        HashSet<Integer> set = new HashSet<>();
//        BufferedReader br = new BufferedReader(new InputStreamReader(
//                new FileInputStream(filename)));
//        String line;
//        while ((line = br.readLine()) != null) {
//            if (line.length() == 0) {
//                continue;
//            }
//            List<Integer> tokens = new ArrayList<>();
//            tokenizeDocSampling(line, tokens);
//            // The tokens have already been hashcode
//            for (int i = 0; i < tokens.size(); i++) {
//                set.add(tokens.get(i));
//            }
//        }
//        br.close();
//        set.add(STARHASH);
//        return set;
//    }

    // tokenize the documents and return the label
    private static String tokenizeDoc(String cur_doc, List<Integer> tokens) {
        StringTokenizer st = new StringTokenizer(cur_doc);
        String label = st.nextToken(); // label will be stored at index 0
        
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (word.length() > 3) {
                tokens.add(word.replaceAll("\\W", "").toLowerCase().hashCode());
            }
        }
        st = null;
        return label;
    }
    
//    private static String tokenizeDocSampling(String cur_doc, List<Integer> tokens) {
//        StringTokenizer st = new StringTokenizer(cur_doc);
//        String label = st.nextToken(); // label will be stored at index 0
//        
//        int sample = 0;
//        while (st.hasMoreTokens()) {
//            String word = st.nextToken();
//            if (word.length() > 3 && sample++ % 2 == 0) {
//                tokens.add(word.replaceAll("\\W", "").toLowerCase().hashCode());
//            }
//        }
//        return label;
//    }
}
