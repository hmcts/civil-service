package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.*;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab.*;
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
    private final ObjectMapper objectMapper;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final UpdateDataRespondentDeadlineResponse updateDataRespondentDeadlineResponse;
    private final AssembleDocumentsForDeadlineResponse assembleDocumentsForDeadlineResponse;

    public SetApplicantResponseDeadline(Time time,
                                        DeadlinesCalculator deadlinesCalculator,
                                        FrcDocumentsUtils frcDocumentsUtils,
                                        FeatureToggleService toggleService,
                                        CaseFlagsInitialiser caseFlagsInitialiser,
                                        IStateFlowEngine stateFlowEngine,
                                        ObjectMapper objectMapper,
                                        CoreCaseUserService coreCaseUserService,
                                        UserService userService,
                                        UpdateDataRespondentDeadlineResponse updateDataRespondentDeadlineResponse,
                                        AssembleDocumentsForDeadlineResponse assembleDocumentsForDeadlineResponse) {
        this.time = time;
        this.deadlinesCalculator = deadlinesCalculator;
        this.frcDocumentsUtils = frcDocumentsUtils;
        this.toggleService = toggleService;
        this.caseFlagsInitialiser = caseFlagsInitialiser;
        this.stateFlowEngine = stateFlowEngine;
        this.objectMapper = objectMapper;
        this.coreCaseUserService = coreCaseUserService;
        this.userService = userService;
        this.updateDataRespondentDeadlineResponse = updateDataRespondentDeadlineResponse;
        this.assembleDocumentsForDeadlineResponse = assembleDocumentsForDeadlineResponse;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedData = updateRespondentAddresses(caseData);

        LocalDateTime responseDate = time.now();
        LocalDateTime applicant1Deadline = getApplicant1ResponseDeadline(responseDate);

        if (isRespondent2SameLegalRep(caseData)) {
            updateDataRespondentDeadlineResponse
                .updateBothRespondentsResponseSameLegalRep(
                    callbackParams,
                    caseData,
                    updatedData,
                    responseDate,
                    applicant1Deadline);
        } else if (isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            updateDataRespondentDeadlineResponse
                .updateResponseDataForSecondRespondent(
                    callbackParams,
                    updatedData,
                    responseDate,
                    caseData,
                    applicant1Deadline);
        } else {
            updateDataRespondentDeadlineResponse
                .updateResponseDataForBothRespondent(
                    callbackParams,
                    updatedData,
                    responseDate,
                    caseData,
                    applicant1Deadline);
        }

        updatedData.isRespondent1(null);
        assembleDocumentsForDeadlineResponse
            .assembleResponseDocuments(caseData, updatedData);
        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        addEventAndDateAddedToRespondentExperts(updatedData);
        addEventAndDateAddedToRespondentWitnesses(updatedData);
        retainSolicitorReferences(callbackParams.getRequest().getCaseDetailsBefore().getData(), updatedData, caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData);
        updateClaimsDetailsForClaimDetailsTab(updatedData, caseData);
        populateDQPartyIds(updatedData);

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE, updatedData);
        updateDocumentGenerationRespondent2(callbackParams, updatedData, caseData);

        updateRequestCourtClaimTabRespondent1(updatedData);
        if (isMultiPartyScenario(caseData) && caseData.getRespondent2SameLegalRepresentative().equals(NO)) {
            updateRequestCourtClaimTabRespondent2(updatedData);
        }

        if (isMultipartyScenario1v2With2LegalRep(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        }

        nullPlaceHolderDocuments(updatedData, caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state("AWAITING_APPLICANT_INTENTION")
            .build();
    }

    private boolean isMultipartyScenario1v2With2LegalRep(CaseData caseData) {
        return getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData);
    }

    private static void updateClaimsDetailsForClaimDetailsTab(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }

        updateRequestCourtClaimTabRespondent1(updatedData);
        if (isMultiPartyScenario(caseData) && caseData.getRespondent2SameLegalRepresentative().equals(NO)) {
            updateRequestCourtClaimTabRespondent2(updatedData);
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
        log.info("Null placeholder documents for Case ID: {}", caseData.getCcdCaseReference());
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

    private CaseData.CaseDataBuilder<?, ?> updateRespondentAddresses(CaseData caseData) {

        Party updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .flags(caseData.getRespondent1Copy().getFlags())
            .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            Party updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .flags(caseData.getRespondent2Copy().getFlags())
                .build();

            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
        }
        return updatedData;
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate);
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

                String defendantSolicitorRef1 = null;
                if (caseData.getSolicitorReferences() != null
                    && caseData.getSolicitorReferences().getRespondentSolicitor1Reference() != null) {
                    defendantSolicitorRef1 = caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
                }

                return SolicitorReferences.builder()
                    .applicantSolicitor1Reference(
                        refMap.getOrDefault("applicantSolicitor1Reference", null))
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

    private boolean isRespondent2SameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean isSolicitorRepresentingOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }
}
