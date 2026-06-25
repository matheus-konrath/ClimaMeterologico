package com.painelmeteorologico.model;

import java.time.LocalDate;

/**
 * Representa a medição diária de uma estação.
 * Mapeada da tabela `history_daily`: temp_high/temp_low/temp_avg, precip_total, humidity_avg.
 */
public class Medicao {

    private String estacaoId; // station_id (varchar)
    private LocalDate data;
    private double temperaturaMin;
    private double temperaturaMax;
    private double temperaturaMedia;
    private double precipitacaoMm;
    private Double umidade; // humidity_avg — pode ser nula

    public Medicao() {}

    public Medicao(String estacaoId, LocalDate data, double temperaturaMin, double temperaturaMax,
                    double temperaturaMedia, double precipitacaoMm, Double umidade) {
        this.estacaoId = estacaoId;
        this.data = data;
        this.temperaturaMin = temperaturaMin;
        this.temperaturaMax = temperaturaMax;
        this.temperaturaMedia = temperaturaMedia;
        this.precipitacaoMm = precipitacaoMm;
        this.umidade = umidade;
    }

    public String getEstacaoId() { return estacaoId; }
    public void setEstacaoId(String estacaoId) { this.estacaoId = estacaoId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public double getTemperaturaMin() { return temperaturaMin; }
    public void setTemperaturaMin(double temperaturaMin) { this.temperaturaMin = temperaturaMin; }

    public double getTemperaturaMax() { return temperaturaMax; }
    public void setTemperaturaMax(double temperaturaMax) { this.temperaturaMax = temperaturaMax; }

    public double getTemperaturaMedia() { return temperaturaMedia; }
    public void setTemperaturaMedia(double temperaturaMedia) { this.temperaturaMedia = temperaturaMedia; }

    public double getPrecipitacaoMm() { return precipitacaoMm; }
    public void setPrecipitacaoMm(double precipitacaoMm) { this.precipitacaoMm = precipitacaoMm; }

    public Double getUmidade() { return umidade; }
    public void setUmidade(Double umidade) { this.umidade = umidade; }
}
