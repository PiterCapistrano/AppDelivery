package com.pitercapistrano.appdelivery;

// Importa as bibliotecas necessárias
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importa as bibliotecas do Google Sign-In e Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FormLogin extends AppCompatActivity {

    // Constante para identificar a solicitação do Google Sign-In
    private static final int RC_SIGN_IN = 9001;
    // Tag usada para logging
    private static final String TAG = "GoogleActivity";

    // Declaração de componentes da interface
    private TextView txt_cadastrar, txt_erro;
    private EditText edit_email, edit_senha;
    private Button bt_entrar;
    private SignInButton bt_google;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth; // Autenticação do Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ativa a configuração "Edge to Edge" para tela inteira
        EdgeToEdge.enable(this);

        // Define o layout da tela
        setContentView(R.layout.activity_form_login);

        // Ajusta o layout para evitar que a interface fique escondida pelas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configura as opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_we_client_id)) // Solicita o token ID do Google
                .requestEmail() // Solicita o e-mail do usuário
                .build();

        // Inicializa o Google Sign-In Client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Inicializa o FirebaseAuth para autenticação de usuários
        mAuth = FirebaseAuth.getInstance();

        // Esconde a barra de ação (ActionBar) para uma interface mais limpa
        getSupportActionBar().hide();

        // Inicializa os componentes da interface
        IniciarComponentes();

        // Configura a ação de clique para o botão de cadastro
        txt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redireciona o usuário para a tela de cadastro
                Intent intent = new Intent(FormLogin.this, FormCadastro.class);
                startActivity(intent);
            }
        });

        // Configura a ação de clique para o botão de login
        bt_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        // Configura a ação de clique para o botão de login com o Google
        bt_google.setOnClickListener(new View.OnClickListener() {
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
        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Mostra o progresso de login
                    progressBar.setVisibility(View.VISIBLE);

                    // Aguarda 2 segundos e redireciona para a tela de produtos
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IniciarTelaProdutos();
                        }
                    }, 2000);
                } else {
                    // Tratamento de erros de autenticação
                    String erro;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erro = "Coloque uma senha com no mínimo 6 caracteres!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "E-mail inválido!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erro = "E-mail já cadastrado!";
                    } catch (FirebaseNetworkException e) {
                        erro = "Sem conexão com a internet!";
                    } catch (Exception e) {
                        erro = "Erro ao logar o usuário!";
                    }
                    // Exibe a mensagem de erro na tela
                    txt_erro.setText(erro);
                }
            }
        });
    }

    // Função para iniciar a tela de produtos após o login
    public void IniciarTelaProdutos() {
        // Redireciona para a tela de lista de produtos
        Intent intent = new Intent(FormLogin.this, ListaProdutos.class);
        startActivity(intent);
        finish(); // Fecha a activity de login para que o usuário não possa voltar
    }

    // Função chamada quando a activity é iniciada
    @Override
    protected void onStart() {
        super.onStart();

        // Verifica se o usuário já está autenticado
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        // Se o usuário estiver autenticado, redireciona para a tela de produtos
        if (usuarioAtual != null) {
            IniciarTelaProdutos();
        }
    }

    // Inicializa os componentes da interface
    public void IniciarComponentes() {
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
        super.onActivityResult(requestCode, resultCode, data);

        // Verifica se o resultado é do Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Obtém a conta do Google com sucesso
                GoogleSignInAccount account = task.getResult(Exception.class);
                Log.d(TAG, "Google Sign-In sucesso: " + account.getId());

                // Autentica no Firebase com o token do Google
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                // Exibe uma mensagem de erro se o login falhar
                Log.w(TAG, "Google Sign-In falhou.", e);
                txt_erro.setText("Falha no login com Google.");
            }
        }
    }

    // Autentica o usuário no Firebase com as credenciais do Google
    private void firebaseAuthWithGoogle(String idToken) {
        // Obtém as credenciais do Google para autenticar com Firebase
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Autenticação com sucesso, redireciona para a tela de produtos
                    Log.d(TAG, "signInWithCredential: sucesso");
                    progressBar.setVisibility(View.VISIBLE);

                    // Aguarda 2 segundos antes de redirecionar para tela de produtos
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IniciarTelaProdutos();
                        }
                    }, 2000);
                } else {
                    // Exibe uma mensagem de erro se a autenticação falhar
                    Log.w(TAG, "signInWithCredential: falha", task.getException());
                    txt_erro.setText("Falha na autenticação com Google.");
                }
            }
        });
    }
}
