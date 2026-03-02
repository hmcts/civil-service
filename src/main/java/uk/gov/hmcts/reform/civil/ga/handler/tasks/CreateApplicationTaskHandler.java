package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.data.ExternalTaskInput;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.ga.utils.OrgPolicyUtils.getRespondent1SolicitorOrgId;
import static uk.gov.hmcts.reform.civil.ga.utils.OrgPolicyUtils.getRespondent2SolicitorOrgId;

@RequiredArgsConstructor
@Component
public class CreateApplicationTaskHandler extends BaseExternalTaskHandler {

    private static final String GENERAL_APPLICATION_CASE_ID = "generalApplicationCaseId";
    private static final List<String> BILINGUAL_TYPES = Arrays.asList("BOTH", "WELSH");
    private final GaCoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;
    private final GaStateFlowEngine stateFlowEngine;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        ExternalTaskInput variables = mapper.convertValue(externalTask.getAllVariables(), ExternalTaskInput.class);
        String caseId = variables.getCaseId();
        log.info("Starting CreateApplicationTaskHandler for case ID: {}", caseId);

        StartEventResponse startEventResponse = coreCaseDataService.startUpdate(caseId, variables.getCaseEvent());
        log.debug("Started event update for case ID: {}, event token: {}", caseId, startEventResponse.getToken());
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(startEventResponse.getCaseDetails());
        var generalApplications = caseData.getGeneralApplications();
        GeneralApplicationCaseData generalAppCaseData = null;

        if (generalApplications != null && !generalApplications.isEmpty()) {

            var genApps = generalApplications.stream()
                .filter(application -> application.getValue() != null
                    && application.getValue().getBusinessProcess() != null
                    && application.getValue().getBusinessProcess().getStatus() == BusinessProcessStatus.STARTED
                    && application.getValue().getBusinessProcess().getProcessInstanceId() != null).findFirst();

            if (genApps.isPresent()) {
                log.debug("Eligible general application found for processing in case ID: {}", caseId);

                GeneralApplication generalApplication = genApps.get().getValue();

                boolean claimantBilingual = BILINGUAL_TYPES.contains(caseData.getClaimantBilingualLanguagePreference());
                boolean defendantBilingual = caseData.getRespondent1LiPResponse() != null
                    && BILINGUAL_TYPES.contains(caseData.getRespondent1LiPResponse().getRespondent1ResponseLanguage());
                generalAppCaseData = createGeneralApplicationCase(caseId, generalApplication, claimantBilingual, defendantBilingual);
                log.debug("General application case created with ID: {}", generalAppCaseData.getCcdCaseReference());
                updateParentCaseGeneralApplication(variables, generalApplication, generalAppCaseData);

                caseData = withoutNoticeNoConsent(generalApplication, caseData, generalAppCaseData);
            }
        }

