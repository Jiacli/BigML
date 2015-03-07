import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
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
public class CountSize {

    public static class CountSizeMap extends Mapper<Object, Text, Text, LongWritable> {

        private Text outKey = new Text();
        private final LongWritable one = new LongWritable(1);

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // separate uni/bi-gram and counts
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                return;
            }
            
            String[] part = seg[1].split(" ");
            long Cx = Long.parseLong(part[0]);
            long Bx = Long.parseLong(part[1]);
            
            String type = null;
            // check the gram type
            if (seg[0].contains(" ")) { // bigram
                type = "b";
                outKey.set("**");
                context.write(outKey, one);
            } else {
                type = "u";
                outKey.set("*");
                context.write(outKey, one);
            }
            
            // output 4 keys ub/uc/bb/bc
            outKey.set(type + "c");
            context.write(outKey, new LongWritable(Cx));
            outKey.set(type + "b");
            context.write(outKey, new LongWritable(Bx));
        }
    }

    public static class CountSizeReduce extends Reducer<Text, LongWritable, Text, LongWritable> {

        public void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {

            long sum = 0;
            
            for (LongWritable val : values) {
                sum += val.get();
            }
            
            context.write(key, new LongWritable(sum));
        }
    }
}
