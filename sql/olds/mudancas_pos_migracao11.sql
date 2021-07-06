CREATE TABLE "objeto_profissional"
(
    "id" serial,
    "obj_int_cod" int8,
    "id_profissional" int8,
    PRIMARY KEY ("id")
);

ALTER TABLE ONLY "objeto_profissional"
    ADD CONSTRAINT objeto_profissional_objeto FOREIGN KEY ("obj_int_cod") REFERENCES seg_objeto("obj_int_cod");

ALTER TABLE ONLY "objeto_profissional"
    ADD CONSTRAINT objeto_profissional_profissional FOREIGN KEY ("id_profissional") REFERENCES profissional("id");