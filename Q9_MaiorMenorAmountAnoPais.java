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

/**
 * Questão 9 - transação com maior e menor 'amount' por ano e país.
 * Chave: AnoPaisWritable (WritableComparable). Valor: MinMaxWritable. Usa Combiner.
 */
public class Q9_MaiorMenorAmountAnoPais {

    public static void main(String[] args) throws Exception {
        Configuration c = new Configuration();
        String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
        Path input = new Path(files[0]);
        Path output = new Path(files[1]);

        Job j = Job.getInstance(c, "Q9_MaiorMenorAmountAnoPais");

        // 1. classes
        j.setJarByClass(Q9_MaiorMenorAmountAnoPais.class);
        j.setMapperClass(Map.class);
        j.setCombinerClass(MinMaxReduce.class);
        j.setReducerClass(MinMaxReduce.class);

        // 2. tipos de saída (map e reduce emitem os mesmos tipos)
        j.setMapOutputKeyClass(AnoPaisWritable.class);
        j.setMapOutputValueClass(MinMaxWritable.class);
        j.setOutputKeyClass(AnoPaisWritable.class);
        j.setOutputValueClass(MinMaxWritable.class);

        // 3. entrada/saída
        FileInputFormat.addInputPath(j, input);
        FileOutputFormat.setOutputPath(j, output);

        System.exit(j.waitForCompletion(true) ? 0 : 1);
    }

    public static class Map extends Mapper<LongWritable, Text, AnoPaisWritable, MinMaxWritable> {
        @Override
        public void map(LongWritable key, Text value, Context con)
                throws IOException, InterruptedException {
            Transacao t = Transacao.parse(value.toString());
            // descarta cabeçalho e linhas sem país, ano ou amount
            if (t == null || !t.temPais() || !t.temAno() || !t.temQuantidade()) return;
            if (t.codigo.equalsIgnoreCase("TOTAL")) return;
            con.write(new AnoPaisWritable(t.ano, t.pais), new MinMaxWritable(t.quantidade, t.commodity));
        }
    }

    /** Reducer e Combiner (mesmos tipos de entrada e saída). */
    public static class MinMaxReduce
            extends Reducer<AnoPaisWritable, MinMaxWritable, AnoPaisWritable, MinMaxWritable> {
        @Override
        public void reduce(AnoPaisWritable key, Iterable<MinMaxWritable> values, Context con)
                throws IOException, InterruptedException {
            MinMaxWritable acc = null;
            for (MinMaxWritable v : values) {
                if (acc == null) acc = new MinMaxWritable(v); // cópia, pois 'v' é reutilizado
                
                else acc.merge(v);
            }
            if (acc != null) con.write(key, acc);
        }
    }
}
