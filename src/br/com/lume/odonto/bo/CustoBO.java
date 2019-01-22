package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;

public class CustoBO extends BO<PlanoTratamentoProcedimentoCusto> {

    /**
     *
     */
    private static final long serialVersionUID = 8488804355378293993L;
    private Logger log = Logger.getLogger(CustoBO.class);

    public CustoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(PlanoTratamentoProcedimentoCusto.class);
    }

    @Override
    public boolean remove(PlanoTratamentoProcedimentoCusto custo) throws BusinessException, TechnicalException {
        custo.setExcluido(Status.SIM);
        custo.setDataExclusao(Calendar.getInstance().getTime());
        custo.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(custo);
        return true;
    }

    private String tratarCamposIn(List<Paciente> pacientes) {
        String out = "";
        for (Paciente p : pacientes) {
            out += "," + p.getId();
        }
        out = out.replaceFirst(",", "");
        return out;
    }

    public List<PlanoTratamentoProcedimentoCusto> listByPaciente(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("planoTratamentoProcedimento.planoTratamento.paciente", paciente);
        parametros.put("planoTratamentoProcedimento.planoTratamento.excluido", Status.NAO);
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, null);
    }

    public List<PlanoTratamentoProcedimentoCusto> listbyIdCusto(long id) throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("* ");
            sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ");
            sb.append("WHERE ");
            sb.append("ID_PLANO_TRATAMENTO_PROCEDIMENTO = ?1 ");
            sb.append("AND EXCLUIDO = ?2");
            Query query = this.getDao().createNativeQuery(sb.toString(), PlanoTratamentoProcedimentoCusto.class);
            query.setParameter(1, id);
            query.setParameter(2, Status.NAO);
            return query.getResultList();
        } catch (Exception e) {
            this.log.error("Erro no listComCredito", e);
        }
        return null;
    }

}
