package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.NotaFiscal;

public class NotaFiscalBO extends BO<NotaFiscal> {

    private Logger log = Logger.getLogger(NotaFiscalBO.class);

    private static final long serialVersionUID = 1L;

    public NotaFiscalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(NotaFiscal.class);
    }

    @Override
    public List<NotaFiscal> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<NotaFiscal> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

}
