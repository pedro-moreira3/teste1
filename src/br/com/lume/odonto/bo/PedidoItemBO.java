package br.com.lume.odonto.bo;

import java.util.Calendar;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.PedidoItem;

public class PedidoItemBO extends BO<PedidoItem> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PedidoItemBO.class);

    public PedidoItemBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(PedidoItem.class);
    }

    @Override
    public boolean remove(PedidoItem pedidoItem) throws BusinessException, TechnicalException {
        pedidoItem.setExcluido(Status.SIM);
        pedidoItem.setDataExclusao(Calendar.getInstance().getTime());
        pedidoItem.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(pedidoItem);
        return true;
    }

}
