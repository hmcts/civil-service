package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceHelper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder.STRING_CONSTANT;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder.STRING_NUM_CONSTANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {
    InitiateGeneralApplicationServiceHelper.class,
    JacksonAutoConfiguration.class,
})
public class InitiateGeneralApplicationServiceHelperTest {

    private static final String TEST_USER_EMAILID = "test@gmail.com";
    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String RESPONDENT_EMAIL_ID_CONSTANT = "respondent@gmail.com";

    @Autowired
    private InitiateGeneralApplicationServiceHelper helper;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private UserService userService;

    @MockBean
    private CrossAccessUserConfiguration crossAccessUserConfiguration;

    @MockBean
    protected IdamClient idamClient;

    CaseData caseData = GeneralApplicationDetailsBuilder.builder()
        .getTestCaseData(CaseData.builder().build());

    public UserDetails getUserDetails(String email) {
        return UserDetails.builder().id(STRING_NUM_CONSTANT)
            .email(email)
            .build();
    }

    @BeforeEach
    void setup() {
        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseAssignedApplicantUserRoles()).build());
    }

    public List<CaseAssignedUserRole> getCaseAssignedApplicantUserRoles() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("3")
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("4")
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("5")
                .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    public CaseData getTestCaseData(CaseData caseData, boolean respondentExits) {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("1")
            .email("test@gmail.com").organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("2")
            .email("test@gmail.com").organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        if (respondentExits) {
            return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                                    .types(singletonList(EXTEND_TIME))
                                    .build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id(STRING_CONSTANT)
                                                    .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .generalAppRespondentSolictor(respondentSols)
                .generalAppApplnSolictor(GASolicitorDetailsGAspec
                                             .builder()
                                             .id("1")
                                             .email(TEST_USER_EMAILID)
                                             .organisationIdentifier("Org1").build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                    .organisationID(STRING_CONSTANT).build())
                                                  .orgPolicyReference(STRING_CONSTANT).build())
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .build();
        } else {
            return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                                    .types(singletonList(EXTEND_TIME))
                                    .build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                    .id(STRING_CONSTANT)
                                                    .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .generalAppApplnSolictor(GASolicitorDetailsGAspec
                                             .builder()
                                             .id("1")
                                             .email(TEST_USER_EMAILID)
                                             .organisationIdentifier("Org1").build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                    .organisationID(STRING_CONSTANT).build())
                                                  .orgPolicyReference(STRING_CONSTANT).build())
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .build();
        }
    }

    public GeneralApplication getGeneralApplication() {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        return builder.generalAppType(GAApplicationType.builder()
                                          .types(singletonList(SUMMARY_JUDGEMENT))
                                          .build())
            .build();
    }

    @Test
    void shouldReturnsFourRespondents() {

        GeneralApplication result = helper
            .setApplicantAndRespondentDetailsIfExits(
                GeneralApplication.builder().build(),
                getTestCaseData(CaseData.builder().build(), true),
                getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
            );

        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppRespondentSolictor()).isNotNull();
        assertThat(result.getGeneralAppRespondentSolictor().size()).isEqualTo(4);

        ArrayList<String> userID = new ArrayList<>(Arrays.asList("2", "3", "4", "5"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolictor()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolictor()
                       .stream().filter(e -> STRING_NUM_CONSTANT
                .equals(e.getValue().getId())).count()).isEqualTo(0);

    }

    @Test
    void shouldThrowExceptionIfNoRespondentExits() {

        try {
            helper
                .setApplicantAndRespondentDetailsIfExits(
                    GeneralApplication
                        .builder()
                        .build(),
                    getTestCaseData(CaseData.builder()
                                        .build(), false),
                    getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
                );
        } catch (Exception e) {
            assertEquals("java.lang.NullPointerException", e.toString());
        }
    }
}
