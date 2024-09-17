package com.pitercapistrano.appdelivery;

// Importa as bibliotecas necessárias
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importa a biblioteca de autenticação do Firebase
import com.google.firebase.auth.FirebaseAuth;

public class ListaProdutos extends AppCompatActivity {

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
