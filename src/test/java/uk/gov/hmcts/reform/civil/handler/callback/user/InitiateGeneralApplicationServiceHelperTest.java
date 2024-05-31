package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
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
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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
    public static final String CL_LIP_USER_ID = "456";
    public static final String DEF_LIP_USER_ID = "123";

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

    @MockBean
    protected UserRoleCaching userRoleCaching;

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

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(List.of(
                                CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build())).build());
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

    public List<CaseAssignedUserRole> getCaseUsersForLrVLipAppLr() {
        return List.of(
                CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId(DEF_LIP_USER_ID)
                        .caseRole(DEFENDANT.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForLrVLipAppLip() {
        return List.of(
                CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(DEFENDANT.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId(DEF_LIP_USER_ID)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForLipVLrAppLip() {
        return List.of(
                CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(CLAIMANT.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForLipVLip() {
        return List.of(
                CaseAssignedUserRole.builder().caseDataId("1").userId(CL_LIP_USER_ID)
                        .caseRole(CLAIMANT.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId(DEF_LIP_USER_ID)
                        .caseRole(DEFENDANT.getFormattedName()).build()
        );
    }

    public List<CaseAssignedUserRole> getCaseUsersForLipVLrAppLr() {
        return List.of(
                CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(CLAIMANT.getFormattedName()).build()
        );
    }

    @Test
    void shouldReturnsFourRespondents() {

        GeneralApplication result = helper
                .setRespondentDetailsIfPresent(
                        GeneralApplication.builder().build(),
                        getTestCaseData(CaseData.builder().build(), true, null),
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
                        getTestCaseData(CaseData.builder().build(), true, null),
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
    void shouldSetApplicantSolicitorOrgIDTo200() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(getCaseUsersForApplicantCheck()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                GeneralApplication.builder().build(),
                CaseData.builder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("100").build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                                .build())
                        .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("101").build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName())
                                .build())
                        .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(APPLICANT_EMAIL_ID_CONSTANT).build())
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                        .respondent2(Party.builder().type(COMPANY).companyName("Respondent1").build())
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("200").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build())
                        .applicant2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("201").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build()).build(),
                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppApplnSolicitor().getOrganisationIdentifier()).isEqualTo("200");
    }

    @Test
    void shouldSetApplicantSolicitorOrgIDTo100() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(getCaseUsersForApplicantCheck()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                GeneralApplication.builder().build(),
                CaseData.builder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("100").build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                                .build())
                        .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("101").build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName())
                                .build())
                        .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(APPLICANT_EMAIL_ID_CONSTANT).build())
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                        .applicant2(Party.builder().type(COMPANY).companyName("Applicant2").build())
                        .respondent1(Party.builder().type(COMPANY).companyName("Respondent1").build())
                        .respondent2(Party.builder().type(COMPANY).companyName("Respondent2").build())
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("200").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build())
                        .applicant2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("201").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build()).build(),
                getUserDetails("2", APPLICANT_EMAIL_ID_CONSTANT)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppApplnSolicitor().getOrganisationIdentifier()).isEqualTo("100");
    }

    @Test
    void shouldSetApplicantSolicitorOrgIDTo101() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(getCaseUsersForApplicantCheck()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                GeneralApplication.builder().build(),
                CaseData.builder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("100").build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                                .build())
                        .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("101").build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName())
                                .build())
                        .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(APPLICANT_EMAIL_ID_CONSTANT).build())
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                        .applicant2(Party.builder().type(COMPANY).companyName("Applicant2").build())
                        .respondent1(Party.builder().type(COMPANY).companyName("Respondent1").build())
                        .respondent2(Party.builder().type(COMPANY).companyName("Respondent2").build())
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("200").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build())
                        .applicant2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("201").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build()).build(),
                getUserDetails("3", APPLICANT_EMAIL_ID_CONSTANT)
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppApplnSolicitor().getOrganisationIdentifier()).isEqualTo("101");
    }

    public List<CaseAssignedUserRole> getCaseUsersForApplicantCheck() {
        return List.of(
                CaseAssignedUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignedUserRole.builder().caseDataId("1").userId("3")
                        .caseRole(RESPONDENTSOLICITORTWO.getFormattedName()).build()
        );
    }

    @Test
    void shouldNotExceptionClaimantDetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(getCaseUsers()).build());
        CaseData.CaseDataBuilder cdBuilder = CaseData.builder();
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
                                .build())
                        .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
                        .build(),
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
        assertThat(result.getGaApplicantDisplayName()).isEqualTo("Applicant1 - Claimant");
        assertThat(result.getLitigiousPartyID()).isEqualTo("001");
    }

    @Test
    void shouldNotExceptionDefendent1DetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(getCaseUsersForDefendant1ToBeApplicant()).build());
        CaseData.CaseDataBuilder cdBuilder = CaseData.builder();
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
                        .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
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
        assertThat(result.getGaApplicantDisplayName()).isEqualTo("Respondent1 - Defendant");
        assertThat(result.getLitigiousPartyID()).isEqualTo("002");
    }

    @Test
    void shouldNotExceptionDefendent2DetialsSetToAppln() {

        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignedUserRolesResource.builder()
                        .caseAssignedUserRoles(getCaseUsersForDefendant2ToBeApplicant()).build());
        CaseData.CaseDataBuilder cdBuilder = CaseData.builder();
        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                GeneralApplication.builder().build(),
                CaseData.builder().ccdCaseReference(1234L)
                        .addRespondent2(YesOrNo.NO)
                        .addApplicant2(YesOrNo.NO)
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
                        .applicant1(Party.builder().type(COMPANY).companyName("Applicant2").build())
                        .respondent1(Party.builder().type(COMPANY).companyName("Respondent1").build())
                        .respondent2(Party.builder().type(COMPANY).companyName("Respondent2").build())
                        .applicantSolicitor1UserDetails(IdamUserDetails.builder().id(STRING_NUM_CONSTANT)
                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("6789").build())
                                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                                .build())
                        .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
                        .build(),
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
        assertThat(result.getLitigiousPartyID()).isEqualTo("003");
    }

    /*
     * Scenario 1V1 LR vs LIP
     * Civil Claim Claimant Initiates GA
     * */
    @Nested
    class LipTest {

        @Test
        void shouldReturnsRespondent1_Lr_Vs_Lip_Lr_Is_App() {

            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .organisation(Organisation.builder().organisationID("200").build())
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .respondent1(Party.builder()
                            .partyID("party")
                            .partyEmail("party@gmail.com")
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("party").build())
                    .defendantUserDetails(IdamUserDetails.builder().id(DEF_LIP_USER_ID).email("partyemail@gmail.com").build());
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLrVLipAppLr()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaRespondentOneLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);

            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue().getOrganisationIdentifier()).isNull();
            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue().getId())
                    .isEqualTo("123");
            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue().getEmail())
                    .isEqualTo("partyemail@gmail.com");
            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue().getForename()).isEqualTo("party");
            assertFalse(result.getGeneralAppRespondentSolicitors().get(0).getValue().getSurname().isPresent());

            assertThat(result.getGeneralAppRespondentSolicitors()
                    .stream().filter(e -> STRING_NUM_CONSTANT
                            .equals(e.getValue().getId())).count()).isEqualTo(0);
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(YES);
        }

        @Test
        void shouldReturnsApp_Lr_Vs_Lip_Lip_Is_App() {

            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .organisation(Organisation.builder().organisationID("200").build())
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .respondent1(Party.builder()
                            .partyID("party")
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Lip Lip")
                            .partyEmail("party@gmail.com")
                            .individualFirstName("party").build())
                    .defendantUserDetails(IdamUserDetails.builder().id(DEF_LIP_USER_ID).email("partyemail@gmail.com").build());
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLrVLipAppLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, RESPONDENT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaApplicantLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(NO);
        }

        @Test
        void shouldReturnsRespondent1_Lip_Vs_Lr_Lip_Is_App() {

            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                            .organisation(Organisation.builder().organisationID("123").build())
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .applicant1(Party.builder()
                            .partyID("party")
                            .partyEmail("party@gmail.com")
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("party").build())
                    .claimantUserDetails(IdamUserDetails.builder().id(CL_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .applicant1Represented(NO);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLipVLrAppLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaApplicantLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(YES);
        }

        @Test
        void shouldReturnsRespondent1_Lip_Vs_Lr_Lr_Is_App() {

            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                            .organisation(Organisation.builder().organisationID("123").build())
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .respondent1(Party.builder()
                            .partyID("party")
                            .partyEmail("party@gmail.com")
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("party").build())
                    .claimantUserDetails(IdamUserDetails.builder().id(CL_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .defendantUserDetails(IdamUserDetails.builder().id("2").email(RESPONDENT_EMAIL_ID_CONSTANT).build())
                    .applicant1Represented(NO);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLipVLrAppLr()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaRespondentOneLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue()
                    .getEmail()).isEqualTo(RESPONDENT_EMAIL_ID_CONSTANT);
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(NO);
        }

        @Test
        void shouldWork_Lip_Vs_Lip() {
            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .respondent1(Party.builder()
                            .partyID("party")
                            .partyEmail("party@gmail.com")
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("defF").build())
                    .claimantUserDetails(IdamUserDetails.builder().id(CL_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .defendantUserDetails(IdamUserDetails.builder().id(DEF_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .applicant1Represented(NO);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLipVLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseDataBuilder.build(),
                            getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaApplicantLip()).isEqualTo(YES);
            assertThat(result.getIsGaRespondentOneLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue().getForename()).isEqualTo("defF");
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(YES);
            assertThat(result.getGeneralAppUrgencyRequirement()).isNull();
        }

        @Test
        void shouldUrgency_Lip_Vs_Lip_at_10thDay() {
            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, 10).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .respondent1(Party.builder()
                            .partyID("party")
                            .partyEmail("party@gmail.com")
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("defF").build())
                    .claimantUserDetails(IdamUserDetails.builder().id(CL_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .defendantUserDetails(IdamUserDetails.builder().id(DEF_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .applicant1Represented(NO);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLipVLip()).build());
            CaseData caseData = caseDataBuilder.build();
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseData,
                            getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getGeneralAppUrgencyRequirement()).isNotNull();
            assertThat(result.getGeneralAppUrgencyRequirement().getReasonsForUrgency())
                    .isEqualTo("There is a hearing on the main case within 10 days");
            assertThat(result.getGeneralAppUrgencyRequirement().getGeneralAppUrgency())
                    .isEqualTo(YES);
            assertThat(result.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
                    .isEqualTo(caseData.getHearingDate());
        }

        @Test
        void shouldNotUrgency_Lip_Vs_Lip_at_11thDay() {
            CaseData.CaseDataBuilder caseDataBuilder = getTestCaseData(CaseData.builder().build(), false, 11).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                            .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName())
                            .build())
                    .ccdCaseReference(12L)
                    .respondent1(Party.builder()
                            .partyID("party")
                            .partyEmail("party@gmail.com")
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("defF").build())
                    .claimantUserDetails(IdamUserDetails.builder().id(CL_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .defendantUserDetails(IdamUserDetails.builder().id(DEF_LIP_USER_ID).email("partyemail@gmail.com").build())
                    .applicant1Represented(NO);
            when(caseAccessDataStoreApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignedUserRolesResource.builder()
                            .caseAssignedUserRoles(getCaseUsersForLipVLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            GeneralApplication.builder().build(),
                            caseDataBuilder.build(),
                            getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT)
                    );

            assertThat(result).isNotNull();
            assertThat(result.getGeneralAppUrgencyRequirement()).isNull();
        }

    }

    public CaseData getTestCaseData(CaseData caseData, boolean respondentExits, Integer hearingDateOffset) {

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
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .hearingDate(Objects.nonNull(hearingDateOffset) ? LocalDate.now().plusDays(hearingDateOffset) : null)
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
                                                  .organisation(Organisation.builder()
                                                                    .organisationID(STRING_CONSTANT).build())
                                                  .orgPolicyReference(STRING_CONSTANT).build())
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(Organisation.builder()
                                                                     .organisationID(STRING_CONSTANT).build())
                                                   .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .hearingDate(Objects.nonNull(hearingDateOffset) ? LocalDate.now().plusDays(hearingDateOffset) : null)
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
