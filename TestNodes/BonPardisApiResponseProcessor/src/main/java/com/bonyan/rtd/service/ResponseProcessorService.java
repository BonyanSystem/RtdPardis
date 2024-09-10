package com.bonyan.rtd.service;

import com.bonyan.rtd.entity.Chunk;
import com.bonyan.rtd.utils.JsonParser;
import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.EventRecord;
import com.comptel.mc.node.EventRecordService;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class ResponseProcessorService extends Nodebase {
    private EventRecordService eventRecordService;

    public ResponseProcessorService(EventRecordService eventRecordService) {
        this.eventRecordService = eventRecordService;
    }

    public void processResponseRecord(EventRecord eventRecord) {
        Chunk<String> chunk = new Chunk<>();
        i_begin();

        i_next("Action");
        i_enter();
        String actionId = i_get("action_id");
        String contentId = i_get("content_id");
        chunk.getRtdAction().setActionId(actionId);
        chunk.setContentId(contentId);
        chunk.getRtdAction().setContentId(contentId);
        i_exit();

        i_next("ChunkList");
        i_enter();
        for (int i = 1; i_next("Sub_" + i) > 0; i++) {
            i_enter();
            String msisdn = i_get("msisdn");
            Integer retryCount = Integer.valueOf(i_get("retry_count"));
            chunk.getRecords().add(new AbstractMap.SimpleEntry<>(msisdn, retryCount));
            i_exit();
        }
        i_exit();

        i_next("Response");
        i_enter();
        if (i_field_exists("Status") > 0) {
            String status = i_get("Status");
            String errorMsg = i_get("Body");
            chunk.setStatus(Integer.valueOf(status));
            chunk.setErrorMessage(errorMsg);
        } else if (i_field_exists("ResponseBody") > 0) {
            String responseBody = i_get("ResponseBody");
            chunk.setResponseBody(responseBody);
            List<String> smsIds = JsonParser.getJsonValues(responseBody);
            chunk.getSmsIds().addAll(smsIds);
            chunk.setStatus(200);
        } else {
            i_reject("REJECTED","invalid input");
        }
        i_exit();
        i_end();
        buildNewRecords(chunk);
    }

    public void buildNewRecords(Chunk<String> chunk) {
        EventRecord tempRecord = eventRecordService.newRecord();
        tempRecord.addField("action_id", chunk.getRtdAction().getActionId());
        tempRecord.addField("content_id", chunk.getRtdAction().getContentId());

        if (!chunk.getStatus().equals(200)) {
            tempRecord.addField("error_msg", chunk.getErrorMessage());
            tempRecord.addField("error_status", chunk.getStatus().toString());
            for(Map.Entry<String, Integer> msisdnPair: chunk.getRecords()) {
                EventRecord newRecord = (EventRecord) tempRecord.copy();
                newRecord.addField("msisdn", msisdnPair.getKey());
                newRecord.addField("retry_count", msisdnPair.getValue().toString());
                eventRecordService.write("OUT", newRecord);
            }
        } else {
//            for(int i = 0; i < chunk.getSmsIds().size(); i++) {
//                String smsId = chunk.getSmsIds().get(i);
//                if (isNumeric(smsId)) {
//                    int errorCode = Integer.parseInt(smsId);
//                    if (errorCode > 0 && errorCode < 255) {
//                        Map.Entry<String, Integer> msisdnPair = chunk.getRecords().get(i);
            EventRecord newRecord = (EventRecord) tempRecord.copy();
            newRecord.addField("error_msg", "pardis api could not send sms to this msisdn");
//                        newRecord.addField("error_status", smsId);
            newRecord.addField("response_body", chunk.getResponseBody());
            newRecord.addField("smsIds", String.valueOf(chunk.getSmsIds()));
            newRecord.addField("smsIds_toString", chunk.getSmsIds().toString());
//                        newRecord.addField("error_status", Integer.toString(errorCode));
//                        newRecord.addField("msisdn", msisdnPair.getKey());
//                        newRecord.addField("retry_count", msisdnPair.getValue().toString());
            eventRecordService.write("OUT", newRecord);
//                    }
//                }
        }
    }

    public static boolean isNumeric(String input) {
        return input.matches("-?\\d+");
    }

}
