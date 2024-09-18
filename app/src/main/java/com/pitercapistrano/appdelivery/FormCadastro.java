package com.pitercapistrano.appdelivery;

// Importações necessárias para usar diferentes classes e funcionalidades do Android e Firebase
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

// Classe principal da tela de cadastro, que herda de AppCompatActivity
public class FormCadastro extends AppCompatActivity {

    // Declaração dos elementos de interface (ImageView, Botões, EditTexts e TextView)
    private CircleImageView fotoUsuario;
    private Button bt_selecionarFoto, bt_cadastrar;
    private EditText edit_nome, edit_email, edit_senha;
    private TextView txt_erro;

    // Variáveis para armazenar o ID do usuário e o URI da imagem selecionada
    private String usuarioId;
    private Uri mSelecionarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ativa o layout de borda a borda (Edge-to-Edge) para um design imersivo
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);

        // Ajusta a interface para levar em conta as barras de status e navegação do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa os componentes de interface da tela
        IniciarComponentes();

        // Adiciona TextWatchers para habilitar/desabilitar o botão de cadastro ao preencher os campos
        edit_nome.addTextChangedListener(cadastroTextWatcher);
        edit_email.addTextChangedListener(cadastroTextWatcher);
        edit_senha.addTextChangedListener(cadastroTextWatcher);

        // Ação para o botão de cadastro, que chama a função de cadastro do usuário
        bt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CadastrarUsiario(v);
            }
        });

        // Ação para o botão de selecionar foto, que abre a galeria de imagens
        bt_selecionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelecionarFotoGaleria();
            }
        });
    }

    // Função para cadastrar o usuário no Firebase
    public void CadastrarUsiario(View view){
        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        // Realiza o cadastro do usuário no Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Se o cadastro for bem-sucedido, salva os dados no Firestore
                    SalvarDadosUsuario();

                    // Exibe um Snackbar para notificar sucesso
                    Snackbar snackbar = Snackbar.make(view, "Cadastro realizado com sucesso!", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish(); // Fecha a Activity ao concluir
                        }
                    });
                    snackbar.show();
                } else {
                    // Tratamento de diferentes tipos de erro no cadastro
                    String erro;
                    try {
                        throw task.getException();
                    }catch(FirebaseAuthWeakPasswordException e){
                        erro = "Coloque uma senha com no mínimo 6 caracteres!";
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        erro = "E-mail inválido!";
                    }catch(FirebaseAuthUserCollisionException e){
                        erro = "E-mail já cadastrado!";
                    }catch(FirebaseNetworkException e){
                        erro = "Sem conexão com a internet";
                    }catch (Exception e){
                        erro = "Erro ao cadastrar o usuário";
                    }
                    // Exibe a mensagem de erro no TextView
                    txt_erro.setText(erro);
                }
            }
        });
    }

    // Lançador de resultados para selecionar imagem da galeria
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Se a seleção foi bem-sucedida, obtém a URI da imagem
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        mSelecionarUri = data.getData(); // Armazena a URI da imagem selecionada

                        try {
                            // Define a imagem selecionada no ImageView correspondente
                            fotoUsuario.setImageURI(mSelecionarUri);
                        }catch (Exception e){
                            e.printStackTrace(); // Lida com possíveis exceções ao definir a imagem
                        }
                    }
                }
            }
    );

    // Método que inicia a seleção de uma imagem da galeria
    public void SelecionarFotoGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK); // Intenção para abrir a galeria
        intent.setType("image/*"); // Filtra para apenas arquivos de imagem
        activityResultLauncher.launch(intent); // Lança a Activity para selecionar imagem
    }

    // Método para salvar os dados do usuário no Firebase Storage e Firestore
    public void SalvarDadosUsuario(){
        // Verifica se uma imagem foi selecionada
        if (mSelecionarUri == null) {
            Log.e("Erro", "Nenhuma imagem selecionada"); // Exibe erro no log
            return; // Se não houver imagem, interrompe o processo
        }

        // Gera um nome único para o arquivo da imagem
        String nomeArquivo = UUID.randomUUID().toString();
        // Cria uma referência no Firebase Storage para armazenar a imagem
        final StorageReference reference = FirebaseStorage.getInstance().getReference("/imagens/" + nomeArquivo);

        // Realiza o upload da imagem selecionada
        reference.putFile(mSelecionarUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Após o upload, obtém a URL da imagem no Storage
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("url_img", uri.toString()); // Exibe a URL da imagem no log
                        String foto = uri.toString(); // Armazena a URL da imagem

                        // Inicia a instância do Firebase Firestore para salvar os dados
                        String nome = edit_nome.getText().toString();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Cria um mapa para armazenar os dados do usuário
                        Map<String, Object> usuarios = new HashMap<>();
                        usuarios.put("nome", nome);
                        usuarios.put("foto", foto);

                        // Obtém o ID do usuário autenticado no Firebase Authentication
                        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Cria uma referência ao documento do usuário no Firestore
                        DocumentReference documentReference = db.collection("Usuarios").document(usuarioId);

                        // Salva os dados do usuário no Firestore
                        documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i("db", "Sucesso ao salvar os dados!"); // Log de sucesso
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("db_error", "Erro ao salvar os dados!" + e.toString()); // Log de erro
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tratamento de erro ao obter a URL da imagem
                        Log.i("storage_error", "Erro ao obter URL da imagem: " + e.toString()); // Log de erro
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Tratamento de erro ao realizar o upload da imagem
                Log.i("upload_error", "Erro ao fazer upload da imagem: " + e.toString()); // Log de erro
            }
        });
    }

    // Método para inicializar os componentes de interface da tela
    public void IniciarComponentes(){
        // Referencia os elementos da interface pelo seu ID
        fotoUsuario = findViewById(R.id.foto_usuario);
        bt_selecionarFoto = findViewById(R.id.bt_selecionar_foto);
        edit_nome = findViewById(R.id.adicionar_nome);
        edit_email = findViewById(R.id.adicionar_email);
        edit_senha = findViewById(R.id.adicionar_senha);
        txt_erro = findViewById(R.id.txt_erro_cadastro);
        bt_cadastrar = findViewById(R.id.bt_cadastrar);
    }

    // TextWatcher para verificar se os campos foram preenchidos e habilitar o botão de cadastro
    TextWatcher cadastroTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Verifica se os campos estão preenchidos
            String nome = edit_nome.getText().toString();
            String email = edit_email.getText().toString();
            String senha = edit_senha.getText().toString();

            // Habilita ou desabilita o botão de cadastro baseado no preenchimento
            if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()){
                bt_cadastrar.setEnabled(true);
                bt_cadastrar.setBackgroundColor(getResources().getColor(R.color.dark_red)); // Muda a cor do botão
            } else {
                bt_cadastrar.setEnabled(false);
                bt_cadastrar.setBackgroundColor(getResources().getColor(R.color.gray)); // Muda a cor do botão
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };
}

