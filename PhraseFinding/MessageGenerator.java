import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;

/**
 * @author Jiachen
 *
 */
public class MessageGenerator {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        HashSet<String> stopwords = loadStopWords("stopword.list");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split(" ");
            
            if (stopwords.contains(seg[0]) || stopwords.contains(seg[1]) || seg[0].equals("*")) {
                continue;
            }
            
            bw.write(String.format("%s @ %s\n", seg[0], seg[1]));
            bw.write(String.format("%s %s @\n", seg[1], seg[0]));
            //System.out.println(String.format("%s @ %s", seg[0], seg[1]));
            //System.out.println(String.format("%s %s @", seg[1], seg[0]));
        }
        br.close();
        Runtime.getRuntime().gc();
        bw.flush();
        bw.close();
        Runtime.getRuntime().gc();
    }
    
    public static HashSet<String> loadStopWords(String filename)
            throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        HashSet<String> stopword = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() > 0) {
                stopword.add(line);
            }
        }
        br.close();
        return stopword;
    }

}
