import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


/**
 * @author jiacli
 *
 */
public class Aggregate {

    public static class AggregateMap extends Mapper<Object, Text, Text, Text> {

        private Text outKey = new Text();
        private Text outVal = new Text();
        private HashSet<String> swlist = new HashSet<String>();
        
        public AggregateMap() {
            String[] stopwords = "i,the,to,and,a,an,of,it,you,that,in,my,is,was,for"
                    .split(",");
            for (String token : stopwords) {
                swlist.add(token);
            }
        }

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // extract label
            String[] seg = line.trim().split("\t");
            if (seg.length != 3) {
                return;
            }
            
            // remove stop words first
            String[] words = seg[0].split(" ");
            for (String word : words) {
                if (swlist.contains(word)) {
                    return;
                }
            }
            
            // check the year
            int year = Integer.parseInt(seg[1]);
            
            if (year < 1970) {
                outVal.set("C " + seg[2]); // foreground corpus
            } else {
                outVal.set("B " + seg[2]); // background corpus
            }
            outKey.set(seg[0]);
            context.write(outKey, outVal);
        }
    }

    public static class AggregateReduce extends
            Reducer<Text, Text, Text, Text> {
        
        private Text outVal = new Text();

        public void reduce(Text key, Iterable<Text> values,
                Context context) throws IOException, InterruptedException {
            
            long Cx = 0, Bx = 0;
            for (Text val : values) {
                String[] seg = val.toString().split(" ");
                long count = Long.parseLong(seg[1]);
                if (seg[0].equals("C")) {
                    Cx += count;
                } else {
                    Bx += count;
                }
            }
            outVal.set(Cx + " " + Bx);

            context.write(key, outVal);
        }
    }
}
