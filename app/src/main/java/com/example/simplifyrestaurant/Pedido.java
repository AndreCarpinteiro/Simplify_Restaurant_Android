package com.example.simplifyrestaurant;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Pedido extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchEditText;
    DatabaseReference databaseReference;
    MyAdapter myAdapter;
    ArrayList<Item> list;
    ArrayList<Item> filteredList;
    Dialog mDialog;
    private ArrayList<Item> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);

        recyclerView = findViewById(R.id.pruductList);
        databaseReference = FirebaseDatabase.getInstance().getReference("Produtos");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnFinalizar = findViewById(R.id.btnFinalizar);
        list = new ArrayList<>();
        myAdapter = new MyAdapter(this,list, btnFinalizar, Pedido.this);
        recyclerView.setAdapter(myAdapter);
        TextView textViewPratoRefeicao = findViewById(R.id.textViewRefeicao);
        TextView textViewBebida = findViewById(R.id.textViewBebida);
        TextView textViewSobremesa = findViewById(R.id.textViewSobremesa);


        textViewPratoRefeicao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterItemsByCategory("Prato");
            }
        });

        textViewBebida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterItemsByCategory("Bebida");
            }
        });

        textViewSobremesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterItemsByCategory("Sobremesa");
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allItems = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Item item = dataSnapshot.getValue(Item.class);
                    allItems.add(item);
                }
                updateRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void filterItemsByCategory(String category) {
        list.clear(); // Limpa a lista antes de adicionar os itens filtrados

        for (Item item : allItems) {
            if (item.getCategoria().equalsIgnoreCase(category)) {
                list.add(item);
            }
        }
        // Atualize o RecyclerView após filtrar os itens
        updateRecyclerView();
    }
    private void updateRecyclerView() {
        myAdapter.notifyDataSetChanged();
    }
}