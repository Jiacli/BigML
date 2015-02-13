import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;

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
        
        HashSet<String> stopwords = loadStopWords("stopword.list");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        String line, lastGram = "";
        long Bx = 0, Cx = 0, sumCx = 0, sumBx = 0;
        double hf = 0.01, hf_Cx = 0;
        while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            
            String[] seg = line.split("\t");
            // check stopwords
            if (isUnigram) {
                if (stopwords.contains(seg[0])) {
                    continue;
                }
            } else {
                String[] words = seg[0].split(" ");
                if (stopwords.contains(words[0]) || stopwords.contains(words[1])) {
                    continue;
                }
            }
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
                    if (!isUnigram) {
                        if (Cx <= hf_Cx * hf) {
                            sumCx += Cx;
                            sumBx += Bx;
                            lastGram = seg[0];
                            if (year < 1970) {
                                Cx = count;
                                Bx = 0;
                            } else {
                                Bx = count;
                                Cx = 0;
                            }
                            continue;
                        }
                        if (Cx >= hf_Cx) {
                            hf_Cx = Cx;
                        }
                    }
                    
                    bw.write(lastGram + " " + Cx + " " + Bx + "\n");
                    
                    //bw.write(String.format("%s %d %d\n", lastGram, Cx, Bx));
                    //System.out.println(String.format("%s %d %d", lastGram, Cx, Bx));
                    sumCx += Cx;
                    sumBx += Bx;
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
        stopwords.clear();
        
        if (lastGram.length() > 0) {
            bw.write(lastGram + " " + Cx + " " + Bx + "\n");
            //bw.write(String.format("%s %d %d\n", lastGram, Cx, Bx));
            //System.out.println(String.format("%s %d %d", lastGram, Cx, Bx));
            sumCx += Cx;
            sumBx += Bx;
        }
        
        if (isUnigram) {
            bw.write("* " + sumCx + " " + sumBx + "\n");
            //bw.write(String.format("* %d %d\n", sumCx, sumBx));
            //System.out.println(String.format("* %d %d", sumCx, sumBx));
        } else {
            bw.write("* * " + sumCx + " " + sumBx + "\n");
            //bw.write(String.format("* * %d %d\n", sumCx, sumBx));
            //System.out.println(String.format("* * %d %d", sumCx, sumBx));
        }
        bw.flush();
        bw.close();
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
