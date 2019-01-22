package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.entity.Resposta;
import br.com.lume.security.entity.Empresa;

public class PerguntaBO extends BO<Pergunta> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PerguntaBO.class);

    public PerguntaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Pergunta.class);
    }

    @Override
    public boolean remove(Pergunta pergunta) throws BusinessException, TechnicalException {
        pergunta.setExcluido(Status.SIM);
        pergunta.setDataExclusao(Calendar.getInstance().getTime());
        pergunta.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(pergunta);
        return true;
    }

    public List<Pergunta> listComTipoRespComplexa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            Long idEmpresa = ProfissionalBO.getProfissionalLogado().getIdEmpresa();
            parametros.put("o.tipoResposta in ('" + Pergunta.TIPO_RESPOSTA_VARIAS_EM_VARIAS + "','" + Pergunta.TIPO_RESPOSTA_UMA_EM_VARIAS + "')", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("idEmpresa", idEmpresa);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listComTipoRespComplexa", e);
        }
        return null;
    }

    public List<Pergunta> listPreCadastro(Paciente paciente) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("preCadastro", "S");
            parametros.put("idEmpresa", paciente.getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "ordem asc" });
        } catch (Exception e) {
            this.log.error("Erro no listGenericasPrecadastro", e);
        }
        return null;
    }

    public Map<String, List<Pergunta>> listByEspecialidade(List<Especialidade> profissionalEspecialidades) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            Long idEmpresa = ProfissionalBO.getProfissionalLogado().getIdEmpresa();
            if (profissionalEspecialidades != null && profissionalEspecialidades.size() > 0 && profissionalEspecialidades.get(0) != null) {
                String query = "( (o.especialidade is null) or (o.especialidade.id in ( ";
                for (Especialidade pe : profissionalEspecialidades) {
                    query += pe.getId();
                    if (!(pe.equals(profissionalEspecialidades.get(profissionalEspecialidades.size() - 1)))) {
                        query += ",";
                    }
                }
                query += ")))";
                parametros.put(query, GenericListDAO.FILTRO_GENERICO_QUERY);
            } else {
                parametros.put("o.especialidade is null", GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            parametros.put("idEmpresa", idEmpresa);
            parametros.put("excluido", Status.NAO);
            return this.group(this.listByFields(parametros, new String[] { "ordem asc" }));
        } catch (Exception e) {
            this.log.error("Erro no listByEspecialidade", e);
        }
        return null;
    }

    public Map<String, List<Pergunta>> group(List<Pergunta> perguntas) {
        Map<String, List<Pergunta>> perguntasAgrupadas = new HashMap<>();
        String descricao = GENERICA;
        for (Pergunta p : perguntas) {
            if (p.getEspecialidade() != null) {
                descricao = p.getEspecialidade().getDescricao();
            } else {
                descricao = GENERICA;
            }
            if (!(perguntasAgrupadas.containsKey(descricao))) {
                perguntasAgrupadas.put(descricao, new ArrayList<Pergunta>());
            }
            perguntasAgrupadas.get(descricao).add(p);
        }
        return perguntasAgrupadas;
    }

    public List<Pergunta> listByEspecialidade(Especialidade especialidade) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            Long idEmpresa = ProfissionalBO.getProfissionalLogado().getIdEmpresa();
            if (especialidade != null) {
                parametros.put("especialidade.id", especialidade.getId());
            } else {
                parametros.put("o.especialidade is null", GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            parametros.put("idEmpresa", idEmpresa);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "ordem asc" });
        } catch (Exception e) {
            this.log.error("Erro no listByEspecialidade", e);
        }
        return null;
    }

    public static final String GENERICA = "GENÉRICAS";

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            EspecialidadeBO especialidadeBO = new EspecialidadeBO();
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            List<Pergunta> perguntas = this.listByFields(parametros);
            RespostaBO respostaBO = new RespostaBO();

            for (Pergunta pergunta : perguntas) {
                Especialidade esp = especialidadeBO.findByDescricaoAndEmpresa(pergunta.getEspecialidade().getDescricao(), destino);
                pergunta.setEspecialidade(esp);
                pergunta.setId(0L);
                pergunta.setIdEmpresa(destino.getEmpIntCod());
                detach(pergunta);
                persist(pergunta);
            }
            for (Pergunta pergunta : perguntas) {

                List<Resposta> respostas = pergunta.getRespostas();

                for (Resposta resposta : respostas) {
                    resposta.setId(0L);
                    respostaBO.detach(resposta);
                    respostaBO.persist(resposta);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
