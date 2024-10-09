package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetApplicantResponseDeadline implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService toggleService;
    private final ObjectMapper objectMapper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final Time time;
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;
    private final IStateFlowEngine stateFlowEngine;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FrcDocumentsUtils frcDocumentsUtils;
    private final CourtLocationUtils courtLocationUtils;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;
    private final AssignCategoryId assignCategoryId;
    private final DQResponseDocumentUtils dqResponseDocumentUtils;

    private static final String DEF2 = "Defendant 2";

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();
        Party updatedRespondent1;

        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails()).build();
        } else {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
                    .build();
        }

        if (caseData.getRespondent1Copy() != null) {
            updatedRespondent1 =
                    updatedRespondent1.toBuilder().flags(caseData.getRespondent1Copy().getFlags()).build();
        }

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
                .respondent1(updatedRespondent1)
                .respondent1Copy(null);

        if (respondent2HasSameLegalRep(caseData)
                && caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            updatedData.respondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec());
            updatedData
                    .respondent2ResponseDate(responseDate);
        }

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
                && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                    .flags(caseData.getRespondent2Copy().getFlags())
                    .build();
            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
        }

        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
                && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
                && ifResponseTypeIsPartOrFullAdmission(caseData)) {
            LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
                    LocalDate.now(),
                    RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
            updatedData.respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenBePaid).build());
        }

        CaseRole respondentTwoCaseRoleToCheck;

        respondentTwoCaseRoleToCheck = RESPONDENTSOLICITORTWO;

        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, respondentTwoCaseRoleToCheck)) {
            updatedData.respondent2ResponseDate(responseDate)
                    .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

            if (caseData.getRespondent1ResponseDate() != null) {
                updatedData
                        .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate));
            }

            // 1v1, 2v1
            // represents 1st respondent - need to set deadline if only 1 respondent,
            // or wait for 2nd respondent response before setting deadline
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
                    .respondent2DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent2DQ(caseData, updatedData, dq, callbackParams);
            updatedData.respondent2DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        } else {
            updatedData
                    .respondent1ResponseDate(responseDate)
                    .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate))
                    .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

            if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
                Party updatedRespondent2;

                if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
                    updatedRespondent2 = caseData.getRespondent2().toBuilder()
                            .primaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails()).build();
                } else {
                    updatedRespondent2 = caseData.getRespondent2().toBuilder()
                            .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress()).build();
                }

                updatedData
                        .respondent2(updatedRespondent2.toBuilder()
                                .flags(caseData.getRespondent2Copy().getFlags()).build())
                        .respondent2Copy(null);
                updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
            }

            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                    .respondent1DQStatementOfTruth(statementOfTruth)
                    .respondent1DQWitnesses(Witnesses.builder()
                            .witnessesToAppear(caseData.getRespondent1DQWitnessesRequiredSpec())
                            .details(caseData.getRespondent1DQWitnessesDetailsSpec())
                            .build());
            handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
            updatedData.respondent1DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        }
        if (solicitorHasCaseRole(callbackParams, respondentTwoCaseRoleToCheck)
                && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.defenceAdmitPartPaymentTimeRouteRequired(null);
        }

        if (caseData.getRespondent1DQWitnessesSmallClaim() != null) {
            updatedData.respondent1DQ(
                    updatedData.build().getRespondent1DQ().toBuilder().respondent1DQWitnesses(
                            caseData.getRespondent1DQWitnessesSmallClaim()).build());
        }

        if (caseData.getRespondent2DQWitnessesSmallClaim() != null) {
            updatedData.respondent2DQ(
                    updatedData.build().getRespondent2DQ().toBuilder().respondent2DQWitnesses(
                            caseData.getRespondent2DQWitnessesSmallClaim()).build());
        }

        if (caseData.getRespondent1DQ() != null
                && YES.equals(caseData.getResponseClaimExpertSpecRequired())
                && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
            updatedData.respondent1DQ(
                    updatedData.build().getRespondent1DQ().toBuilder()
                            .respondent1DQExperts(Experts.builder()
                                    .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                    .details(wrapElements(expert))
                                    .build())
                            .build());
        } else if (caseData.getRespondent1DQ() != null
                && NO.equals(caseData.getResponseClaimExpertSpecRequired())) {
            updatedData.respondent1DQ(
                    updatedData.build().getRespondent1DQ().toBuilder()
                            .respondent1DQExperts(Experts.builder()
                                    .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                    .build())
                            .build());
        }

        if (caseData.getRespondent2DQ() != null
                && YES.equals(caseData.getResponseClaimExpertSpecRequired2())
                && caseData.getRespondent2DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent2DQ().getSmallClaimExperts());
            updatedData.respondent2DQ(
                    updatedData.build().getRespondent2DQ().toBuilder()
                            .respondent2DQExperts(Experts.builder()
                                    .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                    .details(wrapElements(expert))
                                    .build())
                            .build());
        } else if (caseData.getRespondent2DQ() != null
                && NO.equals(caseData.getResponseClaimExpertSpecRequired2())) {
            updatedData.respondent2DQ(
                    updatedData.build().getRespondent2DQ().toBuilder()
                            .respondent2DQExperts(Experts.builder()
                                    .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                    .build())
                            .build());
        }

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData,
                toggleService.isUpdateContactDetailsEnabled());

        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }

        if (toggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToRespondentExperts(updatedData);
            addEventAndDateAddedToRespondentWitnesses(updatedData);
        }

        if (toggleService.isHmcEnabled()) {
            populateDQPartyIds(updatedData);
        }

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_SPEC, updatedData);

        // casefileview changes need to assign documents into specific folders, this is help determine
        // which user is "creating" the document and therefore which folder to move the documents
        // into, when directions order is generated in GenerateDirectionsQuestionnaireCallbackHandler
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        updatedData.respondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                .toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
                && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            updatedData.respondent2DocumentGeneration("userRespondent2");
        }

        updateCorrespondenceAddress(callbackParams, updatedData, caseData);

        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
                && isAwaitingAnotherDefendantResponse(caseData)) {

            if (isDefending(caseData)) {
                assembleResponseDocumentsSpec(caseData, updatedData);
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .build();
        } else if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
                && !isAwaitingAnotherDefendantResponse(caseData)) {
            if (!FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                        .data(updatedData.build().toMap(objectMapper))
                        .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                        .build();
            }
        } else if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                    .build();
        } else if (getMultiPartyScenario(caseData) == TWO_V_ONE && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                    .build();
        }
        assembleResponseDocumentsSpec(caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .state(CaseState.AWAITING_APPLICANT_INTENTION.name())
                .build();
    }

    private boolean isDefending(CaseData caseData) {
        log.debug("Checking if the case is defending");
        return FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private boolean ifResponseTypeIsPartOrFullAdmission(CaseData caseData) {
        log.debug("Checking if the response type is part or full admission");
        return (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
            || (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private void updateCorrespondenceAddress(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData, CaseData caseData) {
        log.debug("Updating correspondence address");
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            handleRespondent1CorrespondenceAddress(caseData, updatedCaseData);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            handleRespondent2CorrespondenceAddress(caseData, updatedCaseData);
        }
    }

    private void handleRespondent1CorrespondenceAddress(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Handling respondent 1 correspondence address");
        if (caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            Address newAddress = caseData.getSpecAoSRespondentCorrespondenceAddressdetails();
            updatedCaseData.specRespondentCorrespondenceAddressdetails(newAddress)
                .specAoSRespondentCorrespondenceAddressdetails(Address.builder().build());
            if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP) {
                updatedCaseData.specRespondent2CorrespondenceAddressdetails(newAddress);
            }
        }
    }

    private void handleRespondent2CorrespondenceAddress(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Handling respondent 2 correspondence address");
        if (caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            updatedCaseData.specRespondent2CorrespondenceAddressdetails(
                    caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                .specAoSRespondent2CorrespondenceAddressdetails(Address.builder().build());
        }
    }

    private void assembleResponseDocumentsSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = nonNull(caseData.getDefendantResponseDocuments())
            ? caseData.getDefendantResponseDocuments() : new ArrayList<>();

        ResponseDocument respondent1SpecDefenceResponseDocument = caseData.getRespondent1SpecDefenceResponseDocument();
        if (respondent1SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1SpecDefenceResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent1ClaimDocument, "Defendant",
                    updatedCaseData.build().getRespondent1ResponseDate(),
                    DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1ClaimDocument,
                    DocCategory.DEF1_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        if (respondent1DQ != null) {
            ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
            if (respondent2SpecDefenceResponseDocument != null) {
                uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
                if (respondent2ClaimDocument != null) {
                    Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2ClaimDocument, DEF2,
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                    );
                    assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        DocCategory.DEF2_DEFENSE_DQ.getValue()
                    );
                    defendantUploads.add(documentElement);
                }
            }
        } else {
            ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
            if (respondent2SpecDefenceResponseDocument != null) {
                uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
                if (respondent2ClaimDocument != null) {
                    Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2ClaimDocument, DEF2,
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                    );
                    assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        DocCategory.DEF2_DEFENSE_DQ.getValue()
                    );
                    CaseDocument copy = assignCategoryId
                        .copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF2.getValue());
                    defendantUploads.add(documentElement);
                    if (Objects.nonNull(copy)) {
                        defendantUploads.add(ElementUtils.element(copy));
                    }
                }
            }
        }

        List<Element<CaseDocument>> additionalDocuments = dqResponseDocumentUtils.buildDefendantResponseDocuments(updatedCaseData.build());
        defendantUploads.addAll(additionalDocuments);

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);
        clearTempDocuments(updatedCaseData);
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData,
                                                     Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        log.debug("Handling court location for Respondent 1 DQ");
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant1(caseData, callbackParams);

        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            log.info("Found preferred court location for Respondent 1: {}", courtLocation.getCourtLocationCode());

            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ()
                                               .getRespondToCourtLocation().toBuilder()
                                               .reasonForHearingAtSpecificCourt(
                                                   caseData.getRespondent1DQ()
                                                       .getRespondToCourtLocation()
                                                       .getReasonForHearingAtSpecificCourt())
                                               .responseCourtLocations(null)
                                               .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                               .responseCourtCode(courtLocation.getCourtLocationCode()).build());
            dq.respondToCourtLocation(RequestedCourt.builder()
                                          .responseCourtLocations(null)
                                          .responseCourtCode(courtLocation.getCourtLocationCode())
                                          .build())
                .responseClaimCourtLocationRequired(YES);
        } else {
            log.info("No preferred court location found for Respondent 1");
            dq.responseClaimCourtLocationRequired(NO);
        }
    }

    private Optional<LocationRefData> getCourtLocationDefendant1(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondToCourtLocation() != null) {
            DynamicList courtLocations = caseData
                .getRespondent1DQ().getRespondToCourtLocation().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            log.debug("Retrieved court location data for Respondent 1: {}", courtLocation);
            return Optional.ofNullable(courtLocation);
        } else {
            log.debug("Respondent 1 DQ or RespondToCourtLocation is null");
            return Optional.empty();
        }
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData,  CaseData.CaseDataBuilder<?, ?> updatedCase,
                                                     Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {
        log.debug("Handling court location for Respondent 2 DQ");
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant2(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            log.info("Found preferred court location for Respondent 2: {}", courtLocation.getCourtLocationCode());

            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ().getRespondToCourtLocation2().toBuilder()
                                               .responseCourtLocations(null)
                                               .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                               .responseCourtCode(courtLocation.getCourtLocationCode()).build())
                .respondToCourtLocation2(RequestedCourt.builder()
                                             .responseCourtLocations(null)
                                             .responseCourtCode(courtLocation.getCourtLocationCode())
                                             .reasonForHearingAtSpecificCourt(
                                                 caseData.getRespondent2DQ().getRespondToCourtLocation2()
                                                     .getReasonForHearingAtSpecificCourt()
                                             )
                                             .build());
            updatedCase.responseClaimCourtLocation2Required(YES);
        } else {
            log.info("No preferred court location found for Respondent 2");
            updatedCase.responseClaimCourtLocation2Required(NO);
        }
    }

    public Optional<LocationRefData> getCourtLocationDefendant2(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondToCourtLocation2() != null) {
            DynamicList courtLocations = caseData
                .getRespondent2DQ().getRespondToCourtLocation2().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            log.debug("Retrieved court location data for Respondent 2: {}", courtLocation);
            return Optional.ofNullable(courtLocation);
        } else {
            log.debug("Respondent 2 DQ or RespondToCourtLocation2 is null");
            return Optional.empty();
        }
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        boolean condition = caseData.getRespondent1ClaimResponseTypeForSpec() == null
            || caseData.getRespondent2ClaimResponseTypeForSpec() == null;
        log.debug("Checking if awaiting another defendant response: {}", condition);
        return condition;
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        LocalDateTime deadline = deadlinesCalculator.calculateApplicantResponseDeadlineSpec(responseDate);
        log.debug("Calculated applicant 1 response deadline: {}", deadline);
        return deadline;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        log.debug("Checking if respondent 2 has the same legal representative");
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean twoVsOneDivergent(CaseData caseData) {
        log.debug("Checking if the scenario is two vs one divergent");
        return (!FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
            || (!FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        log.debug("Checking if the solicitor represents only one of the respondents");
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
                && coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                caseRole
        );
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        log.debug("Checking if the solicitor has the case role: {}", caseRole);
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private void clearTempDocuments(CaseData.CaseDataBuilder<?, ?> builder) {
        log.debug("Clearing temporary documents");
        CaseData caseData = builder.build();

        builder.respondent1SpecDefenceResponseDocument(null);
        builder.respondent2SpecDefenceResponseDocument(null);

        if (nonNull(caseData.getRespondent1DQ())) {
            builder.respondent1DQ(builder.build().getRespondent1DQ().toBuilder().respondent1DQDraftDirections(null).build());
        }
        if (nonNull(caseData.getRespondent2DQ())) {
            builder.respondent2DQ(builder.build().getRespondent2DQ().toBuilder().respondent2DQDraftDirections(null).build());
        }
    }
}
