package org.LocalitySensitiveHashing;

public class BitVector {
    private final boolean[] bits;
    public BitVector(int size){
        this.bits = new boolean[size];
    }
    public int size(){ return this.bits.length; }
    public void set(int index, boolean value){ this.bits[index] = value; }
    public boolean get(int index){ return this.bits[index]; }
    public BitVector and(BitVector other){
        int resultSize = (this.size() < other.size())? this.size() : other.size();
        BitVector result = new BitVector(resultSize);
        for(int i = 0; i<resultSize; i++){
            if(this.get(i) && other.get(i)) result.set(i, true);
        }
        return result;
    }
}
