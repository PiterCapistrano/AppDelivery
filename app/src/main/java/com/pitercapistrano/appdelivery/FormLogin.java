package com.pitercapistrano.appdelivery; // Declaração do pacote da aplicação

// Importa as bibliotecas necessárias
import android.content.Intent; // Para criar intents para navegar entre activities
import android.net.Uri; // Para manipulação de URIs
import android.os.Bundle; // Para gerenciar dados de estado
import android.os.Handler; // Para manipulação de threads
import android.os.Looper; // Para gerenciamento de loops de mensagens
import android.util.Log; // Para log de mensagens
import android.view.View; // Para manipulação de views
import android.widget.Button; // Para botões
import android.widget.EditText; // Para campos de entrada de texto
import android.widget.ProgressBar; // Para exibir um indicador de progresso
import android.widget.TextView; // Para exibir texto

import androidx.activity.EdgeToEdge; // Para suporte a layouts de borda a borda
import androidx.annotation.NonNull; // Para indicar que um parâmetro não pode ser nulo
import androidx.annotation.Nullable; // Para indicar que um parâmetro pode ser nulo
import androidx.appcompat.app.AppCompatActivity; // Classe base para atividades com suporte a Action Bar
import androidx.core.graphics.Insets; // Para manipulação de insets
import androidx.core.view.ViewCompat; // Para compatibilidade de views
import androidx.core.view.WindowInsetsCompat; // Para manipulação de insets da janela

// Importa as bibliotecas do Google Sign-In e Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn; // Para autenticação via Google
import com.google.android.gms.auth.api.signin.GoogleSignInAccount; // Para representar a conta do Google
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // Para gerenciar o fluxo de login do Google
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Para configurar opções de login do Google
import com.google.android.gms.common.SignInButton; // Para o botão de login do Google
import com.google.android.gms.tasks.OnCompleteListener; // Para ouvir o resultado das tarefas
import com.google.android.gms.tasks.OnFailureListener; // Para ouvir falhas nas tarefas
import com.google.android.gms.tasks.OnSuccessListener; // Para ouvir sucessos nas tarefas
import com.google.android.gms.tasks.Task; // Para representar uma tarefa
import com.google.firebase.FirebaseNetworkException; // Para tratar exceções de rede do Firebase
import com.google.firebase.auth.AuthCredential; // Para credenciais de autenticação
import com.google.firebase.auth.AuthResult; // Para o resultado da autenticação
import com.google.firebase.auth.FirebaseAuth; // Para gerenciar autenticação com Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; // Para tratar credenciais inválidas
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // Para tratar conflitos de usuário
import com.google.firebase.auth.FirebaseAuthWeakPasswordException; // Para tratar senhas fracas
import com.google.firebase.auth.FirebaseUser; // Para representar um usuário autenticado
import com.google.firebase.auth.GoogleAuthProvider; // Para obter credenciais do Google
import com.google.firebase.firestore.DocumentReference; // Para referência a documentos do Firestore
import com.google.firebase.firestore.FirebaseFirestore; // Para interagir com o Firestore
import com.google.firebase.storage.FirebaseStorage; // Para interagir com o Firebase Storage
import com.google.firebase.storage.StorageReference; // Para referência a arquivos no Storage
import com.google.firebase.storage.UploadTask; // Para tarefas de upload

import java.util.HashMap; // Para utilizar mapas
import java.util.Map; // Para representar pares chave-valor
import java.util.UUID; // Para gerar identificadores únicos

// Classe principal da activity de login
public class FormLogin extends AppCompatActivity {

    // Constante para identificar a solicitação do Google Sign-In
    private static final int RC_SIGN_IN = 9001;
    // Tag usada para logging
    private static final String TAG = "GoogleActivity";

