import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        
//        String[] tokenss = "xy,ab,m,tt\t wrod gmsd_%20_wuhuo_fucky*u! dome you_dam%EC! hao~$ hehe.".split("[^A-Za-z0-9]");
//        for (String string : tokenss) {
//            System.out.println(string);
//        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            List<Integer> tokens = new ArrayList<>();
            String[] labels = tokenizeDoc(line, tokens).split(",");
            
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
                for (int j = 0; j < tokens.size(); j++) {
                    // update word set
                    int wordhash = tokens.get(j);
                    wordSet.add(wordhash);
                    if (wordSet.size() > BUFFSIZE) {
                        for (int hash : wordSet) {
                            System.out.println(hash);
                        }
                        wordSet.clear();
                    }

                    // update #(W=word,Y=label)
                    updateParamCount(labels[i], wordhash, 1);
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
    
    // tokenize the documents and return the label
    private static String tokenizeDoc(String cur_doc, List<Integer> tokens) {
        Set<Integer> stopwords = stopWords();
        String[] words = cur_doc.split("\\s+");
        String label = words[0]; // label will be stored at index 0

        for (int i = 1; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\W", "");
            if (words[i].length() > 0) {
                int wordhash = words[i].toLowerCase().hashCode();
                if (!stopwords.contains(wordhash)){
                    tokens.add(wordhash);
                }                
            }
        }
        return label;
    }
    
    static final String[] stopwords = {"a", "able", "about", "across", "after", "almost",
            "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "all",
            "because", "been", "but", "by", "can", "cannot", "could", "dear", "did",
            "do", "does", "either", "else", "ever", "every", "for", "from", "get",
            "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how",
            "however", "i", "if", "in", "into", "is", "it", "its", "just", "least",
            "let", "like", "likely", "may", "me", "might", "most", "must", "my",
            "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", 
            "other", "our", "own", "rather", "said", "say", "says", "she", "should", 
            "since", "so", "some", "than", "that", "the", "their", "them", "then", 
            "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", 
            "was", "we", "were", "what", "when", "where", "which", "while", "who", 
            "whom", "why", "will", "with", "would", "yet", "you", "your"};
    
    private static Set<Integer> stopWords() {
        Set<Integer> set = new HashSet<>();
        for (String word : stopwords) {
            set.add(word.hashCode());
        }
        return set;        
    }
}
