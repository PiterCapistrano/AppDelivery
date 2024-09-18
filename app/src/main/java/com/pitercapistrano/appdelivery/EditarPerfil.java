// Pacote que contém a classe EditarPerfil
package com.pitercapistrano.appdelivery;

// Importa classes necessárias do Android e de bibliotecas externas
import android.app.Activity;  // Usada para representar a atividade
import android.content.Intent;  // Classe usada para criar intents e iniciar outras atividades
import android.net.Uri;  // Classe para representar URIs, como a URI de uma imagem
import android.os.Bundle;  // Usada para salvar e restaurar o estado da atividade
import android.util.Log;  // Classe para registrar logs
import android.view.View;  // Usada para interagir com as views da interface
import android.widget.Button;  // Botão de ação
import android.widget.EditText;  // Campo de texto editável

// Importa bibliotecas do AndroidX e Firebase
import androidx.activity.EdgeToEdge;  // Habilita layout em tela cheia, sem bordas do sistema
import androidx.activity.result.ActivityResult;  // Representa o resultado de uma atividade
import androidx.activity.result.ActivityResultCallback;  // Callback para tratar o resultado da atividade
import androidx.activity.result.ActivityResultLauncher;  // Inicia uma atividade e aguarda o resultado
import androidx.activity.result.contract.ActivityResultContracts;  // Contrato para iniciar atividades
import androidx.annotation.NonNull;  // Indica que um parâmetro ou retorno não pode ser nulo
import androidx.appcompat.app.AppCompatActivity;  // Classe base para atividades que usam ActionBar
import androidx.core.graphics.Insets;  // Usada para manipular margens da interface
import androidx.core.view.ViewCompat;  // Oferece compatibilidade para operações em views
import androidx.core.view.WindowInsetsCompat;  // Classe usada para gerenciar margens da janela

import com.google.android.gms.tasks.OnCompleteListener;  // Listener para completar tarefas no Firebase
import com.google.android.gms.tasks.OnFailureListener;  // Listener para capturar falhas
import com.google.android.gms.tasks.OnSuccessListener;  // Listener para capturar sucessos
import com.google.android.gms.tasks.Task;  // Representa uma tarefa assíncrona
import com.google.android.material.snackbar.Snackbar;  // Exibe uma barra de notificação
import com.google.firebase.auth.FirebaseAuth;  // Autenticação com Firebase
import com.google.firebase.firestore.FirebaseFirestore;  // Firestore, banco de dados NoSQL do Firebase
import com.google.firebase.storage.FirebaseStorage;  // Firebase Storage, para armazenar arquivos
import com.google.firebase.storage.StorageReference;  // Referência de um arquivo no Firebase Storage
import com.google.firebase.storage.UploadTask;  // Tarefa de upload para o Firebase Storage

import java.util.HashMap;  // Estrutura de dados de mapeamento chave-valor
import java.util.Map;  // Interface para mapas (chave-valor)
import java.util.UUID;  // Classe para gerar identificadores únicos

import de.hdodenhof.circleimageview.CircleImageView;  // Biblioteca para usar imagens de perfil circulares

// Classe EditarPerfil, que permite ao usuário atualizar seus dados de perfil
public class EditarPerfil extends AppCompatActivity {

    // Declaração de componentes de interface e variáveis de controle
    private CircleImageView fotoUsuario;  // Imagem do usuário
    private EditText editNome;  // Campo de texto para o nome
    private Button btAtualizarDados, btSelecionarFoto;  // Botões para atualizar dados e selecionar foto
    private Uri mSelecionarUri;  // URI da imagem selecionada
    private String usuarioId;  // ID do usuário

    // Método chamado quando a atividade é criada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Chama o método onCreate da superclasse
        EdgeToEdge.enable(this);  // Habilita o layout sem bordas
        setContentView(R.layout.activity_editar_perfil);  // Define o layout da atividade

