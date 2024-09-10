package com.bonyan.rtd.token;

import java.net.URI;

public class ApiInfo {
    private String requestId;
    private String method;
    private String remoteScheme;
    private String remoteAddress;
    private Integer remotePort;
    private String requestURI;
    private String queryString;
    private URI requestUri;
    private String errMsg;
    private String responseBody;

    public String getRequestId() {
        return requestId;
    }

    public ApiInfo setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public ApiInfo setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getRemoteScheme() {
        return remoteScheme;
    }

    public ApiInfo setRemoteScheme(String remoteScheme) {
        this.remoteScheme = remoteScheme;
        return this;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public ApiInfo setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public ApiInfo setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public ApiInfo setRequestURI(String requestURI) {
        this.requestURI = requestURI;
        return this;
    }

    public String getQueryString() {
        return queryString;
    }

    public ApiInfo setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public URI getRequestUri() {
        return requestUri;
    }

    public ApiInfo setRequestUri(URI requestUri) {
        this.requestUri = requestUri;
        return this;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public ApiInfo setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public ApiInfo setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }
}
