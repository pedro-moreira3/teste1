package br.com.lume.common.bo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericDAO;
import br.com.lume.common.util.StringUtil;

public class BO<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient GenericDAO<E> dao;

    private Class<E> clazz;

    private Logger log = Logger.getLogger(BO.class);

    private String persistenceUnitName;

    public BO(
            String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public GenericDAO<E> getDao() {
        if (this.dao == null) {
            this.dao = new GenericDAO<>(this.persistenceUnitName);
        }
        return this.dao;
    }

    public E find(E entity) throws Exception {
        return this.getDao().find(entity);
    }

    public E find(Object primaryKey) throws Exception {
        return this.getDao().find(this.getClazz(), primaryKey);
    }

    public boolean refresh(E entity) throws Exception {
        return this.getDao().refresh(entity);
    }

    public boolean persist(E entity) throws Exception {
        StringUtil.toString(entity, this.log);
        if (this.getDao().find(entity) != null) {
            return this.getDao().merge(entity);
        } else {
            return this.getDao().persist(entity);
        }
    }

    public boolean merge(E entity) throws Exception {
        StringUtil.toString(entity, this.log);
        return this.getDao().merge(entity);
    }

    public void persistBatch(List<E> entities) throws Exception {
        this.getDao().persistBatch(entities);
    }

    public void mergeBatch(List<E> entities) throws Exception {
        this.getDao().mergeBatch(entities);
    }

    public boolean remove(E entity) throws Exception {
        StringUtil.toString(entity, this.log);
        return this.getDao().remove(entity);
    }

    public List<E> list(final String jpql) throws Exception {
        return this.getDao().list(jpql);
    }

    public List<E> list(final Query jpql) throws Exception {
        return this.getDao().list(jpql);
    }

    public List<E> listAll() throws Exception {
        return this.getDao().listAll(this.getClazz());
    }

    public Class<E> getClazz() {
        return this.clazz;
    }

    public void setClazz(Class<E> clazz) {
        this.clazz = clazz;
    }

    public String getPersistenceUnitName() {
        return this.persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    protected E findByFields(Map<String, Object> filtros) throws Exception {
        return this.getDao().findByFields(this.clazz, filtros);
    }

    protected List<E> listByFields(Map<String, Object> filtros, String[] ordenacao) throws Exception {
        return this.getDao().listByFields(this.clazz, filtros, ordenacao);
    }

    protected List<E> listByFields(Map<String, Object> filtros) throws Exception {
        return this.listByFields(filtros, null);
    }

    public List<Object[]> listArrayObjects(Query jpql) throws Exception {
        List<Object[]> list = null;
        list = this.getDao().listArrayObjects(jpql);
        return list;
    }

    public List<Object> listObject(Query jpql) throws Exception {
        List<Object> list = null;
        list = this.getDao().listObject(jpql);
        return list;
    }

    protected List<E> listByFields(Map<String, Object> filtros, String[] ordenacao, String[] hints) throws Exception {
        return this.getDao().listByFields(this.clazz, filtros, ordenacao, hints);
    }

    protected List<E> listByFieldsHint(Map<String, Object> filtros, String... hints) throws Exception {
        return this.listByFields(filtros, null, hints);
    }
}
