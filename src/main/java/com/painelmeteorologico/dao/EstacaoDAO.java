package com.painelmeteorologico.dao;

import com.painelmeteorologico.model.Estacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de estações, mapeado para a tabela real `stations` do banco `weather_pws`:
 * station_id (varchar), station_name, latitude, longitude, qc_status_label.
 */
public class EstacaoDAO {

    public List<Estacao> listarTodas() {
        String sql = """
                SELECT station_id, station_name, latitude, longitude, qc_status_label
                FROM stations
                ORDER BY station_name
                """;
        return executar(sql);
    }

    /** Filtra apenas estações com dado confiável (qc_status_label = 'Pass'). */
    public List<Estacao> listarComDadoConfiavel() {
        String sql = """
                SELECT station_id, station_name, latitude, longitude, qc_status_label
                FROM stations
                WHERE qc_status_label = 'Pass'
                ORDER BY station_name
                """;
        return executar(sql);
    }

    public Estacao buscarPorId(String stationId) {
        String sql = """
                SELECT station_id, station_name, latitude, longitude, qc_status_label
                FROM stations
                WHERE station_id = ?
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar estação " + stationId, e);
        }
        return null;
    }

    private List<Estacao> executar(String sql) {
        List<Estacao> estacoes = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                estacoes.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar estações", e);
        }
        return estacoes;
    }

    private Estacao mapear(ResultSet rs) throws SQLException {
        return new Estacao(
                rs.getString("station_id"),
                rs.getString("station_name"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("qc_status_label")
        );
    }
}
