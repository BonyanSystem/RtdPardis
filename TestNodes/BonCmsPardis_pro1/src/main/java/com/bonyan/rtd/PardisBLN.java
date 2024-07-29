package com.bonyan.rtd;

import com.bonyan.rtd.entity.ChunkRepository;
import com.bonyan.rtd.entity.RtdAction;
import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.comptel.mc.node.database.DataSourceFactory;
import com.comptel.mc.node.lookup.LookupService;
import com.comptel.mc.node.lookup.LookupServiceUser;
import com.comptel.mc.node.lookup.LookupTable;
import javafx.util.Pair;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PardisBLN extends Nodebase implements BusinessLogic, Schedulable, LookupServiceUser {

    private static Logger logger = Logger.getLogger(PardisBLN.class.getName());

    public NodeContext nodeContext;
    private EventRecord currentRecord;
    private EventRecordService eventRecordService;
    private ChunkRepository chunkRepository;
    private Integer MAX_CHUNK_SIZE;
    private LookupService lookupService;
    private LookupTable actionTable;

//    private

    @Override
    public void init(NodeContext nodeContext) throws Exception {
        try {
            DataSource dataSource = DataSourceFactory.getInstance().getDataSource(
                    "user","","jdbc:timesten:ccacpDS","com.timesten.jdbc.TimesTenDriver");
            Connection connection = dataSource.getConnection();
            this.nodeContext = nodeContext;
            this.MAX_CHUNK_SIZE = nodeContext.getParameterInt("chunkSize");
            this.chunkRepository = new ChunkRepository();
            this.actionTable = lookupService.getTable("SDKServer","QuotaDefinition", true);


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
        int chunkSize = 0;
        if (eventRecord.getFields().contains("actionid")) {
            RtdAction rtdAction = new RtdAction(getFieldStringValue("actionid"));
            if (eventRecord.getFields().contains("msisdn")) {
                Pair<String, Integer> msisdnPair = new Pair<>(this.getFieldStringValue("msisdn"),
                        this.getFieldIntegerValue("count"));
                chunkSize = chunkRepository.addRecord(rtdAction, msisdnPair);
            }
            //else throw error invalid record
        }

        //else throw error invalid record
        if (MAX_CHUNK_SIZE.equals(chunkSize)) {
            EventRecord newRecord = eventRecordService.newRecord();
            newRecord.addField("jsonBody", "set json here or change it to action and msisdn list");
        }

        Field messageField = eventRecord.addField("Message");
        messageField.setValue("greetingText");
        eventRecordService.write("OUT", eventRecord);

    }

    public String getFieldStringValue(String fieldName) {

        try {
            logger.info("Extracting Field " + fieldName);
            Field tmpField = currentRecord.getField(fieldName);
            String value = tmpField.getValue();
            logger.info(" Value " + value);
            return value.toUpperCase();
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

    @Override
    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }
}
