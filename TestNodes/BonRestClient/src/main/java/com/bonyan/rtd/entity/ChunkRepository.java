package com.bonyan.rtd.entity;

import java.util.HashMap;
import java.util.Map;

public class ChunkRepository extends HashMap<RtdAction, RecordList> {

    public boolean hasAction(RtdAction rtdAction) {
        return this.containsKey(rtdAction);
    }

    public void addActionIfNotExist(RtdAction rtdAction) {
        if (!this.hasAction(rtdAction)) {
            this.put(rtdAction, new RecordList());
        }
    }

    public void addAction(RtdAction rtdAction) {
        this.put(rtdAction, new RecordList());
    }

    public RecordList getRecords(RtdAction rtdAction) {
        if (!this.hasAction(rtdAction)) {
            this.put(rtdAction, new RecordList());
        }
        return this.get(rtdAction);
    }

    public int addRecord(RtdAction rtdAction, Map.Entry<String, Integer> msisdn) {
        this.getRecords(rtdAction).add(msisdn);
        int size = 0;
        for (RecordList msisdnPair: this.values()) {
            size += msisdnPair.size();
        }
        return size;
    }

    public String chunkToJsonString(RtdAction rtdAction) {

        return "{\n" +
                "\"action\" : " + rtdAction + "," +

                "}";
    }
}
