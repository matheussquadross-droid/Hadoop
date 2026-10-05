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
import java.util.Locale;

/** Questão 7 - valor médio por ano das exportações do Brasil */
public class Q7_MediaExportBrasil {

    public static void main(String[] args) throws Exception {
        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);

        Job j = Job.getInstance(c, "Q7_MediaExportBrasil");

        // 1. classes
        j.setJarByClass(Q7_MediaExportBrasil.class);
        j.setMapperClass(Map.class);
        j.setCombinerClass(CombinerSomaCont.class);
        j.setReducerClass(Reduce.class);

        // 2. tipos de saída (a saída do map difere da saída do reduce)
        j.setMapOutputKeyClass(IntWritable.class);
        j.setMapOutputValueClass(SomaContWritable.class);
        j.setOutputKeyClass(IntWritable.class);
        j.setOutputValueClass(Text.class);

        // 3. entrada/saída
        FileInputFormat.addInputPath(j, input);
        FileOutputFormat.setOutputPath(j, output);

        System.exit(j.waitForCompletion(true) ? 0 : 1);
    }

    public static class Map extends Mapper<LongWritable, Text, IntWritable, SomaContWritable> {
        @Override
        public void map(LongWritable key, Text value, Context con)
                throws IOException, InterruptedException {
            Transacao t = Transacao.parse(value.toString());
            if (t == null || !t.temAno() || !t.temPreco()) return; // cabeçalho / dado faltante
            if (!(t.ehBrasil() && t.ehExport())) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(new IntWritable(t.ano), new SomaContWritable(t.preco, 1));
        }
    }

    public static class Reduce extends Reducer<IntWritable, SomaContWritable, IntWritable, Text> {
        @Override
        public void reduce(IntWritable key, Iterable<SomaContWritable> values, Context con)
                throws IOException, InterruptedException {
            double soma = 0;
            long cont = 0;
            for (SomaContWritable v : values) {
                soma += v.getSoma();
                cont += v.getCont();
            }
            double media = soma / cont;
            con.write(key, new Text(String.format(Locale.US, "%.2f", media)));
        }
    }
}
