package com.pitercapistrano.appdelivery; // Declaração do pacote da aplicação

import android.content.Intent; // Para criar intents que navegam entre activities
import android.os.Bundle; // Para gerenciar dados de estado da activity
import android.view.View; // Para manipulação de views
import android.widget.Button; // Para criar botões
import android.widget.TextView; // Para exibir texto na tela

import androidx.activity.EdgeToEdge; // Para suporte a layouts de borda a borda
import androidx.annotation.Nullable; // Para indicar que um parâmetro pode ser nulo
import androidx.appcompat.app.AppCompatActivity; // Classe base para atividades com suporte a Action Bar
import androidx.core.graphics.Insets; // Para manipulação de insets
import androidx.core.view.ViewCompat; // Para compatibilidade de views
import androidx.core.view.WindowInsetsCompat; // Para manipulação de insets da janela

import com.bumptech.glide.Glide; // Para carregar imagens de forma eficiente
import com.google.firebase.auth.FirebaseAuth; // Para gerenciar autenticação com Firebase
import com.google.firebase.firestore.DocumentReference; // Para referência a um documento no Firestore
import com.google.firebase.firestore.DocumentSnapshot; // Para representar um documento retornado
import com.google.firebase.firestore.EventListener; // Para ouvir eventos do Firestore
import com.google.firebase.firestore.FirebaseFirestore; // Para interagir com o Firestore
import com.google.firebase.firestore.FirebaseFirestoreException; // Para manipulação de exceções do Firestore

import de.hdodenhof.circleimageview.CircleImageView; // Para exibir imagens circulares

// Classe principal da activity que exibe o perfil do usuário
public class PerfilUsuario extends AppCompatActivity {

    private CircleImageView foto_perfil; // Componente para exibir a foto de perfil
    private TextView nome_usuario, email_usuario; // Componentes para exibir nome e e-mail do usuário
    private Button bt_editar_perfil; // Botão para editar o perfil do usuário
    private String usuarioId; // ID do usuário autenticado

    // Função chamada quando a activity é criada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Chama o método pai

        EdgeToEdge.enable(this); // Ativa a configuração "Edge to Edge" para uma experiência de tela inteira
        setContentView(R.layout.activity_perfil_usuario); // Define o layout da activity

        // Ajusta o layout para evitar que a interface fique escondida pelas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Obtém os insets do sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Define o padding
            return insets; // Retorna os insets
        });

        IniciarComponentes(); // Chama método para inicializar componentes da interface

        // Define um listener para o botão de editar perfil
        bt_editar_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // Quando o botão é clicado
                Intent intent = new Intent(PerfilUsuario.this, EditarPerfil.class); // Cria um intent para a activity de editar perfil
                startActivity(intent); // Inicia a activity de editar perfil
            }
        });
    }

    // Função chamada quando a activity se torna visível
    @Override
    protected void onStart() {
        super.onStart(); // Chama o método pai

        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Obtém uma instância do Firestore
        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Obtém o ID do usuário autenticado
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Obtém o e-mail do usuário autenticado

        // Referência ao documento do usuário na coleção "Usuarios"
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioId);
        // Adiciona um listener para ouvir mudanças no documento do usuário
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null) { // Verifica se o documento foi retornado
                    // Carrega a foto de perfil usando Glide
                    Glide.with(getApplicationContext()).load(documentSnapshot.getString("foto")).into(foto_perfil);
                    // Define o nome do usuário no TextView
                    nome_usuario.setText(documentSnapshot.getString("nome"));
                    // Define o e-mail do usuário no TextView
                    email_usuario.setText(email);
                }
            }
        });
    }

    // Método para inicializar os componentes da interface
    public void IniciarComponentes() {
        foto_perfil = findViewById(R.id.foto_perfil); // Inicializa o componente da foto de perfil
        nome_usuario = findViewById(R.id.nome_usuario); // Inicializa o componente do nome do usuário
        email_usuario = findViewById(R.id.email_usuario); // Inicializa o componente do e-mail do usuário
        bt_editar_perfil = findViewById(R.id.bt_editar_perfil); // Inicializa o botão de editar perfil
    }
}
