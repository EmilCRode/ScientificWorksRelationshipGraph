package org.ScientificWorksRelationshipGraph;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import java.util.HashMap;
import java.util.List;

public class Neo4jHandler {

    private SessionFactory sessionFactory;
    private Session session;
    public Neo4jHandler(){
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://neo4j:citations@localhost")
                .connectionPoolSize(150)
                .build();
        sessionFactory = new SessionFactory(configuration, "org.ScientificWorksRelationshipGraph");
        session = sessionFactory.openSession();
    }
    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;

    Iterable<Entity> findAll(Class type) {
        return session.loadAll(type, DEPTH_LIST);
    }
    Object find(Class type, Long id) {
        return session.load(type, id, DEPTH_ENTITY);
    }

    void delete(Class type, Long id) {
        session.delete(session.load(type, id));
    }

    Entity createOrUpdate(Entity entity) {
        session.save(entity, DEPTH_ENTITY);
        //List<Entity> all = (List<Entity>) findAll(entity.getClass());
        //Entity foundEntity = all.get(all.indexOf(entity));
        //return foundEntity;
        return session.load(entity.getClass(), entity.getId());
    }

    public void closeSession(){ this.sessionFactory.close();}

}
