package uk.gov.hmcts.reform.civil.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageState;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@SuppressWarnings("PMD.ExcessiveParameterList")
public class CcdServiceBusMessage implements Serializable {

    private static final long serialVersionUID = 3213665975741833471L;

    private final String messageId;
    private final Long sequence;
    private final String caseId;
    private final LocalDateTime eventTimestamp;
    private final Boolean fromDlq;
    private final MessageState state;
    private final JsonNode messageProperties;
    private final String messageContent;
    private final LocalDateTime received;
    private final Integer deliveryCount;
    private final LocalDateTime holdUntil;
    private final Integer retryCount;

    @JsonCreator
    public CcdServiceBusMessage(@JsonProperty("MessageId") String messageId,
                                @JsonProperty("Sequence") Long sequence,
                                @JsonProperty("CaseId") String caseId,
                                @JsonProperty("EventTimestamp") LocalDateTime eventTimestamp,
                                @JsonProperty("FromDlq") Boolean fromDlq,
                                @JsonProperty("State")MessageState state,
                                @JsonProperty("MessageProperties") JsonNode messageProperties,
                                @JsonProperty("MessageContent") String messageContent,
                                @JsonProperty("Received") LocalDateTime received,
                                @JsonProperty("DeliveryCount") Integer deliveryCount,
                                @JsonProperty("HoldUntil") LocalDateTime holdUntil,
                                @JsonProperty("RetryCount") Integer retryCount) {
        this.messageId = messageId;
        this.sequence = sequence;
        this.caseId = caseId;
        this.eventTimestamp = eventTimestamp;
        this.fromDlq = fromDlq;
        this.state = state;
        this.messageProperties = messageProperties;
        this.messageContent = messageContent;
        this.received = received;
        this.deliveryCount = deliveryCount;
        this.holdUntil = holdUntil;
        this.retryCount = retryCount;
    }

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
