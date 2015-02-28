import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * @author jiacli
 *
 */
public class run {

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
        job.setJarByClass(NB_train_hadoop.class);
        job.setNumReduceTasks(Integer.parseInt(args[2]));

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(NB_train_hadoop.NBTrainMap.class);
        job.setReducerClass(NB_train_hadoop.NBTrainReduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }

}
