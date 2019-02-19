package br.com.lume.odonto.bo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Anamnese;
import br.com.lume.odonto.entity.ItemAnamnese;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.AssinaXML;

public class AnamneseBO extends BO<Anamnese> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AnamneseBO.class);

    public AnamneseBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Anamnese.class);
    }

    @Override
    public boolean remove(Anamnese anamnese) throws BusinessException, TechnicalException {
        anamnese.setExcluido(Status.SIM);
        anamnese.setDataExclusao(Calendar.getInstance().getTime());
        anamnese.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(anamnese);
        return true;
    }

    public boolean persist(Profissional profissional, Paciente paciente, List<ItemAnamnese> anamnesesTodos, boolean xmlAssinado) throws Exception {
        Anamnese pacienteAnamnese = new Anamnese();
        Date date = Calendar.getInstance().getTime();
        pacienteAnamnese.setPaciente(paciente);
        pacienteAnamnese.setProfissional(profissional);
        pacienteAnamnese.setDataHora(date);

        for (ItemAnamnese ana : anamnesesTodos) {
            ana.setAnamnese(pacienteAnamnese);
        }
        pacienteAnamnese.setXmlAssinado("");
        pacienteAnamnese.setItensAnamnese(anamnesesTodos);
        return this.persist(pacienteAnamnese);
    }

    private void geraXmlAssinado(Profissional profissional, List<ItemAnamnese> anamneseTodos, Date date, Anamnese pacienteAnamnese) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><anamneses>");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        builder.append("<data_hora>" + sdf.format(date) + "</data_hora>");

        for (ItemAnamnese ana : anamneseTodos) {
            builder.append(ana.toXml());
        }

        builder.append("</anamneses>");
        pacienteAnamnese.setXmlAssinado(AssinaXML.assinaXML(builder.toString(), "anamneses", profissional));

    }

    public boolean persistByProfissional(Profissional profissional, Paciente paciente, List<List<ItemAnamnese>> anamneses) throws Exception {
        return this.persist(profissional, paciente, this.getAnamnesesTodos(anamneses), true);
    }

    public boolean persistByPaciente(Profissional profissional, Paciente paciente, List<ItemAnamnese> anamneses) throws Exception {
        return this.persist(profissional, paciente, anamneses, false);
    }

    public List<ItemAnamnese> getAnamnesesTodos(List<List<ItemAnamnese>> anamneses) {

        List<ItemAnamnese> anamneseTodos = new ArrayList<>();
        for (List<ItemAnamnese> listas : anamneses) {
            if (listas != null) {
                anamneseTodos.addAll(listas);
            }
        }
        return anamneseTodos;
    }

    public List<Anamnese> listByPaciente(Paciente paciente) {
        try {
            if (paciente != null) {
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("paciente.id", paciente.getId());
                parametros.put("excluido", Status.NAO);
                return this.listByFields(parametros, new String[] { "dataHora desc" });
            }
        } catch (Exception e) {
            this.log.error("Erro no listByPaciente", e);
        }
        return null;
    }
}
