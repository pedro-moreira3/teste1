package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Dominio;
//import br.com.lume.security.bo.EmpresaBO;

public class DominioBO extends BO<Dominio> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DominioBO.class);

    private List<Dominio> dominiosLabel = new ArrayList<>();

    public DominioBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Dominio.class);
    }

    public BigDecimal getTributo() {
        try {
            return new BigDecimal(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpFltImposto());
        } catch (Exception e) {
            this.log.error(e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean remove(Dominio dominio) throws BusinessException, TechnicalException {
        dominio.setExcluido(Status.SIM);
        dominio.setDataExclusao(Calendar.getInstance().getTime());
        dominio.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(dominio);
        return true;
    }

    public List<Dominio> listByEmpresaAndObjeto(String objeto) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE  d.objeto = :objeto AND  d.excluido= :excluido  ";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("excluido", Status.NAO);
        return this.list(q);
    }

    public List<Dominio> listByEmpresa() throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE  d.excluido= :excluido" + " ORDER BY d.objeto";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("excluido", Status.NAO);
        return this.list(q);
    }

    public List<Dominio> listByEmpresaAndObjetoAndTipo(String objeto, String tipo) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE  d.objeto = :objeto AND d.tipo = :tipo AND  d.excluido= :excluido " + " ORDER BY d.nome";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("tipo", tipo);
        q.setParameter("excluido", Status.NAO);
        return this.list(q);
    }

    public List<Dominio> listByObjetoAndTipo(String objeto, String tipo) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE d.objeto = :objeto AND d.tipo = :tipo AND  d.excluido= :excluido " + " ORDER BY d.nome";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("tipo", tipo);
        q.setParameter("excluido", Status.NAO);
        return this.list(q);
    }

    public Dominio findByEmpresaAndObjetoAndTipoAndValor(String objeto, String tipo, String valor) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE d.objeto = :objeto AND d.tipo = :tipo AND d.valor = :valor AND  d.excluido= :excluido";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("tipo", tipo);
        q.setParameter("valor", valor);
        q.setParameter("excluido", Status.NAO);
        List<Dominio> dominios = this.list(q);
        if (dominios.isEmpty()) {
            return null;
        } else {
            return dominios.get(0);// Empresa, Objeto, Tipo e Valor é chave exclusiva
        }
    }

    public Dominio findByEmpresaAndObjetoAndTipoAndValor(String objeto, String tipo, String valor, long idEmpresa) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE d.objeto = :objeto AND d.tipo = :tipo AND d.valor = :valor AND  d.excluido= :excluido and d.idEmpresa = :idEmpresa";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("tipo", tipo);
        q.setParameter("valor", valor);
        q.setParameter("excluido", Status.NAO);
        q.setParameter("idEmpresa", idEmpresa);
        List<Dominio> dominios = this.list(q);
        if (dominios.isEmpty()) {
            return null;
        } else {
            return dominios.get(0);// Empresa, Objeto, Tipo e Valor é chave exclusiva
        }
    }

    public Dominio listByTipoAndObjeto(String valor, String objeto) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("valor", valor);
        parametros.put("objeto", objeto);
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public Dominio findByEmpresaAndObjetoAndTipoAndNome(String objeto, String tipo, String nome) {
        try {
            String jpql = "SELECT d FROM Dominio AS d  WHERE d.objeto = :objeto AND d.tipo = :tipo AND d.nome = :nome AND  d.excluido= :excluido";
            Query q;
            q = this.getDao().createQuery(jpql);
            q.setParameter("objeto", objeto);
            q.setParameter("tipo", tipo);
            q.setParameter("nome", nome);
            q.setParameter("excluido", Status.NAO);
            List<Dominio> dominios = this.list(q);
            if (dominios.isEmpty()) {
                return null;
            } else {
                return dominios.get(0);// Empresa, Objeto, Tipo e Valor é chave exclusiva
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Dominio> listByObjetoAndTipoAndNome(String objeto, String tipo, String nome) {
        try {
            String jpql = "SELECT d FROM Dominio AS d  WHERE   d.objeto = :objeto AND d.tipo = :tipo AND d.nome = :nome AND  d.excluido= :excluido";
            Query q;
            q = this.getDao().createQuery(jpql);
            q.setParameter("objeto", objeto);
            q.setParameter("tipo", tipo);
            q.setParameter("nome", nome);
            q.setParameter("excluido", Status.NAO);
            List<Dominio> dominios = this.list(q);
            if (dominios.isEmpty()) {
                return null;
            } else {
                return dominios;// Empresa, Objeto, Tipo e Valor é chave exclusiva
            }
        } catch (Exception e) {
            this.log.error("ERRO FINALIZA PLANO listByObjetoAndTipoAndNome: ", e);
            e.printStackTrace();
        }
        return null;
    }

    public Dominio findLabel(String objeto) {
        try {
            String jpql = "SELECT d FROM Dominio AS d  WHERE d.objeto = :objeto AND d.tipo = :tipo  AND  d.excluido= :excluido";
            Query q;
            q = this.getDao().createQuery(jpql);
            q.setParameter("objeto", objeto);
            q.setParameter("tipo", "label");
            q.setParameter("excluido", Status.NAO);
            List<Dominio> dominios = this.list(q);
            if (dominios.isEmpty()) {
                return null;
            } else {
                return dominios.get(0);// Empresa, Objeto, Tipo é chave exclusiva
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Dominio> getDominiosLabel() {
        this.dominiosLabel.add(new DominioBO().findLabel("paciente"));
        this.dominiosLabel.add(new DominioBO().findLabel("convenio"));
        this.dominiosLabel.add(new DominioBO().findLabel("filial"));
        this.dominiosLabel.add(new DominioBO().findLabel("profissional"));
        this.dominiosLabel.add(new DominioBO().findLabel("convenioprocedimento"));
        this.dominiosLabel.add(new DominioBO().findLabel("planotratamento"));
        return this.dominiosLabel;
    }

    public Dominio findByObjetoAndTipoAndValor(String objeto, String tipo, String valor) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE  d.objeto = :objeto AND d.tipo = :tipo AND d.valor = :valor AND  d.excluido= :excluido";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("tipo", tipo);
        q.setParameter("valor", valor);
        q.setParameter("excluido", Status.NAO);
        List<Dominio> dominios = this.list(q);
        if (dominios.isEmpty()) {
            return null;
        } else {
            return dominios.get(0);// Empresa, Objeto, Tipo e Valor é chave exclusiva
        }
    }

    public Dominio findByObjetoAndTipo(String objeto, String tipo) throws Exception {
        String jpql = "SELECT d FROM Dominio AS d  WHERE  d.objeto = :objeto AND d.tipo = :tipo AND  d.excluido= :excluido";
        Query q = this.getDao().createQuery(jpql);
        q.setParameter("objeto", objeto);
        q.setParameter("tipo", tipo);
        q.setParameter("excluido", Status.NAO);
        List<Dominio> dominios = this.list(q);
        if (dominios.isEmpty()) {
            return null;
        } else {
            return dominios.get(0);// Empresa, Objeto, Tipo e Valor é chave exclusiva
        }
    }
}
