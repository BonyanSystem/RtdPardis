package com.bonyan.rtd.utils;

import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.EventRecord;
import com.comptel.mc.node.Field;

public class NodeRecordUtil extends Nodebase {

    public static String getFieldStringValue(EventRecord eventRecord, String fieldName) {
        try {
            Field tmpField = eventRecord.getField(fieldName);
            String value = tmpField.getValue();
            return value;
        } catch (Exception ex) {
            nb_trace("Cant parse " + fieldName, 1);
            return "";
        }
    }

    public static Integer getFieldIntegerValue(EventRecord eventRecord, String fieldName) {
        try {
            Field tmpField = eventRecord.getField(fieldName);
            Integer value = Integer.valueOf(tmpField.getValue());
            return value;
        } catch (Exception ex) {
            nb_trace("Cant parse " + fieldName, 1);
            return 0;
        }
    }
}
