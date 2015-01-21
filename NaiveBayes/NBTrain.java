import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * 
 */

/**
 * @author Jiachen Li (jiachenl)
 *
 */
public class NBTrain {

    // Streaming Naive Bayes
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // HashMap to store the counts
        Map<String, Integer> map = new HashMap<>();
        map.put("Y=*", 0);
        
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> feats = new ArrayList<>();        
        
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            // process input sample
            // get the label
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                System.out.println("invalid: " + line);
                continue;
            }
            String[] tags = seg[0].trim().split(",");
            for (String tag : tags) {
                if (tag.endsWith("CAT")) {
                    labels.add(tag);
                }
            }
            
            for (int i = 0; i < labels.size(); i++) {
                String label = "Y=" + labels.get(i);
                if (map.containsKey(label)) {
                    map.put(label, map.get(label) + 1);
                } else {
                    map.put(label, 1);
                    map.put(label + ",W=*", 0);
                }
            }
            map.put("Y=*", map.get("Y=*") + labels.size());
            
            // process features
            feats = tokenizeDoc(seg[1]);
            for (String feat : feats) {
                for (int i = 0; i < labels.size(); i++) {
                    String key = "Y=" + labels.get(i) + ",W=" + feat;
                    if (map.containsKey(key)) {
                        map.put(key, map.get(key) + 1);
                    } else {
                        map.put(key, 1);
                    }
                }
            }
            
            for (int i = 0; i < labels.size(); i++) {
                String key = "Y=" + labels.get(i) + ",W=*";
                map.put(key, map.get(key) + feats.size());
            }
            
            labels.clear();
            feats.clear();
        }
        br.close();
        
        for (String key : map.keySet()) {
            System.out.println(key + "\t" + map.get(key));
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
