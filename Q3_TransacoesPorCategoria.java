/*

1. What is args?args (String[] args) is the standard Java parameter 
containing all raw command-line arguments passed to your application
when it is run from the terminal or cluster.When you run a Hadoop
job:Bashhadoop jar myjob.jar MyClass -D mapreduce.job.reduces=2 /user/input /user/output
args contains: ["-D", "mapreduce.job.reduces=2", "/user/input", "/user/output"]

2. What is files?files is an array containing only the positional arguments
(the actual file paths) after Hadoop's GenericOptionsParser strips out all 
Hadoop-specific options (like -D, -files, or -libjars).
In the example above:GenericOptionsParser(c, args) 
reads and applies -D mapreduce.job.reduces=2 to the Configuration 
c..getRemainingArgs() returns 
what is left over: ["/user/input", "/user/output"].Therefore:files[0] 
$\rightarrow$ "/user/input" (assigned to input)files[1] $\rightarrow$ "/user/output" 
(assigned to output)

*/

package pjbl01;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/** Questão 3 - número de transações por categoria */
public class Q3_TransacoesPorCategoria {

    public static void main(String[] args) throws Exception {

        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);

        Job j = Job.getInstance(c, "Q3_TransacoesPorCategoria");

        j.setJarByClass(Q3_TransacoesPorCategoria.class);
        j.setMapperClass(Map.class);
        j.setCombinerClass(SomaInteiros.class);
        j.setReducerClass(SomaInteiros.class);

        j.setMapOutputKeyClass(Text.class);
        j.setMapOutputValueClass(IntWritable.class);

        j.setOutputKeyClass(Text.class);
        j.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(j, input);
        FileOutputFormat.setOutputPath(j, output);

        System.exit(j.waitForCompletion(true) ? 0: 1);

    }

    public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
        private static final IntWritable UM = new IntWritable(1);

        @Override
        public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException {
            Transacao t = Transacao.parse(value.toString());
            if(t == null || !(t.temCategoria())) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(new Text(t.categoria), UM);
        }
    }
}