    // Declaração de componentes da interface
    private TextView txt_cadastrar, txt_erro; // Texto para cadastro e mensagens de erro
    private EditText edit_email, edit_senha; // Campos para entrada de e-mail e senha
    private Button bt_entrar; // Botão para entrar
    private SignInButton bt_google; // Botão para login com Google
    private GoogleSignInClient mGoogleSignInClient; // Cliente para login com Google
    private ProgressBar progressBar; // Indicador de progresso
    private FirebaseAuth mAuth; // Autenticação do Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Chama o método pai

        // Ativa a configuração "Edge to Edge" para tela inteira
        EdgeToEdge.enable(this);

        // Define o layout da tela
        setContentView(R.layout.activity_form_login);

        // Ajusta o layout para evitar que a interface fique escondida pelas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Obtém os insets do sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Define o padding
            return insets; // Retorna os insets
        });

        // Configura as opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // Cria as opções
                .requestIdToken(getString(R.string.default_we_client_id)) // Solicita o token ID do Google
                .requestEmail() // Solicita o e-mail do usuário
                .build(); // Constrói as opções

        // Inicializa o Google Sign-In Client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Inicializa o FirebaseAuth para autenticação de usuários
        mAuth = FirebaseAuth.getInstance();

        // Esconde a barra de ação (ActionBar) para uma interface mais limpa
        getSupportActionBar().hide();

        // Inicializa os componentes da interface
        IniciarComponentes();

        // Configura a ação de clique para o botão de cadastro
        txt_cadastrar.setOnClickListener(v -> { // Estrutura lambda Java 8
            // Redireciona o usuário para a tela de cadastro
            Intent intent = new Intent(FormLogin.this, FormCadastro.class);
            startActivity(intent); // Inicia a activity de cadastro
        });

        // Configura a ação de clique para o botão de login
        bt_entrar.setOnClickListener(view -> { // Estrutura lambda Java 8
            // Obtém o e-mail e a senha inseridos pelo usuário
            String email = edit_email.getText().toString();
            String senha = edit_senha.getText().toString();

            // Verifica se os campos estão vazios
            if (email.isEmpty() || senha.isEmpty()) {
                // Mostra mensagem de erro se os campos não estiverem preenchidos
                txt_erro.setText("Preencha todos os campos!");
            } else {
                // Limpa mensagens de erro anteriores
                txt_erro.setText("");
                // Autentica o usuário com o Firebase
                AutenticarUsuario(email, senha);
            }
        });

        // Configura a ação de clique para o botão de login com o Google
        bt_google.setOnClickListener(new View.OnClickListener() { // Estrutura Java 7
            @Override
            public void onClick(View v) {
                // Inicia o fluxo de login com o Google
                signInWithGoogle();
            }
        });
    }

    // Função para autenticar o usuário com e-mail e senha
    public void AutenticarUsuario(String email, String senha) {
        // Tenta autenticar o usuário com Firebase usando e-mail e senha
        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(task -> { // Estrutura lambda Java 8
            if (task.isSuccessful()) { // Verifica se a autenticação foi bem-sucedida
                // Mostra o progresso de login
                progressBar.setVisibility(View.VISIBLE);

                // Aguarda 2 segundos e redireciona para a tela de produtos
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IniciarTelaProdutos(); // Inicia a tela de produtos
                    }
                }, 2000); // Atraso de 2 segundos
            } else { // Se a autenticação falhar
                // Tratamento de erros de autenticação
                String erro;
                try {
                    throw task.getException(); // Lança a exceção capturada
                } catch (FirebaseAuthWeakPasswordException e) {
                    erro = "Coloque uma senha com no mínimo 6 caracteres!"; // Mensagem para senha fraca
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    erro = "E-mail inválido!"; // Mensagem para e-mail inválido
                } catch (FirebaseAuthUserCollisionException e) {
                    erro = "E-mail já cadastrado!"; // Mensagem para e-mail já cadastrado
                } catch (FirebaseNetworkException e) {
                    erro = "Sem conexão com a internet!"; // Mensagem para falta de conexão
                } catch (Exception e) {
                    erro = "Erro ao logar o usuário!"; // Mensagem genérica de erro
                }
                // Exibe a mensagem de erro na tela
                txt_erro.setText(erro);
            }
        });
    }

    // Função para iniciar a tela de produtos após o login
    public void IniciarTelaProdutos() {
        // Redireciona para a tela de lista de produtos
        Intent intent = new Intent(FormLogin.this, ListaProdutos.class);
        startActivity(intent); // Inicia a activity de lista de produtos
        finish(); // Fecha a activity de login para que o usuário não possa voltar
    }

    // Função chamada quando a activity é iniciada
    @Override
    protected void onStart() {
        super.onStart(); // Chama o método pai

        // Verifica se o usuário já está autenticado
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        // Se o usuário estiver autenticado, redireciona para a tela de produtos
        if (usuarioAtual != null) {
            IniciarTelaProdutos(); // Inicia a tela de produtos
        }
    }

    // Inicializa os componentes da interface
    public void IniciarComponentes() {
        // Referencia os componentes da interface pelo seu ID
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_entrar = findViewById(R.id.bt_entrar);
        txt_erro = findViewById(R.id.txt_erro);
        txt_cadastrar = findViewById(R.id.txt_cadastrar);
        bt_google = findViewById(R.id.bt_google);
        progressBar = findViewById(R.id.progress_bar);
    }

    // Método para iniciar o fluxo de login com Google
    private void signInWithGoogle() {
        // Cria um intent para iniciar o login com Google
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        // Inicia a activity de login com Google
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Função chamada após o login com Google retornar um resultado
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // Chama o método pai

        // Verifica se o resultado é do Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data); // Obtém a conta do Google
            try {
                // Obtém a conta do Google com sucesso
                GoogleSignInAccount account = task.getResult(Exception.class);
                Log.d(TAG, "Google Sign-In sucesso: " + account.getId()); // Log de sucesso

                // Autentica no Firebase com o token do Google
                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                // Exibe uma mensagem de erro se o login falhar
                Log.w(TAG, "Google Sign-In falhou.", e);
                txt_erro.setText("Falha no login com Google."); // Mensagem de erro
            }
        }
    }

    // Autentica o usuário no Firebase com as credenciais do Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "Autenticando com Google:" + account.getId()); // Log de autenticação

        // Obtém as credenciais de autenticação do Google
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        // Tenta autenticar com o Firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // Se a autenticação for bem-sucedida
                            // Autenticação com sucesso, obtém o usuário do Firebase
                            Log.d(TAG, "signInWithCredential:success");
                            progressBar.setVisibility(View.VISIBLE); // Mostra o progresso
                            FirebaseUser user = mAuth.getCurrentUser(); // Obtém o usuário autenticado

                            // Aguarda 2 segundos antes de redirecionar para tela de produtos
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Salva os dados do usuário no Firestore
                                    saveUserToFirestore(user);

                                    // Redireciona para a tela de produtos
                                    IniciarTelaProdutos();
                                }
                            },2000); // Atraso de 2 segundos
                        } else { // Se a autenticação falhar
                            Log.w(TAG, "signInWithCredential:failure", task.getException()); // Log de falha
                        }
                    }
                });
    }

    // Função para salvar os dados do usuário no Firestore
    private void saveUserToFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Obtém uma instância do Firestore

        // Cria um mapa para armazenar os dados do usuário
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nome", user.getDisplayName()); // Armazena o nome do usuário
        userMap.put("email", user.getEmail()); // Armazena o e-mail do usuário
        userMap.put("foto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : ""); // Armazena a URL da foto do usuário

        // Salva os dados no Firestore com o ID do usuário
        db.collection("Usuarios").document(user.getUid()) // Referencia a coleção de usuários
                .set(userMap) // Salva os dados do usuário
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Dados do usuário salvos no Firestore."); // Log de sucesso
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Erro ao salvar dados do usuário.", e); // Log de erro
                    }
                });
    }
}

