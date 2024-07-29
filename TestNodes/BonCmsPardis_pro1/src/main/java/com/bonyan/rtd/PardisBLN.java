package com.bonyan.rtd;

import com.bonyan.rtd.entity.ChunkRepository;
import com.bonyan.rtd.entity.RtdAction;
import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PardisBLN extends Nodebase implements BusinessLogic, Schedulable, TimerObserver
//        , LookupServiceUser
{

    private static Logger logger = Logger.getLogger(PardisBLN.class.getName());

    public NodeContext nodeContext;
    private EventRecord currentRecord;
    private EventRecordService eventRecordService;
    private ChunkRepository chunkRepository;
    private Integer MAX_CHUNK_SIZE;

//    private LookupService lookupService;
//    private String lookupServer;
//    private String actionTableName;
//    private LookupTable actionTable;

//    private String timesTenUser;
//    private String timesTenPassword;
//    private String timesTenDatasource;
//    private String timesTenDriver;

    private HashMap<String, String> actionMessages;


//    private

    @Override
    public void init(NodeContext nodeContext) throws Exception {
        try {
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

//            timesTenUser = nodeContext.getParameter("timesTenUser");
//            timesTenPassword = nodeContext.getParameter("timesTenPassword");
//            timesTenDatasource = nodeContext.getParameter("timesTenDatasource");
//            timesTenDriver = nodeContext.getParameter("timesTenDriver");
//
//            lookupServer = nodeContext.getParameter("lookupServer");
//            actionTableName = nodeContext.getParameter("actionTableName");
//
//            DataSource dataSource = DataSourceFactory.getInstance().getDataSource(
//                    timesTenUser, timesTenPassword, timesTenDatasource, timesTenDriver);

//            Connection connection = dataSource.getConnection();
            this.nodeContext = nodeContext;
            this.MAX_CHUNK_SIZE = nodeContext.getParameterInt("chunker-max-size");
//            this.MAX_CHUNK_SIZE = 5;
            this.chunkRepository = new ChunkRepository();
//            this.actionTable = lookupService.getTable(lookupServer, actionTableName, true);

            logger.info("Init------------");
        } catch (Exception ex) {
            logger.fine("Error in Init" + ex.getMessage());
        }
    }

    @Override
    public void process(EventRecord eventRecord) throws Exception {
        logger.info("Processing record");
        currentRecord = eventRecord;
        logger.info("Processing record");
        int chunkSize;
//        if (eventRecord.getFields().contains("actionid")) {
        RtdAction rtdAction = new RtdAction(getFieldStringValue("actionid"));
//            if (eventRecord.getFields().contains("msisdn")) {
        Map.Entry<String, Integer> msisdnPair = new AbstractMap.SimpleEntry<>(
                this.getFieldStringValue("msisdn"),
                this.getFieldIntegerValue("count"));
        chunkSize = chunkRepository.addRecord(rtdAction, msisdnPair);
//        rtdAction.setActionMessageContent(actionMessages.get(rtdAction.getActionName()));
//        rtdAction.generateNewId();

//                writeRtdPardisRecord(rtdAction, chunkRepository.getRecords(rtdAction));
//            }
        //else throw error invalid record
//        }


        //else throw error invalid record
        if (MAX_CHUNK_SIZE.equals(chunkSize)) {
            for (RtdAction action : chunkRepository.keySet()) {
//             todo: get action messages from lookup server instead of hardcode map
                action.setActionMessageContent(actionMessages.get(action.getActionName()));
                action.generateNewId();

                writeRtdPardisRecord(action, chunkRepository.getRecords(action));
            }
            chunkRepository.clear();
        }

//        Field messageField = eventRecord.addField("Message");
//        messageField.setValue("greetingText");
//        eventRecordService.write("OUT", eventRecord);

    }


    public String getFieldStringValue(String fieldName) {

        try {
            logger.info("Extracting Field " + fieldName);
            Field tmpField = currentRecord.getField(fieldName);
            String value = tmpField.getValue();
            logger.info(" Value " + value);
            return value;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant parse" + fieldName);
            return "";
        }
    }

    public Integer getFieldIntegerValue(String fieldName) {

        try {
            logger.info("Extracting Field " + fieldName);
            Field tmpField = currentRecord.getField(fieldName);
            Integer value = Integer.valueOf(tmpField.getValue());
            logger.info(" Value " + value);
            return value;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant parse" + fieldName);
            return 0;
        }
    }

    @Override
    public void flush() throws Exception {

    }

    @Override
    public void end() throws Exception {
        for (RtdAction rtdAction : chunkRepository.keySet()) {
//             todo: get action messages from lookup server instead of hardcode map
            rtdAction.setActionMessageContent(actionMessages.get(rtdAction.getActionName()));
            rtdAction.generateNewId();

            writeRtdPardisRecord(rtdAction, chunkRepository.getRecords(rtdAction));
        }
        chunkRepository.clear();
    }

    @Override
    public void request(String s) throws Exception {

    }

    @Override
    public void pause(int i) throws Exception {

    }

    @Override
    public void resume(int i) throws Exception {

    }

    @Override
    public void setService(EventRecordService eventRecordService) {
        this.eventRecordService = eventRecordService;
    }

    @Override
    public void schedule() throws Exception {

    }


    public void writeRtdPardisRecord(RtdAction action, List<Map.Entry<String, Integer>> chunkList) {
        EventRecord eventRecord = this.eventRecordService.newRecord();

        Field actionBlock = eventRecord.addField("Action");
        actionBlock.addField("actionName", action.getActionName());
        actionBlock.addField("messageContent", action.getActionMessageContent());
        actionBlock.addField("requestId", action.getRequestId());

        Field chunkListBlock = eventRecord.addField("ChunkList");
        int i = 1;
        for (Map.Entry<String, Integer> chunkPair : chunkList) {
            chunkListBlock.addField("msisdn_" + i, chunkPair.getKey());
            chunkListBlock.addField("retryCount_" + i, String.valueOf(chunkPair.getValue() + 1));
            i++;
        }
        this.eventRecordService.write("OUT", eventRecord);
    }

    @Override
    public void timer() throws Exception {
        for (RtdAction rtdAction : chunkRepository.keySet()) {
//             todo: get action messages from lookup server instead of hardcode map
            rtdAction.setActionMessageContent(actionMessages.get(rtdAction.getActionName()));
            rtdAction.generateNewId();

            writeRtdPardisRecord(rtdAction, chunkRepository.getRecords(rtdAction));
        }
        chunkRepository.clear();
    }
}
