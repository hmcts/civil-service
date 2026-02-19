package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceHelper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
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
import static org.mockito.Mockito.lenient;
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

@ExtendWith(MockitoExtension.class)
public class InitiateGeneralApplicationServiceHelperTest {

    @InjectMocks
    private InitiateGeneralApplicationServiceHelper helper;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserService userService;

    @Mock
    private CrossAccessUserConfiguration crossAccessUserConfiguration;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    protected IdamClient idamClient;

    @Mock
    private GeneralAppFeesService feesService;

    private static final String TEST_USER_EMAILID = "test@gmail.com";
    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String RESPONDENT_EMAIL_ID_CONSTANT = "respondent@gmail.com";
    public static final String CL_LIP_USER_ID = "456";
    public static final String DEF_LIP_USER_ID = "123";

    public UserDetails getUserDetails(String id, String email) {
        return UserDetails.builder().id(id)
                .email(email)
                .build();
    }

    public List<CaseAssignmentUserRole> getCaseAssignmentApplicantUserRoles() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("3")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("4")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("5")
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("6").userId("6")
                    .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("7").userId("7")
                    .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsers() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForDefendant1ToBeApplicant() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("1")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForDefendant2ToBeApplicant() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("1")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(RESPONDENTSOLICITORTWO.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForLrVLipAppLr() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId(DEF_LIP_USER_ID)
                        .caseRole(DEFENDANT.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForLrVLipAppLip() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(DEFENDANT.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId(DEF_LIP_USER_ID)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForLipVLrAppLip() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(CLAIMANT.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForLipVLip() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(CL_LIP_USER_ID)
                        .caseRole(CLAIMANT.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId(DEF_LIP_USER_ID)
                        .caseRole(DEFENDANT.getFormattedName()).build()
        );
    }

    public List<CaseAssignmentUserRole> getCaseUsersForLipVLrAppLr() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(CLAIMANT.getFormattedName()).build()
        );
    }

    @BeforeEach
    public void setUp() {
        lenient().when(feesService.getFeeForGA(any(GeneralApplication.class), any())).thenReturn(new Fee());
    }

    @Test
    void shouldReturnsTwoRespondents() {
        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseAssignmentApplicantUserRoles()).build());

        GeneralApplication result = helper
                .setRespondentDetailsIfPresent(
                        new GeneralApplication(),
                        getTestCaseData(CaseDataBuilder.builder().build(), true, null),
                        getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                        feesService
                );

        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
        assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(2);

        ArrayList<String> userID = new ArrayList<>(Arrays.asList("3", "4"));

        userID.forEach(uid -> assertThat(result.getGeneralAppRespondentSolicitors()
                .stream().filter(e -> uid.equals(e.getValue().getId()))
                .count()).isEqualTo(1));

