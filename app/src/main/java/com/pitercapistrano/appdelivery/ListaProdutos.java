package com.pitercapistrano.appdelivery;

// Importa as bibliotecas necessárias
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importa a biblioteca de autenticação do Firebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pitercapistrano.appdelivery.adapter.AdapterProduto;
import com.pitercapistrano.appdelivery.model.Produto;
import com.pitercapistrano.appdelivery.recyclerViewItemClickListener.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ListaProdutos extends AppCompatActivity {

    private RecyclerView recyclerViewProdutos;
    private AdapterProduto adapterProduto;
    private List<Produto> produtoList;
    private FirebaseFirestore db;

    // Função chamada quando a activity é criada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ativa a configuração "Edge to Edge" para uma experiência de tela inteira
        EdgeToEdge.enable(this);

        // Define o layout da tela
        setContentView(R.layout.activity_lista_produtos);

        // Ajusta o layout para evitar que a interface fique escondida pelas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewProdutos = findViewById(R.id.recycler_view_produtos);
        produtoList = new ArrayList<>();
        adapterProduto = new AdapterProduto(getApplicationContext(), produtoList);
        recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewProdutos.setHasFixedSize(true);
        recyclerViewProdutos.setAdapter(adapterProduto);

        //Evento de click no Recycler View
        recyclerViewProdutos.addOnItemTouchListener(
                new RecyclerViewItemClickListener(
                        getApplicationContext(),
                        recyclerViewProdutos,
                        new RecyclerViewItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Produto produto = produtoList.get(position);
                                Toast.makeText(getApplicationContext(), produto.getNome(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );

        db = FirebaseFirestore.getInstance();

        db.collection("Produtos").orderBy("nome")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                Produto produto = queryDocumentSnapshot.toObject(Produto.class);
                                produtoList.add(produto);
                                adapterProduto.notifyDataSetChanged();
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
        if (itemId == R.id.perfil) {
            Intent intent = new Intent(ListaProdutos.this, PerfilUsuario.class);
            startActivity(intent);
        } else if (itemId == R.id.pedidos) {
            // Ação para o item "Pedidos" (ainda não implementada)
        } else if (itemId == R.id.deslogar) {
            // Ação para o item "Deslogar"
            // Desconecta o usuário do Firebase
            FirebaseAuth.getInstance().signOut();
            // Mostra uma mensagem ao usuário indicando que ele foi deslogado
            Toast.makeText(ListaProdutos.this, "Usuário Deslogado!", Toast.LENGTH_SHORT).show();
            // Redireciona o usuário para a tela de login
            Intent intent = new Intent(ListaProdutos.this, FormLogin.class);
            startActivity(intent);
            // Finaliza a activity atual para evitar que o usuário volte a esta tela após deslogar
            finish();
        }

        // Chama a implementação padrão do sistema para o item selecionado
        return super.onOptionsItemSelected(item);
    }
}
