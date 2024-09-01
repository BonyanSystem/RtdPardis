//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.bonyan.rtd.service;

import com.bonyan.rtd.RestClient;
import com.bonyan.rtd.Utils;
import com.bonyan.rtd.token.*;
import com.comptel.mc.node.*;
import com.comptel.mc.node.logging.NodeLoggerFactory;
import com.comptel.mc.node.logging.TxeLogger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JettyClient {
    public static final int SC_OK = 200;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_CLIENT_ERROR = 900;
    public static final int SC_CLIENT_TIMEOUT = 901;
    public static final int SC_CLIENT_START_ERROR = 903;
    public static final String COOKIE_FIELD_PREFIX = "Cookie-";
    public static final String PARAMETER_FIELD_PREFIX = "Parameter-";
    public static final String HEADER_FIELD_PREFIX = "Header-";
    public static final String TOKEN_HEADER_FIELD_PREFIX = "Token-Header-";
    public static final String HEADER_CONTENT_TYPE_FIELD = "Header-Content-Type";
    public static final String HEADER_ACCEPT_TYPE_FIELD = "Header-Accept";
    public static final String HEADER_USER_AGENT_FIELD = "Header-User-Agent";
    private static final String REALM_REGEX = ".realm$";
    private final TxeLogger nodeLogger = NodeLoggerFactory.getNodeLogger(JettyClient.class.getCanonicalName());
    private final Logger logger = Logger.getLogger(JettyClient.class.getName());
    private final RestClient nodeApplication;
    private final int clientRequestTimeout;
    private final int clientResponseContentBufferSize;
    private NodeContext nodeContext;
    private SslContextFactory sslContextFactory;
    private EventRecordService erService;
    private String requestId;
    private String method;
    private String requestURI;
    private URI apiUri;
    private URI tokenUri;
    private String errMsg;
    private String responseBody;
    private String tokenValue;

    public static final String RESTIF010 = "RESTIF010";
    public static final String REJECTED = "REJECTED";
    public static final String HOST_IS = "Host is:";
    public static final String URI_IS = " URI:";
    public static final String PORT_IS = " Port:";
    public static final String REMOTE_SCHEME = "Remote-Scheme";
    public static final String REMOTE_PORT = "Remote-Port";

    public JettyClient(RestClient app) {
        this.nodeApplication = app;
        this.nodeContext = app.getNodeContext();
        this.clientRequestTimeout = app.getClientRequestTimeout();
        this.clientResponseContentBufferSize = app.getClientResponseContentBufferSize();
    }

    public int createRESTClientConnection(EventRecord eventRecord, EventRecordService erService) {
        this.erService = erService;
        this.method = Utils.getErField(eventRecord, "Method");
        this.requestId = Utils.getErField(eventRecord, "Request-Id");
        this.requestURI = Utils.getErField(eventRecord, "Request-Uri");
        try {
            this.apiUri = new URI(this.requestURI.trim());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.nodeApplication.associateTransaction(eventRecord, this.requestId);
        this.errMsg = this.validateInputFields(this.apiUri);
        if (!this.errMsg.isEmpty()) {
            this.nodeLogger.error(this.errMsg);
            eventRecord.reject(REJECTED, this.errMsg);
            this.createRestIfaceErrorER(eventRecord, SC_BAD_REQUEST);
            return SC_BAD_REQUEST;
        } else {
            HttpClient httpClient;
            if (this.apiUri.getScheme().equalsIgnoreCase("https")) {
                this.nodeLogger.info("HTTPS connection requested...");
                httpClient = new HttpClient();
                httpClient.setFollowRedirects(false);
            } else {
                if (!this.apiUri.getScheme().equalsIgnoreCase("http")) {
                    this.errMsg = "Validation error for field: Remote-Scheme, invalid value: " + this.apiUri.getScheme();
                    this.nodeContext.writeMessage(RESTIF010, REMOTE_SCHEME, this.apiUri.getScheme());
                    this.nodeLogger.error(this.errMsg);
                    eventRecord.reject(REJECTED, this.errMsg);
                    this.createRestIfaceErrorER(eventRecord, SC_BAD_REQUEST);
                    return SC_BAD_REQUEST;
                }

                this.nodeLogger.info("HTTP connection requested...");
                httpClient = new HttpClient();
                httpClient.setFollowRedirects(false);
            }

            try {
                httpClient.start();
            } catch (Exception var6) {
                this.errMsg = var6.getMessage();
                this.nodeContext.writeMessage("RESTIF012", httpClient.getState(), this.errMsg);
                this.nodeLogger.error("Unable to initialize client connection.:" + httpClient.getState() + ";" + this.errMsg);
                eventRecord.reject(REJECTED, this.errMsg);
                this.createRestIfaceErrorER(eventRecord, SC_CLIENT_START_ERROR);
                return SC_CLIENT_START_ERROR;
            }

            int statusCode = this.createAndSendHttpRequest(httpClient, eventRecord, this.apiUri, this.nodeApplication.isUseToken());
            this.nodeLogger.info("http Client start..with state END:" + httpClient.getState());
            return statusCode;
        }
    }

    public Token getToken(EventRecord er) {
        TokenAttributes tokenAttributes = new TokenAttributes(getTokenValue(er));
        tokenAttributes.setTokenDurationType(this.nodeApplication.getTokenDurationType());
        tokenAttributes.setExpirationDuration(this.nodeApplication.getExpirationDuration());
        tokenAttributes.setRenewalMarginPercentage(this.nodeApplication.getRenewalMarginPercentage());

        return TokenFactory.buildToken(this.nodeApplication.getTokenType(),tokenAttributes);
    }

    public String getTokenValue(EventRecord eventRecord) {
        String token = "";

        if (this.nodeApplication.isUseToken()) {
            this.nodeLogger.info("getToken...");
            this.method = this.nodeApplication.getTokenApiInfo().getMethod();
            this.nodeLogger.info("1.4getToken..." + this.method);
            this.requestId = this.nodeApplication.getTokenApiInfo().getRequestId();
            this.requestURI = this.nodeApplication.getTokenApiInfo().getRequestURI();
            try {
                this.tokenUri = new URI(this.requestURI);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            this.errMsg = this.validateInputFields(this.tokenUri);
            if (!this.errMsg.isEmpty()) {
                this.nodeLogger.error(this.errMsg);
                eventRecord.reject(REJECTED, this.errMsg);
                this.createRestIfaceErrorER(eventRecord, SC_BAD_REQUEST);
                return token;
            } else {
                HttpClient httpClient;
                if (this.tokenUri.getScheme().equalsIgnoreCase("https")) {
                    this.nodeLogger.info("HTTPS connection requested...");
                    httpClient = new HttpClient(this.sslContextFactory);
                    httpClient.setFollowRedirects(false);
                } else {
                    if (!this.tokenUri.getScheme().equalsIgnoreCase("http")) {
                        this.errMsg = "Validation error for field: Remote-Scheme, invalid value: " + this.tokenUri.getScheme();
                        this.nodeContext.writeMessage(RESTIF010, REMOTE_SCHEME, this.tokenUri.getScheme());
                        this.nodeLogger.error(this.errMsg);
                        eventRecord.reject(REJECTED, this.errMsg);
                        this.createRestIfaceErrorER(eventRecord, SC_BAD_REQUEST);
                        return token;
                    }

                    this.nodeLogger.info("HTTP connection requested...");
                    httpClient = new HttpClient();
                    httpClient.setFollowRedirects(false);
                }

                try {
                    httpClient.start();
                } catch (Exception var5) {
                    Exception e = var5;
                    this.errMsg = e.getMessage();
                    this.nodeContext.writeMessage("RESTIF012", httpClient.getState(), this.errMsg);
                    this.nodeLogger.error("Unable to initialize client connection.:" + httpClient.getState() + ";" + this.errMsg);
                    eventRecord.reject(REJECTED, this.errMsg);
                    this.createRestIfaceErrorER(eventRecord, SC_CLIENT_START_ERROR);
                    return token;
                }

                if (this.tokenUri == null) {
                    this.nodeLogger.error("tokenUri is null");
                } else if (this.tokenUri.getQuery() == null) {
                    this.nodeLogger.error("tokenUri.getQuery() is null");
                } else {
                    this.nodeLogger.warn("token uri: " + this.tokenUri);
                    this.nodeLogger.warn("token uri query: " + this.tokenUri.getQuery());
                }
                token = this.createAndSendHttpRequestGetToken(httpClient, eventRecord, this.tokenUri, false);
                this.nodeLogger.info("http Client start..with state END:" + token);
                return token;
            }
        }
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImNhdGNyeSIsImV4cCI6MTcyODQzNzMxOH0" +
                ".PZmvrcM238NtuWeKsTauA5VrJsxDGRMGnEUeQZL1YyI";
    }

    private String validateInputFields(URI uri) {
        this.nodeLogger.debug("validateInputFields(): entered the function...");
        String errorMessage;
        if (uri == null) {
            errorMessage = "Validation error uri: uri is null";
            this.nodeContext.writeMessage(RESTIF010, "Request_Uri", "null");
            return errorMessage;
        }
        if (uri.getPort() > 65535) {
            errorMessage = "Validation error for field: Remote-Port, invalid value: " + uri.getPort();
            this.nodeContext.writeMessage(RESTIF010, REMOTE_PORT, String.valueOf(uri.getPort()));
            return errorMessage;
        } else {
            if (uri.getPort() == 0) {
                this.nodeLogger.info("Remote-Port empty or missing, setting to -1");
            }

            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                errorMessage = "Validation error for field: Remote-Address, invalid value: " + uri;
                this.nodeContext.writeMessage(RESTIF010, "Remote-Address", uri.toString());
                return errorMessage;
            } else if (!this.validateHttpMethod(this.method)) {
                errorMessage = "Validation error for field: Method, invalid value: " + this.method;
                this.nodeContext.writeMessage(RESTIF010, "Method", this.method);
                return errorMessage;
            } else {
                try {
                    uri = new URI(uri.toString());
                    this.nodeLogger.info("URL string is:" + uri);
                    return "";
                } catch (URISyntaxException syntaxException) {
                    errorMessage = "URI building error: " + syntaxException.getMessage();
                    this.nodeContext.writeMessage("RESTIF011", syntaxException.getMessage());
                    return errorMessage;
                }
            }
        }
    }

    private boolean validateHttpMethod(String method) {
        return method != null && !method.isEmpty() && method.toUpperCase().matches("GET|PUT|POST|DELETE|CONNECT|HEAD");
    }

    public int createAndSendHttpRequest(HttpClient httpClient, EventRecord eventRecord, URI uri, boolean authFlag) {
        AtomicReference<Result> resultRef = new AtomicReference<>();
        Request request = httpClient.newRequest(uri);
        this.nodeLogger.info(HOST_IS + request.getHost() + PORT_IS + request.getPort() + URI_IS + request.getURI());
        request.method(this.method);
        request.version(HttpVersion.HTTP_1_1);
        this.setParamsHeadersCookies(eventRecord, httpClient, request);
        request.timeout(this.clientRequestTimeout, TimeUnit.MILLISECONDS);
        int statusCode = this.sendHttpRequest(request, resultRef, eventRecord);
        return statusCode != SC_OK ? statusCode : this.processRequestResult(httpClient, resultRef, eventRecord, authFlag);
    }

    public String createAndSendHttpRequestGetToken(HttpClient httpClient, EventRecord eventRecord, URI uri, boolean authFlag) {
        AtomicReference<Result> resultRef = new AtomicReference<>();

        nodeLogger.warn("uri: " + uri);
        nodeLogger.warn("uri query: " + uri.getQuery());
        Request request = httpClient.newRequest(uri);
        nodeLogger.warn("request: " + request);

        if (authFlag) {
            logger.fine(" authFlag is true");
        }
        this.nodeLogger.info(HOST_IS + request.getHost() + PORT_IS + request.getPort() + URI_IS + request.getURI());
        request.method(this.method);
        request.version(HttpVersion.HTTP_1_1);
        this.setParamsHeadersToken(eventRecord, httpClient, request);
        request.timeout(this.clientRequestTimeout, TimeUnit.MILLISECONDS);
        this.nodeLogger.info(HOST_IS + request.getHost() + PORT_IS + request.getPort() + URI_IS + request.getURI());
        this.nodeLogger.warn(HOST_IS + request.getHost() + PORT_IS + request.getPort() + URI_IS + request.getURI());
        this.nodeLogger.warn("request: " + request);
        this.nodeLogger.warn("eventRecord " + eventRecord);
        int statusCode = this.sendHttpRequest(request, resultRef, eventRecord);
        this.nodeLogger.warn("statusCode: " + statusCode);
        this.closeClientConnectionResult(httpClient, eventRecord, statusCode);
        this.nodeLogger.warn("eventRecord after api call: " + eventRecord);
        Field responseBlock = eventRecord.getField("Response");
        if (statusCode!=SC_OK || responseBlock==null || responseBlock.getField("ResponseBody")==null){
            return "";
        } else {
            String tokenApiResponse = responseBlock.getField("ResponseBody").getValue();
            Map<String, String> tokenResponseMap = parseResponseToJsonMap(this.responseBody);
            this.tokenValue = tokenResponseMap.get("access_token");
            return this.tokenValue;
        }
    }

    public void setParamsHeadersCookies(EventRecord eventRecord, HttpClient httpClient, Request request) {
        String body = Utils.getErField(eventRecord, "Body");
        CookieStore cookieStore = httpClient.getCookieStore();
        List<Field> fields = eventRecord.getFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldValue = field.getValue();

            if (fieldName.startsWith(PARAMETER_FIELD_PREFIX)) {
                handleParameterField(request, fieldName, fieldValue);
            } else if (fieldName.startsWith(COOKIE_FIELD_PREFIX)) {
                handleCookieField(cookieStore, fieldName, fieldValue);
            } else if (fieldName.startsWith(HEADER_FIELD_PREFIX)) {
                handleHeaderField(request, fieldName, fieldValue, body);
            }
        }

        httpClient.setCookieStore(cookieStore);
    }

    private void handleParameterField(Request request, String fieldName, String fieldValue) {
        String headerName = fieldName.substring(PARAMETER_FIELD_PREFIX.length());
        request.param(headerName, fieldValue);
        nodeLogger.debug("request.param --><" + headerName + "," + fieldValue + ">");
    }

    private void handleCookieField(CookieStore cookieStore, String fieldName, String fieldValue) {
        String headerName = fieldName.substring(COOKIE_FIELD_PREFIX.length());
        HttpCookie cookie = new HttpCookie(headerName, fieldValue);
        cookieStore.add(tokenUri, cookie);
        nodeLogger.debug("Cookie --><" + headerName + "," + fieldValue + ">");
    }

    private void handleHeaderField(Request request, String fieldName, String fieldValue, String body) {
        String headerName = fieldName.substring(HEADER_FIELD_PREFIX.length());
        request.header(headerName, fieldValue);
        nodeLogger.debug("Header --><" + headerName + "," + fieldValue + ">");

        if ("Content-Type".equals(headerName)) {
            handleContentType(request, fieldValue, body);
        } else if ("Accept".equals(headerName)) {
            request.accept(fieldValue);
            nodeLogger.debug("Accept: " + fieldValue);
        }
    }

    private void handleContentType(Request request, String fieldValue, String body) {
        if (fieldValue != null && body != null) {
            try {
                nodeLogger.info("setting body");
                request.content(new BytesContentProvider(body.getBytes(StandardCharsets.UTF_8)), fieldValue);
                nodeLogger.info("Client with url:" + tokenUri + " Body " + fieldValue + " contents: " + body);
            } catch (Exception ex) {
                nodeLogger.info("exception " + ex.getMessage());
                request.content(new BytesContentProvider(body.getBytes()), fieldValue);
                nodeLogger.info("Client with url:" + tokenUri + " Body " + fieldValue + " contents: " + body);
            }
        }
    }

    public void setParamsHeadersToken(EventRecord eventRecord, HttpClient httpClient, Request request) {
        CookieStore cookieStore = httpClient.getCookieStore();
        List<Field> fields = eventRecord.getFields();
        Iterator<Field> iterator = fields.iterator();

        while (iterator.hasNext()) {
            Field field = iterator.next();
            String fieldName = field.getName();
            String fieldValue = field.getValue();
            String headerName;
            if (fieldName.startsWith(PARAMETER_FIELD_PREFIX)) {
                headerName = fieldName.substring(PARAMETER_FIELD_PREFIX.length());
                request.param(headerName, fieldValue);
                this.nodeLogger.debug("request.param --><" + headerName + "," + fieldValue + ">");
            }

            if (fieldName.startsWith(COOKIE_FIELD_PREFIX)) {
                headerName = fieldName.substring(COOKIE_FIELD_PREFIX.length());
                HttpCookie cookie = new HttpCookie(headerName, fieldValue);
                cookieStore.add(this.tokenUri, cookie);
                this.nodeLogger.debug("Cookie --><" + headerName + "," + fieldValue + ">");
            }

            if (fieldName.startsWith(TOKEN_HEADER_FIELD_PREFIX)) {
                headerName = fieldName.substring(TOKEN_HEADER_FIELD_PREFIX.length());
                request.header(headerName, fieldValue);
                this.nodeLogger.debug("Header --><" + headerName + "," + fieldValue + ">");
                if (headerName.equals("Accept") && fieldValue != null) {
                    request.accept(fieldValue);
                    this.nodeLogger.debug("Accept: " + fieldValue);
                }
            }
        }

        httpClient.setCookieStore(cookieStore);
    }

    private int sendHttpRequest(Request request, final AtomicReference<Result> resultRef, EventRecord eventRecord) {
        final CountDownLatch latch = new CountDownLatch(1);
        boolean responseReceived;
        int statusCode = SC_OK;
        request.send(new BufferingResponseListener(this.clientResponseContentBufferSize) {
            public void onComplete(Result result) {
                JettyClient.this.nodeLogger.info("Network work ready. Processing results...");
                resultRef.set(result);
                if (result.isSucceeded()) {
                    JettyClient.this.nodeLogger.info("result: Succeeded");
                    JettyClient.this.responseBody = this.getContentAsString();
                    JettyClient.this.nodeLogger.info("Response body :" + JettyClient.this.responseBody);
                }

                latch.countDown();
            }
        });

        try {
            responseReceived = latch.await(this.clientRequestTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            // Restore the interrupted status of the thread
            Thread.currentThread().interrupt();

            // Handle the exception and ensure null checks
            this.errMsg = exception.getMessage();
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR_500; // Default to internal server error

            // Check if resultRef or its response is null before accessing
            if (resultRef.get() != null && resultRef.get().getResponse() != null) {
                statusCode = resultRef.get().getResponse().getStatus();
            }

            // Log the error messages appropriately
            this.nodeContext.writeMessage("RESTIF100", this.errMsg);
            logger.log(Level.WARNING, "Error in latch.await InterruptedException: {0}", this.errMsg);
            this.nodeLogger.warn("Error in latch.await InterruptedException: {}", this.errMsg);

            // Create error interface and return the status code
            this.createRestIfaceErrorER(eventRecord, statusCode);
            return statusCode;
        }

        if (!responseReceived) {
            this.errMsg = "Total timeout elapsed.";
            statusCode = SC_CLIENT_TIMEOUT;
            this.nodeContext.writeMessage("RESTIF109");
            this.nodeLogger.error(this.errMsg);
            this.createRestIfaceErrorER(eventRecord, statusCode);
        }
        Field responseBlock = eventRecord.addField("Response");
        responseBlock.addField("ResponseBody", this.responseBody);
        return statusCode;
    }

    private int processRequestResult(HttpClient httpClient, AtomicReference<Result> resultRef, EventRecord eventRecord, boolean authFlag) {
        Result result = resultRef.get();
        Response response = result.getResponse();
        int statusCode = response.getStatus();
        this.nodeLogger.info("HTTP status code: " + statusCode + " : " + response.getReason());
        HttpFields responseHeaders = response.getHeaders();
        if (!authFlag && statusCode == SC_UNAUTHORIZED) {
            return this.sendAuthorizationRequest(httpClient, eventRecord, this.apiUri, responseHeaders);
        } else if (!result.isFailed()) {
            this.createRestIfaceER(httpClient, response, eventRecord);
            return this.closeClientConnectionResult(httpClient, eventRecord, statusCode);
        } else {
            this.nodeLogger.info("result: NOT Succeeded!");
            Throwable failure = result.getFailure();
            this.nodeLogger.debug("failure.getMessage(): " + failure.getMessage());
            this.nodeLogger.debug("response.getReason(): " + response.getReason());
            this.errMsg = response.getReason();
            if (this.errMsg == null || this.errMsg.isEmpty()) {
                this.nodeLogger.debug("errMsg is empty: " + this.errMsg);
                this.errMsg = failure.getMessage();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                failure.printStackTrace(pw);
                this.nodeLogger.warn(failure, pw.toString());
            }

            if (statusCode >= SC_OK && statusCode < SC_BAD_REQUEST) {
                statusCode = SC_CLIENT_ERROR;
                this.nodeLogger.info("Status code changed: " + statusCode);
                this.errMsg = failure.getMessage();
            }

            this.nodeContext.writeMessage(MessageType.WARNING, this.errMsg);
            this.nodeLogger.error(this.errMsg);
//            eventRecord.reject(REJECTED, this.errMsg);
            this.createRestIfaceErrorER(eventRecord, statusCode);
            return statusCode;
        }
    }

    private int sendAuthorizationRequest(HttpClient httpClient, EventRecord er, URI uri, HttpFields responseHeaders) {
        String realm = "";
        String user = Utils.getErField(er, "User-Id");
        String authentication = Utils.getHttpField(responseHeaders, HttpHeader.WWW_AUTHENTICATE.asString());
        this.nodeLogger.info("http client:" + httpClient + ">");
        this.nodeLogger.info("uri:" + uri + ">");
        this.nodeLogger.info("Header :<Header-WWW-Authenticate -- Value:" + authentication + ">");
        String[] authenticationArray = authentication.split("=");
        if (authenticationArray[0].matches(REALM_REGEX)) {
            realm = authenticationArray[1].replace("\"", "").split(",")[0];
            this.nodeLogger.debug("The extracted realm from the ER is " + realm);
        }

        if (!realm.isEmpty() && !user.isEmpty()) {

            this.errMsg = "User Credentials cannot be found in from storage";
            this.nodeContext.writeMessage("RESTIF105");
            this.nodeLogger.warn(this.errMsg);
            er.reject(REJECTED, this.errMsg);
            this.createRestIfaceErrorER(er, SC_UNAUTHORIZED);
            return SC_UNAUTHORIZED;
        } else {
//            this.errMsg = "Realm or UserId field missing in input record";
            this.errMsg = "Realm or UserId field missing in input record tokenUri:" + this.tokenUri + ", token query:" + tokenUri.getQuery();
            this.nodeContext.writeMessage("RESTIF104");
            this.nodeLogger.warn("Missing ER Realm or UserId field!");
//            er.reject(REJECTED, this.errMsg);
            this.createRestIfaceErrorER(er, SC_UNAUTHORIZED);
            return SC_UNAUTHORIZED;
        }
    }

    private int closeClientConnectionResult(HttpClient httpClient, EventRecord eventRecord, int statusCode) {
        this.nodeLogger.info("closeClientConnection(): Entered the function...");

        try {
            if (httpClient != null) {
                httpClient.stop();
                httpClient.destroy();
                this.nodeLogger.info("Connection closed successful with state:" + httpClient.getState());
            } else {
                this.nodeLogger.info("Connection was null to httpClient");
            }

            return statusCode;
        } catch (Exception var5) {
            Exception ex = var5;
            this.errMsg = ex.getMessage();
            this.nodeContext.writeMessage("RESTIF013", ex.getLocalizedMessage(), this.errMsg);
            this.nodeLogger.error("Client cleanup failed: " + ex.getLocalizedMessage() + ";" + this.errMsg);
            eventRecord.reject(REJECTED, this.errMsg);
            this.createRestIfaceErrorER(eventRecord, SC_BAD_REQUEST);
            return SC_BAD_REQUEST;
        }
    }

    private void createRestIfaceErrorER(EventRecord inputRecord, int status) {
        this.nodeLogger.info("Write to ER_OUTPUT_LINK link Error er");
        EventRecord errorER = (EventRecord) inputRecord.copy();
        errorER.setInputType("REST_IFACE_ERROR");
        errorER.setOutputType("REST_IFACE_ERROR");
        Field request = errorER.getField("Request");
        request.setValue("false");
        errorER.addField("Local-Address", this.apiUri.getHost());
        errorER.addField("Local-URI", this.apiUri.toString());
        errorER.addField("Local-Port", Integer.toString(this.apiUri.getPort()));
        errorER.addField("Error-Status", Integer.toString(status));
        errorER.addField("Error-Description", this.errMsg);

        Field tokenUriBlock = errorER.addField("TokenUri");

        tokenUriBlock.addField("Token-Uri", this.tokenUri.toString());
        tokenUriBlock.addField("Token-Query", this.tokenUri.getQuery());
        tokenUriBlock.addField("Token-Scheme", this.tokenUri.getScheme());
        tokenUriBlock.addField("Token-RawPath", this.tokenUri.getRawPath());
        tokenUriBlock.addField("Token-Path", this.tokenUri.getPath());
        tokenUriBlock.addField("Token-Host", this.tokenUri.getHost());
        tokenUriBlock.addField("Token-Authority", this.tokenUri.getAuthority());
        tokenUriBlock.addField("Token-Port", String.valueOf(this.tokenUri.getPort()));
        Map<String, String> responseBodyMap = parseResponseToJsonMap(this.responseBody);
        for (Map.Entry<String, String> entry : responseBodyMap.entrySet()) {
            tokenUriBlock.addField("Token_" + entry.getKey(), entry.getValue());
        }
        tokenUriBlock.addField("Token-Value", this.tokenValue);
        tokenUriBlock.addField("Response_Body", this.responseBody);
        this.nodeApplication.txRebindAndInherit(errorER);


        this.erService.write("INTERFACE_OUT", errorER);
        errorER.reject(REJECTED, this.errMsg);
        this.nodeLogger.info("done Write to ER_OUTPUT_LINK link Error");
        this.errMsg = "";
    }

    private void createRestIfaceER(HttpClient httpClient, Response response, EventRecord requestRecord) {
        this.nodeLogger.info("Write to ER_OUTPUT_LINK Record type REST_IFACE");
        EventRecord restIfaceER = (EventRecord) requestRecord.copy();
        restIfaceER.setInputType("REST_IFACE");
        restIfaceER.setOutputType("REST_IFACE");
        restIfaceER.addField("Request", "False");
        restIfaceER.addField("Request-Id", this.requestId);
        Field responseBlock = restIfaceER.getField("Response");
        Request request = response.getRequest();

        if (request.getURI() != null) {
            responseBlock.addField("Request-Uri", request.getURI().toString());
        }

        this.nodeLogger.info("Response header fields:");
        HttpFields headers = response.getHeaders();

        for (int i = 0; i < headers.size(); ++i) {
            HttpField header = headers.getField(i);
            String value = header.getValue();
            String name = header.getName();
            responseBlock.addField(HEADER_FIELD_PREFIX + name, value);
            this.nodeLogger.info("Header :<Header-" + name + " -- Value:" + value + ">");
        }

        this.nodeLogger.info("http cookies are:");
        List<HttpCookie> cookies = httpClient.getCookieStore().getCookies();

        for (HttpCookie cookie : cookies) {
            responseBlock.addField(COOKIE_FIELD_PREFIX + cookie.getName(), cookie.getValue());
            this.nodeLogger.info("HttpCookie name:" + cookie.getName() + " value:" + cookie.getValue());
        }

        responseBlock.addField("Body", this.responseBody);
        responseBlock.addField("Status", Integer.toString(response.getStatus()));
        this.nodeApplication.txRebindAndInherit(restIfaceER);

        this.erService.write("INTERFACE_OUT", restIfaceER);
        this.nodeLogger.info("done Write to INTERFACE_OUT link");
    }

    public enum UAFlags {
        BASIC,
        DIGEST,
        NONE;
    }

    public Map<String, String> parseResponseToJsonMap(String responseBody) {
        Map<String, String> map = new HashMap<>();
        String[] responseBodyParts = responseBody.trim().replace("{","")
                .replace("}","").split(",");
        for (String responseBodyPart : responseBodyParts) {
            String[] keyValue = responseBodyPart.trim().replace("\"", "").split(":", 2);
            map.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return map;
    }
}
