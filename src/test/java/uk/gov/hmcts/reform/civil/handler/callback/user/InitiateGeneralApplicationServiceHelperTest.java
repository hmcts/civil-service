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
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceHelper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder.STRING_CONSTANT;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder.STRING_NUM_CONSTANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

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

    public UserDetails getUserDetails(String id, String email) {
        return UserDetails.builder().id(id)
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
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("3")
                .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("4")
                .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("5")
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersWithEmptyRole() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsers() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForDefendant1ToBeApplicant() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("1")
                .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForDefendant2ToBeApplicant() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("1")
                .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(RESPONDENTSOLICITORTWO.getFormattedName()).build()
        );
    }

    @Test
    void shouldReturnsFourRespondents() {

        GeneralApplication result = helper
            .setRespondentDetailsIfPresent(
                GeneralApplication.builder().build(),
                getTestCaseData(CaseData.builder().build(), true),
                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
            );

        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
        assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(4);

        ArrayList<String> userID = new ArrayList<>(Arrays.asList("2", "3", "4", "5"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> STRING_NUM_CONSTANT
                .equals(e.getValue().getId())).count()).isEqualTo(0);

    }

    @Test
    void shouldReturnsFourRespondentsWithEmptyDetails() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersWithEmptyRole()).build());

        assertThrows(IllegalArgumentException.class, () -> helper
            .setRespondentDetailsIfPresent(
                GeneralApplication.builder().build(),
                getTestCaseData(CaseData.builder().build(), true),
                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
            ));

    }

    @Test
    void shouldThrowExceptionIfApplicant1OrganisationPolicyIsNull() {

        assertThrows(
            IllegalArgumentException.class,
            () -> helper
                .setRespondentDetailsIfPresent(
                    GeneralApplication.builder().build(),
                    CaseData.builder().ccdCaseReference(1234L).build(),
                    getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
                )
        );

    }

    @Test
    void shouldThrowExceptionIfRespondent1OrganisationPolicyIsNull() {

        assertThrows(
            IllegalArgumentException.class,
            () -> helper
                .setRespondentDetailsIfPresent(
                    GeneralApplication.builder().build(),
                    CaseData.builder().ccdCaseReference(1234L)
                        .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                        .respondent2(Party.builder().type(COMPANY).companyName("Respondent1").build())
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder().build()).build(),
                    getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
                )
        );

    }

    @Test
    void shouldThrowExceptionIfgetRespondent2IsNull() {

        assertThrows(
            IllegalArgumentException.class,
            () -> helper
                .setRespondentDetailsIfPresent(
                    GeneralApplication.builder().build(),
                    CaseData.builder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                        .addRespondent2(YesOrNo.YES)
                        .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                        .respondent2(Party.builder().type(COMPANY).companyName("Respondent1").build())
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder().build()).build(),
                    getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
                )
        );
    }

    @Test
    void shouldNotExceptionClaimantDetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsers()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
            GeneralApplication.builder().build(),
            CaseData.builder().ccdCaseReference(1234L)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("345").build())
                        .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName()).build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent2(Party.builder().type(COMPANY).companyName("Respondent1").build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("123").build())
                                                  .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE
                                                                                 .getFormattedName())
                                                  .build()).build(),
            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        ArrayList<String> userID = new ArrayList<>(Collections.singletonList("2"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> STRING_NUM_CONSTANT
                .equals(e.getValue().getId())).count()).isEqualTo(0);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getEmail()).isEqualTo(RESPONDENT_EMAIL_ID_CONSTANT);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getOrganisationIdentifier()).isEqualTo("345");
        assertThat(result.getApplicantPartyName()).isEqualTo("Applicant1");

    }

    @Test
    void shouldNotExceptionDefendent1DetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForDefendant1ToBeApplicant()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
            GeneralApplication.builder().build(),
            CaseData.builder().ccdCaseReference(1234L)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("345").build())
                                                   .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                                                                  .getFormattedName())
                                                   .build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().id(STRING_NUM_CONSTANT)
                        .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("123").build())
                        .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                        .build())
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1(Party.builder().type(COMPANY).companyName("Respondent1").build())
                .build(),
            getUserDetails("1", RESPONDENT_EMAIL_ID_CONSTANT)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        ArrayList<String> userID = new ArrayList<>(Collections.singletonList(STRING_NUM_CONSTANT));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> "1"
                .equals(e.getValue().getId())).count()).isEqualTo(0);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getEmail()).isEqualTo(APPLICANT_EMAIL_ID_CONSTANT);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getOrganisationIdentifier()).isEqualTo("123");

        assertThat(result.getApplicantPartyName()).isEqualTo("Respondent1");

    }

    @Test
    void shouldNotExceptionDefendent2DetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForDefendant2ToBeApplicant()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
            GeneralApplication.builder().build(),
            CaseData.builder().ccdCaseReference(1234L)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("123").build())
                                                   .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                                                                  .getFormattedName())
                                                   .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("1234").build())
                                                   .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO
                                                                                  .getFormattedName())
                                                   .build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .respondentSolicitor2EmailAddress(TEST_USER_EMAILID)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1(Party.builder().type(COMPANY).companyName("Respondent1").build())
                .respondent2(Party.builder().type(COMPANY).companyName("Respondent2").build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().id(STRING_NUM_CONSTANT)
                        .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("6789").build())
                        .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                        .build()).build(),
            getUserDetails("2", TEST_USER_EMAILID)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        ArrayList<String> userID = new ArrayList<>(Arrays.asList(STRING_NUM_CONSTANT, "1"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> "2"
                .equals(e.getValue().getId())).count()).isEqualTo(0);

        ArrayList<String> respEmailIds = new ArrayList<>(Arrays.asList(APPLICANT_EMAIL_ID_CONSTANT,
                RESPONDENT_EMAIL_ID_CONSTANT));
        respEmailIds.forEach(emailId -> assertThat(result.getGeneralAppRespondentSolicitors()
                .stream().filter(e -> emailId.equals(e.getValue().getEmail()))
                .count()).isEqualTo(1));

        ArrayList<String> respOrgs = new ArrayList<>(Arrays.asList("123", "6789"));
        respOrgs.forEach(org -> assertThat(result.getGeneralAppRespondentSolicitors()
                .stream().filter(e -> org.equals(e.getValue().getOrganisationIdentifier()))
                .count()).isEqualTo(1));
        assertThat(result.getApplicantPartyName()).isEqualTo("Respondent2");
    }

    @Test
    void shouldReturnsTrueifClaimantIDMatchesWithLogin() {

        CaseData caseData = getTestCaseData(CaseData.builder().build(), true);

        assertThat(helper.isPCClaimantEmailIDSameAsLoginUser(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
        )).isEqualTo(true);
    }

    @Test
    void shouldReturnsfalseifClaimantIDMatchesWithLogin() {

        CaseData caseData = getTestCaseData(CaseData.builder().build(), true);

        assertThat(helper.isPCClaimantEmailIDSameAsLoginUser(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getUserDetails(STRING_NUM_CONSTANT, TEST_USER_EMAILID)
        )).isEqualTo(false);
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
                .generalAppRespondentSolicitors(respondentSols)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                                              .builder()
                                              .id("1")
                                              .email(TEST_USER_EMAILID)
                                              .organisationIdentifier("Org1").build())
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent2(Party.builder().type(COMPANY).companyName("Respondent1").build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
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
                .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec
                                                                 .builder()
                                                                 .id("1")
                                                                 .email(TEST_USER_EMAILID)
                                                                 .organisationIdentifier("Org1").build()))
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent2(Party.builder().type(COMPANY).companyName("Respondent1").build())
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
}