        // Ajusta as margens da view principal de acordo com as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());  // Obtém as margens do sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);  // Aplica margens
            return insets;  // Retorna os insets ajustados
        });

        IniciarComponentes();  // Inicializa os componentes da interface

        // Define o listener para o botão de seleção de foto
        btSelecionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelecionarFotoGaleria();  // Inicia o processo de seleção de foto
            }
        });

        // Define o listener para o botão de atualização de dados
        btAtualizarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nome = editNome.getText().toString();  // Obtém o nome inserido

                // Verifica se o nome foi preenchido
                if (nome.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT);
                    snackbar.show();  // Exibe uma mensagem de erro se o nome estiver vazio
                } else {
                    AtualizarDadosPerfil(view);  // Chama o método para atualizar os dados do perfil
                }
            }
        });
    }

    // Método para inicializar as views do layout
    private void IniciarComponentes() {
        fotoUsuario = findViewById(R.id.foto_atualizada);  // Imagem de perfil do usuário
        editNome = findViewById(R.id.atualizar_nome);  // Campo de texto para atualizar o nome
        btSelecionarFoto = findViewById(R.id.bt_atualizar_foto);  // Botão para selecionar nova foto
        btAtualizarDados = findViewById(R.id.bt_atuaizar_dados);  // Botão para atualizar os dados
    }

    // Launcher para receber o resultado da seleção de uma imagem
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Verifica se a atividade de seleção de imagem foi bem-sucedida
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();  // Obtém os dados da imagem selecionada
                        mSelecionarUri = data.getData();  // Obtém o URI da imagem

                        try {
                            fotoUsuario.setImageURI(mSelecionarUri);  // Exibe a imagem selecionada no ImageView
                        } catch (Exception e) {
                            e.printStackTrace();  // Trata exceções ao exibir a imagem
                        }
                    }
                }
            }
    );

    // Método para selecionar uma foto da galeria
    public void SelecionarFotoGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);  // Cria uma intent para selecionar imagem
        intent.setType("image/*");  // Define o tipo de arquivo como imagem
        activityResultLauncher.launch(intent);  // Inicia a atividade de seleção de imagem
    }

    // Método para atualizar os dados do perfil no Firebase
    public void AtualizarDadosPerfil(View view) {
        // Verifica se uma imagem foi selecionada
        if (mSelecionarUri == null) {
            Log.e("Erro", "Nenhuma imagem selecionada");  // Log de erro se nenhuma imagem foi selecionada
            return;  // Interrompe o processo se não houver imagem
        }

        // Gera um nome de arquivo único para a imagem usando UUID
        String nomeArquivo = UUID.randomUUID().toString();

        // Cria uma referência no Firebase Storage para o upload da imagem
        final StorageReference reference = FirebaseStorage.getInstance().getReference("/imagens/" + nomeArquivo);

        // Faz o upload da imagem para o Firebase Storage
        reference.putFile(mSelecionarUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Obtém a URL de download da imagem após o upload
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("url_img", uri.toString());  // Log da URL da imagem
                        String foto = uri.toString();  // Converte a URI para string

                        // Obtém o nome do usuário e inicia o Firestore
                        String nome = editNome.getText().toString();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Cria um mapa para armazenar os dados do usuário
                        Map<String, Object> usuarios = new HashMap<>();
                        usuarios.put("nome", nome);
                        usuarios.put("foto", foto);

                        // Obtém o ID do usuário autenticado no Firebase
                        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Atualiza os dados do usuário no Firestore
                        db.collection("Usuarios").document(usuarioId)
                                .update("nome", nome, "foto", foto)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // Exibe um snackbar indicando sucesso na atualização
                                        Snackbar snackbar = Snackbar.make(view, "Sucesso ao atualizar os dados!", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("ok", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        finish();  // Fecha a atividade após confirmar
                                                    }
                                                });
                                        snackbar.show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Tratamento de falha na atualização dos dados
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log de erro ao tentar obter a URL da imagem
                        Log.i("storage_error", "Erro ao obter URL da imagem: " + e.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Log de erro ao fazer o upload da imagem
                Log.i("upload_error", "Erro ao fazer upload da imagem: " + e.toString());
            }
        });
    }
}
