package com.bonyan.rtd;

import com.bonyan.rtd.entity.ChunkRepository;
import com.bonyan.rtd.entity.RecordList;
import com.bonyan.rtd.entity.RtdAction;
import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PardisBLN extends Nodebase implements BusinessLogic, Schedulable, TimerObserver
//        , LookupServiceUser
{

    private static final Logger logger = Logger.getLogger(PardisBLN.class.getName());
    private static final TxeLogger nodeLogger = NodeLoggerFactory.getNodeLogger(PardisBLN.class.getCanonicalName());
    public NodeContext nodeContext;
    private EventRecord currentRecord;
    private EventRecordService eventRecordService;
    private ChunkRepository chunkRepository;
    private Integer MAX_CHUNK_SIZE_PARAM;
    private String SOURCE_NUM_PARAM;
    private String REQUEST_URI;
    private String REQUEST_ID;
    private String REMOTE_SCHEME;
    private String REMOTE_ADDRESS;
    private String REMOTE_PORT;
    private String HEADER_HOST;
    private int chunker_writer_sleep_time;
    private HashMap<String, String> actionMessages;


//    private

    @Override
    public void init(NodeContext nodeContext) throws Exception {
        try {
//            TimesTenServiceImpl timesTenService = new TimesTenServiceImpl();

            nb_trace("node init start", 0);
            nodeLogger.info("nodeLogger: node init start");
            actionMessages = new HashMap<>();

            actionMessages.put("general_loan_content1", "Sms message content for action id:  general_loan_content1 ");
            actionMessages.put("general_loan_content2", "Sms message content for action id:  general_loan_content2 ");
            actionMessages.put("general_loan_content3", "Sms message content for action id:  general_loan_content3 ");
            actionMessages.put("general_loan_content4", "Sms message content for action id:  general_loan_content4 ");
            actionMessages.put("general_loan_content5", "Sms message content for action id:  general_loan_content5 ");
            actionMessages.put("general_loan_content6", "Sms message content for action id:  general_loan_content6 ");
            actionMessages.put("general_loan_content7", "Sms message content for action id:  general_loan_content7 ");
            actionMessages.put("general_loan_content8", "Sms message content for action id:  general_loan_content8 ");
            actionMessages.put("general_loan_content9", "Sms message content for action id:  general_loan_content9 ");
            actionMessages.put("general_loan_content10", "Sms message content for action id: general_loan_content10");
            nb_trace("action messages map initialized", 0);
            nodeLogger.info("action messages map initialized");

            this.nodeContext = nodeContext;
            this.MAX_CHUNK_SIZE_PARAM = nodeContext.getParameterInt("chunker-max-size");
            this.chunker_writer_sleep_time = nodeContext.getParameterInt("chunker-writer-sleep-time");
            this.SOURCE_NUM_PARAM = nodeContext.getParameter("source-number");
//            this.SOURCE_NUM_PARAM = "981000";
            this.REQUEST_URI = nodeContext.getParameter("request-uri");
//            this.REQUEST_URI = "/api/v2/sendBulk";
            this.REMOTE_SCHEME = nodeContext.getParameter("remote-scheme");
//            this.REMOTE_SCHEME = "http";
            this.REMOTE_ADDRESS = nodeContext.getParameter("remote-address");
//            this.REMOTE_ADDRESS = "192.168.5.84";
            this.REMOTE_PORT = nodeContext.getParameter("remote-port");
//            this.REMOTE_PORT = "4080";
            this.HEADER_HOST = nodeContext.getParameter("header-host");
//            this.HEADER_HOST = "192.168.5.84:4080";
            nodeLogger.info("nodeLogger: nodeContext.getParameters:" + nodeContext.getParameters());
            nb_trace(nodeContext.getParameters().toString(), 0);

            this.chunkRepository = new ChunkRepository();
//            this.actionTable = lookupService.getTable(lookupServer, actionTableName, true);

            logger.info("Init------------");
            nodeLogger.info("nodeLogger: node init end");
        } catch (Exception ex) {
            nodeLogger.info("nodeLogger: Exeption in Init method: " + ex.getMessage());
            nb_trace("Exeption in Init method: " + ex.getMessage(), 0);
            logger.fine("Error in Init" + ex.getMessage());
        }
    }

    @Override
    public void process(EventRecord eventRecord) throws Exception {
        nb_trace("Processing Input record", 1);
        nb_trace("Input record: " + eventRecord.toString(), 1);
        nodeLogger.info("nodeLogger: Processing Input record");
        nodeLogger.info("nodeLogger: Input record: " + eventRecord);

        eventRecord.getOriginalFilename();
        logger.info("Processing record");
        currentRecord = eventRecord;
        int chunkSize;
        RtdAction rtdAction = new RtdAction(getFieldStringValue("actionid"));
        Map.Entry<String, Integer> msisdnPair = new AbstractMap.SimpleEntry<>(this.getFieldStringValue("msisdn"), this.getFieldIntegerValue("count"));
        chunkSize = chunkRepository.addRecord(rtdAction, msisdnPair);
        //else throw error invalid record
        if (MAX_CHUNK_SIZE_PARAM.equals(chunkSize)) {
            for (RtdAction action : chunkRepository.keySet()) {
//             todo: get action messages from lookup server instead of hardcode map
                action.setActionMessageContent(actionMessages.get(action.getActionName()));
                action.generateNewId();

                chunkRecordWriter(action, chunkRepository.getRecords(action));
            }
            afterChunkWriteAction();
        }
    }


    public String getFieldStringValue(String fieldName) {
        nb_trace("in getFieldStringValue method", 1);

        try {
            logger.info("Extracting Field " + fieldName);
            nb_trace("Extracting Field " + fieldName, 1);
            nodeLogger.info("nodeLogger: Extracting Field " + fieldName);

            Field tmpField = currentRecord.getField(fieldName);
            String value = tmpField.getValue();
            logger.info(" Value " + value);
            nb_trace(" Value " + value, 1);
            nodeLogger.info("nodeLogger: Value " + value);

            return value;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant parse" + fieldName);
            nb_trace("Cant parse " + fieldName, 1);
            nodeLogger.info("nodeLogger: Cant parse " + fieldName);
            return "";
        }
    }

    public Integer getFieldIntegerValue(String fieldName) {
        nb_trace("in getFieldIntegerValue method", 1);
        nodeLogger.info("nodeLogger: in getFieldIntegerValue method\"");

        try {
            logger.info("Extracting Field " + fieldName);
            nb_trace("Extracting Field " + fieldName, 1);
            nodeLogger.info("nodeLogger: Extracting Field " + fieldName);

            Field tmpField = currentRecord.getField(fieldName);
            Integer value = Integer.valueOf(tmpField.getValue());
            logger.info(" Value " + value);
            nb_trace(" Value " + value, 1);
            nodeLogger.info("nodeLogger: Value " + value);

            return value;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant parse" + fieldName);
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
    public void request(String s) throws Exception {
        nb_trace("in node request method, input String: " + s, 0);
        nodeLogger.info("nodeLogger: node request start, input String: " + s);

    }

    @Override
    public void pause(int i) throws Exception {
        nb_trace("in node pause method, input int: " + i, 0);
        nodeLogger.info("nodeLogger: pause method, input int: " + i);

    }

    @Override
    public void resume(int i) throws Exception {
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
    public void schedule() throws Exception {
        nb_trace("in node schedule method", 0);
        nodeLogger.info("nodeLogger: node schedule start");

    }


    public void chunkRecordWriter(RtdAction action, RecordList chunkList) {
        EventRecord eventRecord = this.eventRecordService.newRecord();
        nodeLogger.info("nodeLogger: chunk record writer method, action id: " + action.getActionName() + ", chunk size: " + chunkList.size());

        eventRecord.addField("record-generate-time", new Date().toString());
        apiBodyBuilder(action, chunkList, eventRecord);
        apiRequestBuilder(action.getRequestId(), eventRecord);
        nodeLogger.info("nodeLogger: out record for action: " + action.getActionName() + ", record: " + eventRecord);

        this.eventRecordService.write("OUT", eventRecord);
    }

    @Override
    public void timer() {
    }

    public void apiBodyBuilder(RtdAction action, RecordList msisdnList, EventRecord eventRecord) {
        StringBuilder pardisJsonBody = new StringBuilder();
        nodeLogger.info("nodeLogger: api body builder method start");

        pardisJsonBody.append("{\"destinationList\" : ").append(msisdnList.getkeySetString()).append(",").append("\"message\": \"").append(action.getActionMessageContent()).append("\"").append(",").append("\"smsClass\": \"").append("NORMAL").append("\"").append(",").append("\"source\": \"").append(SOURCE_NUM_PARAM).append("\"").append("}");

        eventRecord.addField("Body", pardisJsonBody.toString());
        nodeLogger.info("nodeLogger: api body builder: event record body: " + pardisJsonBody.toString());

        eventRecord.addField("Request-Id", "[" + action.getActionName() + "]" + msisdnList.getKeyValueListString());
        nodeLogger.info("nodeLogger: request values: " + eventRecord.getField("RequestValues"));
    }

    public void apiRequestBuilder(String requestId, EventRecord eventRecord) {
        nodeLogger.info("nodeLogger: api request builder method");

        eventRecord.addField("Method", "POST");
        eventRecord.addField("Request", "true");
        eventRecord.addField("Request-Uri", REQUEST_URI);
//        eventRecord.addField("Request-Id", requestId);
        eventRecord.addField("Remote-Scheme", REMOTE_SCHEME);
        eventRecord.addField("Remote-Address", REMOTE_ADDRESS);
        eventRecord.addField("Remote-Port", REMOTE_PORT);
        eventRecord.addField("Header-Authorization", getToken());
        eventRecord.addField("MMMHeader-User-Agent", "curl/7.19.7 (x86_64-redhat-linux-gnu)libcurl/7.19.7 NSS/3.14.0.0 zlib/1.2.3 libidn/1.18libssh2/1.4.2");
        eventRecord.addField("Header-Content-Type", "application/json; charset=UTF-8");
        eventRecord.addField("Header-Accept", "application/json");
        eventRecord.addField("Header-Host", HEADER_HOST);
    }

    public String getToken() {
        return "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImNhdGNyeSIsImV4cCI6MTcyODQzNzMxOH0.PZmvrcM238NtuWeKsTauA5VrJsxDGRMGnEUeQZL1YyI";
    }

    public void afterChunkWriteAction() {
        chunkRepository.clear();
        try {
            nodeLogger.info("nodeLogger: node sleep for 50 ms");
            Thread.sleep(chunker_writer_sleep_time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
