import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 */

/**
 * @author Jiachen
 *
 */

class Phrase {
    String phrase;
    double phraseness;
    double informativeness;
    public Phrase(String phrase, double phraseness, double informativeness) {
        this.phrase = phrase;
        this.phraseness = phraseness;
        this.informativeness = informativeness;
    }
}

public class PhraseGenerator {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        TreeMap<Double, Phrase> map = new TreeMap<>();
        
        String line;
        boolean hasX = false, hasY = false, hasXY = false;
        long Cxy = 0, Bxy = 0, Cx = 0, Cy = 0;
        long sumCxy = 0, sumBxy = 0, sumCx = 0/*, sumBx = 0*/;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            // input data format:
            // bigram-1 bigram-2 Cxy Bxy
            // bigram-1 bigram-2 xCx Bx
            // bigram-1 bigram-2 yCy By
            String[] seg = line.split(" ");
            
            if (seg[0].equals("*")) {
                sumCxy = Long.parseLong(seg[2]);
                sumBxy = Long.parseLong(seg[3]);
                continue;
            }
            if (seg[0].equals("&")) {
                sumCx = Long.parseLong(seg[2]);
                //sumBx = Long.parseLong(seg[3]);
                continue;
            }
            
            if (seg[2].startsWith("x")) {
                hasX = true;
                Cx = Long.parseLong(seg[2].substring(1));
                //Bx = Long.parseLong(seg[3]);
            } else if (seg[2].startsWith("y")) {
                hasY = true;
                Cy = Long.parseLong(seg[2].substring(1));
                //By = Long.parseLong(seg[3]);
            } else {
                hasXY = true;
                Cxy = Long.parseLong(seg[2]);
                Bxy = Long.parseLong(seg[3]);
            }
            
            if (hasY && hasX && hasXY) {
                // calculate scores
                double p = (double) Cxy / sumCxy;
                double q_f = ((double) Cx / sumCx) * ((double) Cy / sumCx);
                double q_b = (double) Bxy / sumBxy;
                double phraseness = KLDivergence(p, q_f);
                double informativeness = KLDivergence(p, q_b);
                double score = phraseness + informativeness;
                if (map.size() < 20) {
                    map.put(score, new Phrase(seg[0] + " " + seg[1],
                            phraseness, informativeness));
                } else {
                    if (score > map.firstKey()) {
                        map.pollFirstEntry();
                        map.put(score, new Phrase(seg[0] + " " + seg[1],
                                phraseness, informativeness));
                    }
                }
                hasY = false;
                hasX = false;
                hasXY = false;
            }
        }
        br.close();
        Runtime.getRuntime().gc();
        
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        for (double s : map.descendingKeySet()) {
            Phrase ph = map.get(s);
            bw.write(String.format("%s\t%f\t%f\t%f\n", ph.phrase, s, ph.phraseness, ph.informativeness));
            //System.out.println(ph.phrase + "\t" + s + "\t" + ph.phraseness + "\t" + ph.informativeness);
        }
        bw.flush();
        bw.close();
    }
    
    private static double KLDivergence(double p, double q) {
        if (p <= 0 || q <= 0) {
            return -Double.MAX_VALUE;
        }
        return p * Math.log(p / q);        
    }

}
