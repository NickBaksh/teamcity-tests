package com.teamcity.core.client;

public enum RequestType {
    JSON(ContentType.JSON, ContentType.JSON),
    TEXT(ContentType.TEXT_PLAIN, ContentType.TEXT_PLAIN),
    XML(ContentType.XML, ContentType.XML),
    JSON_ACCEPT_TEXT(ContentType.JSON, ContentType.TEXT_PLAIN),
    TEXT_ACCEPT_JSON(ContentType.TEXT_PLAIN, ContentType.JSON),
    JSON_ACCEPT_XML(ContentType.JSON, ContentType.XML),
    XML_ACCEPT_JSON(ContentType.XML, ContentType.JSON);

    private final ContentType contentType;
    private final ContentType accept;

    RequestType(ContentType contentType, ContentType accept) {
        this.contentType = contentType;
        this.accept = accept;
    }

    public String getContentType() {
        return contentType.getValue();
    }

    public String getAccept() {
        return accept.getValue();
    }

    public ContentType getContentTypeEnum() {
        return contentType;
    }

    public ContentType getAcceptEnum() {
        return accept;
    }

    public HeaderConfig toHeaderConfig() {
        return HeaderConfig.defaultHeaders()
                .withContentType(this.contentType)
                .withAccept(this.accept);
    }

    public boolean isJson() {
        return contentType.isJson() || accept.isJson();
    }

    public boolean isText() {
        return contentType.isText() || accept.isText();
    }

    public boolean isXml() {
        return contentType.isXml() || accept.isXml();
    }

    public static RequestType forTextUpdate() {
        return TEXT;
    }

    public static RequestType forJson() {
        return JSON;
    }
}