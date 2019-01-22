package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Pedido;

public class PedidoBO extends BO<Pedido> {

    private Logger log = Logger.getLogger(PedidoBO.class);

    private static final long serialVersionUID = 1L;

    public PedidoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Pedido.class);
    }

    @Override
    public boolean remove(Pedido pedido) throws BusinessException, TechnicalException {
        pedido.setExcluido(Status.SIM);
        pedido.setDataExclusao(Calendar.getInstance().getTime());
        pedido.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(pedido);
        return true;
    }

    @Override
    public List<Pedido> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Pedido> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Pedido> listByEmpresaAndProfissional() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("idProfissional", ProfissionalBO.getProfissionalLogado().getIdUsuario());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
