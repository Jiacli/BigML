import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author JK
 *
 */
public class NBTest {

    static Map<String, Map<Integer, Float>> model;
    static Set<Integer> wordSet;
    static Map<String, Float> prior;
    static final int STARHASH = "*".hashCode();
    static final float SMOOTH = 1.0f;
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        wordSet = getTestWordSet(args[0]);
        buildModelWithNeededWords();
        
        int all = 0, correct = 0;
        
        // predict using NB model
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0])));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            List<Integer> tokens = new ArrayList<>();
            String allLabel = tokenizeDoc(line, tokens);
            
            // predict
            String label = "";
            float score = - Float.MAX_VALUE;
            for (String tag : model.keySet()) {
                Map<Integer, Float> map = model.get(tag);
                float s = prior.get(tag);
                
                for (int i = 0; i < tokens.size(); i++) {
                    int wordhash = tokens.get(i).hashCode();
                    if (!wordSet.contains(wordhash)) {
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
            
            // acc
            all++;
            String[] labels = allLabel.split(",");
            for (String tag : labels) {
                if (label.equals(tag)) {
                    correct++;
                    break;
                }
            }
        }
        br.close();
        
        System.out.println("acc: " + ((float)correct) / all);
    }
    
    private static void buildModelWithNeededWords() throws IOException {
        model = new HashMap<>();
        prior = new HashMap<>();
        
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
                if (!wordSet.contains(wordhash)) {
                    continue;
                }
                float count = Float.parseFloat(seg[2]);
                
                if (!model.containsKey(seg[0])) {
                    Map<Integer, Float> map = new HashMap<>();
                    map.put(wordhash, count);
                    model.put(seg[0], map);
                } else {
                    model.get(seg[0]).put(wordhash, count);
                }                
            } else if (seg.length == 2) {
                prior.put(seg[0], Float.parseFloat(seg[1]));
            } else {
                System.out.println("bad line");
                continue;
            }
        }
        br.close();
        wordSet.clear();
        
        // update model parameters
        float featsize = prior.get("#");
        for (String label : model.keySet()) {
            Map<Integer, Float> map = model.get(label);
            for (int hash : map.keySet()) {
                if (hash == STARHASH) {
                    continue;
                }
                float p = (map.get(hash) + SMOOTH) / (map.get(STARHASH) + SMOOTH * featsize);
                map.put(hash, (float)Math.log(p));
                wordSet.add(hash); // rebuild the wordSet
            }
            float pr = prior.get(label) / prior.get("*");
            prior.put(label, (float)Math.log(pr));
        }
        prior.put("#", (float)Math.log(SMOOTH / (SMOOTH * featsize)));        
    }
    
    private static Set<Integer> getTestWordSet(String filename) throws IOException {
        HashSet<Integer> set = new HashSet<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            List<Integer> tokens = new ArrayList<>();
            tokenizeDoc(line, tokens);
            // The tokens have already been hashcode
            for (int i = 0; i < tokens.size(); i++) {
                set.add(tokens.get(i));
            }
        }
        br.close();
        set.add(STARHASH);
        return set;
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
