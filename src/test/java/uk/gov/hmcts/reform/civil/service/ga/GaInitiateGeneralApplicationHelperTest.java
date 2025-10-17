package uk.gov.hmcts.reform.civil.service.ga;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GaInitiateGeneralApplicationHelperTest {

    private static final String CASE_ID = "1234567890123456";

    @Mock
    private CaseAssignmentApi caseAssignmentApi;
    @Mock
    private UserRoleCaching userRoleCaching;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @Mock
    private CrossAccessUserConfiguration crossAccessUserConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private GaInitiateGeneralApplicationHelper helper;

    @BeforeEach
    void init() {
        lenient().when(userService.getAccessToken(any(), any())).thenReturn("caa");
        lenient().when(authTokenGenerator.generate()).thenReturn("s2s");
        lenient().when(crossAccessUserConfiguration.getUserName()).thenReturn("user");
        lenient().when(crossAccessUserConfiguration.getPassword()).thenReturn("pass");
    }

    @Test
    void respondentAssigned_shouldReturnTrueWhenLipCaseHasRole() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(Long.valueOf(CASE_ID))
            .withIsGaApplicantLip(YesOrNo.YES)
            .build();

        CaseAssignmentUserRole role = CaseAssignmentUserRole.builder()
            .caseRole("[DEFENDANT]")
            .userId("resp")
            .build();
        CaseAssignmentUserRolesResource resource = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(role))
            .build();

        when(caseAssignmentApi.getUserRoles(any(), any(), any())).thenReturn(resource);
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);

        assertThat(helper.respondentAssigned(gaCaseData)).isTrue();
    }

    @Test
    void isGaApplicantSameAsParentCaseClaimant_shouldReturnTrueWhenRoleMatches() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(Long.valueOf(CASE_ID))
            .build()
            .toBuilder()
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole("[APPLICANTSOLICITORONE]")
                .build())
            .build();

        when(userRoleCaching.getUserRoles("auth", CASE_ID)).thenReturn(List.of("[APPLICANTSOLICITORONE]"));
        assertThat(helper.isGaApplicantSameAsParentCaseClaimant(gaCaseData, "auth")).isTrue();
    }

    @Test
    void isGaApplicantSameAsParentCaseClaimant_shouldReturnFalseWhenRolesEmpty() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(Long.valueOf(CASE_ID))
            .build();

        when(userRoleCaching.getUserRoles("auth", CASE_ID)).thenReturn(List.of());
        assertThat(helper.isGaApplicantSameAsParentCaseClaimant(gaCaseData, "auth")).isFalse();
    }

    @Test
    void ensureDefaults_shouldPopulateMissingStructures() {
        GeneralApplicationCaseData input = GeneralApplicationCaseDataBuilder.builder()
            .build()
            .toBuilder()
            .generalAppPBADetails(null)
            .generalAppStatementOfTruth(null)
            .generalAppInformOtherParty(null)
            .generalAppUrgencyRequirement(null)
            .generalAppRespondentAgreement(null)
            .applicantPartyName(null)
            .claimant1PartyName(null)
            .defendant1PartyName(null)
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Applicant One").build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Defendant One").build())
            .applicant2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Applicant Two").build())
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("Defendant Two").build())
            .build();

        GeneralApplicationCaseData result = helper.ensureDefaults(input);

        assertThat(result.getGeneralAppPBADetails()).isNotNull();
        assertThat(result.getGeneralAppStatementOfTruth()).isNotNull();
        assertThat(result.getGeneralAppInformOtherParty()).isNotNull();
        assertThat(result.getGeneralAppUrgencyRequirement()).isNotNull();
        assertThat(result.getGeneralAppRespondentAgreement()).isNotNull();
        assertThat(result.getApplicantPartyName()).isNotNull();
        assertThat(result.getClaimant1PartyName()).isNotNull();
        assertThat(result.getDefendant1PartyName()).isNotNull();
    }

    @Test
    void buildApplications_shouldCreateElementWhenEmpty() {
        GeneralApplicationCaseData gaCaseData = helper.ensureDefaults(GeneralApplicationCaseDataBuilder.builder().build());

        var applications = helper.buildApplications(gaCaseData);

        assertThat(applications).hasSize(1);
        GeneralApplication application = applications.get(0).getValue();
        assertThat(application.getGeneralAppType()).isEqualTo(gaCaseData.getGeneralAppType());
        assertThat(application.getApplicantPartyName()).isEqualTo(gaCaseData.getApplicantPartyName());
    }
}
