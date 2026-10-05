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

/* QUESTAO 02 Número de transações por ano*/

public class Q2_TransacoesPorAno {

    public static void main(String[] args) throws Exception {

        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);

        Job j = Job.getInstance(c, "Q2_TransacoesPorAno");

        j.setJarByClass(Q2_TransacoesPorAno.class);
        j.setMapperClass(Map.class);
        j.setCombinerClass(SomaInteirosAno.class);
        j.setReducerClass(SomaInteirosAno.class);

        j.setMapOutputKeyClass(IntWritable.class);
        j.setMapOutputValueClass(IntWritable.class);
        j.setOutputKeyClass(IntWritable.class);
        j.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(j, input);
        FileOutputFormat.setOutputPath(j, output);

        System.exit(j.waitForCompletion(true) ? 0 : 1);
    }

    public static class Map extends Mapper<LongWritable, Text, IntWritable, IntWritable> {

        private static final IntWritable UM = new IntWritable(1);

        @Override
        public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException {

            Transacao t = Transacao.parse(value.toString());
            if(t == null || !(t.temAno())) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(new IntWritable(t.ano), UM);
        }
    }
}

