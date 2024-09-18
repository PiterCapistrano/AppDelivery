// Pacote que contém o listener para itens do RecyclerView
package com.pitercapistrano.appdelivery.recyclerViewItemClickListener;

// Importa as classes necessárias do Android
import android.content.Context;  // Usado para acessar recursos do sistema
import android.view.GestureDetector;  // Detecta e interpreta gestos do usuário (toques, arrastos, etc.)
import android.view.MotionEvent;  // Representa os eventos de toque (toque, arrasto, etc.)
import android.view.View;  // Classe base para todos os componentes da interface de usuário
import android.widget.AdapterView;  // Usado para trabalhar com eventos de clique em listas

// Importa bibliotecas da API do AndroidX para melhorar compatibilidade
import androidx.annotation.NonNull;  // Anotação que indica que um parâmetro não pode ser nulo
import androidx.recyclerview.widget.RecyclerView;  // Classe para exibir grandes quantidades de dados de forma eficiente

// Classe que implementa a interface OnItemTouchListener para detectar cliques em itens do RecyclerView
public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    // Declara uma interface para lidar com cliques e long cliques em itens do RecyclerView
    private OnItemClickListener mListener;
    // Objeto GestureDetector para detectar gestos como toques e longos toques
    GestureDetector mGestureDetector;

    // Método para interceptar eventos de toque antes que o RecyclerView os processe
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // Obtém a view correspondente à posição onde o usuário tocou
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        // Se houver uma view no ponto do toque, o listener estiver definido e o gesto foi reconhecido
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            // Chama o método onItemClick do listener com a view e a posição do item
            mListener.onItemClick(childView, rv.getChildAdapterPosition(childView));
            return true;  // Intercepta o evento de toque
        }
        return false;  // Não intercepta o evento de toque
    }

    // Método chamado para processar eventos de toque no RecyclerView
    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // Este método pode ser usado para ações após o toque, mas aqui está vazio
    }

    // Método chamado quando o RecyclerView não deve interceptar eventos de toque (não implementado)
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Este método é utilizado para impedir que o RecyclerView intercepte toques, mas não é necessário aqui
    }

    // Interface personalizada para lidar com cliques e longos cliques em itens do RecyclerView
    public interface OnItemClickListener extends AdapterView.OnItemClickListener {
        // Método para cliques curtos em um item
        void onItemClick(View view, int position);

        // Método para cliques longos em um item
        void onLongItemClick(View view, int position);
    }

    // Construtor da classe RecyclerViewItemClickListener
    public RecyclerViewItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        // Define o listener que será acionado nos eventos de clique
        mListener = listener;

        // Cria um GestureDetector para detectar toques simples e longos
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            // Método que reconhece toques simples
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;  // Indica que o toque foi reconhecido
            }

            // Método que reconhece toques longos
            @Override
            public void onLongPress(MotionEvent e) {
                // Obtém a view correspondente à posição onde o toque longo ocorreu
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                // Se houver uma view no ponto do toque longo e o listener estiver definido
                if (child != null && mListener != null) {
                    // Chama o método onLongItemClick do listener com a view e a posição do item
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }
}

