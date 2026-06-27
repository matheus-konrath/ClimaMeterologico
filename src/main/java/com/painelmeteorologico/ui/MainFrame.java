package com.painelmeteorologico.ui;

import com.painelmeteorologico.dao.EstacaoDAO;
import com.painelmeteorologico.dao.MedicaoDAO;
import com.painelmeteorologico.model.Alerta;
import com.painelmeteorologico.model.Estacao;
import com.painelmeteorologico.model.Medicao;
import com.painelmeteorologico.service.AlagamentoService;
import com.painelmeteorologico.service.ChuvaTendenciaService;
import com.painelmeteorologico.service.EstacaoService;
import com.painelmeteorologico.service.HeatmapRenderer;
import com.painelmeteorologico.service.OndaClimaticaService;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {

    private final EstacaoDAO estacaoDAO = new EstacaoDAO();
    private final MedicaoDAO medicaoDAO = new MedicaoDAO();
    private final EstacaoService estacaoService = new EstacaoService();
    private final AlagamentoService alagamentoService = new AlagamentoService(medicaoDAO);
    private final ChuvaTendenciaService chuvaTendenciaService = new ChuvaTendenciaService();
    private final OndaClimaticaService ondaClimaticaService = new OndaClimaticaService(medicaoDAO);
    private final HeatmapRenderer heatmapRenderer = new HeatmapRenderer();

    private static final int JANELA_BUSCA_ONDA_DIAS = 15; // dias olhados pra trás ao checar onda de calor/frio

    private final FiltroPanel filtroPanel = new FiltroPanel();
    private final MapaPanel mapaPanel = new MapaPanel();
    private final DateSliderPanel dateSliderPanel =
            new DateSliderPanel(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 5, 31));

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"Estação", "Data", "Mín", "Máx", "Média", "Chuva (mm)", "Onda"}, 0);
    private final JTable tabelaDados = new JTable(modeloTabela);

    public MainFrame() {
        super("Painel Meteorológico — Região Sul");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel painelEsquerdo = new JPanel(new BorderLayout());
        painelEsquerdo.add(filtroPanel, BorderLayout.NORTH);
        painelEsquerdo.add(new JScrollPane(tabelaDados), BorderLayout.CENTER);

        JPanel painelMapaComSlider = new JPanel(new BorderLayout());
        painelMapaComSlider.add(mapaPanel, BorderLayout.CENTER);
        painelMapaComSlider.add(dateSliderPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelEsquerdo, painelMapaComSlider);
        splitPane.setDividerLocation(380);
        add(splitPane, BorderLayout.CENTER);

        carregarEstacoes();
        configurarListenersDeFiltro();

        atualizarTudo();
    }

    private void carregarEstacoes() {
        try {
            List<Estacao> estacoes = estacaoDAO.listarTodas();
            filtroPanel.preencherEstacoes(estacoes);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String causaRaiz = causaRaiz(e);
            JOptionPane.showMessageDialog(this,
                    "Não foi possível carregar estações do banco:\n" + causaRaiz
                            + "\n\nVerifique src/main/resources/db.properties e veja o console"
                            + " para o stack trace completo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String causaRaiz(Throwable t) {
        Throwable atual = t;
        while (atual.getCause() != null) {
            atual = atual.getCause();
        }
        return atual.getClass().getSimpleName() + ": " + atual.getMessage();
    }

    private void configurarListenersDeFiltro() {
        dateSliderPanel.addChangeListener(e -> atualizarTudo());
        filtroPanel.getComboEstacao().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboQualidade().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboVariavel().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboModoVisualizacao().addActionListener(e -> atualizarTudo());
        filtroPanel.getComboJanelaTendencia().addActionListener(e -> atualizarTudo());
        filtroPanel.getSpinnerLimiarMm().addChangeListener(e -> atualizarTudo());
    }

    /** Recarrega tabela + mapa (no modo selecionado) conforme os filtros atuais. */
    private void atualizarTudo() {
        LocalDate diaSelecionado = dateSliderPanel.getDataSelecionada();
        modeloTabela.setRowCount(0);

        List<Estacao> estacoes;
        try {
            estacoes = estacaoDAO.listarTodas();
        } catch (RuntimeException ex) {
            return; // sem conexão com o banco ainda
        }

        String qualidadeSelecionada = (String) filtroPanel.getComboQualidade().getSelectedItem();
        String estacaoSelecionada = (String) filtroPanel.getComboEstacao().getSelectedItem();
        String modoVisualizacao = (String) filtroPanel.getComboModoVisualizacao().getSelectedItem();
        String variavelSelecionada = (String) filtroPanel.getComboVariavel().getSelectedItem();
        int janelaTendencia = (Integer) filtroPanel.getComboJanelaTendencia().getSelectedItem();
        int limiarSpinner = (Integer) filtroPanel.getSpinnerLimiarMm().getValue();
        double fatorAjusteAlagamento = limiarSpinner / 50.0; // 50 = valor padrão = tabela original do enunciado

        Set<EstacaoWaypoint> waypoints = new HashSet<>();
        List<HeatmapRenderer.PontoValor> pontosHeatmap = new ArrayList<>();
        List<ZonaAlagamentoPainter.ZonaInfo> zonas = new ArrayList<>();

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
            double umidadeMedia = estacaoService.calcularUmidadeMedia(medicoes);

            // Valor usado para colorir o marcador / heatmap, conforme a "Variável climática" selecionada.
            double valorPrincipal = switch (variavelSelecionada) {
                case "Precipitação" -> chuva;
                case "Umidade" -> umidadeMedia;
                default -> resumo.media(); // "Temperatura"
            };

            // Onda de calor/frio só é calculada no modo Marcadores, pra não multiplicar consultas
            // ao banco nos outros modos (cada estação já gera várias queries extras pra isso).
            boolean ondaCalor = false, ondaFrio = false;
            String ondaTexto = "-";
            if ("Marcadores".equals(modoVisualizacao)) {
                ondaCalor = ondaClimaticaService.haOndaDeCalor(estacao.getId(), diaSelecionado, JANELA_BUSCA_ONDA_DIAS);
                ondaFrio = ondaClimaticaService.haOndaDeFrio(estacao.getId(), diaSelecionado, JANELA_BUSCA_ONDA_DIAS);
                ondaTexto = ondaCalor ? "Calor" : (ondaFrio ? "Frio" : "-");
            }

            modeloTabela.addRow(new Object[]{
                    estacao.getNome(), diaSelecionado, resumo.min(), resumo.max(), resumo.media(), chuva, ondaTexto
            });

            globalMin = Math.min(globalMin, valorPrincipal);
            globalMax = Math.max(globalMax, valorPrincipal);

            switch (modoVisualizacao) {
                case "Heatmap" -> pontosHeatmap.add(
                        new HeatmapRenderer.PontoValor(estacao.getLatitude(), estacao.getLongitude(), valorPrincipal));

                case "Zonas de alerta" -> {
                    Alerta alerta = alagamentoService.calcularAlerta(estacao.getId(), diaSelecionado, fatorAjusteAlagamento);
                    zonas.add(new ZonaAlagamentoPainter.ZonaInfo(
                            new GeoPosition(estacao.getLatitude(), estacao.getLongitude()),
                            estacao.getNome(),
                            nivelComoInt(alerta.getNivel())
                    ));
                }

                default -> { // "Marcadores"
                    List<Medicao> ultimosNDias = medicaoDAO.buscarUltimosNDias(estacao.getId(), diaSelecionado, janelaTendencia);
                    double slope = chuvaTendenciaService.calcularSlope(ultimosNDias);
                    ChuvaTendenciaService.Tendencia tendencia = chuvaTendenciaService.classificar(slope);
                    waypoints.add(new EstacaoWaypoint(estacao, valorPrincipal, tendencia, ondaCalor, ondaFrio));
                }
            }
        }

        aplicarModoNoMapa(modoVisualizacao, waypoints, globalMin, globalMax, pontosHeatmap, zonas);
    }

    private void aplicarModoNoMapa(String modo, Set<EstacaoWaypoint> waypoints, double min, double max,
                                   List<HeatmapRenderer.PontoValor> pontosHeatmap,
                                   List<ZonaAlagamentoPainter.ZonaInfo> zonas) {
        switch (modo) {
            case "Heatmap" -> {
                if (pontosHeatmap.isEmpty()) return;
                HeatmapRenderer.Celula[][] grade = heatmapRenderer.interpolar(pontosHeatmap, 40, 2.0);
                double valMin = pontosHeatmap.stream().mapToDouble(HeatmapRenderer.PontoValor::valor).min().orElse(0);
                double valMax = pontosHeatmap.stream().mapToDouble(HeatmapRenderer.PontoValor::valor).max().orElse(1);
                mapaPanel.exibirHeatmap(grade, valMin, valMax);
            }
            case "Zonas de alerta" -> mapaPanel.exibirZonasAlerta(zonas);
            default -> {
                if (!waypoints.isEmpty()) mapaPanel.exibirMarcadores(waypoints, min, max);
            }
        }
    }

    private int nivelComoInt(Alerta.Nivel nivel) {
        return switch (nivel) {
            case ATENCAO -> 1;
            case ALERTA -> 2;
            case EMERGENCIA -> 3;
            default -> 0;
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}