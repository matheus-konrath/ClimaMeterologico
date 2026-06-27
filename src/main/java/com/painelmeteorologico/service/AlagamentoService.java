package com.painelmeteorologico.service;

import com.painelmeteorologico.dao.MedicaoDAO;
import com.painelmeteorologico.model.Alerta;

import java.time.LocalDate;

/**
 * Implementa a regra de negócio "ALERTA DE ALAGAMENTO" do enunciado, com um
 * fator de ajuste opcional ligado ao campo "Limiar de alerta (mm)" da UI:
 *
 * Tabela ORIGINAL do enunciado (fatorAjuste = 1.0, equivalente ao spinner em 50):
 * NORMAL     -> nenhuma condição atingida
 * ATENCAO    -> mm24 >= 30  OU mm48 >= 50  OU mm72 >= 60
 * ALERTA     -> mm24 >= 50  OU mm48 >= 70  OU mm72 >= 90
 * EMERGENCIA -> mm24 >= 80  OU mm48 >= 100 OU mm72 >= 120
 *
 * O fatorAjuste = (valor do spinner) / 50.0 escala TODOS os limiares
 * proporcionalmente — ex.: spinner em 25 deixa o sistema duas vezes mais
 * sensível (limiares pela metade); spinner em 100 deixa metade sensível
 * (limiares dobrados). Em 50 (valor padrão da UI) o comportamento é
 * idêntico à tabela original do enunciado.
 *
 * O sistema retorna sempre o MAIOR nível atingido.
 * O dia de referência vem do slider de data da UI.
 */
public class AlagamentoService {

    private final MedicaoDAO medicaoDAO;

    public AlagamentoService(MedicaoDAO medicaoDAO) {
        this.medicaoDAO = medicaoDAO;
    }

    /** Usa a tabela original do enunciado, sem nenhum ajuste (fatorAjuste = 1.0). */
    public Alerta calcularAlerta(String estacaoId, LocalDate diaSelecionadoNoSlider) {
        return calcularAlerta(estacaoId, diaSelecionadoNoSlider, 1.0);
    }

    /** Versão com fatorAjuste, ligada ao spinner "Limiar de alerta (mm)" da UI. */
    public Alerta calcularAlerta(String estacaoId, LocalDate diaSelecionadoNoSlider, double fatorAjuste) {
        double mm24 = medicaoDAO.somaPrecipitacaoUltimasHoras(estacaoId, diaSelecionadoNoSlider, 24);
        double mm48 = medicaoDAO.somaPrecipitacaoUltimasHoras(estacaoId, diaSelecionadoNoSlider, 48);
        double mm72 = medicaoDAO.somaPrecipitacaoUltimasHoras(estacaoId, diaSelecionadoNoSlider, 72);

        double aten24 = 30 * fatorAjuste, aten48 = 50 * fatorAjuste, aten72 = 60 * fatorAjuste;
        double aler24 = 50 * fatorAjuste, aler48 = 70 * fatorAjuste, aler72 = 90 * fatorAjuste;
        double emer24 = 80 * fatorAjuste, emer48 = 100 * fatorAjuste, emer72 = 120 * fatorAjuste;

        Alerta.Nivel nivel = Alerta.Nivel.NORMAL;

        if (mm24 >= emer24 || mm48 >= emer48 || mm72 >= emer72) {
            nivel = Alerta.Nivel.EMERGENCIA;
        } else if (mm24 >= aler24 || mm48 >= aler48 || mm72 >= aler72) {
            nivel = Alerta.Nivel.ALERTA;
        } else if (mm24 >= aten24 || mm48 >= aten48 || mm72 >= aten72) {
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