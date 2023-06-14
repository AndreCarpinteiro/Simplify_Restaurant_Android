package com.example.simplifyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Pagamento extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 123;
    DatabaseReference databaseReference;
    ArrayList<Item> list;
    Item item;
    private List<HashMap<String, Object>> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        CardView card1 = findViewById(R.id.cardmbway);
        CardView card2 = findViewById(R.id.cardstripe);
        list = new ArrayList<>();
        String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Pedidos");

        calcularValorTotal();

        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcurarPedido();
            }
        });
    }

    private void ProcurarPedido() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reservasRef = FirebaseDatabase.getInstance().getReference().child("Reservas");
        Query query = reservasRef.orderByChild("IdCliente").equalTo(userId).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                    break; // Remover apenas a primeira reserva (a mais antiga)
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Erro ao acessar o banco de dados
            }
        });

        DatabaseReference pedidosRef = FirebaseDatabase.getInstance().getReference().child("Pedidos");
        pedidosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ClassePedido pedido = dataSnapshot.getValue(ClassePedido.class);
                    if (pedido != null && pedido.getUser().equals(userId)) {
                        dataSnapshot.getRef().removeValue(); // Remove o pedido relacionado ao utilizador
                    }
                }
                generatePdf();
                openPdf();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Erro ao obter os pedidos do Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Item> itemList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                generatePdf();
                openPdf();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Erro ao obter os itens do Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void generatePdf() {
        // Verifica as permissões de armazenamento
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
                return; // Retorna para aguardar a resposta de permissão
            }
        }

        // Cria um novo documento PDF
        PdfDocument document = new PdfDocument();

        // Define as configurações da página
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(500, 800, 1).create();

        // Cria uma nova página
        PdfDocument.Page page = document.startPage(pageInfo);

        // Obtém o canvas para desenhar na página
        Canvas canvas = page.getCanvas();

        // Define a cor de fundo da página
        canvas.drawColor(Color.WHITE);

        // Define a cor e a configuração da fonte
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20f);
        // Definir as coordenadas iniciais para o desenho do texto
        float x = 50;
        float y = 50; // Definir a posição no fundo da página


        // Desenhar o cabeçalho do recibo
        paint.setTextSize(40f);
        canvas.drawText("Restaurante Simplify", x, y, paint);
        y += 70;

        paint.setTextSize(30f);
        canvas.drawText("Informações do consumo:", x, y, paint);
        y += 50;
        paint.setTextSize(30f);
        canvas.drawText("Item 1:", x, y, paint);
        y += 25;
        paint.setTextSize(30f);
        canvas.drawText("Descrição do item 1", x, y, paint);
        y += 25;
        paint.setTextSize(30f);
        canvas.drawText("Item 2:", x, y, paint);
        y += 25;
        paint.setTextSize(30f);
        canvas.drawText("Descrição do item 2", x, y, paint);
        y += 50;


        paint.setTextSize(30f);
        y += 40;
        canvas.drawText("Total: 100,00", x, y, paint);
        y += 30;
        paint.setTextSize(20f);

        // Desenhar a morada no fundo do PDF
        y += 25;
        paint.setTextSize(20f);
        canvas.drawText("Obrigado pela visita!", x, y, paint);
        paint.setTextSize(15f);
        y = pageInfo.getPageHeight() - 110;
        canvas.drawText("Rua Principal", x, y, paint);
        y += 25;
        canvas.drawText("Estádio do Dragão 4350-415 Porto, Portugal", x, y, paint);
        y += 25;
        canvas.drawText("Telefone: +351 22 557 04 00", x, y, paint);
        y += 25;
        canvas.drawText("Fax: +351 22 557 04 98", x, y, paint);

        document.finishPage(page);

        // Define o caminho de destino para o arquivo PDF
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "consumo.pdf");

        try {
            // Cria o arquivo PDF
            document.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(getApplicationContext(), "PDF criado com sucesso", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Erro ao criar PDF", Toast.LENGTH_SHORT).show();
        }

        // Fecha o documento
        document.close();
    }
    private void openPdf() {
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "consumo.pdf");

        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Nenhum aplicativo para visualizar PDF encontrado", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdf();
            } else {
                Toast.makeText(getApplicationContext(), "Permissão de armazenamento negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void calcularValorTotal() {
        DatabaseReference pedidosRef = FirebaseDatabase.getInstance().getReference().child("Pedidos");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        pedidosRef.orderByChild("User").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double valorTotal = 0.0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ClassePedido pedido = dataSnapshot.getValue(ClassePedido.class);
                    if (pedido != null) {
                        String precoString = pedido.getPreco();
                        precoString = precoString.replace(",", ".");
                        double preco = Double.parseDouble(precoString);
                        valorTotal += preco;
                    }
                }

                // Formata o valor com uma casa decimal a mais
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                String valorTotalFormatado = decimalFormat.format(valorTotal);

                TextView txtValorTotal = findViewById(R.id.txtValorPagar);
                txtValorTotal.setText("Valor a pagar: " + valorTotalFormatado + "€");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Tratar erro ao acessar o banco de dados
            }
        });
    }
}