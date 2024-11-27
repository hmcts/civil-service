package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.mediation.helpers.PartyDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.mediation.helpers.RepresentedLitigantPopulator;
import uk.gov.hmcts.reform.civil.service.mediation.helpers.UnrepresentedLitigantPopulator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.ENGLISH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;

@SpringBootTest(classes = {
    MediationJsonService.class,
    RepresentedLitigantPopulator.class,
    UnrepresentedLitigantPopulator.class,
    PartyDetailsPopulator.class
})
public class MediationJsonServiceTest {

    private static final String PAPER_RESPONSE = "N";
    private static final String APPLICANT_LR_NAME = "Applicant LR Org";
    private static final String APPLICANT_LR_TELEPHONE = "01234567";
    private static final String APPLICANT_LR_EMAIL = "applicantsolicitor@example.com";

    private static final String RESPONDENT1_LR_NAME = "Respondent 1 LR Org";
    private static final String RESPONDENT1_LR_TELEPHONE = "01234567";
    private static final String RESPONDENT1_LR_EMAIL = "respondentsolicitor@example.com";

    private static final String RESPONDENT2_LR_NAME = "Respondent 2 LR Org";
    private static final String RESPONDENT2_LR_TELEPHONE = "0123456789";
    private static final String RESPONDENT2_LR_EMAIL = "respondentsolicitor2@example.com";

    private static final String MEDIATION_CONTACT_NAME = "Contact person";
    private static final String MEDIATION_CONTACT_EMAIL = "Contact.person@mediation.com";
    private static final String MEDIATION_CONTACT_NUMBER = "07888888888";

    private static final String MEDIATION_ALT_CONTACT_NAME = "Alt contact person";
    private static final String MEDIATION_ALT_CONTACT_EMAIL = "altemail@mediation.com";
    private static final String MEDIATION_ALT_CONTACT_NUMBER = "07222222222";

