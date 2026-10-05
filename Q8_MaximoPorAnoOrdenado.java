package pjbl01;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.Locale;

/**
 * Questão 8 - valor máximo das transações por ano no Brasil, ordenado do maior para o menor.
 * Concatenação de dois Jobs:
 *   Job 1: (ano -> maior preço)            [saída temporária em SequenceFile]
 *   Job 2: inverte para (maior preço -> ano) e ordena de forma decrescente no shuffle.
 */
public class Q8_MaximoPorAnoOrdenado {

    public static void main(String[] args) throws Exception {
        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);
        Path intermediario = new Path(files[1] + "_job1_tmp");

        // ---------------- Job 1: máximo por ano ----------------
        Job j1 = Job.getInstance(c, "Q8_job1_maximo_por_ano");
        j1.setJarByClass(Q8_MaximoPorAnoOrdenado.class);
        j1.setMapperClass(Map1.class);
        j1.setCombinerClass(Max1.class);
        j1.setReducerClass(Max1.class);

        j1.setMapOutputKeyClass(IntWritable.class);
        j1.setMapOutputValueClass(DoubleWritable.class);
        j1.setOutputKeyClass(IntWritable.class);
        j1.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(j1, input);
        j1.setOutputFormatClass(SequenceFileOutputFormat.class);
        FileOutputFormat.setOutputPath(j1, intermediario);

        if (!j1.waitForCompletion(true)) System.exit(1);

        // ---------------- Job 2: ordenação decrescente ----------------
        Job j2 = Job.getInstance(c, "Q8_job2_ordenacao");
        j2.setJarByClass(Q8_MaximoPorAnoOrdenado.class);
        j2.setMapperClass(Map2.class);
        j2.setReducerClass(Reduce2.class);
        j2.setSortComparatorClass(DescendingDoubleComparator.class);
        j2.setNumReduceTasks(1); // ordem global

        j2.setMapOutputKeyClass(DoubleWritable.class);
        j2.setMapOutputValueClass(IntWritable.class);
        j2.setOutputKeyClass(IntWritable.class);
        j2.setOutputValueClass(Text.class);

        j2.setInputFormatClass(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(j2, intermediario);
        FileOutputFormat.setOutputPath(j2, output);

        boolean ok = j2.waitForCompletion(true);

        FileSystem.get(c).delete(intermediario, true); // limpa a saída intermediária
        System.exit(ok ? 0 : 1);
    }

    // ===== Job 1 =====
    public static class Map1 extends Mapper<LongWritable, Text, IntWritable, DoubleWritable> {
        @Override
        public void map(LongWritable key, Text value, Context con)
                throws IOException, InterruptedException {
            Transacao t = Transacao.parse(value.toString());
            if (t == null || !t.temAno() || !t.temPreco() || !t.ehBrasil()) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(new IntWritable(t.ano), new DoubleWritable(t.preco));
        }
    }

    /** Máximo por ano. Serve como Combiner e Reducer do Job 1. */
    public static class Max1 extends Reducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable> {
        @Override
        public void reduce(IntWritable key, Iterable<DoubleWritable> values, Context con)
                throws IOException, InterruptedException {
            double max = Double.NEGATIVE_INFINITY;
            for (DoubleWritable v : values) {
                max = Math.max(max, v.get());
            }
            con.write(key, new DoubleWritable(max));
        }
    }

    // ===== Job 2 =====
    public static class Map2 extends Mapper<IntWritable, DoubleWritable, DoubleWritable, IntWritable> {
        @Override
        public void map(IntWritable ano, DoubleWritable max, Context con)
                throws IOException, InterruptedException {
            con.write(max, ano); // o valor vira chave para ser ordenado no shuffle
        }
    }

    public static class Reduce2 extends Reducer<DoubleWritable, IntWritable, IntWritable, Text> {
        @Override
        public void reduce(DoubleWritable max, Iterable<IntWritable> anos, Context con)
                throws IOException, InterruptedException {
            Text valor = new Text(String.format(Locale.US, "%.2f", max.get()));
            for (IntWritable ano : anos) { // anos com o mesmo máximo (empate)
                con.write(new IntWritable(ano.get()), valor);
            }
        }
    }

    /** Inverte a ordem natural do DoubleWritable: maior primeiro. */
    public static class DescendingDoubleComparator extends WritableComparator {
        public DescendingDoubleComparator() {
            super(DoubleWritable.class, true);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public int compare(WritableComparable a, WritableComparable b) {
            return -a.compareTo(b);
        }
    }
}
