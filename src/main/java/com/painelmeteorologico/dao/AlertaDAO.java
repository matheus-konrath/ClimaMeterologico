package com.painelmeteorologico.dao;

import com.painelmeteorologico.model.Alerta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO de alertas calculados. A tabela `alerta` NÃO existe no dump original
 * (wu.zip só tem stations/history_daily/history_hourly/fetch_log) — é uma tabela
 * extra para você persistir o histórico de alertas já calculados, se quiser.
 * O DDL está em schema-extra.sql. Se preferir, ignore este DAO e calcule
 * o alerta sob demanda direto do AlagamentoService (já funciona sem esta tabela).
 */
public class AlertaDAO {

    public void salvar(Alerta alerta) {
        String sql = """
                INSERT INTO alerta (estacao_id, data, mm24, mm48, mm72, nivel)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE mm24 = ?, mm48 = ?, mm72 = ?, nivel = ?
                """;

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, alerta.getEstacaoId());
            ps.setDate(2, Date.valueOf(alerta.getData()));
            ps.setDouble(3, alerta.getMm24());
            ps.setDouble(4, alerta.getMm48());
            ps.setDouble(5, alerta.getMm72());
            ps.setString(6, alerta.getNivel().name());
            ps.setDouble(7, alerta.getMm24());
            ps.setDouble(8, alerta.getMm48());
            ps.setDouble(9, alerta.getMm72());
            ps.setString(10, alerta.getNivel().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar alerta", e);
        }
    }
}
