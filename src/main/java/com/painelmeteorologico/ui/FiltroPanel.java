package com.painelmeteorologico.ui;

import com.painelmeteorologico.model.Estacao;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Painel esquerdo: filtros da UI. O slider de data NÃO está mais aqui —
 * foi movido para sob o mapa (DateSliderPanel), igual ao mockup do enunciado.
 *
 * A "Estação" agora tem um campo de busca (digite para filtrar), porque o banco
 * tem ~300 estações e um JComboBox simples ficaria difícil de navegar.
 */
public class FiltroPanel extends JPanel {

    private final JTextField campoBuscaEstacao = new JTextField();
    private final JComboBox<String> comboEstacao = new JComboBox<>();
    private final JComboBox<String> comboQualidade = new JComboBox<>(new String[]{"Todas", "Pass", "Fail", "No Data"});
    private final JComboBox<String> comboVariavel = new JComboBox<>(new String[]{"Temperatura", "Precipitação", "Umidade"});
    private final JComboBox<String> comboModoVisualizacao = new JComboBox<>(new String[]{"Marcadores", "Heatmap", "Zonas de alerta"});
    private final JComboBox<Integer> comboJanelaTendencia = new JComboBox<>(new Integer[]{7, 15, 30});
    private final JSpinner spinnerLimiarMm = new JSpinner(new SpinnerNumberModel(50, 0, 500, 5));

    private final List<String> todasEstacoes = new ArrayList<>();

    public FiltroPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Filtros"));
        setPreferredSize(new Dimension(260, 0));

        adicionarBuscaEstacao();
        adicionarComLabel("Qualidade do dado (qc_status_label)", comboQualidade);
        adicionarComLabel("Variável climática", comboVariavel);
        adicionarComLabel("Modo de visualização", comboModoVisualizacao);
        adicionarComLabel("Janela de tendência (dias)", comboJanelaTendencia);
        adicionarComLabel("Limiar de alerta (mm)", spinnerLimiarMm);

        add(Box.createVerticalGlue());
    }

    private void adicionarBuscaEstacao() {
        JPanel painel = new JPanel(new BorderLayout(0, 4));
        painel.setBorder(BorderFactory.createTitledBorder("Estação / Cidade"));
        campoBuscaEstacao.setToolTipText("Digite para filtrar (ex.: nome da cidade)");

        comboEstacao.setMaximumRowCount(15);

        campoBuscaEstacao.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        painel.add(campoBuscaEstacao, BorderLayout.NORTH);
        painel.add(comboEstacao, BorderLayout.CENTER);
        painel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        add(painel);
        add(Box.createVerticalStrut(8));
    }

    /** Refiltra o combo conforme o texto digitado, preservando a seleção atual se ainda existir. */
    private void filtrar() {
        String termo = campoBuscaEstacao.getText().trim().toLowerCase(Locale.ROOT);
        String selecionadoAntes = (String) comboEstacao.getSelectedItem();

        comboEstacao.removeAllItems();
        comboEstacao.addItem("Todas");
        for (String nome : todasEstacoes) {
            if (termo.isEmpty() || nome.toLowerCase(Locale.ROOT).contains(termo)) {
                comboEstacao.addItem(nome);
            }
        }

        if (selecionadoAntes != null) {
            for (int i = 0; i < comboEstacao.getItemCount(); i++) {
                if (comboEstacao.getItemAt(i).equals(selecionadoAntes)) {
                    comboEstacao.setSelectedItem(selecionadoAntes);
                    break;
                }
            }
        }
    }

    public void preencherEstacoes(List<Estacao> estacoes) {
        todasEstacoes.clear();
        for (Estacao e : estacoes) todasEstacoes.add(e.getNome());
        filtrar();
    }

    private void adicionarComLabel(String texto, JComponent componente) {
        JPanel linha = new JPanel(new BorderLayout(4, 4));
        linha.add(new JLabel(texto), BorderLayout.NORTH);
        linha.add(componente, BorderLayout.CENTER);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        add(linha);
        add(Box.createVerticalStrut(8));
    }

    public JComboBox<String> getComboEstacao() { return comboEstacao; }
    public JComboBox<String> getComboQualidade() { return comboQualidade; }
    public JComboBox<String> getComboVariavel() { return comboVariavel; }
    public JComboBox<String> getComboModoVisualizacao() { return comboModoVisualizacao; }
    public JComboBox<Integer> getComboJanelaTendencia() { return comboJanelaTendencia; }
    public JSpinner getSpinnerLimiarMm() { return spinnerLimiarMm; }
}