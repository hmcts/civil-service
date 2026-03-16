package uk.gov.hmcts.reform.civil.client.sendletter.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Letter status.
 */
public class LetterStatus {
    public UUID id;

    public String status;

    @JsonProperty("message_id")
    public String messageId;

    @JsonProperty("checksum")
    public String checksum;

    @JsonProperty("created_at")
    public ZonedDateTime createdAt;

    @JsonProperty("sent_to_print_at")
    public ZonedDateTime sentToPrintAt;

    @JsonProperty("printed_at")
    public ZonedDateTime printedAt;

    @JsonProperty("additional_data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Object> additionalData;

    public Integer copies;

    /**
     * Constructor.
     */
    public LetterStatus() {

    }

    /**
     * Constructor.
     * @param id The ID
     * @param status The status
     * @param checksum The checksum
     * @param createdAt The created at
     * @param sentToPrintAt The sent to print at
     * @param printedAt The printed at
     * @param additionalData The additional data
     * @param copies The copies
     */
    public LetterStatus(
            final UUID id,
            final String status,
            final String checksum,
            final ZonedDateTime createdAt,
            final ZonedDateTime sentToPrintAt,
            final ZonedDateTime printedAt,
            final Map<String, Object> additionalData,
            final Integer copies
    ) {
        this.id = id;
        this.status = status;
        this.checksum = checksum;
        this.messageId = checksum;
        this.createdAt = createdAt;
        this.sentToPrintAt = sentToPrintAt;
        this.printedAt = printedAt;
        this.additionalData = additionalData;
        this.copies = copies;
    }
}