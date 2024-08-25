package com.bonyan.rtd;

import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.comptel.mc.node.lookup.*;
import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;

import java.util.List;
import java.util.logging.Logger;

public class PardisRequestBuilder extends Nodebase implements BusinessLogic, Schedulable, TimerObserver, LookupServiceUser {

    private static final Logger logger = Logger.getLogger(PardisRequestBuilder.class.getName());
    private static final TxeLogger nodeLogger = NodeLoggerFactory.getNodeLogger(PardisRequestBuilder.class.getCanonicalName());
    public EventRecordService erService;
    private String lookupServerName;
    private String lookupTableName;
    private LookupService lookupService;
    private LookupTable contentTable;
    private EventRecord newOutputRecord;

    @Override
    public void setLookupService(LookupService service) {
        lookupService = service;
    }

    @Override
    public void init(NodeContext nodeContext) throws Exception {


        lookupServerName = nodeContext.getParameter("Lookup_Server");
        lookupTableName = nodeContext.getParameter("Lookup_Table");
        contentTable = lookupService.getTable(lookupServerName, lookupTableName,true);



    }

    @Override
    public void flush() throws Exception {
        nodeLogger.info("nodeLogger: node flush start");
    }

    @Override
    public void end() throws Exception {
        nodeLogger.info("nodeLogger: node 'end' start");
    }

    @Override
    public void request(String s) throws Exception {
        nodeLogger.info("nodeLogger: node request start, input String: " + s);
    }

    @Override
    public void pause(int i) throws Exception {
        nodeLogger.info("nodeLogger: pause method, input int: " + i);
    }

    @Override
    public void resume(int i) throws Exception {
        nodeLogger.info("nodeLogger: resume method, input int: " + i);

    }

    @Override
    public void process(EventRecord eventRecord) throws Exception {
        Field actionBlock = eventRecord.getField("Action");
        Field action_id_field = actionBlock.getField("action_id");
        String action_id = action_id_field.getValue();

        try {
            List<NormalLookupResultItem> result = contentTable.lookup(3,2,1,action_id,"Pardis");

            if (result == null || result.isEmpty()) {
                throw new RuntimeException("Record not found in the lookup table: " + contentTable.getName());

            }
            String content_id = result.get(0).getReturnValues().get(2);
            newOutputRecord = this.erService.newRecord();
            newOutputRecord.addField("content_id",content_id);
            erService.write("OUT",newOutputRecord);

        } catch (RuntimeException e) {
            nodeLogger.error("Record not found in the lookup table: " + contentTable.getName());
            eventRecord.reject("LOOKUP_ERROR","Lookup table record not found.");
        }

    }

    @Override
    public void setService(EventRecordService eventRecordService) {
        this.erService = eventRecordService;
    }

    @Override
    public void schedule() throws Exception {

    }

    @Override
    public void timer() throws Exception {

    }



}
