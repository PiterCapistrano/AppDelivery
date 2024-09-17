package com.pitercapistrano.appdelivery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfil extends AppCompatActivity {

    private CircleImageView fotoUsuario;
    private EditText editNome;
    private Button btAtualizarDados, btSelecionarFoto;
    private Uri mSelecionarUri;
    private String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        btSelecionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelecionarFotoGaleria();
            }
        });

        btAtualizarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nome = editNome.getText().toString();

                if (nome.isEmpty()){
                    Snackbar snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }else {
                    AtualizarDadosPerfil(view);
                }
            }
        });
    }

    private void IniciarComponentes(){
        fotoUsuario = findViewById(R.id.foto_atualizada);
        editNome = findViewById(R.id.atualizar_nome);
        btSelecionarFoto = findViewById(R.id.bt_atualizar_foto);
        btAtualizarDados = findViewById(R.id.bt_atuaizar_dados);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                mSelecionarUri = data.getData();

                try {
                    fotoUsuario.setImageURI(mSelecionarUri);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    });

    public void SelecionarFotoGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    public void AtualizarDadosPerfil(View view){
        // Verifica se o URI da imagem foi selecionado
        if (mSelecionarUri == null) {
            Log.e("Erro", "Nenhuma imagem selecionada");
            return; // Se não houver imagem, interrompe o processo
        }
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
                        String nome = editNome.getText().toString();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Cria um mapa para armazenar os dados do usuário
                        Map<String, Object> usuarios = new HashMap<>();
                        usuarios.put("nome", nome);
                        usuarios.put("foto", foto);

                        // Obtém o ID do usuário atual do Firebase Authentication
                        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Cria uma referência ao documento do usuário no Firestore
                        db.collection("Usuarios").document(usuarioId)
                                .update("nome", nome, "foto", foto)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Snackbar snackbar = Snackbar.make(view, "Sucesso ao atualizar os dados!", Snackbar.LENGTH_INDEFINITE)
                                                .setAction("ok", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        finish();
                                                    }
                                                });
                                        snackbar.show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tratamento de falhas ao obter a URL da imagem
                        Log.i("storage_error", "Erro ao obter URL da imagem: " + e.toString()); // Log de erro
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Tratamento de falhas ao fazer upload da imagem
                Log.i("upload_error", "Erro ao fazer upload da imagem: " + e.toString()); // Log de erro
            }
        });
    }
}
