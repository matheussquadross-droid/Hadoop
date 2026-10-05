package pjbl01;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/** Soma inteiros por chave Text. Usado como Reducer e Combiner nas contagens. */

public class SomaInteiros extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context con) throws IOException, InterruptedException{

        int soma = 0;

        for(IntWritable v : values){
            soma += v.get();
        }

        con.write(key, new IntWritable(soma));
    }
}
