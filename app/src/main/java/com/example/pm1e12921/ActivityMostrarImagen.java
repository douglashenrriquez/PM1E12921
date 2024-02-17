package com.example.pm1e12921;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e12921.Configuracion.SQLiteConexion;
import com.example.pm1e12921.Configuracion.Transaciones;

public class ActivityMostrarImagen extends AppCompatActivity {
    ImageView imageView;
    SQLiteConexion conexion;
    long itemId;

    Button btnatras3;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_imagen);

        imageView = findViewById(R.id.imageView4);
        btnatras3 = findViewById(R.id.btnatras3);
        conexion = new SQLiteConexion(this, Transaciones.DBName, null, 1);

        itemId = getIntent().getLongExtra("itemId", -1);

        if (itemId != -1) {
            String uriString = obtenerUriImagenDesdeBaseDeDatos(itemId);

            if (!uriString.isEmpty()) {
                Uri uri = Uri.parse(uriString);
                imageView.setImageURI(uri);
            } else {
                Toast.makeText(getApplicationContext(), "No se pudo encontrar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No se proporcionó una identificación de registro válida", Toast.LENGTH_SHORT).show();
        }


        btnatras3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivitySalvados.class);
                startActivity(intent);
            }
        });
    }

    private String obtenerUriImagenDesdeBaseDeDatos(long itemId) {
        String uriString = "";

        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Cursor cursor = db.rawQuery(Transaciones.SelectUriImagen + " WHERE " + Transaciones.id + "=?", new String[]{String.valueOf(itemId)});

            if (cursor.moveToFirst()) {
                uriString = cursor.getString(cursor.getColumnIndexOrThrow(Transaciones.uri_imagen));
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uriString;
    }
}
