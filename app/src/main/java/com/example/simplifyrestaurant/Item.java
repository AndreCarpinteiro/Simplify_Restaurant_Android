package com.example.simplifyrestaurant;

import java.util.Date;

public class Item {
    private String Categoria;
    private String Descricao;
    private String Img;
    private String Nome;
    private String Preco;
    private String User;
    private Date Data;
    private int Quantidade;

    public Item() {
        // Construtor sem argumentos necessário para o Firebase Database
    }

    public int getQuantidade() {
        return Quantidade;
    }

    public Date getData(){
        return Data;
    }
    public String getUser() {
        return User;
    }

    public void setQuantidade(int quantidade) {
        this.Quantidade = quantidade;
    }
    public String getCategoria() {
        return Categoria;
    }

    public String getDescricao() {
        return Descricao;
    }

    public String getImg() {
        return Img;
    }

    public String getNome() {
        return Nome;
    }

    public String getPreco() {
        return Preco;
    }
}
