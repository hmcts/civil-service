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
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Component
@Slf4j
public class SetApplicantResponseDeadline implements CaseTask {

    private static final int RESPONSE_CLAIM_DEADLINE_EXTENSION_MONTHS = 36;
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
    private final RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

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
                                        AssembleDocumentsForDeadlineResponse assembleDocumentsForDeadlineResponse,
                                        RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab) {
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
        this.requestedCourtForClaimDetailsTab = requestedCourtForClaimDetailsTab;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        updateRespondentAddresses(caseData);
        caseData.setClaimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
            RESPONSE_CLAIM_DEADLINE_EXTENSION_MONTHS,
            LocalDate.now()
        ));

        LocalDateTime responseDate = time.now();
        LocalDateTime applicant1Deadline = getApplicant1ResponseDeadline(responseDate);

        if (isRespondent2SameLegalRep(caseData)) {
            updateDataRespondentDeadlineResponse
                .updateBothRespondentsResponseSameLegalRep(
                    callbackParams,
                    caseData,
                    responseDate,
                    applicant1Deadline
            );
        } else if (isSolicitorRepresentingOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            caseData.setRespondent2ResponseDate(responseDate);
            updateDataRespondentDeadlineResponse
                .updateResponseDataForSecondRespondent(
                    callbackParams,
                    caseData,
                    caseData,
                    applicant1Deadline
            );
        } else {
            updateDataRespondentDeadlineResponse
                .updateResponseDataForBothRespondent(
                    callbackParams,
                    responseDate,
                    caseData,
                    applicant1Deadline
            );
        }

        caseData.setIsRespondent1(null);
        assembleDocumentsForDeadlineResponse
            .assembleResponseDocuments(caseData);
        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        addEventAndDateAddedToRespondentExperts(caseData);
        addEventAndDateAddedToRespondentWitnesses(caseData);
        retainSolicitorReferences(callbackParams.getRequest().getCaseDetailsBefore().getData(), caseData, caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(caseData);
        updateClaimsDetailsForClaimDetailsTab(caseData, caseData);
        populateDQPartyIds(caseData);

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE, caseData);
        updateDocumentGenerationRespondent2(callbackParams, caseData, caseData);

        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO
        )) {
            requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2(callbackParams, caseData);
        } else {
            requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1(callbackParams, caseData);
        }

        if (isMultipartyScenario1v2With2LegalRep(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        }

        nullPlaceHolderDocuments(caseData, caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state("AWAITING_APPLICANT_INTENTION")
            .build();
    }

    private boolean isMultipartyScenario1v2With2LegalRep(CaseData caseData) {
        return getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData);
    }

    private static void updateClaimsDetailsForClaimDetailsTab(CaseData updatedData, CaseData caseData) {
        updatedData.getRespondent1().setFlags(null);
        updatedData.setRespondent1DetailsForClaimDetailsTab(updatedData.getRespondent1());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.getRespondent2().setFlags(null);
            updatedData.setRespondent2DetailsForClaimDetailsTab(updatedData.getRespondent2());
        }
    }

    private void updateDocumentGenerationRespondent2(CallbackParams callbackParams, CaseData updatedData, CaseData caseData) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        updatedData.setRespondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference()
                .toString(), userInfo.getUid(), RESPONDENTSOLICITORONE
        )
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference()
                .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO
        )) {
            updatedData.setRespondent2DocumentGeneration("userRespondent2");
        }
    }

    private static void nullPlaceHolderDocuments(CaseData updatedData, CaseData caseData) {
        log.info("Null placeholder documents for Case ID: {}", caseData.getCcdCaseReference());
        updatedData.setRespondent1ClaimResponseDocument(null);
        updatedData.setRespondent2ClaimResponseDocument(null);
        if (caseData.getRespondent1() != null
            && updatedData.getRespondent1DQ() != null) {
            updatedData.getRespondent1DQ().setRespondent1DQDraftDirections(null);
            updatedData.setRespondent1DQ(updatedData.getRespondent1DQ());
        }
        if (caseData.getRespondent2() != null
            && updatedData.getRespondent2DQ() != null) {
            updatedData.getRespondent2DQ().setRespondent2DQDraftDirections(null);
            updatedData.setRespondent2DQ(updatedData.getRespondent2DQ());
        }
    }

    private void updateRespondentAddresses(CaseData caseData) {

        Party updatedRespondent1 = caseData.getRespondent1();
        updatedRespondent1.setPrimaryAddress(caseData.getRespondent1Copy().getPrimaryAddress());

        caseData.setRespondent1(updatedRespondent1);
        caseData.setRespondent1Copy(null);

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            Party updatedRespondent2 = caseData.getRespondent2();
            updatedRespondent2.setPrimaryAddress(caseData.getRespondent2Copy().getPrimaryAddress());

            caseData.setRespondent2(updatedRespondent2);
            caseData.setRespondent2Copy(null);
        }
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate);
    }

    private void retainSolicitorReferences(Map<String, Object> beforeCaseData,
                                           CaseData updatedData,
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

                SolicitorReferences solicitorReferences1 = new SolicitorReferences();
                solicitorReferences1.setApplicantSolicitor1Reference(
                        refMap.getOrDefault("applicantSolicitor1Reference", null));
                solicitorReferences1.setRespondentSolicitor1Reference(
                        ofNullable(defendantSolicitorRef1)
                            .orElse(refMap.getOrDefault("respondentSolicitor1Reference", null)));
                solicitorReferences1.setRespondentSolicitor2Reference(
                        refMap.getOrDefault("respondentSolicitor2Reference", null));
                return solicitorReferences1;
            })
            .orElse(null);

        updatedData.setSolicitorReferences(solicitorReferences);

        String respondentSolicitor2Reference = ofNullable(caseData.getRespondentSolicitor2Reference())
            .orElse(ofNullable(beforeCaseData.get("respondentSolicitor2Reference"))
                        .map(Object::toString).orElse(null));

        updatedData.setSolicitorReferences(solicitorReferences);
        updatedData.setRespondentSolicitor2Reference(respondentSolicitor2Reference);
        updatedData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(
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
