package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class SdoR2AndNihlWitnessesOfFactFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
                .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                        .isRestrictWitness(NO)
                        .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                .noOfWitnessClaimant(3)
                                .noOfWitnessDefendant(3)
                                .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                .build())
                        .build())
                .sdoRestrictPages(SdoR2RestrictPages.builder()
                        .isRestrictPages(NO)
                        .restrictNoOfPagesDetails(SdoR2RestrictNoOfPagesDetails.builder()
                                .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                .noOfPages(12)
                                .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                .build())
                        .build())
                .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
                .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
                .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
                .build());
    }
}
