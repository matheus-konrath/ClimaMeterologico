package com.painelmeteorologico.ui;

import com.painelmeteorologico.dao.EstacaoDAO;
import com.painelmeteorologico.dao.MedicaoDAO;
import com.painelmeteorologico.model.Estacao;
import com.painelmeteorologico.model.Medicao;
import com.painelmeteorologico.service.EstacaoService;
import com.painelmeteorologico.service.AlagamentoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {

    private final EstacaoDAO estacaoDAO = new EstacaoDAO();
    private final MedicaoDAO medicaoDAO = new MedicaoDAO();
    private final EstacaoService estacaoService = new EstacaoService();
    private final AlagamentoService alagamentoService = new AlagamentoService(medicaoDAO);

    private final FiltroPanel filtroPanel = new FiltroPanel();
    private final MapaPanel mapaPanel = new MapaPanel();
    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"Estação", "Data", "Mín", "Máx", "Média", "Chuva (mm)"}, 0);
    private final JTable tabelaDados = new JTable(modeloTabela);

    public MainFrame() {
        super("Painel Meteorológico — Região Sul");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel painelEsquerdo = new JPanel(new BorderLayout());
        painelEsquerdo.add(filtroPanel, BorderLayout.NORTH);
        painelEsquerdo.add(new JScrollPane(tabelaDados), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelEsquerdo, mapaPanel);
        splitPane.setDividerLocation(380);
        add(splitPane, BorderLayout.CENTER);

        carregarEstacoes();
        configurarListenersDeFiltro();

        // Carga inicial
        atualizarTudo();
    }

    private void carregarEstacoes() {
        try {
            List<Estacao> estacoes = estacaoDAO.listarTodas();
            filtroPanel.preencherEstacoes(estacoes);
        } catch (RuntimeException e) {
            // Banco ainda não configurado/conectado — não impede a UI de abrir.
            JOptionPane.showMessageDialog(this,
                    "Não foi possível carregar estações do banco:\n" + e.getMessage()
                            + "\n\nVerifique src/main/resources/db.properties.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void configurarListenersDeFiltro() {
        filtroPanel.getSliderData().addChangeListener(e -> atualizarTudo());
        filtroPanel.getComboEstacao().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboQualidade().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboVariavel().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboModoVisualizacao().addActionListener(e -> atualizarTudo());
    }

    /** Recarrega tabela + waypoints do mapa conforme os filtros atuais. */
    private void atualizarTudo() {
        LocalDate diaSelecionado = filtroPanel.getDataSelecionada();
        modeloTabela.setRowCount(0);

        List<Estacao> estacoes;
        try {
            estacoes = estacaoDAO.listarTodas();
        } catch (RuntimeException ex) {
            return; // sem conexão com o banco ainda
        }

        String qualidadeSelecionada = (String) filtroPanel.getComboQualidade().getSelectedItem();
        String estacaoSelecionada = (String) filtroPanel.getComboEstacao().getSelectedItem();

        Set<EstacaoWaypoint> waypoints = new HashSet<>();
        double globalMin = Double.MAX_VALUE, globalMax = -Double.MAX_VALUE;

        for (Estacao estacao : estacoes) {
            if (!"Todas".equals(qualidadeSelecionada)
                    && !qualidadeSelecionada.equalsIgnoreCase(estacao.getQcStatusLabel())) {
                continue;
            }
            if (estacaoSelecionada != null && !"Todas".equals(estacaoSelecionada)
                    && !estacaoSelecionada.equals(estacao.getNome())) {
                continue;
            }

            List<Medicao> medicoes;
            try {
                medicoes = medicaoDAO.buscarPorEstacaoEPeriodo(estacao.getId(), diaSelecionado.minusDays(6), diaSelecionado);
            } catch (RuntimeException ex) {
                continue;
            }
            if (medicoes.isEmpty()) continue;

            EstacaoService.Resumo resumo = estacaoService.calcularResumoTemperatura(medicoes);
            double chuva = estacaoService.calcularPrecipitacaoAcumulada(medicoes);

            modeloTabela.addRow(new Object[]{
                    estacao.getNome(), diaSelecionado, resumo.min(), resumo.max(), resumo.media(), chuva
            });

            waypoints.add(new EstacaoWaypoint(estacao, resumo.media()));
            globalMin = Math.min(globalMin, resumo.media());
            globalMax = Math.max(globalMax, resumo.media());
        }

        if (!waypoints.isEmpty()) {
            mapaPanel.atualizarWaypoints(waypoints, globalMin, globalMax);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
