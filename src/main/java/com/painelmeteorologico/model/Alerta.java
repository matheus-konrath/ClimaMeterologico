package com.painelmeteorologico.model;

import java.time.LocalDate;

public class Alerta {

    public enum Nivel { NORMAL, ATENCAO, ALERTA, EMERGENCIA }

    private String estacaoId;
    private LocalDate data;
    private double mm24;
    private double mm48;
    private double mm72;
    private Nivel nivel;

    public Alerta() {}

    public Alerta(String estacaoId, LocalDate data, double mm24, double mm48, double mm72, Nivel nivel) {
        this.estacaoId = estacaoId;
        this.data = data;
        this.mm24 = mm24;
        this.mm48 = mm48;
        this.mm72 = mm72;
        this.nivel = nivel;
    }

    public String getEstacaoId() { return estacaoId; }
    public void setEstacaoId(String estacaoId) { this.estacaoId = estacaoId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public double getMm24() { return mm24; }
    public void setMm24(double mm24) { this.mm24 = mm24; }

    public double getMm48() { return mm48; }
    public void setMm48(double mm48) { this.mm48 = mm48; }

    public double getMm72() { return mm72; }
    public void setMm72(double mm72) { this.mm72 = mm72; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }
}
