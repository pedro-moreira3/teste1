drop table repasse_item;
drop table repasse_lancamento;
drop table repasse_profissional;


CREATE TABLE inteli."repasse_profissional"
(
    "id" bigserial,
    "data" timestamp,
    "status" char(1),
    "data_pagamento" timestamp,
    "id_profissional_pagamento" int8,
    "id_profissional" int8,
    PRIMARY KEY ("id")
);

ALTER TABLE ONLY "repasse_profissional"
    ADD CONSTRAINT "repasse_profissional_profissional" FOREIGN KEY ("id_profissional") REFERENCES "profissional"(id);
    
ALTER TABLE ONLY "repasse_profissional"
    ADD CONSTRAINT "repasse_profissional_profpagamento" FOREIGN KEY ("id_profissional_pagamento") REFERENCES "profissional"(id);    

--------------------------

CREATE TABLE inteli."repasse_item"
(
    "id" bigserial,
    "id_repasse" int8,
    "id_plano_tratamento_procedimento" int8,
    "status" char(1),
    "valor" numeric (10,2) ,
    PRIMARY KEY ("id")
);

ALTER TABLE ONLY "repasse_item"
    ADD CONSTRAINT "repasse_item_repasse" FOREIGN KEY ("id_repasse") REFERENCES "repasse_profissional"(id); 	

ALTER TABLE ONLY "repasse_item"
    ADD CONSTRAINT "repasse_item_ptp" FOREIGN KEY ("id_plano_tratamento_procedimento") REFERENCES plano_tratamento_procedimento(id); 	
    
--------------------------
    
CREATE TABLE inteli."repasse_lancamento"
(
    "id" bigserial,
    "id_plano_tratamento_procedimento" int8,
    "id_lancamento" int8,
    "valor" numeric (10,2) ,
    "valor_desconto" numeric (10,2) ,
    "status" char(1),
    PRIMARY KEY ("id")
);


ALTER TABLE ONLY "repasse_lancamento"
    ADD CONSTRAINT "repasse_lancamento_lancamento" FOREIGN KEY ("id_lancamento") REFERENCES "lancamento"(id); 	

ALTER TABLE ONLY "repasse_lancamento"
    ADD CONSTRAINT "repasse_lancamento_ptp" FOREIGN KEY ("id_plano_tratamento_procedimento") REFERENCES plano_tratamento_procedimento(id); 	