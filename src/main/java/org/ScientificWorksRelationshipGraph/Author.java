package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Affiliation;
import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Author extends Entity{
    @Property("title")
    private String title;
    @Property("firstname")
    private String firstName;
    @Property("middlename")
    private String middleName;
    @Property("lastname")
    private String lastName;
    @Property("email")
    private String email;
    @Relationship(type="CREATED", direction=Relationship.OUTGOING)
    private List<Work> createdWorks;
    @Relationship(type="AFFILIATED", direction=Relationship.UNDIRECTED)
    private List<Organization> affiliatedOrganizations;
    public Author(){
    }

    public Author(Person person){
        this.title = person.getTitle();
        this.firstName = person.getFirstName();
        this.middleName = person.getMiddleName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
        this.affiliatedOrganizations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess= person.getAffiliations();
        if(affiliationsToProcess != null) {
            for (Affiliation affiliation : affiliationsToProcess) {
                this.affiliatedOrganizations.add(new Organization(affiliation));
            }
        }
        this.createdWorks = new ArrayList<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() { return middleName; }

    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Work> getCreatedWorks() {
        return createdWorks;
    }

    public void setCreatedWorks(List<Work> createdWorks) {
        this.createdWorks = createdWorks;
    }

    public void addCreatedWork(Work work){ this.createdWorks.add(work); }

    public List<Organization> getAffiliatedOrganizations() {
        return affiliatedOrganizations;
    }

    public void setAffiliatedOrganizations(List<Organization> affiliatedOrganizations) {
        this.affiliatedOrganizations = affiliatedOrganizations;
    }
}
