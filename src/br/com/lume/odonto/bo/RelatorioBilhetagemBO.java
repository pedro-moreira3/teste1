package br.com.lume.odonto.bo;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.RelatorioBilhetagem;

public class RelatorioBilhetagemBO extends BO<RelatorioBilhetagem> {

    private Logger log = Logger.getLogger(RelatorioBilhetagem.class);

    private static final long serialVersionUID = 1L;

    public RelatorioBilhetagemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioBilhetagem.class);
    }

    public List<RelatorioBilhetagem> listAllByVigenciaAndStatus(Date inicio, Date fim, String status) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(
                    "select a.id, d.nome PACIENTE, dd.nome PROFISSIONAL,a.inicio, a.fim, decode(a.EXCLUIDO,'S','Excluído',decode(a.status,'A','Atendido','C','Cancelado','N','Não Confirmado','S','Confirmado','P','Pré Agendado','B','Falta','E','Encaixe','R','Remarcado','I','Cliente na Clínica','O','Em Atendimento'))  STATUS, count(pr.DESCRICAO) \"QUANTIDADE_DE_PROCEDIMENTOS\" ");
            sb.append(
                    "from agendamento a, paciente pa, dados_basicos d, procedimento pr, plano_tratamento pt, plano_tratamento_procedimento ptp, AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO aptp, profissional pp, DADOS_BASICOS dd ");
            sb.append("where a.inicio between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "' and '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ");
            sb.append("and a.ID_PACIENTE = pa.ID ");
            sb.append("and pa.ID_DADOS_BASICOS = d.ID ");
            sb.append("and a.ID = aptp.ID_AGENDAMENTO ");
            sb.append("and aptp.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.ID ");
            sb.append("and ptp.ID_PLANO_TRATAMENTO = pt.ID ");
            sb.append("and ptp.ID_PROCEDIMENTO = pr.ID ");
            sb.append("and a.ID_DENTISTA = pp.ID ");
            sb.append("and pp.ID_DADOS_BASICOS = dd.ID ");
            if (status.equals("EX")) {
                sb.append("and a.excluido = 'S'");
            } else {
                sb.append("and a.status = '" + status + "' ");
            }
            Procedimento procedimentoInicial = new ProcedimentoBO().findByProcedimentoInicial();
            if (procedimentoInicial != null) {
                sb.append("and pr.id != " + procedimentoInicial.getId());
            }
            sb.append(" and pp.id_empresa =  " + ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            sb.append(
                    " group by a.id ,d.nome , dd.nome ,a.inicio, a.fim,decode(a.EXCLUIDO,'S','Excluído',decode(a.status,'A','Atendido','C','Cancelado','N','Não Confirmado','S','Confirmado','P','Pré Agendado','B','Falta','E','Encaixe','R','Remarcado','I','Cliente na Clínica','O','Em Atendimento')) ");
            Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioBilhetagem.class);
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listAllByVigencia", e);
        }
        return null;
    }
}
