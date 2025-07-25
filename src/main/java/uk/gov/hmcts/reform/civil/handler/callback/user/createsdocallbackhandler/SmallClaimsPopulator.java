package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.HEARING_TIME_TEXT_AFTER;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle.SHOW;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmallClaimsPopulator {

    static final String WITNESS_STATEMENT_STRING = "This witness statement is limited to 10 pages per party, including any appendices.";
    static final String LATER_THAN_FOUR_PM_STRING = "later than 4pm on";
    static final String CLAIMANT_EVIDENCE_STRING = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";
    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService featureToggleService;

    public void setSmallClaimsFields(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        log.info("Setting small claims fields for caseId: {}", caseData.getCcdCaseReference());

        updatedData.smallClaimsJudgesRecital(SmallClaimsJudgesRecital.builder()
                .input(
                        "Upon considering the statements of case and the information provided by the parties,")
                .build());

        updatedData.smallClaimsDocuments(SmallClaimsDocuments.builder()
                .input1("Each party must upload to the Digital Portal copies of all" +
                        " documents which they wish the court to consider when reaching its decision not less than 21 days before the hearing.")
                .input2(
                        "The court may refuse to consider any document which has not been uploaded to the Digital Portal by the above date.")
                .build());

        updatedData.sdoR2SmallClaimsWitnessStatementOther(getSdoR2SmallClaimsWitnessStatements());

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            log.debug(
                    "CARM is enabled for caseId {}, setting small claims mediation section statement.",
                    caseData.getCcdCaseReference()
            );
            updatedData.smallClaimsMediationSectionStatement(SmallClaimsMediation.builder()
                    .input(
                            "If you failed to attend a mediation appointment, then the judge at the hearing may impose a sanction." +
                                    " This could require you to pay costs, or could result in your claim or defence being dismissed." +
                                    " You should deliver to every other party, and to the court, your explanation for non-attendance," +
                                    " with any supporting documents, at least 14 days before the hearing." +
                                    " Any other party who wishes to comment on the failure to attend the mediation" +
                                    " appointment should deliver their comments, with any supporting documents," +
                                    " to all parties and to the court at least 14 days before the hearing.")
                    .build());
        }

        log.debug("SDO R2 is enabled, setting small claims flight delay for caseId: {}", caseData.getCcdCaseReference());
        updatedData.smallClaimsFlightDelay(SmallClaimsFlightDelay.builder()
                .smallClaimsFlightDelayToggle(List.of(SHOW))
                .relatedClaimsInput("""
                        In the event that the Claimant(s) or Defendant(s) are aware if other\s
                        claims relating to the same flight they must notify the court\s
                        where the claim is being managed within 14 days of receipt of\s
                        this Order providing all relevant details of those claims including\s
                        case number(s), hearing date(s) and copy final substantive order(s)\s
                        if any, to assist the Court with ongoing case management which may\s
                        include the cases being heard together.""")
                .legalDocumentsInput("""
                        Any arguments as to the law to be applied to this claim, together with\s
                        copies of legal authorities or precedents relied on, shall be uploaded\s
                        to the Digital Portal not later than 3 full working days before the\s
                        final hearing date.""")
                .build());

        updatedData.smallClaimsHearing(SmallClaimsHearing.builder()
                .input1(
                        "The hearing of the claim will be on a date to be notified to you by a separate notification." +
                                " The hearing will have a time estimate of")
                .input2(HEARING_TIME_TEXT_AFTER)
                .build());

        updatedData.smallClaimsNotes(SmallClaimsNotes.builder()
                .input(
                        "This order has been made without hearing. Each party has the right to apply to have this Order set aside or varied." +
                                " Any such application must be received by the Court (together with the appropriate fee) by 4pm on " +
                                DateFormatHelper.formatLocalDate(
                                        deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5), DATE))
                .build());

        updatedData.smallClaimsCreditHire(getSmallClaimsCreditHire());

        updatedData.smallClaimsRoadTrafficAccident(SmallClaimsRoadTrafficAccident.builder()
                .input(
                        "Photographs and/or a plan of the accident location shall be prepared and" +
                                " agreed by the parties and uploaded to the Digital Portal no later than 21 days before the hearing.")
                .build());
        log.info("Finished setting small claims fields for caseId: {}", caseData.getCcdCaseReference());
    }

    private SmallClaimsCreditHire getSmallClaimsCreditHire() {
        log.debug("Creating small claims credit hire.");
        String partiesLiaseString = "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";
        return SmallClaimsCreditHire.builder()
                .input1("""
                        If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's disclosure as ordered earlier in this Order must include:
                        a) Evidence of all income from all sources for a period of 3 months prior to the commencement of hire until the earlier of:
                              i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        b) Copies of all bank, credit card, and saving account statements for a period of 3 months prior to the commencement of hire until the earlier of:
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""")
                .input2("""
                        The claimant must upload to the Digital Portal a witness statement addressing
                        a) the need to hire a replacement vehicle; and
                        b) impecuniosity""")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input3(
                        "A failure to comply with the paragraph above will result in the claimant being debarred from asserting need or relying on impecuniosity" +
                                " as the case may be at the final hearing, save with permission of the Trial Judge.")
                .input4(partiesLiaseString + LATER_THAN_FOUR_PM_STRING)
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                .input5(
                        "If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above," +
                                " each party may rely upon written evidence by way of witness statement of one witness to provide evidence of" +
                                " basic hire rates available within the claimant's geographical location," +
                                " from a mainstream supplier, or a local reputable supplier if none is available.")
                .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input7(CLAIMANT_EVIDENCE_STRING)
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input11(WITNESS_STATEMENT_STRING)
                .build();
    }

    private SdoR2SmallClaimsWitnessStatements getSdoR2SmallClaimsWitnessStatements() {
        log.debug("Creating SDO R2 small claims witness statements.");
        return SdoR2SmallClaimsWitnessStatements.builder()
                .sdoStatementOfWitness(
                        "Each party must upload to the Digital Portal copies of all witness statements of the witnesses upon whose evidence they" +
                                " intend to rely at the hearing not less than 21 days before the hearing.")
                .isRestrictWitness(NO)
                .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                        .noOfWitnessClaimant(2)
                        .noOfWitnessDefendant(2)
                        .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                        .build())
                .isRestrictPages(NO)
                .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                        .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                        .noOfPages(12)
                        .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                        .build())
                .text(WITNESS_DESCRIPTION_TEXT)
                .build();
    }
}
