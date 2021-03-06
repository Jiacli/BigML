import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import javax.rmi.CORBA.Util;

/**
 * @author JK
 *
 */

public class LR_GD {

    /**
     * @param args
     * @throws IOException
     */
    static int N;

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.err.println("Error: invalid arguments!");
            System.exit(-1);
        }

        // load parameters
        N = Integer.parseInt(args[0]); // vocabulary size
        float lambda = Float.parseFloat(args[1]); // learning rate
        float mu = Float.parseFloat(args[2]); // regularization coefficient
        int T = Integer.parseInt(args[3]);
        String testset = args[4];

        HashMap<String, Integer> map = Initialize();
        float[][] beta = new float[14][N];

        // training
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line = null;
        ArrayList<Integer> feats = new ArrayList<>();
        HashSet<String> labels = new HashSet<>();

        ArrayList<ArrayList<Integer>> featList = new ArrayList<ArrayList<Integer>>();
        ArrayList<HashSet<String>> labelList = new ArrayList<HashSet<String>>();

        while ((line = br.readLine()) != null) {
            tokenizeDoc(line, feats, labels);
            featList.add(new ArrayList<Integer>(feats));
            labelList.add(new HashSet<String>(labels));
        }
        br.close();

        double y, lambda_t = 0.0, alpha = 0.0;

        double[] p = new double[14];
        for (int t = 1; t <= T; t++) {
            // update learning rate
            lambda_t = lambda / (t * t);
            alpha = 1 - 2 * lambda_t * mu;

            // apply regularization first
            for (int k = 0; k < 14; k++) {
                for (int c = 0; c < N; c++) {
                    beta[k][c] *= alpha;
                }
            }

            // calculate gradient over the whole data set
            for (int j = featList.size() - 1; j >= 0; j--) {
                feats = featList.get(j);
                labels = labelList.get(j);

                // calculate z
                for (int id : feats) {
                    for (int k = 0; k < 14; k++) {
                        p[k] += beta[k][id];
                    }
                }
                for (int k = 0; k < 14; k++) {
                    p[k] = sigmoid(p[k]);
                }

                // for each classifier
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    int tag = entry.getValue();

                    if (labels.contains(entry.getKey())) {
                        y = 1.0;
                    } else {
                        y = 0.0;
                    }

                    double val = lambda_t * (y - p[tag]);

                    for (int id : feats) {
                        beta[tag][id] += val;
                    }
                }

                Arrays.fill(p, 0.0);
            }
        }
        br.close();

        // auto evaluation part
        br = new BufferedReader(new InputStreamReader(new FileInputStream(
                testset)));
        double[] z = new double[14];
        int correct_sample = 0, all_sample = 0;

        while ((line = br.readLine()) != null) {
            tokenizeDoc(line, feats, labels);
            for (int feat : feats) {
                for (int i = 0; i < z.length; i++) {
                    z[i] += beta[i][feat];
                }
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                double prob = sigmoid(z[entry.getValue()]);
                String key = entry.getKey();
                if ((prob >= 0.5 && labels.contains(key))
                        || (prob < 0.5 && !labels.contains(key))) {
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
            Arrays.fill(z, 0.0);
        }
        br.close();

        System.out.println("Accuracy: "
                + ((double) correct_sample / all_sample));
    }

    private static double sigmoid(double z) {
        return 1.0 - 1.0 / (1.0 + Math.exp(z));
    }

    private static double overflow = 20;

    protected static double sigmoid_o(double score) {
        if (score > overflow)
            score = overflow;
        else if (score < -overflow)
            score = -overflow;
        double exp = Math.exp(score);
        return exp / (1 + exp);
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
