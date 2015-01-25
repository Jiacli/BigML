import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */

/**
 * @author Jiachen Li (jiachenl)
 *
 */
public class NBTest {


    // NB model parameters
    private static Map<String, Map<Integer, Float>> model;
    private static Map<String, Integer> wordset;
    private static final float smooth = 1.0f;
    

    public static void main(String[] args) throws IOException {
        
        wordset = new HashMap<String, Integer>();
        model = new HashMap<String, Map<Integer, Float>>();
        model.put("CCAT", new HashMap<Integer, Float>()); // Corporate/Industrial
        model.put("ECAT", new HashMap<Integer, Float>()); // Economics
        model.put("GCAT", new HashMap<Integer, Float>()); // Government/Social
        model.put("MCAT", new HashMap<Integer, Float>()); // Markets
        
        Map<String, Float> priorMap = new HashMap<>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            // input validation
            String[] seg = line.split("\t");
            if (seg.length != 2) {
                continue;
            }
            // input type
            // 1. Y=y; 2. Y=*; 3. Y=y,W=w; 4. Y=y,W=*;
            if (seg[0].contains(",")) { // case 3,4
                String[] pair = seg[0].split(",");
                String word = pair[1].substring(2);
                if (!wordset.containsKey(word)) {
                    wordset.put(word, wordset.size() + 1);
                }
                model.get(pair[0].substring(2)).put(wordset.get(word), Float.valueOf(seg[1]));
            } else { // case 1,2
                priorMap.put(seg[0].substring(2), Float.valueOf(seg[1]));
            }          
        }
        br.close();
        
        // build Naive Bayes model
        int star = wordset.remove("*");
        for (String key : model.keySet()) {
            Map<Integer, Float> map = model.get(key);
            for (int feat : map.keySet()) {
                if (feat == star) {
                    continue;
                }
                float p = (map.get(feat) + smooth) / (map.get(star) + smooth * wordset.size());
                map.put(feat, (float)Math.log(p));
            }
            float prior = priorMap.get(key) / priorMap.get("*");
            map.put(0, (float)Math.log(prior));
            map.put(-1, (float)Math.log(smooth / (smooth * wordset.size())));
            map.remove(star);
        }
        // so in the map
        // 0 - prior
        // -1 - words not in this class
        // 1-N - words this class has
        priorMap.clear();
        predict(args[0]);       

//        Runtime runtime = Runtime.getRuntime();
//        System.out.println("Memory used:  "
//                        + ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L))
//                        + " MB");
    }
    
    private static void predict(String testFile) throws IOException {
        // load test data
//        List<String> labels = new ArrayList<>();
        List<String> feats = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                testFile)));
//        int testSize = 0, acc = 0;
        
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            // extract the label
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                continue;
            }
//            testSize++;
            
//            String[] tags = seg[0].trim().split(",");
//            for (String tag : tags) {
//                if (tag.endsWith("CAT")) {
//                    labels.add(tag);
//                }
//            }
            
            // prediction using model
            feats = tokenizeDoc(seg[1]);
            String label = "";
            float score = - Float.MAX_VALUE;
            
            for (String tag : model.keySet()) {
                Map<Integer, Float> map = model.get(tag);
                float s = map.get(0);
                
                for (int i = 0; i < feats.size(); i++) {
                    String feat = feats.get(i);
                    if (!wordset.containsKey(feat)) {
                        continue;
                    }
                    int wordKey = wordset.get(feat);
                    if (map.containsKey(wordKey)) {
                        s += map.get(wordKey);
                    } else {
                        s += map.get(-1);
                    }
                }
                if (s > score) {
                    score = s;
                    label = tag;
                }
            }
            
            System.out.println(label + "\t" + score);
            
//            // check accuracy
//            for (int i = 0; i < labels.size(); i++) {
//                if (label.equals(labels.get(i))) {
//                    acc++;
//                    break;
//                }
//            }
            
//            labels.clear();
            feats.clear();
        }
        br.close();

//        System.out.println("Test Accuracy: " + (double)acc / testSize);
//        System.out.println("Wordset size: " + wordset.size());
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
