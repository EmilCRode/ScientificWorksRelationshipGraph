package org.ScientificWorksRelationshipGraph;

import org.apache.commons.text.similarity.CosineDistance;

import java.util.List;

public class Distances {
    /**
     * Calculates the string distance between source and target strings using
     * the Damerau-Levenshtein algorithm. The distance is case-sensitive.
     *
     * @param source The source String.
     * @param target The target String.
     * @return The distance between source and target strings.
     * @throws IllegalArgumentException If either source or target is null.
     */
    public static int damerauLevenshteinDistance(CharSequence source, CharSequence target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
        int sourceLength = source.length();
        int targetLength = target.length();
        if (sourceLength == 0) return targetLength;
        if (targetLength == 0) return sourceLength;
        int[][] dist = new int[sourceLength + 1][targetLength + 1];
        for (int i = 0; i < sourceLength + 1; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j < targetLength + 1; j++) {
            dist[0][j] = j;
        }
        for (int i = 1; i < sourceLength + 1; i++) {
            for (int j = 1; j < targetLength + 1; j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
                if (i > 1 &&
                        j > 1 &&
                        source.charAt(i - 1) == target.charAt(j - 2) &&
                        source.charAt(i - 2) == target.charAt(j - 1)) {
                    dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + cost);
                }
            }
        }
        return dist[sourceLength][targetLength];
    }
    public static double weightedDamerauLevenshteinSimilarity(CharSequence source, CharSequence target){
        if (source == null || target == null) {
            return (source == null && target == null)? 1 : 0;
        }
        if (source.isEmpty() || target.isEmpty()){
            return (source.isEmpty() && target.isEmpty()) ? 1 : 0;
        }
        double avglength = (source.length() + target.length()) / 2;
        double distance = damerauLevenshteinDistance(source, target);
        return 1- (distance / (avglength));
    }
    public static double cosineSimilarity(final CharSequence sequence1, final CharSequence sequence2){
        CosineDistance cosineDistance = new CosineDistance();
        Double distance = cosineDistance.apply(sequence1, sequence2);
        return 1- distance;
    }
    public static double compareAuthors(List<Author> authors1, List<Author> authors2){
        int counter = 1;
        double matchingAuthors = 0;
        for (Author authorX: authors1) {
            counter ++;
            for(Author authorY: authors2){
                if(authorX.compareTo(authorY) > 0.75){
                    matchingAuthors += 1;
                }
            }
        }
        return matchingAuthors / counter;
    }
    public static int compareYears(int int1, int int2){
        if(int1 == int2){ return 1; } else if ((Math.log(int1) + 1) != (Math.log(int2)+1)) {
            //Branchless code to return 1 if the last two digits match. Returns 0 otherwise

            //Get the last two digits via Modulo
            int lastDigitsOf1 = int1 % 100;
            int lastDigitsOf2 = int2 % 100;
            //The following two assignments make sign 1 if int1 is >= int2
            int diff = lastDigitsOf1 - lastDigitsOf2;
            int sign = (diff >> 31) + 1;
            //The following two assignments make sign2 1 if int1 is <= int2
            int diff2 = lastDigitsOf2 - lastDigitsOf1;
            int sign2 = (diff2 >> 31) + 1;
            //returns 1 only if sign and sign2 are both equal to 1 (So if int1 is >= int2 and int1 is also <= int2, meaning they must be equal)
            return sign * sign2;
        } else return 0;
    }
}
