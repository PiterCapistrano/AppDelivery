package com.pitercapistrano.appdelivery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class DetalhesProduto extends AppCompatActivity {

    private ImageView dtImagemProduto;
    private TextView txtNomeProduto, txtDescricaoProduto, txtPrecoProduto;
    private Button btAdicionarProduto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhes_produto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        String foto = getIntent().getExtras().getString("foto");
        String nome = getIntent().getExtras().getString("nome");
        String descricao = getIntent().getExtras().getString("descricao");
        String preco = getIntent().getExtras().getString("preco");

        Glide.with(getApplicationContext()).load(foto).into(dtImagemProduto);
        txtNomeProduto.setText(nome);
        txtDescricaoProduto.setText(descricao);
        txtPrecoProduto.setText(preco);
    }
    public void IniciarComponentes(){
        dtImagemProduto = findViewById(R.id.dt_image_produto);
        txtNomeProduto = findViewById(R.id.dt_nome_produto);
        txtDescricaoProduto = findViewById(R.id.dt_descricao_produto);
        txtPrecoProduto = findViewById(R.id.dt_preco_produto);
        btAdicionarProduto = findViewById(R.id.bt_adicionar_produto);
    }
}