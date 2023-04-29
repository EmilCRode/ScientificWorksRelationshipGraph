package org.ScientificWorksRelationshipGraph;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;


abstract class Entity {

    @Id @GeneratedValue
    private Long id;

    Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || id==null  ||getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return (id == null) ? -1 : id.hashCode();
    }

    public String compareString(){
        return null;
    }
}
