
ALTER TABLE inteli.lancamento
    ADD COLUMN id_tarifa bigint;

 ALTER TABLE ONLY inteli.tarifa ADD CONSTRAINT pk_tarifa_id PRIMARY KEY (ID); 
    
 ALTER TABLE ONLY inteli.lancamento ADD CONSTRAINT lancamento_tarifa FOREIGN KEY (id_tarifa) REFERENCES inteli.tarifa(id);
  	
