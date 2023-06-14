package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class EcraPrincipal extends AppCompatActivity {

    private TextView nomeUtilizador, emailUtlizador;
    private Button btn_sair, btn_reserva, btn_pedido, btn_acompanhaPedido;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    ArrayList<Item> list;
    String userId;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecra_principal);
        IniciarComponentes();
        //getSupportActionBar().hide();
        databaseReference = FirebaseDatabase.getInstance().getReference("Pedidos");

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
                DatabaseReference reservaRef = FirebaseDatabase.getInstance().getReference("Reservas");
                final boolean[] hasReserva = {false};
                final boolean[] hora = {false};
                reservaRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            for (DataSnapshot reservaSnapshot : snapshot.getChildren()) {
                                String clienteId = reservaSnapshot.child("IdCliente").getValue(String.class);

                                if (clienteId != null && clienteId.equals(currentUserId)) {
                                    String dataReserva = reservaSnapshot.child("DataReserva").getValue(String.class);
                                    hasReserva[0] = true;
                                    if (dataReserva != null) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                        try {
                                            Date reservaDate = sdf.parse(dataReserva);
                                            long reservaTimeMillis = reservaDate.getTime();

                                            long currentTimeMillis = System.currentTimeMillis();
                                            long timeDifferenceMillis = currentTimeMillis - reservaTimeMillis;
                                            long timeDifferenceHours = Math.abs(timeDifferenceMillis / (1000 * 60 * 60));

                                            if (timeDifferenceHours >= 0 && timeDifferenceHours <= 24) {
                                                hora[0] = true;
                                                break;
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        if (hora[0] && hasReserva[0]) {
                            // O cliente tem uma reserva válida
                            Intent intent = new Intent(EcraPrincipal.this, Pedido.class);
                            startActivity(intent);
                        } else {
                            if (!hasReserva[0]) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(EcraPrincipal.this);
                                builder.setTitle("Sem reserva");
                                builder.setMessage("Você precisa fazer uma reserva antes de fazer um pedido.");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                return;
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(EcraPrincipal.this);
                                builder.setTitle("Pedido não permitido");
                                builder.setMessage("Você só pode fazer um pedido 5h antes da reserva.");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Ação a ser executada quando o botão "OK" for clicado
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                return;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        list = new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Item item = dataSnapshot.getValue(Item.class);
                    list.add(item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btn_acompanhaPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasRecentOrder = false;
                long currentTimeMillis = System.currentTimeMillis();

                for (Item item : list) {
                    if (item.getUser().equals(userId)) {
                        Date orderTime = item.getData();

                        if (orderTime != null) { // Verificar se a data não é nula
                            long timeDifferenceMillis = currentTimeMillis - orderTime.getTime();
                            long timeDifferenceHours = timeDifferenceMillis / (1000 * 60 * 60);

                            if (timeDifferenceHours < 3) {
                                hasRecentOrder = true;
                                break;
                            }
                        }
                    }
                }

                if (hasRecentOrder) {
                    // O utilizador tem um pedido recente
                    Intent intent = new Intent(EcraPrincipal.this, AcompanharPedido.class);
                    startActivity(intent);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(EcraPrincipal.this);
                    builder.setTitle("Sem pedidos");
                    builder.setMessage("Ainda não efetuou qualquer pedido");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
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

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }
    private void IniciarComponentes(){
        nomeUtilizador = findViewById(R.id.textNomeUtilizador);
        emailUtlizador = findViewById(R.id.textEmailUtilizador);
        btn_sair = findViewById(R.id.btn_sair);
        btn_reserva = findViewById(R.id.btn_reservar);
        btn_pedido = findViewById(R.id.btn_pedido);
        btn_acompanhaPedido = findViewById(R.id.btn_acompanharPedido);
    }
}