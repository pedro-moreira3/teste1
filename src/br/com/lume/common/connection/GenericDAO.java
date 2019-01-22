package br.com.lume.common.connection;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException;

public class GenericDAO<E> extends GenericListDAO<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String LUME_SECURITY = "LumeSecurity";

    private Logger log = Logger.getLogger(GenericDAO.class);

    public GenericDAO(String persistenceUnitName) {
        super(persistenceUnitName);
    }

    public EntityTransaction getTransaction() {
        return entityManager.getTransaction();
    }

    public boolean persist(E entity) throws Exception {
        boolean persist = false;
        EntityTransaction tx = this.getTransaction();
        if (entity != null) {
            try {
                if (!tx.isActive()) {
                    tx.begin();
                }
                entityManager.persist(entity);
                entityManager.flush();
                tx.commit();
                // refresh(entity);
                persist = true;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                // log.error("Erro no persist", e);
                throw new Exception(e);
            }
        }
        return persist;
    }

    public boolean merge(E entity) throws Exception {
        boolean merge = false;
        EntityTransaction tx = this.getTransaction();
        if (entity != null) {
            try {
                if (!tx.isActive()) {
                    tx.begin();
                }
                entityManager.merge(entity);
                entityManager.flush();
                tx.commit();
                // refresh(entity);
                merge = true;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                this.log.error("Erro no merge", e);
                throw new Exception(e);
            }
        }
        return merge;
    }

    public Long persistNative(String insert) throws Exception {
        Long idGerado = null;
        EntityTransaction tx = this.getTransaction();
        try {
            if (!tx.isActive()) {
                tx.begin();
            }
            java.sql.Connection connection = entityManager.unwrap(java.sql.Connection.class);
            Statement pstmt = connection.createStatement();
            //System.out.println(insert);
            pstmt.execute(insert, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                idGerado = rs.getLong(1);
            }
            pstmt.close();
            tx.commit();
        } catch (SqlIntegrityConstraintViolationException e) {
            tx.rollback();
            this.log.error("Erro no persistNative", e);
        } catch (Exception e) {
            tx.rollback();
            this.log.error("Erro no persistNative", e);
            throw new Exception(e);
        } finally {
            return idGerado;
        }
    }

    public void persistBatchNative(List<String> inserts) throws Exception {
        EntityTransaction tx = this.getTransaction();
        try {
            // entityManager.setFlushMode(FlushModeType.COMMIT);
            if (!tx.isActive()) {
                tx.begin();
            }
            int j = 0;
            for (String insert : inserts) {
                Query q = entityManager.createNativeQuery(insert);
                q.executeUpdate();
                if (++j % 500 == 0) {
                    entityManager.flush();
                }
            }
            entityManager.flush();
            tx.commit();
            // entityManager.setFlushMode(FlushModeType.AUTO);
        } catch (Exception e) {
            tx.rollback();
            this.log.error("Erro no merge", e);
            throw new Exception(e);
        }
    }

    public synchronized void persistBatchSync(List<E> entities) throws Exception {
        this.persistBatch(entities);
    }

    public void persistBatch(List<E> entities) throws Exception {
        // entityManager.setFlushMode(FlushModeType.COMMIT);
        // System.out.println("Comecou 'persistBatch' " + Utils.dateToString(new Date(), "HH:mm:ss"));
        EntityTransaction tx = this.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        int i = 0;
        for (E entity : entities) {
            try {
                entityManager.persist(entity);
                if (++i % 2000 == 0) {
                    entityManager.flush();
                    if (i % 100000 == 0) {
                        entityManager.clear();
                    }
                }
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                this.log.error("Erro no persistBatch", e);
                throw new Exception(e);
            }
        }
        entityManager.flush();
        // entityManager.clear();
        tx.commit();
        // System.out.println("Terminou 'persistBatch' " + Utils.dateToString(new Date(), "HH:mm:ss"));
    }

    public void mergeBatch(List<E> entities) throws Exception {
        EntityTransaction tx = this.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        int i = 0;
        for (E entity : entities) {
            try {
                entityManager.merge(entity);
                if (i++ % 2000 == 0) {
                    entityManager.flush();
                    // entityManager.clear();
                }
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                this.log.error("Erro no merge", e);
                throw new Exception(e);
            }
        }
        entityManager.flush();
        // entityManager.clear();
        tx.commit();
    }

    public void clearEntityManager() {
        entityManager.clear();
    }

    public void detachObject(Object o) {
        entityManager.detach(o);
    }

    public boolean remove(E entity) throws Exception {
        boolean remove = false;
        EntityTransaction tx = this.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        try {
            if (entity != null) {
                entity = entityManager.merge(entity);
                entityManager.remove(entity);
                entityManager.flush();
                tx.commit();
                remove = true;
            }
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new Exception(e);
        }
        return remove;
    }

    public void removeBatch(List<E> entities) throws Exception {
        EntityTransaction tx = this.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        int i = 0;
        for (E entity : entities) {
            try {
                entity = entityManager.merge(entity);
                entityManager.remove(entityManager.merge(entity));
                if (i++ % 2000 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw new Exception(e);
            }
        }
        // entityManager.flush();
        entityManager.clear();
        tx.commit();
    }

    public boolean refresh(E entity) throws Exception {
        boolean refresh = false;
        if (entity != null) {
            entityManager.refresh(entity);
            refresh = true;
        }
        return refresh;
    }

    public Query createQuery(String jpql) throws Exception {
        Query query = null;
        if (jpql != null && !jpql.isEmpty()) {
            query = entityManager.createQuery(jpql);
        }
        return query;
    }

    public Query createNamedQuery(String nameQuery) throws Exception {
        Query query = null;
        if (nameQuery != null && !nameQuery.isEmpty()) {
            query = entityManager.createNamedQuery(nameQuery);
        }
        return query;
    }

    public void executeQuery(Query query) throws Exception {
        EntityTransaction tx = this.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        if (query != null) {
            try {
                query.executeUpdate();
                entityManager.flush();
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                // log.error("Erro no persist", e);
                throw new Exception(e);
            }
        }
    }

    public Query createNativeQuery(String sql, Class clazz) throws Exception {
        Query query = null;
        if (sql != null && !sql.isEmpty()) {
            query = entityManager.createNativeQuery(sql, clazz);
        }
        return query;
    }

    public Query createNativeQuery(String sql) throws Exception {
        Query query = null;
        if (sql != null && !sql.isEmpty()) {
            query = entityManager.createNativeQuery(sql);
        }
        return query;
    }
}
