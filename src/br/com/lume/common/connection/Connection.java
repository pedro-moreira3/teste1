package br.com.lume.common.connection;

import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

public class Connection implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Connection instance;

    private Logger log = Logger.getLogger(Connection.class);

    public HashMap<String, EntityManagerFactory> entityManagerFactoryMap = new HashMap<>();

    private Connection() {
    }

    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    private EntityManager createEntityManager(String persistenceUnitName) {
        try {
            EntityManagerFactory emf = this.entityManagerFactoryMap.get(persistenceUnitName);
            if (emf == null) {
                emf = Persistence.createEntityManagerFactory(persistenceUnitName);
                this.entityManagerFactoryMap.put(persistenceUnitName, emf);
            }
            return emf.createEntityManager();
        } catch (Exception e) {
            this.log.error("Erro no createEntityManager", e);
        }
        return null;
    }

    public EntityManager getEntityManager(String persistenceUnitName) {
        return this.createEntityManager(persistenceUnitName);
    }
}