    private static final String LIP_MEDIATION_CONTACT_NAME = "Lip contact person";

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private MediationJsonService service;

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisationById("QWERTY A"))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_NAME)
                                        .companyNumber(APPLICANT_LR_TELEPHONE)
                                        .build()));

        when(organisationService.findOrganisationById("QWERTY R"))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT1_LR_NAME)
                                        .companyNumber(RESPONDENT1_LR_TELEPHONE)
                                        .build()));

        when(organisationService.findOrganisationById("QWERTY R2"))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT2_LR_NAME)
                                        .companyNumber(RESPONDENT2_LR_TELEPHONE)
                                        .build()));
    }

    @Nested
    class CaseFlags {

        @Test
        void shouldReturnFalse_whenNoCaseLevelFlagsExistNoDQRequirement() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.isCaseFlags()).isFalse();
        }

        @Test
        void shouldReturnTrue_whenOnlyActiveCaseLevelFlagsExist() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withCaseLevelFlags()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.isCaseFlags()).isTrue();
        }

        @Nested
        class SupportAccessNeeds {

            @Test
            void shouldReturnTrue_whenOnlyApplicant1HasSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQHearingSupport(supportRequired(YES))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyApplicant1HasNoSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQHearingSupport(supportRequired(NO))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyApplicant2HasSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQHearingSupport(supportRequired(YES))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyApplicant2HasNoSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQHearingSupport(supportRequired(NO))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent1HasSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                      .respondent1DQHearingSupport(supportRequired(YES))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyRespondent1HasNoSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQHearingSupport(supportRequired(NO))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent2HasSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQHearingSupport(supportRequired(YES))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyRespondent2HasNoSupportAccessNeeds() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQHearingSupport(supportRequired(NO))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }
        }

        @Nested
        class VulnerabilityAdjustmentRequired {

            @Test
            void shouldReturnTrue_whenOnlyApplicant1HasVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(YES))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyApplicant1HasNoVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(NO))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyApplicant2HasVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(YES))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyApplicant2HasNoVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(NO))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent1HasVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(YES))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyRespondent1HasNoVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(NO))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent2HasVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(YES))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyRespondent2HasNoVulnerability() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(NO))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }
        }

        @Nested
        class LanguageRequirements {

            @Test
            void shouldReturnTrue_whenOnlyApplicant1HasChosenWelsh() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQLanguage(languageRequirements(WELSH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnTrue_whenOnlyApplicant1HasChosenBoth() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQLanguage(languageRequirements(BOTH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyApplicant1HasChosenEnglish() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQLanguage(languageRequirements(ENGLISH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyApplicant2HasChosenWelsh() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQLanguage(languageRequirements(WELSH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnTrue_whenOnlyApplicant2HasChosenBoth() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQLanguage(languageRequirements(BOTH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyApplicant2HasChosenEnglish() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQLanguage(languageRequirements(ENGLISH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent1HasChosenWelsh() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQLanguage(languageRequirements(WELSH))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent1HasChosenBoth() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                      .respondent1DQLanguage(languageRequirements(BOTH))
                                      .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyRespondent1HasChosenEnglish() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQLanguage(languageRequirements(ENGLISH))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent2HasChosenWelsh() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQLanguage(languageRequirements(WELSH))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnTrue_whenOnlyRespondent2HasChosenBoth() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQLanguage(languageRequirements(BOTH))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isTrue();
            }

            @Test
            void shouldReturnFalse_whenOnlyRespondent2HasChosenEnglish() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .build().toBuilder()
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQLanguage(languageRequirements(ENGLISH))
                                       .build()).build();

                MediationCase mediationCase = service.generateJsonContent(caseData);

                assertThat(mediationCase.isCaseFlags()).isFalse();
            }

        }

        @Test
        void shouldReturnTrue_whenApplicant1NoSupportRespondent1Vulnerability() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build().toBuilder()
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQVulnerabilityQuestions(vulnerabilityAdjustmentRequired(YES))
                                   .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearingSupport(supportRequired(NO))
                                  .build())
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.isCaseFlags()).isTrue();
        }
    }

    @Nested
    class Litigants {

        @Test
        void shouldBuildUnrepresentedLitigants_when1v1BothUnrepresentedNoAltMediation() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .applicant1Represented(NO)
                .respondent1Represented(NO)
                .addLiPRespondent1MediationInfo(false)
                .addLiPApplicant1MediationInfo(false)
                .build();

            List<MediationLitigant> expected = new ArrayList<>();
            expected.add(addMediationInfoLip(buildClaimant1(NO),
                                             caseData.getApplicant1().getPartyPhone(), caseData.getApplicant1().getPartyEmail()));
            expected.add(addMediationInfoLip(buildRespondent1(NO),
                                             caseData.getRespondent1().getPartyPhone(), caseData.getRespondent1().getPartyEmail()));

            MediationCase actual = service.generateJsonContent(caseData);

            assertThat(actual.getLitigants().size()).isEqualTo(expected.size());
            assertThat(actual.getLitigants()).isEqualTo(expected);
        }

        @Test
        void shouldBuildUnrepresentedLitigants_when1v1BothUnrepresentedNoAltMediationCompany() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssuedCompanyClaimant()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .applicant1Represented(NO)
                .respondent1Represented(NO)
                .addLiPRespondent1MediationInfo(false)
                .addLiPApplicant1MediationInfo(false)
                .build();

            List<MediationLitigant> expected = new ArrayList<>();
            expected.add(addMediationInfoLip(buildUnrepresentedClaimant1Company(),
                                             caseData.getApplicant1().getPartyPhone(), caseData.getApplicant1().getPartyEmail()));
            expected.add(addMediationInfoLip(buildRespondent1(NO),
                                             caseData.getRespondent1().getPartyPhone(), caseData.getRespondent1().getPartyEmail()));

            MediationCase actual = service.generateJsonContent(caseData);

            assertThat(actual.getLitigants().size()).isEqualTo(expected.size());
            assertThat(actual.getLitigants()).isEqualTo(expected);
        }

        @Test
        void shouldBuildUnrepresentedLitigants_when1v1BothUnrepresentedAltMediation() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .applicant1Represented(NO)
                .respondent1Represented(NO)
                .addLiPRespondent1MediationInfo(true)
                .addLiPApplicant1MediationInfo(true)
                .build();

            List<MediationLitigant> expected = new ArrayList<>();
            expected.add(addAltMediationInfoLip(buildClaimant1(NO)));
            expected.add(addAltMediationInfoLip(buildRespondent1(NO)));

            MediationCase actual = service.generateJsonContent(caseData);

            assertThat(actual.getLitigants().size()).isEqualTo(expected.size());
            assertThat(actual.getLitigants()).isEqualTo(expected);
        }

        @Test
        void shouldBuildUnrepresentedLitigants_when1v1BothRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .addApplicant1MediationInfo()
                .addApplicant1MediationAvailability()
                .addRespondent1MediationInfo()
                .addRespondent1MediationAvailability()
                .build();

            List<MediationLitigant> expected = new ArrayList<>();
            expected.add(addMediationInfoRepresented(buildClaimant1(YES)));
            expected.add(addMediationInfoRepresented(buildRespondent1(YES)));

            MediationCase actual = service.generateJsonContent(caseData);

            assertThat(actual.getLitigants().size()).isEqualTo(expected.size());
            assertThat(actual.getLitigants()).isEqualTo(expected);
        }

        @Test
        void shouldBuildLitigants_when1v2BothRespondentsRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .withApplicant1Flags()
                .withRespondent1Flags()
                .withRespondent2Flags()
                .addApplicant1MediationAvailability()
                .addApplicant1MediationInfo()
                .addRespondent1MediationInfo()
                .addRespondent1MediationAvailability()
                .addRespondent2MediationInfo()
                .addRespondent2MediationAvailability()
                .build();

            List<MediationLitigant> expected = new ArrayList<>();
            expected.add(addMediationInfoRepresented(buildClaimant1(YES)));
            expected.add(addMediationInfoRepresented(buildRespondent1(YES)));
            expected.add(addMediationInfoRepresented(buildRespondent2(YES)));

            MediationCase actual = service.generateJsonContent(caseData);

            assertThat(actual.getLitigants().size()).isEqualTo(expected.size());
            assertThat(actual.getLitigants()).isEqualTo(expected);
        }

        @Test
        void shouldBuildLitigants_when2v1AllRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoApplicants()
                .withApplicant1Flags()
                .withApplicant2Flags()
                .withRespondent1Flags()
                .addApplicant1MediationAvailability()
                .addApplicant1MediationInfo()
                .addRespondent1MediationInfo()
                .addRespondent1MediationAvailability()
                .addRespondent2MediationInfo()
                .addRespondent2MediationAvailability()
                .build();

            List<MediationLitigant> expected = new ArrayList<>();
            expected.add(addMediationInfoRepresented(buildClaimant1(YES)));
            expected.add(addMediationInfoRepresented(buildClaimant2()));
            expected.add(addMediationInfoRepresented(buildRespondent1(YES)));

            MediationCase actual = service.generateJsonContent(caseData);

            assertThat(actual.getLitigants().size()).isEqualTo(expected.size());
            assertThat(actual.getLitigants()).isEqualTo(expected);
        }
    }

    @Nested
    class CaseNumber {

        @Test
        void shouldReturnLong_forCCDCaseNumber() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.getCcdCaseNumber()).isEqualTo(1594901956117591L);
        }

        @Test
        void shouldReturnString_forLegacyCaseNumber() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.getCasemanCaseNumber()).isEqualTo("000DC001");
        }
    }

    @Nested
    class CaseTitle {

        @Test
        void shouldReturnCaseName_when1v1BothLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .respondent1Represented(NO)
                .applicant1Represented(NO)
                .withApplicant1Flags()
                .withRespondent1Flags()
                .applicant1Represented(NO)
                .respondent1Represented(NO)
                .addLiPRespondent1MediationInfo(false)
                .addLiPApplicant1MediationInfo(false)
                .buildHmctsInternalCaseName()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.getCaseTitle()).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void shouldReturnCaseName_when1v1BothRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .buildHmctsInternalCaseName()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.getCaseTitle()).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void shouldReturnCaseName_when1v2Both() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .buildHmctsInternalCaseName()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.getCaseTitle()).isEqualTo("Mr. John Rambo v Mr. Sole Trader and Mr. John Rambo");
        }

        @Test
        void shouldReturnCaseName_when2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoApplicants()
                .buildHmctsInternalCaseName()
                .build();

            MediationCase mediationCase = service.generateJsonContent(caseData);

            assertThat(mediationCase.getCaseTitle()).isEqualTo("Mr. John Rambo and Mr. Jason Rambo v Mr. Sole Trader");
        }
    }

    @Test
    void shouldReturnString_forClaimValue() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedSmallClaim()
            .build();

        MediationCase mediationCase = service.generateJsonContent(caseData);

        assertThat(mediationCase.getClaimValue()).isEqualTo(BigDecimal.valueOf(800));
    }

    private HearingSupport supportRequired(YesOrNo option) {
        return HearingSupport.builder()
            .supportRequirements(option)
            .build();
    }

    private VulnerabilityQuestions vulnerabilityAdjustmentRequired(YesOrNo option) {
        return VulnerabilityQuestions.builder()
            .vulnerabilityAdjustmentsRequired(option)
            .build();
    }

    private WelshLanguageRequirements languageRequirements(Language language) {
        return WelshLanguageRequirements.builder()
            .court(language)
            .evidence(language)
            .documents(language)
            .build();
    }

    private MediationLitigant buildClaimant1(YesOrNo represented) {
        if (YES.equals(represented)) {
            return MediationLitigant.builder()
                .partyID("app-1-party-id")
                .partyRole("Claimant 1")
                .partyName("Mr. John Rambo")
                .partyType(INDIVIDUAL)
                .paperResponse(PAPER_RESPONSE)
                .represented(true)
                .solicitorOrgName(APPLICANT_LR_NAME)
                .litigantTelephone(null)
                .litigantEmail(APPLICANT_LR_EMAIL)
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        } else {
            return MediationLitigant.builder()
                .partyID("app-1-party-id")
                .partyRole("Claimant 1")
                .partyName("Mr. John Rambo")
                .partyType(INDIVIDUAL)
                .paperResponse(PAPER_RESPONSE)
                .represented(false)
                .solicitorOrgName(null)
                .litigantTelephone("0123456789")
                .litigantEmail("rambo@email.com")
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        }
    }

    private MediationLitigant buildUnrepresentedClaimant1Company() {
        return MediationLitigant.builder()
            .partyID("app-1-party-id")
            .partyRole("Claimant 1")
            .partyName("Company ltd")
            .partyType(COMPANY)
            .paperResponse(PAPER_RESPONSE)
            .represented(false)
            .solicitorOrgName(null)
            .litigantTelephone("0123456789")
            .litigantEmail("company@email.com")
            .mediationContactName(null)
            .mediationContactNumber(null)
            .mediationContactEmail(null)
            .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
            .build();
    }

    private MediationLitigant buildClaimant2() {
        return MediationLitigant.builder()
            .partyID("app-2-party-id")
            .partyRole("Claimant 2")
            .partyName("Mr. Jason Rambo")
            .partyType(INDIVIDUAL)
            .paperResponse(PAPER_RESPONSE)
            .represented(true)
            .solicitorOrgName(APPLICANT_LR_NAME)
            .litigantTelephone(null)
            .litigantEmail(APPLICANT_LR_EMAIL)
            .mediationContactName(null)
            .mediationContactNumber(null)
            .mediationContactEmail(null)
            .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
            .build();
    }

    private MediationLitigant buildRespondent1(YesOrNo represented) {
        if (YES.equals(represented)) {
            return MediationLitigant.builder()
                .partyID("res-1-party-id")
                .partyRole("Defendant 1")
                .partyName("Mr. Sole Trader")
                .partyType(SOLE_TRADER)
                .paperResponse(PAPER_RESPONSE)
                .represented(true)
                .solicitorOrgName(RESPONDENT1_LR_NAME)
                .litigantTelephone(null)
                .litigantEmail(RESPONDENT1_LR_EMAIL)
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        } else {
            return MediationLitigant.builder()
                .partyID("res-1-party-id")
                .partyRole("Defendant 1")
                .partyName("Mr. Sole Trader")
                .partyType(SOLE_TRADER)
                .paperResponse(PAPER_RESPONSE)
                .represented(false)
                .solicitorOrgName(null)
                .litigantTelephone("0123456789")
                .litigantEmail("sole.trader@email.com")
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        }
    }

    private MediationLitigant buildRespondent2(YesOrNo represented) {
        if (YES.equals(represented)) {
            return MediationLitigant.builder()
                .partyID("res-2-party-id")
                .partyRole("Defendant 2")
                .partyName("Mr. John Rambo")
                .partyType(INDIVIDUAL)
                .paperResponse(PAPER_RESPONSE)
                .represented(true)
                .solicitorOrgName(RESPONDENT2_LR_NAME)
                .litigantTelephone(null)
                .litigantEmail(RESPONDENT2_LR_EMAIL)
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        } else {
            return MediationLitigant.builder()
                .partyID("res-2-party-id")
                .partyRole("Defendant 2")
                .partyName("Mr. John Rambo")
                .partyType(INDIVIDUAL)
                .paperResponse(PAPER_RESPONSE)
                .represented(false)
                .solicitorOrgName(null)
                .litigantTelephone("0123456789")
                .litigantEmail("rambo@email.com")
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        }
    }

    private MediationLitigant addMediationInfoRepresented(MediationLitigant litigant) {
        return litigant.toBuilder()
            .mediationContactName(MEDIATION_CONTACT_NAME)
            .mediationContactNumber(MEDIATION_CONTACT_NUMBER)
            .mediationContactEmail(MEDIATION_CONTACT_EMAIL)
            .dateRangeToAvoid(List.of(MediationUnavailability.builder()
                                          .dateFrom("2024-06-01")
                                          .dateTo("2024-06-01")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-07")
                                          .dateTo("2024-06-07")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-10")
                                          .dateTo("2024-06-15")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-20")
                                          .dateTo("2024-06-25")
                                          .build())).build();
    }

    private MediationLitigant addAltMediationInfoLip(MediationLitigant litigant) {
        String mediationContactName =
            Party.Type.INDIVIDUAL.equals(litigant.getPartyType())
                || SOLE_TRADER.equals(litigant.getPartyType())
                ? litigant.getPartyName() : MEDIATION_ALT_CONTACT_NAME;
        return litigant.toBuilder()
            .mediationContactName(mediationContactName)
            .mediationContactNumber(MEDIATION_ALT_CONTACT_NUMBER)
            .mediationContactEmail(MEDIATION_ALT_CONTACT_EMAIL)
            .dateRangeToAvoid(List.of(MediationUnavailability.builder()
                                          .dateFrom("2024-06-01")
                                          .dateTo("2024-06-01")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-07")
                                          .dateTo("2024-06-07")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-10")
                                          .dateTo("2024-06-15")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-20")
                                          .dateTo("2024-06-25")
                                          .build())).build();
    }

    private MediationLitigant addMediationInfoLip(MediationLitigant litigant,
                                                  String number, String email) {
        String mediationContactName =
            Party.Type.INDIVIDUAL.equals(litigant.getPartyType())
                || SOLE_TRADER.equals(litigant.getPartyType())
                ? litigant.getPartyName() : LIP_MEDIATION_CONTACT_NAME;
        return litigant.toBuilder()
            .mediationContactName(mediationContactName)
            .mediationContactNumber(number)
            .mediationContactEmail(email)
            .dateRangeToAvoid(List.of(MediationUnavailability.builder()
                                          .dateFrom("2024-06-01")
                                          .dateTo("2024-06-01")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-07")
                                          .dateTo("2024-06-07")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-10")
                                          .dateTo("2024-06-15")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-20")
                                          .dateTo("2024-06-25")
                                          .build())).build();
    }
}

