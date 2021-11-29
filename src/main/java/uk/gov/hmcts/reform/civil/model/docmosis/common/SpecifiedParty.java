package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class SpecifiedParty {

    private final String name;
    private final Address primaryAddress;
    private final Representative representative;
    private final LocalDate individualDateOfBirth;
}
