package com.bonyan.rtd;

import com.bonyan.rtd.service.ChunkerService;
import com.comptel.eventlink.core.Nodebase;
import com.comptel.mc.node.*;
import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;

import java.util.logging.Logger;

public class PardisChunkMakerNode extends Nodebase implements BusinessLogic, Schedulable ,TimerObserver {

    private static final Logger logger = Logger.getLogger(PardisChunkMakerNode.class.getName());
    private static final TxeLogger nodeLogger = NodeLoggerFactory.getNodeLogger(PardisChunkMakerNode.class.getCanonicalName());
    private NodeContext nodeContext;
    private ChunkerService chunkerService;
    private PardisChunkMakerNode.NodeParameters nodeParams;

    @Override
    public void init(NodeContext nodeContext) {
        try {
            nodeLogger.info("nodeLogger: node init start");

            this.nodeContext = nodeContext;
            loadNodeParameters();

            nodeLogger.info("nodeLogger: node init end");
        } catch (Exception ex) {
            nodeLogger.info("nodeLogger: Exception in Init method: " + ex.getMessage());
        }
    }

    private void loadNodeParameters() {
        this.nodeParams = new NodeParameters();
        nodeLogger.info("nodeLogger: nodeContext.getParameters:" + nodeContext.getParameters());
    }

    @Override
    public void process(EventRecord eventRecord) {
        boolean successfulAdd = chunkerService.addRecord(eventRecord);
        if (!successfulAdd) {
            nb_reject("REJECTED", "limited retry count overed");
        }
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
    public void request(String s) {
        nodeLogger.info("nodeLogger: node request start, input String: " + s);

    }

    @Override
    public void pause(int i) {
        nodeLogger.info("nodeLogger: pause method, input int: " + i);

    }

    @Override
    public void resume(int i) {
        nodeLogger.info("nodeLogger: resume method, input int: " + i);

    }

    @Override
    public void setService(EventRecordService eventRecordService) {
        nodeLogger.info("nodeLogger: node setService method start");
        this.chunkerService = new ChunkerService(eventRecordService, nodeParams);
    }

    @Override
    public void schedule() {
        nodeLogger.info("nodeLogger: node schedule start");

    }
    @Override
    public void timer() throws Exception {
        chunkerService.makeAndWriteChunkRecords();
    }

    public class NodeParameters {
        private Integer maxChunkSizeParam;
        private Integer maxSendRetryCount;
        private Integer maxUntouchedRecordCount;

        public NodeParameters() {
            this.maxChunkSizeParam = nodeContext.getParameterInt("chunker-max-size");
            this.maxSendRetryCount = nodeContext.getParameterInt("max-retry-count");
            this.maxUntouchedRecordCount = nodeContext.getParameterInt("max-untouched-record-count");
        }

        public Integer getMaxChunkSizeParam() {
            return maxChunkSizeParam;
        }

        public Integer getMaxSendRetryCount() {
            return maxSendRetryCount;
        }

        public Integer getMaxUntouchedRecordCount() {
            return maxUntouchedRecordCount;
        }
    }

}
