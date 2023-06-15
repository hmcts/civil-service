package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureReport;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HNL_DQ_RESPONSE_1V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HNL_DQ_RESPONSE_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HNL_DQ_RESPONSE_1V2_DS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HNL_DQ_RESPONSE_1V2_SS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DirectionsQuestionnaireGenerator.class,
    JacksonAutoConfiguration.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class
})
class DirectionsQuestionnaireGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String FILE_NAME_DEFENDANT = format(N181.getDocumentTitle(), "defendant", REFERENCE_NUMBER);
    private static final String HNL_FILE_NAME_DEFENDANT = format(HNL_DQ_RESPONSE_1V1.getDocumentTitle(), "defendant", REFERENCE_NUMBER);
    private static final String FILE_NAME_CLAIMANT = format(N181.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final String HNL_FILE_NAME_CLAIMANT = format(HNL_DQ_RESPONSE_1V1.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final String HNL_FILE_NAME_CLAIMANT_1v2 = format(HNL_DQ_RESPONSE_1V2_DS.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT_DEFENDANT =
        CaseDocumentBuilder.builder()
            .documentName(FILE_NAME_DEFENDANT)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .build();

    private static final CaseDocument HNL_CASE_DOCUMENT_DEFENDANT =
        CaseDocumentBuilder.builder()
            .documentName(HNL_FILE_NAME_DEFENDANT)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .build();
    private static final CaseDocument CASE_DOCUMENT_CLAIMANT =
        CaseDocumentBuilder.builder()
            .documentName(FILE_NAME_CLAIMANT)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .build();

    private static final CaseDocument HNL_CASE_DOCUMENT_CLAIMANT =
        CaseDocumentBuilder.builder()
            .documentName(HNL_FILE_NAME_CLAIMANT)
            .documentType(DIRECTIONS_QUESTIONNAIRE)
            .build();

    private final Representative defendant1Representative =
        Representative.builder()
            .organisationName("test org")
            .build();

    private final Representative defendant2Representative =
        Representative.builder()
            .organisationName("test org 2")
            .build();

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DirectionsQuestionnaireGenerator generator;

    @MockBean
    private LocationRefDataService locationRefDataService;

    @Nested
    class RespondentOne {

        @BeforeEach
        void setup() {
            when(representativeService.getRespondent1Representative(any())).thenReturn(defendant1Representative);
        }

        @Test
        void shouldGenerateRespondentOneCertificateOfService_whenStateFlowIsFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateClaimantCertificateOfService_whenStateFlowIsRespondToDefenceAndProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .atStateApplicantRespondToDefenceAndProceed().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateDQ_when2v1ScenarioWithFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181_2V1)))
                .thenReturn(new DocmosisDocument(N181_2V1.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(N181_2V1)
            );
        }

        @Test
        void shouldGenerateDQ_when2v1ScenarioWithFullDefence_withHnlToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HNL_DQ_RESPONSE_2V1)))
                .thenReturn(new DocmosisDocument(HNL_DQ_RESPONSE_2V1.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);
            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(HNL_DQ_RESPONSE_2V1)
            );
        }

        @Test
        void shouldGenerateDQ_when1v2SameSolicitorScenarioWithFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(N181_MULTIPARTY_SAME_SOL)))
                .thenReturn(new DocmosisDocument(N181_MULTIPARTY_SAME_SOL.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .respondentResponseIsSame(YES)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(N181_MULTIPARTY_SAME_SOL)
            );
        }

        @Test
        void shouldGenerateDQ_specRespondent() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC.getDocumentTitle(),
                                          "defendant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC)
            );
        }

        @Test
        void shouldGenerateDQ_specClaimant() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.CLAIMANT_RESPONSE_SPEC.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC.getDocumentTitle(),
                                          "claimant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .applicant1DQWithExperts()
                .applicant1DQWithWitnesses()
                .applicant1DQWithHearingSupport()
                .build()
                .toBuilder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE_SPEC").build())
                .caseAccessCategory(SPEC_CLAIM)
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC)
            );
        }

        @Test
        void specGenerate_withHnlToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_HNL)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_HNL.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_HNL.getDocumentTitle(),
                                          "defendant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_HNL)
            );
        }

        @Test
        void specGenerateClaimantDQ_withHnlToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_HNL)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_HNL.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_HNL.getDocumentTitle(),
                                          "claimant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .businessProcess(BusinessProcess.builder().camundaEvent("CLAIMANT_RESPONSE").build())
                .build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_CLAIMANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_HNL)
            );
        }

        @Nested
        class GetTemplateData {

            @Test
            void whenCaseStateIsRespondedToClaim_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com").build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com").build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence1v1ApplicantProceeds_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE").build())
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com").build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com").build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence1v1ApplicantProceedsLRSpec_shouldGetApplicantDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .applicant1DQWithExperts()
                    .applicant1DQWithWitnesses()
                    .applicant1DQWithHearingSupport()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE_SPEC").build())
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .caseAccessCategory(SPEC_CLAIM)
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                //assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
                assertEquals(applicant1ExpertsMock(), templateData.getExperts());
                assertEquals(applicant1WitnessesMock(), templateData.getWitnesses());
                assertEquals(
                    templateData.getSupport(),
                    caseData.getApplicant1DQ().getHearingSupport()
                );
                assertEquals(
                    caseData.getApplicant1DQ().getFileDirectionQuestionnaire(),
                    templateData.getFileDirectionsQuestionnaire()
                );
                assertEquals(
                    templateData.getSupport(),
                    caseData.getApplicant1DQ().getHearingSupport()
                );
            }

            @Test
            void whenCaseStateIsFullDefence2v1Applicant1ProceedsLRSpec_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .multiPartyClaimTwoApplicants()
                    .applicant1DQWithExperts()
                    .applicant1DQWithWitnesses()
                    .applicant1DQWithHearingSupport()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE_SPEC").build())
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .caseAccessCategory(SPEC_CLAIM)
                    .applicant1ProceedWithClaimSpec2v1(YES)
                    .addApplicant2(YES)
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                //assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
                assertEquals(applicant1ExpertsMock(), templateData.getExperts());
                assertEquals(applicant1WitnessesMock(), templateData.getWitnesses());
                assertEquals(
                    templateData.getSupport(),
                    caseData.getApplicant1DQ().getHearingSupport()
                );
                assertEquals(
                    templateData.getFileDirectionsQuestionnaire(),
                    caseData.getApplicant1DQ().getFileDirectionQuestionnaire()
                );
                assertEquals(
                    templateData.getSupport(),
                    caseData.getApplicant1DQ().getHearingSupport()
                );
            }

            @Test
            void whenCaseStateIsFullDefence1v2_ONE_LR_Applicant1ProceedsLRSpec_shouldGetApplicantDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimOneDefendantSolicitor()
                    .atStateApplicantRespondToDefenceAndNotProceed_1v2()
                    .applicant1DQWithLocation()
                    .applicant1DQWithExperts()
                    .applicant1DQWithWitnesses()
                    .applicant1DQWithHearingSupport()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE_SPEC").build())
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .applicant1ProceedWithClaim(YES)
                    .caseAccessCategory(SPEC_CLAIM)
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YES)
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertEquals(applicant1ExpertsMock(), templateData.getExperts());
                assertEquals(applicant1WitnessesMock(), templateData.getWitnesses());
                assertEquals(
                    templateData.getSupport(),
                    caseData.getApplicant1DQ().getHearingSupport()
                );
                assertEquals(
                    templateData.getFileDirectionsQuestionnaire(),
                    caseData.getApplicant1DQ().getFileDirectionQuestionnaire()
                );
                assertEquals(
                    templateData.getSupport(),
                    caseData.getApplicant1DQ().getHearingSupport()
                );
            }

            @Test
            void whenCaseStateIsFullDefence1v2_TWO_LR_Applicant1ProceedsLRSpec_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimOneDefendantSolicitor()
                    .atStateApplicantRespondToDefenceAndNotProceed_1v2_DiffSol()
                    .applicant1DQWithLocation()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE_SPEC").build())
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .applicant1ProceedWithClaim(YES)
                    .caseAccessCategory(SPEC_CLAIM)
                    .respondent2SameLegalRepresentative(NO)
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertEquals(
                    templateData.getFileDirectionsQuestionnaire(),
                    caseData.getApplicant1DQ().getFileDirectionQuestionnaire()
                );
            }

            @Test
            void whenCaseStateIsFullDefence1v2ApplicantProceedsAgainstRes1_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE").build())
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com").build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com").build())
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence2v1Applicant1Proceeds_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE").build())
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com").build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com").build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence2v1Applicant2Proceeds_shouldGetApplicantDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicant2RespondToDefenceAndProceed_2v1()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE").build())
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com")
                                                    .build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com").build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant2DQ(), caseData);
            }

            @Test
            void whenMultiparty2v1_shouldGetDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .build()
                    .toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com")
                                                    .build())
                    .applicant2LitigationFriend(LitigationFriend.builder()
                                                    .fullName("ApplicantTwo LF")
                                                    .firstName("Applicant2")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicant2LF@email.com")
                                                    .build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com")
                                                     .build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect2v1(templateData, caseData.getRespondent1DQ(), caseData);
            }

            @Test
            void whenNoRequestedCourt_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQRequestedCourt(null)
                                       .responseClaimCourtLocationRequired(null)
                                       .respondToCourtLocation(null)
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
            }

            @Test
            void whenNoExperts_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
            }

            @Test
            void whenExperts_includeDetails() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                uk.gov.hmcts.reform.civil.model.dq.Expert expert1 =
                    uk.gov.hmcts.reform.civil.model.dq.Expert.builder()
                        .name("Expert 1")
                        .firstName("first")
                        .lastName("last")
                        .phoneNumber("07123456789")
                        .emailAddress("test@email.com")
                        .fieldOfExpertise("expertise 1")
                        .whyRequired("Explanation")
                        .estimatedCost(BigDecimal.valueOf(10000))
                        .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(uk.gov.hmcts.reform.civil.model.dq.Experts.builder()
                                                                 .expertRequired(YES)
                                                                 .expertReportsSent(ExpertReportsSent.NOT_OBTAINED)
                                                                 .jointExpertSuitable(YES)
                                                                 .details(ElementUtils.wrapElements(expert1))
                                                                 .build())
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                Expert extracted = templateData.getExperts().getDetails().get(0);
                assertThat(extracted.getName()).isEqualTo(expert1.getName());
                assertThat(extracted.getFieldOfExpertise()).isEqualTo(expert1.getFieldOfExpertise());
                assertThat(extracted.getWhyRequired()).isEqualTo(expert1.getWhyRequired());
                assertThat(extracted.getFormattedCost()).isEqualTo("£100.00");
                assertThat(extracted.getFirstName()).isEqualTo("first");
                assertThat(extracted.getLastName()).isEqualTo("last");
                assertThat(extracted.getPhoneNumber()).isEqualTo("07123456789");
                assertThat(extracted.getEmailAddress()).isEqualTo("test@email.com");
            }

            @Test
            void whenDisclosureReport_include() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                String disclosureOrderNumber = "123";
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQDisclosureReport(DisclosureReport.builder()
                                                                          .disclosureFormFiledAndServed(YES)
                                                                          .disclosureProposalAgreed(YES)
                                                                          .draftOrderNumber(disclosureOrderNumber)
                                                                          .build())
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                DisclosureReport extracted = templateData.getDisclosureReport();
                assertThat(extracted.getDraftOrderNumber()).isEqualTo(disclosureOrderNumber);
                assertThat(extracted.getDisclosureProposalAgreed()).isEqualTo(YES);
                assertThat(extracted.getDisclosureFormFiledAndServed()).isEqualTo(YES);
            }

            @Test
            void whenFurtherInformation_include() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();

                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQFutureApplications(
                                           FutureApplications.builder()
                                               .intentionToMakeFutureApplications(YES)
                                               .whatWillFutureApplicationsBeMadeFor("Reason for future apps")
                                               .build()
                                       )
                                       .respondent1DQFurtherInformation(FurtherInformation.builder()
                                                                            .otherInformationForJudge("other info")
                                                                            .build())
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                FurtherInformation extracted = templateData.getFurtherInformation();
                assertThat(extracted.getFutureApplications()).isEqualTo(YES);
                assertThat(extracted.getIntentionToMakeFutureApplications()).isEqualTo(YES);
                assertThat(extracted.getOtherInformationForJudge()).isEqualTo("other info");
            }

            @Test
            public void whenNoWelsh_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQLanguage(null)
                                       .build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWelshLanguageRequirements()).isNotNull();
            }

            @Test
            void whenSmallClaimAndNoWitness() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(uk.gov.hmcts.reform.civil.model.dq.Hearing.builder()
                                                                 .hearingLength(null)
                                                                 .build())
                                       .build())
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(0);
            }

            @Test
            void whenSmallClaimAndWitnesses() {
                int witnessesIncludingDefendant = 2;
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(null)
                                       .build())
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .responseClaimWitnesses(Integer.toString(witnessesIncludingDefendant))
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(witnessesIncludingDefendant);
            }

            @Test
            void whenSmallClaimSpecAndWitnessesNoExperts() {
                int witnessesIncludingDefendant = 2;
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(null)
                                       .build())
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .responseClaimWitnesses(Integer.toString(witnessesIncludingDefendant))
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(witnessesIncludingDefendant);
            }

            @Test
            void whenSmallClaimSpecAndWitnesses() {
                int witnessesIncludingDefendant = 2;
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .setClaimTypeToSpecClaim()
                    .build();
                caseData = caseData.toBuilder()
                    .responseClaimExpertSpecRequired(YES)
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondToClaimExperts(ExpertDetails.builder()
                                                                  .expertName("Mr Expert Defendant")
                                                                  .firstName("Expert")
                                                                  .lastName("Defendant")
                                                                  .phoneNumber("07123456789")
                                                                  .emailAddress("test@email.com")
                                                                  .fieldofExpertise("Roofing")
                                                                  .estimatedCost(new BigDecimal(434))
                                                                  .build())
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(null)
                                       .build())
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .responseClaimWitnesses(Integer.toString(witnessesIncludingDefendant))
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(witnessesIncludingDefendant);
            }

            @Test
            void whenSmallClaimSpecAndWitnesses_withHnlEnabled() {
                when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .applicant1DQWithWitnesses()
                    .build()
                    .toBuilder()
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE_SPEC").build())
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnesses())
                    .isEqualTo(applicant1WitnessesMock());
            }

            @Test
            void whenSmallClaimSpecFullAdmissionNoExperts() {
                int witnessesIncludingDefendant = 2;
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .atStateRespondent1v1FullAdmissionSpec()
                    .setClaimTypeToSpecClaim()
                    .build();
                caseData = caseData.toBuilder()
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondent1DQExperts(null)
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(null)
                                       .build())
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .responseClaimWitnesses(Integer.toString(witnessesIncludingDefendant))
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants()).isNull();
            }

            @Test
            void whenSmallClaimSpecFullAdmission() {
                int witnessesIncludingDefendant = 2;
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .atStateRespondent1v1FullAdmissionSpec()
                    .setClaimTypeToSpecClaim()
                    .build();
                caseData = caseData.toBuilder()
                    .responseClaimExpertSpecRequired(YES)
                    .respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                       .respondToClaimExperts(ExpertDetails.builder()
                                                                  .expertName("Mr Expert Defendant")
                                                                  .firstName("Expert")
                                                                  .lastName("Defendant")
                                                                  .phoneNumber("07123456789")
                                                                  .emailAddress("test@email.com")
                                                                  .fieldofExpertise("Roofing")
                                                                  .estimatedCost(new BigDecimal(434))
                                                                  .build())
                                       .respondent1DQWitnesses(null)
                                       .respondent1DQHearing(null)
                                       .build())
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                    .responseClaimWitnesses(Integer.toString(witnessesIncludingDefendant))
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants()).isNull();
            }

            private void assertThatDqFieldsAreCorrect2v1(DirectionsQuestionnaireForm templateData, DQ dq,
                                                         CaseData caseData) {
                assertEquals(templateData.getApplicant2(), getApplicant2(caseData));
                assertThatDqFieldsAreCorrect(templateData, dq, caseData);
            }

            private void assertThatDqFieldsAreCorrect(DirectionsQuestionnaireForm templateData,
                                                      DQ dq, CaseData caseData) {
                Assertions.assertAll(
                    "DQ data should be as expected",
                    () -> assertEquals(
                        templateData.getFileDirectionsQuestionnaire(),
                        dq.getFileDirectionQuestionnaire()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfElectronicDocuments(),
                        dq.getDisclosureOfElectronicDocuments()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfNonElectronicDocuments(),
                        dq.getDisclosureOfNonElectronicDocuments()
                    ),
                    () -> assertEquals(templateData.getRespondents(), getRespondents(caseData)),
                    () -> assertEquals(templateData.getApplicant(), getApplicant(caseData)),
                    () -> assertEquals(templateData.getExperts(), getExperts(dq)),
                    () -> assertEquals(templateData.getWitnesses(), getWitnesses(dq)),
                    () -> assertEquals(templateData.getHearing(), getHearing(dq)),
                    () -> assertEquals(templateData.getHearingSupport(), getHearingSupport(dq)),
                    () -> assertEquals(templateData.getWelshLanguageRequirements(), getWelshLanguageRequirements(dq)),
                    () -> assertEquals(templateData.getStatementOfTruth(), dq.getStatementOfTruth()),
                    () -> assertEquals(templateData.getVulnerabilityQuestions(), dq.getVulnerabilityQuestions())
                );
            }

            private Party getApplicant(CaseData caseData) {
                var applicant = caseData.getApplicant1();
                return Party.builder()
                    .name(applicant.getPartyName())
                    .emailAddress(applicant.getPartyEmail())
                    .phoneNumber(applicant.getPartyPhone())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("Applicant LF")
                    .litigationFriendFirstName("Applicant")
                    .litigationFriendLastName("LF")
                    .litigationFriendEmailAddress("applicantLF@email.com")
                    .litigationFriendPhoneNumber("1234567890")
                    .build();
            }

            private Party getApplicant2(CaseData caseData) {
                var applicant = caseData.getApplicant2();
                return Party.builder()
                    .name(applicant.getPartyName())
                    .emailAddress(applicant.getPartyEmail())
                    .phoneNumber(applicant.getPartyPhone())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("ApplicantTwo LF")
                    .litigationFriendFirstName("Applicant2")
                    .litigationFriendLastName("LF")
                    .litigationFriendEmailAddress("applicant2LF@email.com")
                    .litigationFriendPhoneNumber("1234567890")
                    .build();
            }

            private List<Party> getRespondents(CaseData caseData) {
                var respondent = caseData.getRespondent1();
                return List.of(Party.builder()
                                   .name(respondent.getPartyName())
                                   .phoneNumber(respondent.getPartyPhone())
                                   .emailAddress(respondent.getPartyEmail())
                                   .primaryAddress(respondent.getPrimaryAddress())
                                   .representative(defendant1Representative)
                                   .litigationFriendName("Respondent LF")
                                   .litigationFriendFirstName("Respondent")
                                   .litigationFriendLastName("LF")
                                   .litigationFriendEmailAddress("respondentLF@email.com")
                                   .litigationFriendPhoneNumber("1234567890")
                                   .build());
            }

            private Experts getExperts(DQ dq) {
                var experts = dq.getExperts();
                return Experts.builder()
                    .expertRequired(experts.getExpertRequired())
                    .expertReportsSent(
                        ofNullable(experts.getExpertReportsSent())
                            .map(ExpertReportsSent::getDisplayedValue)
                            .orElse(""))
                    .jointExpertSuitable(experts.getJointExpertSuitable())
                    .details(getExpertsDetails(dq))
                    .build();
            }

            private List<Expert> getExpertsDetails(DQ dq) {
                return unwrapElements(dq.getExperts().getDetails())
                    .stream()
                    .map(expert -> Expert.builder()
                        .name(expert.getName())
                        .firstName(expert.getFirstName())
                        .lastName(expert.getLastName())
                        .phoneNumber(expert.getPhoneNumber())
                        .emailAddress(expert.getEmailAddress())
                        .fieldOfExpertise(expert.getFieldOfExpertise())
                        .whyRequired(expert.getWhyRequired())
                        .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                           .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                        .build())
                    .collect(toList());
            }

            private Witnesses getWitnesses(DQ dq) {
                var witnesses = dq.getWitnesses();
                return Witnesses.builder()
                    .witnessesToAppear(witnesses.getWitnessesToAppear())
                    .details(unwrapElements(witnesses.getDetails()))
                    .build();
            }

            private Hearing getHearing(DQ dq) {
                var hearing = dq.getHearing();
                return Hearing.builder()
                    .hearingLength(getHearingLength(dq))
                    .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                    .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                    .build();
            }

            private String getHearingLength(DQ dq) {
                var hearing = dq.getHearing();
                switch (hearing.getHearingLength()) {
                    case LESS_THAN_DAY:
                        return hearing.getHearingLengthHours() + " hours";
                    case ONE_DAY:
                        return "One day";
                    default:
                        return hearing.getHearingLengthDays() + " days";
                }
            }

            private HearingSupport getSupportRequirements() {
                return HearingSupport.builder()
                    .requirements(List.of())
                    .supportRequirements(YES)
                    .supportRequirementsAdditional("Additional support needed")
                    .build();
            }

            private String getHearingSupport(DQ dq) {
                var stringBuilder = new StringBuilder();
                ofNullable(dq.getHearingSupport())
                    .map(HearingSupport::getRequirements)
                    .orElse(List.of())
                    .forEach(requirement -> {
                        var hearingSupport = dq.getHearingSupport();
                        stringBuilder.append(requirement.getDisplayedValue());
                        switch (requirement) {
                            case SIGN_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getSignLanguageRequired());
                                break;
                            case LANGUAGE_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getLanguageToBeInterpreted());
                                break;
                            case OTHER_SUPPORT:
                                stringBuilder.append(" - ").append(hearingSupport.getOtherSupport());
                                break;
                            default:
                                break;
                        }
                        stringBuilder.append("\n");
                    });
                return stringBuilder.toString().trim();
            }

            private WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
                var welshLanguageRequirements = dq.getWelshLanguageRequirements();
                return WelshLanguageRequirements.builder()
                    .evidence(ofNullable(
                        welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
                    .court(ofNullable(
                        welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
                    .documents(ofNullable(
                        welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
                    .build();
            }

            private Experts applicant1ExpertsMock() {
                return Experts.builder()
                    .expertRequired(YES)
                    .expertReportsSent(ExpertReportsSent.NO.getDisplayedValue())
                    .jointExpertSuitable(NO)
                    .details(List.of(
                                 uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert.builder()
                                     .firstName("Expert")
                                     .lastName("One")
                                     .phoneNumber("01482764322")
                                     .emailAddress("fast.claim.expert1@example.com")
                                     .whyRequired("Good reasons")
                                     .fieldOfExpertise("Some field")
                                     .formattedCost("£100.00")
                                     .build()
                             )
                    ).build();
            }

            private Witnesses applicant1WitnessesMock() {
                return Witnesses.builder()
                    .witnessesToAppear(YES)
                    .details(List.of(
                        Witness.builder()
                            .firstName("Witness")
                            .lastName("One")
                            .phoneNumber("01482764322")
                            .emailAddress("witness.one@example.com")
                            .reasonForWitness("Saw something")
                            .build()))
                    .build();
            }
        }
    }

    @Nested
    class RespondentTwo {

        @BeforeEach
        void setup() {
            when(representativeService.getRespondent1Representative(any())).thenReturn(defendant1Representative);
            when(representativeService.getRespondent2Representative(any())).thenReturn(defendant2Representative);
        }

        @Test
        void shouldGenerateRespondentTwoCertificateOfService_whenStateFlowIsFullDefenceForBoth() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1DQWithLocation()
                .respondent2DQWithLocation()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            LocationRefData location = LocationRefData.builder()
                .epimmsId(caseData.getRespondent2DQ().getRequestedCourt().getCaseLocation().getBaseLocation())
                .regionId(caseData.getRespondent2DQ().getRequestedCourt().getCaseLocation().getRegion())
                .courtLocationCode(caseData.getRespondent2DQ().getRequestedCourt().getResponseCourtCode())
                .courtName(caseData.getRespondent2DQ().getRequestedCourt().getResponseCourtName())
                .courtTypeId(RoboticsDataUtil.CIVIL_COURT_TYPE_ID)
                .build();
            List<LocationRefData> locationList = Collections.singletonList(location);
            when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
                anyString(),
                eq(caseData.getRespondent2DQ().getRequestedCourt().getCaseLocation().getBaseLocation())
            )).thenReturn(locationList);

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent2Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                argThat((MappableObject templateData) ->
                    templateData instanceof DirectionsQuestionnaireForm
                        && StringUtils.isNotBlank(((DirectionsQuestionnaireForm) templateData)
                                                      .getRequestedCourt().getResponseCourtCode())
                        && StringUtils.isNotBlank(((DirectionsQuestionnaireForm) templateData)
                                                      .getRequestedCourt().getResponseCourtName())
                        && StringUtils.isNotBlank(((DirectionsQuestionnaireForm) templateData)
                                                      .getRequestedCourt().getReasonForHearingAtSpecificCourt())
                ),
                eq(N181));
        }

        @Test
        void shouldGenerateRespondentTwoCertificateOfService_whenStateFlowIsFullDefenceForBoth_withHnlToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HNL_DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(HNL_DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent2Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                                                                      eq(HNL_DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateClaimantDQ_for1v2_DS_withHnlToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HNL_DQ_RESPONSE_1V2_DS)))
                .thenReturn(new DocmosisDocument(HNL_DQ_RESPONSE_1V2_DS.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT_1v2, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                .respondent2SameLegalRepresentative(NO)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                .businessProcess(BusinessProcess.builder().camundaEvent("CLAIMANT_RESPONSE").build())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent2Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT_1v2, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                                                                      eq(HNL_DQ_RESPONSE_1V2_DS));
        }

        @Test
        void shouldGenerateClaimantCertificateOfService_whenStateFlowIsRespondToDefenceAndProceed() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicant1DQWithLocation()
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Nested
        class GetTemplateData {

            @Test
            void whenRespondent2Response_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com")
                                                    .build())
                    .respondent2LitigationFriend(LitigationFriend.builder()
                                                     .fullName("respondent 2 LF")
                                                     .firstName("Respondent2")
                                                     .lastName("LF")
                                                     .phoneNumber("123456789")
                                                     .emailAddress("respondent2LF@email.com").build())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent2DQ(), caseData);
            }

            @Test
            void whenRespondent2LaterResponse_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com")
                                                    .build())
                    .respondent2LitigationFriend(LitigationFriend.builder()
                                                     .fullName("respondent 2 LF")
                                                     .firstName("Respondent2")
                                                     .lastName("LF")
                                                     .phoneNumber("123456789")
                                                     .emailAddress("respondent2LF@email.com").build())
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent2DQ(), caseData);
            }

            @Test
            void whenRespondent2SameLegalRepAndRespondentResponseSame_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com")
                                                    .build())
                    .respondent1LitigationFriend(LitigationFriend.builder()
                                                     .fullName("Respondent LF")
                                                     .firstName("Respondent")
                                                     .lastName("LF")
                                                     .phoneNumber("1234567890")
                                                     .emailAddress("respondentLF@email.com")
                                                     .build())
                    .respondent2LitigationFriend(LitigationFriend.builder()
                                                     .fullName("respondent 2 LF")
                                                     .firstName("Respondent2")
                                                     .lastName("LF")
                                                     .phoneNumber("123456789")
                                                     .emailAddress("respondent2LF@email.com").build())
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YES)
                    .build();
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertEquals(templateData.getRespondents(), getRespondents(caseData));
            }

            @Test
            void when1v2SolRespondsTo2ndDefendantWithDivergentResponse_shouldGetRespondentDQData() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                                                                                            "TWO"
                );

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo2ndDefendantWithDivergentResponse_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                                                                                        "TWO"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent1ResponseDate(createdDate)
                    .respondent1(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                                                                                        "ONE"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo1stDefendantWithDivergentResponseSmallClaim_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent1ResponseDate(createdDate)
                    .respondent1(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .responseClaimTrack("SMALL_CLAIM")
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                                                                                        "ONE"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo2stDefendantWithDivergentResponse_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant2LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent2LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                                                                                        "TWO"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo2ndDefendantWithDivergentResponseSmallClaim_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant2LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent2LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .responseClaimTrack("SMALL_CLAIM")
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                                                                                        "TWO"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void when1v2DiffSol_shouldAcceptOneOrTwo() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant2LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent2LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN, null)
                );
            }

            @Test
            void when1v2DiffSol2withoutResponseDate_shouldFail() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant2LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent2LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .respondent2ResponseDate(null)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                Assertions.assertThrows(
                    NullPointerException.class,
                    () -> generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN, "TWO")
                );
            }

            @Test
            void when1v2DiffSolDocAlreadyGenerated_shouldNotRegenerate() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .legacyCaseReference("reference")
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent1ResponseDate(createdDate)
                    .respondent1(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                caseData.getSystemGeneratedCaseDocuments().add(element(
                    CaseDocument.builder()
                        .createdDatetime(createdDate)
                        .documentName(
                            format(
                                N181.getDocumentTitle(),
                                "defendant",
                                caseData.getLegacyCaseReference()
                            )
                        )
                        .build()));
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                                                                                        "ONE"
                );

                assertThat(caseDocument.isPresent()).isEqualTo(false);
            }

            @Test
            void when1v2SolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                    .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses().build().toBuilder()
                    .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                    .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                                                                                            "ONE"
                );

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(N181)
                );
            }

            @Test
            void whenCaseStateIsFullDefence1v2ApplicantProceedsAgainstRes2Only_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2()
                    .build()
                    .toBuilder()
                    .respondent2(PartyBuilder.builder().company().build())
                    .businessProcess(BusinessProcess.builder()
                                         .camundaEvent("CLAIMANT_RESPONSE").build())
                    .applicant1LitigationFriend(LitigationFriend.builder()
                                                    .fullName("Applicant LF")
                                                    .firstName("Applicant")
                                                    .lastName("LF")
                                                    .phoneNumber("1234567890")
                                                    .emailAddress("applicantLF@email.com").build())
                    .respondent2LitigationFriend(LitigationFriend.builder()
                                                     .fullName("respondent 2 LF")
                                                     .firstName("Respondent2")
                                                     .lastName("LF")
                                                     .phoneNumber("123456789")
                                                     .emailAddress("respondent2LF@email.com").build())
                    .build();

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent2Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            private void assertThatDqFieldsAreCorrect(DirectionsQuestionnaireForm templateData, DQ dq,
                                                      CaseData caseData) {
                Assertions.assertAll(
                    "DQ data should be as expected",
                    () -> assertEquals(
                        templateData.getFileDirectionsQuestionnaire(),
                        dq.getFileDirectionQuestionnaire()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfElectronicDocuments(),
                        dq.getDisclosureOfElectronicDocuments()
                    ),
                    () -> assertEquals(
                        templateData.getDisclosureOfNonElectronicDocuments(),
                        dq.getDisclosureOfNonElectronicDocuments()
                    ),
                    () -> assertEquals(templateData.getRespondents(), getRespondent(caseData)),
                    () -> assertEquals(templateData.getApplicant(), getApplicant(caseData)),
                    () -> assertEquals(templateData.getExperts(), getExperts(dq)),
                    () -> assertEquals(templateData.getWitnesses(), getWitnesses(dq)),
                    () -> assertEquals(templateData.getHearing(), getHearing(dq)),
                    () -> assertEquals(templateData.getHearingSupport(), getHearingSupport(dq)),
                    () -> assertEquals(templateData.getWelshLanguageRequirements(), getWelshLanguageRequirements(dq)),
                    () -> assertEquals(templateData.getStatementOfTruth(), dq.getStatementOfTruth()),
                    () -> assertEquals(templateData.getVulnerabilityQuestions(), dq.getVulnerabilityQuestions())
                );
            }

            private Party getApplicant(CaseData caseData) {
                var applicant = caseData.getApplicant1();
                return Party.builder()
                    .name(applicant.getPartyName())
                    .emailAddress(applicant.getPartyEmail())
                    .phoneNumber(applicant.getPartyPhone())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("Applicant LF")
                    .litigationFriendFirstName("Applicant")
                    .litigationFriendLastName("LF")
                    .litigationFriendEmailAddress("applicantLF@email.com")
                    .litigationFriendPhoneNumber("1234567890")
                    .build();
            }

            private List<Party> getRespondent(CaseData caseData) {
                var respondent = caseData.getRespondent2();
                return List.of(Party.builder()
                                   .name(respondent.getPartyName())
                                   .primaryAddress(respondent.getPrimaryAddress())
                                   .phoneNumber(respondent.getPartyPhone())
                                   .emailAddress(respondent.getPartyEmail())
                                   .representative(defendant2Representative)
                                   .litigationFriendName("respondent 2 LF")
                                   .litigationFriendFirstName("Respondent2")
                                   .litigationFriendLastName("LF")
                                   .litigationFriendPhoneNumber("123456789")
                                   .litigationFriendEmailAddress("respondent2LF@email.com")
                                   .build());
            }

            private List<Party> getRespondents(CaseData caseData) {
                var respondent1 = caseData.getRespondent1();
                var respondent2 = caseData.getRespondent2();
                return List.of(
                    Party.builder()
                        .name(respondent1.getPartyName())
                        .phoneNumber(respondent1.getPartyPhone())
                        .emailAddress(respondent1.getPartyEmail())
                        .primaryAddress(respondent1.getPrimaryAddress())
                        .representative(defendant1Representative)
                        .litigationFriendName("Respondent LF")
                        .litigationFriendFirstName("Respondent")
                        .litigationFriendLastName("LF")
                        .litigationFriendEmailAddress("respondentLF@email.com")
                        .litigationFriendPhoneNumber("1234567890")
                        .build(),
                    Party.builder()
                        .name(respondent2.getPartyName())
                        .phoneNumber(respondent2.getPartyPhone())
                        .emailAddress(respondent2.getPartyEmail())
                        .primaryAddress(respondent2.getPrimaryAddress())
                        .representative(defendant2Representative)
                        .litigationFriendName("respondent 2 LF")
                        .litigationFriendFirstName("Respondent2")
                        .litigationFriendLastName("LF")
                        .litigationFriendPhoneNumber("123456789")
                        .litigationFriendEmailAddress("respondent2LF@email.com")
                        .build()
                );
            }

            private Experts getExperts(DQ dq) {
                var experts = dq.getExperts();
                return Experts.builder()
                    .expertRequired(experts.getExpertRequired())
                    .expertReportsSent(
                        ofNullable(experts.getExpertReportsSent())
                            .map(ExpertReportsSent::getDisplayedValue)
                            .orElse(""))
                    .jointExpertSuitable(experts.getJointExpertSuitable())
                    .details(getExpertsDetails(dq))
                    .build();
            }

            private List<Expert> getExpertsDetails(DQ dq) {
                return unwrapElements(dq.getExperts().getDetails())
                    .stream()
                    .map(expert -> Expert.builder()
                        .name(expert.getName())
                        .firstName(expert.getFirstName())
                        .lastName(expert.getLastName())
                        .phoneNumber(expert.getPhoneNumber())
                        .emailAddress(expert.getEmailAddress())
                        .fieldOfExpertise(expert.getFieldOfExpertise())
                        .whyRequired(expert.getWhyRequired())
                        .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                           .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                        .build())
                    .collect(toList());
            }

            private Witnesses getWitnesses(DQ dq) {
                var witnesses = dq.getWitnesses();
                return Witnesses.builder()
                    .witnessesToAppear(witnesses.getWitnessesToAppear())
                    .details(unwrapElements(witnesses.getDetails()))
                    .build();
            }

            private Hearing getHearing(DQ dq) {
                var hearing = dq.getHearing();
                return Hearing.builder()
                    .hearingLength(getHearingLength(dq))
                    .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                    .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                    .build();
            }

            private String getHearingLength(DQ dq) {
                var hearing = dq.getHearing();
                switch (hearing.getHearingLength()) {
                    case LESS_THAN_DAY:
                        return hearing.getHearingLengthHours() + " hours";
                    case ONE_DAY:
                        return "One day";
                    default:
                        return hearing.getHearingLengthDays() + " days";
                }
            }

            private String getHearingSupport(DQ dq) {
                var stringBuilder = new StringBuilder();
                ofNullable(dq.getHearingSupport())
                    .map(HearingSupport::getRequirements)
                    .orElse(List.of())
                    .forEach(requirement -> {
                        var hearingSupport = dq.getHearingSupport();
                        stringBuilder.append(requirement.getDisplayedValue());
                        switch (requirement) {
                            case SIGN_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getSignLanguageRequired());
                                break;
                            case LANGUAGE_INTERPRETER:
                                stringBuilder.append(" - ").append(hearingSupport.getLanguageToBeInterpreted());
                                break;
                            case OTHER_SUPPORT:
                                stringBuilder.append(" - ").append(hearingSupport.getOtherSupport());
                                break;
                            default:
                                break;
                        }
                        stringBuilder.append("\n");
                    });
                return stringBuilder.toString().trim();
            }

            private WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
                var welshLanguageRequirements = dq.getWelshLanguageRequirements();
                return WelshLanguageRequirements.builder()
                    .evidence(ofNullable(
                        welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
                    .court(ofNullable(
                        welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
                    .documents(ofNullable(
                        welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
                    .build();
            }
        }
    }

    @Nested
    class ResponseToDefence {

        @BeforeEach
        void setup() {
            when(representativeService.getRespondent1Representative(any())).thenReturn(defendant1Representative);
            when(representativeService.getRespondent2Representative(any())).thenReturn(defendant2Representative);
        }

        @Test
        void shouldGenerateN181Document_whenTwoApplicantRespondWithOnlyFirstIntendsToProceed() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YES)
                .applicant1ProceedWithClaimMultiParty2v1(YES)
                .applicant2ProceedWithClaimMultiParty2v1(NO)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateN181Document_whenTwoApplicantRespondWithOnlyFirstIntendsToProceed_withHNlToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HNL_DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(HNL_DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YES)
                .applicant1ProceedWithClaimMultiParty2v1(YES)
                .applicant2ProceedWithClaimMultiParty2v1(NO)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                                                                      eq(HNL_DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateN181Document_whenTwoApplicantRespondWithOnlySecondIntendsToProceed() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO)
                .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.YES)
                .applicant2ResponseDate(LocalDateTime.now())
                .applicant2DQ()
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateN181Document_whenOneApplicantIntendsToProceedAgainstOnlyFirstDefendant() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateN181Document_whenOneApplicantIntendsToProceedAgainstOnlySecondDefendant() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
                .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
        }

        @Test
        void shouldGenerateN181Document_whenOneApplicantIntendsToProceedAgainstBothDefendant() {
            when(featureToggleService.isHearingAndListingSDOEnabled()).thenReturn(false);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class),
                eq(N181_MULTIPARTY_SAME_SOL)
            ))
                .thenReturn(new DocmosisDocument(N181_MULTIPARTY_SAME_SOL.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(N181_MULTIPARTY_SAME_SOL)
            );
        }

        @Test
        void shouldGenerateN181Document_whenOneApplicantIntendsToProceedAgainstBothDefendant_withHNLToggleEnabled() {
            when(featureToggleService.isHearingAndListingLegalRepEnabled()).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class),
                eq(HNL_DQ_RESPONSE_1V2_SS)
            ))
                .thenReturn(new DocmosisDocument(HNL_DQ_RESPONSE_1V2_SS.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(HNL_DQ_RESPONSE_1V2_SS)
            );
        }
    }

    @Nested
    class StatementOfTruthText {
        @Test
        void checkStatementOfTruthTextForClaimant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build()
                .toBuilder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("CLAIMANT_RESPONSE").build())
                .build();

            String statementOfTruth = "The claimant believes that the facts in this claim are true."
                + "\n\n\nI am duly authorised by the claimant to sign this statement.\n\n"
                + "The claimant understands that the proceedings for contempt of court "
                + "may be brought against anyone who makes, or causes to be made, "
                + "a false statement in a document verified by a statement of truth "
                + "without an honest belief in its truth.";

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
            assertNotEquals(caseData.getCaseAccessCategory(), SPEC_CLAIM);
            assertEquals(templateData.getStatementOfTruthText(), statementOfTruth);
        }

        @Test
        void checkStatementOfTruthTextForDefendent() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                              .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                              .courtTypeId("10").courtLocationCode("121")
                              .epimmsId("000000").build());
            when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceWithHearingSupport()
                .build()
                .toBuilder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent("DEFENDANT_RESPONSE").build())
                .build();

            String statementOfTruth = "The defendant believes that the facts stated in the response are true."
                + "\n\n\nI am duly authorised by the defendant to sign this statement.\n\n"
                + "The defendant understands that the proceedings for contempt of court "
                + "may be brought against anyone who makes, or causes to be made, "
                + "a false statement in a document verified by a statement of truth "
                + "without an honest belief in its truth.";

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
            assertNotEquals(caseData.getCaseAccessCategory(), SPEC_CLAIM);
            assertEquals(templateData.getStatementOfTruthText(), statementOfTruth);
        }
    }
}
