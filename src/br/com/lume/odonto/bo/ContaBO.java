package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Conta;

public class ContaBO extends BO<Conta> {

    private Logger log = Logger.getLogger(Conta.class);

    private static final long serialVersionUID = 1L;

    public ContaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Conta.class);
    }

    @Override
    public List<Conta> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Conta conta) throws BusinessException, TechnicalException {
        conta.setExcluido(Status.SIM);
        conta.setDataExclusao(Calendar.getInstance().getTime());
        conta.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(conta);
        return true;
    }

    public List<Conta> listByEmpresa() throws Exception {
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

    public List<Conta> listByEmpresaAndDescricaoParcial(String digitacao) {
        try {
            String jpql = "SELECT i FROM Conta AS i " + "WHERE UPPER(i.descricao) LIKE :descricao AND i.idEmpresa = :empresa AND  i.excluido= :excluido";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("descricao", "%" + digitacao.toUpperCase() + "%");
            q.setParameter("excluido", Status.NAO);
            return this.list(q);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresaAndDescricaoParcial", e);
        }
        return null;
    }
}
