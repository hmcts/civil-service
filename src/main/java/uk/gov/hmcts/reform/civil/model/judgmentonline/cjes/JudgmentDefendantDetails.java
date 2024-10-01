package uk.gov.hmcts.reform.civil.model.judgmentonline.cjes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;

import java.time.LocalDate;

@Data
@Builder
public class JudgmentDefendantDetails {

    private String defendantName;
    private LocalDate defendantDateOfBirth;
    private JudgmentAddress defendantAddress;
}
