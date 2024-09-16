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

// Declaração da classe FormCadastro que estende AppCompatActivity para criar uma tela de cadastro
public class FormCadastro extends AppCompatActivity {

    // Declaração de variáveis para os componentes da interface
    private CircleImageView fotoUsuario;
    private Button bt_selecionarFoto, bt_cadastrar;
    private EditText edit_nome, edit_email, edit_senha;
    private TextView txt_erro;

    // Variáveis para o ID do usuário e a URI da foto selecionada
    private String usuarioId;
    private Uri mSelecionarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Habilita a função de EdgeToEdge no layout para um design mais fluido
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);

        // Ajusta os paddings da view principal para respeitar as barras de status e navegação
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa os componentes da interface
        IniciarComponentes();

        // Adiciona listeners para monitorar mudanças nos campos de texto
        edit_nome.addTextChangedListener(cadastroTextWatcher);
        edit_email.addTextChangedListener(cadastroTextWatcher);
        edit_senha.addTextChangedListener(cadastroTextWatcher);

        // Define uma ação para o botão de cadastro
        bt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CadastrarUsiario(v);
            }
        });

        // Define uma ação para o botão de selecionar foto
        bt_selecionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelecionarFotoGaleria();
            }
        });
    }

    // Método para cadastrar um novo usuário
    public void CadastrarUsiario(View view){
        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        // Cria o usuário no Firebase Authentication com email e senha
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Se o cadastro for bem-sucedido, salva os dados do usuário no Firestore
                    SalvarDadosUsuario();
                    // Mostra uma mensagem de sucesso
                    Snackbar snackbar = Snackbar.make(view, "Cadastro realizado com sucesso!", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish(); // Fecha a activity
                        }
                    });
                    snackbar.show();
                } else {
                    // Tratamento de erros com diferentes exceções específicas
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
                    // Exibe a mensagem de erro na interface
                    txt_erro.setText(erro);
                }
            }
        });
    }

    // Lança um ActivityResultLauncher para selecionar uma imagem da galeria
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        mSelecionarUri = data.getData(); // Obtém o URI da imagem selecionada

                        try {
                            // Define a imagem selecionada no ImageView
                            fotoUsuario.setImageURI(mSelecionarUri);
                        }catch (Exception e){
                            e.printStackTrace(); // Captura possíveis exceções
                        }
                    }
                }
            }
    );

    // Método para abrir a galeria de imagens
    public void SelecionarFotoGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK); // Abre a galeria
        intent.setType("image/*");
        activityResultLauncher.launch(intent); // Lança o resultado para o launcher
    }

    // Método para salvar os dados do usuário no Firebase Storage e Firestore
    public void SalvarDadosUsuario(){
        // Gera um nome único para o arquivo da imagem
        String nomeArquivo = UUID.randomUUID().toString();

        // Referência para o Firebase Storage onde a imagem será salva
        final StorageReference reference = FirebaseStorage.getInstance().getReference("/imagens/" + nomeArquivo);

        // Faz o upload da imagem
        reference.putFile(mSelecionarUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Obtém a URL de download da imagem
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("url_img", uri.toString()); // Log da URL da imagem
                        String foto = uri.toString(); // Converte a URI para string

                        // Inicia o banco de dados Firestore
                        String nome = edit_nome.getText().toString();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Cria um mapa para armazenar os dados do usuário
                        Map<String, Object> usuarios = new HashMap<>();
                        usuarios.put("nome", nome);
                        usuarios.put("foto", foto);

                        // Obtém o ID do usuário atual do Firebase Authentication
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
                        // Tratamento de falhas ao obter a URL da imagem
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Tratamento de falhas ao fazer upload da imagem
            }
        });
    }

    // Método para inicializar os componentes da interface
    public void IniciarComponentes(){
        fotoUsuario = findViewById(R.id.foto_usuario); // Referência ao CircleImageView da foto do usuário
        bt_selecionarFoto = findViewById(R.id.bt_selecionar_foto); // Botão para selecionar foto
        edit_nome = findViewById(R.id.adicionar_nome); // Campo de texto para o nome
        edit_email = findViewById(R.id.adicionar_email); // Campo de texto para o email
        edit_senha = findViewById(R.id.adicionar_senha); // Campo de texto para a senha
        txt_erro = findViewById(R.id.txt_erro_cadastro); // TextView para exibir erros
        bt_cadastrar = findViewById(R.id.bt_cadastrar); // Botão para realizar o cadastro
    }

    // TextWatcher para habilitar/desabilitar o botão de cadastro dependendo dos campos preenchidos
    TextWatcher cadastroTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Verifica se todos os campos estão preenchidos
            String nome = edit_nome.getText().toString();
            String email = edit_email.getText().toString();
            String senha = edit_senha.getText().toString();

            // Habilita ou desabilita o botão de cadastro
            if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()){
                bt_cadastrar.setEnabled(true);
                bt_cadastrar.setBackgroundColor(getResources().getColor(R.color.dark_red)); // Cor para botão habilitado
            }else {
                bt_cadastrar.setEnabled(false);
                bt_cadastrar.setBackgroundColor(getResources().getColor(R.color.gray)); // Cor para botão desabilitado
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };
}
