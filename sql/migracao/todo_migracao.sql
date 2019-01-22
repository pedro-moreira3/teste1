--SUPORTE
--------


-- Copiar o banco ODONTO DE PROD para outra maq
-- Copiar o banco LS DE PROD para outra maq

--------------------------


ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN "VALOR_REPASSE" DECIMAL(11,2);

ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN DATA_REPASSE TIMESTAMP;

ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO ADD COLUMN STATUS_PAGAMENTO CHAR(1) DEFAULT 'P';

CALL SYSPROC.ADMIN_CMD('REORG TABLE PLANO_TRATAMENTO_PROCEDIMENTO');

ALTER TABLE ITEM ADD COLUMN "APLICACAO" CHAR(1 OCTETS) WITH DEFAULT 'I';

CALL SYSPROC.ADMIN_CMD('REORG TABLE ITEM');	

ALTER TABLE PROFISSIONAL ADD COLUMN "NOME_IMAGEM" VARCHAR(100 OCTETS);

CALL SYSPROC.ADMIN_CMD('REORG TABLE PROFISSIONAL');

ALTER TABLE MOTIVO ADD COLUMN "SIGLA" VARCHAR(2 OCTETS);

CALL SYSPROC.ADMIN_CMD('REORG TABLE MOTIVO');

ALTER TABLE AGENDAMENTO  ADD COLUMN "ID_PLANO_TRATAMENTO" BIGINT;

CALL SYSPROC.ADMIN_CMD('REORG TABLE AGENDAMENTO');

ALTER TABLE CONFERENCIA_MATERIAL ADD COLUMN "MOTIVO" CHAR(20 OCTETS);

ALTER TABLE CONFERENCIA_MATERIAL ADD COLUMN "DATA_CADASTRO" TIMESTAMP;

CALL SYSPROC.ADMIN_CMD('REORG TABLE CONFERENCIA_MATERIAL');

ALTER TABLE MATERIAL ADD COLUMN "VALOR_UNIDADE_INFORMADO" DECIMAL(10,5) ;

ALTER TABLE MATERIAL ADD COLUMN ID_FORNECEDOR BIGINT;

ALTER TABLE MATERIAL ADD CONSTRAINT MATERIAL_FORNECEDOR FOREIGN KEY (ID_FORNECEDOR) REFERENCES FORNECEDOR (ID);

CALL SYSPROC.ADMIN_CMD('REORG TABLE MATERIAL');

ALTER TABLE LANCAMENTO DROP COLUMN TRIBUTO;

ALTER TABLE LANCAMENTO ADD COLUMN "VALOR_REPASSADO" DECIMAL(11,2) ;

ALTER TABLE LANCAMENTO ADD COLUMN "TRIBUTO" DECIMAL(11,2) ;

CALL SYSPROC.ADMIN_CMD('REORG TABLE LANCAMENTO');

ALTER TABLE RETORNO ALTER COLUMN DATA_RETORNO SET DATA TYPE DATE;

ALTER TABLE RETORNO ADD COLUMN ID_PACIENTE BIGINT;

ALTER TABLE RETORNO ADD CONSTRAINT RETORNO_PACIENTE FOREIGN KEY (ID_PACIENTE) REFERENCES PACIENTE (ID);

UPDATE RETORNO R SET ID_PACIENTE = (SELECT ID_PACIENTE FROM PLANO_TRATAMENTO PT WHERE PT.ID = R.ID_PLANO_TRATAMENTO);

CALL SYSPROC.ADMIN_CMD('REORG TABLE RETORNO');

ALTER TABLE PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ADD COLUMN "DATA_REGISTRO" DATE;

CALL SYSPROC.ADMIN_CMD('REORG TABLE PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO');

