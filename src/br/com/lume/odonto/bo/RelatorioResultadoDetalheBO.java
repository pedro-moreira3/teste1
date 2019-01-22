package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RelatorioOrcamento;
import br.com.lume.odonto.entity.RelatorioResultado;
import br.com.lume.odonto.entity.RelatorioResultadoDetalhe;

public class RelatorioResultadoDetalheBO extends BO<RelatorioResultadoDetalhe> {

    private Logger log = Logger.getLogger(RelatorioOrcamento.class);

    private static final long serialVersionUID = 1L;

    public RelatorioResultadoDetalheBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioResultadoDetalhe.class);
    }

    public List<RelatorioResultadoDetalhe> listDetalhe(RelatorioResultado relatorioResultado) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append(
                "SELECT ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) AS ID, ITE.DESCRICAO, KITE.QUANTIDADE AS QTD_PEDIDA, MAI.QUANTIDADE QTD_UTILIZADA, KITE.QUANTIDADE - MAI.QUANTIDADE AS QTD_N_UTILIZADA, ");
        sb.append("MAT.VALOR AS VALOR, ");
        sb.append("MAI.QUANTIDADE * MAT.VALOR AS VALOR_UTILIZADO ");
        sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
        sb.append("INNER JOIN PLANO_TRATAMENTO PTR ON PTP.ID_PLANO_TRATAMENTO = PTR.ID ");
        sb.append("INNER JOIN AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP ON APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND APTP.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN RESERVA_KIT_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO RAPT ON RAPT.ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO = APTP.ID ");
        sb.append("INNER JOIN RESERVA_KIT REK ON REK.ID = RAPT.ID_RESERVA_KIT AND REK.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN KIT KIT ON KIT.ID = REK.ID_KIT AND KIT.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN KIT_ITEM KITE ON KITE.ID_KIT = KIT.ID AND KITE.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN ITEM ITE ON ITE.ID = KITE.ID_ITEM AND ITE.EXCLUIDO = 'N' AND ITE.TIPO = 'C' ");
        sb.append("INNER JOIN MATERIAL MAT ON MAT.ID_ITEM = ITE.ID AND MAT.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN MATERIAL_INDISPONIVEL MAI ON MAI.ID_MATERIAL =  MAT.ID AND MAI.ID_RESERVA_KIT = REK.ID AND MAI.STATUS = 'UK' ");
        sb.append("WHERE PTP.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioResultadoDetalhe.class);
        query.setParameter(1, relatorioResultado.getId());
        List<RelatorioResultadoDetalhe> detalhes = this.list(query);
        if (detalhes == null) {
            detalhes = new ArrayList<>();
        }
        List<RelatorioResultadoDetalhe> unitarios = listDetalheUnitario(relatorioResultado);
        if (unitarios == null) {
            unitarios = new ArrayList<>();
        }
        detalhes.addAll(unitarios);
        return detalhes;
    }

    public List<RelatorioResultadoDetalhe> listDetalheUnitario(RelatorioResultado relatorioResultado) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) AS ID, ");
        sb.append("ITE.DESCRICAO || ' (Emp. unit.)' AS DESCRICAO, ");
        sb.append("0 AS QTD_PEDIDA, ");
        sb.append("0 AS QTD_N_UTILIZADA, ");
        sb.append("ABS.QUANTIDADE AS QTD_UTILIZADA, ");
        sb.append("MAT.VALOR AS VALOR, ");
        sb.append("ABS.QUANTIDADE * MAT.VALOR AS VALOR_UTILIZADO ");
        sb.append("FROM PLANO_TRATAMENTO_PROCEDIMENTO PTP ");
        sb.append("INNER JOIN AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP ON APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND APTP.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN ABASTECIMENTO_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO AGPP ON AGPP.ID_AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO = APTP.ID ");
        sb.append("INNER JOIN ABASTECIMENTO ABS ON ABS.ID_AGENDAMENTO = APTP.ID_AGENDAMENTO AND ABS.ID = AGPP.ID_ABASTECIMENTO AND ABS.EXCLUIDO = 'N' AND ABS.STATUS = 'UU' ");
        sb.append("INNER JOIN MATERIAL MAT ON MAT.ID = ABS.ID_MATERIAL AND MAT.EXCLUIDO = 'N' ");
        sb.append("INNER JOIN ITEM ITE ON ITE.ID = MAT.ID_ITEM AND ITE.EXCLUIDO = 'N' AND ITE.TIPO = 'C' ");
        sb.append("WHERE PTP.ID = ?1 ");

        Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioResultadoDetalhe.class);
        query.setParameter(1, relatorioResultado.getId());
        return this.list(query);
    }
}
