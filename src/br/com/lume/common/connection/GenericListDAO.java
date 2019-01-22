package br.com.lume.common.connection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;

import br.com.lume.common.exception.techinical.ErroBuscarRegistro;
import br.com.lume.common.exception.techinical.ErroListarRegistros;

public class GenericListDAO<E> {

    public static final String FILTRO_GENERICO_QUERY = "FILTRO_GENERICO_QUERY";

    protected final EntityManager entityManager;

    private Logger log = Logger.getLogger(GenericListDAO.class);

    public GenericListDAO(String persistenceUnitName) {
        this.entityManager = Connection.getInstance().getEntityManager(persistenceUnitName);
    }

    public E find(Class<E> classEntity, Object primaryKey) throws ErroBuscarRegistro {
        E entity = null;
        if (classEntity != null && primaryKey != null) {
            try {
                entity = this.entityManager.find(classEntity, primaryKey);
            } catch (Exception e) {
                throw new ErroBuscarRegistro(e);
            }
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public E find(E entity) throws ErroBuscarRegistro {
        if (entity != null) {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                    boolean oldAccessible = field.isAccessible();
                    field.setAccessible(true);
                    try {
                        Object key = field.get(entity);
                        field.setAccessible(oldAccessible);
                        return this.find((Class<E>) entity.getClass(), key);
                    } catch (Exception e) {
                        throw new ErroBuscarRegistro(e);
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<E> list(Query query) throws ErroListarRegistros {
        List<E> entities = null;
        if (query != null) {
            try {
                entities = query.getResultList();
            } catch (Exception e) {
                throw new ErroListarRegistros(e);
            }
        }
        return entities;
    }

    public List<E> list(String jpql) throws ErroListarRegistros {
        return this.list(this.createQuery(jpql));
    }

    public List<E> listAll(Class<E> classEntity) throws ErroListarRegistros {
        return this.list(this.createQuery("select o from " + classEntity.getSimpleName() + " o"));
    }

    @SuppressWarnings("unchecked")
    public Object getSingleResult(Query query) throws ErroBuscarRegistro {
        Object entity = null;
        if (query != null) {
            try {
                entity = query.getSingleResult();
            } catch (NoResultException nre) {
                return null;
            } catch (Exception e) {
                throw new ErroBuscarRegistro(e);
            }
        }
        return entity;
    }

    private Query createQuery(String jpql) {
        Query query = null;
        if (jpql != null && !jpql.isEmpty()) {
            query = this.entityManager.createQuery(jpql);
        }
        return query;
    }

    public E findByField(Class<E> classEntity, String nomeCampo, Object valorCampo) throws ErroBuscarRegistro {
        Query query = this.createQuery("select o from " + classEntity.getSimpleName() + " o where o." + nomeCampo + " = :" + nomeCampo.replace('.', '_'));
        query.setParameter(nomeCampo.replace('.', '_'), valorCampo);
        return (E) this.getSingleResult(query);
    }

    public E findByFields(Class<E> classEntity, Map<String, Object> filtros) throws ErroBuscarRegistro {
        Query query = this.montarQueryDinamica(classEntity, filtros);
        return (E) this.getSingleResult(query);
    }

    public List<E> listByFields(Class<E> classEntity, Map<String, Object> filtros, String[] ordenacao) throws ErroListarRegistros {
        Query query = this.montarQueryDinamica(classEntity, filtros, ordenacao);
        return this.list(query);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> listArrayObjects(Query query) throws ErroListarRegistros {
        List<Object[]> entity = null;
        if (query != null) {
            entity = query.getResultList();
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public List<Object> listObject(Query query) throws ErroListarRegistros {
        List<Object> entity = null;
        if (query != null) {
            entity = query.getResultList();
        }
        return entity;
    }

    private Query montarQueryDinamica(Class<E> classEntity, Map<String, Object> filtros, String[] ordenacoes) {
        String sql = "select o from " + classEntity.getSimpleName() + " o ";
        if (filtros != null && !filtros.isEmpty()) {
            sql += "where";
            for (String nomeFiltro : filtros.keySet()) {
                if (FILTRO_GENERICO_QUERY.equals(filtros.get(nomeFiltro))) {
                    sql += " and " + nomeFiltro;
                } else {
                    sql += " and o." + nomeFiltro + " = :" + nomeFiltro.replace('.', '_');
                }

            }
        }
        sql = sql.replaceFirst(" and ", " ");
        if (ordenacoes != null) {
            sql += " order by ";
            for (String ordenacao : ordenacoes) {
                sql += " ,o." + ordenacao;
            }
            sql = sql.replaceFirst(" ,", " ");
        }
        Query query = this.createQuery(sql);
        for (String nomeCampo : filtros.keySet()) {
            if (!FILTRO_GENERICO_QUERY.equals(filtros.get(nomeCampo))) {
                query.setParameter(nomeCampo.replace('.', '_'), filtros.get(nomeCampo));
            }
        }

        return query;
    }

    private Query montarQueryDinamica(Class<E> classEntity, Map<String, Object> filtros) {
        return this.montarQueryDinamica(classEntity, filtros, null);
    }

    public List<E> listByFields(Class<E> classEntity, Map<String, Object> filtros, String[] ordenacao, String[] hints) throws Exception {
        Query query = this.montarQueryDinamica(classEntity, filtros, ordenacao);
        if (hints != null && hints.length > 0) {
            for (String hint : hints) {
                query.setHint(QueryHints.BATCH_TYPE, "IN");
                query.setHint(QueryHints.BATCH, hint);
            }
        }
        return this.list(query);
    }
}
