package com.painelmeteorologico.dao;

import com.painelmeteorologico.model.Medicao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de medições diárias, mapeado para a tabela real `history_daily`:
 * station_id, obs_date, temp_low, temp_high, temp_avg, precip_total, humidity_avg.
 */
public class MedicaoDAO {

    private static final String COLUNAS =
            "station_id, obs_date, temp_low, temp_high, temp_avg, precip_total, humidity_avg";

    /** Busca as medições diárias de uma estação dentro de um período. */
    public List<Medicao> buscarPorEstacaoEPeriodo(String stationId, LocalDate inicio, LocalDate fim) {
        String sql = "SELECT " + COLUNAS + """

                FROM history_daily
                WHERE station_id = ? AND obs_date BETWEEN ? AND ?
                ORDER BY obs_date
                """;
        return executarConsulta(sql, stationId, inicio, fim);
    }

    /** Busca os últimos N dias de uma estação a partir de uma data de referência (tendência/ondas). */
    public List<Medicao> buscarUltimosNDias(String stationId, LocalDate dataReferencia, int n) {
        LocalDate inicio = dataReferencia.minusDays(n - 1L);
        return buscarPorEstacaoEPeriodo(stationId, inicio, dataReferencia);
    }

    /**
     * Soma de precipitação acumulada nas últimas H horas (em múltiplos de 24h, já que o dado é diário)
     * até a data de referência. Usado pelo AlagamentoService (mm24, mm48, mm72).
     * Ex.: horas=48 -> soma precip_total dos últimos 2 dias (incluindo o dia selecionado).
     */
    public double somaPrecipitacaoUltimasHoras(String stationId, LocalDate dataReferencia, int horas) {
        int dias = (int) Math.ceil(horas / 24.0);
        LocalDate inicio = dataReferencia.minusDays(dias - 1L);

        String sql = """
                SELECT COALESCE(SUM(precip_total), 0) AS total
                FROM history_daily
                WHERE station_id = ? AND obs_date BETWEEN ? AND ?
                """;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(dataReferencia));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao somar precipitação", e);
        }
        return 0.0;
    }

    /**
     * Média climatológica de temp_avg para uma estação dentro de uma janela sazonal recorrente
     * (ex: 21/12 a 21/03 para verão), considerando todos os anos disponíveis no histórico.
     */
    public double mediaClimatologica(String stationId, int diaInicio, int mesInicio, int diaFim, int mesFim) {
        // Janela que cruza o ano (ex.: 21/12 a 21/03) precisa de OR; dentro do mesmo ano usa AND.
        String condicaoPeriodo = (mesInicio > mesFim)
                ? "((MONTH(obs_date) > ? OR (MONTH(obs_date) = ? AND DAY(obs_date) >= ?)) " +
                  "OR (MONTH(obs_date) < ? OR (MONTH(obs_date) = ? AND DAY(obs_date) <= ?)))"
                : "((MONTH(obs_date) > ? OR (MONTH(obs_date) = ? AND DAY(obs_date) >= ?)) " +
                  "AND (MONTH(obs_date) < ? OR (MONTH(obs_date) = ? AND DAY(obs_date) <= ?)))";

        String sql = "SELECT AVG(temp_avg) AS media FROM history_daily WHERE station_id = ? AND " + condicaoPeriodo;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            ps.setInt(2, mesInicio); ps.setInt(3, mesInicio); ps.setInt(4, diaInicio);
            ps.setInt(5, mesFim);    ps.setInt(6, mesFim);    ps.setInt(7, diaFim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("media");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular média climatológica", e);
        }
        return 0.0;
    }

    private List<Medicao> executarConsulta(String sql, String stationId, LocalDate inicio, LocalDate fim) {
        List<Medicao> medicoes = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // temp_high/low/avg, precip_total podem ser NULL em algumas leituras com falha de QC.
                    if (rs.getObject("temp_avg") == null || rs.getObject("temp_high") == null
                            || rs.getObject("temp_low") == null) {
                        continue; // pula dias sem dado de temperatura válido
                    }
                    Double umidade = rs.getObject("humidity_avg") != null ? rs.getDouble("humidity_avg") : null;
                    double precip = rs.getObject("precip_total") != null ? rs.getDouble("precip_total") : 0.0;

                    medicoes.add(new Medicao(
                            rs.getString("station_id"),
                            rs.getDate("obs_date").toLocalDate(),
                            rs.getDouble("temp_low"),
                            rs.getDouble("temp_high"),
                            rs.getDouble("temp_avg"),
                            precip,
                            umidade
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar medições", e);
        }
        return medicoes;
    }
}
