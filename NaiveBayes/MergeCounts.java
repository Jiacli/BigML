import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Small Memory Footprint Streaming Naive Bayes
 * - Merge counts
 * 
 * @author JK
 * 
 */
public class MergeCounts {
    
    static final int BUFFSIZE = 5000;

    public static void main(String[] args) throws IOException {
        List<String> buffer = new ArrayList<>();
        
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
                    buffer.add(lastLine.replaceFirst(",", " ") + " " + sum);
                    if (buffer.size() > BUFFSIZE) {
                        for (String str : buffer) {
                            System.out.println(str);
                        }
                        buffer.clear();
                    }                    
                }
                lastLine = seg[0];
                sum = Integer.parseInt(seg[1]);
            }
        }
        if (sum > 0) {
            buffer.add(lastLine.replaceFirst(",", " ") + " " + sum);
        }
        for (String str : buffer) {
            System.out.println(str);
        }
        System.out.println("# " + wordCount);
        buffer.clear();
    }
}
