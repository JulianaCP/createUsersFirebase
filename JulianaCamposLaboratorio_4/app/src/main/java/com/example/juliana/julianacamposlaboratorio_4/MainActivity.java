package com.example.juliana.julianacamposlaboratorio_4;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextNombre;
    EditText editTextCorreo;
    EditText editTextPassword;

    Button buttonGuardar;
    Button buttonFoto;

    ImageView imageView;

    boolean foto = false;
    String nombre, correo, contraseña;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    Persona persona;
    String clave;
    Uri UriResult;
    boolean editar;

    private static final int IMAGE_CAPTURE = 12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        imageView = (ImageView)findViewById(R.id.imageView);

        editTextNombre = (EditText) findViewById(R.id.name);
        editTextCorreo = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);

        buttonFoto = (Button) findViewById(R.id.photo);
        buttonGuardar = (Button) findViewById(R.id.save);


        editTextCorreo.setText("jcp@hotmail.com");
        editTextNombre.setText("ffff");
        editTextPassword.setText("fsfsdf");

        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nombre = editTextNombre.getText().toString().trim();
                contraseña = editTextPassword.getText().toString().trim();
                correo = editTextCorreo.getText().toString().trim();

                if (!nombre.equals("") && !correo.equals("") && !contraseña.equals("") && foto == true) {
                    newUser(correo, contraseña);

                } else {
                    Toast.makeText(getApplicationContext(), "Faltan datos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE) ;
                startActivityForResult ( intent , IMAGE_CAPTURE ) ;
            }
        });


    }
    public void limpiarPantalla(){
        foto = false;
        imageView.setImageDrawable(null);
        editTextCorreo.setText("");
        editTextNombre.setText("");
        editTextPassword.setText("");
    }
    public void newUser(String email, String password) {
        System.out.println("new User");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            System.out.println("success newUser");
                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference("Persona");
                            persona = new Persona();
                            persona.setCorreo(correo);
                            persona.setContraseña(contraseña);
                            persona.setNombre(nombre);
                            myRef.push().setValue(persona);
                            editar = false;
                            obtenerClave();


                            //Toast.makeText(getApplicationContext(),"Exito",Toast.LENGTH_SHORT).show();

                        } else {
                            try{
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                switch (errorCode) {
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        System.out.println("error correo");
                                        editar = true;
                                        mensajeConfirmacion();
                                        break;
                                }
                            }catch (Exception e){
                                System.out.println("error en el else de error code");
                            }
                            System.out.println("task.getException():  " + task.getException());
                            //Toast.makeText(getApplicationContext(),"Fallo: " + task.getException(),Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    public void mensajeConfirmacion(){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("El usuario ya existe en el sistema. ¿Desea remplazar la información?")
                .setTitle("Confirmacion")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog, int id) {
                        obtenerClave();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
    }
    public void obtenerClave() {
        System.out.println("obtenerClave");
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Persona");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Persona user = postSnapshot.getValue(Persona.class);
                    System.out.println("correos en lista: " + user.getCorreo());
                    if(user.getCorreo().equals(correo)){
                        clave = postSnapshot.getKey();
                        System.out.println("obtener key: " + clave);

                        if(editar == true){
                            remplazarInfo();
                            limpiarPantalla();
                        }
                        else{
                            upLoad();
                            limpiarPantalla();
                        }

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Nada", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        myRef.addValueEventListener(postListener);

    }
    String pathFoto;
    public void obtenerPath() {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("FotosPath");

        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Fotos fotos = postSnapshot.getValue(Fotos.class);
                    if(fotos.getCorreo().equals(correo)){
                        pathFoto = fotos.getPath();
                    }
                }
                System.out.println("obtenerPath: uploead -> arriba");
                upLoad();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Nada", "loadPost:onCancelled", databaseError.toException());
                // ...
            }

        };

        myRef.addListenerForSingleValueEvent(postListener);

    }
    public void remplazarInfo(){

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Persona");
        persona = new Persona();
        persona.setCorreo(correo);
        persona.setContraseña(contraseña);
        persona.setNombre(nombre);
        myRef.child(clave).setValue(persona);

        obtenerPath();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_CAPTURE){
            if ( resultCode == RESULT_OK ) {
                UriResult = data.getData ();
                imageView.setImageURI(UriResult);
                imageView.setRotation(270);
                foto = true;

                //Toast . makeText (this , "guardado", Toast . LENGTH_LONG ) . show () ;
            } else if ( resultCode == RESULT_CANCELED ) {
                Toast . makeText (this , " cancelado",
                        Toast . LENGTH_LONG ) . show () ;
            } else {
                Toast . makeText (this , " fallo",
                        Toast . LENGTH_LONG ) . show () ;
            }

        }
    }


    public void upLoad(){
        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference storageReference =
                storageRef.getReferenceFromUrl("gs://auth-aa0a8.appspot.com");

        final Uri fileUri = UriResult;



        if(editar == true){
            final StorageReference ddd = storageReference.child("photos")
                    .child(correo).child(pathFoto);
            ddd.delete();

        }


        final StorageReference photoReference = storageReference.child("photos")
                .child(correo).child(fileUri.getLastPathSegment());



        photoReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference("FotosPath");

                        Fotos foto = new Fotos();
                        foto.setCorreo(correo);
                        foto.setPath(fileUri.getLastPathSegment());
                        myRef.push().setValue(foto);
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        Toast.makeText(MainActivity.this,"No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
