package com.bonyan.rtd.entity;


import java.util.Objects;
import java.util.UUID;

public class RtdAction {
    private String requestId;
    private String actionName;
    private String actionMessageContent;


    public RtdAction() {
    }

    public RtdAction(String actionName) {
        generateNewId();
        this.actionName = actionName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void generateNewId() {
        this.requestId = UUID.randomUUID().toString();
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionMessageContent() {
        return actionMessageContent;
    }

    public void setActionMessageContent(String actionMessageContent) {
        this.actionMessageContent = actionMessageContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RtdAction rtdAction = (RtdAction) o;
        return Objects.equals(actionName, rtdAction.actionName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(actionName);
    }
}

