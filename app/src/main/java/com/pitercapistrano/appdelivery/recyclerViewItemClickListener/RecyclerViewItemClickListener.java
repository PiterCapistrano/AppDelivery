package com.pitercapistrano.appdelivery.recyclerViewItemClickListener;

import android.content.Context;  // Importa a classe Context, usada para acessar recursos do sistema
import android.view.GestureDetector;  // Importa GestureDetector para detectar gestos de toque
import android.view.MotionEvent;  // Importa MotionEvent para lidar com eventos de toque
import android.view.View;  // Importa a classe View para interagir com elementos da interface
import android.widget.AdapterView;  // Importa AdapterView para lidar com eventos de clique em itens de lista

import androidx.annotation.NonNull;  // Importa a anotação NonNull para indicar que um parâmetro não pode ser nulo
import androidx.recyclerview.widget.RecyclerView;  // Importa RecyclerView para trabalhar com listas de rolagem eficiente

public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener mListener;
    GestureDetector mGestureDetector;

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)){
            mListener.onItemClick(childView, rv.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener extends AdapterView.OnItemClickListener{
       void onItemClick(View view, int position);

       void onLongItemClick(View view, int position);
    }

    public RecyclerViewItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e){
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && mListener != null){
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }
}
