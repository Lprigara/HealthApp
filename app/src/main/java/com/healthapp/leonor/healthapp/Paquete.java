package com.healthapp.leonor.healthapp;

public class Paquete {
    private String pulso;
    private String oxygeno;
    private String temperatura;
    private String conductancia;
    private String resistencia;

    public Paquete(String[] arrayVariables){
        this.pulso = arrayVariables[1];
        this.oxygeno = arrayVariables[2];
        this.temperatura = arrayVariables[3];
        this.conductancia = arrayVariables[4];
        this.resistencia = arrayVariables[5];
    }

    public String getPulso() {
        return pulso;
    }

    public String getOxygeno() {
        return oxygeno;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public String getConductancia() {
        return conductancia;
    }

    public String getResistencia() {
        return resistencia;
    }

    public void setPulso(String pulso) {
        this.pulso = pulso;
    }

    public void setConductancia(String conductancia) {
        this.conductancia = conductancia;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public void setOxygeno(String oxygeno) {
        this.oxygeno = oxygeno;
    }
}
