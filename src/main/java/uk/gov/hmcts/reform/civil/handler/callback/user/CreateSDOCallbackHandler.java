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
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
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
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
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
    public static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 14 days before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. If the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";
    public static final String HEARING_TIME_TEXT_AFTER_HNL =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. If the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";

    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final SdoGeneratorService sdoGeneratorService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prePopulateOrderDetailsPages)
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
        updatedData.smallClaimsHearingToggle(checkList);
        updatedData.smallClaimsMethodToggle(checkList);
        updatedData.smallClaimsDocumentsToggle(checkList);
        updatedData.smallClaimsWitnessStatementToggle(checkList);

        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = DisposalHearingJudgesRecital.builder()
            .input("Upon considering the claim form, particulars of claim, statements of case"
                       + " and Directions questionnaires")
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
            .input1("The parties shall serve on each other copies of the documents upon which reliance is to be"
                       + " placed at the disposal hearing by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input2("The parties must upload to the Digital Portal copies of those documents which they wish the"
                       + "court to consider when deciding the amount of damages, by 4pm on")
            .date2(LocalDate.now().plusWeeks(10))
            .build();

        updatedData.disposalHearingDisclosureOfDocuments(tempDisposalHearingDisclosureOfDocuments).build();

        DisposalHearingWitnessOfFact tempDisposalHearingWitnessOfFact = DisposalHearingWitnessOfFact.builder()
            .input1("The claimant shall serve on every other party the witness statements of all witnesses of fact"
                        + " on whose evidence reliance is to be placed by 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("The provisions of CPR 32.6 apply to such evidence.")
            .input3("The claimant must upload to the Digital Portal copies of the witness statements of all witnesses"
                        + " whose evidence they wish the court to consider "
                        + "when deciding the amount of damages by 4pm on")
            .date2(LocalDate.now().plusWeeks(4))
            .input4("The provisions of CPR 32.6 apply to such evidence.")
            .input5("Any application by the defendant pursuant to CPR 32.7 must be made by 4pm on")
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
            .input1("If there is a claim for ongoing/future loss in the original schedule of losses then the claimant"
                        + " must send an up to date schedule of loss to the defendant by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
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

        HearingSupportRequirementsDJ hearingSupportRequirementsDJ = caseData.getHearingSupportRequirementsDJ();

        DisposalHearingBundle tempDisposalHearingBundle = DisposalHearingBundle.builder()
            .input("At least 7 days before the disposal hearing, the claimant must upload to the Digital Portal")
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

        if (judgementSum != null) {
            FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                FastTrackJudgementDeductionValue.builder()
                    .value(judgementSum.getJudgementSum().toString() + "%")
                    .build();

            updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();
        }

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("Documents will be disclosed by uploading to the Digital Portal a list with a disclosure "
                        + "statement by 4pm on")
            .date1(LocalDate.now().plusWeeks(4))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(LocalDate.now().plusWeeks(6))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(LocalDate.now().plusWeeks(4))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();

        FastTrackWitnessOfFact tempFastTrackWitnessOfFact = FastTrackWitnessOfFact.builder()
            .input1("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                        + "fact on whom they intend to rely. This is limited to")
            .input2("")
            .input3("")
            .input4("For this limitation, a party is counted as a witness.")
            .input5("Each witness statement should be no more than")
            .input6("")
            .input7("A4 pages. Statements should be double spaced using a font size of 12.")
            .input8("Witness statements shall be uploaded to the Digital Portal by 4pm on")
            .date(LocalDate.now().plusWeeks(8))
            .input9("Oral evidence will only be permitted at trial with permission from the Court from witnesses"
                        + " whose statements have not been uploaded to the Digital Portal in accordance with this "
                        + "order, or whose statements that have been served late.")
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
            .input4("Upon it being noted that the schedule of loss contains no claim for continuing loss and is "
                        + "therefore final, no further schedule of loss shall be uploaded without permission to amend. "
                        + "The defendant shall upload to the Digital Portal an up-to-date counter schedule of loss by "
                        + "4pm on")
            .date3(LocalDate.now().plusWeeks(12))
            .build();

        updatedData.fastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss).build();

        FastTrackTrial tempFastTrackTrial = FastTrackTrial.builder()
            .input1("The time provisionally allowed for this trial is")
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .input2("If either party considers that the time estimate is insufficient, they must inform the court "
                        + "within 7 days of the date stated on this order.")
            .input3("At least 7 days before the trial, the claimant must upload to the Digital Portal")
            .build();

        updatedData.fastTrackTrial(tempFastTrackTrial).build();

        FastTrackNotes tempFastTrackNotes = FastTrackNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                       + "set aside or varied. Any application must be received by the Court, "
                       + "together with the appropriate fee by 4pm on")
            .date(LocalDate.now().plusWeeks(1))
            .build();

        updatedData.fastTrackNotes(tempFastTrackNotes).build();

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
            .input4("c) Legible copies of the medical and educational records of the claimant, the deceased, and the"
                        + " claimant's mother are to be placed in a separate paginated bundle by the claimant's "
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
            .input7("and the claimant's evidence is reply if so advised to be uploaded by 4pm on")
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
            .input3("The claimant must uploaded to the Digital Portal the Scott Schedule with the relevant "
                        + "columns completed by 4pm on")
            .date1(LocalDate.now().plusWeeks(10))
            .input4("The defendant must uploaded to the Digital Portal the amended Scott Schedule with the "
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
            .input2(featureToggleService.isHearingsAndListingsEnabled() ? HEARING_TIME_TEXT_AFTER_HNL
                        : HEARING_TIME_TEXT_AFTER)
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
            updatedData.setSmallClaimsFlag(YesOrNo.YES).build();
        } else if (SdoHelper.isFastTrack(caseData)) {
            updatedData.setFastTrackFlag(YesOrNo.YES).build();
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

        if (document != null) {
            updatedData.sdoOrderDocument(document.getDocumentLink());
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
