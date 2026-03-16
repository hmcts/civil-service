package uk.gov.hmcts.reform.civil.client.casedocument.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Date;
import java.util.Map;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
    public Classification classification;

    public long size;

    public String mimeType;

    public String originalDocumentName;

    public Date createdOn;

    public Date modifiedOn;

    public String createdBy;

    public String lastModifiedBy;

    public Date ttl;

    public String hashToken;

    public Map<String, String> metadata;

    @JsonProperty("_links")
    public Links links;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        public Link self;
        public Link binary;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        public String href;
    }
}
