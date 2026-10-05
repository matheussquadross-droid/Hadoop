package pjbl01;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/** Questão 6 - transação mais cara e mais barata do Brasil em 2016 (Combiner + writable customizado). */
public class Q6_MaisCaraMaisBarata2016 {

    public static void main(String[] args) throws Exception {
        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);

        Job j = Job.getInstance(c, "Q6_MaisCaraMaisBarata2016");

        // 1. classes
        j.setJarByClass(Q6_MaisCaraMaisBarata2016.class);
        j.setMapperClass(Map.class);
        j.setCombinerClass(Combine.class);
        j.setReducerClass(Reduce.class);

        // 2. tipos de saída
        j.setMapOutputKeyClass(Text.class);
        j.setMapOutputValueClass(MinMaxWritable.class);
        j.setOutputKeyClass(Text.class);
        j.setOutputValueClass(MinMaxWritable.class);

        // 3. entrada/saída
        FileInputFormat.addInputPath(j, input);
        FileOutputFormat.setOutputPath(j, output);

        System.exit(j.waitForCompletion(true) ? 0 : 1);
    }

    public static class Map extends Mapper<LongWritable, Text, Text, MinMaxWritable> {
        private static final Text CHAVE = new Text(Transacao.BRASIL);

        @Override
        public void map(LongWritable key, Text value, Context con)
                throws IOException, InterruptedException {
            Transacao t = Transacao.parse(value.toString());
            if (t == null || !t.temAno() || !t.temPreco()) return;
            if (!t.ehBrasil() || t.ano != 2016) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(CHAVE, new MinMaxWritable(t.preco, t.commodity));
        }
    }

    /** Combiner: reduz localmente os pares (min, max) antes do shuffle. */
    public static class Combine extends Reducer<Text, MinMaxWritable, Text, MinMaxWritable> {
        @Override
        public void reduce(Text key, Iterable<MinMaxWritable> values, Context con)
                throws IOException, InterruptedException {
            MinMaxWritable acc = null;
            for (MinMaxWritable v : values) {
                if (acc == null) acc = new MinMaxWritable(v); // cópia: o Hadoop reutiliza 'v'
                else acc.merge(v);
            }
            if (acc != null) con.write(key, acc);
        }
    }

    public static class Reduce extends Reducer<Text, MinMaxWritable, Text, MinMaxWritable> {
        @Override
        public void reduce(Text key, Iterable<MinMaxWritable> values, Context con)
                throws IOException, InterruptedException {
            MinMaxWritable acc = null;
            for (MinMaxWritable v : values) {
                if (acc == null) acc = new MinMaxWritable(v);
                else acc.merge(v);
            }
            if (acc != null) con.write(key, acc);
        }
    }
}
