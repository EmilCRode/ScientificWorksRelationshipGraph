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
    @Id
    private long hashValue;
    /**
     * the number of Bands hashed from each signature.
     */
    @Property("numberofbands")
    private int numberOfBands;
    /**
     * the number of bucket or size of the hashtable used in the hasfunctions for MinHashing and the hashing of bands.
     */
    @Property("hashtablesize")
    private int hashtableSize;
    /**
     * the number of hashfunctions uses in creating the signature by MinHashing
     */
    @Property("numberoffunctions")
    private int numberOfHashfunctions;
    @Relationship(type="lshHashedto",direction="INCOMING")
    private List<Entity> hashedToThis;
    public LocalitySensitiveHash(){}

    /**
     * A constructor creating the Locality-sensitive Hash without connections to any nodes but with its attributes set.
     * @param hashValue
     * @param hashtableSize
     * @param numberOfHashfunctions
     * @param numberOfBands
     */
    public LocalitySensitiveHash(int hashValue, int hashtableSize, int numberOfHashfunctions, int numberOfBands){
        this.hashValue = hashValue;
        this.numberOfBands  = numberOfBands;
        this.hashtableSize = hashtableSize;
        this.numberOfHashfunctions = numberOfHashfunctions;
        this.hashedToThis = new ArrayList<>();
    }

    /**
     *
     * @return a List of all Entities whose compareString has a band of signatures which hashes to this hash.
     */
    public List<Entity> getHashedToThis(){ return this.hashedToThis; }
}
