
create table relatorio_agenda_tags(
	ID SERIAL PRIMARY KEY,
	data_execucao_relatorio timestamp without time zone,
	em_execucao boolean,		
	empresa_id bigint references seg_empresa(emp_int_cod),	
	data_de timestamp without time zone,
	data_ate timestamp without time zone,
	paciente_id bigint references paciente(id),
	dentista_executor bigint references profissional(id),
	profissional_alteracao_id bigint references profissional(id),
	profissional_agendamento_id bigint references profissional(id),
	convenio_id bigint references convenio(id),
	mostrar_somente_inicial boolean,
	mostrar_todos_agendamentos boolean,	
	lista_status VARCHAR(255)	
);

alter table RELATORIO_AGENDA_TAGS add column filtro_periodo char(1)


create table relatorio_agenda_colunas(
	ID SERIAL PRIMARY KEY,
	agendamento_id bigint references agendamento(id),
	paciente_id bigint references paciente(id),
	dentista_executor_id bigint references profissional(id),
	relatorio_agenda_tags_id bigint references relatorio_agenda_tags(id),
	empresa_id bigint references seg_empresa(emp_int_cod),	
	nome_paciente VARCHAR(255),
	telefone_paciente VARCHAR(255),
	convenio VARCHAR(255),
	dentista_executor VARCHAR(255),
	consulta_inicial VARCHAR(255),
	data_agendada timestamp without time zone,
	status_agendamento VARCHAR(255)
);

create table relatorio_relacionamento_tags(
	ID SERIAL PRIMARY KEY,
	data_execucao_relatorio timestamp without time zone,
	em_execucao boolean,		
	empresa_id bigint references seg_empresa(emp_int_cod),
	filtro_periodo char(1),
	data_de timestamp without time zone,
	data_ate timestamp without time zone,
	paciente_id bigint references paciente(id),
	status_paciente char(1),
	convenio_id bigint references convenio(id),
	profissional_agendamento_id bigint references profissional(id),	
	tag_sem_agendamento boolean,
	tag_sem_agendamento_futuro boolean,
	tag_sem_retorno_futuro boolean,
	tag_mostrar_todos_agendamentos boolean,
	lista_status VARCHAR(255)	
);

create table relatorio_relacionamento_colunas(
	ID SERIAL PRIMARY KEY,
	paciente_id bigint references paciente(id),
	profissional_id bigint references profissional(id),
	nome_paciente VARCHAR(255),
	convenio VARCHAR(255),
	telefone_paciente VARCHAR(255),
	dentista_ultimo_agendamento VARCHAR(255),
	data_ultimo_agendamento timestamp without time zone,
	status_ultimo_agendamento VARCHAR(255),
	data_proximo_agendamento timestamp without time zone,
	status_proximo_agendamento VARCHAR(255),
	data_proximo_retorno timestamp without time zone,	
	relatorio_relacionamento_tags_id bigint references relatorio_relacionamento_tags(id),
	empresa_id bigint references seg_empresa(emp_int_cod)
);

