package com.painelmeteorologico.model;

/**
 * Representa uma estação meteorológica.
 * Mapeada a partir da tabela real `stations` do banco `weather_pws`:
 * station_id (varchar, ex: "IALEGR18"), station_name, latitude, longitude, qc_status_label.
 */
public class Estacao {

    private String id;             // station_id (varchar) — NÃO é numérico
    private String nome;           // station_name (também usado como "cidade")
    private double latitude;
    private double longitude;
    private String qcStatusLabel;  // "Pass" / "Fail" / "No Data" — qualidade do dado da estação

    public Estacao() {}

    public Estacao(String id, String nome, double latitude, double longitude, String qcStatusLabel) {
        this.id = id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.qcStatusLabel = qcStatusLabel;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getQcStatusLabel() { return qcStatusLabel; }
    public void setQcStatusLabel(String qcStatusLabel) { this.qcStatusLabel = qcStatusLabel; }

    @Override
    public String toString() {
        return nome + " (" + id + ")";
    }
}
