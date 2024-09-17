package com.pitercapistrano.appdelivery;

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
import com.google.firebase.auth.GoogleAuthProvider;

public class FormLogin extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleActivity";
    private TextView txt_cadastrar, txt_erro;
    private EditText edit_email, edit_senha;
    private Button bt_entrar;
    private SignInButton bt_google;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configura as opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_we_client_id))
                .requestEmail()
                .build();

        // Inicializa o Google Sign-In Client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();  // Inicializa FirebaseAuth

        getSupportActionBar().hide();
        IniciarComponentes();

        txt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FormLogin.this, FormCadastro.class);
                startActivity(intent);
            }
        });
        bt_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();

                if (email.isEmpty() || senha.isEmpty()) {
                    txt_erro.setText("Preencha todos os campos!");
                } else {
                    txt_erro.setText("");
                    AutenticarUsuario(email, senha);
                }
            }
        });

        bt_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }


    public void AutenticarUsuario(String email, String senha) {
        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.VISIBLE);

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IniciarTelaProdutos();
                        }
                    }, 3000);
                } else {
                    // Tratamento de erros com diferentes exceções específicas
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
                    // Exibe a mensagem de erro na interface
                    txt_erro.setText(erro);
                }
            }
        });
    }

    public void IniciarTelaProdutos() {
        Intent intent = new Intent(FormLogin.this, ListaProdutos.class);
        startActivity(intent);
        finish(); // Fecha a activity atual para que o usuário não possa voltar
    }

    public void IniciarComponentes() {
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_entrar = findViewById(R.id.bt_entrar);
        txt_erro = findViewById(R.id.txt_erro);
        txt_cadastrar = findViewById(R.id.txt_cadastrar);
        bt_google = findViewById(R.id.bt_google);
        progressBar = findViewById(R.id.progress_bar);
    }

    // Método para iniciar o fluxo de login do Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verifica se o resultado é do Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                // Login com sucesso
                Log.d(TAG, "Google Sign-In sucesso: " + account.getId());

                // Autentica no Firebase com o token do Google
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (Exception e) {
                Log.w(TAG, "Google Sign-In falhou.", e);
                txt_erro.setText("Falha no login com Google.");
            }
        }
    }

    // Autentica o usuário no Firebase com as credenciais do Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Login com sucesso, direciona para a tela de produtos
                    Log.d(TAG, "signInWithCredential: sucesso");
                    progressBar.setVisibility(View.VISIBLE);

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IniciarTelaProdutos();
                        }
                    }, 3000);

                } else {
                    // Falha na autenticação com Firebase
                    Log.w(TAG, "signInWithCredential: falha", task.getException());
                    txt_erro.setText("Falha na autenticação com Google.");
                }
            }
        });
    }
}