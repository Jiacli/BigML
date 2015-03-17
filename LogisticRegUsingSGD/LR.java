import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author JK
 *
 */

public class LR {

    /**
     * @param args
     * @throws IOException
     */
    static int N;

    public static void main(String[] args) throws IOException {
        if (args.length != 6) {
            System.err.println("Error: invalid arguments!");
            System.exit(-1);
        }

        // load parameters
        N = Integer.parseInt(args[0]) / 10; // vocabulary size
        float lambda = Float.parseFloat(args[1]); // learning rate
        float mu = Float.parseFloat(args[2]); // regularization coefficient
        int T = Integer.parseInt(args[3]); // maximum iteration
        int sizeT = Integer.parseInt(args[4]); // size of trianing dataset
        String testset = args[5];
        HashMap<String, Integer> map = Initialize();
        float[][] beta = new float[14][N];
        short[][] A = new short[14][N];

        // training
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line = null;
        ArrayList<Integer> feats = new ArrayList<>();
        HashSet<String> labels = new HashSet<>();
        double y, lambda_t = 0.0, alpha = 0.0;
        int k = 0;
        for (int t = 1; t < T; t++) {
            // update learning rate
            lambda_t = lambda / (t * t);
            alpha = 1 - 2 * lambda_t * mu;

            for (k = sizeT * (t - 1); k < sizeT * t; k++) {
                line = br.readLine();
                // if (line == null) {
                // continue;
                // }
                tokenizeDoc(line, feats, labels);
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    if (labels.contains(entry.getKey())) {
                        y = 1.0;
                    } else {
                        y = 0.0;
                    }
                    int tag = entry.getValue();
                    double z = 0.0;
                    for (int id : feats) {
                        z += beta[tag][id];
                    }
                    double val = lambda_t * (y - sigmoid(z));
                    // for each feature
                    for (int feat : feats) {
                        if (k - A[tag][feat] > 0) {
                            beta[tag][feat] *= Math
                                    .pow(alpha, k - A[tag][feat]);
                            A[tag][feat] = (short) k;
                        }
                        beta[tag][feat] += val;
                    }
                }
            }
        }
        br.close();
        // apply final regularization
        for (int i = 0; i < beta.length; i++) {
            for (int j = 0; j < beta[0].length; j++) {
                if (k - A[i][j] > 0) {
                    beta[i][j] *= Math.pow(alpha, k - A[i][j]);
                }
            }
        }

        br = new BufferedReader(new InputStreamReader(new FileInputStream(
                testset)));
        double[] z = new double[14];
        while ((line = br.readLine()) != null) {
            tokenizeDocTest(line, feats);
            for (int feat : feats) {
                for (int i = 0; i < z.length; i++) {
                    z[i] += beta[i][feat];
                }
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                sb.append(entry.getKey());
                sb.append("\t");
                sb.append(sigmoid(z[entry.getValue()]));
                sb.append(",");
            }
            System.out.println(sb.substring(0, sb.length() - 1));
            Arrays.fill(z, 0.0);
        }
        br.close();

    }

    private static double sigmoid(double z) {
        return 1.0 - 1.0 / (1.0 + Math.exp(z));
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

    private static void tokenizeDocTest(String cur_doc, ArrayList<Integer> feats) {
        feats.clear();

        StringTokenizer st = new StringTokenizer(cur_doc, " \t\r\n");
        st.nextToken(); // label will be stored at index 0

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
