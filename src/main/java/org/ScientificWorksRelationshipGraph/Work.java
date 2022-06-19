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
    private List<Person> authors;
    @Relationship ("PUBLISHED_AT")
    private List<Organization> organisations;
    @Relationship("CITES")
    private List<Work> citations;

    public Work(){
        this.title = null;
        this.citations = new ArrayList<Work>();
    }

    public Work(BiblioItem bibItem){
        String title = bibItem.getTitle();
        List<Person> authors = bibItem.getFullAuthors();

        this.title = title;
        this.authors = authors;

        List<Organization> orgs = new ArrayList<>();
        List<Affiliation> affiliations = bibItem.getFullAffiliations();
        for (Affiliation currentAffiliation: affiliations) {
            orgs.add(new Organization(currentAffiliation));
        }
        this.organisations = orgs;

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

    public List<Person> getAuthors() { return authors; }

    public void setAuthors(List<Person> authors) { this.authors = authors; }

    public List<Organization> getOrganisations() { return organisations; }

    public void setOrganisations(List<Organization> organisations) { this.organisations = organisations; }

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
                ", organisations=" + organisations +
                ", citations=" + citations +
                '}';
    }
}
