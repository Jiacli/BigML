import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jiachen
 *
 */
public class MessageGenerator {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split(" ");
            System.out.println(String.format("%s * %s", seg[0], seg[1]));
            System.out.println(String.format("%s %s *", seg[1], seg[0]));
        }
        br.close();
    }

}