CREATE TABLE "PROCEDIMENTO_KIT"
(
   ID BIGINT  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY NOT NULL,
   ID_PROCEDIMENTO bigint NOT NULL,
   ID_KIT bigint NOT NULL,
   ID_EMPRESA bigint,
   EXCLUIDO char(1),
   DATA_EXCLUSAO timestamp,
   EXCLUIDO_POR bigint,
   QUANTIDADE int
)
;
ALTER TABLE "PROCEDIMENTO_KIT"
ADD CONSTRAINT SQL161107083853530
FOREIGN KEY (ID_PROCEDIMENTO)
REFERENCES "PROCEDIMENTO"(ID)
;
ALTER TABLE "PROCEDIMENTO_KIT"
ADD CONSTRAINT SQL161107083853610
FOREIGN KEY (ID_KIT)
REFERENCES "KIT"(ID)
;
CREATE UNIQUE INDEX SQL161107083853130 ON "PROCEDIMENTO_KIT"(ID)
;


CREATE TABLE PAGAMENTO ( 
ID BIGINT  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY NOT NULL, 
ID_EMPRESA	BIGINT,
VALOR	DECIMAL (10,2),
DATA TIMESTAMP ) ;

CREATE TABLE "PLANO"  (
		  "ID" BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (  
		    START WITH +1  
		    INCREMENT BY +1  
		    MINVALUE +1  
		    MAXVALUE +9223372036854775807  
		    NO CYCLE  
		    CACHE 20  
		    NO ORDER ) , 
		  "NOME" CHAR(200 OCTETS) , 
		  "NOME_PAYPAL" CHAR(200 OCTETS) , 
		  "CONSULTAS" INTEGER ) ;


CREATE UNIQUE INDEX "PK_PLANO" ON "PLANO" 
		("ID" ASC)
		

ALTER TABLE "PLANO" 
	ADD CONSTRAINT "PK_PLANO" PRIMARY KEY
		("ID");

		
		CREATE TABLE "HELP"  (
                  "ID" BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (
                    START WITH +1
                    INCREMENT BY +1
                    MINVALUE +1
                    MAXVALUE +9223372036854775807
                    NO CYCLE
                    CACHE 20
                    NO ORDER ) ,
                  "TELA" CHAR(100 OCTETS) ,
                  "CONTEUDO" CLOB(307200 OCTETS) LOGGED NOT COMPACT ,
                  "LABEL" VARCHAR(80 OCTETS) )
                 IN "TBSP_DATA_4K"
                 ORGANIZE BY ROW;
  ALTER TABLE ODONTO.HELP
  ADD CONSTRAINT PK_HELP PRIMARY KEY  (ID);
  
  
  
CREATE TABLE "HELP_IMG"
(
   ID bigint PRIMARY KEY NOT NULL,
   ID_HELP bigint,
   NOME_ARQUIVO varchar(100),
   NOME_ORIGINAL varchar(100)
)
;
CREATE UNIQUE INDEX PK_HELP_IMG_ID ON "HELP_IMG"(ID)
;

