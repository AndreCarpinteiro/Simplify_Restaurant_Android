package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Registo extends AppCompatActivity {

    private EditText edit_nome, edit_email, edit_senha, edit_contacto, edit_morada, edit_nif;

    private TextView edit_datanasc;
    private Button btn_registar;
    DatePickerDialog.OnDateSetListener setListener;
    String[]mensagens = {"Preencha todos os campos", "Criado com sucesso"};
    String utilizadorid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registo);

        //getSupportActionBar().hide();
        IniciarComponentes();

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        edit_datanasc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        Registo.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = day+"/"+month+"/"+year;
                edit_datanasc.setText(date);
            }
        };

        btn_registar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = edit_nome.getText().toString();
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();
                String contacto = edit_contacto.getText().toString();
                String morada = edit_morada.getText().toString();
                String datanasc = edit_datanasc.getText().toString();
                String nif = edit_nif.getText().toString();

                if(nome.isEmpty() || email.isEmpty() || senha.isEmpty() || contacto.isEmpty() ||
                morada.isEmpty() || datanasc.isEmpty() || nif.isEmpty()){
                    Snackbar snackbar = Snackbar.make(v, mensagens[0], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else{
                    RegistarUtilizador(v);
                }
            }
        });


        View rootView = findViewById(R.id.registo);//Vou buscar a raiz do layout

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });


    }

    private void RegistarUtilizador(View v){
        String email = edit_email.getText().toString(); //Vou buscar o conteudo das text boxes
        String senha = edit_senha.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){ //O task tem o resultado emitido do firebase

                    SalvarDadosUtilizador();

                    Snackbar snackbar = Snackbar.make(v, mensagens[1], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else{
                    String erro;
                    try {
                        throw task.getException();

                    }catch(FirebaseAuthWeakPasswordException e){
                        erro = "Digite uma senha com mais de 6 caracteres";
                    }catch(FirebaseAuthUserCollisionException e) {
                        erro = "Este email já consta na base de dados";
                    }catch(FirebaseAuthInvalidCredentialsException e) {
                        erro = "Email inválido";
                    } catch(Exception e){
                        erro = "Erro ao registar";
                    }
                    Snackbar snackbar = Snackbar.make(v, erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }

    private void SalvarDadosUtilizador(){
        String nome = edit_nome.getText().toString();
        String email = edit_email.getText().toString();
        String contacto = edit_contacto.getText().toString();
        String morada = edit_morada.getText().toString();
        String datanasc = edit_datanasc.getText().toString();
        String nif = edit_nif.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        int meio = nome.length() / 2;
        String primeiraMetade = nome.substring(0, meio);
        String segundaMetade = nome.substring(meio);


        reference.child("ListaClientes").child(userId).child("Apelido").setValue(segundaMetade);
        reference.child("ListaClientes").child(userId).child("ContactoTelefonico").setValue(contacto);
        reference.child("ListaClientes").child(userId).child("DataNascimento").setValue(datanasc);
        reference.child("ListaClientes").child(userId).child("Email").setValue(email);
        reference.child("ListaClientes").child(userId).child("Endereco").setValue(morada);
        reference.child("ListaClientes").child(userId).child("Genero").setValue("Masculino");
        reference.child("ListaClientes").child(userId).child("NIF").setValue(nif);
        reference.child("ListaClientes").child(userId).child("Nome").setValue(primeiraMetade);
        reference.child("ListaClientes").child(userId).child("Id").setValue(userId);
    }
    private void IniciarComponentes(){
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        edit_contacto = findViewById(R.id.edit_contacto);
        edit_morada = findViewById(R.id.edit_morada);
        edit_datanasc = findViewById(R.id.edit_data);
        edit_nif = findViewById(R.id.edit_nif);
        btn_registar = findViewById(R.id.btn_registar);
    }
}