import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
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
public class Compute {
    
    public static class ComputeMap extends Mapper<Object, Text, Text, Text> {

        private Text outKey = new Text();
        private Text outVal = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                return;
            }
            
            outKey.set(seg[0]);
            outVal.set(seg[1]);
            context.write(outKey, outVal);            
        }
    }

    public static class ComputeReduce extends Reducer<Text, Text, Text, Text> {
        
        private Text outVal = new Text();
        
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            
            // load parameters
            long sumCxy = 0, sumBxy = 0, sumCx = 0, ctUni = 0, ctBi = 0;
            Configuration conf = context.getConfiguration();
            sumCxy = conf.getLong("sumCxy", 0);
            sumBxy = conf.getLong("sumBxy", 0);
            sumCx = conf.getLong("sumCx", 0);
            ctUni = conf.getLong("ctUni", 0);
            ctBi = conf.getLong("ctBi", 0);
            
            boolean hasX = false, hasY = false, hasXY = false;
            long Cxy = 0, Bxy = 0, Cx = 0, Cy = 0;
            
            for (Text val : values) {
                String[] seg = val.toString().split(" ");
                if ("x".equals(seg[0])) {
                    Cx = Long.parseLong(seg[1]);
                    hasX = true;
                } else if ("y".equals(seg[0])) {
                    Cy = Long.parseLong(seg[1]);
                    hasY = true;
                } else {
                    Cxy = Long.parseLong(seg[0]);
                    Bxy = Long.parseLong(seg[1]);
                    hasXY = true;
                }
            }
            
            // compute the score
            if (hasX && hasY && hasXY) {
                double p = (Cxy + 1.0) / (sumCxy + ctBi);
                double q_f = ((Cx + 1.0) / (sumCx + ctUni)) * ((Cy + 1.0) / (sumCx + ctUni));
                double q_b = (Bxy + 1.0) / (sumBxy + ctBi);
                double phraseness = KLDivergence(p, q_f);
                double informativeness = KLDivergence(p, q_b);
                double score = phraseness + informativeness;
                
                outVal.set(score + "\t" + phraseness + "\t" + informativeness);
                context.write(key, outVal);
            }
        }
        
        private static double KLDivergence(double p, double q) {
            if (p <= 0 || q <= 0) {
                return -Double.MAX_VALUE;
            }
            return p * Math.log(p / q);        
        }
    }
}
//<code>Configuration conf = new Configuration();
//conf.set("test", "123");
//
//Job job = new Job(conf);</code>
// 
//And then you retrieve them in the mapper/reducer using the context argument of the mapper/reducer,
//<code>Configuration conf = context.getConfiguration();
//String param = conf.get("test");</code>