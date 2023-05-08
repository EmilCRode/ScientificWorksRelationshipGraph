package org.ScientificWorksRelationshipGraph;

import org.LocalitySensitiveHashing.Hashing;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.*;

public class Neo4jHandler {

    private final SessionFactory sessionFactory;
    private final Session session;
    private final Map<Integer, LocalitySensitiveHash> hashesInDatabase;
    private final Hashing hashingHandler;
    public Neo4jHandler(){
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://neo4j:citations@localhost")
                .connectionPoolSize(150)
                .build();
        sessionFactory = new SessionFactory(configuration, "org.ScientificWorksRelationshipGraph");
        session = sessionFactory.openSession();
        hashesInDatabase = new HashMap<>();
        for(LocalitySensitiveHash lshObjectInDb: session.loadAll(LocalitySensitiveHash.class,2).stream().toList()){
            hashesInDatabase.put(lshObjectInDb.getHashValue(), lshObjectInDb);
        }
        this.hashingHandler = new Hashing();
    }
    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 3;
    Object find(Class type, Long id) {
        return session.load(type, id, DEPTH_ENTITY);
    }

    Collection<Object> findAll(Class type){
        return session.loadAll(type, 2);
    }
    public void delete(Class type, Long id) {
        session.delete(session.load(type, id));
    }

    public void deleteAll(Class type){
        session.deleteAll(type);
    }

    Entity createOrUpdate(Entity entity) {
        session.save(entity, DEPTH_ENTITY);
        return session.load(entity.getClass(), entity.getId());
    }
    public void closeSession(){ this.sessionFactory.close();}
    /**
     * This method takes an entity and returns the most similar Entity by comparing it to all entities of its class in the database.
     * null is returned if the threshhold of @value #threshhold
     * @param entity
     * @return
     * @throws IllegalAccessException
     */
    public Entity findSimilar(Entity entity, int[] hashValues) throws IllegalAccessException {
        final double threshhold = 0.9;
        Entity closestMatch = null;
        double currentBestScore = 0;
        double currentScore;
        Set<Entity> candidates = getSimilarCandidates(hashValues, entity.getClass());

        for (Entity entityToCompare : candidates) {
            currentScore = entitySimilarity(entity, entityToCompare);
            if (currentScore > currentBestScore) {
                currentBestScore = currentScore;
                closestMatch = entityToCompare;
            }
        }
        return (currentBestScore > threshhold) ? closestMatch : null;
    }
    /**
     This method is used to compare two Entities. it returns a value between 0 and 1 (some unexpected floating point behaviour might result in values slightly outside of scope)
     * @param entity1 An Entity to compare; Entity of type Work is assumed
     * @param entity2 An Entity to compare to the first; Entity of type Work is expected
     */
    public double entitySimilarity(Entity entity1, Entity entity2){
        if(!Objects.equals(entity1.getClass(), entity2.getClass())){
            System.out.println("[ERROR]: Classmissmatch between" + entity1.getClass() + " and " +  entity2.getClass() +"in" + this.getClass().getName() + ".enitySimilarity()");
            return -1;
        }
        switch(entity1.getClass().getSimpleName()){
            case "Work":
                return ( (Work) entity1).compareTo( (Work) entity2);
            case "Author":
                return ((Author) entity1).compareTo((Author) entity2);
        }
        return 0;
    }
    public Set<Entity> getSimilarCandidates(int[] hashValues, Class type){
        Set<Entity> candidates = new HashSet<>();
        Set<LocalitySensitiveHash> lshObjects = new HashSet<>();
        for(int i:hashValues){
            lshObjects.add(this.hashesInDatabase.get(i));
        }
        lshObjects.remove(null);
        for(LocalitySensitiveHash localitySensitiveHash: lshObjects){
            for(Entity possibleCandidate: localitySensitiveHash.getHashedToThis()){
                if(possibleCandidate.getClass().equals(type)) { candidates.add(possibleCandidate); }
            }
        }
        return candidates;
    }
    public LocalitySensitiveHash createOrUpdateHashObject(int hashValue, Entity hashedEntity){
        LocalitySensitiveHash foundInDb = hashesInDatabase.get(hashValue);
        if(foundInDb != null){
            foundInDb.getHashedToThis().add(hashedEntity);
            return foundInDb;
        } else {
            LocalitySensitiveHash newHashObject = new LocalitySensitiveHash(hashValue);
            newHashObject.getHashedToThis().add(hashedEntity);
            hashesInDatabase.put(hashValue, newHashObject);
            return newHashObject;
        }
    }
    public Hashing getHashingHandler(){ return this.hashingHandler; }
}
