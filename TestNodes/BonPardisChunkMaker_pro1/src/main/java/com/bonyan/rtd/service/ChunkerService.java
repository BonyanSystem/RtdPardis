package com.bonyan.rtd.service;

import com.bonyan.rtd.PardisChunkMakerNode;
import com.bonyan.rtd.entity.Chunk;
import com.bonyan.rtd.entity.ChunkRepository;
import com.bonyan.rtd.entity.RecordList;
import com.bonyan.rtd.entity.RtdAction;
import com.bonyan.rtd.utils.NodeRecordUtil;
import com.comptel.mc.node.EventRecord;
import com.comptel.mc.node.EventRecordService;
import com.comptel.mc.node.Field;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class ChunkerService {

    private final ChunkWriter chunkWriter;
    private final EventRecordService eventRecordService;
    private final ChunkRepository<String> chunkRepository;
    private final PardisChunkMakerNode.NodeParameters nodeParameters;
    private Integer recordNumber;


    public ChunkerService(EventRecordService eventRecordService, PardisChunkMakerNode.NodeParameters nodeParameters) {
        this.eventRecordService = eventRecordService;
        this.nodeParameters = nodeParameters;
        this.chunkWriter = new ChunkWriter(eventRecordService);
        this.chunkRepository = new ChunkRepository<>();
        this.recordNumber = 0;
    }

    public void makeOutput(String actionName, RecordList chunkList) {
        EventRecord eventRecord = this.eventRecordService.newRecord();

        eventRecord.addField("record_generate_time", new Date().toString());
        makeChunkRecord(chunkRepository.get(actionName).getRtdAction(), chunkList, eventRecord);

        chunkWriter.writeRecord(eventRecord);
    }

    public void makeChunkRecord(RtdAction action, RecordList msisdnList, EventRecord eventRecord) {
        Field actionBlock = eventRecord.addField("Action");
        actionBlock.addField("action_id", action.getActionName());
        actionBlock.addField("request_id", action.getRequestId());

        int i = 1;
        Field chunkListBlock = eventRecord.addField("ChunkList");
        for (Map.Entry<String, Integer> msisdnPair : msisdnList) {
            Field subListBlock = chunkListBlock.addField("Sub_" + i);
            subListBlock.addField("msisdn", msisdnPair.getKey());
            subListBlock.addField("retry_count", msisdnPair.getValue().toString());
        }

    }

    public void makeAndWriteChunkRecords() {
        for (String actionName : chunkRepository.keySet()) {
            makeOutput(actionName, chunkRepository.get(actionName).getRecords());
        }
        removeUsedChunk(null);
    }

    public void makeAndWriteChunkRecord(RtdAction action) {
        makeOutput(action.getActionName(), chunkRepository.get(action.getActionName()).getRecords());
        removeUsedChunk(action);
    }

    public void removeUsedChunk(RtdAction action) {
        if (action != null) {
            chunkRepository.remove(action.getActionName());
        } else {
            chunkRepository.clear();
        }
    }

    public boolean addRecord(EventRecord eventRecord) {
        recordNumber++;
        checkMaxUntouched();

        Integer retryCount = NodeRecordUtil.getFieldIntegerValue(eventRecord, "count");

        if (retryCount < nodeParameters.getMaxSendRetryCount()) {
            String msisdn = NodeRecordUtil.getFieldStringValue(eventRecord, "msisdn");

            RtdAction rtdAction = new RtdAction(NodeRecordUtil.getFieldStringValue(eventRecord, "actionid"));
            Map.Entry<String, Integer> msisdnPair = new AbstractMap.SimpleEntry<>(msisdn, retryCount);
            int chunkSize = chunkRepository.addRecord(rtdAction, msisdnPair);

            if (nodeParameters.getMaxChunkSizeParam().equals(chunkSize)) {
                makeAndWriteChunkRecord(rtdAction);
            }
            return true;
        }
        return false;
    }

    public void checkMaxUntouched() {
        if (recordNumber >= nodeParameters.getMaxUntouchedRecordCount()) {
            writeUntouchedChunk();
            recordNumber = 0;
        }
    }

    public void writeUntouchedChunk() {
        Set<Chunk<String>> untouchedChunk = chunkRepository.getUntouchedChunk();
        for (Chunk<String> chunk : untouchedChunk) {
            makeAndWriteChunkRecord(chunk.getRtdAction());
        }
        chunkRepository.resetTouched();
    }
}
