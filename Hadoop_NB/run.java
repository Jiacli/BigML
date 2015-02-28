import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * @author jiacli
 *
 */
public class run {

    public static class NBTrainMap extends
            Mapper<Object, Text, Text, IntWritable> {

        private Text outKey = new Text();
        private final static IntWritable one = new IntWritable(1);

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // extract label
            String[] seg = line.trim().split("\t");
            if (seg.length != 2) {
                return;
            }

            ArrayList<String> labels = new ArrayList<String>();

            String[] tags = seg[0].trim().split(",");
            for (String tag : tags) {
                if (tag.endsWith("CAT")) {
                    labels.add(tag);
                    outKey.set("Y=" + tag);
                    context.write(outKey, one);
                }
            }
            outKey.set("Y=*");
            context.write(outKey, new IntWritable(labels.size()));

            // tokenize and map
            ArrayList<String> feats = tokenizeDoc(seg[1]);
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (String feat : feats) {
                if (map.containsKey(feat)) {
                    map.put(feat, map.get(feat) + 1);
                } else {
                    map.put(feat, 1);
                }
            }
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);

                outKey.set("Y=" + label + ",W=*");
                context.write(outKey, new IntWritable(feats.size()));

                for (String feat : map.keySet()) {
                    outKey.set("Y=" + label + ",W=" + feat);
                    context.write(outKey, new IntWritable(map.get(feat)));
                }
            }
        }

        // simple tokenizer
        private static ArrayList<String> tokenizeDoc(String cur_doc) {
            String[] words = cur_doc.split("\\s+");
            ArrayList<String> tokens = new ArrayList<>();
            for (int i = 0; i < words.length; i++) {
                words[i] = words[i].replaceAll("\\W", "");
                if (words[i].length() > 0) {
                    tokens.add(words[i]);
                }
            }
            return tokens;
        }
    }

    public static class NBTrainReduce extends
            Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {

            int sum = 0;

            for (IntWritable val : values) {
                sum += val.get();
            }

            context.write(key, new IntWritable(sum));
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out
                    .println("Usage: <Input Path> <Output Path> <# of Reduce job>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        conf.set("mapred.textoutputformat.separator", "\t");

        Job job = new Job(conf, "Naive Bayes Training");
        job.setJarByClass(run.class);
        job.setNumReduceTasks(Integer.parseInt(args[2]));

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(NBTrainMap.class);
        job.setReducerClass(NBTrainReduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }

}
