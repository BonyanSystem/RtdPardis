package com.bonyan.rtd.entity;

import java.util.*;

public class ChunkRepository<T> extends HashMap<T, Chunk<T>> {


    public int addRecord(RtdAction rtdAction, Map.Entry<String, Integer> msisdn) {
        return this.get(rtdAction.getActionName()).addRecord(msisdn);
    }

    @Override
    public Chunk<T> get(Object key) {
        if (super.get(key) == null) this.addChunk((T) key);
        return super.get(key);
    }

    public void addChunk(T actionName) {
        Chunk<T> chunk = new Chunk<>();
        this.put(actionName, chunk);
    }

    public Set<Chunk<T>> getUntouchedChunk(){
        Set<Chunk<T>> untouchedChunk = new HashSet<>();
        for (Chunk<T> chunk: this.values()) {
            if (!chunk.isTouched()) {
                untouchedChunk.add(chunk);
            }
        }
        return untouchedChunk;
    }

    public void clearTouched(){
        for(Chunk<T> chunk: this.values()) {
            chunk.setTouched(false);
        }
    }
}
