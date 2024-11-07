package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackWitnessOfFactFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;
    private final FeatureToggleService featureToggleService;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact());
        } else {
            updatedData.fastTrackWitnessOfFact(getFastTrackWitnessOfFact());
        }
    }

    private FastTrackWitnessOfFact getFastTrackWitnessOfFact() {
        log.debug("Building FastTrackWitnessOfFact");
        return FastTrackWitnessOfFact.builder()
                .input1("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                        + "fact on whom they intend to rely.")
                .input2("3")
                .input3("3")
                .input4("For this limitation, a party is counted as a witness.")
                .input5("Each witness statement should be no more than")
                .input6("10")
                .input7("A4 pages. Statements should be double spaced using a font size of 12.")
                .input8("Witness statements shall be uploaded to the Digital Portal by 4pm on")
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input9("Evidence will not be permitted at trial from a witness whose statement has not been uploaded "
                        + "in accordance with this Order. Evidence not uploaded, or uploaded late, will not be "
                        + "permitted except with permission from the Court.")
                .build();
    }

    private static SdoR2WitnessOfFact getSdoR2WitnessOfFact() {
        return SdoR2WitnessOfFact.builder()
                .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
                .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                        .isRestrictWitness(NO)
                        .restrictNoOfWitnessDetails(
                                SdoR2RestrictNoOfWitnessDetails.builder()
                                        .noOfWitnessClaimant(3)
                                        .noOfWitnessDefendant(3)
                                        .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                        .build())
                        .build())
                .sdoRestrictPages(SdoR2RestrictPages.builder()
                        .isRestrictPages(NO)
                        .restrictNoOfPagesDetails(
                                SdoR2RestrictNoOfPagesDetails.builder()
                                        .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                        .noOfPages(12)
                                        .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                        .build())
                        .build())
                .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
                .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
                .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
                .build();
    }
}
