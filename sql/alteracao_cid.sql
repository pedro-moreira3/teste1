UPDATE CID SET DESCRICAO = 'Atrito dentário excessivo' WHERE ID = 'K03.0';
INSERT INTO CID(ID, DESCRICAO, EXCLUIDO) VALUES('K03.00', 'Oclusal', 'N');

UPDATE CID SET DESCRICAO = 'Pulpite não especificada' WHERE ID = 'K04.09';
INSERT INTO CID(ID, DESCRICAO, EXCLUIDO) VALUES('K04.1', 'Necrose da polpa', 'N');

UPDATE CID SET DESCRICAO = 'Pericoronite aguda' WHERE ID = 'K05.22';
INSERT INTO CID(ID, DESCRICAO, EXCLUIDO) VALUES('K05.28', 'Outras periodontites agudas especificadas', 'N');

UPDATE CID SET DESCRICAO = 'Hiperplasia irritativa do rebordo alveolar [hiperplasia devido à dentadura]' WHERE ID = 'K06.23';
INSERT INTO CID(ID, DESCRICAO, EXCLUIDO) VALUES('K06.28', 'Outras lesões especificadas da gengiva e do rebordo alveolar sem dentes', 'N');

UPDATE CID SET DESCRICAO = 'Outros cistos dos maxilares especificados' WHERE ID = 'K09.28';
INSERT INTO CID(ID, DESCRICAO, EXCLUIDO) VALUES('K09.29', 'Cisto da mandíbula, não especificado', 'N');

UPDATE CID SET DESCRICAO = 'Periostite dos maxilares' WHERE ID = 'K10.22';
INSERT INTO CID(ID, DESCRICAO, EXCLUIDO) VALUES('K10.23', 'Periostite maxilar crônica', 'N');

UPDATE CID SET DESCRICAO = 'Glossite, não especificada', ID = 'K14.09' WHERE ID = 'Kl';