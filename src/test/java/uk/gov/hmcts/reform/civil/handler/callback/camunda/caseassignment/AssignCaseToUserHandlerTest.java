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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.AutomaticallyAssignCaseToCaaConfiguration;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    AssignCaseToUserHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class AssignCaseToUserHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AssignCaseToUserHandler assignCaseToUserHandler;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private AutomaticallyAssignCaseToCaaConfiguration automaticallyAssignCaseToCaaConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    private CallbackParams params;
    private CaseData caseData;

    @Nested
    class AssignRolesIn1v1CasesRegisteredAndRespresented {

        @BeforeEach
        void setup() {
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
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator() {
            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(true);

            when(organisationService.findUsersInOrganisation("OrgId2"))
                .thenReturn(Optional.of(buildPrdResponse()));

            assignCaseToUserHandler.handle(params);

            verifyApplicantSolicitorOneRoles();

            verify(coreCaseUserService).assignCase(
                caseData.getCcdCaseReference().toString(),
                "12345678",
                "OrgId2",
                CaseRole.RESPONDENTSOLICITORONE
            );

        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRemoveCreator() {
            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(false);

            assignCaseToUserHandler.handle(params);

            verifyApplicantSolicitorOneRoles();

            verify(coreCaseUserService, never()).assignCase(
                caseData.getCcdCaseReference().toString(),
                "12345678",
                "OrgId2",
                CaseRole.RESPONDENTSOLICITORONE
            );

        }

        @Test
        void shouldRemoveSubmitterIdAfterCaseAssignment() {
            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(true);

            when(organisationService.findUsersInOrganisation(anyString()))
                .thenReturn(Optional.of(buildPrdResponse()));

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) assignCaseToUserHandler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getApplicantSolicitor1UserDetails().getId()).isNull();
        }
    }

    @Nested
    class AssignRolesIn1v1CasesUnregisteredAndUnrespresented {

        @BeforeEach
        void setup() {
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
            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
        }

        @Test
        void shouldAssignCaseToApplicantSolicitorOneAndRespondentOrgCaaAndRemoveCreator() {
            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(true);

            assignCaseToUserHandler.handle(params);

            verifyApplicantSolicitorOneRoles();

            verify(coreCaseUserService, never()).assignCase(
                caseData.getCcdCaseReference().toString(),
                "12345678",
                "OrgId2",
                CaseRole.RESPONDENTSOLICITORONE
            );

        }
    }

    @Nested
    class AssignRolesIn1v2Cases {

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

            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(true);

            when(organisationService.findUsersInOrganisation("OrgId2"))
                .thenReturn(Optional.of(buildPrdResponse()));

            assignCaseToUserHandler.handle(params);

            verifyApplicantSolicitorOneRoles();

            verify(coreCaseUserService).assignCase(
                caseData.getCcdCaseReference().toString(),
                "12345678",
                "OrgId2",
                CaseRole.RESPONDENTSOLICITORONE
            );
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

            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(true);

            when(organisationService.findUsersInOrganisation("OrgId2"))
                .thenReturn(Optional.of(buildPrdResponse()));

            when(organisationService.findUsersInOrganisation("OrgId3"))
                .thenReturn(Optional.of(buildPrdResponseForOrg3()));

            assignCaseToUserHandler.handle(params);

            verifyApplicantSolicitorOneRoles();

            verify(coreCaseUserService).assignCase(
                caseData.getCcdCaseReference().toString(),
                "12345678",
                "OrgId2",
                CaseRole.RESPONDENTSOLICITORONE
            );

            verify(coreCaseUserService).assignCase(
                caseData.getCcdCaseReference().toString(),
                "gggggggg",
                "OrgId3",
                CaseRole.RESPONDENTSOLICITORTWO
            );
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

            params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);

            when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
                .thenReturn(true);

            when(organisationService.findUsersInOrganisation("OrgId2"))
                .thenReturn(Optional.of(buildPrdResponse()));

            assignCaseToUserHandler.handle(params);

            verifyApplicantSolicitorOneRoles();

            verify(coreCaseUserService).assignCase(
                caseData.getCcdCaseReference().toString(),
                "12345678",
                "OrgId2",
                CaseRole.RESPONDENTSOLICITORONE
            );

            verify(coreCaseUserService, never()).assignCase(
                caseData.getCcdCaseReference().toString(),
                "gggggggg",
                "OrgId3",
                CaseRole.RESPONDENTSOLICITORTWO
            );
        }

        private ProfessionalUsersEntityResponse buildPrdResponseForOrg3() {
            List<ProfessionalUsersResponse> users = new ArrayList<>();
            users.add(ProfessionalUsersResponse.builder()
                          .userIdentifier("gggggggg")
                          .email("hmcts.civil+organisation.3.CAA@gmail.com")
                          .roles(Arrays.asList("caseworker", "caseworker-civil", "pui-caa"))
                          .build());

            users.add(ProfessionalUsersResponse.builder()
                          .email("hmcts.civil+organisation.3.solicitor.1@gmail.com")
                          .userIdentifier("aaaaaaaa")
                          .roles(Arrays.asList("caseworker", "caseworker-civil", "caseworker-civil-solicitor"))
                          .build());

            return ProfessionalUsersEntityResponse.builder()
                .organisationIdentifier("OrgId3")
                .users(users)
                .build();
        }
    }

    private ProfessionalUsersEntityResponse buildPrdResponse() {
        List<ProfessionalUsersResponse> users = new ArrayList<>();
        users.add(ProfessionalUsersResponse.builder()
                      .userIdentifier("12345678")
                      .email("hmcts.civil+organisation.2.CAA@gmail.com")
                      .roles(Arrays.asList("caseworker", "caseworker-civil", "pui-caa"))
                      .build());

        users.add(ProfessionalUsersResponse.builder()
                      .email("hmcts.civil+organisation.2.superuser@gmail.com")
                      .userIdentifier("abcdefg")
                      .roles(Arrays.asList("caseworker", "caseworker-civil", "pui-organisation-manager"))
                      .build());

        users.add(ProfessionalUsersResponse.builder()
                      .email("hmcts.civil+organisation.2.solicitor.1@gmail.com")
                      .userIdentifier("a1b2c3")
                      .roles(Arrays.asList("caseworker", "caseworker-civil", "caseworker-civil-solicitor"))
                      .build());

        return ProfessionalUsersEntityResponse.builder()
            .organisationIdentifier("OrgId2")
            .users(users)
            .build();
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
}
