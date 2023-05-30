package com.example.simplifyrestaurant;

public class Item {
    private String Categoria;
    private String Descricao;
    private String Img;
    private String Nome;
    private String Preco;
    private int quantity; // Novo atributo para controlar a quantidade

    public Item() {
        // Construtor sem argumentos necessário para o Firebase Database
    }

    public int getQuantidade() {
        return quantity;
    }

    public void setQuantidade(int quantidade) {
        this.quantity = quantidade;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
