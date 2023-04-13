package org.LocalitySensitiveHashing;

import org.apache.commons.codec.digest.MurmurHash3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Hashing {
    /**
     * @param input The integer to be digested by the hashfunctions
     * @param hashId the identifier of the hashfunction
     * @param numberOfHashfunctions: The number of hashfunctions to simulate.#
     * @param hashtableSize the size of the hashtable / number ofbuckets to hash to
     * @return the digest a specific hashfunction on the inputs.
     * hash1 is a simple input to the power of 4 modulo the hashtableSize
     * hash2 is gained by taking the input to the power of 6 and cutting digits from the front and
     * trimming digits from the front and back of the number to result in a number of digits equal
     * to the number of digits in the hashtableSize
     */
    private static int hashFunction(int input, int hashId, short numberOfHashfunctions, int hashtableSize){
        // hash1
        int hash1 = (int) Math.pow(1.0 + input, 3) % hashtableSize;
        // hash2
        int targetNumberOfDigits = String.valueOf(hashtableSize).length();
        int hash2 = (int) Math.pow(input + 1, 8);
        int currentDigitCount = String.valueOf(hash2).length();
        if(currentDigitCount > targetNumberOfDigits){
            int lengthOfTailToCut =  (int) Math.floor((double) (currentDigitCount - targetNumberOfDigits) / 2);
            int moduloPower = targetNumberOfDigits+lengthOfTailToCut;
            hash2 = hash2 % (int) Math.pow(10,moduloPower);
            hash2 = (int) (hash2 / Math.pow(10,lengthOfTailToCut));
        }
        // simulating multiple hashfunctions

        return  (hash1 + (hashId * hash2) ) % hashtableSize;
    }
    public static BitSet toBitVector(Shingling shingling, int hashtableSize){
        BitSet bitvector = new BitSet(hashtableSize);
        for (int hash: shingling.getHashesforShingles(hashtableSize)) {
            bitvector.set(hash, true);
        }
        return bitvector;
    }

    /**
     * @param vector this method takes a bitVector in form of a BitSet as input
     * @param numberOfHashFunctions the number of Hashfunctions to simulate
     * @param hastableSize the hastablesize or number of buckets
     * @return an integer-array which represents the signature. a signature is an array of Integers in which the values represent the index which is hashed gives the first set bit in the signature while incrementing through a number of hashfunction equal to the size of the signature.
     */
    public static int[] signature(BitSet vector, short numberOfHashFunctions, int hastableSize){
        /*
            create the signature vector/Array and fill it with Integer.MAXVALUE.
            In the algorithm infinity was used but this is practically the same.
        */
        int[] signature = new int[numberOfHashFunctions];
        Arrays.fill(signature, -1);
        for(int hashfunctionIndex = 0; hashfunctionIndex < numberOfHashFunctions; hashfunctionIndex++){
            List<Integer> currentHashFunction = generateHashFunctions(vector.size(), hastableSize, hashfunctionIndex);
            for(int index = 0; index < vector.size(); index++){
                int hash = currentHashFunction.get(index);
                if(vector.get(hash)){
                    signature[hashfunctionIndex]  = index;
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
    public static int[] hashedBandsFromSignature(int[] signature, int numberOfBands){
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
            hashedBands[bucketIndex] = workString.toString().hashCode() & 0xfffffff;
        }
        return hashedBands;
    }

    /**
     * This function generates a number of hashvalues from the given key Object those hashvalues fall into numBuckets buckets.
     * @param numHashes the number of different hashfunctions to use.
     * @param numBuckets thoe number of bucket aka the size of the hashtable.
     * @param key the key to be digested by the hashfunctions
     * @return a List of hashvalues one for each hashfunction
     */
    public static List<Integer> generateHashFunctions(int numHashes, int numBuckets, Object key) {
        List<Integer> bucketIndices = new ArrayList<>(numHashes);

        for (int i = 0; i < numHashes; i++) {
            int seed = i + 1; // use a different seed for each hash function
            byte[] bytes = key.toString().getBytes(StandardCharsets.UTF_8);
            int hash = MurmurHash3.hash32x86(bytes, 0, bytes.length, seed);
            int bucketIndex = (hash % numBuckets) & 0xffffff;
            bucketIndices.add(bucketIndex);
        }

        return bucketIndices;
    }
}
