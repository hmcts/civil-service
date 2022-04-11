package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.apache.commons.lang.StringUtils;
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
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
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
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
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
                .build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForDefendant1ToBeApplicant() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("1")
                .caseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForDefendant2ToBeApplicant() {
        return List.of(
            CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                .caseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build(),
            CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                .caseRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()).build()
        );
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

        GeneralApplication result = helper
            .setApplicantAndRespondentDetailsIfExits(
                GeneralApplication.builder().build(),
                getTestCaseData(CaseData.builder().build(), true),
                getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
            );

        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
        assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);

        ArrayList<String> userID = new ArrayList<>(Collections.singletonList("2"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> STRING_NUM_CONSTANT
                .equals(e.getValue().getId())).count()).isEqualTo(0);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getEmail()).isEqualTo(StringUtils.EMPTY);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getOrganisationIdentifier()).isEqualTo(StringUtils.EMPTY);

    }

    @Test
    void shouldThrowExceptionIfApplicant1OrganisationPolicyIsNull() {

        assertThrows(
            IllegalArgumentException.class,
            () -> helper
                .setApplicantAndRespondentDetailsIfExits(
                    GeneralApplication.builder().build(),
                    CaseData.builder().ccdCaseReference(1234L).build(),
                    getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
                )
        );

    }

    @Test
    void shouldThrowExceptionIfRespondent1OrganisationPolicyIsNull() {

        assertThrows(
            IllegalArgumentException.class,
            () -> helper
                .setApplicantAndRespondentDetailsIfExits(
                    GeneralApplication.builder().build(),
                    CaseData.builder().ccdCaseReference(1234L)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder().build()).build(),
                    getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
                )
        );

    }

    @Test
    void shouldThrowExceptionIfgetRespondent2IsNull() {

        assertThrows(
            IllegalArgumentException.class,
            () -> helper
                .setApplicantAndRespondentDetailsIfExits(
                    GeneralApplication.builder().build(),
                    CaseData.builder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder().build()).build(),
                    getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
                )
        );
    }

    @Test
    void shouldNotExceptionClaimantDetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsers()).build());

        GeneralApplication result = helper.setApplicantAndRespondentDetailsIfExits(
            GeneralApplication.builder().build(),
            CaseData.builder().ccdCaseReference(1234L)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("test@gmail.com").build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .organisation(Organisation.builder().organisationID("123").build())
                                                  .orgPolicyCaseAssignedRole(CaseRole
                                                                                 .RESPONDENTSOLICITORONE
                                                                                 .getFormattedName())
                                                  .build()).build(),
            getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
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
                       .getEmail()).isEqualTo("test@gmail.com");

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getOrganisationIdentifier()).isEqualTo("123");

    }

    @Test
    void shouldNotExceptionDefendent1DetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForDefendant1ToBeApplicant()).build());

        GeneralApplication result = helper.setApplicantAndRespondentDetailsIfExits(
            GeneralApplication.builder().build(),
            CaseData.builder().ccdCaseReference(1234L)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("123").build())
                                                   .orgPolicyCaseAssignedRole(CaseRole
                                                                                  .RESPONDENTSOLICITORONE
                                                                                  .getFormattedName())
                                                   .build())
                .respondentSolicitor1EmailAddress("test@gmail.com")
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .orgPolicyCaseAssignedRole(CaseRole
                                                                                 .APPLICANTSOLICITORONE
                                                                                 .getFormattedName())
                                                  .build()).build(),
            getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        ArrayList<String> userID = new ArrayList<>(Collections.singletonList("1"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                                             .stream().filter(e -> uid.equals(e.getValue().getId()))
                                             .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                       .stream().filter(e -> STRING_NUM_CONSTANT
                .equals(e.getValue().getId())).count()).isEqualTo(0);

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getEmail()).isEqualTo("test@gmail.com");

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getOrganisationIdentifier()).isEqualTo("123");

    }

    @Test
    void shouldNotExceptionDefendent2DetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
            .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForDefendant2ToBeApplicant()).build());

        GeneralApplication result = helper.setApplicantAndRespondentDetailsIfExits(
            GeneralApplication.builder().build(),
            CaseData.builder().ccdCaseReference(1234L)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("123").build())
                                                   .orgPolicyCaseAssignedRole(CaseRole
                                                                                  .RESPONDENTSOLICITORONE
                                                                                  .getFormattedName())
                                                   .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder().organisationID("1234").build())
                                                   .orgPolicyCaseAssignedRole(CaseRole
                                                                                  .RESPONDENTSOLICITORONE
                                                                                  .getFormattedName())
                                                   .build())
                .respondentSolicitor2EmailAddress("test@gmail.com")
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                  .orgPolicyCaseAssignedRole(CaseRole
                                                                                 .APPLICANTSOLICITORONE
                                                                                 .getFormattedName())
                                                  .build()).build(),
            getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
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
                       .getEmail()).isEqualTo("test@gmail.com");

        assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                       .getOrganisationIdentifier()).isEqualTo("1234");

    }

    @Test
    void shouldReturnsTrueifClaimantIDMatchesWithLogin() {

        CaseData caseData = getTestCaseData(CaseData.builder().build(), true);

        assertThat(helper.isPCClaimantEmailIDSameAsLoginUser(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getUserDetails(APPLICANT_EMAIL_ID_CONSTANT)
        )).isEqualTo(true);
    }

    @Test
    void shouldReturnsfalseifClaimantIDMatchesWithLogin() {

        CaseData caseData = getTestCaseData(CaseData.builder().build(), true);

        assertThat(helper.isPCClaimantEmailIDSameAsLoginUser(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            getUserDetails(TEST_USER_EMAILID)
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
                .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec
                                                                 .builder()
                                                                 .id("1")
                                                                 .email(TEST_USER_EMAILID)
                                                                 .organisationIdentifier("Org1").build()))
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
