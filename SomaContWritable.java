package pjbl01;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Writable customizado (soma, contagem) para calcular médias de forma correta
 * mesmo com Combiner (média das médias parciais seria errada).
 */

public class SomaContWritable implements Writable {

    private double soma;
    private long cont;

    public SomaContWritable(){}

    public SomaContWritable(double soma, long cont){
        this.soma = soma;
        this.cont = cont;
    }

    public void setCont(long cont){
        this.cont = cont;
    }
    public long getCont(){return cont;}

        public void setSoma(double soma){
        this.soma = soma;
    }
    public double getSoma(){return soma;}

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(soma);
        out.writeLong(cont);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        soma = in.readDouble();
        cont = in.readLong();
    }

    @Override
    public String toString(){
        return soma + "\t" + cont;
    }
}