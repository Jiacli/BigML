import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

        String line, unigram = "";
        long Cx = 0/*, Bx = 0*/;
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
                bw.write("& & " + seg[1] + " " + seg[2] + "\n");
                //System.out.println("& & " + seg[1] + " " + seg[2]);
                continue;
            }
            
            if (!unigram.equals(seg[0])) {
                unigram = seg[0];
                Cx = Long.parseLong(seg[1]);
                //Bx = Long.parseLong(seg[2]);
            } else {
                if ("@".equals(seg[1])) {
                    bw.write(unigram + " " + seg[2] + " x" + Cx + "\n"); 
                    //bw.write(String.format("%s %s x%d\n", unigram, seg[2], Cx));
                    //System.out.println(String.format("%s %s x%d %d", unigram, seg[2], Cx, Bx));
                } else {
                    bw.write(seg[1] + " " + unigram + " y" + Cx + "\n");
                    //bw.write(String.format("%s %s y%d\n", seg[1], unigram, Cx));
                    //System.out.println(String.format("%s %s y%d %d", seg[1], unigram, Cx, Bx));
                }
            }
        }
        br.close();
        Runtime.getRuntime().gc();
        bw.flush();
        bw.close();
        Runtime.getRuntime().gc();
    }

}
