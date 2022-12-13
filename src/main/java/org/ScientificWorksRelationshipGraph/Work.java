package org.ScientificWorksRelationshipGraph;

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
    @Transient
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

    /*@Property
    private String fullGrobidDataString;*/

    @Relationship(type="AUTHORED", direction = Relationship.INCOMING)
    private List<Author> authors;
    /*@Relationship ("PUBLISHED_AT")
    private List<Organization> affiliatedOrganisations;

     */
    @Relationship("CITES")
    private List<Work> citations;

    public Work(){
        this.title = null;
        this.authors = new ArrayList<>();
        //this.affiliatedOrganisations = new ArrayList<>();
        this.citations = new ArrayList<>();
    }

    public Work(BiblioItem bibItem, Neo4jHandler handler)throws IllegalAccessException{
        this.title = bibItem.getTitle();
        //Adding Authors to the work
        this.authors = new ArrayList<>();
        List<Person> authorsToProcess = bibItem.getFullAuthors();
        Author currentAuthor;
        if(authorsToProcess!= null){
            for (Person person : authorsToProcess) {
                currentAuthor = Author.CreateUniqueAuthor(person, handler);
                currentAuthor.addCreatedWork(this);
                addAuthor(currentAuthor);
            }
        }
        this.publicationDate = bibItem.getNormalizedPublicationDate();
        this.citations = new ArrayList<Work>();
        /*Adding affiliated Organizations to the work
        this.affiliatedOrganisations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess = bibItem.getFullAffiliations();
        if(affiliationsToProcess != null){
            for (Affiliation currentAffiliation: affiliationsToProcess) {
                addAffiliatedOrganization(new Organization(currentAffiliation));
            }
        }
        this.fullGrobidDataString = bibItem.toString().replaceAll("\\w*='*null'*,*","");
        */
    }

    public static Work createUniqueWork(BiblioItem bibItem, Neo4jHandler handler)throws IllegalAccessException{
        Work work = new Work(bibItem, handler);
        Work alias = (Work) handler.findSimilar(work);
        return (alias == null) ? work : alias;
    }
    public static Work createUniqueWork(BiblioItem bibItem, Neo4jHandler handler)throws IllegalAccessException{
        Work work = new Work(bibItem, handler);
        Work alias = (Work) handler.findSimilar(work, inDatabase);
        return (alias == null) ? work : alias;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public int getPublicationYear() { return publicationYear; }

    public int getPublicationMonth() { return publicationMonth; }

    public int getPublicationDay() { return publicationDay; }

    public List<Author> getAuthors() { return authors; }

    public void setAuthors(List<Author> authors) { this.authors = authors; }

    public void addAuthor(Author author){ this.authors.add(author); }

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

    /*public String getFullGrobidDataString() { return fullGrobidDataString; }

    public void setFullGrobidDataString(String fullGrobidDataString) { this.fullGrobidDataString = fullGrobidDataString; }

    /*public List<Organization> getAffiliatedOrganisations() { return affiliatedOrganisations; }

    public void setAffiliatedOrganisations(List<Organization> affiliatedOrganisations) { this.affiliatedOrganisations = affiliatedOrganisations; }

    public void addAffiliatedOrganization(Organization org){ this.affiliatedOrganisations.add(org); }
     */

    public double compareTo(Work other){
        if(this.equals(other)){return 1;}
        double similarity = 6*Distances.weightedDamerauLevenshteinSimilarity(this.title, other.getTitle());
        similarity += 3*Distances.compareAuthors(this.authors, other.getAuthors());
        if(this.publicationDate != null && other.getPublicationDate() != null){
            similarity += (this.publicationDate.compareTo(other.getPublicationDate()) == 0) ? 1 : 0;
        } else { similarity += (this.publicationDate == null || other.getPublicationDate() == null) ? 0 : 1;}
        return similarity / 10;
    }

    @Override
    public String toString() {
        return "Work{" +
                "title='" + title + '\'' +
                ", publicationDate=" + publicationDate +
                ", publicationYear=" + publicationYear +
                ", publicationMonth=" + publicationMonth +
                ", publicationDay=" + publicationDay +
                //", fullGrobidDataString='" + fullGrobidDataString + '\'' +
                ", authors=" + authors +
                //", affiliatedOrganisations=" + affiliatedOrganisations +
                ", citations=" + citations +
                '}';
    }
}
