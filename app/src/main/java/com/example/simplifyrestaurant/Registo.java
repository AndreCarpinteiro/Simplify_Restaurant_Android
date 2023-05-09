package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

import java.util.HashMap;
import java.util.Map;

public class Registo extends AppCompatActivity {

    private EditText edit_nome, edit_email, edit_senha;
    private Button btn_registar;
    String[]mensagens = {"Preencha todos os campos", "Criado com sucesso"};
    String utilizadorid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registo);

        //getSupportActionBar().hide();
        IniciarComponentes();

        btn_registar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = edit_nome.getText().toString();
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();


                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


                reference.child("ListaClientes").setValue(nome);

                if(nome.isEmpty() || email.isEmpty() || senha.isEmpty()){
                    Snackbar snackbar = Snackbar.make(v, mensagens[0], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else{
                    RegistarUtilizador(v);
                }
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

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> utilizadores = new HashMap<>();
        utilizadores.put("nome", nome);

        utilizadorid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference.child("ListaClientes").setValue(nome);
    }
    private void IniciarComponentes(){
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        btn_registar = findViewById(R.id.btn_registar);
    }
}