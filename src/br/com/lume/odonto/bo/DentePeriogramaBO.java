package br.com.lume.odonto.bo;

import java.util.List;

import javax.persistence.Query;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DentePeriograma;

public class DentePeriogramaBO extends BO<DentePeriograma> {

    public DentePeriogramaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(DentePeriograma.class);
    }

    public List<DentePeriograma> listByFaceDente(int idPeriograma, String face, int denteDe, int denteAte) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("* ");
        sb.append("FROM DENTE_PERIOGRAMA WHERE ");
        sb.append("ID_PERIOGRAMA = ?1 AND ");
        sb.append("FACE = ?2 AND ");
        sb.append("DENTE >= ?3 AND DENTE <= ?4 ");
        sb.append("ORDER BY DENTE ASC ");
        Query q = this.getDao().createNativeQuery(sb.toString(), DentePeriograma.class);
        q.setParameter(1, idPeriograma);
        q.setParameter(2, face);
        q.setParameter(3, denteDe);
        q.setParameter(4, denteAte);
        return q.getResultList();
    }

}
