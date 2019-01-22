CREATE TABLE "LS"."SEG_ACAO"
(
   ACA_INT_COD bigint PRIMARY KEY NOT NULL,
   ACA_STR_DES varchar(50) NOT NULL
)
;
CREATE TABLE "LS"."SEG_COMUNICADO"
(
   CMN_INT_COD bigint PRIMARY KEY NOT NULL,
   CMN_STR_DES varchar(3000) NOT NULL,
   CMN_CHA_STS char(1) DEFAULT 'A' NOT NULL,
   SIS_INT_COD bigint,
   CMN_DTM_CADASTRO timestamp NOT NULL,
   CMN_DTM_INI timestamp NOT NULL,
   CMN_DTM_FIM timestamp NOT NULL
)
;
CREATE TABLE "LS"."SEG_EMPCOMUNICADO"
(
   ECM_INT_COD bigint PRIMARY KEY NOT NULL,
   EMP_INT_COD bigint NOT NULL,
   CMN_INT_COD bigint NOT NULL
)
;
CREATE TABLE "LS"."SEG_EMPRESA"
(
   EMP_INT_COD bigint PRIMARY KEY NOT NULL,
   EMP_STR_NME varchar(100) NOT NULL,
   EMP_STR_NMEFANTASIA varchar(100),
   EMP_CHA_CNPJ char(18),
   EMP_STR_ENDERECO varchar(100),
   EMP_STR_BAIRRO varchar(50),
   EMP_STR_CIDADE varchar(50),
   EMP_CHA_UF char(2),
   EMP_CHA_CEP char(10),
   EMP_CHA_FONE char(15),
   EMP_CHA_FAX char(15),
   EMP_CHA_INSESTADUAL char(20),
   EMP_CHA_INSMUNICIPAL char(20),
   EMP_STR_RESPONSAVEL varchar(100),
   EMP_STR_URL varchar(200),
   EMP_STR_EMAIL varchar(100),
   EMP_CHA_STS char(1) NOT NULL,
   EMP_DTM_CRIACAO date,
   EMP_CHA_TRIAL char(1) DEFAULT 'N',
   EMP_STR_EMAIL_PAG varchar(100),
   EMP_DTM_EXPIRACAO date,
   EMP_DTM_PAGAMENTO date,
   ID_PLANO bigint,
   EMP_STR_ASSINATURAID varchar(20),
   EMP_CHA_TIPO char(1),
   EMP_CHA_CPF char(15),
   EMP_CHA_RG char(15),
   EMP_CHA_CRO char(30),
   EMP_STR_COMPLEMENTO varchar(100),
   EMP_DTM_ACEITE timestamp,
   EMP_CHA_IP char(15),
   EMP_CHA_NUMENDERECO char(15),
   EMP_STR_ORGAO_EXPEDIDOR varchar(100),
   EMP_CHA_TROCA_PLANO char(1) DEFAULT 'N',
   EMP_STR_PAYERID varchar(20)
)
;
CREATE TABLE "LS"."SEG_LOGACESSO"
(
   LOG_INT_COD bigint PRIMARY KEY NOT NULL,
   USU_INT_COD bigint NOT NULL,
   OBJ_INT_COD bigint NOT NULL,
   LOG_DTM_ENTRADA timestamp,
   LOG_STR_IP varchar(30),
   LOG_STR_ACAO varchar(200)
)
;
CREATE TABLE "LS"."SEG_OBJETO"
(
   OBJ_INT_COD bigint PRIMARY KEY NOT NULL,
   OBJ_INT_CODPAI bigint,
   OBJ_STR_DES varchar(80) NOT NULL,
   OBJ_CHA_STS char(1) NOT NULL,
   OBJ_STR_CAMINHO char(100),
   SIS_INT_COD bigint,
   OBJ_INT_ORDEM int,
   OBJ_CHA_TIPO char(1) DEFAULT 'L' NOT NULL,
   OBJ_STR_ICON varchar(50)
)
;
CREATE TABLE "LS"."SEG_PERFIL"
(
   PER_INT_COD bigint PRIMARY KEY NOT NULL,
   SIS_INT_COD bigint NOT NULL,
   PER_STR_DES varchar(50) NOT NULL,
   PER_CHA_STS char(1) NOT NULL
)
;
CREATE TABLE "LS"."SEG_PEROBJETO"
(
   POB_INT_COD bigint PRIMARY KEY NOT NULL,
   PER_INT_COD bigint NOT NULL,
   OBJ_INT_COD bigint NOT NULL
)
;
CREATE TABLE "LS"."SEG_PERUSUARIO"
(
   PEU_INT_COD bigint PRIMARY KEY NOT NULL,
   PER_INT_COD bigint NOT NULL,
   USU_INT_COD bigint NOT NULL
)
;
CREATE TABLE "LS"."SEG_RESTRICAO"
(
   RES_INT_COD bigint PRIMARY KEY NOT NULL,
   ACA_INT_COD bigint NOT NULL,
   OBJ_INT_COD bigint,
   PER_INT_COD bigint
)
;
CREATE TABLE "LS"."SEG_SISTEMA"
(
   SIS_INT_COD bigint PRIMARY KEY NOT NULL,
   SIS_STR_DES varchar(60) NOT NULL,
   SIS_CHA_STS char(1) NOT NULL,
   SIS_CHA_SIGLA char(10)
)
;
CREATE TABLE "LS"."SEG_USUARIO"
(
   USU_INT_COD bigint PRIMARY KEY NOT NULL,
   USU_STR_NME varchar(100) NOT NULL,
   USU_STR_EML varchar(100),
   USU_STR_LOGIN varchar(100) NOT NULL,
   USU_STR_SENHA varchar(32) NOT NULL,
   USU_INT_DIASTROCASENHA smallint NOT NULL,
   USU_DTM_ULTTROCASENHA timestamp NOT NULL,
   USU_CHA_INLOGACESSO char(1) NOT NULL,
   USU_CHA_CPF char(15),
   USU_CHA_STS char(1) NOT NULL,
   USU_CHA_ADM char(1) DEFAULT 'N' NOT NULL
)
;
CREATE UNIQUE INDEX SQL160912153225410 ON "LS"."SEG_ACAO"(ACA_INT_COD)
;
ALTER TABLE "LS"."SEG_COMUNICADO"
ADD CONSTRAINT CC1315226280299
FOREIGN KEY (SIS_INT_COD)
REFERENCES "LS"."SEG_SISTEMA"(SIS_INT_COD)
;
CREATE UNIQUE INDEX CC1315226074364 ON "LS"."SEG_COMUNICADO"(CMN_INT_COD)
;
ALTER TABLE "LS"."SEG_EMPCOMUNICADO"
ADD CONSTRAINT CC1315226456382
FOREIGN KEY (EMP_INT_COD)
REFERENCES "LS"."SEG_EMPRESA"(EMP_INT_COD)
;
ALTER TABLE "LS"."SEG_EMPCOMUNICADO"
ADD CONSTRAINT CC1315226464702
FOREIGN KEY (CMN_INT_COD)
REFERENCES "LS"."SEG_COMUNICADO"(CMN_INT_COD)
;
CREATE UNIQUE INDEX CC1315226450038 ON "LS"."SEG_EMPCOMUNICADO"(ECM_INT_COD)
;
CREATE UNIQUE INDEX SEG_EMPRESA_PK ON "LS"."SEG_EMPRESA"(EMP_INT_COD)
;
ALTER TABLE "LS"."SEG_LOGACESSO"
ADD CONSTRAINT SEG_LOGACESSO_SEG_USUARIO
FOREIGN KEY (USU_INT_COD)
REFERENCES "LS"."SEG_USUARIO"(USU_INT_COD)
;
ALTER TABLE "LS"."SEG_LOGACESSO"
ADD CONSTRAINT SEG_LOGACESSO_SEG_OBJETO
FOREIGN KEY (OBJ_INT_COD)
REFERENCES "LS"."SEG_OBJETO"(OBJ_INT_COD)
;
CREATE UNIQUE INDEX SEG_LOGACESSO_PK ON "LS"."SEG_LOGACESSO"(LOG_INT_COD)
;
ALTER TABLE "LS"."SEG_OBJETO"
ADD CONSTRAINT FK_OBJETO_SISTEMA
FOREIGN KEY (SIS_INT_COD)
REFERENCES "LS"."SEG_SISTEMA"(SIS_INT_COD)
;
CREATE UNIQUE INDEX SEG_OBJETO_PK ON "LS"."SEG_OBJETO"(OBJ_INT_COD)
;
ALTER TABLE "LS"."SEG_PERFIL"
ADD CONSTRAINT SEG_PERFIL_SEG_SISTEMA
FOREIGN KEY (SIS_INT_COD)
REFERENCES "LS"."SEG_SISTEMA"(SIS_INT_COD)
;
CREATE UNIQUE INDEX SEG_PERFIL_PK ON "LS"."SEG_PERFIL"(PER_INT_COD)
;
ALTER TABLE "LS"."SEG_PEROBJETO"
ADD CONSTRAINT CC1315229238259
FOREIGN KEY (OBJ_INT_COD)
REFERENCES "LS"."SEG_OBJETO"(OBJ_INT_COD)
;
ALTER TABLE "LS"."SEG_PEROBJETO"
ADD CONSTRAINT CC1315229231052
FOREIGN KEY (PER_INT_COD)
REFERENCES "LS"."SEG_PERFIL"(PER_INT_COD)
;
CREATE UNIQUE INDEX CC1315229225905 ON "LS"."SEG_PEROBJETO"(POB_INT_COD)
;
ALTER TABLE "LS"."SEG_PERUSUARIO"
ADD CONSTRAINT CC1315240193090
FOREIGN KEY (USU_INT_COD)
REFERENCES "LS"."SEG_USUARIO"(USU_INT_COD)
;
ALTER TABLE "LS"."SEG_PERUSUARIO"
ADD CONSTRAINT CC1315240185969
FOREIGN KEY (PER_INT_COD)
REFERENCES "LS"."SEG_PERFIL"(PER_INT_COD)
;
CREATE UNIQUE INDEX CC1315240181189 ON "LS"."SEG_PERUSUARIO"(PEU_INT_COD)
;
ALTER TABLE "LS"."SEG_RESTRICAO"
ADD CONSTRAINT CC1359128665620
FOREIGN KEY (ACA_INT_COD)
REFERENCES "LS"."SEG_ACAO"(ACA_INT_COD)
;
ALTER TABLE "LS"."SEG_RESTRICAO"
ADD CONSTRAINT CC1359372635615
FOREIGN KEY (OBJ_INT_COD)
REFERENCES "LS"."SEG_OBJETO"(OBJ_INT_COD)
;
CREATE UNIQUE INDEX SQL160912153225870 ON "LS"."SEG_RESTRICAO"(RES_INT_COD)
;
CREATE UNIQUE INDEX SEG_SISTEMA_PK ON "LS"."SEG_SISTEMA"(SIS_INT_COD)
;
CREATE UNIQUE INDEX SEG_USUARIO_PK ON "LS"."SEG_USUARIO"(USU_INT_COD)
;
