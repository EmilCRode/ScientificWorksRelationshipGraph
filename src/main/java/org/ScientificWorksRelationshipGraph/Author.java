package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Relationship(type="AUTHORED")
    private Set<Work> createdWorks;
    /*@Relationship(type="AFFILIATED", direction=Relationship.UNDIRECTED)
    private List<Organization> affiliatedOrganizations;*/
    public Author(){}
    public Author(Person person){
        this.title = person.getTitle();
        if(person.getFirstName()!= null) this.firstName = person.getFirstName().replaceAll("\\b(et|Et)\\b","");
        if(person.getMiddleName() != null) this.middleName = person.getMiddleName().replaceAll("\\b(et|Et)\\b","");
        if(person.getLastName() != null) this.lastName = person.getLastName().replaceAll("\\b(et|Et)\\b","");
        this.email = person.getEmail();
        this.createdWorks = new HashSet<>();
        /*this.affiliatedOrganizations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess= person.getAffiliations();
        if(affiliationsToProcess != null) {
            for (Affiliation affiliation : affiliationsToProcess) {
                this.affiliatedOrganizations.add(new Organization(affiliation));
            }
        }*/
    }
    public static Author CreateUniqueAuthor(Person person, Neo4jHandler neo4jHandler)throws IllegalAccessException{
        Author newAuthor = new Author(person);
        if(newAuthor.firstName == null || newAuthor.lastName == null){return null;}
        if(newAuthor.firstName.isBlank()|| newAuthor.lastName.isBlank()){return null;}
        int[] hashValues = neo4jHandler.getHashingHandler().generateLSHHashValues(newAuthor.compareString());
        Author existingAuthor = (Author) neo4jHandler.findSimilar(newAuthor, hashValues);
        if(existingAuthor == null){
            neo4jHandler.addHashesFromEntity(newAuthor, hashValues);
            return newAuthor;
        }
        return neo4jHandler.mergeAndUpdate(newAuthor, existingAuthor);
    }
    public String getFirstName(){ return firstName; }
    public void setFirstName(String firstName){ this.firstName = firstName; }
    public String getMiddleName(){ return middleName; }
    public void setMiddleName(String middleName){ this.middleName = middleName; }
    public String getLastName(){ return lastName; }
    public void setLastName(String lastName){ this.lastName = lastName;}
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<Work> getCreatedWorks(){ return createdWorks; }
    public void setCreatedWorks(Set<Work> createdWorks){ this.createdWorks = createdWorks; }
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
        double similarity = Distances.weightedDamerauLevenshteinSimilarity(this.firstName, other.getFirstName());
        similarity += Distances.weightedDamerauLevenshteinSimilarity(this.middleName, other.getMiddleName());
        similarity += Distances.weightedDamerauLevenshteinSimilarity(this.lastName, other.getLastName());
        similarity += Distances.weightedDamerauLevenshteinSimilarity(this.title, other.getTitle());
        return similarity / 4;
    }
    @Override
    public String compareString(){
        return this.title +
                this.firstName +
                this.middleName +
                this.lastName +
                this.email;
    }
}
