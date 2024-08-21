package com.bonyan.rtd.service;

import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.EventRecord;
import com.comptel.mc.node.EventRecordService;
import com.comptel.mc.node.Field;

public class ResponseProcessorService extends Nodebase{
    private EventRecordService eventRecordService;

    public ResponseProcessorService(EventRecordService eventRecordService) {
        this.eventRecordService = eventRecordService;
    }

    public void processResponseRecord(EventRecord eventRecord) {
        Field responseBlock = eventRecord.getField("Response___");
        if (responseBlock != null) {
            Field responseBody = responseBlock.getField("Body");
        }

    }
}
