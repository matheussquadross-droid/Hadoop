package pjbl01;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/** Soma inteiros por chave IntWritable (ano). Reducer e Combiner da Questão 2. */

public class SomaInteirosAno extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context con) throws IOException, InterruptedException {

        int soma = 0; 

        for(IntWritable v : values){
            soma += v.get();
        }

        con.write(key, new IntWritable(soma));
    }
}