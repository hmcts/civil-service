package uk.gov.hmcts.reform.civil.model.judgmentonline.cjes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentDefendantDetails {

    private String defendantName;
    private LocalDate defendantDateOfBirth;
    private JudgmentAddress defendantAddress;
}
