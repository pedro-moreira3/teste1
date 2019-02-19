package br.com.lume.security.bo;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.security.entity.Acao;

public class AcaoBO extends BO<Acao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AcaoBO.class);

    public AcaoBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Acao.class);
    }

    @Override
    public boolean persist(Acao entity) throws Exception {
        // TODO Auto-generated method stub
        return super.persist(entity);
    }

}
