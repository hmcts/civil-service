package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages.OrderDetailsPagesCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderDetailsPagesDisclosureOfDocumentFieldsFieldBuilder implements OrderDetailsPagesCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

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

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Updating disclosure of document fields with calculated dates");
        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
                .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)))
                .input3("Requests will be complied with within 7 days of the receipt of the request.")
                .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();
        updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact()).build();
        log.debug("Disclosure of document fields updated successfully");
    }
}
