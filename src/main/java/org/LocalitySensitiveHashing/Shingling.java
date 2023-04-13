package org.LocalitySensitiveHashing;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Shingling {

    private final int shingleSize;
    private final Set<String> shingels;

    public Shingling(String inputString, int shingleSize){
        shingels = new TreeSet<>();
        this.shingleSize = shingleSize;
        String[] words = inputString.toLowerCase().split(" ");
        for(String word: words) {
            for (int i = 0; i <= (word.length() - shingleSize); i++) {
                shingels.add(word.substring(i, i + shingleSize));
            }
        }
    }
    public Set<Integer> getHashesforShingles(int hashtableSize){
        Set<Integer> hashes = new HashSet<>();
        for (String shingle: shingels) {
            hashes.add(shingle.hashCode() % hashtableSize);
        }
        return hashes;
    }
    @Override
    public String toString(){
        return "ShingleSize: " + this.shingleSize + "\n" + this.shingels;
    }
}
