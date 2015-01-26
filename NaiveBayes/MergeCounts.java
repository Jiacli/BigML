import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Small Memory Footprint Streaming Naive Bayes
 * - Merge counts
 * 
 * @author JK
 * 
 */
public class MergeCounts {

    public static void main(String[] args) throws IOException {
        
        int wordCount = 0, sum = 0;
        String lastLine = "";
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split("\t");
            if (seg.length == 1) {
                wordCount++;
                continue;
            }
            
            if (seg[0].equals(lastLine)) {
                sum += Integer.parseInt(seg[1]);
            } else {
                if (sum > 0) {
                    System.out.println(lastLine.replaceFirst(",", " ") + " " + sum);
                }
                lastLine = seg[0];
                sum = Integer.parseInt(seg[1]);
            }
        }
        
        if (sum > 0) {
            System.out.println(lastLine.replaceFirst(",", " ") + " " + sum);
        }
        
        System.out.println("# " + wordCount);
    }

}