update motivo set DESCRICAO = 'Pagamento Paciente', sigla = 'PX' where id = 2;
update motivo set DESCRICAO = 'Compra Materiais', sigla = 'CM' where id = 4;
update motivo set DESCRICAO = 'Despesa Tarifas', sigla = 'PT' where id = 3;
INSERT INTO MOTIVO (DATA_EXCLUSAO,DESCRICAO,EXCLUIDO,EXCLUIDO_POR,ID_EMPRESA,SIGLA,TIPO,ID_CONTA) VALUES (null,'Devolução Paciente','N',null,0,'DP','Pagar',2);
INSERT INTO MOTIVO (DATA_EXCLUSAO,DESCRICAO,EXCLUIDO,EXCLUIDO_POR,ID_EMPRESA,SIGLA,TIPO,ID_CONTA) VALUES (null,'Pagamento Serviços','N',null,0,'PS','Pagar',2);
INSERT INTO MOTIVO (DATA_EXCLUSAO,DESCRICAO,EXCLUIDO,EXCLUIDO_POR,ID_EMPRESA,SIGLA,TIPO,ID_CONTA) VALUES (null,'Despesas Diversas','N',null,0,'DD','Pagar',2);
INSERT INTO MOTIVO (DATA_EXCLUSAO,DESCRICAO,EXCLUIDO,EXCLUIDO_POR,ID_EMPRESA,SIGLA,TIPO,ID_CONTA) VALUES (null,'Pagamento Profissional','N',null,0,'PP','Pagar',2);
INSERT INTO MOTIVO (DATA_EXCLUSAO,DESCRICAO,EXCLUIDO,EXCLUIDO_POR,ID_EMPRESA,SIGLA,TIPO,ID_CONTA) VALUES (null,'Pagamento Laboratório','N',null,0,'PL','Pagar',2);
INSERT INTO MOTIVO (DATA_EXCLUSAO,DESCRICAO,EXCLUIDO,EXCLUIDO_POR,ID_EMPRESA,SIGLA,TIPO,ID_CONTA) VALUES (null,'Saldo Inicial','N',null,0,'SI','Inicial',2);

update motivo set descricao = 'Recebimento de Paciente' where descricao = 'Pagamento Paciente';
update motivo set descricao = 'Compra de Material' where descricao = 'Compra Materiais';
update motivo set descricao = 'Despesas com Tarifa' where descricao = 'Despesa Tarifas';
update motivo set descricao = 'Devolução de Paciente' where descricao = 'Devolução Paciente';
update motivo set descricao = 'Pagamento de Laboratório' where descricao = 'Pagamento Laboratório';
update motivo set descricao = 'Recebimento de Profissional' where descricao = 'Pagamento Profissional';
update motivo set descricao = 'Pagamento de Serviços' where descricao = 'Pagamento Serviços';

