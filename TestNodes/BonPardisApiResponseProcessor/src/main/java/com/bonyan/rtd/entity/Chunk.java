package com.bonyan.rtd.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chunk<T> {
    private T contentId;
    private RtdAction<T> rtdAction;
    private RecordList records = new RecordList();
    private List<String> smsIds = new ArrayList<>();
    private Integer status;
    private String errorMessage;
    private String responseBody;

    public Chunk() {
        this.rtdAction = new RtdAction<>();
    }
    public Chunk(T contentId) {
        this.rtdAction = new RtdAction<>(contentId);
        this.contentId = contentId;
    }

    public int addRecord(Map.Entry<String, Integer> msisdn) {
        records.add(msisdn);
        return records.size();
    }


    public T getContentId() {
        return contentId;
    }

    public void setContentId(T contentId) {
        this.contentId = contentId;
    }

    public RtdAction<T> getRtdAction() {
        return rtdAction;
    }

    public void setRtdAction(RtdAction<T> rtdAction) {
        this.rtdAction = rtdAction;
    }

    public RecordList getRecords() {
        return records;
    }

    public void setRecords(RecordList records) {
        this.records = records;
    }

    public List<String> getSmsIds() {
        return smsIds;
    }

    public Chunk<T> setSmsIds(List<String> smsIds) {
        this.smsIds = smsIds;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public Chunk<T> setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Chunk<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Chunk<T> setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }
}
