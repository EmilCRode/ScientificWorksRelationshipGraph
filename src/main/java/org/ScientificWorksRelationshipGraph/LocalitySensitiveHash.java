package org.ScientificWorksRelationshipGraph;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * This Class represents a Hash obtained by hashing a band of signatures using String concatination.
 * it holds the hash value which can be used to lookup Entities in whose signature a band hashes to this.
 * it also holds the parameters used in hashing the bands and the underlying signatures.
 */
@NodeEntity
public class LocalitySensitiveHash extends Entity{
    /**
     * the hashvalue of the band
     */
    @Property
    private Long hashValue;
   /* /**
     * the number of Bands hashed from each signature.
     *
    @Property("numberofbands")
    private int numberOfBands;
    /**
     * the number of bucket or size of the hashtable used in the hasfunctions for MinHashing and the hashing of bands.
     *
    @Property("hashtablesize")
    private int hashtableSize;
    /**
     * the number of hashfunctions uses in creating the signature by MinHashing

    @Property("numberoffunctions")
    private int numberOfHashfunctions;*/
    @Relationship(type="lshHashedTo",direction="INCOMING")
    private List<Entity> hashedToThis;
    public LocalitySensitiveHash(){}

    /**
     * A constructor creating the Locality-sensitive Hash without connections to any nodes but with its attributes set.
     * @param hashValue
     */
    public LocalitySensitiveHash(int hashValue){
        this.hashValue = (long) hashValue;
        this.hashedToThis = new ArrayList<>();
    }
    public LocalitySensitiveHash(int hashValue, Entity initialHashedToThis){
        this.hashValue = (long) hashValue;
        this.hashedToThis = new ArrayList<>();
        this.hashedToThis.add(initialHashedToThis);
    }
    /**
     *
     * @return a List of all Entities whose compareString has a band of signatures which hashes to this hash.
     */
    public List<Entity> getHashedToThis(){ return this.hashedToThis; }
    public long getHashValue(){ return this.hashValue; }
}