update item set tipo = 'C';
update item set tipo = 'I' where id = 48;
update item set tipo = 'I' where ID_ITEM_PAI = 48;
update item set tipo = 'C';
update item set tipo = 'I' where id = 48;
update item set tipo = 'I' where ID_ITEM_PAI = 48;
update item set tipo = 'I' WHERE ID_ITEM_PAI in
(1489,1492,1506,1522,1551,1556,1603,1604,1606,1610,1612,1624,1625,1626,1628,1811,1817,1875,2067,2068,
2158,2172,2218,2631,2743,2856,2874,3015,3042,3043,3086,3087,3088,3089,3090,3091,3101);
update item set tipo = 'I' WHERE ID_ITEM_PAI in
(3227,136,141,2112,1438,1456,1457,1477,1478,1479,1480,1481,1482,1483,1485,1486,1487,1488,1490,1491,1493,
1494,1495,1496,1497,1500,1505,1507,1508,1509,1510,1511,1512,1514,1515,1516,1518,1520,1521,1523,1524,
1528,1531,1532,1533,1534,1535,1536,1537,1538,1542,1579,1580,1581,1582,1583,1587,1588,1589,1590,1591,
1592,1593,1594,1595,1596,1597,1598,1599,1600,1601,1602,1605,1607,1608,1609,1611,1613,1614,1615,1616,
1617,1618,1619,1620,1621,1622,1623,1627,1630,1809,1810,1815,1816,1834,1837,1853,1856,1858,1871,1876,
2044,1923,1932,1948,1954,1955,1956,1957,1958,2063,2064,2065,2066,2069,2070,2071,2072,2077,2078,2083,
2084,2085,2086,2087,2088,2089,2090,2091,2092,2094,2130,2136,2142,2143,2145,2149,2150,2151,2152,2155,
2156,2157,2162,2168,2169,2170,2174,2175,2176,2177,2207,2210,2212,2213,2214,2215,2216,2217,2219,2220,
2221,2222,2227,2228,2231,2235,2240,2241,2242,2259,2261,2262,2263,2264,2284,2285,2286,2287,2291,2292,
2294,2295,2296,2374,2375,2376,2378,2598,2599,2602,2603,2604,2605,2606,2607,2626,2627,2630,2632,2638,
2639,2642,2654,2662,2664,2665,2668,2669,2671,2672,2741,2742,2745,2747,2748,2756,2827,2828,2829,2830,
2831,2832,2833,2839,2840,2841,2842,2843,2844,2846,2849,2850,2851,2852,2853,2854,2873,2880,2896,2910,
2927,2936,2944,2953,2954,2955,2988,2991,2999,3002,3003,3004,3005,3006,3009,3026,3030,3039,3041,3048,
3054,3063,3069,3074,3103,3105,3106,3119,3124,3129,3130,3131,3132,3147,3156,3164,3173,3178,3196,3242,
3244,3260,3261,3262,3264,3266);
update item set tipo = 'I' WHERE ID_ITEM_PAI in
(3229,3230,3231,3232,3234,3235,617,619,2113,2117,2118,2121,2122,2125,2126,2127,2129,2131,2132,2133,2134,
2135,2144,2148,2154,2163,2164,2165,2166,2167,2178,2180,2181,2182,2183,2184,2185,2186,2187,2188,2189,
2190,2192,2193,2194,2195,2196,2197,2600,2601,2609,2610,2611,2612,2613,2614,2615,2616,2617,2618,2619,
2622,2623,2624,2625,2628,2629,2637,2678,2679,2683,2761,2762,2767,2768,2769,2770,2771,2773,2774,2775,
2776,2777,2778,2779,2780,2781,2782,2783,2784,2785,2786,2787,2788,2789,2790,2791,2792,2793,2794,2795,
2796,2797,2798,2799,2800,2801,2802,2803,2804,2805,2806,2807,2808,2809,2810,2811,2812,2813,2814,2815,
2816,2817,2818,2819,2820,2821,2822,2823,2824,2825,2826,2857,2858,2884,2885,2890,2891,2892,2893,2898,
2899,2901,2902,2903,2935,3019,3020,3021,3022,3023,3024,3025,3028,3031,3051,3052,3053,3121,3185,3250,
3257,3258);
update item set tipo = 'I' WHERE ID_ITEM_PAI in
(2114,2115,2116,2119,2120,2621,2674,2680,2681,2766,2772,2930,2931,3122,3123);
update item set tipo = 'I' WHERE ID_ITEM_PAI in
(3228,3233,1924,616,618,1484,1498,1539,1540,1541,1543,1544,1545,1546,1547,1548,1549,1550,1552,1553,1554,
1555,1557,1558,1559,1560,1561,1562,1563,1564,1565,1566,1567,1568,1569,1570,1571,1572,1573,1574,1575,
1576,1577,1578,1631,1632,1633,1634,1635,1636,1637,1638,1639,1640,1641,1642,1643,1644,1645,1646,1647,
1648,1649,1650,1651,1652,1653,1654,1655,1656,1657,1658,1659,1660,1661,1662,1663,1664,1665,1666,1667,
1668,1669,1670,1671,1672,1673,1674,1675,1676,1677,1678,1679,1680,1681,1682,1683,1684,1685,1686,1687,
1688,1689,1690,1691,1692,1693,1694,1695,1696,1697,1698,1699,1700,1701,1702,1703,1704,1705,1706,1707,
1708,1709,1710,1711,1712,1713,1714,1715,1716,1717,1718,1719,1720,1721,1722,1723,1724,1725,1726,1727,
1728,1729,1730,1731,1732,1733,1734,1735,1736,1737,1738,1739,1740,1741,1742,1743,1744,1745,1746,1747,
1748,1749,1750,1751,1752,1753,1754,1755,1756,1757,1758,1759,1760,1761,1762,1763,1764,1765,1766,1767,
1768,1769,1770,1771,1772,1773,1774,1775,1776,1777,1778,1779,1780,1781,1782,1783,1784,1785,1786,1787,
1788,1789,1790,1791,1792,1793,1794,1795,1796,1797,1798,1799,1800,1801,1802,1925,1926,1927,1928,1929,
1930,1931,1965,1966,2123,2124,2128,2146,2147,2153,2171,2173,2179,2191,2208,2209,2377,2620,2633,2634,
2635,2636,2640,2641,2643,2644,2645,2646,2651,2652,2653,2682,2744,2746,2757,2758,2759,2760,2763,2765,
2834,2835,2836,2837,2838,2868,2882,2883,2889,2900,2926,2928,2929,2937,2938,2939,2940,2942,2943,2948,
2949,2969,2975,3007,3008,3027,3029,3036,3037,3044,3045,3046,3066,3067,3068,3070,3071,3072,3073,3075,
3076,3077,3078,3079,3080,3081,3082,3133,3134,3135,3136,3137,3138,3139,3140,3141,3142,3143,3144,3145,
3146,3148,3149,3150,3151,3152,3153,3154,3157,3158,3159,3160,3161,3162,3165,3166,3168,3179,3180,3181,
3206,3213,3214,3215,3216,3217,3218,3220,3246,3247);


