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
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
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
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;

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

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "order-details"), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
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
    // FlowStateAllowedEventService.java.
    // This way pressing previous on the ccd page won't end up calling this method again and thus
    // repopulating the fields if they have been changed.
    // There is no reason to add conditionals to avoid this here since having it as an about to start event will mean
    // it is only ever called once.
    // Then any changes to fields in ccd will persist in ccd regardless of backwards or forwards page navigation.
    private CallbackResponse prePopulateOrderDetailsPages(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        JudgementSum judgementSum = caseData.getDrawDirectionsOrder();

        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = DisposalHearingJudgesRecital.builder()
            .input("Upon considering the claim Form and Particulars of Claim/statements of case"
                       + " [and the directions questionnaires] \n\nIT IS ORDERED that:-")
            .build();

        updatedData.disposalHearingJudgesRecital(tempDisposalHearingJudgesRecital).build();

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
            .input2("The claimant must file and serve a witness statement addressing, (a) need to hire a replacement "
                        + "vehicle and (b) impecuniosity no later than 4pm on")
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

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();
        OrderType orderType = caseData.getOrderType();

        Boolean smallClaimsPath1 = (drawDirectionsOrderRequired == YesOrNo.NO)
            && (claimsTrack == ClaimsTrack.smallClaimsTrack);
        Boolean smallClaimsPath2 = (drawDirectionsOrderRequired == YesOrNo.YES)
            && (drawDirectionsOrderSmallClaims == YesOrNo.YES);
        Boolean fastTrackPath1 = (drawDirectionsOrderRequired == YesOrNo.NO)
            && (claimsTrack == ClaimsTrack.fastTrack);
        Boolean fastTrackPath2 = (drawDirectionsOrderRequired == YesOrNo.YES)
            && (drawDirectionsOrderSmallClaims == YesOrNo.NO) && (orderType == OrderType.DECIDE_DAMAGES);

        updatedData.setSmallClaimsFlag(YesOrNo.NO);
        updatedData.setFastTrackFlag(YesOrNo.NO);

        if (smallClaimsPath1 || smallClaimsPath2) {
            updatedData.setSmallClaimsFlag(YesOrNo.YES)
                .build();
        } else if (fastTrackPath1 || fastTrackPath2) {
            updatedData.setFastTrackFlag(YesOrNo.YES)
                .build();
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
}
