package com.painelmeteorologico.service;

import com.painelmeteorologico.dao.MedicaoDAO;
import com.painelmeteorologico.model.Alerta;

import java.time.LocalDate;

/**
 * Implementa a regra de negócio "ALERTA DE ALAGAMENTO" exatamente como descrita no enunciado:
 *
 * NORMAL     -> nenhuma condição atingida
 * ATENCAO    -> mm24 >= 30  OU mm48 >= 50  OU mm72 >= 60
 * ALERTA     -> mm24 >= 50  OU mm48 >= 70  OU mm72 >= 90
 * EMERGENCIA -> mm24 >= 80  OU mm48 >= 100 OU mm72 >= 120
 *
 * O sistema retorna sempre o MAIOR nível atingido.
 * O dia de referência vem do jSlider da UI.
 */
public class AlagamentoService {

    private final MedicaoDAO medicaoDAO;

    public AlagamentoService(MedicaoDAO medicaoDAO) {
        this.medicaoDAO = medicaoDAO;
    }

    public Alerta calcularAlerta(String estacaoId, LocalDate diaSelecionadoNoSlider) {
        double mm24 = medicaoDAO.somaPrecipitacaoUltimasHoras(estacaoId, diaSelecionadoNoSlider, 24);
        double mm48 = medicaoDAO.somaPrecipitacaoUltimasHoras(estacaoId, diaSelecionadoNoSlider, 48);
        double mm72 = medicaoDAO.somaPrecipitacaoUltimasHoras(estacaoId, diaSelecionadoNoSlider, 72);

        Alerta.Nivel nivel = Alerta.Nivel.NORMAL;

        if (mm24 >= 80 || mm48 >= 100 || mm72 >= 120) {
            nivel = Alerta.Nivel.EMERGENCIA;
        } else if (mm24 >= 50 || mm48 >= 70 || mm72 >= 90) {
            nivel = Alerta.Nivel.ALERTA;
        } else if (mm24 >= 30 || mm48 >= 50 || mm72 >= 60) {
            nivel = Alerta.Nivel.ATENCAO;
        }

        return new Alerta(estacaoId, diaSelecionadoNoSlider, mm24, mm48, mm72, nivel);
    }

    /** Raio da zona alagadiça no mapa, proporcional ao nível (1/2/3), usado pelo painter de polígonos. */
    public int raioZona(Alerta.Nivel nivel) {
        return switch (nivel) {
            case ATENCAO -> 1;
            case ALERTA -> 2;
            case EMERGENCIA -> 3;
            default -> 0;
        };
    }
}
