package com.example.juliana.julianacamposlaboratorio_4;

import java.io.Serializable;

/**
 * Created by Juliana on 16/05/2018.
 */

public class Persona{
    private String nombre;
    private String correo;
    private String contraseña;



    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
}
