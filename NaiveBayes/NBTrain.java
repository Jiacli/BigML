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

    static final int BUFFSIZE = 20000;     // buffer size
    static final int STARHASH = "*".hashCode();

    public static void main(String[] args) throws IOException {
        Map<String, IntHashMap> model = new HashMap<>();
        Set<Integer> wordSet = new HashSet<>();
        Map<String, Integer> prior = new HashMap<>();
        prior.put("*", 0);
        int model_size = 0;
        
//        String[] tokenss = "xy,ab,m,tt\t wrod gmsd_%20_wuhuo_fucky*u! dome you_dam%EC! hao~$ hehe.".split("[^A-Za-z0-9]");
//        for (String string : tokenss) {
//            System.out.println(string);
//        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<Integer> tokens = new ArrayList<>();
        String line;        
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] labels = tokenizeDoc(line, tokens).split(",");
            for (int i = 0; i < labels.length; i++) {
                IntHashMap map;
                if (!model.containsKey(labels[i])) {
                    map = new IntHashMap();
                    map.put(STARHASH, 0);
                    model.put(labels[i], map);
                    prior.put(labels[i], 0);
                } else {
                    map = model.get(labels[i]);
                }
                for (int j = 0; j < tokens.size(); j++) {
                    // update word set
                    int wordhash = tokens.get(j);
                    wordSet.add(wordhash);

                    // update #(W=word,Y=label)
                    if (map.containsKey(wordhash)) {          
                        map.put(wordhash, map.get(wordhash) + 1);
                    } else {
                        map.put(wordhash, 1);
                        model_size++;
                    }
                    
                    // check whether buffer is full
                    if (model_size > BUFFSIZE) {
                        for (String tag : model.keySet()) {
                            map = model.get(tag);
                            for (int hash : map.keySet()) {
                                System.out.println(tag + "," + hash + "\t"
                                        + map.get(hash));
                            }
                            map.clear();
                            map.put(STARHASH, 0);
                        }
                        model_size = 0;

                        for (int hash : wordSet) {
                            System.out.println(hash);
                        }
                        wordSet.clear();
                    }
                }
                map.put(STARHASH, map.get(STARHASH) + tokens.size()); // update #(W=*,Y=label)
                prior.put(labels[i], prior.get(labels[i]) + 1);
                prior.put("*", prior.get("*") + 1);
            }
            tokens.clear();
        }
        br.close();
        
        if (model_size > 0) {
            for (String tag : model.keySet()) {
                IntHashMap map = model.get(tag);
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
    
    // tokenize the documents and return the label
    private static String tokenizeDoc(String cur_doc, List<Integer> tokens) {
        StringTokenizer st = new StringTokenizer(cur_doc);
        String label = st.nextToken(); // label will be stored at index 0
        
        int sample = 0;
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (word.length() > 3 && sample++ % 3 == 0) {
                tokens.add(word.replaceAll("\\W", "").toLowerCase().hashCode());
            }
        }
        st = null;
        return label;
    }

}
