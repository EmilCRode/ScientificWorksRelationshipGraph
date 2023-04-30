package org.ScientificWorksRelationshipGraph;

import org.LocalitySensitiveHashing.Hashing;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.*;

public class Neo4jHandler {

    private final SessionFactory sessionFactory;
    private final Session session;
    private final List<Entity> worksInDatabase;
    private final List<Entity> authorsInDatabase;
    private final List<LocalitySensitiveHash> hashesInDatabase;
    private final Hashing hashingHandler;

    public Neo4jHandler(){
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://neo4j:citations@localhost")
                .connectionPoolSize(150)
                .build();
        sessionFactory = new SessionFactory(configuration, "org.ScientificWorksRelationshipGraph");
        session = sessionFactory.openSession();
        authorsInDatabase = new ArrayList<>();
        authorsInDatabase.addAll(session.loadAll(Author.class,2).stream().toList());
        worksInDatabase = new ArrayList<>();
        worksInDatabase.addAll(session.loadAll(Work.class).stream().toList());
        hashesInDatabase = new ArrayList<>();
        hashesInDatabase.addAll(session.loadAll(LocalitySensitiveHash.class).stream().toList());
        this.hashingHandler = new Hashing();
    }
    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = -1;
    Object find(Class type, Long id) {
        return session.load(type, id, DEPTH_ENTITY);
    }

    Iterable<Entity> findAll(Class type){
        return (type == Work.class) ?  worksInDatabase : authorsInDatabase;
    }

    public void delete(Class type, Long id) {
        session.delete(session.load(type, id));
    }

    public void deleteAll(Class type){
        session.deleteAll(type);
    }

    Entity createOrUpdate(Entity entity) {
        session.save(entity, -1);
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

    public Entity findSimilar(Entity entity) throws IllegalAccessException {
        final double threshhold = 0.9;
        Entity closestMatch = null;
        double currentBestScore = 0;
        double currentScore;

        Iterable<Entity> inDatabase = findAll(entity.getClass());

        for (Entity entityToCompare : inDatabase) {
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
    public List<Work> getSimilarCandidates(Work work){
        List<Work> candidates = new ArrayList<>();
        for(LocalitySensitiveHash lshHashObject: work.getHashes()){
            for(Entity entity: lshHashObject.getHashedToThis()){
                if(entity.getClass() == Work.class){ candidates.add((Work) entity);}
            }
        }
        return candidates;
    }
    public List<Author> getSimilarCandidates(Author author){
        List<Author> candidates = new ArrayList<>();
        for(LocalitySensitiveHash lshHashObject: author.getHashes()){
            for(Entity entity: lshHashObject.getHashedToThis()){
                if(entity.getClass() == Author.class){ candidates.add((Author) entity);}
            }
        }
        return candidates;
    }
    public LocalitySensitiveHash createOrUpdateHashObject(int hashValue, Entity hashedEntity){
        Map<String, Object> params = new HashMap<>(1);
        params.put ("hashValue", hashValue);
        LocalitySensitiveHash foundInDb = null;
        for(LocalitySensitiveHash lsh: hashesInDatabase){
            if(lsh.getHashValue() == hashValue) { foundInDb = lsh; }
        }
        if(foundInDb != null){
            foundInDb.getHashedToThis().add(hashedEntity);
            return foundInDb;
        } else {
            LocalitySensitiveHash newHashObject = new LocalitySensitiveHash(hashValue);
            newHashObject.getHashedToThis().add(hashedEntity);
            hashesInDatabase.add(newHashObject);
            return newHashObject;
        }
    }
    /*
    /**
     * This method is used to compare the similarity of attributes of the same Class taken from Work and Author
     * @param obj1
     * @param obj2
     * @return a score which should be between 0 and 1 (it exceedes 1 sometimes because of floating point weirdness)

    public double attributeSimilarityScore(Object obj1, Object obj2){
        switch(obj1.getClass().getSimpleName()){
            case "String":
                //Just compares if word tokens match (even if not in the right order
                return Distances.cosineSimilarity((CharSequence) obj1, (CharSequence) obj2);
            case "Integer":
                //Assumes a year, returns 1 for same year and 0 otherwise
                return Distances.compareYears((int) obj1, (int) obj2);
            case "Date":
                Date date1 = (Date) obj1;
                int result = (date1.compareTo((Date) obj2) == 0) ? 1 : 0;
                return result;
            case "Author":
                ((Author) obj1).compareTo((Author) obj2);
            default:
                System.out.println("[Error]: Attribute to compare is not of Type String, Integer or Date");
        }
        return 0;
    }
    public double attributeSimilarityScore(List<Object> objectList1, List<Object> objectList2){
        try {
            double result = 0;
            int numberOfComparisons = 0;
            for(int j = 0; j < objectList1.size(); j++) {
                for(int k = 0; k < objectList2.size(); k++){
                    result = result + attributeSimilarityScore(objectList1.get(j), objectList2.get(k));
                    numberOfComparisons++;
                }
            }
            return result / numberOfComparisons;
        } catch (NullPointerException exception) {System.out.println(exception.getMessage());}
        return -1;
    }*/
    public List<Entity> getWorksInDatabase() {
        return worksInDatabase;
    }

    public List<Entity> getAuthorsInDatabase() {
        return authorsInDatabase;
    }

    public Hashing getHashingHandler(){ return this.hashingHandler; }
}
