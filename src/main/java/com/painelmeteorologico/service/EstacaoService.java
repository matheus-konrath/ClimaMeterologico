package com.painelmeteorologico.service;

import com.painelmeteorologico.model.Medicao;

import java.util.List;

public class EstacaoService {

    public record Resumo(double min, double max, double media) {}

    /** Calcula min/max/média de temperatura sobre o período filtrado. */
    public Resumo calcularResumoTemperatura(List<Medicao> medicoes) {
        if (medicoes.isEmpty()) return new Resumo(0, 0, 0);

        double min = medicoes.stream().mapToDouble(Medicao::getTemperaturaMin).min().orElse(0);
        double max = medicoes.stream().mapToDouble(Medicao::getTemperaturaMax).max().orElse(0);
        double media = medicoes.stream().mapToDouble(Medicao::getTemperaturaMedia).average().orElse(0);

        return new Resumo(min, max, media);
    }

    public double calcularPrecipitacaoAcumulada(List<Medicao> medicoes) {
        return medicoes.stream().mapToDouble(Medicao::getPrecipitacaoMm).sum();
    }

    /** Média de umidade do período (ignora dias sem leitura de umidade). Retorna 0 se não houver dado. */
    public double calcularUmidadeMedia(List<Medicao> medicoes) {
        return medicoes.stream()
                .map(Medicao::getUmidade)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }
}