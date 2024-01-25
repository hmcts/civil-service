package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PartyDetailsChange {

    private final String fieldName;
    private final String previousValue;
    private final String updatedValue;
}
