package org.LocalitySensitiveHashing;

import org.ScientificWorksRelationshipGraph.Config;
import org.apache.commons.codec.digest.MurmurHash3;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Hashing {
    public Hashing(){}
    public  int[] generateLSHHashValues(String inputString){
        Shingling shingling = new Shingling(inputString, Config.SHINGLE_SIZE);
        BitVector bitVector = toBitVector(shingling, Config.HASHTABLE_SIZE);
        int sizeVector = bitVector.size();
        int[] signature = signature(bitVector, Config.NUMBER_OF_HASHFUNCTIONS, Config.HASHTABLE_SIZE);
        return hashedBandsFromSignature(signature, Config.NUMBER_OF_BANDS, Config.HASHTABLE_SIZE);
    }
    public static BitVector toBitVector(Shingling shingling, int hashtableSize){
        BitVector bitvector = new BitVector(hashtableSize);
        for (int hash: shingling.getHashesforShingles(hashtableSize)) {
            bitvector.set(hash, true);
        }
        return bitvector;
    }

    /**
     * @param vector this method takes a bitVector in form of a BitSet as input
     * @param numberOfHashFunctions the number of Hashfunctions to simulate
     * @param hashtableSize the hastablesize or number of buckets
     * @return an integer-array which represents the signature. a signature is an array of Integers in which the values represent the index which is hashed gives the first set bit in the signature while incrementing through a number of hashfunction equal to the size of the signature.
     */
    public static int[] signature(BitVector vector, short numberOfHashFunctions, int hashtableSize){
        /*
            create the signature vector/Array and fill it with Integer.MAXVALUE.
            In the algorithm infinity was used but this is practically the same.
        */
        int[] signature = new int[numberOfHashFunctions];
        Arrays.fill(signature, -1);
        for(int hashfunctionIndex = 0; hashfunctionIndex < numberOfHashFunctions; hashfunctionIndex++){
            for(int index = 0; index < vector.size(); index++){
                int currentHash = hashFunction(hashfunctionIndex, hashtableSize, index);
                if(vector.get(currentHash)){
                    signature[hashfunctionIndex] = index;
                    break;
                }
            }
        }
        return signature;
    }

    /**
     * @param signature the signature to hash in the format of an Integer Array
     * @param numberOfBands the number of buckets to create. ceiling(signature.length / numberOfBands)
     *                    is used to calculated bandsize
     * @return the buckets in form of an integer array. the buckets are created by concatenating all integers
     * in a band and then getting the hashCode for that String. Collisions (aka false positives) can come from two sources:
     * the .hashCode() method of String and the fact that two or more different integers concatenated can result in the same
     * string of digits. Like 122 and 12 resolving to 12212 while 12 and 212 resolve to 12212 too.
     */
    public static int[] hashedBandsFromSignature(int[] signature, int numberOfBands, int hashtableSize){
        int bandsize  = (int) Math.ceil(signature.length  / (double) numberOfBands);
        int[] hashedBands = new int[numberOfBands];
        Arrays.fill(hashedBands,0);
        StringBuilder workString;
        for(int bucketIndex = 0; bucketIndex < numberOfBands; bucketIndex++){
            workString = new StringBuilder();
            for (int i = bucketIndex*bandsize; i < (bucketIndex+1)*bandsize && i < signature.length; i++){
                workString.append(signature[i]);
            }
            //This normalizes the string to positive numbers only by doing a bitwise AND with Integer.MAX_VALUE
            hashedBands[bucketIndex] = (workString.toString().hashCode() & 0xfffffff)%hashtableSize;
        }
        return hashedBands;
    }

    /**
     * This function generates a number of hashvalues from the given key Object those hashvalues fall into numBuckets buckets.
     * @param hashFunctionIndex the number of different hashfunctions to use.
     * @param numBuckets thoe number of bucket aka the size of the hashtable.
     * @param key the key to be digested by the hashfunctions
     * @return a List of hashvalues one for each hashfunction
     */
    public static int hashFunction(int hashFunctionIndex, int numBuckets, int key) {
        int seed = hashFunctionIndex; // use a different seed for each hash function
        int hash = MurmurHash3.hash32(key, seed);
        return Math.abs(hash % numBuckets)%numBuckets; // return only positive values in the bucketrange
    }
}
