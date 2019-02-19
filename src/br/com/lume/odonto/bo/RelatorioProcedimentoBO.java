package br.com.lume.odonto.bo;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Filial;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioProcedimento;

public class RelatorioProcedimentoBO extends BO<RelatorioProcedimento> {

    private Logger log = Logger.getLogger(RelatorioProcedimento.class);

    private static final long serialVersionUID = 1L;

    public RelatorioProcedimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioProcedimento.class);
    }

    public List<RelatorioProcedimento> listAllByPacienteAndProfissionalAndFilialAndEspecialidadeAndTipo(Paciente paciente, Profissional profissional, Filial filial, Especialidade especialidade,
            Date inicio, Date fim, String tipo) {
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("select ");
            sql.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) AS ID, ");
            sql.append("pa.id AS IDPACIENTE, ");
            sql.append("po.DESCRICAO AS PROCEDIMENTO , ");
            sql.append("d.NOME AS PROFISSIONAL , ");
            sql.append("d2.NOME as PACIENTE , ");
            sql.append("d3.NOME as FILIAL , ");
            sql.append("e.DESCRICAO AS ESPECIALIDADE, ");
            sql.append("pa.TITULAR AS IDTITULAR, ");
            sql.append("ptp.DATA_FINALIZADO, ");
            sql.append("po.VALOR_SERVICO, ");
            sql.append("po.VALOR, ");
            sql.append("pt.DATA_HORA ");
            sql.append("from procedimento po , ");
            sql.append("plano_tratamento_procedimento ptp , ");
            sql.append("PLANO_TRATAMENTO pt , ");
            sql.append("profissional p , ");
            sql.append("ESPECIALIDADE e , ");
            sql.append("DADOS_BASICOS d , ");
            sql.append("PACIENTE pa , ");
            sql.append("DADOS_BASICOS d2 , ");
            sql.append("DADOS_BASICOS d3 , ");
            sql.append("filial f , ");
            sql.append("PROFISSIONAL_FILIAL pf ");
            sql.append("where ptp.ID_PROCEDIMENTO = po.ID ");
            sql.append("and ptp.ID_PLANO_TRATAMENTO = pt.ID ");
            sql.append("and p.ID_DADOS_BASICOS = d.ID ");
            sql.append("and po.ID_ESPECIALIDADE = e.ID ");
            sql.append("and pt.ID_PROFISSIONAL = p.ID ");
            sql.append("and pt.ID_PACIENTE = pa.ID ");
            sql.append("and pa.ID_DADOS_BASICOS = d2.ID ");
            sql.append("and pf.ID_PROFISSIONAL = ptp.finalizado_por ");
            sql.append("and pf.ID_FILIAL = f.ID ");
            sql.append("and f.ID_DADOS_BASICOS = d3.ID ");
            sql.append("and p.ID_EMPRESA = pa.id_empresa ");
            sql.append("and e.id_empresa = pa.id_empresa ");
            sql.append("and pa.id_empresa = " + ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            if (inicio != null && fim != null) {
                sql.append(" and DATE(ptp.data_finalizado) BETWEEN ?1 AND ?2 ");
            }
            if (tipo != null) {
                if (tipo.equals(Status.SIM)) {
                    sql.append("  and (pa.TITULAR is null or pa.TITULAR = pa.ID) ");
                } else if (tipo.equals(Status.NAO)) {
                    sql.append(" and pa.TITULAR != pa.ID ");
                }
            }
            if (paciente != null) {
                sql.append(" and pa.id = " + paciente.getId());
            }
            if (profissional != null) {
                sql.append(" and p.id = " + profissional.getId());
            }
            if (filial != null) {
                sql.append(" and f.id =  " + filial.getId());
            }
            if (especialidade != null) {
                sql.append("  and e.id = " + especialidade.getId());
            }
            Query query = this.getDao().createNativeQuery(sql.toString(), RelatorioProcedimento.class);
            query.setParameter(1, inicio);
            query.setParameter(2, fim);
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listAllByPacienteAndProfissionalAndFilialAndEspecialidadeAndTipo", e);
        }
        return null;
    }
}
