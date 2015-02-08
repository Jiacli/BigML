import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 
 */

/**
 * @author Jiachen
 *
 */
public class Aggregate {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: <option 0/1 for uni/bi-gram>");
            System.exit(-1);
        }
        boolean isUnigram = "0".equals(args[0]);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line, lastGram = "";
        long Bx = 0, Cx = 0;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split("\t");
            int year = Integer.parseInt(seg[1]);
            long count = Long.parseLong(seg[2]);
            
            if (lastGram.equals(seg[0])) {
                if (year < 1970) { // foreground
                    Cx += count;
                } else {
                    Bx += count;
                }
            } else {
                if (lastGram.length() > 0) {
                    System.out.println(String.format("%s %d %d", lastGram, Cx, Bx));
                }
                lastGram = seg[0];
                if (year < 1970) {
                    Cx = count;
                    Bx = 0;
                } else {
                    Bx = count;
                    Cx = 0;
                }
            }
        }
        br.close();
        
        if (lastGram.length() > 0) {
            System.out.println(String.format("%s %d %d", lastGram, Cx, Bx));
        }
        
        if (isUnigram) {
            
        }

    }
}
