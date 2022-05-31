package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                                               .organisation(Organisation.builder().organisationID("OrgId2").build())
                                               .build())
            .build();

        Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
        });
        params = callbackParamsOf(dataMap, CallbackType.ABOUT_TO_SUBMIT);
    }

    @Test
    void shouldAssignCaseToApplicantSolicitorOneAndRemoveCreator() {
        when(automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa())
            .thenReturn(true);

        when(organisationService.findUsersInOrganisation("OrgId2"))
            .thenReturn(Optional.of(buildPrdResponse()));

        assignCaseToUserHandler.handle(params);

        verify(coreCaseUserService).assignCase(
            caseData.getCcdCaseReference().toString(),
            caseData.getApplicantSolicitor1UserDetails().getId(),
            "OrgId1",
            CaseRole.APPLICANTSOLICITORONE
        );

        verify(coreCaseUserService).assignCase(
            caseData.getCcdCaseReference().toString(),
            "12345678",
            "OrgId2",
            CaseRole.RESPONDENTSOLICITORONE
        );

        verify(coreCaseUserService).removeCreatorRoleCaseAssignment(
            caseData.getCcdCaseReference().toString(),
            caseData.getApplicantSolicitor1UserDetails().getId(),
            "OrgId1"
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
}
