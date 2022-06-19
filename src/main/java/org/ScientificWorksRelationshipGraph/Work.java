package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.List;

@NodeEntity
public class Work extends Entity{
    @Property
    private String title;
    @Property
    private Date publicationDate;
    /**
     * This Attribute is set by setting the publicationDate
     */
    @Property
    private int publicationYear;
    /**
     * This Attribute is set by setting the publicationDate
     */
    @Property
    private int publicationMonth;
    /**
     * This Attribute is set by setting the publicationDate
     */
    @Property
    private int publicationDay;
    @Property
    private double confidence = 1.0;
    @Relationship(type="AUTHORED", direction = Relationship.INCOMING)
    private List<Author> authors;
    @Relationship ("PUBLISHED_AT")
    private List<Organization> affiliatedOrganisations;
    @Relationship("CITES")
    private List<Work> citations;

    public Work(){
        this.title = null;
        this.authors = new ArrayList<>();
        this.affiliatedOrganisations = new ArrayList<>();
        this.citations = new ArrayList<>();
    }

    public Work(BiblioItem bibItem){
        this.title = bibItem.getTitle();
        //Adding Authors to the work
        this.authors = new ArrayList<>();
        //needs null check
        for(Person person: bibItem.getFullAuthors()){
            addAuthor(new Author(person));
        }
        //Adding affiliated Organizations to the work
        //needs null check
        this.affiliatedOrganisations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess = bibItem.getFullAffiliations();
        for (Affiliation currentAffiliation: affiliationsToProcess) {
            addAffiliatedOrganization(new Organization(currentAffiliation));
        }


        this.publicationDate = bibItem.getNormalizedPublicationDate();
        this.citations = new ArrayList<Work>();
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public int getPublicationYear() { return publicationYear; }

    public int getPublicationMonth() { return publicationMonth; }

    public int getPublicationDay() { return publicationDay; }

    public void setConfidence(double confidence) { this.confidence = confidence; }

    public double getConfidence() { return confidence; }

    public List<Author> getAuthors() { return authors; }

    public void setAuthors(List<Author> authors) { this.authors = authors; }

    public void addAuthor(Author author){ this.authors.add(author); }

    public List<Organization> getAffiliatedOrganisations() { return affiliatedOrganisations; }

    public void setAffiliatedOrganisations(List<Organization> affiliatedOrganisations) { this.affiliatedOrganisations = affiliatedOrganisations; }

    public void addAffiliatedOrganization(Organization org){ this.affiliatedOrganisations.add(org); }

    public List<Work> getCitations() { return citations; }

    public void setCitations(List<Work> citations) { this.citations = citations; }

    public void addCitation(Work citedWork){ this.citations.add(citedWork); }

    public Date getPublicationDate() { return publicationDate; }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
        if(this.publicationDate.isNotNull()) {
            this.publicationYear = publicationDate.getYear();
            this.publicationMonth = publicationDate.getMonth();
            this.publicationDay = publicationDate.getDay();
        }
    }

    @Override
    public String toString() {
        return "Work{" +
                "title='" + title + '\'' +
                ", publicationDate=" + publicationDate +
                ", publicationYear=" + publicationYear +
                ", publicationMonth=" + publicationMonth +
                ", publicationDay=" + publicationDay +
                ", confidence=" + confidence +
                ", authors=" + authors +
                ", organisations=" + affiliatedOrganisations +
                ", citations=" + citations +
                '}';
    }
}
