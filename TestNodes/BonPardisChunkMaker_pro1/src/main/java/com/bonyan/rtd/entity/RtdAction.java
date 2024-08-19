package com.bonyan.rtd.entity;


import java.util.Objects;
import java.util.UUID;

public class RtdAction<T> {
    private String requestId;
    private T actionName;
    private String actionMessageContent;


    public RtdAction() {
    }

    public RtdAction(T actionName) {
        generateNewId();
        this.actionName = actionName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void generateNewId() {
        this.requestId = UUID.randomUUID().toString();
    }

    public T getActionName() {
        return actionName;
    }

    public void setActionName(T actionName) {
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
        RtdAction<T> rtdAction = (RtdAction<T>) o;
        return Objects.equals(actionName, rtdAction.actionName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(actionName);
    }
}

