package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.RepasseItem;
import br.com.lume.odonto.entity.RepasseLancamento;

public class RepasseLancamentoBO extends BO<RepasseLancamento> {

    private Logger log = Logger.getLogger(RepasseLancamentoBO.class);

    private static final long serialVersionUID = 1L;

    public RepasseLancamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RepasseLancamento.class);
    }

    public List<RepasseLancamento> listByPlanoTratamentoProcedimento(Long idPtp) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("planoTratamentoProcedimento.id", idPtp);
        parametros.put("status", RepasseItem.REPASSADO);
        return this.listByFields(parametros);
    }
}
