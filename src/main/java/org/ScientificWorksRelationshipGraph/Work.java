package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.neo4j.ogm.annotation.*;

import java.util.*;
import org.apache.commons.lang3.StringUtils;

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
    private Set<String> sourcefiles;
    @Property
    private String fullGrobidDataString;
    @Property
    private String doi;
    @Relationship(type="AUTHORED", direction = Relationship.INCOMING)
    private List<Author> authors;
    /*@Relationship ("PUBLISHED_AT")
    private List<Organization> affiliatedOrganisations;
    */
    @Relationship("CITES")
    private List<Work> citations;
    public Work(){
        this.title = title;
        this.authors = new ArrayList<>();
        this.citations = new ArrayList<>();
        this.sourcefiles = new HashSet<>();
        //this.affiliatedOrganisations = new ArrayList<>();
    }
    public Work(BiblioItem bibItem, Neo4jHandler neo4jHandler, String sourcefile)throws IllegalAccessException{
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
                currentAuthor = Author.CreateUniqueAuthor(person, neo4jHandler);
                if(currentAuthor != null) {
                    currentAuthor.addCreatedWork(this);
                    addAuthor(currentAuthor);
                }
            }
        }
        this.citations = new ArrayList<>();
        this.sourcefiles = new HashSet<>();
        this.sourcefiles.add(sourcefile);
        //this.fullGrobidDataString = bibItem.toString().replaceAll("\\w*='*null'*,*","");
        Date grobidDate = bibItem.getNormalizedPublicationDate();
        if(grobidDate != null) {
            this.publicationYear = grobidDate.getYear();
            this.publicationMonth = grobidDate.getMonth();
            this.publicationDay = grobidDate.getDay();
        }
        this.doi = bibItem.getDOI();
        /*Adding affiliated Organizations to the work
        this.affiliatedOrganisations = new ArrayList<>();
        List<Affiliation> affiliationsToProcess = bibItem.getFullAffiliations();
        if(affiliationsToProcess != null){
            for (Affiliation currentAffiliation: affiliationsToProcess) {
                addAffiliatedOrganization(new Organization(currentAffiliation));
            }
        }*/
    }
    public static Work createUniqueWork(BiblioItem bibItem, Neo4jHandler neo4jHandler, String sourcefile)throws IllegalAccessException{
        if(StringUtils.isBlank(bibItem.getTitle())){ return null; }
        Work newWork = new Work(bibItem, neo4jHandler, sourcefile);
        int[] hashValues = neo4jHandler.getHashingHandler().generateLSHHashValues(newWork.compareString());
        Work existingWork = (Work) neo4jHandler.findSimilar(newWork, hashValues);
        if(existingWork == null){
            neo4jHandler.addHashesFromEntity(newWork, hashValues);
            return newWork;
        }
        return neo4jHandler.mergeAndUpdate(newWork, existingWork); //returns null if there is an existingWork aka a duplicate NEEDS_TEST
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear){ this.publicationYear = publicationYear; }
    public int getPublicationMonth() { return publicationMonth; }
    public void setPublicationMonth(int publicationMonth) { this.publicationMonth = publicationMonth; }
    public void setPublicationDay(int publicationDay) { this.publicationDay = publicationDay; }
    public int getPublicationDay() { return publicationDay; }
    public List<Author> getAuthors() { return authors; }
    public void addAuthor(Author author){ this.authors.add(author); }
    public List<Work> getCitations() { return citations; }
    public void addCitation(Work citedWork){ this.citations.add(citedWork); }
    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }
    public String getJournal() {
        return journal;
    }
    public void setJournal(String journal) {
        this.journal = journal;
    }
    public String getFullGrobidDataString() { return fullGrobidDataString; }
    public Set<String> getSourcefiles() {
        return sourcefiles;
    }
    public void addSourcefile(String newSourceFile){ this.sourcefiles.add(newSourceFile); }
    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
    /*public List<Organization> getAffiliatedOrganisations() { return affiliatedOrganisations; }

    public void setAffiliatedOrganisations(List<Organization> affiliatedOrganisations) { this.affiliatedOrganisations = affiliatedOrganisations; }

    public void addAffiliatedOrganization(Organization org){ this.affiliatedOrganisations.add(org); }
     */
    public double compareTo(Work other){
        int similarityDivident = 10;
        if(this.equals(other)){
            System.out.println("Work: " +this.title+ " matched itself");
            return 1;}
        if(this == null || other == null){
            System.out.println("null compared");
            return 0.0;
        }
        double similarity = 9*Distances.weightedDamerauLevenshteinSimilarity(this.title, other.getTitle());
        similarity += 1*Distances.compareAuthors(this.authors, other.getAuthors());

        if(this.doi != null && other.doi != null){
            similarity += (this.doi.equals(other.doi))? 10 : 0;
            similarityDivident += 10;
        }
        /*if(this.publicationDate != null && other.getPublicationDate() != null){
            similarity += (this.publicationDate.compareTo(other.getPublicationDate()) == 0) ? 1 : 0;
        } else { similarity += (this.publicationDate == null || other.getPublicationDate() == null) ? 0 : 1;}*/
        return similarity / similarityDivident;
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
    @Override
    public String compareString(){
        return this.title;
    }
}
