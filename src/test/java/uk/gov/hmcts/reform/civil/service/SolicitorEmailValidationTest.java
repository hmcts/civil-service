package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {
    SolicitorEmailValidation.class,
    GaForLipService.class,
    JacksonAutoConfiguration.class,
})
public class SolicitorEmailValidationTest {

    @Autowired
    private SolicitorEmailValidation solicitorEmailValidation;

    @MockBean
    private FeatureToggleService featureToggleService;

    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String CLAIM_CL_LIP_EMAIL = "hmcts.civil.cl@gmail.com";
    private static final String CLAIM_DEF_LIP_EMAIL = "hmcts.civil.def@gmail.com";
    @Autowired
    private GaForLipService gaForLipService;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
    }

    @Test
    void shouldMatchIfThereIsNoChangeInGAApplicantEmailAndCivilApplicantEmail_1V1() {

        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                        getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsNoChangeInGARespondentEmailAndCivilRespondentEmail_1V1() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                        getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchRespondentOneEmailId_LRvsLIP() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, CLAIM_DEF_LIP_EMAIL),
                        getGaCaseData(NO, YES, NO, YES));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_DEF_LIP_EMAIL);
    }

    @Test
    void shouldMatchApplicantEmailId_LIPvsLR() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, null),
                        getGaCaseData(NO, YES, YES, NO)
                );

        // Print everything in one line for debugging
        System.out.println("DEBUG -> " + caseData);

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(CLAIM_CL_LIP_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchRespondentOneEmailId_LIPvsLR_DEF() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, null),
                        getGaCaseData(NO, NO, NO, YES));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_CL_LIP_EMAIL);
    }

    @Test
    void shouldMatchALlEmailId_LIPvsLIP() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, CLAIM_DEF_LIP_EMAIL),
                        getGaCaseData(NO, YES, YES, YES));
        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(CLAIM_CL_LIP_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_DEF_LIP_EMAIL);
    }

    @Test
    void shouldMatchALlEmailId_LIPvsLIP_DEF() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, CLAIM_DEF_LIP_EMAIL),
                        getGaCaseData(NO, NO, YES, YES));
        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(CLAIM_DEF_LIP_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_CL_LIP_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsNoChangeInGARespondentEmailAndCivilRespondentEmail_1V1_LIP() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, DUMMY_EMAIL),
                        getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGAApplicantEmailAndCivilApplicantEmail_1V1() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData("civilApplicant@gmail.com", DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                        getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("civilApplicant@gmail.com");
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondentEmailAndCivilRespondentEmail_1V1() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData(DUMMY_EMAIL,
                                "civilrespondent1@gmail.com", DUMMY_EMAIL, null, null
                        ), getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(DUMMY_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo("civilrespondent1@gmail.com");
    }

    @Test
    void shouldMatchIfThereIsChangeInGAApplicantEmailAndCivilApplicantEmail_2V1() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData("civilApplicant@gmail.com", DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                        getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("civilApplicant@gmail.com");
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondentEmailAndCivilRespondentEmail_2V1() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData(DUMMY_EMAIL,
                                "civilrespondent1@gmail.com", DUMMY_EMAIL, null, null
                        ), getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(DUMMY_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo("civilrespondent1@gmail.com");
    }

    @Test
    void shouldMatchIfThereIsChangeInGAApplicantEmailAndCivilApplicantEmail_1V2() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData("civilApplicant@gmail.com", DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                        getGaCaseData(YES, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("civilApplicant@gmail.com");
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondent1EmailAndCivilRespondentEmail_1V2() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData(DUMMY_EMAIL,
                                "civilrespondent1@gmail.com", DUMMY_EMAIL, null, null
                        ), getGaCaseData(YES, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(DUMMY_EMAIL);
        assertThat(checkIfThereIsMatchOrgIdAndEmailId(caseData.getGeneralAppRespondentSolicitors(),
                "2", "civilrespondent1@gmail.com"
        )).isEqualTo(true);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondent2EmailAndCivilRespondentEmail_1V2() {
        CaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(
                        getCivilCaseData(DUMMY_EMAIL,
                                DUMMY_EMAIL, "civilrespondent2@gmail.com", null, null
                        ), getGaCaseData(YES, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(DUMMY_EMAIL);
        assertThat(checkIfThereIsMatchOrgIdAndEmailId(caseData.getGeneralAppRespondentSolicitors(),
                "3", "civilrespondent2@gmail.com"
        )).isEqualTo(true);
    }

    public boolean checkIfThereIsMatchOrgIdAndEmailId(List<Element<GASolicitorDetailsGAspec>>
                                                              generalAppRespondentSolicitors, String orgID, String email) {

        return generalAppRespondentSolicitors.stream().anyMatch(rs ->
                rs.getValue().getOrganisationIdentifier()
                        .equals(orgID) && rs.getValue().getEmail()
                        .equals(email));
    }

    private CaseData getCivilCaseData(String applicantEmail,
                                      String respondent1SolEmail,
                                      String respondent2SolEmail,
                                      String lipClEmail,
                                      String lipDefEmail) {

        CaseData caseData = new CaseDataBuilder()
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id").forename("GAApplnSolicitor")
                        .email(DUMMY_EMAIL).organisationIdentifier("1").build())
                .respondentSolicitor1EmailAddress(respondent1SolEmail)
                .respondentSolicitor2EmailAddress(respondent2SolEmail)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .id("123")
                        .email(applicantEmail)
                        .build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("1").build())
                        .build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("2").build())
                        .build())
                .respondent2SameLegalRepresentative(NO)
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("3").build())
                        .build())
                .build();
        var builder = caseData.toBuilder();
        if (Objects.nonNull(lipClEmail)) {
            builder.claimantUserDetails(IdamUserDetails.builder()
                    .id("123")
                    .email(lipClEmail)
                    .build());
        }
        if (Objects.nonNull(lipDefEmail)) {
            builder.defendantUserDetails(IdamUserDetails.builder()
                    .id("1")
                    .email(lipDefEmail)
                    .build());
        }
        return builder.build();
    }

    private CaseData getGaCaseData(YesOrNo isMultiParty, YesOrNo parentClaimantIsApplicant, YesOrNo isLipApp, YesOrNo isLipResp) {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).forename("Respondent One").organisationIdentifier("2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).forename("Resondent Two").organisationIdentifier("3").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        CaseData caseData =  new CaseDataBuilder()
                .isMultiParty(isMultiParty)
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id").forename("Applicant One")
                        .email(DUMMY_EMAIL).organisationIdentifier("1").build())
                .generalAppRespondentSolicitors(respondentSols)
                .parentClaimantIsApplicant(parentClaimantIsApplicant)
                .gaRespondentOrderAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .build();
        var builder = caseData.toBuilder();
        if (isLipApp.equals(YES)) {
            builder.isGaApplicantLip(YES);
        }
        if (isLipResp.equals(YES)) {
            builder.isGaRespondentOneLip(YES);
        }
        return builder.build();
    }
}
