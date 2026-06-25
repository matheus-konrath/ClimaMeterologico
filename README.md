# Painel Meteorológico — Região Sul

Esqueleto funcional do trabalho de Programação Avançada, organizado exatamente
nas 4 camadas do enunciado.

## Estrutura

```
src/main/java/com/painelmeteorologico/
  model/        Estacao, Medicao, Alerta
  dao/          ConnectionFactory, EstacaoDAO, MedicaoDAO, AlertaDAO     (Camada 3)
  service/      EstacaoService, ChuvaTendenciaService, AlagamentoService,
                OndaClimaticaService, HeatmapRenderer                    (Camada 2)
  ui/           MainFrame, FiltroPanel, MapaPanel,
                EstacaoWaypoint, EstacaoWaypointRenderer                 (Camadas 1 e 4)
```

## Banco de dados (já integrado: `weather_pws`)

O dump enviado (`wu.zip` → `wu.sql`) tem 4 tabelas:

- **`stations`**: `station_id` (varchar, ex: `IALEGR18`), `station_name`,
  `latitude`, `longitude`, `qc_status_label` (`Pass`/`Fail`/`No Data`).
  301 estações no dump (o enunciado menciona 179 — pode ser que o seu professor
  tenha um recorte menor, mas a estrutura é igual).
- **`history_daily`**: `station_id`, `obs_date`, `temp_high`/`temp_low`/`temp_avg`,
  `precip_total`, `humidity_avg`, etc. Dados de **2023-06-01 a 2026-05-31**.
- **`history_hourly`**: mesma ideia, granularidade horária (não usada ainda no
  código, mas pronta se você quiser refinar o alagamento por hora em vez de dia).
- **`fetch_log`**: log interno de coleta — não é usada pela aplicação.

Pontos importantes da integração:

1. **`station_id` é texto, não número.** Por isso `Estacao.id` e
   `Medicao.estacaoId` agora são `String` em todo o código (DAOs, Services, UI).
2. **Não existe coluna de "região"** no banco. Troquei o filtro de Região por
   um filtro real que existe: **Qualidade do dado** (`qc_status_label`:
   Pass/Fail/No Data). Se quiser mesmo um filtro geográfico por região, dá pra
   criar uma tabela de mapeamento `cidade -> região` ou classificar por
   latitude/longitude (ex: Fronteira Oeste, Serra, Metropolitana...) — me avise
   se quiser que eu implemente isso.
3. `db.properties` já está com `db.url=jdbc:mysql://localhost:3306/weather_pws...`.
   Restaure o `wu.sql` no MySQL (`mysql -u root -p < wu.sql`) e ajuste só usuário/senha.
4. O slider de período agora cobre `2025-01-01` a `2026-05-31` (a faixa com mais
   estações ativas) e começa na data mais recente.
5. Criei `src/main/resources/schema-extra.sql` com o `CREATE TABLE alerta`
   opcional, caso você queira persistir o histórico de alertas calculados
   (o `AlagamentoService` já funciona sem isso, calculando tudo em memória).

## Como rodar

1. Abra o projeto no IntelliJ/Eclipse como projeto Maven (precisa de internet
   para baixar `jxmapviewer2` e `mysql-connector-java` do Maven Central).
2. Ajuste `src/main/resources/db.properties` com a URL/usuário/senha do seu MySQL.
3. Execute `com.painelmeteorologico.ui.MainFrame`.

## O que já está implementado

- **Mapa interativo** (pan com arraste, zoom com a roda do mouse, teclas de
  seta) usando `OSMTileFactoryInfo` do próprio jxmapviewer2 — equivalente ao
  seu snippet original, só usando a factory pronta da biblioteca em vez de
  recalcular a URL de tiles manualmente. Se preferir manter exatamente o seu
  `TileFactoryInfo` customizado, é só trocar o conteúdo de
  `MapaPanel.configurarTileFactory()`.
- **Marcadores de estação** coloridos por temperatura (azul→vermelho), com
  popup ao clicar mostrando min/max/média do período.
- **Regra de alagamento** implementada exatamente conforme a tabela do
  enunciado (`AlagamentoService`), com soma de chuva 24h/48h/72h via
  `MedicaoDAO.somaPrecipitacaoUltimasHoras`.
- **Onda de calor / onda de frio** (`OndaClimaticaService`), usando médias
  climatológicas de verão (21/12–21/03) e inverno (21/06–21/09) calculadas com
  `GROUP BY` no banco.
- **Tendência de chuva** via regressão linear simples (`ChuvaTendenciaService`).
- **Heatmap por IDW** (`HeatmapRenderer`) — gera a grade interpolada; falta só
  plugar um `AbstractPainter` na UI para desenhar os retângulos (deixei a
  estrutura pronta em `MapaPanel.definirPaintersExtras`).
- **GUI**: split pane com filtros + tabela à esquerda, mapa à direita; slider
  de data, combos de estação/qualidade do dado/variável/modo de
  visualização/janela de tendência, campo de limiar de alerta.

## O que falta / pontos para você ajustar

1. **Filtro de região**: não existe no banco real; hoje o filtro foi trocado
   por "Qualidade do dado" (`qc_status_label`). Avise se quiser um filtro
   geográfico de fato (precisa de uma tabela/lógica de classificação por
   cidade ou lat/lon).
2. **Polígonos de zona alagadiça** e **isócronas** (isotermas/isoietas) —
   ainda não têm painter próprio; dá pra usar o mesmo padrão do
   `EstacaoWaypointRenderer`/`AbstractPainter` que já está no projeto.
3. **Clustering** (zoom < 8 agrupa estações) — não implementado ainda.
4. **Sparkline de 7 dias** no popup — hoje é só texto; pode usar um mini
   `JFreeChart` ou desenhar à mão num `JPanel` pequeno.
5. Algumas estações do dump ficam fora do RS (ex: Artigas/Bella Unión, no
   Uruguai) — são estações PWS próximas à fronteira; não é erro, mas vale
   mencionar na defesa do trabalho caso o professor pergunte.

Me diga qual dessas partes você quer priorizar primeiro e eu sigo de onde parei.
