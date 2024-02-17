package com.example.pm1e12921;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pm1e12921.Configuracion.SQLiteConexion;
import com.example.pm1e12921.Configuracion.Transaciones;
import com.example.pm1e12921.Models.Personas;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ActivitySalvados extends AppCompatActivity {
    SQLiteConexion conexion;
    ListView listpersonas;
    Button btncompartir, btnverimagen, btneliminar, btnactualizar, btnatras;
    ArrayList<Personas> Lista;
    ArrayList<String> Arreglo;
    int posicionSeleccionada = -1;

    SearchView buscar;
    ArrayAdapter<String> adapter;
    ArrayList<String> listaFiltrada;

    private static final int REQUEST_CALL_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salvados);
        conexion = new SQLiteConexion(this, Transaciones.DBName, null, 1);
        listpersonas = findViewById(R.id.listpersonas);
        btncompartir = findViewById(R.id.btncompartir);
        btnverimagen = findViewById(R.id.btnverimagen);
        btneliminar = findViewById(R.id.btneliminar);
        btnactualizar = findViewById(R.id.btnactualizar);
        btnatras = findViewById(R.id.btnatras);

        buscar = findViewById(R.id.buscar);
        buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarLista(newText);
                return true;
            }
        });

        ObtenerInfo();

        listpersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                posicionSeleccionada = position;
                Personas personaSeleccionada = Lista.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySalvados.this);
                builder.setTitle("Confirmar llamada");
                builder.setMessage("¿Deseas llamar a " + personaSeleccionada.getTelefono() + "?");
                builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkCallPermission()) {
                            makePhoneCall();
                        }
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
            }
        });

        btneliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posicionSeleccionada != -1) {
                    eliminarPersona(posicionSeleccionada);
                    Lista.remove(posicionSeleccionada);
                    Arreglo.remove(posicionSeleccionada);

                    adapter = new ArrayAdapter<>(ActivitySalvados.this, android.R.layout.simple_list_item_1, Arreglo);
                    listpersonas.setAdapter(adapter);

                    Toast.makeText(getApplicationContext(), "Contacto eliminado", Toast.LENGTH_SHORT).show();
                    posicionSeleccionada = -1;
                } else {
                    Toast.makeText(getApplicationContext(), "Ninguna Contacto seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnatras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        btncompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posicionSeleccionada != -1) {
                    compartirDatos(posicionSeleccionada);
                } else {
                    Toast.makeText(getApplicationContext(), "Ninguna Contacto seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posicionSeleccionada != -1) {
                    Personas personaSeleccionada = Lista.get(posicionSeleccionada);
                    Intent intent = new Intent(ActivitySalvados.this, MainActivity.class);
                    intent.putExtra("id", personaSeleccionada.getId().toString());
                    intent.putExtra("pais", personaSeleccionada.getPais());
                    intent.putExtra("nombre", personaSeleccionada.getNombre());
                    intent.putExtra("telefono", personaSeleccionada.getTelefono());
                    intent.putExtra("nota", personaSeleccionada.getNota());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Ninguna Contacto seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnverimagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posicionSeleccionada != -1) {
                    Personas personaSeleccionada = Lista.get(posicionSeleccionada);
                    long idSeleccionado = personaSeleccionada.getId();

                    Intent intent = new Intent(ActivitySalvados.this, ActivityMostrarImagen.class);
                    intent.putExtra("itemId", idSeleccionado);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Ninguna persona seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private Uri obtenerUriImagen(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    private void filtrarLista(String texto) {
        listaFiltrada.clear();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(Arreglo);
        } else {
            texto = texto.toLowerCase();
            for (String item : Arreglo) {
                if (item.toLowerCase().contains(texto)) {
                    listaFiltrada.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void ObtenerInfo() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        Personas person = null;
        Lista = new ArrayList<>();
        Arreglo = new ArrayList<>();

        Cursor cursor = db.rawQuery(Transaciones.SelectAllPersonas, null);

        while(cursor.moveToNext()) {
            person = new Personas();
            person.setId(cursor.getInt(0));
            person.setPais(cursor.getString(1));
            person.setNombre(cursor.getString(2));
            person.setTelefono(cursor.getInt(3));
            person.setNota(cursor.getString(4));

            Lista.add(person);
            Arreglo.add(person.getId() + " - " + person.getPais()+ " - " + person.getNombre()+ " - " + person.getTelefono() + " - " + person.getNota());
        }
        cursor.close();

        listaFiltrada = new ArrayList<>(Arreglo); // Inicialización de listaFiltrada con la lista completa

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFiltrada);
        listpersonas.setAdapter(adapter);
    }

    private boolean checkCallPermission() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permiso denegado para llamar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makePhoneCall() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar acción");
        builder.setMessage("¿Estás seguro de que deseas realizar esta acción?");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Personas personaSeleccionada = Lista.get(posicionSeleccionada);

                Intent intent = new Intent(ActivitySalvados.this, ActivityLlamada.class);

                intent.putExtra("nombre", personaSeleccionada.getNombre());
                intent.putExtra("telefono", String.valueOf(personaSeleccionada.getTelefono()));

                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void eliminarPersona(int posicion) {
        Personas personaSeleccionada = Lista.get(posicion);
        SQLiteDatabase db = conexion.getWritableDatabase();
        db.delete(Transaciones.Tablepersonas, Transaciones.id + "=?", new String[]{String.valueOf(personaSeleccionada.getId())});
        db.close();
    }

    private void compartirDatos(int posicion) {
        Personas personaSeleccionada = Lista.get(posicion);

        // Construir el mensaje a compartir
        String mensaje = "País: " + personaSeleccionada.getPais() + "\n" +
                "Nombre: " + personaSeleccionada.getNombre() + "\n" +
                "Teléfono: " + personaSeleccionada.getTelefono() + "\n" +
                "Nota: " + personaSeleccionada.getNota();

        Intent intentCompartir = new Intent(Intent.ACTION_SEND);
        intentCompartir.setType("text/plain");
        intentCompartir.putExtra(Intent.EXTRA_SUBJECT, "Datos de Persona");
        intentCompartir.putExtra(Intent.EXTRA_TEXT, mensaje);

        startActivity(Intent.createChooser(intentCompartir, "Compartir datos"));
    }
}
