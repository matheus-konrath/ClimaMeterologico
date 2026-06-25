-- Tabela opcional para persistir o histórico de alertas de alagamento já calculados.
-- Não existe no dump original (wu.zip). Rode este script no banco `weather_pws`
-- se quiser usar o AlertaDAO; caso contrário, o AlagamentoService já funciona
-- calculando tudo em memória, sem precisar desta tabela.

USE `weather_pws`;

CREATE TABLE IF NOT EXISTS `alerta` (
  `estacao_id` varchar(30) NOT NULL,
  `data` date NOT NULL,
  `mm24` decimal(8,2) NOT NULL,
  `mm48` decimal(8,2) NOT NULL,
  `mm72` decimal(8,2) NOT NULL,
  `nivel` varchar(20) NOT NULL,
  PRIMARY KEY (`estacao_id`, `data`),
  CONSTRAINT `fk_alerta_station` FOREIGN KEY (`estacao_id`) REFERENCES `stations` (`station_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
