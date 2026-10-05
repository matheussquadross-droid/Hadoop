package pjbl01;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/** Combiner de (soma, contagem) por ano. Entrada e saída têm o mesmo tipo. */
public class CombinerSomaCont
        extends Reducer<IntWritable, SomaContWritable, IntWritable, SomaContWritable> {

    @Override
    public void reduce(IntWritable key, Iterable<SomaContWritable> values, Context con)
            throws IOException, InterruptedException {
        double soma = 0;
        long cont = 0;
        for (SomaContWritable v : values) {
            soma += v.getSoma();
            cont += v.getCont();
        }
        con.write(key, new SomaContWritable(soma, cont));
    }
}
