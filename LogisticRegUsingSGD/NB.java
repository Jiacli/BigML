import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author JK
 *
 */

public class NB {

    /**
     * @param args
     * @throws IOException
     */
    static int N;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Error: invalid arguments!");
            System.exit(-1);
        }

        // load parameters
        N = Integer.parseInt(args[0]); // vocabulary size
        String testset = args[1];

        HashMap<String, Integer> map = Initialize();
        double[][] W_pos = new double[14][N];
        double[][] W_neg = new double[14][N];
        double[] Prior_pos = new double[14];
        double[] Prior_neg = new double[14];

        // training
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line = null;
        ArrayList<Integer> feats = new ArrayList<>();
        HashSet<String> labels = new HashSet<>();

        while ((line = br.readLine()) != null) {
            tokenizeDoc(line, feats, labels);

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                int tag = entry.getValue();

                if (labels.contains(entry.getKey())) {
                    Prior_pos[tag]++;
                    for (int id : feats) {
                        W_pos[tag][id]++;
                    }
                } else {
                    Prior_neg[tag]++;
                    for (int id : feats) {
                        W_neg[tag][id]++;
                    }
                }
            }
        }
        br.close();

        // calculate parameters
        // priors
        count_to_prob(Prior_pos);
        count_to_prob(Prior_neg);
        // probs
        for (int i = 0; i < 14; i++) {
            count_to_prob_smooth(W_pos[i]);
            count_to_prob_smooth(W_neg[i]);
        }

        // auto evaluation part
        br = new BufferedReader(new InputStreamReader(new FileInputStream(
                testset)));
        int correct_sample = 0, all_sample = 0;
        while ((line = br.readLine()) != null) {
            tokenizeDoc(line, feats, labels);

            // eval for each label
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                int tag = entry.getValue();
                String key = entry.getKey();
                double pos = Prior_pos[tag], neg = Prior_neg[tag];
                for (int id : feats) {
                    pos += W_pos[tag][id];
                    neg += W_neg[tag][id];
                }

                if ((pos >= neg && labels.contains(key))
                        || (pos < neg && !labels.contains(key))) {
                    correct_sample++;
                }
                all_sample++;
            }

            // StringBuilder sb = new StringBuilder();
            // for (Map.Entry<String, Integer> entry : map.entrySet()) {
            // sb.append(entry.getKey());
            // sb.append("\t");
            // sb.append(sigmoid(z[entry.getValue()]));
            // sb.append(",");
            // }
            // System.out.println(sb.substring(0, sb.length() - 1));

        }
        br.close();

        System.out.println("Accuracy: "
                + ((double) correct_sample / all_sample));
    }

    private static void count_to_prob(double[] v) {
        int V = v.length;
        double sum = 0.0;
        for (int i = V - 1; i >= 0; i--) {
            sum += v[i];
        }
        for (int i = V - 1; i >= 0; i--) {
            v[i] = Math.log(v[i] / sum);
        }
    }

    private static void count_to_prob_smooth(double[] v) {
        int V = v.length;
        double sum = 0.0;
        for (int i = V - 1; i >= 0; i--) {
            sum += v[i];
        }
        for (int i = V - 1; i >= 0; i--) {
            v[i] = Math.log((v[i] + 1.0) / (sum + V));
        }
    }

    private static void tokenizeDoc(String cur_doc, ArrayList<Integer> feats,
            HashSet<String> labels) {
        feats.clear();
        labels.clear();

        StringTokenizer st = new StringTokenizer(cur_doc, " \t\r\n");
        String label = st.nextToken(); // label will be stored at index 0
        for (String tag : label.split(",")) {
            labels.add(tag);
        }

        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (word.length() > 3 && word.length() < 15) {
                int word_id = word.hashCode() % N;
                while (word_id < 0)
                    word_id += N;
                feats.add(word_id);
            }
        }
    }

    private static HashMap<String, Integer> Initialize() {
        HashMap<String, Integer> map = new HashMap<>();

        map.put("nl", 0);
        map.put("el", 1);
        map.put("ru", 2);
        map.put("sl", 3);
        map.put("pl", 4);
        map.put("ca", 5);
        map.put("fr", 6);
        map.put("tr", 7);
        map.put("hu", 8);
        map.put("de", 9);
        map.put("hr", 10);
        map.put("es", 11);
        map.put("ga", 12);
        map.put("pt", 13);

        return map;
    }

}
