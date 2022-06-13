package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.*;

import java.util.List;

@NodeEntity
public class Work extends Entity{
    @Property
    private String title;
    @Property
    private String publicationDate;
    @Property
    private int year;
    @Property
    private short month;
    @Relationship(type="AUTHORED", direction = Relationship.INCOMING)
    private List<Person> authors;
    @Relationship ("PUBLISHED_AT")
    private Organization organisation;
    @Relationship("CITES")
    private Work[] works;

    public Work( String title, List<Person> authors) {
        if(title.isBlank()){System.err.println("No title found for work");}
        this.title = title;
        this.authors = authors;

    }
    public Work(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public short getMonth() {
        return month;
    }

    public void setMonth(short month) {
        this.month = month;
    }

    public List<Person> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Person> authors) {
        this.authors = authors;
    }

    public Organization getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organization organisation) {
        this.organisation = organisation;
    }

    public Work[] getWorks() {
        return works;
    }

    public void setWorks(Work[] works) {
        this.works = works;
    }

    public String getPublicationDate() {return publicationDate;}

    public void setPublicationDate(String publicationDate) {this.publicationDate = publicationDate;}

    @Override
    public String toString(){
        return "Work: title: "+ this.title + ", id: " + this.getId() + ", authors: " + this.authors + " publicationDate: " + this.publicationDate;
    }
}
