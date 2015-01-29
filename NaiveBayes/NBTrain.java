import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Small Memory Footprint Streaming Naive Bayes
 * - Training Part
 * 
 * @author JK
 *
 */
public class NBTrain {

    static Map<String, Map<Integer, Short>> model;
    static Map<String, Integer> prior;
    static int model_size;
    static Set<Integer> wordSet;           // Word hash
    static final int BUFFSIZE = 20000;     // buffer size for set and map
    static final int STARHASH = "*".hashCode();

    public static void main(String[] args) throws IOException {
        wordSet = new HashSet<>();
        prior = new HashMap<>();
        prior.put("*", 0);
        model = new HashMap<>();
        model_size = 0;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<Integer> tokens = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] labels = tokenizeDoc(line, tokens).split(",");
            
            for (int i = 0; i < labels.length; i++) {
                Map<Integer, Short> map;
                if (!model.containsKey(labels[i])) {
                    map = new HashMap<>();
                    map.put(STARHASH, (short) 0);
                    model.put(labels[i], map);
                    prior.put(labels[i], 0);
                } else {
                    map = model.get(labels[i]);
                }
                for (int j = 0; j < tokens.size(); j++) {
                    // update word set
                    int wordhash = tokens.get(j);
                    wordSet.add(wordhash);
//                    if (wordSet.size() > BUFFSIZE) {
//                        for (int hash : wordSet) {
//                            System.out.println(hash);
//                        }
//                        wordSet.clear();
//                    }

                    // update #(W=word,Y=label)
                    updateParamCount(labels[i], wordhash, (short) 1);
                }
                map.put(STARHASH, (short) (map.get(STARHASH) + tokens.size() - 1)); // update #(W=*,Y=label)
                prior.put(labels[i], prior.get(labels[i]) + 1);
                prior.put("*", prior.get("*") + 1);
            }
            tokens.clear();
        }
        br.close();
        
        if (model_size > 0) {
            for (String tag : model.keySet()) {
                Map<Integer, Short> map = model.get(tag);
                for (int hash : map.keySet()) {
                    System.out.println(tag + "," + hash + "\t" + map.get(hash));
                }
                map.clear();
            }
            model_size = 0;
        }
        
//        if (wordSet.size() > 0) {
//            for (int hash : wordSet) {
//                System.out.println(hash);
//            }
//            wordSet.clear();
//        }

        // output prior count #(Y=y) & #(Y=*)
        for (String label : prior.keySet()) {
            System.out.println(label + "\t" + prior.get(label));
        }
        prior.clear();
        
        // output wordsize
        System.out.println("# " + wordSet.size() * 2);
        wordSet.clear();        
    }
    
    private static void updateParamCount(String label, int wordhash, short n) {
        Map<Integer, Short> map = model.get(label);
        
        if (map.containsKey(wordhash)) {          // update #(W=word,Y=label)
            map.put(wordhash, (short) (map.get(wordhash) + n));
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
                map.put(STARHASH, (short) 0);
            }
            model_size = 0;
        }
    }
    
    // tokenize the documents and return the label
    private static String tokenizeDoc(String cur_doc, List<Integer> tokens) {
        StringTokenizer st = new StringTokenizer(cur_doc, " \t\r\n_");
        String label = st.nextToken(); // label will be stored at index 0
        
        int sample = 0;
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (word.length() > 3 && word.length() < 15 && sample++ % 3 == 0) {
                tokens.add(word.replaceAll("\\W", "").toLowerCase().hashCode());
            }
        }
        return label;
    }

}
