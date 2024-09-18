// Pacote que contém a atividade DetalhesProduto
package com.pitercapistrano.appdelivery;

// Importa classes necessárias do Android
import android.os.Bundle;  // Classe usada para salvar e restaurar o estado da atividade
import android.widget.Button;  // Botão de ação para a interface de usuário
import android.widget.ImageView;  // Classe para exibir imagens
import android.widget.TextView;  // Classe para exibir texto na interface

// Importa classes da biblioteca AndroidX para compatibilidade com versões anteriores do Android
import androidx.activity.EdgeToEdge;  // Habilita o layout em tela cheia, sem bordas
import androidx.appcompat.app.AppCompatActivity;  // Classe base para atividades que usam ActionBar
import androidx.core.graphics.Insets;  // Usada para manipular margens e bordas da interface
import androidx.core.view.ViewCompat;  // Oferece funcionalidades de compatibilidade para visualizações
import androidx.core.view.WindowInsetsCompat;  // Classe usada para gerenciar margens e áreas seguras da janela

// Importa a biblioteca Glide para carregar e exibir imagens
import com.bumptech.glide.Glide;  // Usada para carregar imagens a partir de URLs ou recursos locais

// Classe DetalhesProduto que estende AppCompatActivity e exibe os detalhes de um produto
public class DetalhesProduto extends AppCompatActivity {

    // Declaração das views usadas na interface da atividade (imagem, textos e botão)
    private ImageView dtImagemProduto;
    private TextView txtNomeProduto, txtDescricaoProduto, txtPrecoProduto;
    private Button btAdicionarProduto;

    // Método onCreate, que é chamado quando a atividade é criada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Chama o método onCreate da superclasse para inicializar a atividade
        EdgeToEdge.enable(this);  // Habilita o modo Edge-to-Edge (sem bordas de sistema) para a atividade
        setContentView(R.layout.activity_detalhes_produto);  // Define o layout da atividade

        // Define um listener para ajustar as margens da view principal de acordo com as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Obtém as margens (barras de status, barra de navegação) do sistema
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Ajusta as margens da view principal com base nas barras de sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;  // Retorna os insets ajustados
        });

        // Inicializa os componentes da interface chamando o método IniciarComponentes
        IniciarComponentes();

        // Obtém os dados do produto enviados através da Intent (imagem, nome, descrição, preço)
        String foto = getIntent().getExtras().getString("foto");
        String nome = getIntent().getExtras().getString("nome");
        String descricao = getIntent().getExtras().getString("descricao");
        String preco = getIntent().getExtras().getString("preco");

        // Usa o Glide para carregar a imagem do produto e exibí-la na ImageView
        Glide.with(getApplicationContext()).load(foto).into(dtImagemProduto);
        // Define o nome, descrição e preço do produto nos TextViews correspondentes
        txtNomeProduto.setText(nome);
        txtDescricaoProduto.setText(descricao);
        txtPrecoProduto.setText(preco);
    }

    // Método para inicializar as views da interface (ImageView, TextViews e Button)
    public void IniciarComponentes() {
        // Associa as views da interface aos IDs definidos no layout XML
        dtImagemProduto = findViewById(R.id.dt_image_produto);  // ImageView para exibir a imagem do produto
        txtNomeProduto = findViewById(R.id.dt_nome_produto);  // TextView para exibir o nome do produto
        txtDescricaoProduto = findViewById(R.id.dt_descricao_produto);  // TextView para exibir a descrição do produto
        txtPrecoProduto = findViewById(R.id.dt_preco_produto);  // TextView para exibir o preço do produto
        btAdicionarProduto = findViewById(R.id.bt_adicionar_produto);  // Botão para adicionar o produto
    }
}
