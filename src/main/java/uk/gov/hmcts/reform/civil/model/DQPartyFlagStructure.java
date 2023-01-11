package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DQPartyFlagStructure {

    private final String firstName;
    private final String lastName;
    private final Flags flags;
}
