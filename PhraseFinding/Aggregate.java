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
        // Using regular expression to parse the data
        Pattern pattern = null;
        if ("0".equals(args[0])) {
            pattern = Pattern.compile("(\\w+)\\s(\\d+)\\s(\\d+)");
        } else if ("1".equals(args[1])) {
            pattern = Pattern.compile("(\\w+\\s\\w+)\\s(\\d+)\\s(\\d+)");
        } else {
            System.err.print("Unknown parameter!");
            System.err.println("Usage: <option 0/1 for uni/bi-gram>");
            System.exit(-1);
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line, lastGram = "", lastYear = "";
        while ((line = br.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String 
                int year = Integer.parseInt(matcher.group(2));
                long count = Long.parseLong(matcher.group(3));
            }
        }

    }
    
    private void parseUnigramInput(BufferedReader br) throws IOException {
        
    }

}
