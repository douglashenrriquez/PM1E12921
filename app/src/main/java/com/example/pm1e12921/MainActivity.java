package com.example.pm1e12921;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pm1e12921.Configuracion.SQLiteConexion;
import com.example.pm1e12921.Configuracion.Transaciones;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    static final int permiso_camara = 100;
    static final int peticion_foto = 102;
    static final int seleccionar_foto = 103;
    private Bitmap fotoBitmap;

    EditText nombres, telefono, nota;
    Spinner pais;
    ImageView imageView;
    Button btnsalvar,btnsalvados,btnfoto;

     Uri imageUri;
    String idPersona = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pais = (Spinner) findViewById(R.id.pais);
        nombres = (EditText) findViewById(R.id.nombre);
        telefono = (EditText) findViewById(R.id.telefono);
        nota = (EditText) findViewById(R.id.nota);
        btnsalvar = (Button) findViewById(R.id.btnsalvar);
        btnsalvados = (Button) findViewById(R.id.btnsalvados);
        btnfoto = (Button) findViewById(R.id.btnfoto);
        imageView = (ImageView) findViewById(R.id.imageView);

        String[] paises = {"Honduras(+504)", "Guatemala(+502)", "Costa Rica(+506)","Belize(+501)","Nicaragua(+505)","Panamá(+507)","El Salvador(+503)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pais.setAdapter(adapter);

        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogo();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            idPersona = intent.getStringExtra("id");
            nombres.setText(intent.getStringExtra("nombre"));
            telefono.setText(String.valueOf(intent.getIntExtra("telefono", 0)));
            nota.setText(intent.getStringExtra("nota"));

            String paisSeleccionado = intent.getStringExtra("pais");

            if (pais.getAdapter() instanceof ArrayAdapter) {
                adapter = (ArrayAdapter<String>) pais.getAdapter();
            } else {
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
                pais.setAdapter(adapter);
            }

            int position = adapter.getPosition(paisSeleccionado);
            pais.setSelection(position);
        }

        btnsalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("id")) {
                    updatePerson();
                    Intent intent = new Intent(MainActivity.this, ActivitySalvados.class);
                    startActivity(intent);
                } else {
                    AddPerson();
                }
            }
        });

        btnsalvados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivitySalvados.class);
                startActivity(intent);
            }
        });
    }

    private void AddPerson() {
        String nombre = nombres.getText().toString().trim();
        String telefonoString = telefono.getText().toString().trim();
        String notaString = nota.getText().toString().trim();

        if (nombre.isEmpty()) {
            showAlert("Debe escribir un nombre");
            return;
        } else if (telefonoString.isEmpty()) {
            showAlert("Debe escribir un teléfono");
            return;
        } else if (notaString.isEmpty()) {
            showAlert("Debe escribir una nota");
            return;
        } else if (imageView.getDrawable() == null) {
            showAlert("Debe seleccionar una imagen");
            return;
        } else {
            SQLiteConexion conexion = new SQLiteConexion(this, Transaciones.DBName, null, 1);
            SQLiteDatabase db = conexion.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put(Transaciones.pais, pais.getSelectedItem().toString());
            valores.put(Transaciones.nombre, nombre);
            valores.put(Transaciones.telefono, telefonoString);
            valores.put(Transaciones.nota, notaString);
            valores.put(Transaciones.uri_imagen, imageUri.toString()); // Guardar la URI de la imagen

            Long resultado = db.insert(Transaciones.Tablepersonas, Transaciones.id, valores);

            Toast.makeText(getApplicationContext(), "Registro Ingresado Correctamente " + resultado.toString(), Toast.LENGTH_SHORT).show();

            db.close();
            nombres.setText("");
            telefono.setText("");
            nota.setText("");
            imageView.setImageDrawable(null);
        }
    }



    private void mostrarDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar una opción")
                .setItems(new CharSequence[]{"Tomar foto", "Seleccionar de la galería"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                permisos();
                                break;
                            case 1:
                                seleccionarDeGaleria();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private void seleccionarDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, seleccionar_foto);
    }

    private void permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, permiso_camara);
        } else {
            tomarfoto();
        }
    }

    private void tomarfoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, peticion_foto);
    }

    private void guardarFotoEnGaleria(Bitmap bitmap) {
        String nombreImagen = "foto_" + System.currentTimeMillis() + ".jpg";
        String rutaImagen = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, nombreImagen, null);

        if (rutaImagen != null) {
            Intent intentScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uriImagen = Uri.parse(rutaImagen);
            intentScan.setData(uriImagen);
            sendBroadcast(intentScan);

            Toast.makeText(getApplicationContext(), "Foto guardada en la galería", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Error al guardar la foto en la galería", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permiso_camara) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarfoto();
            } else {
                Toast.makeText(getApplicationContext(), "PERMISO DENEGADO", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == peticion_foto) {
                Bundle extras = data.getExtras();
                Bitmap imagen = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imagen);

                guardarFotoEnGaleria(imagen);
                imageUri = getImageUri(imagen);
            } else if (requestCode == seleccionar_foto) {
                Uri path = data.getData();
                imageView.setImageURI(path);
                imageUri = path;
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updatePerson() {
        String nombre = nombres.getText().toString().trim();
        String telefonoString = telefono.getText().toString().trim();
        String notaString = nota.getText().toString().trim();
        String paisSeleccionado = pais.getSelectedItem().toString();

        if (nombre.isEmpty() || telefonoString.isEmpty() || notaString.isEmpty()) {
            showAlert("Todos los campos son obligatorios");
            return;
        }

        SQLiteConexion conexion = new SQLiteConexion(this, Transaciones.DBName, null, 1);
        SQLiteDatabase db = conexion.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put(Transaciones.nombre, nombre);
        valores.put(Transaciones.telefono, telefonoString);
        valores.put(Transaciones.nota, notaString);
        valores.put(Transaciones.pais, paisSeleccionado);

        int filasAfectadas = db.update(Transaciones.Tablepersonas, valores, Transaciones.id + "=?", new String[]{idPersona});
        db.close();

        if (filasAfectadas > 0) {
            Toast.makeText(getApplicationContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
        }
    }





}