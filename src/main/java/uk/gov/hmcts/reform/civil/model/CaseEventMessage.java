package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageState;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("PMD.ExcessiveParameterList")
public class CaseEventMessage implements Serializable {

    private static final long serialVersionUID = 3213665975741833471L;

    private String messageId;
    private Long sequence;
    private String caseId;
    private LocalDateTime eventTimestamp;
    private Boolean fromDlq;
    private MessageState state;
    private JsonNode messageProperties;
    private String messageContent;
    private LocalDateTime received;
    private Integer deliveryCount;
    private LocalDateTime holdUntil;
    private Integer retryCount;

    public String getMessageId() {
        return messageId;
    }

    public Long getSequence() {
        return sequence;
    }

    public String getCaseId() {
        return caseId;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public Boolean getFromDlq() {
        return fromDlq;
    }

    public MessageState getState() {
        return state;
    }

    public JsonNode getMessageProperties() {
        return messageProperties;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public Integer getDeliveryCount() {
        return deliveryCount;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    @Override
    public String toString() {
        return "CaseEventMessage{"
            + "messageId=" + messageId
            + ", sequence=" + sequence
            + ", caseId=" + caseId
            + ", eventTimestamp=" + eventTimestamp
            + ", fromDlq=" + fromDlq
            + ", state=" + state
            + ", received=" + received
            + ", deliveryCount=" + deliveryCount
            + ", holdUntil=" + holdUntil
            + ", retryCount=" + retryCount
            + "}";
    }
}
