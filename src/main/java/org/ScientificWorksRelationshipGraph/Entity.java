package org.ScientificWorksRelationshipGraph;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.id.UuidStrategy;

import java.util.UUID;

abstract class Entity {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private UUID id;

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || id == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (!id.equals(entity.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (id == null) ? -1 : id.hashCode();
    }
}
