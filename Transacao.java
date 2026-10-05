package pjbl01;



public class Transacao {
    public static final int COLUNAS = 10;
    public static final String BRASIL = "BRAZIL";

    public String pais = "";
    public int ano = 1;
    public String codigo = "";
    public String commodity = "";
    public String fluxo = "";
    public double preco = Double.NaN;
    public double peso = Double.NaN;
    public String unidade = "";
    public double quantidade = Double.NaN;
    public String categoria = "";


    public static Transacao parse(String linha) {

        if(linha == null || linha.trim().isEmpty()) return null;

        String[] c = linha.split(";", -1); // -1 mantém colunas vazias no final
        if(c.length != COLUNAS) return null;
        if(c[0].trim().equalsIgnoreCase("country")) return null; // cabeçalho

        Transacao t = new Transacao();
        t.pais = c[0].trim();
        t.ano = inteiro(c[1]);
        t.codigo = c[2].trim();
        t.commodity = c[3].trim();
        t.fluxo = c[4].trim();
        t.preco = decimal(c[5]);
        t.peso = decimal(c[6]);
        t.unidade = c[7].trim();
        t.quantidade = decimal(c[8]);
        t.categoria = c[9].trim();
        return t;
    }

    public boolean temPais()    {return !pais.isEmpty();}
    public boolean temAno()     {return ano > 0;}
    public boolean temFluxo()   {return !fluxo.isEmpty();}
    public boolean temCategoria() {return !categoria.isEmpty();}
    public boolean temPreco()   {return !Double.isNaN(preco);}
    public boolean temQuantidade() {return !Double.isNaN(quantidade);}

    public boolean ehBrasil() {return pais.equalsIgnoreCase(BRASIL);}
    public boolean ehExport() {return fluxo.equalsIgnoreCase("Export");}

    private static int inteiro(String s){
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double decimal(String s){
        try {
            String v = s.trim();
            if(v.isEmpty()) return Double.NaN;
            double d = Double.parseDouble(v);
            if(Double.isInfinite(d)) {
                return Double.NaN;
            } else {
                return d;
            }
        } catch (NumberFormatException e){
            return Double.NaN;
        }
    }


}
