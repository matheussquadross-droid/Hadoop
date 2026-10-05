package pjbl01;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Writable customizado: menor e maior valor, cada um com a descrição da commodity
 * da transação correspondente.
 */
public class MinMaxWritable implements Writable {

    private double min;
    private Text minCommodity = new Text();
    private double max;
    private Text maxCommodity = new Text();

    public MinMaxWritable() {
    }

    /** Uma única transação: ela é, ao mesmo tempo, o menor e o maior valor. */
    public MinMaxWritable(double valor, String commodity) {
        this.min = valor;
        this.minCommodity.set(commodity);
        this.max = valor;
        this.maxCommodity.set(commodity);
    }

    /** Construtor de cópia (o Hadoop reaproveita os objetos do iterador do reduce). */
    public MinMaxWritable(MinMaxWritable o) {
        this.min = o.min;
        this.minCommodity.set(o.minCommodity);
        this.max = o.max;
        this.maxCommodity.set(o.maxCommodity);
    }

    /** Incorpora outro registro, mantendo os extremos. */
    public void merge(MinMaxWritable o) {
        if (o.min < min) {
            min = o.min;
            minCommodity.set(o.minCommodity);
        }
        if (o.max > max) {
            max = o.max;
            maxCommodity.set(o.maxCommodity);
        }
    }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }
    public Text getMinCommodity() { return minCommodity; }
    public void setMinCommodity(Text t) { this.minCommodity = t; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
    public Text getMaxCommodity() { return maxCommodity; }
    public void setMaxCommodity(Text t) { this.maxCommodity = t; }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(min);
        minCommodity.write(out);
        out.writeDouble(max);
        maxCommodity.write(out);
    }

    // Mesma ordem do write()
    @Override
    public void readFields(DataInput in) throws IOException {
        min = in.readDouble();
        minCommodity.readFields(in);
        max = in.readDouble();
        maxCommodity.readFields(in);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US,
                "menor=%.2f (%s)\tmaior=%.2f (%s)",
                min, minCommodity, max, maxCommodity);
    }
}
