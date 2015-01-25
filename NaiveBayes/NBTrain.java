import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */

/**
 * Streaming Naive Bayes - Training Part
 * Counting is done in memory
 * 
 * @author Jiachen Li (jiachenl)
 *
 */
public class NBTrain {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // HashMap to store the counts
        Map<String, Integer> map = new HashMap<>();
        map.put("*", 0); // Y=*
        
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> feats = new ArrayList<>();        
        
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            // process input sample
            // extract the label
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                //System.out.println("invalid: " + line);
                continue;
            }
            String[] tags = seg[0].trim().split(",");
            for (String tag : tags) {
                if (tag.endsWith("CAT")) {
                    labels.add(tag);
                }
            }
            
            // label format: * or *CAT
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                if (map.containsKey(label)) {
                    map.put(label, map.get(label) + 1);
                } else {
                    map.put(label, 1);
                    map.put(label + ",*", 0);
                }
            }
            map.put("*", map.get("*") + labels.size()); // update Y=*
            
            // count each features
            // feature format *CAT,* or *CAT,Word
            feats = tokenizeDoc(seg[1]);
            for (String feat : feats) {
                for (int i = 0; i < labels.size(); i++) {
                    String key = labels.get(i) + "," + feat;
                    if (map.containsKey(key)) {
                        map.put(key, map.get(key) + 1);
                    } else {
                        map.put(key, 1);
                    }
                }
            }
            
            for (int i = 0; i < labels.size(); i++) {
                String key = labels.get(i) + ",*";
                map.put(key, map.get(key) + feats.size());
            }
            
            labels.clear();
            feats.clear();
        }
        br.close();
        
        for (String key : map.keySet()) {
            StringBuilder sb = new StringBuilder();
            if (!key.contains(",")) { // prior count
                sb.append("Y=");
                sb.append(key);
                sb.append("\t");
                sb.append(map.get(key));
            } else { // feature count
                String[] seg = key.split(",");
                sb.append("Y=");
                sb.append(seg[0]);
                sb.append(",W=");
                sb.append(seg[1]);
                sb.append("\t");
                sb.append(map.get(key));
            }
            System.out.println(sb.toString());
        }       
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
