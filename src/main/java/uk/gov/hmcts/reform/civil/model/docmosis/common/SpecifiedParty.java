package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private final LocalDate individualDateOfBirth;
}
