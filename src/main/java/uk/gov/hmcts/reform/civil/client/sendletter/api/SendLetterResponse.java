package uk.gov.hmcts.reform.civil.client.sendletter.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Send letter response.
 */
public class SendLetterResponse {

    public final UUID letterId;

    /**
     * Constructor.
     * @param letterId The letter ID
     */
    public SendLetterResponse(@JsonProperty("letter_id") UUID letterId) {
        this.letterId = letterId;
    }
}
