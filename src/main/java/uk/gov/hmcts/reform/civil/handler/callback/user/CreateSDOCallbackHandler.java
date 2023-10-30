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
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle.SHOW;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderType.DISPOSAL;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_SDO);
    private static final String HEARING_CHANNEL = "HearingChannel";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";
    public static final String CONFIRMATION_HEADER = "# Your order has been issued"
        + "%n## Claim number: %s";
    public static final String CONFIRMATION_SUMMARY_1v1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    public static final String CONFIRMATION_SUMMARY_2v1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Claimant 2</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    public static final String CONFIRMATION_SUMMARY_1v2 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 2</strong>%n"
        + "<br/>%s";
    private static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    public static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";

    public static final String FEEDBACK_LINK = "<p>%s"
        + " <a href='https://www.smartsurvey.co.uk/s/QKJTVU//' target=_blank>here</a></p>";

    private final ObjectMapper objectMapper;
    private final LocationRefDataService locationRefDataService;
    @Autowired
    private final DeadlinesCalculator deadlinesCalculator;
    private final SdoGeneratorService sdoGeneratorService;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final AssignCategoryId assignCategoryId;
    private final CategoryService categoryService;
    private final  List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(V_1, ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
            .put(callbackKey(MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(MID, "validateInputValue"), this::validateInputValue)
            .put(callbackKey(V_1, MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // This is currently a mid event but once pre states are defined it should be moved to an about to start event.
    // Once it has been moved to an about to start event the following file will need to be updated:
    //  FlowStateAllowedEventService.java.
    // This way pressing previous on the ccd page won't end up calling this method again and thus
    // repopulating the fields if they have been changed.
    // There is no reason to add conditionals to avoid this here since having it as an about to start event will mean
    // it is only ever called once.
    // Then any changes to fields in ccd will persist in ccd regardless of backwards or forwards page navigation.
    private CallbackResponse prePopulateOrderDetailsPages(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updatedData
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);

        Optional<RequestedCourt> preferredCourt = locationHelper.getCaseManagementLocation(caseData);
        preferredCourt.map(RequestedCourt::getCaseLocation)
            .ifPresent(updatedData::caseManagementLocation);

        if (V_1.equals(callbackParams.getVersion())) {
            String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
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
                .collect(Collectors.toList());
            hearingMethodList.setListItems(hearingMethodListWithoutNotInAttendance);
            DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
                .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
            hearingMethodList.setValue(hearingMethodInPerson);
            updatedData.hearingMethodValuesFastTrack(hearingMethodList);
            updatedData.hearingMethodValuesDisposalHearing(hearingMethodList);
            updatedData.hearingMethodValuesSmallClaims(hearingMethodList);
        }

        DynamicList locationsList = getLocationList(callbackParams, updatedData, preferredCourt.orElse(null));
        updatedData.disposalHearingMethodInPerson(locationsList);
        updatedData.fastTrackMethodInPerson(locationsList);
        updatedData.smallClaimsMethodInPerson(locationsList);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(SHOW);
        setCheckList(updatedData, checkList);

        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = DisposalHearingJudgesRecital.builder()
            .input(UPON_CONSIDERING)
            .build();

        updatedData.disposalHearingJudgesRecital(tempDisposalHearingJudgesRecital).build();

        updateDeductionValue(caseData, updatedData);

        DisposalHearingDisclosureOfDocuments tempDisposalHearingDisclosureOfDocuments =
            DisposalHearingDisclosureOfDocuments.builder()
                .input1("The parties shall serve on each other copies of the documents upon which reliance is to be"
                            + " placed at the disposal hearing by 4pm on")
                .date1(LocalDate.now().plusWeeks(10))
                .input2("The parties must upload to the Digital Portal copies of those documents which they wish the "
                            + "court to consider when deciding the amount of damages, by 4pm on")
                .date2(LocalDate.now().plusWeeks(10))
                .build();

        updatedData.disposalHearingDisclosureOfDocuments(tempDisposalHearingDisclosureOfDocuments).build();

        DisposalHearingWitnessOfFact tempDisposalHearingWitnessOfFact = DisposalHearingWitnessOfFact.builder()
            .input3("The claimant must upload to the Digital Portal copies of the witness statements of all witnesses"
                        + " of fact on whose evidence reliance is to be placed by 4pm on")
            .date2(LocalDate.now().plusWeeks(4))
            .input4("The provisions of CPR 32.6 apply to such evidence.")
            .input5("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on")
            .date3(LocalDate.now().plusWeeks(6))
            .input6("and must be accompanied by proposed directions for allocation and listing for trial on quantum. "
                        + "This is because cross-examination will cause the hearing to exceed the 30-minute "
                        + "maximum time estimate for a disposal hearing.")
            .build();

        updatedData.disposalHearingWitnessOfFact(tempDisposalHearingWitnessOfFact).build();

        DisposalHearingMedicalEvidence tempDisposalHearingMedicalEvidence = DisposalHearingMedicalEvidence.builder()
            .input("The claimant has permission to rely upon the written expert evidence already uploaded to the"
                       + " Digital Portal with the particulars of claim and in addition has permission to rely upon"
                       + " any associated correspondence or updating report which is uploaded to the Digital Portal"
                       + " by 4pm on")
            .date(LocalDate.now().plusWeeks(4))
            .build();

        updatedData.disposalHearingMedicalEvidence(tempDisposalHearingMedicalEvidence).build();

        DisposalHearingQuestionsToExperts tempDisposalHearingQuestionsToExperts = DisposalHearingQuestionsToExperts
            .builder()
            .date(LocalDate.now().plusWeeks(6))
            .build();

        updatedData.disposalHearingQuestionsToExperts(tempDisposalHearingQuestionsToExperts).build();

        DisposalHearingSchedulesOfLoss tempDisposalHearingSchedulesOfLoss = DisposalHearingSchedulesOfLoss.builder()
            .input2("If there is a claim for ongoing or future loss in the original schedule of losses, the claimant"
                        + " must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
            .date2(LocalDate.now().plusWeeks(10))
            .input3("If the defendant wants to challenge this claim, "
                        + "they must send an up-to-date counter-schedule of loss to the claimant by 4pm on")
            .date3(LocalDate.now().plusWeeks(12))
            .input4("If the defendant want to challenge the sums claimed in the schedule of loss they must upload"
                        + " to the Digital Portal an updated counter schedule of loss by 4pm on")
            .date4(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.disposalHearingSchedulesOfLoss(tempDisposalHearingSchedulesOfLoss).build();

        DisposalHearingFinalDisposalHearing tempDisposalHearingFinalDisposalHearing =
            DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build();

        updatedData.disposalHearingFinalDisposalHearing(tempDisposalHearingFinalDisposalHearing).build();

        // updated Hearing time field copy of the above field, leaving above field in as requested to not break
        // existing cases
        DisposalHearingHearingTime tempDisposalHearingHearingTime =
            DisposalHearingHearingTime.builder()
                .input(
                    "This claim will be listed for final disposal before a judge on the first available date after")
                .dateTo(LocalDate.now().plusWeeks(16))
                .build();

        updatedData.disposalHearingHearingTime(tempDisposalHearingHearingTime).build();

        DisposalOrderWithoutHearing disposalOrderWithoutHearing = DisposalOrderWithoutHearing.builder()
            .input(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply to have this Order set "
                    + "aside or varied. Any such application must be received "
                    + "by the Court (together with the appropriate fee) "
                    + "by 4pm on %s.",
                deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            )).build();
        updatedData.disposalOrderWithoutHearing(disposalOrderWithoutHearing).build();

        DisposalHearingBundle tempDisposalHearingBundle = DisposalHearingBundle.builder()
            .input("At least 7 days before the disposal hearing, the claimant must file and serve")
            .build();

        updatedData.disposalHearingBundle(tempDisposalHearingBundle).build();

        DisposalHearingNotes tempDisposalHearingNotes = DisposalHearingNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order"
                       + " set aside or varied. Any such application must be uploaded to the Digital Portal"
                       + " together with the appropriate fee, by 4pm on")
            .date(LocalDate.now().plusWeeks(1))
            .build();

        updatedData.disposalHearingNotes(tempDisposalHearingNotes).build();

        FastTrackJudgesRecital tempFastTrackJudgesRecital = FastTrackJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.fastTrackJudgesRecital(tempFastTrackJudgesRecital).build();

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(LocalDate.now().plusWeeks(6))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(LocalDate.now().plusWeeks(8))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();

        FastTrackWitnessOfFact tempFastTrackWitnessOfFact = FastTrackWitnessOfFact.builder()
            .input1("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                        + "fact on whom they intend to rely.")
            .input2("3")
            .input3("3")
            .input4("For this limitation, a party is counted as a witness.")
            .input5("Each witness statement should be no more than")
            .input6("10")
            .input7("A4 pages. Statements should be double spaced using a font size of 12.")
            .input8("Witness statements shall be uploaded to the Digital Portal by 4pm on")
            .date(LocalDate.now().plusWeeks(8))
            .input9("Evidence will not be permitted at trial from a witness whose statement has not been uploaded "
                        + "in accordance with this Order. Evidence not uploaded, or uploaded late, will not be "
                        + "permitted except with permission from the Court.")
            .build();

        updatedData.fastTrackWitnessOfFact(tempFastTrackWitnessOfFact).build();

        FastTrackSchedulesOfLoss tempFastTrackSchedulesOfLoss = FastTrackSchedulesOfLoss.builder()
            .input1("The claimant must upload to the Digital Portal an up-to-date schedule of loss to the "
                        + "defendant by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input2("If the defendant wants to challenge this claim, upload to the Digital Portal "
                        + "counter-schedule of loss by 4pm on")
            .date2(LocalDate.now().plusWeeks(12))
            .input3("If there is a claim for future pecuniary loss and the parties have not already set out "
                        + "their case on periodical payments, they must do so in the respective schedule and "
                        + "counter-schedule.")
            .build();

        updatedData.fastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss).build();

        FastTrackTrial tempFastTrackTrial = FastTrackTrial.builder()
            .input1("The time provisionally allowed for this trial is")
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .input2("If either party considers that the time estimate is insufficient, they must inform the court "
                        + "within 7 days of the date stated on this order.")
            .input3("At least 7 days before the trial, the claimant must upload to the Digital Portal")
            .type(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS))
            .build();

        updatedData.fastTrackTrial(tempFastTrackTrial).build();

        FastTrackHearingTime tempFastTrackHearingTime = FastTrackHearingTime.builder()
            .dateFrom(LocalDate.now().plusWeeks(22))
            .dateTo(LocalDate.now().plusWeeks(30))
            .dateToToggle(dateToShowTrue)
            .helpText1("If either party considers that the time estimate is insufficient, "
                           + "they must inform the court within 7 days of the date of this order.")
            .helpText2("Not more than seven nor less than three clear days before the trial, "
                           + "the claimant must file at court and serve an indexed and paginated bundle of "
                           + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                           + "and which complies with requirements of PD32. The parties must endeavour to agree "
                           + "the contents of the bundle before it is filed. The bundle will include a case "
                           + "summary and a chronology.")
            .build();
        updatedData.fastTrackHearingTime(tempFastTrackHearingTime);

        FastTrackNotes tempFastTrackNotes = FastTrackNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                       + "set aside or varied. Any application must be received by the Court, "
                       + "together with the appropriate fee by 4pm on")
            .date(LocalDate.now().plusWeeks(1))
            .build();

        updatedData.fastTrackNotes(tempFastTrackNotes).build();

        FastTrackOrderWithoutJudgement tempFastTrackOrderWithoutJudgement = FastTrackOrderWithoutJudgement.builder()
            .input(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply "
                    + "to have this Order set aside or varied. Any such application must be "
                    + "received by the Court (together with the appropriate fee) by 4pm "
                    + "on %s.",
                deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(LocalDateTime.now())
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            ))
            .build();

        updatedData.fastTrackOrderWithoutJudgement(tempFastTrackOrderWithoutJudgement);

        FastTrackBuildingDispute tempFastTrackBuildingDispute = FastTrackBuildingDispute.builder()
            .input1("The claimant must prepare a Scott Schedule of the defects, items of damage, "
                        + "or any other relevant matters")
            .input2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged defect\n"
                        + "  •  Claimant’s costing\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Defendant’s costing\n"
                        + "  •  Reserved for Judge’s use")
            .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns"
                        + " completed by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input4("The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
                        + "with the relevant columns in response completed by 4pm on")
            .date2(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.fastTrackBuildingDispute(tempFastTrackBuildingDispute).build();

        FastTrackClinicalNegligence tempFastTrackClinicalNegligence = FastTrackClinicalNegligence.builder()
            .input1("Documents should be retained as follows:")
            .input2("a) The parties must retain all electronically stored documents relating to the issues in this "
                        + "claim.")
            .input3("b) the defendant must retain the original clinical notes relating to the issues in this claim. "
                        + "The defendant must give facilities for inspection by the claimant, the claimant's legal "
                        + "advisers and experts of these original notes on 7 days written notice.")
            .input4("c) Legible copies of the medical and educational records of the claimant "
                        + "are to be placed in a separate paginated bundle by the claimant's "
                        + "solicitors and kept up to date. All references to medical notes are to be made by reference "
                        + "to the pages in that bundle.")
            .build();

        updatedData.fastTrackClinicalNegligence(tempFastTrackClinicalNegligence).build();

        FastTrackCreditHire tempFastTrackCreditHire = FastTrackCreditHire.builder()
            .input1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.")
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(LocalDate.now().plusWeeks(4))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                        + "later than 4pm on")
            .date2(LocalDate.now().plusWeeks(6))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(LocalDate.now().plusWeeks(8))
            .input7("and the claimant's evidence in reply if so advised to be uploaded by 4pm on")
            .date4(LocalDate.now().plusWeeks(10))
            .input8("This witness statement is limited to 10 pages per party, including any appendices.")
            .build();

        updatedData.fastTrackCreditHire(tempFastTrackCreditHire).build();

        FastTrackHousingDisrepair tempFastTrackHousingDisrepair = FastTrackHousingDisrepair.builder()
            .input1("The claimant must prepare a Scott Schedule of the items in disrepair.")
            .input2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged disrepair\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Reserved for Judge’s use")
            .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
                        + "columns completed by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input4("The defendant must upload to the Digital Portal the amended Scott Schedule with the "
                        + "relevant columns in response completed by 4pm on")
            .date2(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.fastTrackHousingDisrepair(tempFastTrackHousingDisrepair).build();

        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
            .input1("The claimant has permission to rely upon the written expert evidence already uploaded to "
                        + "the Digital Portal with the particulars of claim and in addition has permission to rely upon"
                        + " any associated correspondence or updating report which is uploaded to the Digital Portal by"
                        + " 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("Any questions which are to be addressed to an expert must be sent to the expert directly "
                        + "and uploaded to the Digital Portal by 4pm on")
            .date2(LocalDate.now().plusWeeks(4))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(LocalDate.now().plusWeeks(8))
            .input4("and uploaded to the Digital Portal by")
            .date4(LocalDate.now().plusWeeks(8))
            .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();

        FastTrackRoadTrafficAccident tempFastTrackRoadTrafficAccident = FastTrackRoadTrafficAccident.builder()
            .input("Photographs and/or a place of the accident location shall be prepared and agreed by the "
                       + "parties and uploaded to the Digital Portal by 4pm on")
            .date(LocalDate.now().plusWeeks(8))
            .build();

        updatedData.fastTrackRoadTrafficAccident(tempFastTrackRoadTrafficAccident).build();

        SmallClaimsJudgesRecital tempSmallClaimsJudgesRecital = SmallClaimsJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.smallClaimsJudgesRecital(tempSmallClaimsJudgesRecital).build();

        SmallClaimsDocuments tempSmallClaimsDocuments = SmallClaimsDocuments.builder()
            .input1("Each party must upload to the Digital Portal copies of all documents which they wish the court to"
                        + " consider when reaching its decision not less than 14 days before the hearing.")
            .input2("The court may refuse to consider any document which has not been uploaded to the "
                        + "Digital Portal by the above date.")
            .build();

        updatedData.smallClaimsDocuments(tempSmallClaimsDocuments).build();

        SmallClaimsWitnessStatement tempSmallClaimsWitnessStatement = SmallClaimsWitnessStatement.builder()
            .smallClaimsNumberOfWitnessesToggle(checkList)
            .input1("Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                        + " upon whose evidence they intend to rely at the hearing not less than 14 days before"
                        + " the hearing.")
            .input2("2")
            .input3("2")
            .input4("For this limitation, a party is counted as a witness.")
            .text("A witness statement must: \na) Start with the name of the case and the claim number;"
                      + "\nb) State the full name and address of the witness; "
                      + "\nc) Set out the witness's evidence clearly in numbered paragraphs on numbered pages;"
                      + "\nd) End with this paragraph: 'I believe that the facts stated in this witness "
                      + "statement are true. I understand that proceedings for contempt of court may be "
                      + "brought against anyone who makes, or causes to be made, a false statement in a "
                      + "document verified by a statement of truth without an honest belief in its truth'."
                      + "\ne) be signed by the witness and dated."
                      + "\nf) If a witness is unable to read the statement there must be a certificate that "
                      + "it has been read or interpreted to the witness by a suitably qualified person and "
                      + "at the final hearing there must be an independent interpreter who will not be "
                      + "provided by the Court."
                      + "\n\nThe judge may refuse to allow a witness to give evidence or consider any "
                      + "statement of any witness whose statement has not been uploaded to the Digital Portal in "
                      + "accordance with the paragraphs above."
                      + "\n\nA witness whose statement has been uploaded in accordance with the above must attend "
                      + "the hearing. If they do not attend, it will be for the court to decide how much "
                      + "reliance, if any, to place on their evidence.")
            .build();

        updatedData.smallClaimsWitnessStatement(tempSmallClaimsWitnessStatement).build();

        SmallClaimsHearing tempSmallClaimsHearing = SmallClaimsHearing.builder()
            .input1("The hearing of the claim will be on a date to be notified to you by a separate notification. "
                        + "The hearing will have a time estimate of")
            .input2(HEARING_TIME_TEXT_AFTER)
            .build();

        updatedData.smallClaimsHearing(tempSmallClaimsHearing).build();

        SmallClaimsNotes.SmallClaimsNotesBuilder tempSmallClaimsNotes = SmallClaimsNotes.builder();
        tempSmallClaimsNotes.input("This order has been made without hearing. "
                                       + "Each party has the right to apply to have this Order set aside or varied. "
                                       + "Any such application must be received by the Court "
                                       + "(together with the appropriate fee) by 4pm on "
                                       + DateFormatHelper.formatLocalDate(
            deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5), DATE)
        );

        updatedData.smallClaimsNotes(tempSmallClaimsNotes.build()).build();

        SmallClaimsCreditHire tempSmallClaimsCreditHire = SmallClaimsCreditHire.builder()
            .input1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.")
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(LocalDate.now().plusWeeks(4))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                        + "later than 4pm on")
            .date2(LocalDate.now().plusWeeks(6))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(LocalDate.now().plusWeeks(8))
            .input7("and the claimant's evidence in reply if so advised to be uploaded by 4pm on")
            .date4(LocalDate.now().plusWeeks(10))
            .input11("This witness statement is limited to 10 pages per party, including any appendices.")
            .build();

        updatedData.smallClaimsCreditHire(tempSmallClaimsCreditHire).build();

        SmallClaimsRoadTrafficAccident tempSmallClaimsRoadTrafficAccident = SmallClaimsRoadTrafficAccident.builder()
            .input("Photographs and/or a place of the accident location shall be prepared and agreed by the parties"
                       + " and uploaded to the Digital Portal no later than 14 days before the hearing.")
            .build();

        updatedData.smallClaimsRoadTrafficAccident(tempSmallClaimsRoadTrafficAccident).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue);

                FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();

                SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
            });
    }

    /**
     * Creates the dynamic list for the hearing location, pre-selecting the preferred court if possible.
     *
     * @param callbackParams callback params
     * @param updatedData    updated case data
     * @param preferredCourt (optional) preferred court if any
     * @return dynamic list, with a value selected if appropriate and possible
     */
    private DynamicList getLocationList(CallbackParams callbackParams,
                                        CaseData.CaseDataBuilder<?, ?> updatedData,
                                        RequestedCourt preferredCourt) {
        List<LocationRefData> locations = locationRefDataService.getCourtLocationsForDefaultJudgments(
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
            .flatMap(requestedCourt -> locationHelper.updateCaseManagementLocation(
                updatedData,
                requestedCourt,
                () -> locations
            ));
        DynamicList locationsList;
        if (matchingLocation.isPresent()) {
            locationsList = DynamicList.fromList(locations, this::getLocationEpimms, LocationRefDataService::getDisplayEntry,
                                                 matchingLocation.get(), true
            );
        } else {
            locationsList = DynamicList.fromList(locations, this::getLocationEpimms, LocationRefDataService::getDisplayEntry,
                                                 null, true
            );
        }
        return locationsList;
    }

    private String getLocationEpimms(LocationRefData location) {
        return location.getEpimmsId();
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        updateDeductionValue(caseData, updatedData);

        updatedData.setSmallClaimsFlag(YesOrNo.NO).build();
        updatedData.setFastTrackFlag(YesOrNo.NO).build();

        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            updatedData.setSmallClaimsFlag(YES).build();
        } else if (SdoHelper.isFastTrack(caseData)) {
            updatedData.setFastTrackFlag(YES).build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        CaseData caseData = V_1.equals(callbackParams.getVersion())
            ? mapHearingMethodFields(callbackParams.getCaseData())
            : callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        CaseDocument document = sdoGeneratorService.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        if (document != null) {
            updatedData.sdoOrderDocument(document);
        }
        assignCategoryId.assignCategoryIdToCaseDocument(document, "sdo");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CaseData mapHearingMethodFields(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (caseData.getHearingMethodValuesDisposalHearing() != null
            && caseData.getHearingMethodValuesDisposalHearing().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearing().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesFastTrack() != null
            && caseData.getHearingMethodValuesFastTrack().getValue() != null) {
            String fastTrackHearingMethodLabel = caseData.getHearingMethodValuesFastTrack().getValue().getLabel();
            if (fastTrackHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesSmallClaims() != null
            && caseData.getHearingMethodValuesSmallClaims().getValue() != null) {
            String smallClaimsHearingMethodLabel = caseData.getHearingMethodValuesSmallClaims().getValue().getLabel();
            if (smallClaimsHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
            }
        }

        return updatedData.build();
    }

    private String getHearingInPersonSmall(CaseData caseData) {
        if (caseData.getSmallClaimsMethod() == SmallClaimsMethod.smallClaimsMethodInPerson
            && Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
            .map(DynamicList::getValue).isPresent()) {
            return caseData.getSmallClaimsMethodInPerson().getValue().getLabel();
        }
        return null;
    }

    private String getHearingInPersonFast(CaseData caseData) {
        if (caseData.getFastTrackMethod() == FastTrackMethod.fastTrackMethodInPerson
            && Optional.ofNullable(caseData.getFastTrackMethodInPerson())
            .map(DynamicList::getValue).isPresent()) {
            return caseData.getFastTrackMethodInPerson().getValue().getLabel();
        }
        return null;
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);

        CaseData caseData = callbackParams.getCaseData();

        CaseDocument document = caseData.getSdoOrderDocument();
        if (document != null) {
            List<Element<CaseDocument>> generatedDocuments = callbackParams.getCaseData()
                .getSystemGeneratedCaseDocuments();
            generatedDocuments.add(element(document));
            dataBuilder.systemGeneratedCaseDocuments(generatedDocuments);
        }
        // null/remove preview SDO document, otherwise it will show as duplicate within case file view
        dataBuilder.sdoOrderDocument(null);

        dataBuilder.hearingNotes(getHearingNotes(caseData));

        if (featureToggleService.isEarlyAdoptersEnabled()) {
            if (featureToggleService.isLocationWhiteListedForCaseProgression(
                getEpimmsId(caseData))) {
                log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
                dataBuilder.eaCourtLocation(YES);
            } else {
                log.info("Case {} is NOT whitelisted for case progression.", caseData.getCcdCaseReference());
                dataBuilder.eaCourtLocation(YesOrNo.NO);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private String getEpimmsId(CaseData caseData) {

        if (caseData.getOrderType() != null && caseData.getOrderType().equals(DISPOSAL)) {
            return caseData.getDisposalHearingMethodInPerson().getValue().getCode();
        }
        if (caseData.getSetFastTrackFlag() != null && caseData.getSetFastTrackFlag().equals(YES)) {
            return caseData.getFastTrackMethodInPerson().getValue().getCode();
        }
        if (caseData.getSetSmallClaimsFlag() != null && caseData.getSetSmallClaimsFlag().equals(YES)) {
            return caseData.getSmallClaimsMethodInPerson().getValue().getCode();
        }
        throw new IllegalArgumentException("Could not determine claim track");
    }

    private boolean nonNull(Object object) {
        if (object != null) {
            return true;
        } else {
            return false;
        }
    }

    private CallbackResponse validateInputValue(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        //CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();
        if (nonNull(caseData.getSmallClaimsWitnessStatement())) {
            String inputValue1 = caseData.getSmallClaimsWitnessStatement().getInput2();
            String inputValue2 = caseData.getSmallClaimsWitnessStatement().getInput3();
            if (validateNegativeWitness(errors, inputValue1, inputValue2)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build();
            }
        } else if (nonNull(caseData.getFastTrackWitnessOfFact())) {
            String inputValue1 = caseData.getFastTrackWitnessOfFact().getInput2();
            String inputValue2 = caseData.getFastTrackWitnessOfFact().getInput3();
            if (validateNegativeWitness(errors, inputValue1, inputValue2)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build();
            }
        }
        return generateSdoOrder(callbackParams);
    }

    private boolean validateNegativeWitness(List<String> errors, String inputValue1, String inputValue2) {
        if (inputValue1 != null && inputValue2 != null) {
            int number1 = Integer.parseInt(inputValue1);
            int number2 = Integer.parseInt(inputValue2);
            if (number1 < 0 || number2 < 0) {
                errors.add("The number entered cannot be less than zero");
                return true;
            }
        }
        return false;
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();

        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SDO));

        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(
            CONFIRMATION_HEADER,
            caseData.getLegacyCaseReference()
        );
    }

    private String getBody(CaseData caseData) {
        String applicant1Name = caseData.getApplicant1().getPartyName();
        String respondent1Name = caseData.getRespondent1().getPartyName();
        Party applicant2 = caseData.getApplicant2();
        Party respondent2 = caseData.getRespondent2();

        String initialBody = format(
            CONFIRMATION_SUMMARY_1v1,
            applicant1Name,
            respondent1Name
        );

        if (applicant2 != null) {
            initialBody =  format(
                CONFIRMATION_SUMMARY_2v1,
                applicant1Name,
                applicant2.getPartyName(),
                respondent1Name
            );
        } else if (respondent2 != null) {
            initialBody =  format(
                CONFIRMATION_SUMMARY_1v2,
                applicant1Name,
                respondent1Name,
                respondent2.getPartyName()
            );
        }
        String body = initialBody + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");
        return body;
    }

    private void setCheckList(
        CaseData.CaseDataBuilder<?, ?> updatedData,
        List<OrderDetailsPagesSectionsToggle> checkList
    ) {
        updatedData.fastTrackAltDisputeResolutionToggle(checkList);
        updatedData.fastTrackVariationOfDirectionsToggle(checkList);
        updatedData.fastTrackSettlementToggle(checkList);
        updatedData.fastTrackDisclosureOfDocumentsToggle(checkList);
        updatedData.fastTrackWitnessOfFactToggle(checkList);
        updatedData.fastTrackSchedulesOfLossToggle(checkList);
        updatedData.fastTrackCostsToggle(checkList);
        updatedData.fastTrackTrialToggle(checkList);
        updatedData.fastTrackMethodToggle(checkList);
        updatedData.disposalHearingDisclosureOfDocumentsToggle(checkList);
        updatedData.disposalHearingWitnessOfFactToggle(checkList);
        updatedData.disposalHearingMedicalEvidenceToggle(checkList);
        updatedData.disposalHearingQuestionsToExpertsToggle(checkList);
        updatedData.disposalHearingSchedulesOfLossToggle(checkList);
        updatedData.disposalHearingFinalDisposalHearingToggle(checkList);
        updatedData.disposalHearingMethodToggle(checkList);
        updatedData.disposalHearingBundleToggle(checkList);
        updatedData.disposalHearingClaimSettlingToggle(checkList);
        updatedData.disposalHearingCostsToggle(checkList);
        updatedData.smallClaimsHearingToggle(checkList);
        updatedData.smallClaimsMethodToggle(checkList);
        updatedData.smallClaimsDocumentsToggle(checkList);
        updatedData.smallClaimsWitnessStatementToggle(checkList);
    }

}
