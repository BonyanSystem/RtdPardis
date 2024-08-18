package com.bonyan.rtd;

import com.bonyan.rtd.entity.ChunkRepository;
import com.bonyan.rtd.entity.RecordList;
import com.bonyan.rtd.entity.RtdAction;
import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PardisBLN extends Nodebase implements BusinessLogic, Schedulable
//        , LookupServiceUser
{

    private static final Logger logger = Logger.getLogger(PardisBLN.class.getName());
    private static final TxeLogger nodeLogger = NodeLoggerFactory.getNodeLogger(PardisBLN.class.getCanonicalName());
    private NodeContext nodeContext;
    private EventRecord currentRecord;
    private EventRecordService eventRecordService;
    private ChunkRepository<String> chunkRepository;
    private Integer maxChunkSizeParam;
    private Integer maxSendRetryCount;
    private String lastFileName;

    @Override
    public void init(NodeContext nodeContext) {
        try {
            nb_trace("node init start", 0);
            nodeLogger.info("nodeLogger: node init start");
            this.nodeContext = nodeContext;
            loadNodeParameters();
            this.chunkRepository = new ChunkRepository<String>();
            logger.info("Init------------");
            nodeLogger.info("nodeLogger: node init end");
        } catch (Exception ex) {
            nodeLogger.info("nodeLogger: Exception in Init method: " + ex.getMessage());
            nb_trace("Exception in Init method: " + ex.getMessage(), 0);
            logger.fine("Error in Init" + ex.getMessage());
        }
    }

    private void loadNodeParameters() {
        lastFileName = nb_get_original_filename();
        this.maxChunkSizeParam = nodeContext.getParameterInt("chunker-max-size");
        this.maxSendRetryCount = nodeContext.getParameterInt("max-retry-count");
        nodeLogger.info("nodeLogger: nodeContext.getParameters:" + nodeContext.getParameters());
        nb_trace(nodeContext.getParameters().toString(), 0);
    }

    @Override
    public void process(EventRecord eventRecord) {
        nb_trace("Processing Input record", 1);
        nb_trace("Input record: " + eventRecord.toString(), 1);
        nodeLogger.info("nodeLogger: Processing Input record");
        nodeLogger.info("nodeLogger: Input record: " + eventRecord);
        logger.info("Processing record");
        String recordFilename = eventRecord.getOriginalFilename().toString();

        if (lastFileName != null && !lastFileName.equals(recordFilename)) {
            chunksWriter();
        }

        Integer retryCount = this.getFieldIntegerValue("count");

        if (retryCount < maxSendRetryCount) {
            String msisdn = this.getFieldStringValue("msisdn");
            currentRecord = eventRecord;
            int chunkSize;
            RtdAction rtdAction = new RtdAction(getFieldStringValue("actionid"));
            Map.Entry<String, Integer> msisdnPair = new AbstractMap.SimpleEntry<>(msisdn, retryCount);
            chunkSize = chunkRepository.addRecord(rtdAction, msisdnPair);
            if (maxChunkSizeParam.equals(chunkSize)) {
                chunkWriter(rtdAction);
            }
        }

    }


    public String getFieldStringValue(String fieldName) {
        nb_trace("in getFieldStringValue method", 1);

        try {
            logger.log(Level.FINE, "Extracting Field {}", fieldName);
            nb_trace("Extracting Field " + fieldName, 1);
            nodeLogger.info("nodeLogger: Extracting Field " + fieldName);

            Field tmpField = currentRecord.getField(fieldName);
            String value = tmpField.getValue();
            logger.log(Level.FINE, " Value {}", value);
            nb_trace(" Value " + value, 1);
            nodeLogger.info("nodeLogger: Value " + value);

            return value;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant parse {}", fieldName);
            nb_trace("Cant parse " + fieldName, 1);
            nodeLogger.info("nodeLogger: Cant parse " + fieldName);
            return "";
        }
    }

    public Integer getFieldIntegerValue(String fieldName) {
        nb_trace("in getFieldIntegerValue method", 1);
        nodeLogger.info("nodeLogger: in getFieldIntegerValue method\"");

        try {
            logger.log(Level.FINE, "Extracting Field {}", fieldName);
            nb_trace("Extracting Field " + fieldName, 1);
            nodeLogger.info("nodeLogger: Extracting Field " + fieldName);

            Field tmpField = currentRecord.getField(fieldName);
            Integer value = Integer.valueOf(tmpField.getValue());
            logger.log(Level.FINE, " Value {}", value);
            nb_trace(" Value " + value, 1);
            nodeLogger.info("nodeLogger: Value " + value);

            return value;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant parse {}", fieldName);
            nb_trace("Cant parse " + fieldName, 1);
            nodeLogger.info("nodeLogger: Cant parse " + fieldName);

            return 0;
        }
    }

    @Override
    public void flush() throws Exception {
        nb_trace("in node flush method", 0);
        nodeLogger.info("nodeLogger: node flush start");
    }

    @Override
    public void end() throws Exception {
        nb_trace("node end method start", 0);
        nodeLogger.info("nodeLogger: node 'end' start");

    }

    @Override
    public void request(String s) {
        nb_trace("in node request method, input String: " + s, 0);
        nodeLogger.info("nodeLogger: node request start, input String: " + s);

    }

    @Override
    public void pause(int i) {
        nb_trace("in node pause method, input int: " + i, 0);
        nodeLogger.info("nodeLogger: pause method, input int: " + i);

    }

    @Override
    public void resume(int i) {
        nb_trace("in node resume method, input int: " + i, 0);
        nodeLogger.info("nodeLogger: resume method, input int: " + i);

    }

    @Override
    public void setService(EventRecordService eventRecordService) {
        nb_trace("in node setService method (EventRecordService will set here)", 0);
        nodeLogger.info("nodeLogger: node setService method start");

        this.eventRecordService = eventRecordService;
    }

    @Override
    public void schedule() {
        nb_trace("in node schedule method", 0);
        nodeLogger.info("nodeLogger: node schedule start");

    }


    public void chunkRecordWriter(String actionName, RecordList chunkList) {
        EventRecord eventRecord = this.eventRecordService.newRecord();
        nodeLogger.info("nodeLogger: chunk record writer method, action id: " + actionName + ", chunk size: " + chunkList.size());

        eventRecord.addField("record_generate_time", new Date().toString());
        chunkRecordMaker(chunkRepository.get(actionName).getRtdAction(), chunkList, eventRecord);
        nodeLogger.info("nodeLogger: out record for action: " + actionName + ", record: " + eventRecord);

        this.eventRecordService.write("OUT", eventRecord);
    }

    public void chunkRecordMaker(RtdAction action, RecordList msisdnList, EventRecord eventRecord) {
        nodeLogger.info("nodeLogger: api body builder method start");

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

        nodeLogger.info("nodeLogger: request values: " + eventRecord.getField("RequestValues"));
    }

    public void chunksWriter() {
        for (String actionName : chunkRepository.keySet()) {
            chunkRecordWriter(actionName, chunkRepository.get( actionName).getRecords());
        }
        afterChunkWriteAction(null);
    }

    public void chunkWriter(RtdAction action) {
        chunkRecordWriter(action.getActionName(), chunkRepository.get(action.getActionName()).getRecords());
        afterChunkWriteAction(action);
    }

    public void afterChunkWriteAction(RtdAction action) {
        if (action != null) {
            chunkRepository.remove(action.getActionName());
        } else {
            chunkRepository.clear();
        }
    }
}
