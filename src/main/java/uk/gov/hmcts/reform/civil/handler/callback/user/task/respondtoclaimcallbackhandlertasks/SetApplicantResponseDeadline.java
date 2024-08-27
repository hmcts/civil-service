package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
// import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Component
@Slf4j
public class SetApplicantResponseDeadline implements CaseTask {

    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FrcDocumentsUtils frcDocumentsUtils;
    private final FeatureToggleService toggleService;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final IStateFlowEngine stateFlowEngine;
    private final AssignCategoryId assignCategoryId;
    private final ObjectMapper objectMapper;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    public SetApplicantResponseDeadline(Time time,
                                        DeadlinesCalculator deadlinesCalculator,
                                        FrcDocumentsUtils frcDocumentsUtils,
                                        FeatureToggleService toggleService,
                                        CaseFlagsInitialiser caseFlagsInitialiser,
                                        IStateFlowEngine stateFlowEngine,
                                        AssignCategoryId assignCategoryId,
                                        ObjectMapper objectMapper,
                                        CoreCaseUserService coreCaseUserService,
                                        UserService userService,
                                        LocationReferenceDataService locationRefDataService,
                                        CourtLocationUtils courtLocationUtils) {
        this.time = time;
        this.deadlinesCalculator = deadlinesCalculator;
        this.frcDocumentsUtils = frcDocumentsUtils;
        this.toggleService = toggleService;
        this.caseFlagsInitialiser = caseFlagsInitialiser;
        this.stateFlowEngine = stateFlowEngine;
        this.assignCategoryId = assignCategoryId;
        this.objectMapper = objectMapper;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
        this.locationRefDataService = locationRefDataService;
        this.courtLocationUtils = courtLocationUtils;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedData = updateRespondentAddresses(caseData);

        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        LocalDateTime applicant1Deadline = getApplicant1ResponseDeadline(responseDate, allocatedTrack);

        // 1v2 same legal rep - will respond for both and set applicant 1 response deadline
        if (respondent2HasSameLegalRep(caseData)) {
            // if responses are marked as same, copy respondent 1 values into respondent 2
            handleBothRespondentsSameLegalRepResponse(
                callbackParams,
                caseData,
                updatedData,
                responseDate,
                applicant1Deadline
            );
            // only represents 2nd respondent - need to wait for respondent 1 before setting applicant response deadline
        } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            handleSingleRespondentResponse(callbackParams, updatedData, responseDate, caseData, applicant1Deadline);
        } else {
            handleDefaultResponse(callbackParams, updatedData, responseDate, caseData, applicant1Deadline);
        }

        updatedData.isRespondent1(null);
        assembleResponseDocuments(caseData, updatedData);
        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        if (toggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToRespondentExperts(updatedData);
            addEventAndDateAddedToRespondentWitnesses(updatedData);
        }

        retainSolicitorReferences(callbackParams.getRequest().getCaseDetailsBefore().getData(), updatedData, caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(
            updatedData,
            toggleService.isUpdateContactDetailsEnabled()
        );

        updateClaimsDetailsForClaimDetailsTab(updatedData, caseData);

        updateDQPartyIdsIfHmcEnabled(updatedData);

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE, updatedData);

