package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Affiliation;
import org.neo4j.ogm.annotation.*;

/**
 * Organization is the class which represents an Organization at which a Work is published
 * or with which an author or work is affiliated. It is populated with data from the Affiliation
 * Object found in {@link org.grobid.core.data.Affiliation} documented in
 * <a href="https://grobid.github.io/grobid-core/index.html">JavaDoc for Grobid</a> which is passed into the Organization constructor
 *
 */
@NodeEntity
public class Organization extends Entity{
    @Property("name")
    private String name;
    /**
     * This field is directly lifted from {@link org.grobid.core.data.Affiliation} where it is not documented.
     * The assumption is that it aggregates the attributed of the Affiliation objects in one string.
     * This assumption should be tested through data exploration.
     */
    @Property
    private String affiliationString;
    /**
     * This field is the most specific location given.
     * it is populated in this priority: Settlement > Region > Country.
     * If a lower level location is missing the higher one is used.
     */
    @Property("country")
    private String country;
    @Property("region")
    private String region;
    @Property("settlement")
    private String settlement;
    @Property("location")
    private String location;
    @Property("acronym")
    private String acronym;

    @Relationship(type="PUBLISHED_AT", direction=Relationship.INCOMING)
    Work[] works;

    public Organization(){}
    public Organization(Affiliation affiliation){
        this.name = affiliation.getName();
        this.affiliationString = affiliation.getAffiliationString();
        this.country = affiliation.getCountry();
        this.region = affiliation.getRegion();
        this.settlement = affiliation.getSettlement();
        this.acronym = affiliation.getAcronym();

        //location Assignment
        this.location = country;
        if(region != null && region != ""){ this.location = region; }
        if(settlement != null && settlement != ""){ this.location = settlement; }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliationString() {
        return affiliationString;
    }

    public void setAffiliationString(String affiliationString) {
        this.affiliationString = affiliationString;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public Work[] getWorks() {
        return works;
    }

    public void setWorks(Work[] works) {
        this.works = works;
    }
}
