package com.painelmeteorologico.service;

import com.painelmeteorologico.dao.MedicaoDAO;
import com.painelmeteorologico.model.Medicao;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Implementa as definições do enunciado:
 *
 * ONDA DE CALOR: temperatura máxima >= 5°C acima da média climatológica
 *                por 3 dias consecutivos ou mais.
 * ONDA DE FRIO:  temperatura mínima  <= 5°C abaixo da média climatológica
 *                por 3 dias consecutivos ou mais.
 *
 * Média climatológica:
 *  - verão:   21/12 a 21/03 (média histórica da estação nesse período, todos os anos)
 *  - inverno: 21/06 a 21/09
 */
public class OndaClimaticaService {

    private static final double LIMIAR = 5.0;
    private static final int DIAS_CONSECUTIVOS_MIN = 3;

    private final MedicaoDAO medicaoDAO;

    public OndaClimaticaService(MedicaoDAO medicaoDAO) {
        this.medicaoDAO = medicaoDAO;
    }

    /** Determina a estação do ano (verão/inverno) de uma data, para escolher a janela climatológica certa. */
    private boolean estaNoVerao(LocalDate data) {
        // 21/12 a 21/03 (cruza o ano)
        return isEntre(data, 12, 21, 3, 21);
    }

    private boolean estaNoInverno(LocalDate data) {
        // 21/06 a 21/09
        return isEntre(data, 6, 21, 9, 21);
    }

    private boolean isEntre(LocalDate data, int mesIni, int diaIni, int mesFim, int diaFim) {
        int mes = data.getMonthValue();
        int dia = data.getDayOfMonth();
        if (mesIni > mesFim) {
            // janela cruza o ano (ex.: verão)
            return (mes > mesIni || (mes == mesIni && dia >= diaIni))
                    || (mes < mesFim || (mes == mesFim && dia <= diaFim));
        }
        return (mes > mesIni || (mes == mesIni && dia >= diaIni))
                && (mes < mesFim || (mes == mesFim && dia <= diaFim));
    }

    private double obterMediaClimatologica(String estacaoId, LocalDate data) {
        if (estaNoVerao(data)) {
            return medicaoDAO.mediaClimatologica(estacaoId, 21, 12, 21, 3);
        }
        if (estaNoInverno(data)) {
            return medicaoDAO.mediaClimatologica(estacaoId, 21, 6, 21, 9);
        }
        // Fora das janelas de verão/inverno definidas no enunciado: usa o ano inteiro como aproximação.
        return medicaoDAO.mediaClimatologica(estacaoId, 1, Month.JANUARY.getValue(), 31, Month.DECEMBER.getValue());
    }

    /** Verifica se há onda de calor terminando na data de referência (olhando para trás). */
    public boolean haOndaDeCalor(String estacaoId, LocalDate dataReferencia, int janelaMaximaDeBusca) {
        double media = obterMediaClimatologica(estacaoId, dataReferencia);
        List<Medicao> medicoes = medicaoDAO.buscarUltimosNDias(estacaoId, dataReferencia, janelaMaximaDeBusca);
        return maiorSequenciaConsecutiva(medicoes, m -> m.getTemperaturaMax() >= media + LIMIAR)
                >= DIAS_CONSECUTIVOS_MIN;
    }

    /** Verifica se há onda de frio terminando na data de referência. */
    public boolean haOndaDeFrio(String estacaoId, LocalDate dataReferencia, int janelaMaximaDeBusca) {
        double media = obterMediaClimatologica(estacaoId, dataReferencia);
        List<Medicao> medicoes = medicaoDAO.buscarUltimosNDias(estacaoId, dataReferencia, janelaMaximaDeBusca);
        return maiorSequenciaConsecutiva(medicoes, m -> m.getTemperaturaMin() <= media - LIMIAR)
                >= DIAS_CONSECUTIVOS_MIN;
    }

    private int maiorSequenciaConsecutiva(List<Medicao> medicoes, java.util.function.Predicate<Medicao> condicao) {
        int maior = 0, atual = 0;
        for (Medicao m : medicoes) {
            if (condicao.test(m)) {
                atual++;
                maior = Math.max(maior, atual);
            } else {
                atual = 0;
            }
        }
        return maior;
    }
}