        assertThat(result.getGeneralAppRespondentSolicitors()
                .stream().filter(e -> STRING_NUM_CONSTANT
                        .equals(e.getValue().getId())).count()).isEqualTo(0);

    }

    @Test
    void shouldReturnsNoRespondents() {

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder()
                                                             .caseDataId("1")
                                                             .userId(STRING_NUM_CONSTANT)
                                                             .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build())
                        ).build());

        assertDoesNotThrow(() -> helper
                .setRespondentDetailsIfPresent(
                        new GeneralApplication(),
                        getTestCaseData(CaseDataBuilder.builder().build(), true, null),
                        getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                        feesService
                ));

    }

    @Test
    void shouldThrowExceptionIfApplicant1OrganisationPolicyIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> helper
                        .setRespondentDetailsIfPresent(
                                new GeneralApplication(),
                                CaseDataBuilder.builder().ccdCaseReference(1234L).build(),
                                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                                feesService
                        )
        );

    }

    @Test
    void shouldThrowExceptionIfRespondent1OrganisationPolicyIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> helper
                        .setRespondentDetailsIfPresent(
                                new GeneralApplication(),
                                CaseDataBuilder.builder().ccdCaseReference(1234L)
                                        .applicant1(createParty(COMPANY, "Applicant1"))
                                        .respondent2(createParty(COMPANY, "Respondent1"))
                                        .applicant1OrganisationPolicy(new OrganisationPolicy()).build(),
                                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                                feesService
                        )
        );

    }

    @Test
    void shouldThrowExceptionIfgetRespondent2IsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> helper
                        .setRespondentDetailsIfPresent(
                                new GeneralApplication(),
                                CaseDataBuilder.builder().ccdCaseReference(1234L)
                                        .respondent1OrganisationPolicy(new OrganisationPolicy())
                                        .addRespondent2(YesOrNo.YES)
                                        .applicant1(createParty(COMPANY, "Applicant1"))
                                        .respondent2(createParty(COMPANY, "Respondent1"))
                                        .applicant1OrganisationPolicy(new OrganisationPolicy()).build(),
                                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                                feesService
                        )
        );
    }

    @Test
    void shouldSetApplicantSolicitorOrgIDTo200() {

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseUsersForApplicantCheck()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                new GeneralApplication(),
                CaseDataBuilder.builder().build().toBuilder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(organisationPolicy(
                            "100",
                            RESPONDENTSOLICITORONE.getFormattedName()))
                        .respondent2OrganisationPolicy(organisationPolicy(
                            "101",
                            RESPONDENTSOLICITORTWO.getFormattedName()))
                        .applicantSolicitor1UserDetails(createIdamUserDetails(null, APPLICANT_EMAIL_ID_CONSTANT))
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(createParty(COMPANY, "Applicant1"))
                        .respondent2(createParty(COMPANY, "Respondent1"))
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(organisationPolicy(
                            "200",
                            APPLICANTSOLICITORONE.getFormattedName()))
                        .applicant2OrganisationPolicy(organisationPolicy(
                            "201",
                            APPLICANTSOLICITORONE.getFormattedName())).build(),
                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                feesService
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppApplnSolicitor().getOrganisationIdentifier()).isEqualTo("200");
    }

    @Test
    void shouldSetApplicantSolicitorOrgIDTo100() {

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseUsersForApplicantCheck()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                new GeneralApplication(),
                CaseDataBuilder.builder().build().toBuilder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(organisationPolicy(
                            "100",
                            RESPONDENTSOLICITORONE.getFormattedName()))
                        .respondent2OrganisationPolicy(organisationPolicy(
                            "101",
                            RESPONDENTSOLICITORTWO.getFormattedName()))
                        .applicantSolicitor1UserDetails(createIdamUserDetails(null, APPLICANT_EMAIL_ID_CONSTANT))
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(createParty(COMPANY, "Applicant1"))
                        .applicant2(createParty(COMPANY, "Applicant2"))
                        .respondent1(createParty(COMPANY, "Respondent1"))
                        .respondent2(createParty(COMPANY, "Respondent2"))
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(organisationPolicy(
                            "200",
                            APPLICANTSOLICITORONE.getFormattedName()))
                        .applicant2OrganisationPolicy(organisationPolicy(
                            "201",
                            APPLICANTSOLICITORONE.getFormattedName())).build(),
                getUserDetails("2", APPLICANT_EMAIL_ID_CONSTANT),
                feesService
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppApplnSolicitor().getOrganisationIdentifier()).isEqualTo("100");
    }

    @Test
    void shouldSetApplicantSolicitorOrgIDTo101() {

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseUsersForApplicantCheck()).build());

        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                new GeneralApplication(),
                CaseDataBuilder.builder().build().toBuilder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(organisationPolicy(
                            "100",
                            RESPONDENTSOLICITORONE.getFormattedName()))
                        .respondent2OrganisationPolicy(organisationPolicy(
                            "101",
                            RESPONDENTSOLICITORTWO.getFormattedName()))
                        .applicantSolicitor1UserDetails(createIdamUserDetails(null, APPLICANT_EMAIL_ID_CONSTANT))
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(createParty(COMPANY, "Applicant1"))
                        .applicant2(createParty(COMPANY, "Applicant2"))
                        .respondent1(createParty(COMPANY, "Respondent1"))
                        .respondent2(createParty(COMPANY, "Respondent2"))
                        .addRespondent2(YesOrNo.YES)
                        .applicant1OrganisationPolicy(organisationPolicy(
                            "200",
                            APPLICANTSOLICITORONE.getFormattedName()))
                        .applicant2OrganisationPolicy(organisationPolicy(
                            "201",
                            APPLICANTSOLICITORONE.getFormattedName())).build(),
                getUserDetails("3", APPLICANT_EMAIL_ID_CONSTANT),
                feesService
        );

        assertDoesNotThrow(() -> helper);
        assertThat(result).isNotNull();
        assertThat(result.getGeneralAppApplnSolicitor().getOrganisationIdentifier()).isEqualTo("101");
    }

    public List<CaseAssignmentUserRole> getCaseUsersForApplicantCheck() {
        return List.of(
                CaseAssignmentUserRole.builder().caseDataId("1").userId(STRING_NUM_CONSTANT)
                        .caseRole(APPLICANTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("2")
                        .caseRole(RESPONDENTSOLICITORONE.getFormattedName()).build(),
                CaseAssignmentUserRole.builder().caseDataId("1").userId("3")
                        .caseRole(RESPONDENTSOLICITORTWO.getFormattedName()).build()
        );
    }

    @Test
    void shouldNotExceptionClaimantDetialsSetToAppln() {

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseUsers()).build());
        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                new GeneralApplication(),
                CaseDataBuilder.builder().build().toBuilder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(organisationPolicy("345", RESPONDENTSOLICITORONE.getFormattedName()))
                        .applicantSolicitor1UserDetails(createIdamUserDetails(null, APPLICANT_EMAIL_ID_CONSTANT))
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicant1(createParty(COMPANY, "Applicant1"))
                        .respondent2(createParty(COMPANY, "Respondent1"))
                        .applicant1OrganisationPolicy(organisationPolicy("123", APPLICANTSOLICITORONE
                                        .getFormattedName()))
                        .generalAppEvidenceDocument(wrapElements(createDocument(STRING_CONSTANT)))
                        .build(),
                getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                feesService
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

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseUsersForDefendant1ToBeApplicant()).build());
        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                new GeneralApplication(),
                CaseDataBuilder.builder().build().toBuilder().ccdCaseReference(1234L)
                        .respondent1OrganisationPolicy(organisationPolicy("345", RESPONDENTSOLICITORONE
                                        .getFormattedName()))
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .applicantSolicitor1UserDetails(createIdamUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT))
                        .applicant1OrganisationPolicy(organisationPolicy("123", APPLICANTSOLICITORONE.getFormattedName()))
                        .applicant1(createParty(COMPANY, "Applicant1"))
                        .respondent1(createParty(COMPANY, "Respondent1"))
                        .generalAppEvidenceDocument(wrapElements(createDocument(STRING_CONSTANT)))
                        .build(),
                getUserDetails("1", RESPONDENT_EMAIL_ID_CONSTANT),
                feesService
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

        when(caseAssignmentApi.getUserRoles(any(), any(), any()))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                        .caseAssignmentUserRoles(getCaseUsersForDefendant2ToBeApplicant()).build());
        GeneralApplication result = helper.setRespondentDetailsIfPresent(
                new GeneralApplication(),
                CaseDataBuilder.builder().build().toBuilder().ccdCaseReference(1234L)
                        .addRespondent2(YesOrNo.NO)
                        .addApplicant2(YesOrNo.NO)
                        .respondent1OrganisationPolicy(organisationPolicy("123", RESPONDENTSOLICITORONE
                                        .getFormattedName()))
                        .respondent2OrganisationPolicy(organisationPolicy("1234", RESPONDENTSOLICITORTWO
                                        .getFormattedName()))
                        .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                        .respondentSolicitor2EmailAddress(TEST_USER_EMAILID)
                        .applicant1(createParty(COMPANY, "Applicant1"))
                        .applicant1(createParty(COMPANY, "Applicant2"))
                        .respondent1(createParty(COMPANY, "Respondent1"))
                        .respondent2(createParty(COMPANY, "Respondent2"))
                        .applicantSolicitor1UserDetails(createIdamUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT))
                        .applicant1OrganisationPolicy(organisationPolicy("6789", APPLICANTSOLICITORONE.getFormattedName()))
                        .generalAppEvidenceDocument(wrapElements(createDocument(STRING_CONSTANT)))
                        .build(),
                getUserDetails("2", TEST_USER_EMAILID),
                feesService
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

            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(organisationPolicy("200", APPLICANTSOLICITORONE.getFormattedName()))
                    .ccdCaseReference(12L)
                    .respondent1(createLipParty("party", "party@gmail.com", "party", Party.Type.INDIVIDUAL))
                    .defendantUserDetails(createIdamUserDetails(DEF_LIP_USER_ID, "partyemail@gmail.com"));
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLrVLipAppLr()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                            feesService
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

            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(organisationPolicy("200", APPLICANTSOLICITORONE.getFormattedName()))
                    .respondent1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName()))
                    .ccdCaseReference(12L)
                    .respondent1(createLipPartyWithName("party", "party@gmail.com", "Lip Lip", "party"))
                    .defendantUserDetails(createIdamUserDetails(DEF_LIP_USER_ID, "partyemail@gmail.com"));
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLrVLipAppLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, RESPONDENT_EMAIL_ID_CONSTANT),
                            feesService
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaApplicantLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(NO);
        }

        @Test
        void shouldReturnsRespondent1_Lip_Vs_Lr_Lip_Is_App() {

            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()))
                    .respondent1OrganisationPolicy(organisationPolicy("123", RESPONDENTSOLICITORONE
                                    .getFormattedName()))
                    .ccdCaseReference(12L)
                    .applicant1(createLipParty("party", "party@gmail.com", "party", Party.Type.INDIVIDUAL))
                    .claimantUserDetails(createIdamUserDetails(CL_LIP_USER_ID, "partyemail@gmail.com"))
                    .applicant1Represented(NO);
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLipVLrAppLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                            feesService
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaApplicantLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(YES);
        }

        @Test
        void shouldReturnsRespondent1_Lip_Vs_Lr_Lr_Is_App() {

            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()))
                    .respondent1OrganisationPolicy(organisationPolicy("123", RESPONDENTSOLICITORONE
                                    .getFormattedName()))
                    .ccdCaseReference(12L)
                    .respondent1(createLipParty("party", "party@gmail.com", "party", Party.Type.INDIVIDUAL))
                    .claimantUserDetails(createIdamUserDetails(CL_LIP_USER_ID, "partyemail@gmail.com"))
                    .defendantUserDetails(createIdamUserDetails("2", RESPONDENT_EMAIL_ID_CONSTANT))
                    .applicant1Represented(NO);
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLipVLrAppLr()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseDataBuilder.build(),
                            getUserDetails(STRING_NUM_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT),
                            feesService
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
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, null).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()))
                    .respondent1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName()))
                    .ccdCaseReference(12L)
                    .respondent1(createLipParty("party", "party@gmail.com", "defF", Party.Type.INDIVIDUAL))
                    .claimantUserDetails(createIdamUserDetails(CL_LIP_USER_ID, "partyemail@gmail.com"))
                    .defendantUserDetails(createIdamUserDetails(DEF_LIP_USER_ID, "partyemail@gmail.com"))
                    .applicant1Represented(NO);
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLipVLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseDataBuilder.build(),
                            getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT),
                            feesService
                    );

            assertThat(result).isNotNull();
            assertThat(result.getIsGaApplicantLip()).isEqualTo(YES);
            assertThat(result.getIsGaRespondentOneLip()).isEqualTo(YES);
            assertThat(result.getGeneralAppRespondentSolicitors()).isNotNull();
            assertThat(result.getGeneralAppRespondentSolicitors().size()).isEqualTo(1);
            assertThat(result.getGeneralAppRespondentSolicitors().get(0).getValue().getForename()).isEqualTo("defF");
            assertThat(result.getParentClaimantIsApplicant()).isEqualTo(YES);
        }

        @Test
        void shouldUrgency_Lip_Vs_Lip_at_10thDay() {
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, 10).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()))
                    .respondent1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName()))
                    .ccdCaseReference(12L)
                    .respondent1(createLipParty("party", "party@gmail.com", "defF", Party.Type.INDIVIDUAL))
                    .claimantUserDetails(createIdamUserDetails(CL_LIP_USER_ID, "partyemail@gmail.com"))
                    .defendantUserDetails(createIdamUserDetails(DEF_LIP_USER_ID, "partyemail@gmail.com"))
                    .applicant1Represented(NO);
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLipVLip()).build());
            CaseData caseData = caseDataBuilder.build();
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseData,
                            getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT),
                            feesService
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
        void should_Non_Urgency_Lip_Vs_Lip_At_25th() {
            LocalDate hearingDate = LocalDate.now().plusDays(25);
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder =
                getTestCaseData(CaseDataBuilder.builder().build(), false, 25).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                .addApplicant2(YesOrNo.NO)
                .applicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()))
                .respondent1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                                                                  .getFormattedName()))
                .ccdCaseReference(12L)
                .respondent1(createLipParty("party", "party@gmail.com", "defF", Party.Type.INDIVIDUAL))
                .claimantUserDetails(createIdamUserDetails(CL_LIP_USER_ID, "partyemail@gmail.com"))
                .defendantUserDetails(createIdamUserDetails(DEF_LIP_USER_ID, "partyemail@gmail.com"))
                .applicant1Represented(NO)
                .hearingDate(hearingDate);
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                .thenReturn(CaseAssignmentUserRolesResource.builder()
                                .caseAssignmentUserRoles(getCaseUsersForLipVLip()).build());

            CaseData caseData = caseDataBuilder.build();
            GeneralApplication result = helper
                .setRespondentDetailsIfPresent(
                    new GeneralApplication(),
                    caseData,
                    getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT),
                    feesService
                );

            assertThat(result).isNotNull();
            assertThat(result.getGeneralAppUrgencyRequirement()).isNotNull();
            assertThat(result.getGeneralAppUrgencyRequirement().getGeneralAppUrgency())
                .isEqualTo(NO);
            assertThat(result.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate()).isEqualTo(hearingDate);
        }

        @Test
        void shouldNotUrgency_Lip_Vs_Lip_at_11thDay() {
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = getTestCaseData(CaseDataBuilder.builder().build(), false, 11).toBuilder();
            caseDataBuilder.addRespondent2(YesOrNo.NO)
                    .addApplicant2(YesOrNo.NO)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()))
                    .respondent1OrganisationPolicy(new OrganisationPolicy().setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE
                                    .getFormattedName()))
                    .ccdCaseReference(12L)
                    .respondent1(createLipParty("party", "party@gmail.com", "defF", Party.Type.INDIVIDUAL))
                    .claimantUserDetails(createIdamUserDetails(CL_LIP_USER_ID, "partyemail@gmail.com"))
                    .defendantUserDetails(createIdamUserDetails(DEF_LIP_USER_ID, "partyemail@gmail.com"))
                    .applicant1Represented(NO);
            when(caseAssignmentApi.getUserRoles(any(), any(), eq(List.of("12"))))
                    .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(getCaseUsersForLipVLip()).build());
            GeneralApplication result = helper
                    .setRespondentDetailsIfPresent(
                            new GeneralApplication(),
                            caseDataBuilder.build(),
                            getUserDetails(CL_LIP_USER_ID, APPLICANT_EMAIL_ID_CONSTANT),
                            feesService
                    );

            assertThat(result).isNotNull();
            assertThat(result.getGeneralAppUrgencyRequirement()).isNotNull();
        }

    }

    public CaseData getTestCaseData(CaseData caseData, boolean respondentExits, Integer hearingDateOffset) {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = new GASolicitorDetailsGAspec().setId("1")
            .setEmail("test@gmail.com").setOrganisationIdentifier("org2");

        GASolicitorDetailsGAspec respondent2 = new GASolicitorDetailsGAspec().setId("2")
            .setEmail("test@gmail.com").setOrganisationIdentifier("org2");

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        if (respondentExits) {
            return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                                    .types(singletonList(EXTEND_TIME))
                                    .build())
                .applicantSolicitor1UserDetails(createIdamUserDetails(STRING_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT))
                .generalAppRespondentSolicitors(respondentSols)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec
                                              .builder()
                                              .id("1")
                                              .email(TEST_USER_EMAILID)
                                              .organisationIdentifier("Org1").build())
                .applicant1(createParty(COMPANY, "Applicant1"))
                .respondent2(createParty(COMPANY, "Respondent1"))
                .applicant1OrganisationPolicy(organisationPolicy(STRING_CONSTANT, APPLICANTSOLICITORONE.getFormattedName()).setOrgPolicyReference(STRING_CONSTANT))
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(organisationPolicy(STRING_CONSTANT, RESPONDENTSOLICITORONE.getFormattedName()).setOrgPolicyReference(STRING_CONSTANT))
                .respondent2OrganisationPolicy(organisationPolicy(STRING_CONSTANT, null).setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .hearingDate(Objects.nonNull(hearingDateOffset) ? LocalDate.now().plusDays(hearingDateOffset) : null)
                .build();
        } else {
            return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                                    .types(singletonList(EXTEND_TIME))
                                    .build())
                .applicantSolicitor1UserDetails(createIdamUserDetails(STRING_CONSTANT, APPLICANT_EMAIL_ID_CONSTANT))
                .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec
                                                                 .builder()
                                                                 .id("1")
                                                                 .email(TEST_USER_EMAILID)
                                                                 .organisationIdentifier("Org1").build()))
                .applicant1(createParty(COMPANY, "Applicant1"))
                .respondent2(createParty(COMPANY, "Respondent1"))
                .applicant1OrganisationPolicy(organisationPolicy(STRING_CONSTANT, null).setOrgPolicyReference(STRING_CONSTANT))
                .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
                .respondent1OrganisationPolicy(organisationPolicy(STRING_CONSTANT, null).setOrgPolicyReference(STRING_CONSTANT))
                .respondent2OrganisationPolicy(organisationPolicy(STRING_CONSTANT, null).setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .hearingDate(Objects.nonNull(hearingDateOffset) ? LocalDate.now().plusDays(hearingDateOffset) : null)
                .build();
        }
    }

    public GeneralApplication getGeneralApplication() {
        GeneralApplication generalApplication = new GeneralApplication();
        GAApplicationType gaApplicationType = new GAApplicationType();
        gaApplicationType.setTypes(singletonList(SUMMARY_JUDGEMENT));
        generalApplication.setGeneralAppType(gaApplicationType);
        return generalApplication;
    }

    private Document createDocument(String url) {
        Document document = new Document();
        document.setDocumentUrl(url);
        return document;
    }

    private Party createParty(Party.Type type, String companyName) {
        Party party = new Party();
        party.setType(type);
        party.setCompanyName(companyName);
        return party;
    }

    private Party createLipParty(String partyId, String email, String firstName, Party.Type type) {
        Party party = new Party();
        party.setPartyID(partyId);
        party.setPartyEmail(email);
        party.setIndividualFirstName(firstName);
        party.setType(type);
        return party;
    }

    private Party createLipPartyWithName(String partyId, String email, String partyName, String firstName) {
        Party party = new Party();
        party.setPartyID(partyId);
        party.setPartyEmail(email);
        party.setPartyName(partyName);
        party.setIndividualFirstName(firstName);
        party.setType(Party.Type.INDIVIDUAL);
        return party;
    }

    private IdamUserDetails createIdamUserDetails(String id, String email) {
        IdamUserDetails idamUserDetails = new IdamUserDetails();
        idamUserDetails.setId(id);
        idamUserDetails.setEmail(email);
        return idamUserDetails;
    }

    private OrganisationPolicy organisationPolicy(String organisationId, String caseRole) {
        OrganisationPolicy policy = new OrganisationPolicy();
        if (organisationId != null) {
            policy.setOrganisation(new Organisation().setOrganisationID(organisationId));
        }
        return policy.setOrgPolicyCaseAssignedRole(caseRole);
    }
}
