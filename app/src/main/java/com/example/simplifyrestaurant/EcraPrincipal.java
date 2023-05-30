package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;

import java.util.Map;

public class EcraPrincipal extends AppCompatActivity {

    private TextView nomeUtilizador, emailUtlizador;
    private Button btn_sair, btn_reserva, btn_pedido;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecra_principal);
        IniciarComponentes();
        //getSupportActionBar().hide();

        btn_reserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EcraPrincipal.this, Reserva.class);//encaminhar para a atividade da reserva
                startActivity(intent);
            }
        });
        btn_sair.setOnClickListener(new View.OnClickListener() { //logOut da app
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EcraPrincipal.this, LoginActivity.class);//depois de logout reencaminhar para ecrã login
                startActivity(intent);
                finish();
            }
        });

        btn_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EcraPrincipal.this, Pedido.class);//encaminhar para a atividade do pedido
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ListaClientes").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map != null && map.containsKey("Nome")){
                        String nome = (String) map.get("Nome") + map.get("Apelido");
                        nomeUtilizador.setText(nome);
                        emailUtlizador.setText(email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Erro ao procurar utilizador");
            }
        });
    }

    private void IniciarComponentes(){
        nomeUtilizador = findViewById(R.id.textNomeUtilizador);
        emailUtlizador = findViewById(R.id.textEmailUtilizador);
        btn_sair = findViewById(R.id.btn_sair);
        btn_reserva = findViewById(R.id.btn_reservar);
        btn_pedido = findViewById(R.id.btn_pedido);
    }
}