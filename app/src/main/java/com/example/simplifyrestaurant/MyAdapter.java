package com.example.simplifyrestaurant;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MyAdapter  extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Item> list;
    private List<HashMap<String, Object>> selectedItems;
    private List<Item> filteredList;
    Dialog mDialog;
    private Button btnFinalizar;
    String UserId;
    private static int counter = 1;
    private AppCompatActivity activity;
    public MyAdapter(Context context, ArrayList<Item> list, Button btnFinalizar, AppCompatActivity activity) {
        this.context = context;
        this.list = list;
        this.filteredList = new ArrayList<>();
        this.UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.btnFinalizar = btnFinalizar;
        this.selectedItems = new ArrayList<>();
        this.activity = activity;
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
        holder.preco.setText(item.getPreco() + "\u20AC");
        holder.buttonPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new Dialog(context);
                mDialog.setContentView(R.layout.popup);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show(); // Exibe o diálogo pop-up

                Button btnMinus = mDialog.findViewById(R.id.btnMinus);
                Button btnPlus = mDialog.findViewById(R.id.btnPlus);
                TextView quantidade = mDialog.findViewById(R.id.textQuantity);
                quantidade.setText(String.valueOf(holder.getCurrentQuantity()));
                TextView popupNome = mDialog.findViewById(R.id.popnome);
                TextView popupDescricao = mDialog.findViewById(R.id.popdescricao);
                //TextView popupCategoria = mDialog.findViewById(R.id.popcategoria);
                TextView popupPreco = mDialog.findViewById(R.id.txtPreco);
                ImageView img = mDialog.findViewById(R.id.imgComida);
                popupNome.setText(item.getNome());
                popupDescricao.setText("Descrição: " +String.valueOf(item.getDescricao()));
                //popupCategoria.setText(item.getCategoria());
                int quantidadeProdutos = Integer.parseInt(quantidade.getText().toString());
                String precoString = item.getPreco().replace(",", ".");
                double precoProduto = Double.parseDouble(precoString);
                double precoTotal = precoProduto * quantidadeProdutos;
                String precoFormatado = String.format("%.2f€", precoProduto);
                String precoTotalFormatado = String.format("%.2f€", precoTotal);
                String textoPreco = precoFormatado + " x " + quantidadeProdutos + " = " + precoTotalFormatado;
                popupPreco.setText(textoPreco);

                // Converter a imagem base64 em um Bitmap
                byte[] decodedString = Base64.decode(item.getImg(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Definir o Bitmap no ImageView
                img.setImageBitmap(decodedBitmap);

                Button btnConfirmar = mDialog.findViewById(R.id.btnConfirmar);
                DatabaseReference pedidoRef = FirebaseDatabase.getInstance().getReference("Pedidos").push();
                HashMap<String, Object> pedidoItem = new HashMap<>();

                btnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decrement(item, quantidade, holder);
                        popupPreco.setText(getPrecoTotal(item, holder.getCurrentQuantity()));
                    }
                });

                btnPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        increment(item, quantidade, holder);
                        popupPreco.setText(getPrecoTotal(item, holder.getCurrentQuantity()));
                    }
                });

                btnConfirmar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, Object> pedidoItem = new HashMap<>();
                        pedidoItem.put("Nome", item.getNome());
                        pedidoItem.put("Quantidade", holder.getCurrentQuantity());
                        pedidoItem.put("Preco", item.getPreco());
                        pedidoItem.put("User", UserId);
                        pedidoItem.put("Categoria", item.getCategoria());
                        Date dataAtual = new Date();
                        pedidoItem.put("Data", dataAtual);
                        selectedItems.add(pedidoItem);
                        quantidade.setText("1");
                        mDialog.dismiss();
                    }
                });
            }
        });
        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference pedidoRef = FirebaseDatabase.getInstance().getReference("Pedidos");
                if (!selectedItems.isEmpty()) {
                    for (HashMap<String, Object> pedidoItem : selectedItems) {
                        pedidoRef.push().setValue(pedidoItem);
                    }

                    // Montar a mensagem com os itens selecionados
                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("Itens selecionados:\n");
                    for (HashMap<String, Object> item : selectedItems) {
                        String nome = (String) item.get("Nome");
                        int quantidade = (int) item.get("Quantidade");
                        String precoString = (String) item.get("Preco");
                        precoString = precoString.replace(",", ".");
                        double preco = Double.parseDouble(precoString);

                        mensagem.append("- ");
                        mensagem.append(nome);
                        mensagem.append(" x ");
                        mensagem.append(quantidade);
                        mensagem.append(" = ");
                        mensagem.append(String.format("%.2f€", preco * quantidade));
                        mensagem.append("\n");
                    }
                    selectedItems.clear();
                    mDialog.dismiss();
                    showAlertDialog("Pedido com sucesso", mensagem.toString());
                }else{
                    showAlertDialog("Mensagem", "Nenhum item selecionado!");
                }
            }
        });
    }
    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.finish(); // Fechar a atividade após a confirmação
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void decrement(Item item, TextView textQuantity, MyViewHolder holder) {
        int quantity = holder.getCurrentQuantity();
        if (quantity > 1) {
            holder.setCurrentQuantity(quantity - 1);
            textQuantity.setText(String.valueOf(quantity - 1));
        }
    }

    private void increment(Item item, TextView textQuantity, MyViewHolder holder) {
        int quantity = holder.getCurrentQuantity();
        holder.setCurrentQuantity(quantity + 1);
        textQuantity.setText(String.valueOf(holder.getCurrentQuantity()));
    }

    private String getPrecoTotal(Item item, int quantidadeProdutos) {
        double precoProduto = Double.parseDouble(item.getPreco().replace(",", "."));
        double precoTotal = precoProduto * quantidadeProdutos;
        return String.format("%.2f€ x %d = %.2f€", precoProduto, quantidadeProdutos, precoTotal);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome, preco, categoria;
        private Button btnMinus, btnPlus;
        Button buttonPopup;
        private int currentQuantity;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            currentQuantity = 1;
            nome = itemView.findViewById(R.id.nome);
            preco = itemView.findViewById(R.id.preco);
            categoria = itemView.findViewById(R.id.categoria);
            buttonPopup = itemView.findViewById(R.id.buttonPopup);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            counter = 1;
        }
        public int getCurrentQuantity() {
            return currentQuantity;
        }

        public void setCurrentQuantity(int currentQuantity) {
            this.currentQuantity = currentQuantity;
        }
    }
}
