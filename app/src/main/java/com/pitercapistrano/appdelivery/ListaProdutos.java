package com.pitercapistrano.appdelivery; // Declaração do pacote da aplicação

// Importa as bibliotecas necessárias
import android.content.Intent; // Para criar intents para navegar entre activities
import android.os.Bundle; // Para gerenciar dados de estado
import android.view.Menu; // Para manipulação de menus
import android.view.MenuItem; // Para manipulação de itens de menu
import android.view.View; // Para manipulação de views
import android.widget.AdapterView; // Para manipulação de eventos de clique em adapter views
import android.widget.Toast; // Para exibir mensagens curtas na tela

import androidx.activity.EdgeToEdge; // Para suporte a layouts de borda a borda
import androidx.annotation.NonNull; // Para indicar que um parâmetro não pode ser nulo
import androidx.appcompat.app.AppCompatActivity; // Classe base para atividades com suporte a Action Bar
import androidx.core.graphics.Insets; // Para manipulação de insets
import androidx.core.view.ViewCompat; // Para compatibilidade de views
import androidx.core.view.WindowInsetsCompat; // Para manipulação de insets da janela
import androidx.recyclerview.widget.LinearLayoutManager; // Para gerenciar o layout do RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Para exibir listas de dados em uma interface

// Importa a biblioteca de autenticação do Firebase
import com.google.android.gms.tasks.OnCompleteListener; // Para ouvir o resultado das tarefas
import com.google.android.gms.tasks.Task; // Para representar uma tarefa
import com.google.firebase.auth.FirebaseAuth; // Para gerenciar autenticação com Firebase
import com.google.firebase.firestore.FirebaseFirestore; // Para interagir com o Firestore
import com.google.firebase.firestore.QueryDocumentSnapshot; // Para representar documentos de consulta
import com.google.firebase.firestore.QuerySnapshot; // Para representar um conjunto de documentos de consulta
import com.pitercapistrano.appdelivery.adapter.AdapterProduto; // Adaptador personalizado para exibir produtos
import com.pitercapistrano.appdelivery.model.Produto; // Modelo de dados para produtos
import com.pitercapistrano.appdelivery.recyclerViewItemClickListener.RecyclerViewItemClickListener; // Listener para cliques no RecyclerView

import java.util.ArrayList; // Para manipulação de listas dinâmicas
import java.util.List; // Para representar listas

// Classe principal da activity que exibe a lista de produtos
public class ListaProdutos extends AppCompatActivity {

    private RecyclerView recyclerViewProdutos; // Componente RecyclerView para exibir produtos
    private AdapterProduto adapterProduto; // Adaptador para o RecyclerView
    private List<Produto> produtoList; // Lista de produtos a serem exibidos
    private FirebaseFirestore db; // Instância do Firestore para acessar dados

    // Função chamada quando a activity é criada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Chama o método pai

        // Ativa a configuração "Edge to Edge" para uma experiência de tela inteira
        EdgeToEdge.enable(this);

        // Define o layout da tela
        setContentView(R.layout.activity_lista_produtos);

        // Ajusta o layout para evitar que a interface fique escondida pelas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Obtém os insets do sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Define o padding
            return insets; // Retorna os insets
        });

        // Inicializa o RecyclerView e a lista de produtos
        recyclerViewProdutos = findViewById(R.id.recycler_view_produtos); // Obtém referência ao RecyclerView
        produtoList = new ArrayList<>(); // Cria uma nova lista de produtos
        adapterProduto = new AdapterProduto(getApplicationContext(), produtoList); // Inicializa o adaptador com a lista de produtos
        recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(getApplicationContext())); // Define o gerenciador de layout para o RecyclerView
        recyclerViewProdutos.setHasFixedSize(true); // Define que o tamanho do RecyclerView é fixo
        recyclerViewProdutos.setAdapter(adapterProduto); // Define o adaptador para o RecyclerView

        // Evento de clique no RecyclerView
        recyclerViewProdutos.addOnItemTouchListener(
                new RecyclerViewItemClickListener(
                        getApplicationContext(),
                        recyclerViewProdutos,
                        new RecyclerViewItemClickListener.OnItemClickListener() { // Implementa o listener de clique
                            @Override
                            public void onItemClick(View view, int position) { // Quando um item é clicado
                                // Cria um intent para a activity de detalhes do produto
                                Intent intent = new Intent(ListaProdutos.this, DetalhesProduto.class);
                                // Passa dados do produto clicado para a próxima activity
                                intent.putExtra("foto", produtoList.get(position).getFoto());
                                intent.putExtra("nome", produtoList.get(position).getNome());
                                intent.putExtra("descricao", produtoList.get(position).getDescricao());
                                intent.putExtra("preco", produtoList.get(position).getPreco());
                                startActivity(intent); // Inicia a activity de detalhes do produto
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                // Método para clique longo (ainda não implementado)
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                // Método para clique em AdapterView (não utilizado aqui)
                            }
                        }
                )
        );

        db = FirebaseFirestore.getInstance(); // Obtém uma instância do Firestore

        // Faz uma consulta na coleção "Produtos" e ordena por nome
        db.collection("Produtos").orderBy("nome")
                .get() // Executa a consulta
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() { // Adiciona um listener para a conclusão da tarefa
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) { // Verifica se a tarefa foi bem-sucedida
                            // Itera pelos documentos retornados
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Produto produto = queryDocumentSnapshot.toObject(Produto.class); // Converte o documento em um objeto Produto
                                produtoList.add(produto); // Adiciona o produto à lista
                                adapterProduto.notifyDataSetChanged(); // Notifica o adaptador para atualizar a lista exibida
                            }
                        }
                    }
                });

    }

    // Função para criar o menu na barra de opções
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla o layout do menu (carrega o layout de menu_principal.xml para a barra de opções)
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true; // Retorna true para indicar que o menu foi criado com sucesso
    }

    // Função chamada quando um item do menu é selecionado
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Obtém o ID do item selecionado no menu
        int itemId = item.getItemId();

        // Verifica qual item foi selecionado e executa a ação correspondente
        if (itemId == R.id.perfil) { // Se o item "perfil" foi selecionado
            Intent intent = new Intent(ListaProdutos.this, PerfilUsuario.class); // Cria um intent para a tela de perfil do usuário
            startActivity(intent); // Inicia a activity de perfil
        } else if (itemId == R.id.pedidos) { // Se o item "pedidos" foi selecionado
            // Ação para o item "Pedidos" (ainda não implementada)
        } else if (itemId == R.id.deslogar) { // Se o item "deslogar" foi selecionado
            // Desconecta o usuário do Firebase
            FirebaseAuth.getInstance().signOut();
            // Mostra uma mensagem ao usuário indicando que ele foi deslogado
            Toast.makeText(ListaProdutos.this, "Usuário Deslogado!", Toast.LENGTH_SHORT).show();
            // Redireciona o usuário para a tela de login
            Intent intent = new Intent(ListaProdutos.this, FormLogin.class);
            startActivity(intent); // Inicia a activity de login
            // Finaliza a activity atual para evitar que o usuário volte a esta tela após deslogar
            finish();
        }

        // Chama a implementação padrão do sistema para o item selecionado
        return super.onOptionsItemSelected(item);
    }
}

