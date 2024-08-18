package com.bonyan.rtd.entity;

import java.util.Map;

public class Chunk<T> {
    private T actionName;
    private RtdAction rtdAction;
    private RecordList records;
    private boolean touched;



    public int addRecord(Map.Entry<String, Integer> msisdn) {
        RecordList records = this.records;
        records.add(msisdn);
        setTouched(true);
        return records.size();
    }


    public T getActionName() {
        return actionName;
    }

    public void setActionName(T actionName) {
        this.actionName = actionName;
    }

    public RtdAction getRtdAction() {
        return rtdAction;
    }

    public void setRtdAction(RtdAction rtdAction) {
        this.rtdAction = rtdAction;
    }

    public RecordList getRecords() {
        return records;
    }

    public void setRecords(RecordList records) {
        this.records = records;
    }

    public boolean isTouched() {
        return touched;
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }
}
