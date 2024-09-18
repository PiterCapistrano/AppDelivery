// Pacote que contém a classe de modelo Produto
package com.pitercapistrano.appdelivery.model;

// Definição da classe Produto que representa os dados de um produto
public class Produto {

    // Atributos privados da classe Produto (foto, nome, preço, descrição)
    private String foto;
    private String nome;
    private String preco;
    private String descricao;

    // Método getter para retornar a URL ou caminho da foto do produto
    public String getFoto() {
        return foto;
    }

    // Método setter para definir a URL ou caminho da foto do produto
    public void setFoto(String foto) {
        this.foto = foto;
    }

    // Método getter para retornar o nome do produto
    public String getNome() {
        return nome;
    }

    // Método setter para definir o nome do produto
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Método getter para retornar o preço do produto
    public String getPreco() {
        return preco;
    }

    // Método setter para definir o preço do produto
    public void setPreco(String preco) {
        this.preco = preco;
    }

    // Método getter para retornar a descrição do produto
    public String getDescricao() {
        return descricao;
    }

    // Método setter para definir a descrição do produto
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

