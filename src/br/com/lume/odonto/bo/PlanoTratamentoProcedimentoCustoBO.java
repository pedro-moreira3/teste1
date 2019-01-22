package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Abastecimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;

public class PlanoTratamentoProcedimentoCustoBO extends BO<PlanoTratamentoProcedimentoCusto> {

    private Logger log = Logger.getLogger(PlanoTratamentoProcedimentoCustoBO.class);

    private static final long serialVersionUID = 1L;

    public PlanoTratamentoProcedimentoCustoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(PlanoTratamentoProcedimentoCusto.class);
    }

    public PlanoTratamentoProcedimentoCusto findByAbastecimento(Abastecimento abastecimento) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("abastecimento", abastecimento);
        List<PlanoTratamentoProcedimentoCusto> lista = this.listByFields(parametros);
        return lista != null && !lista.isEmpty() ? lista.get(0) : null;
    }
}
