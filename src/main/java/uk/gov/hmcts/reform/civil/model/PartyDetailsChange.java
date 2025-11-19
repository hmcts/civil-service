package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PartyDetailsChange {

    private String fieldName;
    private String previousValue;
    private String updatedValue;
}