        // casefileview changes need to assign documents into specific folders, this is help determine
        // which user is "creating" the document and therefore which folder to move the documents
        // into, when directions order is generated in GenerateDirectionsQuestionnaireCallbackHandler
        updateDocumentGenerationRespondent2(callbackParams, updatedData, caseData);

        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        }

        // these documents are added to defendantUploads, if we do not remove/null the original,
        // case file view will show duplicate documents
        nullPlaceHolderDocuments(updatedData, caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state("AWAITING_APPLICANT_INTENTION")
            .build();
    }

    private void handleDefaultResponse(CallbackParams callbackParams,
                                       CaseData.CaseDataBuilder<?, ?> updatedData,
                                       LocalDateTime responseDate,
                                       CaseData caseData,
                                       LocalDateTime applicant1Deadline) {
        updatedData.respondent1ResponseDate(responseDate)
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

        setApplicantDeadlineIfRequired(caseData, updatedData, applicant1Deadline);
        updateRespondent2AdressesAndSetDeadline(caseData, updatedData);
        updateRespondent2Date(caseData, updatedData, responseDate);
        updateRespondent1StatementOfTruth(callbackParams, caseData, updatedData);
    }

    private void handleSingleRespondentResponse(CallbackParams callbackParams,
                                                CaseData.CaseDataBuilder<?, ?> updatedData,
                                                LocalDateTime responseDate,
                                                CaseData caseData,
                                                LocalDateTime applicant1Deadline) {
        updatedData.respondent2ResponseDate(responseDate)
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));
        // 1v1, 2v1
        // represents 1st respondent - need to set deadline if only 1 respondent,
        // or wait for 2nd respondent response before setting deadline
        updateRespondent2StatementOfTruth(callbackParams, updatedData, responseDate, caseData);
        setApplicantDeadLineIfRespondent1DateExist(caseData, updatedData, applicant1Deadline);
    }

    private void handleBothRespondentsSameLegalRepResponse(CallbackParams callbackParams,
                                                          CaseData caseData,
                                                          CaseData.CaseDataBuilder<?, ?> updatedData,
                                                          LocalDateTime responseDate,
                                                          LocalDateTime applicant1Deadline) {
        if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            responseHasSameUpdateValues(callbackParams, updatedData, caseData, responseDate, applicant1Deadline);
        } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
            responseDoesNotHaveSameUpdateValues(callbackParams, updatedData, responseDate, applicant1Deadline, caseData);
        }
    }

    private static void updateClaimsDetailsForClaimDetailsTab(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }
    }

    private void updateDQPartyIdsIfHmcEnabled(CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (toggleService.isHmcEnabled()) {
            populateDQPartyIds(updatedData);
        }
    }

    private void updateDocumentGenerationRespondent2(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        updatedData.respondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                     .toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                       .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            updatedData.respondent2DocumentGeneration("userRespondent2");
        }
    }

    private static void nullPlaceHolderDocuments(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        log.info("Null placeholder documents");
        updatedData.respondent1ClaimResponseDocument(null);
        updatedData.respondent2ClaimResponseDocument(null);
        if (caseData.getRespondent1() != null
            && updatedData.build().getRespondent1DQ() != null) {
            updatedData.respondent1DQ(updatedData.build().getRespondent1DQ().toBuilder().respondent1DQDraftDirections(
                null).build());
        }
        if (caseData.getRespondent2() != null
            && updatedData.build().getRespondent2DQ() != null) {
            updatedData.respondent2DQ(updatedData.build().getRespondent2DQ().toBuilder().respondent2DQDraftDirections(
                null).build());
        }
    }

    private void updateRespondent1StatementOfTruth(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        // moving statement of truth value to correct field, this was not possible in mid event.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
        updatedData.respondent1DQ(dq.build());
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void updateRespondent2Date(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime responseDate) {
        // same legal rep - will respond for both and set applicant 1 response deadline
        if (respondent2HasSameLegalRep(caseData)) {
            // if responses are marked as same, copy respondent 1 values into respondent 2
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
            }

            updatedData.respondent2ResponseDate(responseDate);
        }
    }

    private void updateRespondent2AdressesAndSetDeadline(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            updatedData
                .respondent2(updatedRespondent2)
                .respondent2Copy(null)
                .respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());

            if (caseData.getRespondent2ResponseDate() == null) {
                updatedData.nextDeadline(caseData.getRespondent2ResponseDeadline().toLocalDate());
            }
        }
    }

    private boolean applicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private void setApplicantDeadlineIfRequired(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime applicant1Deadline) {
        if (respondent2NotPresent(caseData)
            || applicant2Present(caseData)
            || caseData.getRespondent2ResponseDate() != null) {
            updatedData
                .applicant1ResponseDeadline(applicant1Deadline)
                .nextDeadline(applicant1Deadline.toLocalDate());
        }
    }

    private void setApplicantDeadLineIfRespondent1DateExist(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime applicant1Deadline) {
        if (caseData.getRespondent1ResponseDate() != null) {
            updatedData
                .nextDeadline(applicant1Deadline.toLocalDate())
                .applicant1ResponseDeadline(applicant1Deadline);
        } else {
            updatedData.nextDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate());
        }
    }

    private void updateRespondent2StatementOfTruth(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime responseDate, CaseData caseData) {

        // moving statement of truth value to correct field, this was not possible in mid event.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
            .respondent2DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent2DQ(caseData, dq, callbackParams);
        updatedData.respondent2DQ(dq.build());

        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void responseDoesNotHaveSameUpdateValues(CallbackParams callbackParams,
                                               CaseData.CaseDataBuilder<?, ?> updatedData,
                                               LocalDateTime responseDate,
                                               LocalDateTime applicant1Deadline,
                                               CaseData caseData) {
        updatedData
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
            .respondent1ResponseDate(responseDate)
            .respondent2ResponseDate(responseDate)
            .nextDeadline(applicant1Deadline.toLocalDate())
            .applicant1ResponseDeadline(applicant1Deadline);

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        if (caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
            updatedData.respondent1DQ(dq.build());

        } else {
            // required as ccd populated the respondent DQ with null objects.
            updatedData.respondent1DQ(null);
        }

        if (caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {

            Respondent2DQ.Respondent2DQBuilder dq2 = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent2DQ(caseData, dq2, callbackParams);
            updatedData.respondent2DQ(dq2.build());

        } else {
            updatedData.respondent2DQ(null);
        }

        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void responseHasSameUpdateValues(CallbackParams callbackParams,
                                            CaseData.CaseDataBuilder<?, ?> updatedData,
                                            CaseData caseData,
                                            LocalDateTime responseDate,
                                            LocalDateTime applicant1Deadline) {
        updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
        updatedData
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
            .respondent1ResponseDate(responseDate)
            .respondent2ResponseDate(responseDate)
            .nextDeadline(applicant1Deadline.toLocalDate())
            .applicant1ResponseDeadline(applicant1Deadline);

        // moving statement of truth value to correct field, this was not possible in mid event.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth);

        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);

        updatedData.respondent1DQ(dq.build());

        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        // 1v2 same Solicitor responding to respondents individually
    }

    private  CaseData.CaseDataBuilder<?, ?> updateRespondentAddresses(CaseData caseData) {
        if (ofNullable(caseData.getRespondent1Copy()).isPresent()
            && (caseData.getRespondent1Copy().getPrimaryAddress() == null)) {
            throw new IllegalArgumentException("Primary Address cannot be empty");
        }

        // persist respondent address (ccd issue)
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .flags(caseData.getRespondent1Copy().getFlags())
            .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .flags(caseData.getRespondent2Copy().getFlags())
                .build();

            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
        }
        return updatedData;
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate, AllocatedTrack allocatedTrack) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate, allocatedTrack);
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData, Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        // data for court location
        if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations)
            .map(DynamicList::getValue).isPresent()) {
            DynamicList courtLocations = caseData
                .getRespondent1DQ().getRespondent1DQRequestedCourt().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), courtLocations);
            RequestedCourt.RequestedCourtBuilder dqBuilder = caseData.getRespondent1DQ()
                .getRespondent1DQRequestedCourt().toBuilder()
                .responseCourtLocations(null)
                .responseCourtCode(Optional.ofNullable(courtLocation)
                                       .map(LocationRefData::getCourtLocationCode)
                                       .orElse(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                                                   .getResponseCourtCode()));
            buildWithMatching(courtLocation).ifPresent(dqBuilder::caseLocation);
            dq.respondent1DQRequestedCourt(dqBuilder.build());
        } else if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations).isPresent()) {
            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ()
                                               .getRespondent1DQRequestedCourt()
                                               .toBuilder().responseCourtLocations(null).build());
        }
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData, Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {
        // data for court location
        if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations)
            .map(DynamicList::getValue).isPresent()) {
            DynamicList courtLocations = caseData
                .getRespondent2DQ().getRespondent2DQRequestedCourt().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), courtLocations);
            RequestedCourt.RequestedCourtBuilder dqBuilder = caseData.getRespondent2DQ().getRequestedCourt().toBuilder()
                .responseCourtLocations(null)
                .responseCourtCode(Optional.ofNullable(courtLocation)
                                       .map(LocationRefData::getCourtLocationCode)
                                       .orElse(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                                                   .getResponseCourtCode()));
            buildWithMatching(courtLocation).ifPresent(dqBuilder::caseLocation);
            dq.respondent2DQRequestedCourt(dqBuilder.build());
        } else if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations).isPresent()) {
            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ()
                                               .getRespondent2DQRequestedCourt()
                                               .toBuilder().responseCourtLocations(null).build());
        }

    }

    private boolean respondent2NotPresent(CaseData caseData) {
        return caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2() == NO;
    }

    private void assembleResponseDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        assembleRespondent1ResponseDocuments(caseData, updatedCaseData, defendantUploads);
        assembleRespondent2ResponseDocuments(caseData, updatedCaseData, defendantUploads);
    }

    private void assembleRespondent2ResponseDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent2ClaimResponseDocument = caseData.getRespondent2ClaimResponseDocument();
        if (respondent2ClaimResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2ClaimResponseDocument.getFile();
            if (respondent2ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent2ClaimDocument, "Defendant 2",
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
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        if (respondent2DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2DQDraftDirections = respondent2DQ.getRespondent2DQDraftDirections();
            if (respondent2DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent2DQDraftDirections,
                    "Defendant 2",
                    updatedCaseData.build().getRespondent2ResponseDate(),
                    DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent2DQDraftDirections,
                    DocCategory.DQ_DEF2.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
    }

    private void assembleRespondent1ResponseDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent1ClaimResponseDocument = caseData.getRespondent1ClaimResponseDocument();
        if (respondent1ClaimResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1ClaimResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                Element<CaseDocument> documentElement =
                    buildElemCaseDocument(respondent1ClaimDocument, "Defendant",
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
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1DQDraftDirections = respondent1DQ.getRespondent1DQDraftDirections();
            if (respondent1DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent1DQDraftDirections,
                    "Defendant",
                    updatedCaseData.build().getRespondent1ResponseDate(),
                    DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1DQDraftDirections,
                    DocCategory.DQ_DEF1.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
    }

    private void retainSolicitorReferences(Map<String, Object> beforeCaseData,
                                           CaseData.CaseDataBuilder<?, ?> updatedData,
                                           CaseData caseData) {

        @SuppressWarnings("unchecked")
        Map<String, String> solicitorRefs = ofNullable(beforeCaseData.get("solicitorReferences"))
            .map(refs -> objectMapper.convertValue(refs, HashMap.class))
            .orElse(null);
        SolicitorReferences solicitorReferences = ofNullable(solicitorRefs)
            .map(refMap -> {

                // collect data from recent form - defendantSolicitorRef1
                String defendantSolicitorRef1 = null;
                if (caseData.getSolicitorReferences() != null
                    && caseData.getSolicitorReferences().getRespondentSolicitor1Reference() != null) {
                    defendantSolicitorRef1 = caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
                }

                return SolicitorReferences.builder()
                    .applicantSolicitor1Reference(
                        refMap.getOrDefault("applicantSolicitor1Reference", null))
                    // if solicitor reference recently changed in defendant response then use defendantSolicitorRef1
                    // else use data before it's updated
                    .respondentSolicitor1Reference(
                        ofNullable(defendantSolicitorRef1)
                            .orElse(refMap.getOrDefault("respondentSolicitor1Reference", null)))
                    .respondentSolicitor2Reference(
                        refMap.getOrDefault("respondentSolicitor2Reference", null))
                    .build();
            })
            .orElse(null);

        updatedData.solicitorReferences(solicitorReferences);

        String respondentSolicitor2Reference = ofNullable(caseData.getRespondentSolicitor2Reference())
            .orElse(ofNullable(beforeCaseData.get("respondentSolicitor2Reference"))
                        .map(Object::toString).orElse(null));

        updatedData
            .solicitorReferences(solicitorReferences)
            .respondentSolicitor2Reference(respondentSolicitor2Reference)
            .caseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(
                solicitorReferences != null ? ofNullable(solicitorReferences.getRespondentSolicitor1Reference())
                    .map(Object::toString).orElse(null) : null,
                respondentSolicitor2Reference
            ));
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseType() == null
            || caseData.getRespondent2ClaimResponseType() == null;
    }

    private Optional<CaseLocationCivil> buildWithMatching(LocationRefData courtLocation) {
        return Optional.ofNullable(courtLocation).map(LocationHelper::buildCaseLocation);
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        return solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, caseRole);
    }

    private boolean solicitorRepresentsOnlyOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
