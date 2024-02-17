package com.example.pm1e12921;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ActivityLlamada extends AppCompatActivity {
    private static final int REQUEST_CALL_PERMISSION = 1;

    TextView tvNumero,tvNombre;
    Button btnLlamada, btnatras2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamada);


        tvNombre = findViewById(R.id.tvNombre);
        tvNumero = findViewById(R.id.tvNumero);
        btnLlamada = findViewById(R.id.btnLlamada);
        btnatras2 = findViewById(R.id.btnatras2);

        String nombre = getIntent().getStringExtra("nombre");
        String telefono = getIntent().getStringExtra("telefono");


        tvNombre.setText(nombre);
        tvNumero.setText(telefono);


        btnLlamada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(ActivityLlamada.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(ActivityLlamada.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
                } else {

                    String telefono = tvNumero.getText().toString();
                    Intent intentLlamada = new Intent(Intent.ACTION_CALL);
                    intentLlamada.setData(Uri.parse("tel:" + telefono));
                    startActivity(intentLlamada);
                }
            }
        });

        btnatras2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivitySalvados.class);
                startActivity(intent);
            }
        });
    }




}