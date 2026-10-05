package pjbl01;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Chave composta (ano, país) sem concatenar strings.
 * Ordena por ano e depois por país.
 */
public class AnoPaisWritable implements WritableComparable<AnoPaisWritable> {

    private int ano;
    private Text pais = new Text();

    public AnoPaisWritable() {
    }

    public AnoPaisWritable(int ano, String pais) {
        this.ano = ano;
        this.pais.set(pais);
    }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public Text getPais() { return pais; }
    public void setPais(Text pais) { this.pais = pais; }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(ano);
        pais.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        ano = in.readInt();
        pais.readFields(in);
    }

    @Override
    public int compareTo(AnoPaisWritable o) {
        int c = Integer.compare(ano, o.ano);
        if (c != 0) return c;
        return pais.compareTo(o.pais);
    }

    // hashCode estável: usado pelo HashPartitioner para distribuir as chaves
    @Override
    public int hashCode() {
        return 31 * ano + pais.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AnoPaisWritable)) return false;
        AnoPaisWritable o = (AnoPaisWritable) obj;
        return ano == o.ano && pais.equals(o.pais);
    }

    @Override
    public String toString() {
        return ano + "\t" + pais;
    }
}
