package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

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
    private Work[] createdWorks;
    @Relationship(type="AFFILIATED", direction=Relationship.UNDIRECTED)
    private Organization[] affiliatedOrganizations;
    public Author(){
    }

    public Author(Person person){
        this.title = person.getTitle();
        this.firstName = person.getFirstName();
        this.middleName = person.getMiddleName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
        person.getAffiliations().get(0).
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

    public Work[] getCreatedWorks() {
        return createdWorks;
    }

    public void setCreatedWorks(Work[] createdWorks) {
        this.createdWorks = createdWorks;
    }

    public Organization[] getAffiliatedOrganizations() {
        return affiliatedOrganizations;
    }

    public void setAffiliatedOrganizations(Organization[] affiliatedOrganizations) {
        this.affiliatedOrganizations = affiliatedOrganizations;
    }
}
