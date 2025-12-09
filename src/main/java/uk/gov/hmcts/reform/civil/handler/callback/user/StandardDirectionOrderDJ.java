package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingDisclosureOfDocumentsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingMedicalEvidenceDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingQuestionsToExpertsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingSchedulesOfLossDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingWitnessOfFactDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHireDetails;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.referencedata.LocationRefData;
import uk.gov.hmcts.reform.civil.service.refdata.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardDirectionOrderDJ extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(STANDARD_DIRECTION_ORDER_DJ);
    private static final String HEARING_CHANNEL = "HearingChannel";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";
    private final ObjectMapper objectMapper;
    private final DefaultJudgmentOrderFormGenerator defaultJudgmentOrderFormGenerator;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    String participantString;
    public static final String DISPOSAL_HEARING = "DISPOSAL_HEARING";
    public static final String ORDER_1_CLAI = "The directions order has been sent to: "
        + "%n%n ## Claimant 1 %n%n %s";
    public static final String ORDER_1_DEF = "%n%n ## Defendant 1 %n%n %s";
    public static final String ORDER_2_DEF = "%n%n ## Defendant 2 %n%n %s";
    public static final String ORDER_ISSUED = "# Your order has been issued %n%n ## Claim number %n%n # %s";

    private final UserService userService;
    private final AssignCategoryId assignCategoryId;
    private final CategoryService categoryService;
    private final LocationHelper locationHelper;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    @Autowired
    private final DeadlinesCalculator deadlinesCalculator;
    private final WorkingDayIndicator workingDayIndicator;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::initiateSDO)
            .put(callbackKey(MID, "trial-disposal-screen"), this::populateDisposalTrialScreen)
            .put(callbackKey(V_1, MID, "trial-disposal-screen"), this::populateDisposalTrialScreen)
            .put(callbackKey(MID, "create-order"), this::createOrderScreen)
            .put(callbackKey(V_1, MID, "create-order"), this::createOrderScreen)
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

        caseData.setApplicantVRespondentText(caseParticipants(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();

    }

    public String caseParticipants(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        switch (multiPartyScenario) {
            case ONE_V_ONE ->
                participantString = (caseData.getApplicant1().getPartyName() + " v " + caseData.getRespondent1()
                    .getPartyName());
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                participantString = (caseData.getApplicant1().getPartyName() + " v " + caseData.getRespondent1()
                    .getPartyName() + " and " + caseData.getRespondent2().getPartyName());
            case TWO_V_ONE ->
                participantString = (caseData.getApplicant1().getPartyName() + " and " + caseData.getApplicant2()
                    .getPartyName() + " v " + caseData.getRespondent1().getPartyName());
            default -> throw new CallbackException("Invalid participants");
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
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        Optional<RequestedCourt> preferredCourt = locationHelper.getCaseManagementLocation(caseData);
        DynamicList locationsList = getLocationList(callbackParams, preferredCourt.orElse(null));
        caseData.setTrialHearingMethodInPersonDJ(locationsList);
        caseData.setDisposalHearingMethodInPersonDJ(locationsList);

        if (V_1.equals(callbackParams.getVersion())) {
            String serviceId = caseData.getCaseAccessCategory().equals(CaseCategory.SPEC_CLAIM)
                ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID;
            Optional<CategorySearchResult> categorySearchResult = categoryService.findCategoryByCategoryIdAndServiceId(
                authToken, HEARING_CHANNEL, serviceId
            );
            DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult.orElse(null));
            List<DynamicListElement> hearingMethodListWithoutNotInAttendance = hearingMethodList
                .getListItems()
                .stream()
                .filter(elem -> !elem.getLabel().equals(HearingMethod.NOT_IN_ATTENDANCE.getLabel()))
                .toList();
            hearingMethodList.setListItems(hearingMethodListWithoutNotInAttendance);
            hearingMethodList.setValue(hearingMethodListWithoutNotInAttendance.stream().filter(
                elem -> HearingMethod.IN_PERSON.getLabel().equals(elem.getLabel())).findFirst().orElse(null));

            caseData.setHearingMethodValuesDisposalHearingDJ(hearingMethodList);
            caseData.setHearingMethodValuesTrialHearingDJ(hearingMethodList);
        }

        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String judgeNameTitle = userDetails.getFullName();

        //populates the disposal screen
        DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ = new DisposalHearingJudgesRecitalDJ();
        disposalHearingJudgesRecitalDJ.setInput(judgeNameTitle + ",");
        disposalHearingJudgesRecitalDJ.setJudgeNameTitle(judgeNameTitle);
        caseData
            .setDisposalHearingJudgesRecitalDJ(disposalHearingJudgesRecitalDJ);

        DisposalHearingDisclosureOfDocumentsDJ disposalHearingDisclosureOfDocumentsDJ = new DisposalHearingDisclosureOfDocumentsDJ();
        disposalHearingDisclosureOfDocumentsDJ.setInput("The parties shall serve on each other "
                                                            + "copies of the documents upon which "
                                                            + "reliance is to be"
                                                            + " placed at the disposal hearing "
                                                            + "by 4pm on");
        disposalHearingDisclosureOfDocumentsDJ.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        caseData
            .setDisposalHearingDisclosureOfDocumentsDJ(disposalHearingDisclosureOfDocumentsDJ);

        DisposalHearingWitnessOfFactDJ disposalHearingWitnessOfFactDJ  = new DisposalHearingWitnessOfFactDJ();
        disposalHearingWitnessOfFactDJ.setInput1("The claimant must upload to the Digital Portal copies of "
                        + "the witness statements of all witnesses "
                        + "of fact on whose evidence reliance is "
                        + "to be placed by 4pm on ");
        disposalHearingWitnessOfFactDJ.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        disposalHearingWitnessOfFactDJ.setInput2("The provisions of CPR 32.6 apply to such evidence.");
        disposalHearingWitnessOfFactDJ.setInput3("Any application by the defendant in relation to CPR 32.7 "
                        + "must be made by 4pm on");
        disposalHearingWitnessOfFactDJ.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(2)));
        disposalHearingWitnessOfFactDJ.setInput4("and must be accompanied by proposed directions for allocation"
                        + " and listing for trial on quantum. This is because"
                        + " cross-examination will cause the hearing to exceed"
                        + " the 30 minute maximum time estimate for a disposal"
                        + " hearing.");
        caseData
            .setDisposalHearingWitnessOfFactDJ(disposalHearingWitnessOfFactDJ);

        DisposalHearingMedicalEvidenceDJ disposalHearingMedicalEvidenceDJ  = new DisposalHearingMedicalEvidenceDJ();
        disposalHearingMedicalEvidenceDJ.setInput1("The claimant has permission to rely upon the"
                    + " written expert evidence already uploaded to"
                    + " the Digital Portal with the particulars of "
                    + "claim and in addition has permission to rely"
                    + " upon any associated correspondence or "
                    + "updating report which is uploaded to the "
                    + "Digital Portal by 4pm on");
        disposalHearingMedicalEvidenceDJ.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        caseData.setDisposalHearingMedicalEvidenceDJ(disposalHearingMedicalEvidenceDJ);

        DisposalHearingQuestionsToExpertsDJ disposalHearingQuestionsToExpertsDJ  = new DisposalHearingQuestionsToExpertsDJ();
        disposalHearingQuestionsToExpertsDJ.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));
        caseData.setDisposalHearingQuestionsToExpertsDJ(disposalHearingQuestionsToExpertsDJ);

        DisposalHearingSchedulesOfLossDJ disposalHearingSchedulesOfLossDJ  = new DisposalHearingSchedulesOfLossDJ();
        disposalHearingSchedulesOfLossDJ.setInput1("If there is a claim for ongoing or future loss "
                                                                                 + "in the original schedule of losses then"
                                                                                 + " the claimant"
                                                                                 + " must send an up to date schedule of "
                                                                                 + "loss to the defendant by 4pm on the");
        disposalHearingSchedulesOfLossDJ.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        disposalHearingSchedulesOfLossDJ.setInput2("If the defendant wants to challenge this claim,"
                        + " they must send an up-to-date "
                        + "counter-schedule of loss to the "
                        + "claimant by 4pm on");
        disposalHearingSchedulesOfLossDJ.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        disposalHearingSchedulesOfLossDJ.setInput3("If the defendant wants to challenge the"
                        + " sums claimed in the schedule of loss they"
                        + " must upload to the Digital Portal an "
                        + "updated counter schedule of loss by 4pm on");
        disposalHearingSchedulesOfLossDJ.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        disposalHearingSchedulesOfLossDJ.setInputText4("If there is a claim for future pecuniary loss"
                            + " and the parties have not already set out"
                            + " their case on periodical payments, they"
                            + " must do so in the respective schedule"
                            + " and counter-schedule.");
        caseData.setDisposalHearingSchedulesOfLossDJ(disposalHearingSchedulesOfLossDJ);

        DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ = new DisposalHearingFinalDisposalHearingDJ();
        disposalHearingFinalDisposalHearingDJ.setInput("This claim will be listed for final "
                             + "disposal before a Judge on the first "
                             + "available date after");
        disposalHearingFinalDisposalHearingDJ.setDate(LocalDate.now().plusWeeks(16));
        caseData.setDisposalHearingFinalDisposalHearingDJ(disposalHearingFinalDisposalHearingDJ);

        DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ  = new DisposalHearingFinalDisposalHearingTimeDJ();
        disposalHearingFinalDisposalHearingTimeDJ.setInput("This claim will be listed for final "
                             + "disposal before a Judge on the "
                             + "first available date after");
        disposalHearingFinalDisposalHearingTimeDJ.setDate(LocalDate.now().plusWeeks(16));
        // copy of the above field to update the Hearing time field while not breaking existing cases
        caseData.setDisposalHearingFinalDisposalHearingTimeDJ(disposalHearingFinalDisposalHearingTimeDJ);

        DisposalHearingBundleDJ disposalHearingBundleDJ  = new DisposalHearingBundleDJ();
        disposalHearingBundleDJ.setInput("At least 7 days before the disposal hearing, the claimant"
                               + " must file and serve");
        caseData.setDisposalHearingBundleDJ(disposalHearingBundleDJ);

        DisposalHearingNotesDJ disposalHearingNotesDJ  = new DisposalHearingNotesDJ();
        disposalHearingNotesDJ.setInput("This order has been made without a hearing. Each "
                              + "party has the right to apply to have this order set "
                              + "aside or varied. Any such application must be uploaded "
                              + "to the Digital Portal together with payment of any "
                              + "appropriate fee, by 4pm on");
        disposalHearingNotesDJ.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)));
        caseData.setDisposalHearingNotesDJ(disposalHearingNotesDJ);

        // copy of disposalHearingNotesDJ field to update order made without hearing field without breaking
        // existing cases
        DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ  = new DisposalHearingOrderMadeWithoutHearingDJ();
        disposalHearingOrderMadeWithoutHearingDJ.setInput(String.format(
                            "This order has been made without a hearing. "
                                + "Each party has the right to apply to have this Order "
                                + "set aside or varied. Any such application must "
                                + "be received by the Court "
                                + "(together with the appropriate fee) by 4pm on %s.",
                            deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)
                                .format(DateTimeFormatter
                                            .ofPattern("dd MMMM yyyy", Locale.ENGLISH))));

        caseData.setDisposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ);

        // populates the trial screen
        TrialHearingJudgesRecital trialHearingJudgesRecital = new TrialHearingJudgesRecital();
        trialHearingJudgesRecital.setJudgeNameTitle(judgeNameTitle);
        trialHearingJudgesRecital.setInput(judgeNameTitle + ",");
        caseData
            .setTrialHearingJudgesRecitalDJ(trialHearingJudgesRecital);

        TrialHearingWitnessOfFact trialHearingWitnessOfFact = new TrialHearingWitnessOfFact();
        trialHearingWitnessOfFact.setInput1("Each party must upload to the Digital Portal copies of the "
                                                + "statements of all witnesses of fact on whom they "
                                                + "intend to rely.");
        trialHearingWitnessOfFact.setInput2("3");
        trialHearingWitnessOfFact.setInput3("3");
        trialHearingWitnessOfFact.setInput4("For this limitation, a party is counted as witness.");
        trialHearingWitnessOfFact.setInput5("Each witness statement should be no more than");
        trialHearingWitnessOfFact.setInput6("10");
        trialHearingWitnessOfFact.setInput7("A4 pages. Statements should be double spaced "
                                                + "using a font size of 12.");
        trialHearingWitnessOfFact.setInput8("Witness statements shall be uploaded to the "
                                                + "Digital Portal by 4pm on");
        trialHearingWitnessOfFact.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        trialHearingWitnessOfFact.setInput9("Evidence will not be permitted at trial from a witness whose "
                                                + "statement has not been uploaded in accordance with this"
                                                + " Order. Evidence not uploaded, or uploaded late, will not "
                                                + "be permitted except with permission from the Court");
        caseData.setTrialHearingWitnessOfFactDJ(trialHearingWitnessOfFact);

        TrialHearingSchedulesOfLoss trialHearingSchedulesOfLoss = new TrialHearingSchedulesOfLoss();
        trialHearingSchedulesOfLoss.setInput1("The claimant must upload to the Digital Portal an "
                                                  + "up-to-date schedule of loss by 4pm on");
        trialHearingSchedulesOfLoss.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        trialHearingSchedulesOfLoss.setInput2("If the defendant wants to challenge this claim, "
                                                  + "upload to the Digital Portal counter-schedule"
                                                  + " of loss by 4pm on");
        trialHearingSchedulesOfLoss.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        trialHearingSchedulesOfLoss.setInput3("If there is a claim for future pecuniary loss and the parties"
                                                  + " have not already set out their "
                                                  + "case on periodical payments. "
                                                  + "then they must do so in the respective schedule "
                                                  + "and counter-schedule");
        caseData.setTrialHearingSchedulesOfLossDJ(trialHearingSchedulesOfLoss);

        TrialHearingTrial trialHearingTrial = new TrialHearingTrial();
        trialHearingTrial.setInput1("The time provisionally allowed for the trial is");
        trialHearingTrial.setDate1(LocalDate.now().plusWeeks(22));
        trialHearingTrial.setDate2(LocalDate.now().plusWeeks(34));
        trialHearingTrial.setInput2("If either party considers that the time estimates is"
                                        + " insufficient, they must inform the court within "
                                        + "7 days of the date of this order.");
        trialHearingTrial.setInput3("At least 7 days before the trial, the claimant must"
                                        + " upload to the Digital Portal ");
        caseData.setTrialHearingTrialDJ(trialHearingTrial);

        List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
        // copy of above method as to not break existing cases
        TrialHearingTimeDJ trialHearingTimeDJ = new TrialHearingTimeDJ();
        trialHearingTimeDJ.setHelpText1(
            "If either party considers that the time estimate is insufficient, "
                + "they must inform the court within 7 days of the date of "
                + "this order.");
        trialHearingTimeDJ.setHelpText2(
            "Not more than seven nor less than three clear days before the "
                + "trial, the claimant must file at court and serve an indexed "
                + "and paginated bundle of documents which complies with the "
                + "requirements of Rule 39.5 Civil Procedure Rules "
                + "and which complies with requirements of PD32. The parties "
                + "must endeavour to agree the contents of the bundle before it "
                + "is filed. The bundle will include a case summary and a "
                + "chronology.");
        trialHearingTimeDJ.setDateToToggle(dateToShowTrue);
        trialHearingTimeDJ.setDate1(LocalDate.now().plusWeeks(22));
        trialHearingTimeDJ.setDate2(LocalDate.now().plusWeeks(30));
        caseData.setTrialHearingTimeDJ(trialHearingTimeDJ);

        TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ = new TrialOrderMadeWithoutHearingDJ();
        trialOrderMadeWithoutHearingDJ.setInput(String.format(
            "This order has been made without a hearing. "
                + "Each party has the right to apply to have this Order "
                + "set aside or varied. Any such application must be "
                + "received by the Court "
                + "(together with the appropriate fee) by 4pm on %s.",
            deadlinesCalculator
                .plusWorkingDays(LocalDate.now(), 5)
                .format(DateTimeFormatter
                            .ofPattern(
                                "dd MMMM yyyy",
                                Locale.ENGLISH
                            ))
        ));
        caseData.setTrialOrderMadeWithoutHearingDJ(trialOrderMadeWithoutHearingDJ);

        TrialHearingNotes trialHearingNotes = new TrialHearingNotes();
        trialHearingNotes.setInput("This order has been made without a hearing. Each party has "
                                       + "the right to apply to have this order set "
                                       + "aside or varied."
                                       + " Any such application must be received by the court "
                                       + "(together with the appropriate fee) by 4pm on");
        trialHearingNotes.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)));
        caseData.setTrialHearingNotesDJ(trialHearingNotes);

        TrialBuildingDispute trialBuildingDispute = new TrialBuildingDispute();
        trialBuildingDispute.setInput1("The claimant must prepare a Scott Schedule of the defects,"
                                           + " items of damage "
                                           + "or any other relevant matters");
        trialBuildingDispute.setInput2("The columns should be headed: \n - Item \n - "
                                           + "Alleged Defect "
                                           + "\n - Claimant's costing\n - Defendant's"
                                           + " response\n - Defendant's costing"
                                           + " \n - Reserved for Judge's use");
        trialBuildingDispute.setInput3("The claimant must upload to the Digital Portal the "
                                           + "Scott Schedule with the relevant "
                                           + "columns completed by 4pm on");
        trialBuildingDispute.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        trialBuildingDispute.setInput4("The defendant must upload to the Digital Portal "
                                           + "an amended version of the Scott Schedule with the relevant"
                                           + " columns in response completed by 4pm on");
        trialBuildingDispute.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        caseData.setTrialBuildingDispute(trialBuildingDispute);

        TrialClinicalNegligence trialClinicalNegligence = new TrialClinicalNegligence();
        trialClinicalNegligence.setInput1("Documents should be retained as follows:");
        trialClinicalNegligence.setInput2("the parties must retain all electronically stored "
                                              + "documents relating to the issues in this Claim.");
        trialClinicalNegligence.setInput3("the defendant must retain the original clinical notes"
                                              + " relating to the issues in this Claim. "
                                              + "The defendant must give facilities for inspection "
                                              + "by the claimant, "
                                              + "the claimant's legal advisers and experts of these"
                                              + " original notes on 7 days written notice.");
        trialClinicalNegligence.setInput4("Legible copies of the medical and educational "
                                              + "records of the claimant are to be placed in a"
                                              + " separate paginated bundle by the claimant’s "
                                              + "solicitors and kept up to date. All references "
                                              + "to medical notes are to be made by reference to"
                                              + " the pages in that bundle");
        caseData.setTrialClinicalNegligence(trialClinicalNegligence);

        SdoDJR2TrialCreditHireDetails tempSdoDJR2TrialCreditHireDetails = new SdoDJR2TrialCreditHireDetails();
        tempSdoDJR2TrialCreditHireDetails.setInput2("The claimant must upload to the Digital Portal a witness "
                                                       + "statement addressing \na) the need to hire a replacement "
                                                       + "vehicle; and \nb) impecuniosity");
        tempSdoDJR2TrialCreditHireDetails.setInput3("This statement must be uploaded to the Digital Portal by 4pm on");
        tempSdoDJR2TrialCreditHireDetails.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        tempSdoDJR2TrialCreditHireDetails.setInput4("A failure to comply will result in the claimant being "
                                                       + "debarred from asserting need or relying on impecuniosity "
                                                       + "as the case may be at the final hearing, unless they "
                                                       + "have the permission of the trial Judge.");
        tempSdoDJR2TrialCreditHireDetails.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempSdoDJR2TrialCreditHireDetails.setInput5("The parties are to liaise and use reasonable endeavours to"
                                                       + " agree the basic hire rate no "
                                                       + "later than 4pm on");

        SdoDJR2TrialCreditHire sdoDJR2TrialCreditHire = new SdoDJR2TrialCreditHire();
        sdoDJR2TrialCreditHire.setInput1(
            "If impecuniosity is alleged by the claimant and not admitted "
                + "by the defendant, the claimant's "
                + "disclosure as ordered earlier in this order must "
                + "include:\n"
                + "a. Evidence of all income from all sources for a period "
                + "of 3 months prior to the "
                + "commencement of hire until the earlier of \n    i) 3 months "
                + "after cessation of hire or \n    ii) "
                + "the repair or replacement of the claimant's vehicle;\n"
                + "b. Copy statements of all bank, credit card and savings "
                + "account statements for a period of 3 months "
                + "prior to the commencement of hire until"
                + " the earlier of \n    i)"
                + " 3 months after cessation of hire "
                + "or \n    ii) the repair or replacement of the "
                + "claimant's vehicle;\n"
                + "c. Evidence of any loan, overdraft or other credit "
                + "facilities available to the claimant");
        sdoDJR2TrialCreditHire.setInput6(
            "If the parties fail to agree rates subject to liability "
                + "and/or other issues pursuant to the paragraph above, "
                + "each party may rely upon the written evidence by way of"
                + " witness statement of one witness to provide evidence of "
                + "basic hire rates available within the claimant’s "
                + "geographical"
                + " location from a mainstream supplier, or a local reputable "
                + "supplier if none is available. The defendant’s evidence is "
                + "to be uploaded to the Digital Portal by 4pm on");
        sdoDJR2TrialCreditHire.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        sdoDJR2TrialCreditHire.setInput7("and the claimant’s evidence in reply if "
                                             + "so advised is to be uploaded by 4pm on");
        sdoDJR2TrialCreditHire.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(14)));
        sdoDJR2TrialCreditHire.setInput8(
            "This witness statement is limited to 10 pages per party "
                + "(to include any appendices).");
        List<AddOrRemoveToggle> addOrRemoveToggleList = List.of(AddOrRemoveToggle.ADD);
        sdoDJR2TrialCreditHire.setDetailsShowToggle(addOrRemoveToggleList);
        sdoDJR2TrialCreditHire.setSdoDJR2TrialCreditHireDetails(tempSdoDJR2TrialCreditHireDetails);
        caseData.setSdoDJR2TrialCreditHire(sdoDJR2TrialCreditHire);

        TrialPersonalInjury trialPersonalInjury = new TrialPersonalInjury();
        trialPersonalInjury.setInput1("The claimant has permission to rely upon the written "
                                          + "expert evidence already uploaded to the Digital"
                                          + " Portal with the particulars of claim and in addition "
                                          + "has permission to rely upon any associated "
                                          + "correspondence or updating report which is uploaded "
                                          + "to the Digital Portal by 4pm on");
        trialPersonalInjury.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        trialPersonalInjury.setInput2("Any questions which are to be addressed to an expert must "
                                          + "be sent to the expert directly and"
                                          + " uploaded to the Digital "
                                          + "Portal by 4pm on");
        trialPersonalInjury.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        trialPersonalInjury.setInput3("The answers to the questions shall be answered "
                                          + "by the Expert by");
        trialPersonalInjury.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        trialPersonalInjury.setInput4("and uploaded to the Digital Portal by");
        trialPersonalInjury.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        caseData.setTrialPersonalInjury(trialPersonalInjury);

        TrialRoadTrafficAccident trialRoadTrafficAccident = new TrialRoadTrafficAccident();
        trialRoadTrafficAccident.setInput("Photographs and/or a plan of the accident location "
                                              + "shall be prepared "
                                              + "and agreed by the parties and uploaded to the"
                                              + " Digital Portal by 4pm on");
        trialRoadTrafficAccident.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        caseData.setTrialRoadTrafficAccident(trialRoadTrafficAccident);

        TrialHousingDisrepair trialHousingDisrepair = new TrialHousingDisrepair();
        trialHousingDisrepair.setInput1("The claimant must prepare a Scott Schedule of the items "
                                            + "in disrepair");
        trialHousingDisrepair.setInput2("The columns should be headed: \n - Item \n - "
                                            + "Alleged disrepair "
                                            + "\n - Defendant's Response \n - "
                                            + "Reserved for Judge's Use");
        trialHousingDisrepair.setInput3("The claimant must upload to the Digital Portal the "
                                            + "Scott Schedule with the relevant columns "
                                            + "completed by 4pm on");
        trialHousingDisrepair.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        trialHousingDisrepair.setInput4("The defendant must upload to the Digital Portal "
                                            + "the amended Scott Schedule with the relevant columns "
                                            + "in response completed by 4pm on");
        trialHousingDisrepair.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        caseData.setTrialHousingDisrepair(trialHousingDisrepair);

        SdoR2WelshLanguageUsage disposalWelshLanguageUsage = new SdoR2WelshLanguageUsage();
        disposalWelshLanguageUsage.setDescription(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
        caseData.setSdoR2DisposalHearingWelshLanguageDJ(disposalWelshLanguageUsage);
        SdoR2WelshLanguageUsage trialWelshLanguageUsage = new SdoR2WelshLanguageUsage();
        trialWelshLanguageUsage.setDescription(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
        caseData.setSdoR2TrialWelshLanguageDJ(trialWelshLanguageUsage);

        updateDisclosureOfDocumentFields(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void updateDisclosureOfDocumentFields(CaseData caseData) {
        TrialHearingDisclosureOfDocuments trialHearingDisclosureOfDocuments = new TrialHearingDisclosureOfDocuments();
        trialHearingDisclosureOfDocuments.setInput1("Standard disclosure shall be provided by "
                                                        + "the parties by uploading to the digital "
                                                        + "portal their lists of documents by 4pm on");
        trialHearingDisclosureOfDocuments.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        trialHearingDisclosureOfDocuments.setInput2("Any request to inspect a document, or for a copy of a "
                                                        + "document, shall be made directly to the other"
                                                        + " party by 4pm on");
        trialHearingDisclosureOfDocuments.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)));
        trialHearingDisclosureOfDocuments.setInput3("Requests will be complied with within 7 days of the"
                                                        + " receipt of the request");
        trialHearingDisclosureOfDocuments.setInput4("Each party must upload to the Digital Portal"
                                                        + " copies of those documents on which they wish to rely"
                                                        + " at trial");
        trialHearingDisclosureOfDocuments.setInput5("by 4pm on");
        trialHearingDisclosureOfDocuments.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        caseData.setTrialHearingDisclosureOfDocumentsDJ(trialHearingDisclosureOfDocuments);
    }

    private DynamicList getLocationList(CallbackParams callbackParams,
                                        RequestedCourt preferredCourt) {
        List<LocationRefData> locations = locationRefDataService.getCourtLocationsForDefaultJudgments(
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
            .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        DynamicList locationsList;
        if (matchingLocation.isPresent()) {
            locationsList = DynamicList.fromList(locations, this::getLocationEpimms, LocationReferenceDataService::getDisplayEntry,
                                                 matchingLocation.get(), true
            );
        } else {
            locationsList = DynamicList.fromList(locations, this::getLocationEpimms, LocationReferenceDataService::getDisplayEntry,
                                                 null, true
            );
        }
        return locationsList;
    }

    private String getLocationEpimms(LocationRefData location) {
        return location.getEpimmsId();
    }

    private CallbackResponse generateSDONotifications(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // Case File View will show any document uploaded even without an categoryID under uncategorized section,
        // we only use orderSDODocumentDJ as a preview and do not want it shown on case file view, so to prevent it
        // showing, we remove.
        caseData.setOrderSDODocumentDJ(null);
        assignCategoryId.assignCategoryIdToCollection(caseData.getOrderSDODocumentDJCollection(),
                                                      document -> document.getValue().getDocumentLink(), "caseManagementOrders");
        caseData.setBusinessProcess(BusinessProcess.ready(STANDARD_DIRECTION_ORDER_DJ));
        caseData.setHearingNotes(getHearingNotes(caseData));

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (featureToggleService.isWelshEnabledForMainCase()) {
                caseData.setEaCourtLocation(YES);
            } else {
                boolean isLipCase = caseData.isApplicantLiP() || caseData.isRespondent1LiP() || caseData.isRespondent2LiP();
                if (!isLipCase) {
                    log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
                    caseData.setEaCourtLocation(YES);
                } else {
                    boolean isLipCaseEaCourt = isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData);
                    caseData.setEaCourtLocation(isLipCaseEaCourt ? YesOrNo.YES : YesOrNo.NO);
                }
            }
        }

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                caseData
            ));
        }

        var state = "CASE_PROGRESSION";
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state(state)
            .build();
    }

    public boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne())
            && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation());
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private CallbackResponse createOrderScreen(CallbackParams callbackParams) {
        CaseData caseData = V_1.equals(callbackParams.getVersion())
            ? mapHearingMethodFields(callbackParams.getCaseData())
            : callbackParams.getCaseData();

        List<String> errors = new ArrayList<>();
        final String witnessValidationErrorMessage = validateInputValue(callbackParams);

        if (!witnessValidationErrorMessage.isEmpty()) {
            errors.add(witnessValidationErrorMessage);
        }

        if (errors.isEmpty()) {
            CaseDocument document = defaultJudgmentOrderFormGenerator.generate(
                caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
            caseData.setOrderSDODocumentDJ(document.getDocumentLink());

            List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
            systemGeneratedCaseDocuments.add(element(document));
            caseData.setOrderSDODocumentDJCollection(systemGeneratedCaseDocuments);
            caseData.setDisposalHearingMethodInPersonDJ(deleteLocationList(
                caseData.getDisposalHearingMethodInPersonDJ()));
            caseData.setTrialHearingMethodInPersonDJ(deleteLocationList(
                caseData.getTrialHearingMethodInPersonDJ()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CaseData mapHearingMethodFields(CaseData caseData) {

        if (caseData.getHearingMethodValuesDisposalHearingDJ() != null
            && caseData.getHearingMethodValuesDisposalHearingDJ().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearingDJ().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                caseData.setDisposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                caseData.setDisposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                caseData.setDisposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesTrialHearingDJ() != null
            && caseData.getHearingMethodValuesTrialHearingDJ().getValue() != null) {
            String trialHearingMethodLabel = caseData.getHearingMethodValuesTrialHearingDJ().getValue().getLabel();
            if (trialHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                caseData.setTrialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
            } else if (trialHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                caseData.setTrialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
            } else if (trialHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                caseData.setTrialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
            }
        }

        return caseData;
    }

    private CaseData fillDisposalToggle(CaseData caseData, List<DisposalAndTrialHearingDJToggle> checkList) {

        caseData.setDisposalHearingDisclosureOfDocumentsDJToggle(checkList);
        caseData.setDisposalHearingWitnessOfFactDJToggle(checkList);
        caseData.setDisposalHearingMedicalEvidenceDJToggle(checkList);
        caseData.setDisposalHearingQuestionsToExpertsDJToggle(checkList);
        caseData.setDisposalHearingSchedulesOfLossDJToggle(checkList);
        caseData.setDisposalHearingStandardDisposalOrderDJToggle(checkList);
        caseData.setDisposalHearingFinalDisposalHearingDJToggle(checkList);
        caseData.setDisposalHearingBundleDJToggle(checkList);
        caseData.setDisposalHearingClaimSettlingDJToggle(checkList);
        caseData.setDisposalHearingCostsDJToggle(checkList);
        return caseData;
    }

    private CaseData fillTrialToggle(CaseData caseData, List<DisposalAndTrialHearingDJToggle> checkList) {
        caseData.setTrialHearingAlternativeDisputeDJToggle(checkList);
        caseData.setTrialHearingVariationsDirectionsDJToggle(checkList);
        caseData.setTrialHearingSettlementDJToggle(checkList);
        caseData.setTrialHearingDisclosureOfDocumentsDJToggle(checkList);
        caseData.setTrialHearingWitnessOfFactDJToggle(checkList);
        caseData.setTrialHearingSchedulesOfLossDJToggle(checkList);
        caseData.setTrialHearingCostsToggle(checkList);
        caseData.setTrialHearingTrialDJToggle(checkList);

        return caseData;
    }

    private DynamicList deleteLocationList(DynamicList list) {
        if (isNull(list)) {
            return null;
        }
        DynamicList cleanedList = new DynamicList();
        cleanedList.setValue(list.getValue());
        return cleanedList;
    }

    private String validateInputValue(CallbackParams callbackParams) {
        final String errorMessage = "";
        CaseData caseData = callbackParams.getCaseData();
        if (nonNull(caseData.getTrialHearingWitnessOfFactDJ())) {
            String inputValue1 = caseData.getTrialHearingWitnessOfFactDJ().getInput2();
            String inputValue2 = caseData.getTrialHearingWitnessOfFactDJ().getInput3();
            List<String> errors = new ArrayList<>();
            if (inputValue1 != null && inputValue2 != null) {
                int number1 = Integer.parseInt(inputValue1);
                int number2 = Integer.parseInt(inputValue2);
                if (number1 < 0 || number2 < 0) {
                    return "The number entered cannot be less than zero";
                }
            }
        }
        return errorMessage;
    }
}