select * from item i1 where i1.EXCLUIDO = 'N' and exists (select 1 from item i2 where i1.ID_ITEM_PAI = i2.id and i1.tipo <> i2.tipo);


update material set TAMANHO_UNIDADE = QUANTIDADE_UNIDADE where TAMANHO_UNIDADE != QUANTIDADE_UNIDADE;

UPDATE DOMINIO SET OBJETO = TRIM(OBJETO), TIPO = TRIM(TIPO), NOME = TRIM(NOME) , VALOR = TRIM(VALOR);

delete from lancamento_contabil;

----------------------
--- SEGURANCA

ALTER TABLE SEG_USUARIO ADD COLUMN USU_CHA_TUTORIAL CHAR(1) DEFAULT 'S';

ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_DTM_CRIACAO date;
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_TRIAL char(1) DEFAULT 'N';
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_STR_EMAIL_PAG varchar(100);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_DTM_EXPIRACAO date;
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_DTM_PAGAMENTO date;
ALTER TABLE SEG_EMPRESA  ADD COLUMN ID_PLANO bigint;
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_STR_ASSINATURAID varchar(20);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_TIPO char(1);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_CPF char(15);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_RG char(15);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_CRO char(30);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_STR_COMPLEMENTO varchar(100);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_DTM_ACEITE timestamp;
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_IP char(15);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_NUMENDERECO char(15);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_STR_ORGAO_EXPEDIDOR varchar(100);
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_CHA_TROCA_PLANO char(1) DEFAULT 'N';
ALTER TABLE SEG_EMPRESA  ADD COLUMN EMP_STR_PAYERID varchar(20);
   
CALL SYSPROC.ADMIN_CMD('REORG TABLE SEG_EMPRESA');

delete from SEG_PEROBJETO;
delete from SEG_LOGACESSO;
delete from SEG_RESTRICAO;
delete from SEG_OBJETO;

-- FIM PARTE 1

-- MIGRAR DO jdbc:db2://GCPRDB16.lume.global:50000/INTELI os dados da tabela SEG_OBJETO para jdbc:db2://GCPRDB07.lume.global:50003/LS

-- MIGRAR DO jdbc:db2://GCPRDB16.lume.global:50000/INTELI os dados das tabelas HELP_IMG e HELP para jdbc:db2://gcprdb02.lume.global:50000/ODONTO

-- COMECO parte 2

update SEG_OBJETO set SIS_INT_COD = 123;

INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (246,16);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (246,20);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (246,19);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (246,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,18);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,17);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,15);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,29);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,30);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,89);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,90);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,91);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (247,95);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,17);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,18);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,21);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,59);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,47);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,75);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (248,82);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,86);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,33);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,59);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,87);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,83);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,97);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,96);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,44);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,29);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,84);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,30);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,18);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,67);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,46);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,45);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,85);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,17);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (249,65);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (252,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (252,17);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (252,15);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (252,64);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (252,70);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,49);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,44);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,23);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,52);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,54);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,31);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,88);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,50);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,29);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,26);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,58);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,56);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,57);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,47);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,30);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,97);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,27);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,18);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,96);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,48);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,17);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,25);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,78);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,87);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,15);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,33);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,53);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,76);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,60);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,61);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,75);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,59);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,84);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,83);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,89);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,91);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,90);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,92);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,63);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,64);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,46);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,45);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,85);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,86);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,79);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,68);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,66);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,67);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,65);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,69);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,21);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,70);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,81);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,82);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (253,95);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,18);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,96);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,27);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,58);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,83);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,30);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,33);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,29);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,25);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,84);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,59);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,49);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,15);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,53);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,61);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,48);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,52);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,90);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,60);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,50);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,26);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,57);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,17);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,88);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,63);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,87);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,44);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,89);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,76);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,97);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,54);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,47);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,75);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,91);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,92);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,31);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,23);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,56);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,64);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,78);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,45);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,85);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,86);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,79);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,65);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,66);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,67);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,68);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,69);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,21);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,70);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,81);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,82);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,95);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,46);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,72);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (266,122);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (286,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (286,102);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (286,141);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (286,98);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (286,99);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (250,14);
INSERT INTO SEG_PEROBJETO (PER_INT_COD,OBJ_INT_COD) VALUES (250,100);
   

update seg_perusuario set per_int_cod = (select per_int_cod from SEG_PERFIL where sis_int_cod = 123 and per_str_des = 'Administrador Clínica') where usu_int_cod = 1300; 

update seg_perusuario set per_int_cod = (select per_int_cod from SEG_PERFIL where sis_int_cod = 123 and per_str_des = 'Administradores') where usu_int_cod = 1036;

update profissional set perfil = 'Administrador Clínica' where id = 658;

INSERT INTO profissional 
(id_dados_basicos,senha_certificado,id_usuario,id_especialidade,id_empresa,percentual_remuneracao,perfil           ,tempo_consulta,registro_conselho,excluido,data_exclusao,excluido_por,certificado               ,desconto,alterado_por,data_ultima_alteracao            ,status,justificativa,id_profissional_filial,remuneracao_manutencao,nome_imagem) VALUES
(2639             ,''               ,1036      ,null            ,41        ,0                     ,'Administradores',0             ,1111             ,'N'     ,null         ,null        ,'[Ljava.lang.Byte;@3b3489',null    ,457         ,{ts '2015-07-17 10:21:13.904000'},'A'   ,null         ,null                  ,0                     ,null       )
;

update seg_usuario set usu_str_senha = '8e4b0c771fd4576f524fe153e5d114f9' where usu_int_cod = 1036;

insert into seg_perobjeto (per_int_cod,obj_int_cod) values (286, (select obj_int_cod from seg_objeto where obj_str_caminho='perfilObjeto.jsf'));


update SEG_OBJETO set OBJ_STR_DES = 'Receituário' where OBJ_STR_CAMINHO = 'receituario.jsf'

alter table SEG_USUARIO drop column emp_int_cod;

CALL SYSPROC.ADMIN_CMD('REORG TABLE SEG_USUARIO');



-- ANTES DE EXECUTAR ME FALAR -- APAGAR TODO O ESTOQUE

update material set excluido = 'S' , data_exclusao = now() where excluido = 'N' and id_empresa = 41;

--------------------------------------
-- POS MIGRACAO DE DADOS--------------
--------------------------------------

-- Rodar DocumentoBO.main() apontando para producao para trocar ##hashtag por #hashtag
-- RECONFIGURAR OS DOCUMENTOS COM AS HASHTAGS certas


-- Migrar para POSTGREsql

-- Cadastrar dados para as empresas novas