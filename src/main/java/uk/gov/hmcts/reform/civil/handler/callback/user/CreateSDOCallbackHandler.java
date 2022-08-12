package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingPreferredEmail;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingPreferredTelephone;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPreferredEmail;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPreferredTelephone;
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
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class CreateSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_SDO);
    public static final String CONFIRMATION_HEADER = "# Your order has been issued"
        + "<br/>%n%nClaim number"
        + "<br/><strong>%s</strong>";
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

    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final SdoGeneratorService sdoGeneratorService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "order-details"), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
            .put(callbackKey(MID, "generate-sdo-order"), this::generateSdoOrder)
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

        updatedData.disposalHearingMethodInPerson(fromList(fetchLocationData(callbackParams)));
        updatedData.fastTrackMethodInPerson(fromList(fetchLocationData(callbackParams)));
        updatedData.smallClaimsMethodInPerson(fromList(fetchLocationData(callbackParams)));

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(OrderDetailsPagesSectionsToggle.SHOW);

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
        updatedData.disposalHearingApplicationsOrderToggle(checkList);
        updatedData.smallClaimsHearingToggle(checkList);
        updatedData.smallClaimsMethodToggle(checkList);
        updatedData.smallClaimsDocumentsToggle(checkList);
        updatedData.smallClaimsWitnessStatementToggle(checkList);

        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = DisposalHearingJudgesRecital.builder()
            .input("Upon considering the claim Form and Particulars of Claim/statements of case"
                       + " [and the directions questionnaires] \n\nIT IS ORDERED that:-")
            .build();

        updatedData.disposalHearingJudgesRecital(tempDisposalHearingJudgesRecital).build();

        JudgementSum judgementSum = caseData.getDrawDirectionsOrder();

        if (judgementSum != null) {
            DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                DisposalHearingJudgementDeductionValue.builder()
                    .value(judgementSum.getJudgementSum().toString() + "%")
                    .build();

            updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue).build();
        }

        DisposalHearingDisclosureOfDocuments tempDisposalHearingDisclosureOfDocuments =
            DisposalHearingDisclosureOfDocuments.builder()
            .input("The parties shall serve on each other copies of the documents upon which reliance is to be"
                       + " placed at the disposal hearing by 4pm on")
            .date(LocalDate.now().plusWeeks(4))
            .build();

        updatedData.disposalHearingDisclosureOfDocuments(tempDisposalHearingDisclosureOfDocuments).build();

        DisposalHearingWitnessOfFact tempDisposalHearingWitnessOfFact = DisposalHearingWitnessOfFact.builder()
            .input1("The claimant shall serve on every other party the witness statements of all witnesses of fact"
                        + " on whose evidence reliance is to be placed by 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("The provisions of CPR 32.6 apply to such evidence.")
            .input3("Any application by the defendant/s pursuant to CPR 32.7 must be made by 4pm on")
            .date2(LocalDate.now().plusWeeks(2))
            .input4("and must be accompanied by proposed directions for allocation and listing for trial on quantum as"
                        + " cross-examination will result in the hearing exceeding the 30 minute maximum time estimate"
                        + " for a disposal hearing")
            .build();

        updatedData.disposalHearingWitnessOfFact(tempDisposalHearingWitnessOfFact).build();

        DisposalHearingMedicalEvidence tempDisposalHearingMedicalEvidence = DisposalHearingMedicalEvidence.builder()
            .input1("The claimant has permission to rely upon the written expert evidence served with the"
                        + " Particulars of Claim to be disclosed by 4pm")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("and any associated correspondence and/or updating report disclosed not later than 4pm on the")
            .date2(LocalDate.now().plusWeeks(4))
            .build();

        updatedData.disposalHearingMedicalEvidence(tempDisposalHearingMedicalEvidence).build();

        DisposalHearingQuestionsToExperts tempDisposalHearingQuestionsToExperts = DisposalHearingQuestionsToExperts
            .builder()
            .date(LocalDate.now().plusWeeks(6))
            .build();

        updatedData.disposalHearingQuestionsToExperts(tempDisposalHearingQuestionsToExperts).build();

        DisposalHearingSchedulesOfLoss tempDisposalHearingSchedulesOfLoss = DisposalHearingSchedulesOfLoss.builder()
            .input1("If there is a claim for ongoing/future loss in the original schedule of losses then the claimant"
                        + " must send an up to date schedule of loss to the defendant by 4pm on the")
            .date1(LocalDate.now().plusWeeks(10))
            .input2("The defendant, in the event of challenge, must send an up to date counter-schedule of loss"
                        + " to the claimant by 4pm on the")
            .date2(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.disposalHearingSchedulesOfLoss(tempDisposalHearingSchedulesOfLoss).build();

        DisposalHearingStandardDisposalOrder tempDisposalHearingStandardDisposalOrder =
            DisposalHearingStandardDisposalOrder.builder()
            .input("input")
            .build();

        updatedData.disposalHearingStandardDisposalOrder(tempDisposalHearingStandardDisposalOrder).build();

        DisposalHearingFinalDisposalHearing tempDisposalHearingFinalDisposalHearing =
            DisposalHearingFinalDisposalHearing.builder()
            .input("This claim be listed for final disposal before a Judge on the first available date after.")
            .date(LocalDate.now().plusWeeks(16))
            .build();

        updatedData.disposalHearingFinalDisposalHearing(tempDisposalHearingFinalDisposalHearing).build();

        HearingSupportRequirementsDJ hearingSupportRequirementsDJ = caseData.getHearingSupportRequirementsDJ();

        String preferredTelephone = hearingSupportRequirementsDJ != null
            ? hearingSupportRequirementsDJ.getHearingPreferredTelephoneNumber1() : "N/A";

        DisposalHearingPreferredTelephone tempDisposalHearingPreferredTelephone = DisposalHearingPreferredTelephone
            .builder()
            .telephone(preferredTelephone)
            .build();

        updatedData.disposalHearingPreferredTelephone(tempDisposalHearingPreferredTelephone).build();

        String preferredEmail = hearingSupportRequirementsDJ != null
            ? hearingSupportRequirementsDJ.getHearingPreferredEmail() : "N/A";

        DisposalHearingPreferredEmail tempDisposalHearingPreferredEmail = DisposalHearingPreferredEmail
            .builder()
            .email(preferredEmail)
            .build();

        updatedData.disposalHearingPreferredEmail(tempDisposalHearingPreferredEmail).build();

        DisposalHearingBundle tempDisposalHearingBundle = DisposalHearingBundle.builder()
            .input("The claimant must lodge at court at least 7 days before the disposal")
            .build();

        updatedData.disposalHearingBundle(tempDisposalHearingBundle).build();

        DisposalHearingNotes tempDisposalHearingNotes = DisposalHearingNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order"
                       + " set aside or varied. Any such application must be received by the Court"
                       + " (together with the appropriate fee) by 4pm on")
            .date(LocalDate.now().plusWeeks(1))
            .build();

        updatedData.disposalHearingNotes(tempDisposalHearingNotes).build();

        FastTrackJudgesRecital tempFastTrackJudgesRecital = FastTrackJudgesRecital.builder()
            .input("District Judge Perna has considered the statements of case and the information provided by the "
                       + "parties,"
                       + " \n\nIT IS ORDERED that:-")
            .build();

        updatedData.fastTrackJudgesRecital(tempFastTrackJudgesRecital).build();

        if (judgementSum != null) {
            FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                FastTrackJudgementDeductionValue.builder()
                    .value(judgementSum.getJudgementSum().toString() + "%")
                    .build();

            updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();
        }

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("By serving a list with a disclosure statement by 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("Any request to inspect or for a copy of a document shall be made by 4pm on")
            .date2(LocalDate.now().plusWeeks(6))
            .input3("and complied with within 7 days of receipt of the request.")
            .input4("Each party must serve and file with the court a list of issues relevant to the search for and "
                        + "disclosure of electronically stored documents, or must confirm there are no such issues, "
                        + "following Civil Procedure Rule Practise Direction 31B.")
            .input5("By 4pm on")
            .date3(LocalDate.now().plusWeeks(4))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();

        FastTrackWitnessOfFact tempFastTrackWitnessOfFact = FastTrackWitnessOfFact.builder()
            .input1("Each party shall serve on every other party the witness statements of all "
                        + "witnesses of fact on whom he intends to rely")
            .input2("All statements to be no more than")
            .input3("")
            .input4("pages long, A4, double spaced and in font size 12.")
            .input5("There shall be simultaneous exchange of such statements by 4pm on")
            .date(LocalDate.now().plusWeeks(8))
            .input6("Oral evidence will not be permitted at trail from a witness whose statement has not been served "
                        + "in accordance with this order or has been served late, except with "
                        + "permission from the Court.")
            .build();

        updatedData.fastTrackWitnessOfFact(tempFastTrackWitnessOfFact).build();

        FastTrackSchedulesOfLoss tempFastTrackSchedulesOfLoss = FastTrackSchedulesOfLoss.builder()
            .input1("The claimant shall serve an updated schedule of loss on the defendant(s) by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input2("The defendant(s) shall serve a counter schedule on the Claimant by 4pm on")
            .date2(LocalDate.now().plusWeeks(12))
            .input3("If there is a claim for future pecuniary loss and the parties have not already set out their case "
                        + "on periodical payments, then they must do so in the respective schedule "
                        + "and counter-schedule.")
            .input4("Upon it being noted that the schedule of loss contains no claim for continuing loss and is "
                        + "therefore final, no further schedule of loss shall be served without permission to amend. "
                        + "The defendant shall file a counter-schedule of loss by 4pm on")
            .date3(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.fastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss).build();

        FastTrackTrial tempFastTrackTrial = FastTrackTrial.builder()
            .input1("The time provisionally allowed for the trial is")
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .input2("If either party considers that the time estimate is insufficient, they must inform the court "
                        + "within 7 days of the date of this Order.")

            .input3("Not more than seven nor less than three clear days before the trial, "
                        + "the claimant must file at court and serve an indexed and paginated bundle of "
                        + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules and "
                        + "Practice Direction 39A. The parties must endeavour to agree the contents of the bundle "
                        + "before it is filed. the bundle will include a case summary and a chronology.")
            .build();

        updatedData.fastTrackTrial(tempFastTrackTrial).build();

        FastTrackNotes tempFastTrackNotes = FastTrackNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                        + "set aside or varied. Any such application must be received by the Court "
                        + "(together with the appropriate fee) by 4pm on")
            .date(LocalDate.now().plusWeeks(1))
            .build();

        updatedData.fastTrackNotes(tempFastTrackNotes).build();

        FastTrackBuildingDispute tempFastTrackBuildingDispute = FastTrackBuildingDispute.builder()
            .input1("The claimant must prepare a Scott Schedule of the defects, items of damage "
                        + "or any other relevant matters")
            .input2("The column headings will be as follows: Item; Alleged Defect; claimant's Costing; "
                        + "defendant's Response; defendant's Costing; Reserved for Judge's Use")
            .input3("The claimant must serve the Scott Schedule with the relevant columns completed by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input4("The defendant must file and serve the Scott Schedule with the relevant columns "
                        + "in response completed by 4pm on")
            .date2(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.fastTrackBuildingDispute(tempFastTrackBuildingDispute).build();

        FastTrackClinicalNegligence tempFastTrackClinicalNegligence = FastTrackClinicalNegligence.builder()
            .input1("Documents are to be retained as follows:")
            .input2("the parties must retain all electronically stored documents relating to the issues in this Claim.")
            .input3("the defendant must retain the original clinical notes relating to the issues in this Claim. "
                        + "The defendant must give facilities for inspection by the claimant, "
                        + "the claimant's legal advisers and experts of these original notes on 7 days written notice.")
            .input4("Legible copies of the medical and educational records of the claimant / Deceased / "
                        + "claimant's Mother are to be placed in a separate paginated bundle by the "
                        + "claimant's Solicitors and kept up to date. All references to medical notes are to be made "
                        + "by reference to the pages in that bundle.")
            .build();

        updatedData.fastTrackClinicalNegligence(tempFastTrackClinicalNegligence).build();

        FastTrackCreditHire tempFastTrackCreditHire = FastTrackCreditHire.builder()
            .input1("1. If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this order must include:\n"
                        + "a. Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of i) 3 months after cessation of hire or ii) "
                        + "the repair/replacement of the claimant's vehicle;\n"
                        + "b. Copy statements of all blank, credit care and savings accounts for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of i) 3 months after cessation of hire "
                        + "or ii) the repair/replacement of the claimant's vehicle;\n"
                        + "c. Evidence of any loan, overdraft or other credit facilities available to the claimant")
            .input2("3. The claimant must file and serve a witness statement addressing, (a) need to hire a replacement"
                        + " vehicle and (b) impecuniosity no later than 4pm on")
            .date1(LocalDate.now().plusWeeks(8))
            .input3("Failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4("4. The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                        + "later than 4pm on.")
            .date2(LocalDate.now().plusWeeks(10))
            .input5("5. If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream (or, if none available, a local reputable) "
                        + "supplier. The defendant's evidence to be served by 4pm on")
            .date3(LocalDate.now().plusWeeks(12))
            .input6("and the claimant's evidence in reply if so advised to be served by 4pm on")
            .date4(LocalDate.now().plusWeeks(14))
            .input7("This witness statement is limited to 10 pages per party (to include any appendices).")
            .build();

        updatedData.fastTrackCreditHire(tempFastTrackCreditHire).build();

        FastTrackHousingDisrepair tempFastTrackHousingDisrepair = FastTrackHousingDisrepair.builder()
            .input1("The claimant must prepare a Scott Schedule of the items of disrepair")
            .input2("The column headings will be as follows: Item; Alleged disrepair; "
                        + "Defendant's Response; Reserved for Judge's Use")
            .input3("The claimant must serve the Scott Schedule with the relevant columns completed by 4pm on")
            .date1(LocalDate.now().plusWeeks(10)) // date to be confirmed, placeholder for now
            .input4("The Defendant must file and serve the Scott Schedule with the relevant column "
                        + "in response completed by 4pm on")
            .date2(LocalDate.now().plusWeeks(12)) // date to be confirmed, placeholder for now
            .build();

        updatedData.fastTrackHousingDisrepair(tempFastTrackHousingDisrepair).build();

        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
            .input1("1. The claimant has permission to rely on the written expert evidence annexed to the "
                        + "Particulars of Claim. Defendant may raise written questions of the expert by 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("which must be answered by 4pm on")
            .date2(LocalDate.now().plusWeeks(8))
            .input3("No other permission is given for expert evidence.")
            .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();

        FastTrackRoadTrafficAccident tempFastTrackRoadTrafficAccident = FastTrackRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the location of the accident shall be prepared and "
                        + "agreed by the parties.")
            .build();

        updatedData.fastTrackRoadTrafficAccident(tempFastTrackRoadTrafficAccident).build();

        FastTrackPreferredTelephone tempFastTrackPreferredTelephone = FastTrackPreferredTelephone
            .builder()
            .telephone(preferredTelephone)
            .build();

        updatedData.fastTrackPreferredTelephone(tempFastTrackPreferredTelephone).build();

        FastTrackPreferredEmail tempFastTrackPreferredEmail = FastTrackPreferredEmail
            .builder()
            .email(preferredEmail)
            .build();

        updatedData.fastTrackPreferredEmail(tempFastTrackPreferredEmail).build();

        SmallClaimsJudgesRecital tempSmallClaimsJudgesRecital = SmallClaimsJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.smallClaimsJudgesRecital(tempSmallClaimsJudgesRecital).build();

        if (judgementSum != null) {
            SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                SmallClaimsJudgementDeductionValue.builder()
                .value(judgementSum.getJudgementSum().toString() + "%")
                .build();

            updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
        }

        SmallClaimsDocuments tempSmallClaimsDocuments = SmallClaimsDocuments.builder()
            .input1("Each party must upload to the Digital Portal copies of all documents which they wish the court to"
                        + " consider when reaching its decision not less than 14 days before the hearing.")
            .input2("The court may refuse to consider any document which has not been uploaded to the "
                        + "Digital Portal by the above date.")
            .build();

        updatedData.smallClaimsDocuments(tempSmallClaimsDocuments).build();

        SmallClaimsWitnessStatement tempSmallClaimsWitnessStatement = SmallClaimsWitnessStatement.builder()
            .input1("Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                        + " upon whose evidence they intend to rely at the hearing not less than 14 days before"
                        + " the hearing.")
            .input2("")
            .input3("")
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
            .input2("The claimant must by no later than 14 days before the hearing date, pay the court the "
                        + "required hearing fee or submit a fully completed application for Help with Fees. If the "
                        + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
                        + "struck without further order.")
            .build();

        updatedData.smallClaimsHearing(tempSmallClaimsHearing).build();

        SmallClaimsNotes tempSmallClaimsNotes = SmallClaimsNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                       + "set aside or varied. Any such application must be received by the Court, "
                       + "together with the appropriate fee by 4pm on")
            .date(LocalDate.now().plusWeeks(1))
            .build();

        updatedData.smallClaimsNotes(tempSmallClaimsNotes).build();

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
                        + "later than 4pm on.")
            .date2(LocalDate.now().plusWeeks(6))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(LocalDate.now().plusWeeks(8))
            .input7("and the claimant's evidence is reply if so advised to be uploaded by 4pm on")
            .date4(LocalDate.now().plusWeeks(10))
            .input8("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                + "paragraph above, each party may rely upon the written evidence by way of witness statement "
                + "of one witness to provide evidence of basic hire rates available within the claimant's "
                + "geographical location from a mainstream supplier, or a local reputable supplier if none is "
                + "available.")
            .input9("The defendant’s evidence is to be uploaded to the Digital Portal by 4pm on")
            .date5(LocalDate.now().plusWeeks(8))
            .input10(", and the claimant’s evidence in reply if so advised is to be uploaded by 4pm on")
            .date6(LocalDate.now().plusWeeks(10))
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

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        updatedData.setSmallClaimsFlag(YesOrNo.NO).build();
        updatedData.setFastTrackFlag(YesOrNo.NO).build();

        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            updatedData.setSmallClaimsFlag(YesOrNo.YES)
                .build();
        } else if (SdoHelper.isFastTrack(caseData)) {
            updatedData.setFastTrackFlag(YesOrNo.YES)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        CaseDocument document = sdoGeneratorService.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        // TODO: once fast track and disposal templates are done the if conditional is not needed
        if (document != null) {
             updatedData.sdoOrderDocument(document.getDocumentLink()); // need to add this as a ccd field
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

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

        if (applicant2 != null) {
            return format(
                CONFIRMATION_SUMMARY_2v1,
                applicant1Name,
                applicant2.getPartyName(),
                respondent1Name
            );
        } else if (respondent2 != null) {
            return format(
                CONFIRMATION_SUMMARY_1v2,
                applicant1Name,
                respondent1Name,
                respondent2.getPartyName()
            );
        } else {
            return format(
                CONFIRMATION_SUMMARY_1v1,
                applicant1Name,
                respondent1Name
            );
        }
    }

    private List<String> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        return locationRefDataService.getCourtLocations(authToken);
    }
}
