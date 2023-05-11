package org.ScientificWorksRelationshipGraph;

import me.tongfei.progressbar.ProgressBar;
import org.LocalitySensitiveHashing.Hashing;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

import java.lang.reflect.Array;
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
        this.hashingHandler = new Hashing();
        List<Work> worksInDatabase = session.loadAll(Work.class, 1).stream().toList();
        List<Author> authorsInDatabase = session.loadAll(Author.class, 1).stream().toList();
        List<Entity> entitiesInDatabase = new ArrayList<>();
        entitiesInDatabase.addAll(worksInDatabase);
        entitiesInDatabase.addAll(authorsInDatabase);
        ProgressBar pb = new ProgressBar("Hashing Entities in Database", entitiesInDatabase.size()).start();
        for(Entity entity: entitiesInDatabase){
            int[] currentHashValues = hashingHandler.generateLSHHashValues(entity.compareString());
            for(Integer hashValue: currentHashValues){
                LocalitySensitiveHash currentHashObject = hashesInDatabase.get(hashValue);
                currentHashObject = (currentHashObject == null)? new LocalitySensitiveHash(hashValue): currentHashObject;
                currentHashObject.getHashedToThis().add(entity);
                hashesInDatabase.put(hashValue, currentHashObject);
            }
            pb.step();
        }
        pb.stop();
        System.out.println("Finished hashing Entities in Database");
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
    public void closeSession(){
        /*List<Author> authors =new ArrayList<>();
        Iterable<Map<String, Object>> results = this.session.query("Match(a:Author{firstname:\"Taofeek\"}) Return a", new HashMap<>()).queryResults();
        for(Map result: results){
            authors.add((Author) result.get("a"));
        }
        int[] hashes0 = hashingHandler.generateLSHHashValues(authors.get(0).compareString());
        int[] hashes1 = hashingHandler.generateLSHHashValues(authors.get(1).compareString());
        int[] hashes2 = hashingHandler.generateLSHHashValues(authors.get(2).compareString());
        for(Integer i: hashes0){
            for(Integer x1: hashes1){
                if(i==x1){ System.out.print(" "+ i + " is in hashes0 and hashes1");}
            }
            for(Integer x2: hashes2){
                if(i==x2) {System.out.print(" "+ i + " is in hashes0 and hashes2");}
            }
        }
        for(Integer i: hashes1){
            for(Integer x2: hashes2){
                if(i==x2) {System.out.print(" "+ i + " is in hashes1 and hashes2");}
            }
        }
*/
        this.sessionFactory.close();}
    /**
     * This method takes an entity and returns the most similar Entity by comparing it to all entities of its class in the database.
     * null is returned if the threshhold of @value #threshhold
     * @param entity
     * @return
     * @throws IllegalAccessException
     */
    public Entity findSimilar(Entity entity, int[] hashValues) throws IllegalAccessException {
        final double threshhold = (entity.getClass().equals(Work.class))?Config.SIMILARITY_THRESHHOLD: Config.SIMILARITY_THRESHHOLD_AUTHOR;
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
        return (currentBestScore >= threshhold) ? closestMatch : null;
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
    public void addHashesFromEntity(Entity entity, int[] hashValues){
        for(Integer hashValue: hashValues){
            createOrUpdateHashObject(hashValue, entity);
        }
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

    /**
     * This method merges a new Work with one that already exists after they are determined to be the same work.
     * Every field that is set in the new Work that isn't set int the existing one or is set to a default value is set in the existing work.
     * @param newWork
     * @param existingWork
     * @return existing Work with all the values that the new Work has set and the existing one didn't
     */
    public Work mergeAndUpdate(Work newWork, Work existingWork){
        if(existingWork.getPublicationYear() == -1 && newWork.getPublicationYear() != -1){
            existingWork.setPublicationYear(newWork.getPublicationYear()); }
        if(existingWork.getPublicationMonth() == -1 && newWork.getPublicationMonth() != -1){
            existingWork.setPublicationMonth(newWork.getPublicationMonth()); }
        if(existingWork.getPublicationDay() == -1 && newWork.getPublicationDay() != -1){
            existingWork.setPublicationDay(newWork.getPublicationDay()); }
        for(String newSourceFile: newWork.getSourcefiles()){ //Adding all sourcefiles from the newWork to the existing one.
            if(!existingWork.getSourcefiles().contains(newSourceFile)){ existingWork.addSourcefile(newSourceFile); }
        }
        if((existingWork.getDoi() == null || existingWork.getDoi().isBlank()) && (newWork.getDoi() != null && !newWork.getDoi().isBlank())){
            existingWork.setDoi(newWork.getDoi());}
        for(Author newAuthor: newWork.getAuthors()){ //Adding all authors from the newWork to the existing one.
            if(!existingWork.getAuthors().contains(newAuthor)){ existingWork.addAuthor(newAuthor); }
        }
        for(Work newCitation: newWork.getCitations()){//Adding all citations from the newWork to the existing one.
            if(!existingWork.getCitations().contains(newCitation)){ existingWork.addCitation(newCitation);}
        }
            return existingWork;
    }

    /**
     * This method merges a new Author into one that already exists after they are determined to be the same work.
     * Every field that is set in the new Author that isn't set int the existing one or is set to a default value is set in the existing Author.
     * @param newAuthor
     * @param existingAuthor
     * @return
     */
    public Author mergeAndUpdate(Author newAuthor, Author existingAuthor){
        if((existingAuthor.getTitle() == null || existingAuthor.getTitle().isBlank()) && (newAuthor.getTitle() != null && !newAuthor.getTitle().isBlank())){
            existingAuthor.setTitle(newAuthor.getTitle()); }
        if((existingAuthor.getFirstName() == null || existingAuthor.getFirstName().isBlank()) && (newAuthor.getFirstName() != null && !newAuthor.getFirstName().isBlank())){
            existingAuthor.setFirstName(newAuthor.getFirstName()); }
        if((existingAuthor.getLastName() == null || existingAuthor.getLastName().isBlank()) && (newAuthor.getLastName() != null && !newAuthor.getLastName().isBlank())){
            existingAuthor.setLastName(newAuthor.getLastName()); }
        if((existingAuthor.getMiddleName() == null || existingAuthor.getMiddleName().isBlank()) && (newAuthor.getMiddleName() != null && !newAuthor.getMiddleName().isBlank())){
            existingAuthor.setMiddleName(newAuthor.getMiddleName()); }
        if((existingAuthor.getEmail() == null || existingAuthor.getEmail().isBlank()) && (newAuthor.getEmail() != null && !newAuthor.getEmail().isBlank())){
            existingAuthor.setEmail(newAuthor.getEmail()); }
        existingAuthor.getCreatedWorks().addAll(newAuthor.getCreatedWorks());
        return existingAuthor;
    }
    public Hashing getHashingHandler(){ return this.hashingHandler; }
}
