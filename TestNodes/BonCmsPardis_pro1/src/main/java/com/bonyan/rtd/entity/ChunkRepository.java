package com.bonyan.rtd.entity;

import javafx.util.Pair;

import java.util.HashMap;

public class ChunkRepository extends HashMap<RtdAction, RecordList> {

    public boolean hasAction(RtdAction rtdAction) {
        return this.containsKey(rtdAction);
    }

    public void addActionIfNotExist(RtdAction rtdAction) {
        if (!this.hasAction(rtdAction)){
            this.put(rtdAction, new RecordList());
        }
    }

    public void addAction(RtdAction rtdAction) {
        this.put(rtdAction, new RecordList());
    }

    public RecordList getRecords(RtdAction rtdAction) {
        if (!this.hasAction(rtdAction)) {
            this.put(rtdAction, new RecordList());
        }
        return this.get(rtdAction);
    }

    public int addRecord(RtdAction rtdAction, Pair<String, Integer> msisdn) {
        this.get(rtdAction).add(msisdn);
        return this.get(rtdAction).size();
    }

    public String chunkToJsonString(RtdAction rtdAction) {
//        json sample:
//        {
//            "destinationList":
//              [
//                  {
//                      "mobileNo": "121234567"
//                  },
//                  {
//                      "mobileNo": "9121234568"
//                  },
//                  {
//                      "mobileNo": "9121234569"
//                  }
//              ],
//            "message": "Hello World",
//            "smsClass": "NORMAL",
//            "source": "981000"
//        }

        return "{\n" +
                "\"action\" : " + rtdAction + "," +

                "}";
    }
}
