package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RecipientData {

    private final String email;
    private final String orgId;
}
