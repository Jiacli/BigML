import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
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
public class run_hadoop_phrase {

    /**
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException,
            ClassNotFoundException, InterruptedException {
        if (args.length > 6) {
            System.out
                    .println("Usage: <uni> <bi> <out/aggregated> <out/sizecount> <out/uni-msg> <out/final>");
            System.exit(-1);
        }
        
        // Aggregate
        Configuration conf = new Configuration();
        conf.set("mapred.textoutputformat.separator", "\t");
        //conf.set("mapred.reduce.slowstart.completed.maps", "1.0");

        Job job = new Job(conf, "PF: Aggregate");
        //job.setNumReduceTasks(nReducer);
        job.setJarByClass(Aggregate.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(Aggregate.AggregateMap.class);
        job.setReducerClass(Aggregate.AggregateReduce.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);
        System.out.println("Aggregate Finished!");
        
        // CountSize
        job = new Job(conf, "PF: CountSize");
        job.setJarByClass(CountSize.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        job.setMapperClass(CountSize.CountSizeMap.class);
        job.setReducerClass(CountSize.CountSizeReduce.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job, new Path(args[2]));
        FileOutputFormat.setOutputPath(job, new Path(args[3]));
        
        job.waitForCompletion(true);
        System.out.println("CountSize Finished!");
        // keys: ub/uc/bb/bc
        
        
        // MessageUnigram
        job = new Job(conf, "PF: MessageUnigram");
        job.setJarByClass(MessageUnigram.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(MessageUnigram.MessageUnigramMap.class);
        job.setReducerClass(MessageUnigram.MessageUnigramReduce.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job, new Path(args[2]));
        FileOutputFormat.setOutputPath(job, new Path(args[4]));
        
        job.waitForCompletion(true);
        System.out.println("MessageUnigram Finished!");
        

        // load count size parameters
        // keys: ub/uc/bb/bc
        long uniCxSum = 0, uniBxSum = 0, biCxSum = 0, biBxSum = 0;
        long ctUni = 0, ctBi = 0;
        FileSystem fs = FileSystem.get(new Configuration());
        FileStatus[] status = fs.listStatus(new Path(args[3]));
        for (int i = 0; i < status.length; i++) {
            if (!fs.isFile(status[i].getPath())) {
                continue;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
            String line;
            while ((line = br.readLine()) != null) {
                String[] seg = line.split("\t");
                if (seg.length != 2) {
                    continue;
                }
                long sum = Long.parseLong(seg[1]);
                if ("uc".equals(seg[0])) {
                    uniCxSum = sum;
                } else if ("ub".equals(seg[0])) {
                    uniBxSum = sum;
                } else if ("bc".equals(seg[0])) {
                    biCxSum = sum;
                } else if ("bb".equals(seg[0])) {
                    biBxSum = sum;
                } else if ("*".equals(seg[0])) {
                    ctUni = sum;
                } else if ("**".equals(seg[0])) {
                    ctBi = sum;
                }
            }
        }
        if (uniCxSum > 0 && uniBxSum > 0 && biCxSum > 0 && biBxSum > 0 && ctBi > 0 && ctUni > 0) {
            System.out.println("Count Load Succeed!");
        }
        
        // MessageUnigram
        conf = new Configuration();
        conf.set("mapred.textoutputformat.separator", "\t");
        conf.setLong("sumCxy", biCxSum);
        conf.setLong("sumBxy", biBxSum);
        conf.setLong("sumCx", uniCxSum);
        conf.setLong("ctUni", ctUni);
        conf.setLong("ctBi", ctBi);
        
        job = new Job(conf, "PF: Compute");
        job.setJarByClass(Compute.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(Compute.ComputeMap.class);
        job.setReducerClass(Compute.ComputeReduce.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job, new Path(args[4]));
        FileOutputFormat.setOutputPath(job, new Path(args[5]));
        
        job.waitForCompletion(true);
        System.out.println("Compute Finished!");
    }
}
