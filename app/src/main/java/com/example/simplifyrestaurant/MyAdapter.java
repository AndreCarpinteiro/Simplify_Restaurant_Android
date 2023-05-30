package com.example.simplifyrestaurant;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter  extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Item> list;
    private List<Item> itemList;
    private List<Item> filteredList;
    Dialog mDialog;
    private static int counter = 0;
    public MyAdapter(Context context, ArrayList<Item> list) {
        this.context = context;
        this.list = list;
        this.itemList = new ArrayList<>(list);
        this.filteredList = new ArrayList<>();
    }

    public void filterList(List<Item> filteredItems) {
        filteredList.clear();
        filteredList.addAll(filteredItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item = list.get(position);
        holder.categoria.setText(item.getCategoria());
        holder.nome.setText(item.getNome());
        holder.preco.setText(item.getPreco());

        holder.buttonPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new Dialog(context);
                mDialog.setContentView(R.layout.popup);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show(); // Exibe o diálogo pop-up

                TextView popupNome = mDialog.findViewById(R.id.popnome);
                TextView popupDescricao = mDialog.findViewById(R.id.popdescricao);
                //TextView popupCategoria = mDialog.findViewById(R.id.popcategoria);
                TextView popupPreco = mDialog.findViewById(R.id.poppreco);
                ImageView img = mDialog.findViewById(R.id.imgComida);

                popupNome.setText(item.getNome());
                popupDescricao.setText(String.valueOf(item.getDescricao()));
                //popupCategoria.setText(item.getCategoria());
                popupPreco.setText(item.getPreco() + "€");

                // Converter a imagem base64 em um Bitmap
                byte[] decodedString = Base64.decode(item.getImg(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Definir o Bitmap no ImageView
                img.setImageBitmap(decodedBitmap);

                Button btnMinus = mDialog.findViewById(R.id.btnMinus);
                Button btnPlus = mDialog.findViewById(R.id.btnPlus);
                TextView quantidade = mDialog.findViewById(R.id.textQuantity);
                quantidade.setText(String.valueOf(item.getQuantidade()));



                btnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decrement(item, quantidade);
                    }
                });

                btnPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        increment(item, quantidade);
                    }
                });
            }
        });
    }
    private void decrement(Item item, TextView textQuantity) {
        int quantity = item.getQuantity();
        if (quantity > 0) {
            item.setQuantity(quantity - 1);
            textQuantity.setText(String.valueOf(quantity - 1));
            notifyDataSetChanged();
        }
    }

    private void increment(Item item, TextView textQuantity) {
        int quantity = item.getQuantidade();
        item.setQuantidade(quantity + 1);
        textQuantity.setText(String.valueOf(item.getQuantidade()));
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome, preco, categoria;
        private Button btnMinus, btnPlus;
        Button buttonPopup;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.nome);
            preco = itemView.findViewById(R.id.preco);
            categoria = itemView.findViewById(R.id.categoria);
            buttonPopup = itemView.findViewById(R.id.buttonPopup);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            counter = 0;
        }
    }
}
