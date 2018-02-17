package caiquecoelho.com.gasolina.model;

/**
 * Created by caique_coelho on 06/01/18.
 */

public class Carro {

    private String marca;
    private String apelido;

    public Carro(){
        apelido = "Não informado";
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }
}
