import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Small Memory Footprint Streaming Naive Bayes
 * - Training Part
 * 
 * @author JK
 *
 */
public class NBTrain {

    
    
    static Map<String, Map<Integer, Integer>> model;
    static Map<String, Integer> prior;
    static int model_size;
    static Set<Integer> wordSet;            // Word hash
    static final int BUFFSIZE = 100000;     // buffer size for set and map
    static final int STARHASH = "*".hashCode();

    public static void main(String[] args) throws IOException {
        wordSet = new HashSet<>();
        prior = new HashMap<>();
        prior.put("*", 0);
        model = new HashMap<>();
        model_size = 0;
        
//        List<String> tokens = tokenizeDoc("xy,ab,m,tt\t wrod gmsd_%20_wuhuo_fucky*u! dome you_dam%EC! hao~$ hehe.");
//        for (String string : tokens) {
//            System.out.println(string);
//        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            ArrayList<String> tokens = tokenizeDoc(line);
            String[] labels = tokens.get(0).split(",");
            
            for (int i = 0; i < labels.length; i++) {
                Map<Integer, Integer> map;
                if (!model.containsKey(labels[i])) {
                    map = new HashMap<>();
                    map.put(STARHASH, 0);
                    model.put(labels[i], map);
                    prior.put(labels[i], 0);
                } else {
                    map = model.get(labels[i]);
                }
                for (int j = 1; j < tokens.size(); j++) {
                    // update word set
                    String word = tokens.get(j);
                    wordSet.add(word.hashCode());
                    if (wordSet.size() > BUFFSIZE) {
                        for (int hash : wordSet) {
                            System.out.println(hash);
                        }
                        wordSet.clear();
                    }

                    // update #(W=word,Y=label)
                    updateParamCount(labels[i], word.hashCode(), 1);
                }
                map.put(STARHASH, map.get(STARHASH) + tokens.size() - 1); // update #(W=*,Y=label)
                prior.put(labels[i], prior.get(labels[i]) + 1);
                prior.put("*", prior.get("*") + 1);
            }
        }
        br.close();
        
        if (model_size > 0) {
            for (String tag : model.keySet()) {
                Map<Integer, Integer> map = model.get(tag);
                for (int hash : map.keySet()) {
                    System.out.println(tag + "," + hash + "\t" + map.get(hash));
                }
                map.clear();
            }
            model_size = 0;
        }
        
        if (wordSet.size() > 0) {
            for (int hash : wordSet) {
                System.out.println(hash);
            }
            wordSet.clear();
        }

        // output prior count #(Y=y) & #(Y=*)
        for (String label : prior.keySet()) {
            System.out.println(label + "\t" + prior.get(label));
        }
        prior.clear();
        
    }
    
    private static void updateParamCount(String label, int wordhash, int n) {
        Map<Integer, Integer> map = model.get(label);
        
        if (map.containsKey(wordhash)) {          // update #(W=word,Y=label)
            map.put(wordhash, map.get(wordhash) + n);
        } else {
            map.put(wordhash, n);
            model_size++;
        }

        // check whether buffer is full
        if (model_size > BUFFSIZE) {
            for (String tag : model.keySet()) {
                map = model.get(tag);
                for (int hash : map.keySet()) {
                    System.out.println(tag + "," + hash + "\t" + map.get(hash));
                }
                map.clear();
                map.put(STARHASH, 0);
            }
            model_size = 0;
        }
    }
    
    
    private static ArrayList<String> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        ArrayList<String> tokens = new ArrayList<>();
        tokens.add(words[0]);  // label will be stored at index 0
        for (int i = 1; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\W", "");
            if (words[i].length() > 0) {
                tokens.add(words[i].toLowerCase());
            }
        }
        return tokens;
    }

}
