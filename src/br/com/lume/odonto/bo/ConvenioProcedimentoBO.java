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
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.RelatorioConvenioProcedimento;

public class ConvenioProcedimentoBO extends BO<ConvenioProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConvenioProcedimentoBO.class);

    public ConvenioProcedimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ConvenioProcedimento.class);
    }

    @Override
    public boolean remove(ConvenioProcedimento convenioProcedimento) throws BusinessException, TechnicalException {
        convenioProcedimento.setExcluido(Status.SIM);
        convenioProcedimento.setDataExclusao(Calendar.getInstance().getTime());
        convenioProcedimento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(convenioProcedimento);
        return true;
    }

    public List<RelatorioConvenioProcedimento> listRelatorioConvenioProcedimentoByEmpresa(int mes, int ano) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * CP.ID AS ID,P.DESCRICAO,DB.NOME,CP.VALOR,CP.VALOR_REPASSE, ");
        sb.append("(    SELECT ");
        sb.append(" SUM(VALOR_DESCONTO) ");
        sb.append(" FROM ");
        sb.append(" PLANO_TRATAMENTO_PROCEDIMENTO PTP, PLANO_TRATAMENTO PT ");
        sb.append(" WHERE ");
        sb.append(" PTP.ID_PLANO_TRATAMENTO = PT.ID AND ");
        sb.append(" PT.ID_CONVENIO = C.ID AND ");
        sb.append(" PTP.ID_PROCEDIMENTO = P.ID AND ");
        sb.append(" PTP.STATUS = 'F' AND ");
        sb.append(" EXTRACT(MONTH FROM PTP.DATA_FINALIZADO) =  " + mes + "  AND EXTRACT(YEAR FROM PTP.DATA_FINALIZADO) = " + ano + " ");
        sb.append(") AS VALOR_FATURADO ");
        sb.append("FROM ");
        sb.append("CONVENIO_PROCEDIMENTO CP, CONVENIO C, PROCEDIMENTO P, DADOS_BASICOS DB ");
        sb.append("WHERE CP.ID_EMPRESA = ?1 AND ");
        sb.append("CP.ID_CONVENIO = C.ID AND ");
        sb.append("CP.ID_PROCEDIMENTO = P.ID AND ");
        sb.append("C.ID_DADOS_BASICOS = DB.ID AND ");
        sb.append("C.EXCLUIDO = 'N' AND ");
        sb.append("P.EXCLUIDO = 'N' AND ");
        sb.append("CP.EXCLUIDO = 'N' ");
        sb.append("ORDER BY P.DESCRICAO ");
        Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioConvenioProcedimento.class);
        query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        return query.getResultList();
    }

    @Override
    public List<ConvenioProcedimento> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<ConvenioProcedimento> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<ConvenioProcedimento> listByConvenio(Convenio convenio) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("convenio", convenio);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public ConvenioProcedimento findByConvenioAndProcedimento(Convenio convenio, Procedimento procedimento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("convenio", convenio);
            parametros.put("procedimento", procedimento);
            //parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("idEmpresa", 41);
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no findByConvenioAndProcedimento", e);
        }
        return null;
    }

    public List<ConvenioProcedimento> listByProcedimento(Procedimento procedimento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("procedimento", procedimento);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
