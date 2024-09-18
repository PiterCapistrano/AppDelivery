// Pacote do adaptador de produtos para o app de delivery
package com.pitercapistrano.appdelivery.adapter;

// Importa as classes necessárias do Android
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Importa biblioteca Glide para carregar imagens
import com.bumptech.glide.Glide;
import com.pitercapistrano.appdelivery.R;
import com.pitercapistrano.appdelivery.model.Produto;


import java.util.List;
// Importa uma biblioteca para exibir imagens em forma circular
import de.hdodenhof.circleimageview.CircleImageView;

// Definição da classe do adaptador para RecyclerView
public class AdapterProduto extends RecyclerView.Adapter<AdapterProduto.ProdutoViewHolder> {

    // Variáveis para armazenar o contexto da aplicação e a lista de produtos
    private Context context;
    private List<Produto> produtoList;

    // Construtor do adaptador, inicializa o contexto e a lista de produtos
    public AdapterProduto(Context context, List<Produto> produtoList) {
        this.context = context;
        this.produtoList = produtoList;
    }

    // Método que cria e infla o layout de cada item da lista (produto_item.xml)
    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista;
        // Cria um objeto LayoutInflater para inflar o layout do item da lista
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // Infla o layout do item da lista e o atribui à variável itemLista
        itemLista = layoutInflater.inflate(R.layout.produto_item, parent, false);
        // Retorna um novo ViewHolder que contém o layout do item
        return new ProdutoViewHolder(itemLista);
    }

    // Método que vincula os dados (imagem, nome, preço) ao ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        // Carrega a imagem do produto com Glide e exibe na ImageView circular
        Glide.with(context).load(produtoList.get(position).getFoto()).into(holder.foto);
        // Define o nome do produto no TextView correspondente
        holder.nome.setText(produtoList.get(position).getNome());
        // Define o preço do produto no TextView correspondente
        holder.preco.setText(produtoList.get(position).getPreco());
    }

    // Método que retorna o número total de itens na lista de produtos
    @Override
    public int getItemCount() {
        return produtoList.size();
    }

    // Classe interna que define o ViewHolder para os itens da lista
    public class ProdutoViewHolder extends RecyclerView.ViewHolder{

        // Declaração das views que compõem o layout do item (imagem, nome, preço)
        private CircleImageView foto;
        private TextView nome, preco;

        // Construtor do ViewHolder que mapeia as views do layout
        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Associa a ImageView circular ao layout correspondente
            foto = itemView.findViewById(R.id.foto_produto);
            // Associa o TextView do nome do produto
            nome = itemView.findViewById(R.id.nome_produto);
            // Associa o TextView do preço do produto
            preco = itemView.findViewById(R.id.preco_produto);
        }
    }
}
