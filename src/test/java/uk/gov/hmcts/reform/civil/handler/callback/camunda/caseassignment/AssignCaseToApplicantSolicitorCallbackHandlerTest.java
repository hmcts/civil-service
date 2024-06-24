package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

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

@SpringBootTest(classes = {
    AssignCaseToApplicantSolicitorCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class AssignCaseToApplicantSolicitorCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AssignCaseToApplicantSolicitorCallbackHandler assignCaseToApplicantSolicitorCallbackHandler;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @MockBean
    private FeatureToggleService toggleService;

    @Autowired
    private ObjectMapper objectMapper;

    private CallbackParams params;
    private CaseData caseData;

    @Nested
    class AssignHmctsServiceId {

        @BeforeEach
        void setup() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");

            Map<String, Object> dataMap = objectMapper.convertValue(localCaseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
        }

        @Test
        void shouldReturnSupplementaryDataOnSubmitted() {
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);
            verify(coreCaseDataService).setSupplementaryData(any(), eq(supplementaryData()));
        }
    }

    @Nested
    class AssignHmctsServiceIdSpec {

        @BeforeEach
        void setup() {
            CaseData localCaseData = CaseDataBuilder.builder().atStateClaimSubmitted().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
            when(paymentsConfiguration.getSpecSiteId()).thenReturn("AAA6");

            Map<String, Object> dataMap = objectMapper.convertValue(localCaseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
        }

        @Test
        void shouldReturnSpecSupplementaryData() {
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);
            verify(coreCaseDataService).setSupplementaryData(1594901956117591L, supplementaryDataSpec());
        }

    }

    @Nested
    class AssignRolesIn1v1CasesRegisteredAndRespresented {

        @BeforeEach
        void setup() {
            when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                                    .email("applicant@someorg.com")
                                                    .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("OrgId1").build())
                                                  .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID("OrgId2").build())
                                                   .build())
                .build();

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator() {
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRemoveCreator() {
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }
    }

    @Nested
    class AssignRolesIn1v1CasesUnregisteredAndUnrespresented {

        @BeforeEach
        void setup() {
            when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                                    .email("applicant@someorg.com")
                                                    .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("OrgId1").build())
                                                  .build())
                .respondent1OrgRegistered(NO)
                .respondent1Represented(NO)
                .build();

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator() {
            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }
    }

    @Nested
    class AssignRolesIn1v2Cases {

        @BeforeEach
        void setup() {
            when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator1v2SS() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                                    .email("applicant@someorg.com")
                                                    .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("OrgId1").build())
                                                  .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID("OrgId2").build())
                                                   .build())
                .multiPartyClaimOneDefendantSolicitor()
                .build();

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator1v2DS() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                                    .email("applicant@someorg.com")
                                                    .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("OrgId1").build())
                                                  .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID("OrgId2").build())
                                                   .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID("OrgId3").build())
                                                   .build())
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(YES)
                .build();

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRemoveCreator1v2DSUnregisteredRespondent2() {
            caseData = new CaseDataBuilder().atStateClaimDraft()
                .caseReference(CaseDataBuilder.CASE_ID)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id("f5e5cc53-e065-43dd-8cec-2ad005a6b9a9")
                                                    .email("applicant@someorg.com")
                                                    .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("OrgId1").build())
                                                  .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID("OrgId2").build())
                                                   .build())
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2Represented(NO)
                .respondent2OrgRegistered(NO)
                .build();

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });

            params = callbackParamsOf(dataMap, ASSIGN_CASE_TO_APPLICANT_SOLICITOR1.name(), CallbackType.SUBMITTED);

            assignCaseToApplicantSolicitorCallbackHandler.handle(params);

            verifyApplicantSolicitorOneRoles();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(assignCaseToApplicantSolicitorCallbackHandler.handledEvents()).containsOnly(ASSIGN_CASE_TO_APPLICANT_SOLICITOR1);
    }

    @Test
    void shouldReturnCorrectCamundaTaskID() {
        assertThat(assignCaseToApplicantSolicitorCallbackHandler.camundaActivityId(CallbackParamsBuilder.builder()
                                                                 .request(CallbackRequest.builder().eventId(
            "ASSIGN_CASE_TO_APPLICANT_SOLICITOR1").build()).build())).isEqualTo(TASK_ID);
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
