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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);

        recyclerView = findViewById(R.id.pruductList);
        databaseReference = FirebaseDatabase.getInstance().getReference("Produtos");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        myAdapter = new MyAdapter(this,list);
        recyclerView.setAdapter(myAdapter);
        searchEditText = findViewById(R.id.searchEditText);

       /* LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.item, null);
        Button buttonPopup = layout.findViewById(R.id.buttonPopup);*/

       // mDialog = new Dialog(this);

        /*buttonPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setContentView(R.layout.popup);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show(); // Exibe o diálogo pop-up
            }
        });*/
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //String searchText = s.toString().toLowerCase();
                //filterItems(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                filterItems(query);
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //list.clear(); // Limpa a lista antes de adicionar os itens filtrados
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Item item = dataSnapshot.getValue(Item.class);
                    list.add(item);
                }
                myAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void filterItems(String searchText) {
        filteredList = new ArrayList<>(); // Inicializa a lista filteredList

        for (Item item : list) {
            if (item.getNome().toLowerCase().contains(searchText)) {
                filteredList.add(item);
            }
        }
        myAdapter.filterList(filteredList);
    }
}