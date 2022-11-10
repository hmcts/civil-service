package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingDisclosureOfDocumentsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingMedicalEvidenceDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingQuestionsToExpertsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingSchedulesOfLossDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingWitnessOfFactDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJ extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(STANDARD_DIRECTION_ORDER_DJ);
    private final ObjectMapper objectMapper;
    private final DefaultJudgmentOrderFormGenerator defaultJudgmentOrderFormGenerator;
    private final LocationRefDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    String participantString;
    public static final String DISPOSAL_HEARING = "DISPOSAL_HEARING";
    public static final String ORDER_1_CLAI = "The directions order has been sent to: "
        + "%n%n ## Claimant 1 %n%n %s";
    public static final String ORDER_1_DEF = "%n%n ## Defendant 1 %n%n %s";
    public static final String ORDER_2_DEF = "%n%n ## Defendant 2 %n%n %s";
    public static final String ORDER_ISSUED = "# Your order has been issued %n%n ## Claim number %n%n # %s";
    private final IdamClient idamClient;

    @Autowired
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::initiateSDO)
            .put(callbackKey(MID, "trial-disposal-screen"), this::populateDisposalTrialScreen)
            .put(callbackKey(MID, "create-order"), this::createOrderScreen)
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
            case ONE_V_TWO_TWO_LEGAL_REP:
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
        List<DisposalAndTrialHearingDJToggle> checkList = List.of(
            DisposalAndTrialHearingDJToggle.SHOW);
        if (caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)) {
            caseData = fillDisposalToggle(caseData, checkList);
        } else {
            caseData = fillTrialToggle(caseData, checkList);
        }
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsForDefaultJudgments(authToken));
        DynamicList locationsList = getLocationsFromList(locations);
        caseDataBuilder.trialHearingMethodInPersonDJ(locationsList);
        caseDataBuilder.disposalHearingMethodInPersonDJ(locationsList);

        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String judgeNameTitle = userDetails.getFullName();

        //populates the disposal screen
        caseDataBuilder
            .disposalHearingJudgesRecitalDJ(DisposalHearingJudgesRecitalDJ
                                                            .builder()
                                                            .judgeNameTitle(judgeNameTitle)
                                                            .input(judgeNameTitle
                                                                      + ", Upon considering the claim form and "
                                                                      + "Particulars of Claim/statements of case"
                                                                      + " [and the directions questionnaires] "
                                                                      + "\n\nIT IS ORDERED that:-").build());
        caseDataBuilder
            .disposalHearingDisclosureOfDocumentsDJ(DisposalHearingDisclosureOfDocumentsDJ
                                                                   .builder()
                                                                   .input("The parties shall serve on each other "
                                                                              + "copies of the documents upon which "
                                                                              + "reliance is to be"
                                                                              + " placed at the disposal hearing "
                                                                              + "by 4pm on")
                                                        .date(LocalDate.now().plusWeeks(4))
                                                        .build());

        caseDataBuilder
            .disposalHearingWitnessOfFactDJ(DisposalHearingWitnessOfFactDJ
                                                .builder()
                                                .input1("The claimant shall serve on every other party the witness "
                                                            + "statements of all witnesses of fact"
                                                            + " on whose evidence reliance is to be placed by 4pm on")
                                                .date1(LocalDate.now().plusWeeks(4))
                                                .input2("The provisions of CPR 32.6 apply to such evidence.")
                                                .input3("The claimant must upload to the Digital Portal copies of the "
                                                            + "witness statements of all witnesses whose evidence they "
                                                            + "wish the court to consider when deciding the amount of "
                                                            + "damages by by 4pm on ")
                                                .date2(LocalDate.now().plusWeeks(4))
                                                .input4("The provisions of CPR 32.6 apply to such evidence.")
                                                .input5("Any application by the defendant/s pursuant to CPR 32.7 "
                                                            + "must be made by 4pm on")
                                                .date3(LocalDate.now().plusWeeks(2))
                                                .input6("and must be accompanied by proposed directions for "
                                                            + "allocation and listing for trial on quantum as"
                                                            + " cross-examination will result in the hearing "
                                                            + "exceeding the 30 minute maximum time estimate"
                                                            + " for a disposal hearing")
                                                .build());

        caseDataBuilder.disposalHearingMedicalEvidenceDJ(DisposalHearingMedicalEvidenceDJ
                                                             .builder()
                                                             .input1("The claimant has permission to rely upon the"
                                                                     + " written expert evidence already uploaded to"
                                                                     + " the Digital Portal with the particulars of "
                                                                     + "claim and in addition has permission to rely"
                                                                     + " upon any associated correspondence or "
                                                                     + "updating report which is uploaded to the "
                                                                     + "Digital Portal by 4pm on")
                                                             .date1(LocalDate.now().plusWeeks(4))

                                                             .build());

        caseDataBuilder.disposalHearingQuestionsToExpertsDJ(DisposalHearingQuestionsToExpertsDJ
                                                                .builder()
                                                                .date(LocalDate.now().plusWeeks(6))
                                                                .build());

        caseDataBuilder.disposalHearingSchedulesOfLossDJ(DisposalHearingSchedulesOfLossDJ
                                                             .builder()
                                                             .input1("If there is a claim for ongoing/future loss "
                                                                         + "in the original schedule of losses then"
                                                                         + " the claimant"
                                                                         + " must send an up to date schedule of "
                                                                         + "loss to the defendant by 4pm on the")
                                                             .date1(LocalDate.now().plusWeeks(10))
                                                             .input2("If the defendant wants to challenge this claim,"
                                                                         + " they must send an up-to-date "
                                                                         + "counter-schedule of loss to the "
                                                                         + "claimant by 4pm on")
                                                             .date2(LocalDate.now().plusWeeks(12))
                                                             .input3("If the defendant wants to challenge the"
                                                                         + " sums claimed in the schedule of loss they"
                                                                         + " must upload to the Digital Portal an "
                                                                         + "updated counter schedule of loss by 4pm on")
                                                             .date3(LocalDate.now().plusWeeks(12))
                                                             .build());

        caseDataBuilder.disposalHearingFinalDisposalHearingDJ(DisposalHearingFinalDisposalHearingDJ
                                                                  .builder()
                                                                  .input("This claim be listed for final "
                                                                             + "disposal before a Judge on the first "
                                                                             + "available date after.")
                                                                  .date(LocalDate.now().plusWeeks(16))
                                                                  .build());

        // copy of the above field to update the Hearing time field while not breaking existing cases
        if (featureToggleService.isHearingAndListingSDOEnabled()) {
            caseDataBuilder.disposalHearingFinalDisposalHearingTimeDJ(DisposalHearingFinalDisposalHearingTimeDJ
                                                                          .builder()
                                                                          .input("This claim be listed for final "
                                                                                     + "disposal before a Judge on the "
                                                                                     + "first available date after")
                                                                          .date(LocalDate.now().plusWeeks(16))
                                                                          .build());
        }

        // copy of the above field to update the Hearing time field while not breaking existing cases
        if (featureToggleService.isHearingAndListingSDOEnabled()) {
            caseDataBuilder.disposalHearingFinalDisposalHearingTimeDJ(DisposalHearingFinalDisposalHearingTimeDJ
                                                                          .builder()
                                                                          .input("This claim will be listed for final "
                                                                                     + "disposal before a Judge on the "
                                                                                     + "first available date after")
                                                                          .date(LocalDate.now().plusWeeks(16))
                                                                          .build());
        }

        caseDataBuilder.disposalHearingBundleDJ(DisposalHearingBundleDJ
                                                    .builder()
                                                    .input("The claimant must lodge at court at least 7 "
                                                               + "days before the disposal")
                                                    .build());

        caseDataBuilder.disposalHearingNotesDJ(DisposalHearingNotesDJ
                                                 .builder()
                                                 .input("This order has been made without a hearing. Each "
                                                     + "party has the right to apply to have this order set "
                                                     + "aside or varied. Any such application must be uploaded "
                                                     + "to the Digital Portal together with payment of any "
                                                     + "appropriate fee, by 4pm on")
                                                 .date(LocalDate.now().plusWeeks(1))
                                                 .build());

        // copy of disposalHearingNotesDJ field to update order made without hearing field without breaking
        // existing cases
        if (featureToggleService.isHearingAndListingSDOEnabled()) {
            caseDataBuilder.disposalHearingOrderMadeWithoutHearingDJ(DisposalHearingOrderMadeWithoutHearingDJ
                                                   .builder()
                                                   .input(String.format("This order has been made without a hearing. "
                                                              + "Each party has the right to apply to have this order "
                                                              + "set aside or varied. Any such application must be "
                                                              + "received by the Court "
                                                              + "(together with the appropriate fee) by 4pm on %s.",
                                                          deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)
                                                              .format(DateTimeFormatter
                                                                          .ofPattern("dd MMMM yyyy", Locale.ENGLISH))))
                                                   .build());
        }
        // populates the trial screen
        caseDataBuilder
            .trialHearingJudgesRecitalDJ(TrialHearingJudgesRecital
                                             .builder()
                                             .judgeNameTitle(judgeNameTitle)
                                             .input(judgeNameTitle
                                                        + ", has considered the statements of "
                                                        + "the case and the information provided "
                                                        + "by the parties, \n\n "
                                                        + "IT IS ORDERED THAT:").build());

        caseDataBuilder
            .trialHearingDisclosureOfDocumentsDJ(TrialHearingDisclosureOfDocuments
                                                     .builder()
                                                     .input1("By uploading to a Digital Portal a list with a "
                                                                 + "disclosure statement by 4pm on")
                                                     .date1(LocalDate.now().plusWeeks(4))
                                                     .input2("Any request to inspect or for a copy of a document "
                                                                 + "shall by made by 4pm on")
                                                     .date2(LocalDate.now().plusWeeks(6))
                                                     .input3("and complied with with 7 days of the request")
                                                     .input4("Each party must upload to the Digital Portal"
                                                         + " copies of those documents on which they wish to rely"
                                                         + " at trial")
                                                     .input5("By 4pm on")
                                                     .date3(LocalDate.now().plusWeeks(4))
                                                     .build());

        caseDataBuilder
            .trialHearingWitnessOfFactDJ(TrialHearingWitnessOfFact
                                             .builder()
                                             .input1("Each party must upload to the Digital Portal copies of the "
                                                         + "statements of all witnesses of fact on whom they "
                                                         + "intend to rely.")
                                             .input4("For this limitation, a party is counted as witness.")
                                             .input5("Each witness statement should be no more than")
                                             .input7("A4 pages. Statements should be double spaced "
                                                         + "using a font size of 12.")
                                             .input8("Witness statements shall be uploaded to the "
                                                         + "Digital Portal by 4pm on")
                                             .date1(LocalDate.now().plusWeeks(8))
                                             .input9("Oral evidence will only be permitted at trial with permission "
                                                         + "from the Court from witnesses whose statements have not"
                                                         + " been uploaded to the Digital Portal in accordance with "
                                                         + "this order, or whose statements that have"
                                                         + " been served late")
                                             .build());

        caseDataBuilder
            .trialHearingSchedulesOfLossDJ(TrialHearingSchedulesOfLoss
                                               .builder()
                                               .input1("The claimant must upload to the Digital Portal an "
                                                   + "up-to-date schedule of loss to the defendant by 4pm on")
                                               .date1(LocalDate.now().plusWeeks(10))
                                               .input2("If the defendant wants to challenge this claim, "
                                                           + "upload to the Digital Portal counter-schedule"
                                                           + " of loss by 4pm on")
                                               .date2(LocalDate.now().plusWeeks(12))
                                               .input3("If there is a claim for future pecuniary loss and the parties"
                                                           + " have not already set out their "
                                                           + "case on periodical payments. "
                                                           + "then they must do so in the respective schedule "
                                                           + "and counter-schedule")
                                               .input4("Upon it being noted that the schedule of loss "
                                                           + "contains no claim for continuing loss and is "
                                                           + "therefore final, no further schedule of loss shall"
                                                           + " be uploaded without permission to amend. "
                                                           + "The defendant shall upload to the Digital Portal"
                                                           + " an up-to-date counter "
                                                           + "schedule of loss by 4pm on")
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
                                                .input3("At least 7 days before the trial, the claimant must"
                                                            + " upload to the Digital Portal ")
                                                .build());

        // copy of above method as to not break existing cases
        if (featureToggleService.isHearingAndListingSDOEnabled()) {
            caseDataBuilder.trialHearingTimeDJ(TrialHearingTimeDJ.builder()
                                               .helpText1(
                                                   "If either party considers that the time estimate is insufficient, "
                                                       + "they must inform the court within 7 days of the date of "
                                                       + "this order.")
                                               .helpText2(
                                                   "Not more than seven nor less than three clear days before the "
                                                       + "trial, the claimant must file at court and serve an indexed "
                                                       + "and paginated bundle of documents which complies with the "
                                                       + "requirements of Rule 39.5 Civil Procedure Rules "
                                                       + "and which complies with requirements of PD32. The parties "
                                                       + "must endeavour to agree the contents of the bundle before it "
                                                       + "is filed. The bundle will include a case summary and a "
                                                       + "chronology.")
                                               .build());
        }

        if (featureToggleService.isHearingAndListingSDOEnabled()) {
            caseDataBuilder.trialOrderMadeWithoutHearingDJ(TrialOrderMadeWithoutHearingDJ.builder()
                                               .input(String.format(
                                                   "This order has been made without a hearing. "
                                                       + "Each party has the right to apply to have this Order "
                                                       + "set aside or varied. Any such application must be "
                                                       + "received by the Court "
                                                       + "(together with the appropriate fee) by 4pm on %s.",
                                                   deadlinesCalculator
                                                       .plusWorkingDays(LocalDate.now(), 5)
                                                       .format(DateTimeFormatter
                                                                   .ofPattern("dd MMMM yyyy", Locale.ENGLISH))))
                                               .build());
        }

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
                                                 .input2("The columns should be headed: Item; Alleged Defect; "
                                                             + "Claimant's costing; Defendant's response; Defendant's"
                                                             + " costing; Reserved for Judge's use.")
                                                 .input3("The claimant must upload to the Digital Portal the "
                                                             + "Scott Schedule with the relevant "
                                                             + "columns completed by 4pm on")
                                                 .date1(LocalDate.now().plusWeeks(10))
                                                 .input4("The defendant must upload to the Digital Portal "
                                                     + "an amended version of the Scott Schedule with the relevant"
                                                         + " columns in response completed by 4pm on")
                                                 .date2(LocalDate.now().plusWeeks(12))
                                                 .build());

        caseDataBuilder.trialClinicalNegligence(TrialClinicalNegligence
                                                    .builder()
                                                    .input1("Documents should be retained as follows:")
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
                                            .input1("If impecuniosity is alleged by the claimant and not admitted "
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
                                            .input2("The claimant must upload to the Digital Portal a witness "
                                                        + "statement addressing a)the need to hire a replacement "
                                                        + "vehicle; and b)impecuniosity")
                                            .input3("This statement must be uploaded to the Digital Portal by 4pm on")
                                            .date1(LocalDate.now().plusWeeks(8))
                                            .input4("A failure to comply will result in the claimant being "
                                                        + "debarred from asserting need or relying on impecuniosity "
                                                        + "as the case may be at the final hearing, unless they "
                                                        + "have the permission of the trial Judge.")
                                            .input5("The parties are to liaise and use reasonable endeavours to"
                                                        + " agree the basic hire rate no "
                                                        + "later than 4pm on.")
                                            .date2(LocalDate.now().plusWeeks(10))
                                            .input6("If the parties fail to agree rates subject to liability and/or"
                                                        + " other issues pursuant to the "
                                                        + "paragraph above, each party may rely upon written evidence "
                                                        + "by way of witness statement of "
                                                        + "one witness to provide evidence of basic hire rates "
                                                        + "available within the claimant's "
                                                        + "geographical location, from a mainstream (or, if none"
                                                        + " available, a local reputable) "
                                                        + "supplier. The defendant's evidence to be served by 4pm on")
                                            .date3(LocalDate.now().plusWeeks(12))
                                            .input7("and the claimant’s evidence in reply if "
                                                        + "so advised is to be uploaded by 4pm on")
                                            .date4(LocalDate.now().plusWeeks(14))
                                            .input8("This witness statement is limited to 10 pages per party "
                                                        + "(to include any appendices).")
                                            .build());

        caseDataBuilder.trialPersonalInjury(TrialPersonalInjury
                                                .builder()
                                                .input1("The claimant has permission to rely upon the written "
                                                            + "expert evidence already uploaded to the Digital"
                                                            + " Portal with the particulars of claim and in addition "
                                                            + "has permission to rely upon any associated "
                                                            + "correspondence or updating report which is uploaded "
                                                            + "to the Digital Portal by 4pm on")
                                                .date1(LocalDate.now().plusWeeks(4))
                                                .input2("which must be answered by 4pm on")
                                                .date2(LocalDate.now().plusWeeks(8))
                                                .input3("Any questions which are to be addressed to an expert"
                                                            + " must be sent to the expert directly and uploaded to "
                                                            + "the Digital Portal by 4pm on")
                                                .date3(LocalDate.now().plusWeeks(4))
                                                .build());

        caseDataBuilder.trialRoadTrafficAccident(TrialRoadTrafficAccident
                                                     .builder()
                                                     .input("Photographs and/or a place of the accident location "
                                                                + "shall be prepared "
                                                                + "and agreed by the parties and uploaded to the"
                                                                + " Digital Portal by 4pm on")
                                                     .date1(LocalDate.now().plusWeeks(4))
                                                     .build());

        caseDataBuilder.trialHousingDisrepair(TrialHousingDisrepair.builder()
                                                  .input1("The claimant must prepare a Scott Schedule of the items "
                                                              + "of disrepair")
                                                  .input2("The column headings will be as follows: Item; Alleged "
                                                    + "disrepair; Defendant's Response; Reserved for Judge's Use")
                                                  .input3("The claimant must uploaded to the Digital Portal the "
                                                              + "Scott Schedule with the relevant columns "
                                                              + "completed by 4pm on")
                                                  .date1(LocalDate.now().plusWeeks(10))
                                                  .input4("The defendant must uploaded to the Digital Portal "
                                                      + "the amended Scott Schedule with the relevant columns "
                                                      + "in response completed by 4pm on")
                                                  .date2(LocalDate.now().plusWeeks(12))
                                        .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateSDONotifications(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.businessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ));
        var state = "CASE_PROGRESSION";
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsForDefaultJudgments(authToken));
        LocationRefData location = null;
        if (nonNull(locations)) {
            location = fillPreferredLocationData(locations, getLocationListFromCaseData(
                caseData.getDisposalHearingMethodInPersonDJ(), caseData.getTrialHearingMethodInPersonDJ()));
            Optional.ofNullable(location)
                .map(LocationRefDataService::buildCaseLocation)
                .ifPresent(caseDataBuilder::caseManagementLocation)
                       ;
            Optional.ofNullable(location).map(value -> value.getSiteName() ).map(caseDataBuilder::locationName);

        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(state)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private CallbackResponse createOrderScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        CaseDocument document = defaultJudgmentOrderFormGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.orderSDODocumentDJ(document.getDocumentLink());
        caseDataBuilder.disposalHearingMethodInPersonDJ(deleteLocationList(
            caseData.getDisposalHearingMethodInPersonDJ()));
        caseDataBuilder.trialHearingMethodInPersonDJ(deleteLocationList(
            caseData.getTrialHearingMethodInPersonDJ()));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData fillDisposalToggle(CaseData caseData, List<DisposalAndTrialHearingDJToggle> checkList) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.disposalHearingDisclosureOfDocumentsDJToggle(checkList);
        caseDataBuilder.disposalHearingWitnessOfFactDJToggle(checkList);
        caseDataBuilder.disposalHearingMedicalEvidenceDJToggle(checkList);
        caseDataBuilder.disposalHearingQuestionsToExpertsDJToggle(checkList);
        caseDataBuilder.disposalHearingSchedulesOfLossDJToggle(checkList);
        caseDataBuilder.disposalHearingStandardDisposalOrderDJToggle(checkList);
        caseDataBuilder.disposalHearingFinalDisposalHearingDJToggle(checkList);
        caseDataBuilder.disposalHearingBundleDJToggle(checkList);
        caseDataBuilder.disposalHearingClaimSettlingDJToggle(checkList);
        caseDataBuilder.disposalHearingCostsDJToggle(checkList);
        return caseDataBuilder.build();
    }

    private CaseData fillTrialToggle(CaseData caseData, List<DisposalAndTrialHearingDJToggle> checkList) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.trialHearingAlternativeDisputeDJToggle(checkList);
        caseDataBuilder.trialHearingVariationsDirectionsDJToggle(checkList);
        caseDataBuilder.trialHearingSettlementDJToggle(checkList);
        caseDataBuilder.trialHearingDisclosureOfDocumentsDJToggle(checkList);
        caseDataBuilder.trialHearingWitnessOfFactDJToggle(checkList);
        caseDataBuilder.trialHearingSchedulesOfLossDJToggle(checkList);
        caseDataBuilder.trialHearingCostsToggle(checkList);
        caseDataBuilder.trialHearingTrialDJToggle(checkList);

        return caseDataBuilder.build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream()
                            .map(location -> location.getSiteName()
                                + " - " + location.getCourtAddress()
                                + " - " + location.getPostcode())
                            .sorted()
                            .collect(Collectors.toList()));
    }

    private DynamicList deleteLocationList(DynamicList list) {
        if (isNull(list)) {
            return null;
        }
        return DynamicList.builder().value(list.getValue()).build();
    }

    private LocationRefData fillPreferredLocationData(final List<LocationRefData> locations,
                                                      DynamicList caseDataList) {
        if (Objects.isNull(caseDataList) || Objects.isNull(locations)) {
            return null;
        }
        String locationLabel = caseDataList.getValue().getLabel();
        var preferredLocation =
            locations
                .stream()
                .filter(locationRefData -> checkLocation(locationRefData,
                                                         locationLabel)).findFirst();
        return preferredLocation.orElse(null);
    }

    private Boolean checkLocation(final LocationRefData location, String locationTempLabel) {
        String locationLabel = location.getSiteName()
            + " - " + location.getCourtAddress()
            + " - " + location.getPostcode();
        return locationLabel.equals(locationTempLabel);
    }

    private DynamicList getLocationListFromCaseData(DynamicList hearingList, DynamicList trialList) {
        if (nonNull(hearingList) && nonNull(hearingList.getValue())) {
            return hearingList;
        } else if (nonNull(trialList) && nonNull(trialList.getValue())) {
            return trialList;
        } else {
            return null;
        }
    }
}
