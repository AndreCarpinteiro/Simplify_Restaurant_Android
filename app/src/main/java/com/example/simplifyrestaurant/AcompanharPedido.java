package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AcompanharPedido extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchEditText;
    DatabaseReference databaseReference;
    MyAdapter myAdapter;
    ArrayList<Item> list;
    ArrayList<Item> filteredList;
    Dialog mDialog;
    Button btnFinalizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acompanhar_pedido);

        recyclerView = findViewById(R.id.pruductListPed);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        databaseReference = FirebaseDatabase.getInstance().getReference("Pedidos");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Item> tempList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    tempList.add(item);
                }

                RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                        return new RecyclerView.ViewHolder(view) {};
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        Item item = tempList.get(position);

                        TextView nomeTextView = holder.itemView.findViewById(R.id.nome);
                        TextView precoTextView = holder.itemView.findViewById(R.id.preco);
                        TextView categoriaTextView = holder.itemView.findViewById(R.id.categoria);

                        nomeTextView.setText(item.getNome());
                        precoTextView.setText(String.valueOf(item.getPreco()) + "€");
                        categoriaTextView.setText(item.getCategoria());
                    }

                    @Override
                    public int getItemCount() {
                        return tempList.size();
                    }
                };
                recyclerView.setAdapter(adapter);
                double totalPedidos = 0.0;
                for (Item item : tempList) {
                    double preco = Double.parseDouble(item.getPreco().replaceAll(",", "."));
                    int quantidade = item.getQuantidade();
                    totalPedidos += preco * quantidade;
                }

                TextView totalTextView = findViewById(R.id.txtValorTotal);
                totalTextView.setText(Html.fromHtml(String.format("<font face='monospace'>Total atual: %.2f€</font>", totalPedidos)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AcompanharPedido.this, Pagamento.class);
                startActivity(intent);
            }
        });
    }
}