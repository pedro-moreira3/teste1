package br.com.lume.odonto.bo;

import java.util.Calendar;

import br.com.lume.common.bo.BO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.CID;

public class CIDBO extends BO<CID> {

    private static final long serialVersionUID = 1L;

    public CIDBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(CID.class);
    }

    @Override
    public boolean remove(CID cid) throws BusinessException, TechnicalException, Exception {
        cid.setExcluido(Status.SIM);
        cid.setDataExclusao(Calendar.getInstance().getTime());
        cid.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(cid);
        return true;
    }
}
