package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT_SOLICITOR1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment.AssignCaseToApplicantSolicitorCallbackHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
class AssignCaseToApplicantSolicitorCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private AssignCaseToApplicantSolicitorCallbackHandler assignCaseToApplicantSolicitorCallbackHandler;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private ObjectMapper objectMapper;

    private CallbackParams params;
    private CaseData caseData;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Nested
    class AssignHmctsServiceId {

        @Test
        void shouldReturnSupplementaryDataOnSubmitted() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

            Map<String, Object> dataMap = objectMapper.convertValue(localCaseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(localCaseData);
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);
            verify(coreCaseDataService).setSupplementaryData(any(), eq(supplementaryData()));
        }
    }

    @Nested
    class AssignHmctsServiceIdSpec {

        @Test
        void shouldReturnSpecSupplementaryData() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimSubmitted().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
            when(paymentsConfiguration.getSpecSiteId()).thenReturn("AAA6");

            Map<String, Object> dataMap = objectMapper.convertValue(localCaseData, new TypeReference<>() {
            });
            //Supplementary data must be saved on the submitted callback otherwise it isn't persisted correclty
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(localCaseData);
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);
            verify(coreCaseDataService).setSupplementaryData(1594901956117591L, supplementaryDataSpec());
        }
    }

    @Nested
    class AssignRolesIn1v1CasesRegisteredAndRespresented {

        @BeforeEach
        void setup() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .build();
            IdamUserDetails applicantSolicitor1UserDetails = new IdamUserDetails();
            applicantSolicitor1UserDetails.setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9");
            applicantSolicitor1UserDetails.setEmail("applicant@someorg.com");
            caseData.setApplicantSolicitor1UserDetails(applicantSolicitor1UserDetails);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setStatus(BusinessProcessStatus.READY);
            caseData.setBusinessProcess(businessProcess);
            OrganisationPolicy applicant1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation1 = new Organisation();
            organisation1.setOrganisationID("OrgId1");
            applicant1OrganisationPolicy.setOrganisation(organisation1);
            caseData.setApplicant1OrganisationPolicy(applicant1OrganisationPolicy);
            OrganisationPolicy respondent1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation2 = new Organisation();
            organisation2.setOrganisationID("OrgId2");
            respondent1OrganisationPolicy.setOrganisation(organisation2);
            caseData.setRespondent1OrganisationPolicy(respondent1OrganisationPolicy);

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator() {
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRemoveCreator() {
            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }
    }

    @Nested
    class AssignRolesIn1v1CasesUnregisteredAndUnrespresented {
        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator() {

            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .respondent1OrgRegistered(NO)
                .respondent1Represented(NO)
                .build();
            IdamUserDetails applicantSolicitor1UserDetails = new IdamUserDetails();
            applicantSolicitor1UserDetails.setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9");
            applicantSolicitor1UserDetails.setEmail("applicant@someorg.com");
            caseData.setApplicantSolicitor1UserDetails(applicantSolicitor1UserDetails);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setStatus(BusinessProcessStatus.READY);
            caseData.setBusinessProcess(businessProcess);
            OrganisationPolicy applicant1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation1 = new Organisation();
            organisation1.setOrganisationID("OrgId1");
            applicant1OrganisationPolicy.setOrganisation(organisation1);
            caseData.setApplicant1OrganisationPolicy(applicant1OrganisationPolicy);

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }
    }

    @Nested
    class AssignRolesIn1v2Cases {

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator1v2SS() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .multiPartyClaimOneDefendantSolicitor()
                .build();
            IdamUserDetails applicantSolicitor1UserDetails = new IdamUserDetails();
            applicantSolicitor1UserDetails.setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9");
            applicantSolicitor1UserDetails.setEmail("applicant@someorg.com");
            caseData.setApplicantSolicitor1UserDetails(applicantSolicitor1UserDetails);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setStatus(BusinessProcessStatus.READY);
            caseData.setBusinessProcess(businessProcess);
            OrganisationPolicy applicant1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation1 = new Organisation();
            organisation1.setOrganisationID("OrgId1");
            applicant1OrganisationPolicy.setOrganisation(organisation1);
            caseData.setApplicant1OrganisationPolicy(applicant1OrganisationPolicy);
            OrganisationPolicy respondent1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation2 = new Organisation();
            organisation2.setOrganisationID("OrgId2");
            respondent1OrganisationPolicy.setOrganisation(organisation2);
            caseData.setRespondent1OrganisationPolicy(respondent1OrganisationPolicy);

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator1v2DS() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(YES)
                .build();
            IdamUserDetails applicantSolicitor1UserDetails = new IdamUserDetails();
            applicantSolicitor1UserDetails.setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9");
            applicantSolicitor1UserDetails.setEmail("applicant@someorg.com");
            caseData.setApplicantSolicitor1UserDetails(applicantSolicitor1UserDetails);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setStatus(BusinessProcessStatus.READY);
            caseData.setBusinessProcess(businessProcess);
            OrganisationPolicy applicant1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation1 = new Organisation();
            organisation1.setOrganisationID("OrgId1");
            applicant1OrganisationPolicy.setOrganisation(organisation1);
            caseData.setApplicant1OrganisationPolicy(applicant1OrganisationPolicy);
            OrganisationPolicy respondent1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation2 = new Organisation();
            organisation2.setOrganisationID("OrgId2");
            respondent1OrganisationPolicy.setOrganisation(organisation2);
            caseData.setRespondent1OrganisationPolicy(respondent1OrganisationPolicy);
            OrganisationPolicy respondent2OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation3 = new Organisation();
            organisation3.setOrganisationID("OrgId3");
            respondent2OrganisationPolicy.setOrganisation(organisation3);
            caseData.setRespondent2OrganisationPolicy(respondent2OrganisationPolicy);

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRemoveCreator1v2DSUnregisteredRespondent2() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(NO)
                .respondent2OrgRegistered(NO)
                .build();
            IdamUserDetails applicantSolicitor1UserDetails = new IdamUserDetails();
            applicantSolicitor1UserDetails.setId("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9");
            applicantSolicitor1UserDetails.setEmail("applicant@someorg.com");
            caseData.setApplicantSolicitor1UserDetails(applicantSolicitor1UserDetails);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setStatus(BusinessProcessStatus.READY);
            caseData.setBusinessProcess(businessProcess);
            OrganisationPolicy applicant1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation1 = new Organisation();
            organisation1.setOrganisationID("OrgId1");
            applicant1OrganisationPolicy.setOrganisation(organisation1);
            caseData.setApplicant1OrganisationPolicy(applicant1OrganisationPolicy);
            OrganisationPolicy respondent1OrganisationPolicy = new OrganisationPolicy();
            Organisation organisation2 = new Organisation();
            organisation2.setOrganisationID("OrgId2");
            respondent1OrganisationPolicy.setOrganisation(organisation2);
            caseData.setRespondent1OrganisationPolicy(respondent1OrganisationPolicy);

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            when(caseDetailsConverter.toCaseData(params.getRequest().getCaseDetails())).thenReturn(caseData);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(assignCaseToApplicantSolicitorCallbackHandler.handledEvents()).containsOnly(
            ASSIGN_CASE_TO_APPLICANT_SOLICITOR1);
    }

    @Test
    void shouldReturnCorrectCamundaTaskID() {
        assertThat(assignCaseToApplicantSolicitorCallbackHandler.camundaActivityId(CallbackParamsBuilder.builder()
                                                                                       .request(CallbackRequest.builder().eventId(
                                                                                           "ASSIGN_CASE_TO_APPLICANT_SOLICITOR1").build()).build())).isEqualTo(
            TASK_ID);
    }

    private void verifyApplicantSolicitorOneRoles() {
        verify(coreCaseUserService).assignCase(
            caseData.getCcdCaseReference().toString(),
            caseData.getApplicantSolicitor1UserDetails().getId(),
            "OrgId1",
            CaseRole.APPLICANTSOLICITORONE
        );

        verify(coreCaseUserService).removeCreatorRoleCaseAssignment(
            caseData.getCcdCaseReference().toString(),
            caseData.getApplicantSolicitor1UserDetails().getId(),
            "OrgId1"
        );

    }

    private Map<String, Map<String, Map<String, Object>>> supplementaryData() {
        Map<String, Object> hmctsServiceIdMap = new HashMap<>();
        hmctsServiceIdMap.put("HMCTSServiceId", "AAA7");

        Map<String, Map<String, Object>> supplementaryDataRequestMap = new HashMap<>();
        supplementaryDataRequestMap.put("$set", hmctsServiceIdMap);

        Map<String, Map<String, Map<String, Object>>> supplementaryDataUpdates = new HashMap<>();
        supplementaryDataUpdates.put("supplementary_data_updates", supplementaryDataRequestMap);

        return supplementaryDataUpdates;
    }

    private Map<String, Map<String, Map<String, Object>>> supplementaryDataSpec() {
        Map<String, Object> hmctsServiceIdMap = new HashMap<>();
        hmctsServiceIdMap.put("HMCTSServiceId", "AAA6");

        Map<String, Map<String, Object>> supplementaryDataRequestMap = new HashMap<>();
        supplementaryDataRequestMap.put("$set", hmctsServiceIdMap);

        Map<String, Map<String, Map<String, Object>>> supplementaryDataUpdates = new HashMap<>();
        supplementaryDataUpdates.put("supplementary_data_updates", supplementaryDataRequestMap);

        return supplementaryDataUpdates;
    }

}