        var parentCaseData = coreCaseDataService.submitUpdate(caseId,
                                                              coreCaseDataService.caseDataContentFromStartEventResponse(
                                                                  startEventResponse,
                                                                  caseData.toMap(mapper)));
        return new ExternalTaskData()
            .setParentCaseData(parentCaseData)
            .setUpdateGeneralApplicationCaseData(generalAppCaseData);
    }

    private GeneralApplicationCaseData withoutNoticeNoConsent(GeneralApplication generalApplication,
                                                              GeneralApplicationCaseData caseData,
                                                              GeneralApplicationCaseData generalAppCaseData) {

        /*
         * Add the case to applicant solicitor collection if parent claimant is applicant
         * Hide the case if parent claimant isn't GA applicant and initiate without notice application
         * */
        var applications = ofNullable(caseData.getClaimantGaAppDetails()).orElse(newArrayList());

        if (generalApplication.getParentClaimantIsApplicant().equals(YES)) {
            applications = addApplication(
                buildApplication(generalApplication, generalAppCaseData),
                caseData.getClaimantGaAppDetails()
            );
        }

        /*
         * Add the GA in respondent one collection if he/she initiate without notice application.
         * */

        var respondentSpecficGADetails =
            ofNullable(caseData.getRespondentSolGaAppDetails()).orElse(newArrayList());

        if (generalApplication.getGeneralAppApplnSolicitor() != null
            && generalApplication.getGeneralAppApplnSolicitor().getOrganisationIdentifier() != null
            && generalApplication.getGeneralAppApplnSolicitor().getOrganisationIdentifier()
            .equals(getRespondent1SolicitorOrgId(caseData))) {

            GADetailsRespondentSol gaDetailsRespondentSol = buildRespApplication(generalApplication, generalAppCaseData);

            if (gaDetailsRespondentSol != null) {
                respondentSpecficGADetails = addRespApplication(
                    gaDetailsRespondentSol, caseData.getRespondentSolGaAppDetails());
            }
        }

        /*
         * Add the GA in respondent one collection if he/she initiate without notice application, and he is Lip.
         * */

        var respondentTwoSpecficGADetails =
            ofNullable(caseData.getRespondentSolTwoGaAppDetails()).orElse(newArrayList());
        if (generalApplication.getGeneralAppApplnSolicitor() != null
            && generalApplication.getParentClaimantIsApplicant().equals(NO)
            && Objects.nonNull(generalApplication.getIsGaApplicantLip())
            && generalApplication.getIsGaApplicantLip().equals(YES)) {

            GADetailsRespondentSol gaDetailsRespondentSol = buildRespApplication(generalApplication, generalAppCaseData);

            if (gaDetailsRespondentSol != null) {
                respondentSpecficGADetails = addRespApplication(
                    gaDetailsRespondentSol, caseData.getRespondentSolGaAppDetails());
            }
        }

        /*
         * Add the GA in respondent two collection if he/she initiate without notice application.
         * */
        if (YES.equals(generalApplication.getIsMultiParty()) && NO.equals(caseData.getAddApplicant2())
            && NO.equals(caseData.getRespondent2SameLegalRepresentative())
            && generalApplication.getGeneralAppApplnSolicitor().getOrganisationIdentifier()
            .equals(getRespondent2SolicitorOrgId(caseData))) {

            GADetailsRespondentSol gaDetailsRespondentSolTwo = buildRespApplication(
                generalApplication, generalAppCaseData);

            if (gaDetailsRespondentSolTwo != null) {
                respondentTwoSpecficGADetails = addRespApplication(
                    gaDetailsRespondentSolTwo, caseData.getRespondentSolTwoGaAppDetails());
            }
        }

        return caseData.copy()
            .claimantGaAppDetails(applications)
            .respondentSolGaAppDetails(respondentSpecficGADetails)
            .respondentSolTwoGaAppDetails(respondentTwoSpecficGADetails)
            .build();
    }

    private GeneralApplicationsDetails buildApplication(GeneralApplication generalApplication,
                                                        GeneralApplicationCaseData generalAppCaseData) {
        List<GeneralApplicationTypes> types = generalApplication.getGeneralAppType().getTypes();
        String collect = types.stream().map(GeneralApplicationTypes::getDisplayedValue)
            .collect(Collectors.joining(", "));
        GeneralApplicationsDetails gaDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType(collect)
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(CaseLink.builder().caseReference(String.valueOf(
                generalAppCaseData.getCcdCaseReference())).build())
            .caseState(PENDING_APPLICATION_ISSUED.getDisplayedValue())
            .build();

        return gaDetails.toBuilder()
            .parentClaimantIsApplicant(generalApplication.getParentClaimantIsApplicant())
            .build();
    }

    private GADetailsRespondentSol buildRespApplication(GeneralApplication generalApplication,
                                                        GeneralApplicationCaseData generalAppCaseData) {
        List<GeneralApplicationTypes> types = generalApplication.getGeneralAppType().getTypes();
        String collect = types.stream().map(GeneralApplicationTypes::getDisplayedValue)
            .collect(Collectors.joining(", "));

        GADetailsRespondentSol gaRespondentDetails = GADetailsRespondentSol.builder()
            .generalApplicationType(collect)
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(CaseLink.builder().caseReference(String.valueOf(
                generalAppCaseData.getCcdCaseReference())).build())
            .caseState(PENDING_APPLICATION_ISSUED.getDisplayedValue())
            .build();

        return gaRespondentDetails.toBuilder()
            .parentClaimantIsApplicant(generalApplication.getParentClaimantIsApplicant())
            .build();
    }

    private List<Element<GeneralApplicationsDetails>> addApplication(GeneralApplicationsDetails application,
                                                                     List<Element<GeneralApplicationsDetails>>
                                                                         claimantGaAppDetails) {
        List<Element<GeneralApplicationsDetails>> newApplication = ofNullable(claimantGaAppDetails)
            .orElse(newArrayList());
        newApplication.add(element(application));
        return newApplication;
    }

    private List<Element<GADetailsRespondentSol>> addRespApplication(GADetailsRespondentSol application,
                                                                     List<Element<GADetailsRespondentSol>>
                                                                         respondentSpecficGADetails) {
        List<Element<GADetailsRespondentSol>> newApplication = ofNullable(respondentSpecficGADetails)
            .orElse(newArrayList());
        newApplication.add(element(application));
        return newApplication;
    }

    private void updateParentCaseGeneralApplication(ExternalTaskInput variables,
                                                    GeneralApplication generalApplication,
                                                    GeneralApplicationCaseData generalAppCaseData) {
        generalApplication.getBusinessProcess().setStatus(BusinessProcessStatus.FINISHED);
        if (Objects.nonNull(generalApplication.getGeneralAppEvidenceDocument())) {
            generalApplication.getGeneralAppEvidenceDocument().clear();
        }
        generalApplication.setGeneralAppN245FormUpload(null);
        generalApplication.getBusinessProcess().setCamundaEvent(variables.getCaseEvent().name());
        if (generalAppCaseData != null && generalAppCaseData.getCcdCaseReference() != null) {
            generalApplication.setCaseLink(CaseLink.builder().caseReference(String.valueOf(
                generalAppCaseData.getCcdCaseReference())).build());
        }
    }

    private GeneralApplicationCaseData createGeneralApplicationCase(String caseId, GeneralApplication generalApplication,
                                                                    boolean claimantBilingual, boolean defendantBilingual) {
        Map<String, Object> map = generalApplication.toMap(mapper);
        map.put("isDocumentVisible", checkVisibility(generalApplication));
        map.put("generalAppNotificationDeadlineDate", generalApplication.getGeneralAppDateDeadline());
        map.put("applicationTypes", String.join(", ", getTypesString(generalApplication)));
        map.put("parentCaseReference", caseId);
        List<Element<CaseDocument>> addlDoc =
            DocUploadUtils.prepareDocuments(
                generalApplication.getGeneralAppEvidenceDocument(),
                DocUploadUtils.APPLICANT, CaseEvent.INITIATE_GENERAL_APPLICATION
            );
        if (Objects.nonNull(addlDoc)) {
            map.put("gaAddlDoc", addlDoc);
            map.put("gaAddlDocStaff", addlDoc);
            map.put("gaAddlDocClaimant", addlDoc);
            map.put("generalAppEvidenceDocument", null);
        }
        if (claimantBilingual) {
            if (generalApplication.getParentClaimantIsApplicant() == YES) {
                map.put("applicantBilingualLanguagePreference", YES);
            } else {
                map.put("respondentBilingualLanguagePreference", YES);
            }
        }
        if (defendantBilingual) {
            if (generalApplication.getParentClaimantIsApplicant() == YES) {
                map.put("respondentBilingualLanguagePreference", YES);
            } else {
                map.put("applicantBilingualLanguagePreference", YES);
            }
        }

        return coreCaseDataService.createGeneralAppCase(removeGeneralAppFeeToPayInTextField(map));
    }

    private Map<String, Object> removeGeneralAppFeeToPayInTextField(Map<String, Object> map) {
        if (map.get("generalAppPBADetails") instanceof Map<?, ?> generalAppPBADetailsMap) {
            generalAppPBADetailsMap.remove("generalAppFeeToPayInText");
        }
        return map;
    }

    private String getTypesString(final GeneralApplication generalApplication) {
        List<String> types = generalApplication.getGeneralAppType()
            .getTypes().stream().map(GeneralApplicationTypes::getDisplayedValue).toList();
        return String.join(", ", types);
    }

    private YesOrNo checkVisibility(GeneralApplication generalApplication) {
        /*
         * Respondent Agreement is No and without notice application.
         * Application should be visible to solicitor who initiates the ga
         * */
        if ((generalApplication.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)
            && ofNullable(generalApplication.getGeneralAppInformOtherParty()).isPresent()
            && NO.equals(generalApplication.getGeneralAppInformOtherParty().getIsWithNotice()))) {
            return NO;
        }
        /*
         * Respondent Agreement is NO and with notice.
         * Application should be visible to all solicitors
         * Consent order should be visible to all solicitors
         * */
        if ((generalApplication.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)
            && ofNullable(generalApplication.getGeneralAppInformOtherParty()).isPresent()
            && YES.equals(generalApplication.getGeneralAppInformOtherParty().getIsWithNotice()))
            || generalApplication.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES)) {
            return YES;
        }

        /* Urgent Application */

        if (generalApplication.getGeneralAppUrgencyRequirement() != null
            && generalApplication.getGeneralAppUrgencyRequirement().getGeneralAppUrgency().equals(YES)) {
            return NO;
        }

        return YES;
    }

    @Override
    public VariableMap getVariableMap(ExternalTaskData externalTaskData) {
        var data = externalTaskData.parentCaseData().orElseThrow();
        VariableMap variables = Variables.createVariables();
        var stateFlow = stateFlowEngine.evaluate(data);
        var stateFlowName = stateFlow.getState().getName();
        var stateFlowFlags = stateFlow.getFlags();
        variables.putValue(FLOW_STATE, stateFlowName);
        variables.putValue(FLOW_FLAGS, stateFlowFlags);
        log.debug("State flow evaluation completed with {} state and {} flags for case ID: {}",
                  stateFlowName, stateFlowFlags, data.getCcdCaseReference());
        var generalAppCaseData = externalTaskData.getUpdateGeneralApplicationCaseData();
        if (generalAppCaseData != null && generalAppCaseData.getCcdCaseReference() != null) {
            variables.putValue(GENERAL_APPLICATION_CASE_ID, generalAppCaseData.getCcdCaseReference());
            log.info("Added general application case ID to variables: {}", generalAppCaseData.getCcdCaseReference());
        }
        return variables;
    }
}
