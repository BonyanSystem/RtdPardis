package com.bonyan.rtd;

import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;
import com.comptel.mc.node.lookup.LookupService;
import com.comptel.mc.node.lookup.LookupServiceUser;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PardisRequestMaker extends Nodebase implements BusinessLogic, Schedulable, TimerObserver, LookupServiceUser {


    private LookupService lookupService;
    @Override
    public void setLookupService(LookupService service) {
            lookupService = service;
    }

    @Override
    public void init(NodeContext nodeContext) throws Exception {

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
    public void process(EventRecord eventRecord) throws Exception {

    }

    @Override
    public void setService(EventRecordService eventRecordService) {

    }

    @Override
    public void schedule() throws Exception {

    }

    @Override
    public void timer() throws Exception {

    }


}
