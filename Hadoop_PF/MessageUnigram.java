import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 
 */

/**
 * @author jiacli
 *
 */
public class MessageUnigram {
    public static class MessageUnigramMap extends
            Mapper<Object, Text, Text, Text> {

        private Text outKey = new Text();
        private Text outVal = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // separate uni/bi-gram and counts
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                return;
            }

            // check the gram type
            if (seg[0].contains(" ")) { // bigram
                String[] token = seg[0].split(" ");
                outKey.set(token[0]);
                outVal.set("* " + token[1]);
                context.write(outKey, outVal);

                outKey.set(token[1]);
                outVal.set(token[0] + " *");
                context.write(outKey, outVal);
                // bigram1 \t * bigram2
                // bigram2 \t bigram1 *
                
                // original bigram
                outKey.set(seg[0]);
                outVal.set(seg[1]);
            } else {
                String[] ct = seg[1].split(" ");
                outKey.set(seg[0]);
                outVal.set(ct[0]);
                // unigram \t Cx
            }
            context.write(outKey, outVal);
        }
    }

    public static class MessageUnigramReduce extends
            Reducer<Text, Text, Text, Text> {

        private Text outKey = new Text();
        private Text outVal = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            String keyStr = key.toString();
            if (keyStr.contains(" ")) { // bigram
                for (Text val : values) {
                    context.write(key, val);
                }
                return;
            }
            
            ArrayList<String> list = new ArrayList<String>();
            String Cx = null;
            
            for (Text val : values) {
                String str = val.toString();
                if (str.startsWith("*") || str.endsWith("*")) {
                    list.add(str);
                } else {
                    Cx = str;
                }
            }

            for (String str : list) {
                String[] token = str.split(" ");
                if ("*".equals(token[0])) {
                    outKey.set(keyStr + " " + token[1]);
                    outVal.set("x " + Cx);
                } else if ("*".equals(token[1])) {
                    outKey.set(token[0] + " " + keyStr);
                    outVal.set("y " + Cx);
                }
                context.write(outKey, outVal);
            }
            
            list.clear();
        }
    }
}
