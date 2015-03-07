import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

/**
 * 
 */

/**
 * @author jiacli
 *
 */
public class CountSize {

    public static class CountSizeMap extends Mapper<Text, Text, Text, LongWritable> {

        private Text outKey = new Text();

        public void map(Text key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // extract label
            String[] seg = line.trim().split("\t");
            if (seg.length != 3) {
                return;
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

    public static class CountSizeReduce extends Reducer<Text, Text, Text, Text> {

        private Text outVal = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

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
