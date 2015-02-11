import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 */

/**
 * @author Jiachen
 *
 */
public class MessageUnigramCombiner {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line, unigram = "";
        long Cx = 0, Bx = 0;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            // input data format:
            // unigram Cx Bx or
            // unigram * another
            // unigram another *
            String[] seg = line.split(" ");
            if ("*".equals(seg[0])) {
                System.out.println("& & " + seg[1] + " " + seg[2]);
                continue;
            }
            
            if (!unigram.equals(seg[0])) {
                unigram = seg[0];
                Cx = Long.parseLong(seg[1]);
                Bx = Long.parseLong(seg[2]);
            } else {
                if ("@".equals(seg[1])) {
                    System.out.println(String.format("%s %s x%d %d", unigram,
                            seg[2], Cx, Bx));
                } else {
                    System.out.println(String.format("%s %s y%d %d", seg[1],
                            unigram, Cx, Bx));
                }
            }
        }
        br.close();
    }

}
