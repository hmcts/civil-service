package uk.gov.hmcts.reform.civil.model.robotics;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public enum EventType {
    MISCELLANEOUS("999"),
    ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED("38"),
    CONSENT_EXTENSION_FILING_DEFENCE("45"),
    DEFENCE_FILED("50"),
    DEFENCE_AND_COUNTER_CLAIM("52"),
    RECEIPT_OF_PART_ADMISSION("60"),
    RECEIPT_OF_ADMISSION("40"),
    REPLY_TO_DEFENCE("66"),
    DIRECTIONS_QUESTIONNAIRE_FILED("197");

    private String code;

    EventType(String code) {
        this.code = code;
    }

    private static final Map<String, EventType> BY_CODE = new HashMap<>();

    static {
        for (EventType e : values()) {
            BY_CODE.put(e.code, e);
        }
    }

    public static Optional<EventType> valueOfCode(String label) {
        return Optional.ofNullable(BY_CODE.get(label));
    }
}
