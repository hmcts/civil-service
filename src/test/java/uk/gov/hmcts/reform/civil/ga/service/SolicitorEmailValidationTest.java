package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class SolicitorEmailValidationTest {

    @InjectMocks
    private SolicitorEmailValidation solicitorEmailValidation;

    @Spy
    private GaForLipService gaForLipService;

    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String CLAIM_CL_LIP_EMAIL = "hmcts.civil.cl@gmail.com";
    private static final String CLAIM_DEF_LIP_EMAIL = "hmcts.civil.def@gmail.com";

    @Test
    void shouldMatchIfThereIsNoChangeInGAApplicantEmailAndCivilApplicantEmail_1V1() {

        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                    getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsNoChangeInGARespondentEmailAndCivilRespondentEmail_1V1() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                    getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
            .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchRespondentOneEmailId_LRvsLIP() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, CLAIM_DEF_LIP_EMAIL),
                    getGaCaseData(NO, YES, NO, YES));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
            .isEqualTo(CLAIM_DEF_LIP_EMAIL);
    }

    @Test
    void shouldMatchApplicantEmailId_LIPvsLR() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, null),
                        getGaCaseData(NO, YES, YES, NO));
        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(CLAIM_CL_LIP_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchRespondentOneEmailId_LIPvsLR_DEF() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, null),
                        getGaCaseData(NO, NO, NO, YES));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_CL_LIP_EMAIL);
    }

    @Test
    void shouldMatchALlEmailId_LIPvsLIP() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, CLAIM_DEF_LIP_EMAIL),
                        getGaCaseData(NO, YES, YES, YES));
        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(CLAIM_CL_LIP_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_DEF_LIP_EMAIL);
    }

    @Test
    void shouldMatchALlEmailId_LIPvsLIP_DEF() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
                .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, CLAIM_CL_LIP_EMAIL, CLAIM_DEF_LIP_EMAIL),
                        getGaCaseData(NO, NO, YES, YES));
        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo(CLAIM_DEF_LIP_EMAIL);
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
                .isEqualTo(CLAIM_CL_LIP_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsNoChangeInGARespondentEmailAndCivilRespondentEmail_1V1_LIP() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(getCivilCaseData(DUMMY_EMAIL, DUMMY_EMAIL, DUMMY_EMAIL, null, DUMMY_EMAIL),
                    getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
            .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGAApplicantEmailAndCivilApplicantEmail_1V1() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(
                getCivilCaseData("civilApplicant@gmail.com", DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                    getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("civilApplicant@gmail.com");
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
            .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondentEmailAndCivilRespondentEmail_1V1() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
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
        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(
                getCivilCaseData("civilApplicant@gmail.com", DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                    getGaCaseData(NO, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("civilApplicant@gmail.com");
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
            .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondentEmailAndCivilRespondentEmail_2V1() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
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
        GeneralApplicationCaseData caseData = solicitorEmailValidation
            .validateSolicitorEmail(
                getCivilCaseData("civilApplicant@gmail.com", DUMMY_EMAIL, DUMMY_EMAIL, null, null),
                    getGaCaseData(YES, YES, NO, NO));

        assertThat(caseData.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("civilApplicant@gmail.com");
        assertThat(caseData.getGeneralAppRespondentSolicitors().stream().findFirst().get().getValue().getEmail())
            .isEqualTo(DUMMY_EMAIL);
    }

    @Test
    void shouldMatchIfThereIsChangeInGARespondent1EmailAndCivilRespondentEmail_1V2() {
        GeneralApplicationCaseData caseData = solicitorEmailValidation
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
        GeneralApplicationCaseData caseData = solicitorEmailValidation
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

    private GeneralApplicationCaseData getCivilCaseData(String applicantEmail,
                                      String respondent1SolEmail,
                                      String respondent2SolEmail,
                                      String lipClEmail,
                                      String lipDefEmail) {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder()
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id").forename("GAApplnSolicitor")
                                          .email(DUMMY_EMAIL).organisationIdentifier("1").build())
            .respondentSolicitor1EmailAddress(respondent1SolEmail)
            .respondentSolicitor2EmailAddress(respondent2SolEmail)
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId("123")
                                                .setEmail(applicantEmail))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation().setOrganisationID("1")))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation().setOrganisationID("2")))
            .respondent2SameLegalRepresentative(NO)
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation().setOrganisationID("3")))
            .build();
        var builder = caseData.copy();
        if (Objects.nonNull(lipClEmail)) {
            builder.claimantUserDetails(new IdamUserDetails()
                    .setId("123")
                    .setEmail(lipClEmail));
        }
        if (Objects.nonNull(lipDefEmail)) {
            builder.defendantUserDetails(new IdamUserDetails()
                    .setId("1")
                    .setEmail(lipDefEmail));
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getGaCaseData(YesOrNo isMultiParty, YesOrNo parentClaimantIsApplicant, YesOrNo isLipApp, YesOrNo isLipResp) {

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).forename("Respondent One").organisationIdentifier("2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).forename("Resondent Two").organisationIdentifier("3").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        GeneralApplicationCaseData caseData =  new GeneralApplicationCaseDataBuilder()
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
        var builder = caseData.copy();
        if (isLipApp.equals(YES)) {
            builder.isGaApplicantLip(YES);
        }
        if (isLipResp.equals(YES)) {
            builder.isGaRespondentOneLip(YES);
        }
        return builder.build();
    }
}
