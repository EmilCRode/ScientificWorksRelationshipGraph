package org.ScientificWorksRelationshipGraph;

import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Organization {
    String name;
    @Relationship(type="PUBLISHED_AT", direction=Relationship.INCOMING)
    Work[] works;
    public Organization(){

    }
}
