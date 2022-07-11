package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJ extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(STANDARD_DIRECTION_ORDER_DJ);
    private final ObjectMapper objectMapper;
    String participantString;

    public static final String ORDER_1_CLAI = "The directions order has been sent to: "
        + "%n%n ## Claimant 1 %n%n %s";
    public static final String ORDER_1_DEF = "%n%n ## Defendant 1 %n%n %s";
    public static final String ORDER_2_DEF = "%n%n ## Defendant 2 %n%n %s";
    public static final String ORDER_ISSUED = "# Your order has been issued %n%n ## Claim number %n%n # %s";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::initiateSDO)
            .put(callbackKey(MID, "trial-disposal-screen"), this::populateDisposalTrialScreen)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::generateSDONotifications)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private String getBody(CaseData caseData) {
        if (caseData.getRespondent2() != null
            && caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            return format(ORDER_1_CLAI, caseData.getApplicant1().getPartyName())
                + format(ORDER_1_DEF, caseData.getRespondent1().getPartyName())
                + format(ORDER_2_DEF, caseData.getRespondent2().getPartyName());

        } else {
            return format(ORDER_1_CLAI, caseData.getApplicant1().getPartyName())
                + format(ORDER_1_DEF, caseData.getRespondent1().getPartyName());
        }
    }

    private String getHeader(CaseData caseData) {
        return format(ORDER_ISSUED, caseData.getLegacyCaseReference());
    }

    private CallbackResponse initiateSDO(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.applicantVRespondentText(caseParticipants(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

    public String caseParticipants(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        switch (multiPartyScenario) {

            case ONE_V_ONE:
                participantString = (caseData.getApplicant1().getPartyName() + " v " + caseData.getRespondent1()
                    .getPartyName());
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                participantString = (caseData.getApplicant1().getPartyName() + " v " + caseData.getRespondent1()
                    .getPartyName() + " and " + caseData.getRespondent2().getPartyName());
                break;

            case TWO_V_ONE:
                participantString = (caseData.getApplicant1().getPartyName() + " and " + caseData.getApplicant2()
                    .getPartyName() + " v " + caseData.getRespondent1().getPartyName());
                break;
            default:
                throw new CallbackException(String.format("Invalid participants"));
        }
        return participantString;

    }

    private CallbackResponse populateDisposalTrialScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        //populates the disposal screen
        caseDataBuilder
            .disposalHearingJudgesRecitalDJ(DisposalHearingJudgesRecital
                                                           .builder()
                                                           .input("Upon considering the claim Form and "
                                                                      + "Particulars of Claim/statements of case"
                                                                      + " [and the directions questionnaires] "
                                                                      + "\n\nIT IS ORDERED that:-").build());
        caseDataBuilder
            .disposalHearingDisclosureOfDocumentsDJ(DisposalHearingDisclosureOfDocuments
                                                                   .builder()
                                                                   .input("The parties shall serve on each other "
                                                                              + "copies of the documents upon which "
                                                                              + "reliance is to be"
                                                                              + " placed at the disposal hearing "
                                                                              + "by 4pm on")
                                                        .date(LocalDate.now().plusWeeks(4))
                                                        .build());

        caseDataBuilder
            .disposalHearingWitnessOfFactDJ(DisposalHearingWitnessOfFact
                                                .builder()
                                                .input1("The claimant shall serve on every other party the witness "
                                                            + "statements of all witnesses of fact"
                                                            + " on whose evidence reliance is to be placed by 4pm on")
                                                .date1(LocalDate.now().plusWeeks(4))
                                                .input2("The provisions of CPR 32.6 apply to such evidence.")
                                                .input3("Any application by the defendant/s pursuant to CPR 32.7 "
                                                            + "must be made by 4pm on")
                                                .date2(LocalDate.now().plusWeeks(2))
                                                .input4("and must be accompanied by proposed directions for "
                                                            + "allocation and listing for trial on quantum as"
                                                            + " cross-examination will result in the hearing "
                                                            + "exceeding the 30 minute maximum time estimate"
                                                            + " for a disposal hearing")
                                                .build());

        caseDataBuilder.disposalHearingMedicalEvidenceDJ(DisposalHearingMedicalEvidence
                                                             .builder()
                                                             .input1("The claimant has permission to rely upon the"
                                                                         + " written expert evidence served with the"
                                                                         + " Particulars of Claim to be disclosed "
                                                                         + "by 4pm")
                                                             .date1(LocalDate.now().plusWeeks(4))
                                                             .input2("and any associated correspondence and/or "
                                                                         + "updating report disclosed not later "
                                                                         + "than 4pm on the")
                                                             .date2(LocalDate.now().plusWeeks(4))
                                                             .build());

        caseDataBuilder.disposalHearingQuestionsToExpertsDJ(DisposalHearingQuestionsToExperts
                                                                .builder()
                                                                .date(LocalDate.now().plusWeeks(6))
                                                                .build());

        caseDataBuilder.disposalHearingSchedulesOfLossDJ(DisposalHearingSchedulesOfLoss
                                                             .builder()
                                                             .input1("If there is a claim for ongoing/future loss "
                                                                         + "in the original schedule of losses then"
                                                                         + " the claimant"
                                                                         + " must send an up to date schedule of "
                                                                         + "loss to the defendant by 4pm on the")
                                                             .date1(LocalDate.now().plusWeeks(10))
                                                             .input2("The defendant, in the event of challenge, "
                                                                         + "must send an up to date counter-schedule "
                                                                         + "of loss"
                                                                         + " to the claimant by 4pm on the")
                                                             .date2(LocalDate.now().plusWeeks(12))
                                                             .build());

        caseDataBuilder.disposalHearingStandardDisposalOrderDJ(DisposalHearingStandardDisposalOrder
                                                                   .builder()
                                                                   .input("input")
                                                                   .build());

        caseDataBuilder.disposalHearingFinalDisposalHearingDJ(DisposalHearingFinalDisposalHearing
                                                                  .builder()
                                                                  .input("This claim be listed for final "
                                                                             + "disposal before a Judge on the first "
                                                                             + "available date after.")
                                                                  .date(LocalDate.now().plusWeeks(16))
                                                                  .build());

        caseDataBuilder.disposalHearingBundleDJ(DisposalHearingBundle
                                                    .builder()
                                                    .input("The claimant must lodge at court at least 7 "
                                                               + "days before the disposal")
                                                    .build());

        caseDataBuilder.disposalHearingNotesDJ(DisposalHearingNotes
                                                 .builder()
                                                 .input("This Order has been made without a hearing. Each party "
                                                            + "has the right to apply to have this Order"
                                                            + " set aside or varied. Any such application must be "
                                                            + "received by the Court"
                                                            + " (together with the appropriate fee) by 4pm on")
                                                 .date(LocalDate.now().plusWeeks(1))
                                                 .build());

        // populates the trial screen
        caseDataBuilder
            .trialHearingJudgesRecitalDJ(TrialHearingJudgesRecital
                                             .builder()
                                             .input("[Title] [your name] has considered the statements of "
                                                        + "the case and the information provided "
                                                        + "by the parties, \n\n "
                                                        + "IT IS ORDERED THAT:").build());

        caseDataBuilder
            .trialHearingDisclosureOfDocumentsDJ(TrialHearingDisclosureOfDocuments
                                                     .builder()
                                                     .input1("By serving a list with a disclosure statement by 4pm on")
                                                     .date1(LocalDate.now().plusWeeks(4))
                                                     .input2("Any request to inspect or for a copy of a document "
                                                                 + "shall by made by 4pm on")
                                                     .date2(LocalDate.now().plusWeeks(6))
                                                     .input3("and complied with with 7 days of the request")
                                                     .input4("Each party must serve and file with the court a "
                                                                 + "list of issues relevant to the search for and "
                                                                 + "disclosure of electronically stored documents, "
                                                                 + "or must confirm there are no such issues, following"
                                                                 + " Civil Rule Practice Direction 31B.")
                                                     .input5("By 4pm on")
                                                     .date3(LocalDate.now().plusWeeks(4))
                                                     .build());

        caseDataBuilder
            .trialHearingWitnessOfFactDJ(TrialHearingWitnessOfFact
                                             .builder()
                                             .input1("Each party shall serve on every other party the witness "
                                                         + "statements of all witnesses of fact on whom he "
                                                         + "intends to rely")
                                             .input2("All statements to be no more than")
                                             .input4("pages long, A4, double spaced and in font size 12.")
                                             .input5("There shall be simultaneous exchange of such "
                                                         + "statements by 4pm on")
                                             .date1(LocalDate.now().plusWeeks(8))
                                             .input6("Oral evidence will not be permitted at trial from a "
                                                         + "witness whose statement has not been served in accordance"
                                                         + " with this order or has been served late, except with "
                                                         + "permission from the court")
                                             .build());

        caseDataBuilder
            .trialHearingSchedulesOfLossDJ(TrialHearingSchedulesOfLoss
                                               .builder()
                                               .input1("The claimant shall serve an updated schedule of loss "
                                                           + "on the defendant(s) by 4pm on")
                                               .date1(LocalDate.now().plusWeeks(10))
                                               .input2("The defendant(s) shall serve a counter "
                                                           + "schedule on the claimant by 4pm on")
                                               .date2(LocalDate.now().plusWeeks(12))
                                               .input3("If there is a claim for future pecuniary loss and the parties"
                                                           + " have not already set out their "
                                                           + "case on periodical payments. "
                                                           + "then they must do so in the respective schedule "
                                                           + "and counter-schedule")
                                               .input4("Upon it being noted that the schedule of loss "
                                                           + "contains no claim "
                                                           + "for continuing loss and is therefore final, no further"
                                                           + " schedule of loss shall be served without permission "
                                                           + "to amend. The defendant shall file a counter-schedule "
                                                           + "of loss by 4pm on")
                                               .date3(LocalDate.now().plusWeeks(12))
                                               .build());

        caseDataBuilder.trialHearingTrialDJ(TrialHearingTrial
                                                .builder()
                                                .input1("The time provisionally allowed for the trial is")
                                                .date1(LocalDate.now().plusWeeks(22))
                                                .date2(LocalDate.now().plusWeeks(34))
                                                .input2("If either party considers that the time estimates is"
                                                            + " insufficient, they must inform the court within "
                                                            + "7 days of the date of this order.")
                                                .input3("Not more than seven nor less than three clear days before "
                                                            + "the trial, the claimant must file at court and serve an"
                                                            + "indexed and paginated bundle of documents which complies"
                                                            + " with the requirements of Rule 39.5 Civil "
                                                            + "Procedure Rules"
                                                            + " and Practice Direction 39A. The parties must "
                                                            + "endeavour to agree the contents of the "
                                                            + "bundle before it is filed. "
                                                            + "The bundle will include a case summary"
                                                            + " and a chronology.")
                                                .build());

        caseDataBuilder.trialHearingNotesDJ(TrialHearingNotes
                                                .builder()
                                                .input("This order has been made without a hearing. Each party has "
                                                           + "the right to apply to have this order set "
                                                           + "aside or varied."
                                                           + " Any such application must be received by the court "
                                                           + "(together with the appropriate fee) by 4pm on")
                                                .date(LocalDate.now().plusWeeks(1))
                                                .build());

        caseDataBuilder.trialBuildingDispute(TrialBuildingDispute
                                                 .builder()
                                                 .input1("The claimant must prepare a Scott Schedule of the defects,"
                                                             + " items of damage "
                                                             + "or any other relevant matters")
                                                 .input2("The column headings will be as follows: Item; "
                                                             + "Alleged Defect; claimant's Costing; "
                                                             + "defendant's Response; defendant's Costing; "
                                                             + "Reserved for Judge's Use")
                                                 .input3("The claimant must serve the Scott Schedule with the "
                                                             + "relevant columns completed by 4pm on")
                                                 .date1(LocalDate.now().plusWeeks(10))
                                                 .input4("The defendant must file and serve the Scott Schedule "
                                                             + "with the relevant columns "
                                                             + "in response completed by 4pm on")
                                                 .date2(LocalDate.now().plusWeeks(12))
                                                 .build());

        caseDataBuilder.trialClinicalNegligence(TrialClinicalNegligence
                                                    .builder()
                                                    .input1("Documents are to be retained as follows:")
                                                    .input2("the parties must retain all electronically stored "
                                                                +
                                                                "documents relating to the issues in this Claim.")
                                                    .input3("the defendant must retain the original clinical notes"
                                                                + " relating to the issues in this Claim. "
                                                                + "The defendant must give facilities for inspection "
                                                                + "by the claimant, "
                                                                + "the claimant's legal advisers and experts of these"
                                                                + " original notes on 7 days written notice.")
                                                    .input4("Legible copies of the medical and educational records of"
                                                                + " the claimant / Deceased / "
                                                                + "claimant's Mother are to be placed in a separate"
                                                                + " paginated bundle by the "
                                                                + "claimant's Solicitors and kept up to date. All "
                                                                + "references to medical notes are to be made "
                                                                + "by reference to the pages in that bundle.")
                                                    .build());

        caseDataBuilder.trialCreditHire(TrialCreditHire
                                            .builder()
                                            .input1("1. If impecuniosity is alleged by the claimant and not admitted "
                                                        + "by the defendant, the claimant's "
                                                        + "disclosure as ordered earlier in this order must "
                                                        + "include:\n"
                                                        + "a. Evidence of all income from all sources for a period "
                                                        + "of 3 months prior to the "
                                                        + "commencement of hire until the earlier of i) 3 months "
                                                        + "after cessation of hire or ii) "
                                                        + "the repair/replacement of the claimant's vehicle;\n"
                                                        + "b. Copy statements of all blank, credit care and savings "
                                                        + "accounts for a period of 3 months "
                                                        + "prior to the commencement of hire until the earlier of i)"
                                                        + " 3 months after cessation of hire "
                                                        + "or ii) the repair/replacement of the claimant's vehicle;\n"
                                                        + "c. Evidence of any loan, overdraft or other credit "
                                                        + "facilities available to the claimant")
                                            .input2("The claimant must file and serve a witness statement addressing, "
                                                        + "(a) need to hire a replacement "
                                                        + "vehicle and (b) impecuniosity no later than 4pm on")
                                            .date1(LocalDate.now().plusWeeks(8))
                                            .input3("Failure to comply with the paragraph above will result in the "
                                                        + "claimant being debarred from "
                                                        + "asserting need or relying on impecuniosity as the case"
                                                        + " may be at the final hearing, "
                                                        + "save with permission of the Trial Judge.")
                                            .input4("4. The parties are to liaise and use reasonable endeavours to"
                                                        + " agree the basic hire rate no "
                                                        + "later than 4pm on.")
                                            .date2(LocalDate.now().plusWeeks(10))
                                            .input5("5. If the parties fail to agree rates subject to liability and/or"
                                                        + " other issues pursuant to the "
                                                        + "paragraph above, each party may rely upon written evidence "
                                                        + "by way of witness statement of "
                                                        + "one witness to provide evidence of basic hire rates "
                                                        + "available within the claimant's "
                                                        + "geographical location, from a mainstream (or, if none"
                                                        + " available, a local reputable) "
                                                        + "supplier. The defendant's evidence to be served by 4pm on")
                                            .date3(LocalDate.now().plusWeeks(12))
                                            .input6("and the claimant's evidence in reply if so advised to be"
                                                        + " served by 4pm on")
                                            .date4(LocalDate.now().plusWeeks(14))
                                            .input7("This witness statement is limited to 10 pages per party "
                                                        + "(to include any appendices).")
                                            .build());

        caseDataBuilder.trialPersonalInjury(TrialPersonalInjury
                                                .builder()
                                                .input1("1. The claimant has permission to rely on the written"
                                                            + " expert evidence annexed to the "
                                                            + "Particulars of Claim. Defendant may raise written "
                                                            + "questions of the expert by 4pm on")
                                                .date1(LocalDate.now().plusWeeks(4))
                                                .input2("which must be answered by 4pm on")
                                                .date2(LocalDate.now().plusWeeks(8))
                                                .input3("No other permission is given for expert evidence.")
                                                .build());

        caseDataBuilder.trialRoadTrafficAccident(TrialRoadTrafficAccident
                                                     .builder()
                                                     .input("Photographs and/or a plan of the location of the"
                                                                + " accident shall be prepared and "
                                                                + "agreed by the parties.")
                                                     .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateSDONotifications(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.businessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }
}
