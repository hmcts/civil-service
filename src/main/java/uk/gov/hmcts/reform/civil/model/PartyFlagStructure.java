package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

@Data
@Builder(toBuilder = true)
public class PartyFlagStructure {

    private final String firstName;
    private final String lastName;
    private final Flags flags;
}
