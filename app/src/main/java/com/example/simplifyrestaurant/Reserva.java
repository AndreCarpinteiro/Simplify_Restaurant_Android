package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import android.app.AlertDialog;
import android.widget.Toast;

public class Reserva extends AppCompatActivity {

    EditText etDate, qtdPessoas;
    DatePickerDialog.OnDateSetListener setListener;
    Spinner spinner;
    String dateSelected = "", horaSelecionada, datafinal;
    Button bt_reservar2;
    ArrayList<String> idMesasCReservaDataEscolhida = new ArrayList<>();
    Map<String, Object> mesaMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IniciarComponentes();

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                horaSelecionada = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        Reserva.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        String monthString = (month < 10) ? "0" + month : String.valueOf(month);
                        dateSelected = day + "/" + monthString + "/" + year;
                        etDate.setText(dateSelected);
                    }
                }, year, month, day);

                // Defina a data mínima como a data atual
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

                datePickerDialog.show();
            }
        });

        View rootView = findViewById(android.R.id.content);//Vou buscar a raiz do layout

        rootView.setOnTouchListener(new View.OnTouchListener() { //Esconder teclado quando toco fora
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });


        bt_reservar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                datafinal = dateSelected;
                datafinal += " " + horaSelecionada;
                datafinal += ":00";
                Calendar calendar = Calendar.getInstance();
                Date dataAtual = calendar.getTime();

                // Converter a data final para um objeto Date
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date dataFinal;

                //Verificar se os campos foram preenchidos
                if(dateSelected.isEmpty() || horaSelecionada.isEmpty() || qtdPessoas.getText().toString().isEmpty()){
                    AlertDialog.Builder cxMsg = createDialogWithMessage("Todos os campos devem ser preenchidos");
                    cxMsg.show();
                }
                try {
                    dataFinal = sdf.parse(datafinal);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return;
                }

                // Comparar as datas
                if (dataFinal.before(dataAtual)) {
                    // Data final é anterior à data atual (reserva no passado)
                    AlertDialog.Builder cxMsg3 = createDialogWithMessage("Data inválida");
                    cxMsg3.show();
                    return;
                }
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Reservas");
                    Query query = databaseReference.orderByChild("DataReserva").equalTo(datafinal);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            idMesasCReservaDataEscolhida.clear();
                            if (dataSnapshot.exists()) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String reserva = snapshot.getValue().toString();
                                    String snapshotAsString = snapshot.getValue().toString();
                                    String[] parts = snapshotAsString.split(",");
                                    String mesaIdPart = "";
                                    for (String part : parts) {
                                        if (part.trim().startsWith("MesaID=")) {
                                            mesaIdPart = part.trim();
                                            break;
                                        }
                                    }
                                    String mesaId = mesaIdPart.split("=")[1]; //Partir a string para ficar só vom o id
                                    idMesasCReservaDataEscolhida.add(mesaId);
                                }
                            } else {
                                System.out.println("Nenhuma reserva encontrada para a data selecionada"); //Msg teste
                            }
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Mesas");
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        boolean foundMesa = false; // Variável para indicar se uma mesa foi encontrada

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Map<String, Object> mesa = (Map<String, Object>) snapshot.getValue();
                                            String mesaId = mesa.get("ID").toString();
                                            mesaMap.put(mesaId, mesa);// Adiciona todas as mesas da bd ao mapa
                                        }
                                        for (Map.Entry<String, Object> entry : mesaMap.entrySet()) {
                                            String mesaId = entry.getKey();
                                            Object value = entry.getValue();

                                            if (value instanceof Map) {
                                                Map<String, Object> mesa = (Map<String, Object>) value;
                                                Object qtdPessoasNode = mesa.get("Capacidade");

                                                if (qtdPessoasNode != null) {
                                                    String qtdPessoasString = qtdPessoasNode.toString();
                                                    int qtdPessoasInt = Integer.parseInt(qtdPessoas.getText().toString());

                                                    if (qtdPessoasInt % 2 != 0) { //Se for reserva com pessoas impar, soma mais um para comparar
                                                        qtdPessoasInt += 1;
                                                    }

                                                    if (qtdPessoasString.equals(String.valueOf(qtdPessoasInt))) { //Compara quantidade de pessoas a reservar com capacidade das mesas
                                                        if (!idMesasCReservaDataEscolhida.contains(mesaId)) {

                                                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                            DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("ListaClientes").child(userId);

                                                            databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        String nome = snapshot.child("Nome").getValue(String.class);
                                                                        String apelido = snapshot.child("Apelido").getValue(String.class);
                                                                        String contacto = snapshot.child("ContactoTelefonico").getValue(String.class);
                                                                        String nif = snapshot.child("NIF").getValue(String.class);


                                                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Reservas");

                                                                        String idAutomatico = databaseReference.push().getKey(); // Obtém um ID automático do Firebase

                                                                        databaseReference.child(idAutomatico).child("Apelido").setValue(apelido);
                                                                        databaseReference.child(idAutomatico).child("ContactoTelefonico").setValue(contacto);
                                                                        databaseReference.child(idAutomatico).child("DataReserva").setValue(datafinal);
                                                                        databaseReference.child(idAutomatico).child("MesaID").setValue(mesaId);
                                                                        databaseReference.child(idAutomatico).child("NIF").setValue(nif);
                                                                        databaseReference.child(idAutomatico).child("Nome").setValue(nome);
                                                                        databaseReference.child(idAutomatico).child("IdCliente").setValue(userId);
                                                                        databaseReference.child(idAutomatico).child("NumeroPessoas").setValue(qtdPessoas.getText().toString());

                                                                        AlertDialog.Builder cxMsg = createDialogWithMessage("Mesa reservada com sucesso");
                                                                        cxMsg.show();
                                                                    } else {
                                                                        AlertDialog.Builder cxMsg2 = createDialogWithMessage("Não há mesas para essa lotação");
                                                                        cxMsg2.show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                            break;
                                                        } else {
                                                            AlertDialog.Builder cxMsg2 = createDialogWithMessage("Não há mesas para essa lotação");
                                                            cxMsg2.show();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!foundMesa) {
                                            // Mensagem quando nenhuma mesa foi encontrada
                                            AlertDialog.Builder cxMsg3 = createDialogWithMessage("Nenhuma mesa encontrada para essa lotação");
                                            cxMsg3.show();
                                        }
                                    } else {
                                        AlertDialog.Builder cxMsg2 = createDialogWithMessage("Não há mesas para essa hora");
                                        cxMsg2.show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Lidar com erros aqui
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Lidar com erros aqui
                        }
                    });
                    System.out.println("Entrou5");
            }
        });
    }

    private AlertDialog.Builder createDialogWithMessage(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Reserva.this);
        dialogBuilder.setMessage(message);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Reserva.this, EcraPrincipal.class);
                startActivity(intent);
                finish();
            }
        });

        return dialogBuilder;
    }
    private void IniciarComponentes() {
        setContentView(R.layout.activity_reserva);
        bt_reservar2 = findViewById(R.id.bt_reservar2);
        etDate = findViewById(R.id.et_date);
        spinner = findViewById(R.id.spinner);
        qtdPessoas = findViewById(R.id.text_qtdPessoas);
    }

}