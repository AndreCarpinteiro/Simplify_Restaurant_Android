package com.example.simplifyrestaurant;

public class ClassePedido {
    private String Categoria;
    private String Preco;
    private int Quantidade;
    private String User;

    public ClassePedido() {
        // Tenho de ter o construtor vazio para o firebase
    }

    public ClassePedido(String categoria, String data, String nome, String preco, int quantidade, String user) {
        this.Categoria = categoria;
        this.Preco = preco;
        this.Quantidade = quantidade;
        this.User = user;
    }
    public String getCategoria() {
        return Categoria;
    }

    public void setCategoria(String categoria) {
        this.Categoria = categoria;
    }

    public String getPreco() {
        return Preco;
    }

    public void setPreco(String preco) {
        this.Preco = preco;
    }

    public int getQuantidade() {
        return Quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.Quantidade = quantidade;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        this.User = user;
    }
}
