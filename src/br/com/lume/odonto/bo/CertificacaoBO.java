package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Certificacao;

public class CertificacaoBO extends BO<Certificacao> {

    private static final Logger log = Logger.getLogger(Certificacao.class);

    private static final long serialVersionUID = 1L;

    public CertificacaoBO() {
        super(PersistenceUnitName.PORTALID);
//        setClazz(Certificacao.class);
    }

    public List<Certificacao> listByDescricao(String descricao) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("descricao", descricao);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByDescricao", e);
        }
        return null;
    }

    public Certificacao findByDescricao(String descricao) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("descricao", descricao);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByDescricao", e);
        }
        return null;
    }
}
