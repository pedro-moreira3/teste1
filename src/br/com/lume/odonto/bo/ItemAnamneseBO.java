package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.ItemAnamnese;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.entity.Profissional;

public class ItemAnamneseBO extends BO<ItemAnamnese> {

    /**
     *
     */
    private static final long serialVersionUID = -568437935660836170L;
    private Logger log = Logger.getLogger(ItemAnamneseBO.class);

    public ItemAnamneseBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ItemAnamnese.class);
    }

    @Override
    public boolean remove(ItemAnamnese itemAnamnese) throws BusinessException, TechnicalException {
        itemAnamnese.setExcluido(Status.SIM);
        itemAnamnese.setDataExclusao(Calendar.getInstance().getTime());
        itemAnamnese.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(itemAnamnese);
        return true;
    }

    public ItemAnamnese findByPerguntaAndPacienteAndProfissional(Pergunta pergunta, Paciente paciente, Profissional profissional) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("pergunta.id", pergunta.getId());
            parametros.put("paciente.id", paciente.getId());
            parametros.put("profissional.id", profissional.getId());
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no findByPerguntaAndPaciente", e);
        }
        return null;
    }

    public List<List<ItemAnamnese>> perguntasAnamnese(Map<String, List<Pergunta>> perguntas) {
        List<List<ItemAnamnese>> anamneses = new ArrayList<>();
        ItemAnamnese item = new ItemAnamnese();
        List<Pergunta> grupo = new ArrayList<>();
        if (perguntas != null) {
            for (String especialidade : perguntas.keySet()) {
                List<ItemAnamnese> perguntasAgrupadas = new ArrayList<>();
                grupo = perguntas.get(especialidade);
                for (Pergunta pergunta : grupo) {
                    item = new ItemAnamnese();
                    item.setPergunta(pergunta);
                    item.setEspecialidade(especialidade);
                    perguntasAgrupadas.add(item);
                }
                anamneses.add(perguntasAgrupadas);
            }
        }
        return anamneses;
    }

    public List<ItemAnamnese> perguntasAnamnese(List<Pergunta> perguntas) {
        List<ItemAnamnese> anamneses = new ArrayList<>();
        ItemAnamnese item = new ItemAnamnese();
        if (perguntas != null) {
            for (Pergunta pergunta : perguntas) {
                item = new ItemAnamnese();
                item.setPergunta(pergunta);
                anamneses.add(item);
            }
        }
        return anamneses;
    }

    public List<ItemAnamnese> listByPergunta(long id) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("pergunta.id", id);
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros);
    }
}
