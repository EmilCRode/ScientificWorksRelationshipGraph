package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.id.UuidStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.wipo.analyzers.wipokr.utils.StringUtil;

@NodeEntity
public class Work extends Entity{
    @Property
    private String title;
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
    private String discipline;

    @Property
    private String journal;

    @Property
    private ArrayList<String> sourcefiles;

    @Property
    private String fullGrobidDataString;

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
        this.sourcefiles = new ArrayList<>();
    }
    public Work(String sourcefile){
        this.title = null;
        this.authors = new ArrayList<>();
        //this.affiliatedOrganisations = new ArrayList<>();
        this.citations = new ArrayList<>();
        this.sourcefiles = new ArrayList<>();
        this.sourcefiles.add(sourcefile);
    }

    public Work(BiblioItem bibItem, Neo4jHandler handler, String sourcefile)throws IllegalAccessException{
        this.title = bibItem.getTitle();
        if(this.title == null || this.title.isBlank()){
            String bookTitle = bibItem.getBookTitle();
            if(!StringUtils.isEmpty(bookTitle) || !StringUtils.isEmpty(bibItem.getArticleTitle())) this.title = (StringUtils.isEmpty(bookTitle))? bibItem.getArticleTitle(): bookTitle;
        }
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
        this.citations = new ArrayList<Work>();
        /*Adding affiliated Organizations to the work
        this.affiliatedOrganisations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess = bibItem.getFullAffiliations();
        if(affiliationsToProcess != null){
            for (Affiliation currentAffiliation: affiliationsToProcess) {
                addAffiliatedOrganization(new Organization(currentAffiliation));
            }
        }*/
        this.sourcefiles = new ArrayList<>();
        this.sourcefiles.add(sourcefile);
        this.fullGrobidDataString = bibItem.toString().replaceAll("\\w*='*null'*,*","");
        Date grobidDate = bibItem.getNormalizedPublicationDate();
        if(grobidDate != null) {
            this.publicationYear = grobidDate.getYear();
            this.publicationMonth = grobidDate.getMonth();
            this.publicationDay = grobidDate.getDay();
        }
    }


    public static Work createUniqueWork(BiblioItem bibItem, Neo4jHandler handler, String sourcefile)throws IllegalAccessException{
        Work work = new Work(bibItem, handler, sourcefile);
        if(StringUtils.isEmpty(work.title)) return null;
        Work alias = (Work) handler.findSimilar(work);
        if(alias == null){
            handler.getWorksInDatabase().add(work);
            return work;
        }
        //System.out.println("found alias: "+ alias.toString() + "\nfor: " + work.toString());
        return alias;
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

    /*public java.util.Date getPublicationDate() { return publicationDate; }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
        if(this.publicationDate != null) {
            this.publicationYear = publicationDate.getYear();
            this.publicationMonth = publicationDate.getMonth();
            this.publicationDay = publicationDate.getDay();
        }
    }*/

    public String getDiscipline() { return discipline; }

    public void setDiscipline(String discipline) { this.discipline = discipline; }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getFullGrobidDataString() { return fullGrobidDataString; }

    public ArrayList<String> getSourcefiles() {
        return sourcefiles;
    }

    public void setSourcefiles(ArrayList<String> sourcefiles) {
        this.sourcefiles = sourcefiles;
    }

    public void setFullGrobidDataString(String fullGrobidDataString) { this.fullGrobidDataString = fullGrobidDataString; }

    /*public List<Organization> getAffiliatedOrganisations() { return affiliatedOrganisations; }

    public void setAffiliatedOrganisations(List<Organization> affiliatedOrganisations) { this.affiliatedOrganisations = affiliatedOrganisations; }

    public void addAffiliatedOrganization(Organization org){ this.affiliatedOrganisations.add(org); }
     */

    public double compareTo(Work other){
        if(this.equals(other)){
            System.out.println("Work: " +this.title+ " matched itself");
            return 1;}
        if(this == null || other == null){
            System.out.println("null compared");
            return 0.0;
        }
        double similarity = 6*Distances.weightedDamerauLevenshteinSimilarity(this.title, other.getTitle());
        similarity += 3*Distances.compareAuthors(this.authors, other.getAuthors());
        /*if(this.publicationDate != null && other.getPublicationDate() != null){
            similarity += (this.publicationDate.compareTo(other.getPublicationDate()) == 0) ? 1 : 0;
        } else { similarity += (this.publicationDate == null || other.getPublicationDate() == null) ? 0 : 1;}*/
        //if(Double.isNaN(similarity)) System.out.println("this: " + this.toString() + "\nother: " + other.toString() + "\n\nSimilarity: " + similarity / 10);
        if(other.title!= null && this.title != null) {
            if (this.title.contains("Traduit par Syl") && other.title.contains("Traduit par Syl")){
                System.out.println("this: " + this.toString() + "\nother: " + other.toString() + "\n\nSimilarity: " + similarity / 10);}
        }
        return similarity / 10;
    }

    @Override
    public String toString() {
        return "Work{" +
                "title='" + title + '\'' +
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
