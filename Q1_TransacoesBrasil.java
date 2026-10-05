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

/* QUESTAO 01 - número de transações envolvendo o Brasil */

public class Q1_TransacoesBrasil {

    public static void main(String[] args) throws Exception {
        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);

        Job j = Job.getInstance(c, "Q1_TransacoesBrasil");

        j.setJarByClass(Q1_TransacoesBrasil.class);
        j.setMapperClass(Map.class);
        j.setCombinerClass(SomaInteiros.class);
        j.setReducerClass(SomaInteiros.class);

        j.setMapOutputKeyClass(Text.class);
        j.setMapOutputValueClass(IntWritable.class);
        j.setOutputKeyClass(Text.class);
        j.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(j, input);
        FileOutputFormat.setOutputPath(j, output);

        System.exit(j.waitForCompletion(true) ? 0 : 1);
    }

    public static class Map  extends Mapper<LongWritable, Text, Text, IntWritable > {

        private static final IntWritable UM = new IntWritable(1);

        @Override
        public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException {

            Transacao t = Transacao.parse(value.toString());
            if(t == null || !(t.temPais() && t.ehBrasil())) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(new Text(Transacao.BRASIL), UM);
        }
    }
}