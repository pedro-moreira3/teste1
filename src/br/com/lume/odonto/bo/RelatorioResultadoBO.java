package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioOrcamento;
import br.com.lume.odonto.entity.RelatorioResultado;

public class RelatorioResultadoBO extends BO<RelatorioResultado> {

    private Logger log = Logger.getLogger(RelatorioOrcamento.class);

    private static final long serialVersionUID = 1L;

    public RelatorioResultadoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioResultado.class);
    }

    public BigDecimal findValorGastoMaterial(long aptpID) throws Exception {

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        sb.append("SUM(MI.QUANTIDADE * (SELECT M2.VALOR FROM MATERIAL M2 WHERE M2.ID_ITEM = M.ID_ITEM ORDER BY ID DESC FETCH FIRST 1 ROWS ONLY)) AS VALOR_MATERIAL ");
        sb.append("FROM RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO RK_A_PTP, RESERVA_KIT RK, MATERIAL_INDISPONIVEL MI, MATERIAL M, ITEM I ");
        sb.append(
                "WHERE RK_A_PTP.ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO IN (SELECT ID FROM AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO WHERE ID_PLANO_TRATAMENTO_PROCEDIMENTO = ?1 AND EXCLUIDO = 'N' ) ");
        sb.append("AND RK_A_PTP.ID_RESERVA_KIT = RK.ID ");
        sb.append("AND RK.ID = MI.ID_RESERVA_KIT ");
        sb.append("AND MI.ID_MATERIAL = M.ID ");
        sb.append("AND M.ID_ITEM = I.ID ");
        sb.append("AND I.TIPO = 'C' ");
        sb.append("AND MI.STATUS = 'UK' ");
        sb.append("AND RK.EXCLUIDO = 'N' AND MI.EXCLUIDO = 'N'");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, aptpID);
        List<Object> resultList = query.getResultList();
        return resultList != null && !resultList.isEmpty() ? (BigDecimal) resultList.get(0) : new BigDecimal(0);
    }

    public List<RelatorioResultado> listAll(Date inicio, Date fim, Profissional profissional) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT PTP.ID AS ID, PTP.FINALIZADO_POR AS ID_DENTISTA, PTR.ID_PACIENTE, PTP.ID_PROCEDIMENTO AS ID_PROCEDIMENTO, PTP.ID_PLANO_TRATAMENTO,PTP.DATA_FINALIZADO::DATE AS DATA, ");
        sb.append("VALOR_DESCONTO AS VALOR, ");
        sb.append("SUM(MAI.QUANTIDADE * MAT.VALOR) AS VALOR_MATERIAL, ");
        sb.append("VALOR_REPASSE AS VALOR_REPASSADO ");
        sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
        sb.append("INNER JOIN PLANO_TRATAMENTO PTR ON PTP.ID_PLANO_TRATAMENTO = PTR.ID ");
        sb.append("INNER JOIN PROFISSIONAL PRO ON PTP.FINALIZADO_POR = PRO.ID ");
        sb.append("LEFT OUTER JOIN AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP ON APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND APTP.EXCLUIDO = 'N' ");
        sb.append("LEFT OUTER JOIN RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO RAPT ON RAPT.ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO = APTP.ID ");
        sb.append("LEFT OUTER JOIN RESERVA_KIT REK ON REK.ID = RAPT.ID_RESERVA_KIT AND REK.EXCLUIDO = 'N' ");
        sb.append("LEFT OUTER JOIN KIT KIT ON KIT.ID = REK.ID_KIT AND KIT.EXCLUIDO = 'N' ");
        sb.append("LEFT OUTER JOIN KIT_ITEM KITE ON KITE.ID_KIT = KIT.ID AND KITE.EXCLUIDO = 'N' ");
        sb.append("LEFT OUTER JOIN ITEM ITE ON ITE.ID = KITE.ID_ITEM AND ITE.EXCLUIDO = 'N' AND ITE.TIPO = 'C' ");
        sb.append("LEFT OUTER JOIN MATERIAL MAT ON MAT.ID_ITEM = ITE.ID AND MAT.EXCLUIDO = 'N' ");
        sb.append("LEFT OUTER JOIN MATERIAL_INDISPONIVEL MAI ON MAI.ID_MATERIAL =  MAT.ID AND MAI.ID_RESERVA_KIT = REK.ID AND MAI.STATUS = 'UK' ");
        sb.append("WHERE ");
        sb.append("PTP.EXCLUIDO = 'N' ");
        sb.append("AND PTP.DATA_FINALIZADO::DATE BETWEEN '" + sdf.format(inicio) + "' AND '" + sdf.format(fim) + "' ");
        sb.append("AND PRO.ID_EMPRESA = ?1 ");
        if (profissional != null) {
            sb.append("AND PTP.FINALIZADO_POR = " + profissional.getId() + " ");
        }
        sb.append("GROUP BY PTP.ID,PTP.FINALIZADO_POR, PTR.ID_PACIENTE, PTP.ID_PROCEDIMENTO, PTP.ID_PLANO_TRATAMENTO, VALOR_DESCONTO, VALOR_REPASSE, PTP.DATA_FINALIZADO::DATE ");
        sb.append("ORDER BY PTP.ID_PLANO_TRATAMENTO ");

        Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioResultado.class);
        query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        List<RelatorioResultado> list = this.list(query);
        for (RelatorioResultado relatorioResultado : list) {
            if (relatorioResultado.getValorMaterial() == null) {
                relatorioResultado.setValorMaterial(new BigDecimal(0d));
            }
            relatorioResultado.setValorMaterial(relatorioResultado.getValorMaterial().add(findValorEmprestimoUnitario(relatorioResultado.getId())));
        }
        return list;
    }

    public BigDecimal findValorEmprestimoUnitario(Long idPTP) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT ");
        sb.append("SUM(ABS.QUANTIDADE * MAT.VALOR) ");
        sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
        sb.append("INNER JOIN AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP ON APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND APTP.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN ABASTECIMENTO_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO AGPP ON AGPP.ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO = APTP.ID ");
        sb.append("INNER JOIN ABASTECIMENTO ABS ON ABS.ID_AGENDAMENTO = APTP.ID_AGENDAMENTO AND ABS.ID = AGPP.ID_ABASTECIMENTO AND ABS.EXCLUIDO = 'N' AND ABS.STATUS = 'UU' ");
        sb.append("INNER JOIN MATERIAL MAT ON MAT.ID = ABS.ID_MATERIAL AND MAT.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN ITEM ITE ON ITE.ID = MAT.ID_ITEM AND ITE.EXCLUIDO = 'N' AND ITE.TIPO = 'C' ");
        sb.append("WHERE PTP.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, idPTP);
        Object r = query.getSingleResult();
        return r != null ? (BigDecimal) r : new BigDecimal(0d);
    }

}
