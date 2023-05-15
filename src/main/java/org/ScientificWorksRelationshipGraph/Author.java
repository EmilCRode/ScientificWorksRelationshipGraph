package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Author extends Entity{
    @Property("title")
    private String title;
    @Property("lastname")
    private String lastName;
    @Property("firstname")
    private String firstName;
    @Property("middlename")
    private String middleName;
    @Property("email")
    private String email;
    @Property("orcid")
    private String orcid;
    @Relationship(type="AUTHORED")
    private List<Work> createdWorks;
    /*@Relationship(type="AFFILIATED", direction=Relationship.UNDIRECTED)
    private List<Organization> affiliatedOrganizations;*/
    public Author(){}
    public Author(String title, String firstName, String middleName, String lastName, String email, String orcid){
        this.title = (title == null) ? null : title.replaceAll("[^a-zA-Z ]", "");
        this.firstName = firstName.replaceAll("\\b(et|Et)\\b","");
        this.middleName = (middleName == null) ? null : middleName.replaceAll("\\b(et|Et)\\b","");
        this.lastName = lastName.replaceAll("\\b(et|Et)\\b","");
        this.email = email;
        this.orcid = orcid;
        this.createdWorks = new ArrayList<>();
        /*this.affiliatedOrganizations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess= person.getAffiliations();
        if(affiliationsToProcess != null) {
            for (Affiliation affiliation : affiliationsToProcess) {
                this.affiliatedOrganizations.add(new Organization(affiliation));
            }
        }*/
    }
    public static Author CreateUniqueAuthor(Person person, Neo4jHandler neo4jHandler)throws IllegalAccessException{
        String firstName = (person.getFirstName() == null) ? null : person.getFirstName().replaceAll("[^a-zA-Z ]","").trim();
        String middleName = (person.getMiddleName() == null) ? null : person.getMiddleName().replaceAll("[^a-zA-Z ]","").trim();
        String lastName = (person.getLastName() == null) ? null : person.getLastName().replaceAll("[^a-zA-Z ]","").trim();
        if(firstName == null || lastName == null){return null;}
        if(firstName.isBlank()|| lastName.isBlank()){return null;}
        Author newAuthor = new Author(person.getTitle(),firstName,middleName,lastName, person.getEmail(), person.getORCID());
        //Searching for duplicates
        int[] hashValues = neo4jHandler.getHashingHandler().generateLSHHashValues(newAuthor.compareString());
        Author existingAuthor = (Author) neo4jHandler.findSimilar(newAuthor, hashValues);
        if(existingAuthor == null){
            neo4jHandler.addHashesFromEntity(newAuthor, hashValues);
            return newAuthor;
        }
        return existingAuthor.mergeAndUpdate(newAuthor);
    }
    /**
     * This method merges a new Author into one that already exists after they are determined to be the same work.
     * Every field that is set in the new Author that isn't set int the existing one or is set to a default value is set in the existing Author.
     * @param newAuthor
     * @return
     */
    public Author mergeAndUpdate(Author newAuthor){
        if((this.getTitle() == null || this.getTitle().isBlank()) && (newAuthor.getTitle() != null && !newAuthor.getTitle().isBlank())){
            this.setTitle(newAuthor.getTitle());
        }
        if((this.firstName == null || this.firstName.isBlank()) && (newAuthor.firstName != null && !newAuthor.firstName.isBlank())){
            this.setFirstName(newAuthor.firstName);
        } else if(newAuthor.firstName != null && newAuthor.firstName.contains(this.firstName)){
            //Update Abbreviation with Full String
            this.firstName= newAuthor.firstName;
        }
        if((this.lastName == null || this.lastName.isBlank()) && (newAuthor.lastName != null && !newAuthor.lastName.isBlank())){
            this.setLastName(newAuthor.getLastName());
        } else if(newAuthor.lastName != null && newAuthor.lastName.contains(this.lastName)){
            //Update Abbreviation with Full String
            this.lastName = newAuthor.lastName;
        }
        if((this.middleName == null || this.middleName.isBlank()) && (newAuthor.middleName != null && !newAuthor.middleName.isBlank())){
            this.setMiddleName(newAuthor.middleName);
        } else if(newAuthor.middleName != null && newAuthor.middleName.contains(this.middleName)){
        //Update Abbreviation with Full String
        this.middleName = newAuthor.middleName;
        }
        if((this.email == null || this.email.isBlank()) && (newAuthor.email != null && !newAuthor.email.isBlank())){
            this.email =newAuthor.email;
        }
        if((this.orcid == null || this.orcid.isBlank()) && (newAuthor.orcid != null && !newAuthor.orcid.isBlank())){
            this.orcid = newAuthor.orcid;
        }
        for(Work newCreatedWork: newAuthor.getCreatedWorks()){
            if(!this.getCreatedWorks().contains(newCreatedWork)){
                this.addCreatedWork(newCreatedWork);
                newCreatedWork.getAuthors().remove(newAuthor);
            }
        }
        this.getCreatedWorks().addAll(newAuthor.getCreatedWorks());
        return this;
    }
    public String getFirstName(){ return firstName; }
    public void setFirstName(String firstName){ this.firstName = firstName; }
    public String middleName(){ return middleName; }
    public void setMiddleName(String middleName){ this.middleName = middleName; }
    public String getLastName(){ return lastName; }
    public void setLastName(String lastName){ this.lastName = lastName;}
    public String email() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Work> getCreatedWorks(){ return createdWorks; }
    public void setCreatedWorks(List<Work> createdWorks){ this.createdWorks = createdWorks; }
    public void addCreatedWork(Work work){ this.createdWorks.add(work); }
    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title = title; }
    /*public List<Organization> getAffiliatedOrganizations() {
        return affiliatedOrganizations;
    }
    public void setAffiliatedOrganizations(List<Organization> affiliatedOrganizations) {
        this.affiliatedOrganizations = affiliatedOrganizations;
    }*/
    @Override
    public String toString(){
        return "Author{" +
                "Title: '" + title + '\'' +
                ", FirstName: " + this.firstName +
                ", MiddleName: " + this.middleName +
                ", LastName: " + this.lastName +
                ", e-Mail: " + this.email +
                " }";
    }
    public double compareTo(Author other){
        if(this.equals(other)){ return 1; }
        int divisor = 0;
        double similarity = 0;
        double currentDistance;
        currentDistance = Distances.weightedDamerauLevenshteinSimilaritySubstring(this.firstName, other.getFirstName(),divisor);
        if(currentDistance != -1){
            similarity += currentDistance;
            divisor ++;
        }
        currentDistance = Distances.weightedDamerauLevenshteinSimilaritySubstring(this.middleName, other.middleName(),divisor);
        if(currentDistance != -1){
            similarity += currentDistance;
            divisor ++;
        }
        currentDistance = Distances.weightedDamerauLevenshteinSimilaritySubstring(this.lastName, other.getLastName(),divisor);
        if(currentDistance != -1){
            similarity += currentDistance;
            divisor ++;
        }
        if(this.orcid != null && !this.orcid.isBlank() && other.orcid != null && !other.orcid.isBlank() && this.orcid.equals(other.orcid)){
            similarity += 7;
            divisor += 7;
        }
        return (divisor == 0)? 0 : similarity / divisor;
    }
    @Override
    public String compareString(){
        return (this.title +
                this.firstName +
                this.middleName +
                this.lastName +
                this.email).toLowerCase();
    }
}
