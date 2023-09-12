package caiquecoelho.com.gasolina.model;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by caique_coelho on 06/01/18.
 */

public class Abastecimento {

    private String posto;
    private double preco;
    private String data;
    private String tipo;
    private double quantidade;
    private String carro;
    private String real;
    private double qtdLitroAbastecida;
    private String kms;
    private String timestamp;

    public Abastecimento(){
        posto = "Não informado";
        data = "15/02/1996";
        tipo = "Gasolina";
        real = "Litro";
        carro = "Não informado";
        kms = "";
    }

    public Abastecimento(String posto, double preco, String data, String tipo, double quantidade, String carro, String real, double qtdLitroAbastecida, String kms, String timestamp){
        this.posto = posto;
        this.preco = preco;
        this.data = data;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.real = real;
        this.carro = carro;
        this.qtdLitroAbastecida = qtdLitroAbastecida;
        this.kms = kms;
        this.timestamp = timestamp;
    }

    public String getPosto() { return posto; }

    public void setPosto(String posto) {
        this.posto = posto;
    }

    public String getTimestamp() {return timestamp;}

    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getKms() { return kms; }

    public void setKms(String kms) {
        this.kms = kms;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getCarro() {
        return carro;
    }

    public void setCarro(String carro) {
        this.carro = carro;
    }

    public String getReal() {
        return real;
    }

    public void setReal(String real) {
        this.real = real;
    }

    public double getQtdLitroAbastecida() {
        return qtdLitroAbastecida;
    }

    public void setQtdLitroAbastecida(double qtdLitroAbastecida) {
        this.qtdLitroAbastecida = qtdLitroAbastecida;
    }
}
