package com.bonyan.rtd.service;

import com.bonyan.rtd.entity.DbTableColumn;
import com.bonyan.rtd.entity.RtdAction;
import com.comptel.mc.node.EventRecord;
import com.comptel.mc.node.EventRecordService;
import com.comptel.mc.node.Field;
import javafx.util.Pair;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChunkWriterService {

    public static final String REQUEST_ACTION_TABLE_NAME = "Request_Action";
    public static final String REQUEST_CHUNK_TABLE_NAME = "Request_Chunk";

//    private EventRecordWriterService eventRecordWriterService;
    private EventRecordService eventRecordService;
    private TimesTenDbWriterService timesTenDbWriterService;
    private String outputPortName;
    private HashMap<String, DbTableColumn> requestActionColumns = new HashMap<>();
    private HashMap<String, DbTableColumn> requestChunkColumns = new HashMap<>();


    public ChunkWriterService(EventRecordService eventRecordService,
                              TimesTenDbWriterService timesTenDbWriterService, String outputPortName) {
        this.eventRecordService = eventRecordService;
        this.timesTenDbWriterService = timesTenDbWriterService;
        this.outputPortName = outputPortName;
    }

    public void writeRtdPardisRecord(RtdAction action, List<Pair<String, Integer>> chunkList) {
        EventRecord eventRecord = eventRecordService.newRecord();

        Field actionBlock = eventRecord.addField("Action");
        actionBlock.addField("actionName", action.getActionName());
        actionBlock.addField("messageContent", action.getActionMessageContent());
        actionBlock.addField("requestId", action.getRequestId());

        Field chunkListBlock = eventRecord.addField("ChunkList");
        int i = 1;
        for (Pair<String, Integer> chunkPair: chunkList) {
            chunkListBlock.addField("msisdn_" + i, chunkPair.getKey());
            chunkListBlock.addField("retryCount_" + i, chunkPair.getValue().toString());
            i++;
        }
        eventRecordService.write(outputPortName, eventRecord);
    }

    public void writeRecordInTimesTen(RtdAction action, List<Pair<String, Integer>> chunkList) {
        try {
            requestActionColumns.get("request_id").setValue(action.getRequestId());
            requestActionColumns.get("action_name").setValue(action.getActionName());
            requestChunkColumns.get("request_id").setValue(action.getActionName());
            timesTenDbWriterService.insertRecord(REQUEST_ACTION_TABLE_NAME, requestActionColumns.values());

            for (Pair<String, Integer> chunkPair: chunkList) {
                requestChunkColumns.get("msisdn").setValue(chunkPair.getKey());
                requestChunkColumns.get("retry_count").setValue(chunkPair.getValue());
                timesTenDbWriterService.insertRecord(REQUEST_CHUNK_TABLE_NAME, requestChunkColumns.values());
            }

            timesTenDbWriterService.getConnection().commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createRequestActionTable() {
        DbTableColumn requestId = new DbTableColumn("request_id", "VARCHAR");
        DbTableColumn actionName = new DbTableColumn("action_Name", "VARCHAR");
        requestActionColumns.put(requestId.getColumnName(), requestId);
        requestActionColumns.put(actionName.getColumnName(), actionName);
        try {
            timesTenDbWriterService.tableCreate(REQUEST_ACTION_TABLE_NAME, requestActionColumns.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createRequestChunkTable() {
        DbTableColumn requestId = new DbTableColumn("request_id", "VARCHAR");
        DbTableColumn msisdn = new DbTableColumn("msisdn", "VARCHAR");
        DbTableColumn retryCount = new DbTableColumn("retry_count", "INTEGER");
        requestChunkColumns.put(requestId.getColumnName(), requestId);
        requestChunkColumns.put(msisdn.getColumnName(), msisdn);
        requestChunkColumns.put(retryCount.getColumnName(), retryCount);
        try {
            timesTenDbWriterService.tableCreate(REQUEST_CHUNK_TABLE_NAME, requestChunkColumns.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
