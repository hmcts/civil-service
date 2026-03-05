package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2WitnessOfFact {

    private String sdoStatementOfWitness;
    private SdoR2RestrictWitness sdoR2RestrictWitness;
    private SdoR2RestrictPages sdoRestrictPages;
    private String sdoWitnessDeadline;
    private LocalDate sdoWitnessDeadlineDate;
    private String sdoWitnessDeadlineText;

}
