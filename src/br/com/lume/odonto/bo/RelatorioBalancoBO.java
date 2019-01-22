package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioBalanco;

public class RelatorioBalancoBO extends BO<RelatorioBalanco> {

    private Logger log = Logger.getLogger(RelatorioBalanco.class);

    private static final long serialVersionUID = 1L;

    public RelatorioBalancoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioBalanco.class);
    }

    public List<RelatorioBalanco> listByDate(Date inicio, Date fim, boolean finalizado, boolean repassado, Profissional profissional) {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT ");
            sb.append("PT.ID, ");
            sb.append("PT.DATA_HORA AS DATA_HORA, ");
            sb.append("PT.DESCRICAO AS DESCRICAO_PLANO, ");
            sb.append("DB1.NOME AS NOME_PACIENTE, ");
            sb.append("DB2.NOME AS NOME_PROFISSIONAL, ");
            sb.append("PT.VALOR_TOTAL_DESCONTO AS VALOR_TOTAL_DESCONTO, ");
            sb.append(
                    "(SELECT SUM(LAN.VALOR) FROM LANCAMENTO LAN,ORCAMENTO ORC WHERE LAN.EXCLUIDO = 'N' AND LAN.ID_ORCAMENTO = ORC.ID AND ORC.ID_PLANO_TRATAMENTO = PT.ID AND LAN.DATA_PAGAMENTO IS NOT NULL ) AS VALOR_PAGO, ");
            sb.append("(SELECT SUM(PTP.VALOR_REPASSE) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID) AS VALOR_REPASSE, ");
            sb.append("(SELECT SUM(PTP.VALOR_REPASSADO) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID) AS VALOR_REPASSADO, ");
            sb.append(
                    "(SELECT SUM(PTPC.VALOR) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP,PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO PTPC WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID AND PTP.ID = PTPC.ID_PLANO_TRATAMENTO_PROCEDIMENTO AND PTPC.EXCLUIDO = 'N') AS VALOR_CUSTO, ");
            sb.append(
                    "(SELECT COALESCE(SUM(LAN.VALOR * (1-(LAN.TRIBUTO/100))),0) FROM LANCAMENTO LAN,ORCAMENTO ORC WHERE LAN.EXCLUIDO = 'N' AND LAN.ID_ORCAMENTO = ORC.ID AND ORC.ID_PLANO_TRATAMENTO = PT.ID AND LAN.DATA_PAGAMENTO IS NOT NULL) AS VALOR_PAGO_TRIBUTADO, ");
            sb.append(
                    "(SELECT COALESCE(SUM(LAN.VALOR * (1-(LAN.TRIBUTO/100))),0) FROM LANCAMENTO LAN,ORCAMENTO ORC WHERE LAN.EXCLUIDO = 'N' AND LAN.ID_ORCAMENTO = ORC.ID AND ORC.ID_PLANO_TRATAMENTO = PT.ID AND LAN.DATA_PAGAMENTO IS NOT NULL) - ");
            sb.append("(SELECT SUM(PTP.VALOR_REPASSADO) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID) AS RESULTADO, ");
            sb.append("PT.DATA_FINALIZADO ");
            sb.append("FROM PLANO_TRATAMENTO PT, PACIENTE PAC, DADOS_BASICOS DB1, PROFISSIONAL PROF, DADOS_BASICOS DB2 ");
            sb.append("WHERE ");
            sb.append("PT.ID_PACIENTE = PAC.ID AND ");
            sb.append("PAC.ID_EMPRESA = ?1 AND ");
            sb.append("DATE(PT.DATA_HORA) BETWEEN ?2 AND ?3 AND ");
            sb.append("PT.ID_PACIENTE = PAC.ID AND ");
            sb.append("PAC.ID_DADOS_BASICOS = DB1.ID AND ");
            sb.append("PT.ID_PROFISSIONAL = PROF.ID AND ");
            sb.append("PROF.ID_DADOS_BASICOS = DB2.ID ");
            if (finalizado) {
                sb.append("AND PT.DATA_FINALIZADO IS NOT NULL ");
            }
            if (repassado) {
                sb.append("AND ");
                sb.append("(SELECT SUM(PTP.VALOR_REPASSE) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID) = ");
                sb.append("(SELECT SUM(PTP.VALOR_REPASSADO) FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID) ");
            }
            if (profissional != null) {
                sb.append(" AND EXISTS( SELECT 1 FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP WHERE PTP.ID_PLANO_TRATAMENTO = PT.ID AND PTP.FINALIZADO_POR = " + profissional.getId() + ") ");
            }
            Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioBalanco.class);
            query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            query.setParameter(2, inicio);
            query.setParameter(3, fim);
            query.setParameter(4, PlanoTratamentoProcedimento.PAGAMENTO_PAGO);
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listByDate", e);
        }
        return null;
    }

    public List<RelatorioBalanco> listAllByFilterToReport(Profissional profissional, Paciente paciente, Procedimento procedimento, Date inicio, Date fim, boolean analitico) {
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append(" PTP.ID, ");
            sql.append(" PT.DATA_HORA, ");
            sql.append(" P.DESCRICAO AS PROCEDIMENTO , ");
            sql.append(" DPR.NOME AS PROFISSIONAL, ");
            sql.append(" DPA.NOME AS PACIENTE, ");
            sql.append(" P.VALOR AS VALOR_PROCEDIMENTO, ");
            sql.append(" PTP.VALOR_DESCONTO AS VALOR_RECEBIDO , ");
            sql.append(" PR.PERCENTUAL_REMUNERACAO , ");
            sql.append(" (select sum(ptpc.VALOR) from PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ptpc where ptpc.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id) CUSTO_TERCEIRIZADO, ");
            sql.append(" (select sum(R.VALOR) from REPASSE R where R.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id) VALOR_REPASSADO , ");
            sql.append(" PTP.TRIBUTO AS TRIBUTO , ");
            sql.append(" PTP.VALOR_DESCONTO*DECODE(PTP.TRIBUTO,null,0,0,0,PTP.TRIBUTO)/100 AS VALOR_TRIBUTO,  ");
            sql.append(
                    " (DECODE(P.TIPO,'N',PR.PERCENTUAL_REMUNERACAO,'M', PR.REMUNERACAO_MANUTENCAO))*(DECODE(ptp.TRIBUTO,null,1,(100-ptp.TRIBUTO)/100)*ptp.VALOR_DESCONTO - decode((select sum(ptpc.VALOR) from PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ptpc where ptpc.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id)/100,null,0,(select sum(ptpc.VALOR) from PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ptpc where ptpc.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id)))/100 CUSTO_PROFISSIONAL, ");
            sql.append(
                    " (SELECT sum((m.valor*mi.UNIDADE)/(select count(id) from AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO where ID_AGENDAMENTO = APTP.ID_AGENDAMENTO)) FROM RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO RKAPTP, AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP, MATERIAL_INDISPONIVEL MI, MATERIAL M WHERE RKAPTP.ID_RESERVA_KIT = MI.ID_RESERVA_KIT AND M.ID = MI.ID_MATERIAL AND RKAPTP.ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO = APTP.ID AND APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID ) AS CUSTO_MATERIAL, ");
            sql.append(
                    " (DECODE(P.TIPO,'N',100-PR.PERCENTUAL_REMUNERACAO,'M', 100-PR.REMUNERACAO_MANUTENCAO))*(DECODE(ptp.TRIBUTO,null,1,(100-ptp.TRIBUTO)/100)*ptp.VALOR_DESCONTO - decode((select sum(ptpc.VALOR) from PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ptpc where ptpc.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id)/100,null,0,(select sum(ptpc.VALOR) from PLANO_TRATAMENTO_PROCEDIMENTO_CUSTO ptpc where ptpc.ID_PLANO_TRATAMENTO_PROCEDIMENTO = ptp.id)))/100  VALOR_LIQUIDO ");
            sql.append(" FROM PLANO_TRATAMENTO PT , ");
            sql.append(" PLANO_TRATAMENTO_PROCEDIMENTO PTP LEFT OUTER ");
            sql.append(" JOIN PROFISSIONAL PR ON PR.ID =PTP.FINALIZADO_POR LEFT OUTER ");
            sql.append(" JOIN DADOS_BASICOS DPR ON PR.ID_DADOS_BASICOS = DPR.ID , ");
            sql.append(" PROCEDIMENTO P, ");
            sql.append(" PACIENTE PA , ");
            sql.append(" DADOS_BASICOS DPA, ");
            sql.append(" REPASSE REP ");
            sql.append(" WHERE PA.ID_DADOS_BASICOS = DPA.ID ");
            sql.append(" AND PT.ID_PACIENTE = PA.ID ");
            sql.append(" AND PTP.ID_PLANO_TRATAMENTO = PT.ID ");
            sql.append(" AND PTP.ID_PROCEDIMENTO = P.ID ");
            sql.append(" AND PTP.EXCLUIDO = 'N' ");
            sql.append(" AND REP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID ");
            sql.append(" AND REP.DATA IS NOT NULL ");
            sql.append(" AND PA.ID_EMPRESA = " + ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            if (paciente != null) {
                sql.append(" AND pa.id = " + paciente.getId());
            }
            if (profissional != null) {
                sql.append(" AND pr.id = " + profissional.getId());
            }
            if (procedimento != null) {
                sql.append(" AND p.id =  " + procedimento.getId());
            }
            if (inicio != null && fim != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(fim);
                c.add(Calendar.DAY_OF_MONTH, 1);
                fim = c.getTime();
                sql.append(" AND PT.DATA_HORA  between '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss"));
                sql.append("' AND '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "' ");
            } else {
                if (inicio != null) {
                    sql.append(" AND PT.DATA_HORA  > '" + Utils.dateToString(inicio, "yyyy-MM-dd HH:mm:ss") + "'");
                } else if (fim != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(fim);
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    fim = c.getTime();
                    sql.append(" AND PT.DATA_HORA < '" + Utils.dateToString(fim, "yyyy-MM-dd HH:mm:ss") + "'");
                }
            }
            Query query = this.getDao().createNativeQuery(sql.toString(), RelatorioBalanco.class);
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listAllByFilterToReport", e);
        }
        return null;
    }
}
