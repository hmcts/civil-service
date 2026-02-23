package uk.gov.hmcts.reform.civil.service.docmosis.dq;

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
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DeterWithoutHearing;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureReport;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.builders.DQGeneratorFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.GetRespondentsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.SetApplicantsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V1_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_DS_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_SS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_1V2_SS_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_RESPONSE_2V1_FAST_TRACK_INT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DirectionsQuestionnaireGenerator.class,
    JacksonAutoConfiguration.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    CaseDetailsConverter.class,
    SetApplicantsForDQGenerator.class,
    GetRespondentsForDQGenerator.class,
    RespondentTemplateForDQGenerator.class,
    DQGeneratorFormBuilder.class
})
class DirectionsQuestionnaireGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String FILE_NAME_DEFENDANT = format(DQ_RESPONSE_1V1.getDocumentTitle(), "defendant", REFERENCE_NUMBER);
    private static final String HNL_FILE_NAME_DEFENDANT = format(DQ_RESPONSE_1V1.getDocumentTitle(), "defendant", REFERENCE_NUMBER);
    private static final String FILE_NAME_CLAIMANT = format(DQ_RESPONSE_1V1.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final String HNL_FILE_NAME_CLAIMANT = format(DQ_RESPONSE_1V1.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final String HNL_FILE_NAME_CLAIMANT_1v2 = format(DQ_RESPONSE_1V2_DS.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final String FILE_NAME_CLAIMANT_1v2 = format(DQ_RESPONSE_1V2_DS_FAST_TRACK_INT.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
    private static final String FILE_NAME_CLAIMANT_1v2SS = format(DQ_RESPONSE_1V2_SS_FAST_TRACK_INT.getDocumentTitle(), "claimant", REFERENCE_NUMBER);
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
        new Representative()
            .setOrganisationName("test org");

    private final Representative defendant2Representative =
        new Representative()
            .setOrganisationName("test org 2");

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DirectionsQuestionnaireGenerator generator;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @Nested
    class RespondentOne {

        @BeforeEach
        void setup() {
            when(representativeService.getRespondent1Representative(any())).thenReturn(defendant1Representative);
        }

        @Test
        void shouldGenerateRespondentOneCertificateOfService_whenStateFlowIsFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(
                DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateClaimantCertificateOfService_whenStateFlowIsRespondToDefenceAndProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .atStateApplicantRespondToDefenceAndProceed().build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(
                DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateDQ_when2v1ScenarioWithFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_2V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_2V1.getDocumentTitle(), bytes));

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
                eq(DQ_RESPONSE_2V1)
            );
        }

        @Test
        void shouldGenerateDQ_when1v2SameSolicitorScenarioWithFullDefence() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DQ_RESPONSE_1V2_SS)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V2_SS.getDocumentTitle(), bytes));

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
                caseData.setRespondent2Represented(YES);
            }
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DQ_RESPONSE_1V2_SS)
            );
        }

        @Test
        void shouldGenerateDQ_specRespondent() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(),
                "defendant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .responseClaimTrack("FAST_CLAIM")
                .atStateRespondentFullDefence()
                .respondent1DQWithFixedRecoverableCosts()
                .build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT)
            );
        }

        @Test
        void shouldGenerateDQ_specClaimant() {
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
                .responseClaimTrack("FAST_CLAIM")
                .atStateClaimantFullDefence()
                .applicant1DQWithExperts()
                .applicant1DQWithWitnesses()
                .applicant1DQWithHearingSupport()
                .applicant1DQWithFixedRecoverableCosts()
                .build();
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
            caseData.setBusinessProcess(businessProcess);
            caseData.setCaseAccessCategory(SPEC_CLAIM);

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
        void specGenerate_defendantDQ() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(),
                "defendant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .responseClaimTrack("FAST_CLAIM")
                .atStateRespondentFullDefence()
                .respondent1DQWithFixedRecoverableCosts()
                .build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT)
            );
        }

        @Test
        void specGenerateClaimantDQ() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC.getDocumentTitle(),
                "claimant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .responseClaimTrack("FAST_CLAIM")
                .atStateApplicantRespondToDefenceAndProceed()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_CLAIMANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC)
            );
        }

        @Nested
        class MintiToggleIsOn {
            @BeforeEach
            void setup() {
                when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            }

            @Test
            void shouldGenerateDefendantDQWhen1v1() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1_FAST_TRACK_INT)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1_FAST_TRACK_INT.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .setMultiTrackClaim()
                    .build();

                CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1_FAST_TRACK_INT)
                );
            }

            @Test
            void shouldGenerateDefendantDQWhen1v2SS() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V2_SS_FAST_TRACK_INT)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V2_SS_FAST_TRACK_INT.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT_1v2SS, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_CLAIMANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimOneDefendantSolicitor()
                    .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                    .applicantsProceedIntention(YesOrNo.YES)
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                    .build();
                CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT_1v2SS, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V2_SS_FAST_TRACK_INT)
                );
            }

            @Test
            void shouldGenerateClaimantDQWhen1v2DS() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V2_DS_FAST_TRACK_INT)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V2_DS_FAST_TRACK_INT.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT_1v2, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_CLAIMANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                    .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                    .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData.setRespondent2Represented(YES);
                }

                CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT_1v2, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V2_DS_FAST_TRACK_INT)
                );
            }

            @Test
            void shouldGenerateDQWhen2v1() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_2V1_FAST_TRACK_INT)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_2V1_FAST_TRACK_INT.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .build();

                CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_2V1_FAST_TRACK_INT)
                );
            }
        }

        @Nested
        class GetTemplateData {

            @Test
            void whenCaseStateIsRespondedToClaim_shouldGetRespondentDQData() {
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence1v1ApplicantProceeds_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);

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
                    .applicant1DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setResponseClaimTrack("FAST_CLAIM");

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
                assertEquals(
                    templateData.getFixedRecoverableCosts(),
                    FixedRecoverableCostsSection.from(caseData.getApplicant1DQ().getFixedRecoverableCosts())
                );
            }

            @Test
            void whenCaseStateIsFullDefence2v1Applicant1ProceedsLRSpec_shouldGetApplicantDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .multiPartyClaimTwoApplicants()
                    .applicant1DQWithExperts()
                    .applicant1DQWithWitnesses()
                    .applicant1DQWithHearingSupport()
                    .applicant1DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setApplicant1ProceedWithClaimSpec2v1(YES);
                caseData.setAddApplicant2(YES);
                caseData.setResponseClaimTrack("FAST_CLAIM");

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
                assertEquals(
                    templateData.getFixedRecoverableCosts(),
                    FixedRecoverableCostsSection.from(caseData.getApplicant1DQ().getFixedRecoverableCosts())
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
                    .applicant1DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.setApplicant1ProceedWithClaim(YES);
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondentResponseIsSame(YES);
                caseData.setResponseClaimTrack("FAST_CLAIM");

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
                assertEquals(
                    templateData.getFixedRecoverableCosts(),
                    FixedRecoverableCostsSection.from(caseData.getApplicant1DQ().getFixedRecoverableCosts())
                );

            }

            @Test
            void whenCaseStateIsFullDefence1v2_TWO_LR_Applicant1ProceedsLRSpec_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimOneDefendantSolicitor()
                    .atStateApplicantRespondToDefenceAndNotProceed_1v2_DiffSol()
                    .applicant1DQWithLocation()
                    .applicant1DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.setApplicant1ProceedWithClaim(YES);
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setRespondent2SameLegalRepresentative(NO);
                caseData.setResponseClaimTrack("FAST_CLAIM");

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertEquals(
                    templateData.getFileDirectionsQuestionnaire(),
                    caseData.getApplicant1DQ().getFileDirectionQuestionnaire()
                );

                assertEquals(
                    templateData.getFixedRecoverableCosts(),
                    FixedRecoverableCostsSection.from(caseData.getApplicant1DQ().getFixedRecoverableCosts())
                );
            }

            @Test
            void whenCaseStateIsFullDefence1v2ApplicantProceedsAgainstRes1_shouldGetApplicantDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .applicant1DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.setApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence2v1Applicant1Proceeds_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .applicant1DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
            }

            @Test
            void whenCaseStateIsFullDefence2v1Applicant2Proceeds_shouldGetApplicantDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicant2RespondToDefenceAndProceed_2v1()
                    .applicant2DQWithFixedRecoverableCosts()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant2DQ(), caseData);
            }

            @Test
            void whenMultiparty2v1_shouldGetDQData() {

                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .build();
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend applicant2LitigationFriend = new LitigationFriend();
                applicant2LitigationFriend.setFullName("ApplicantTwo LF");
                applicant2LitigationFriend.setFirstName("Applicant2");
                applicant2LitigationFriend.setLastName("LF");
                applicant2LitigationFriend.setPhoneNumber("1234567890");
                applicant2LitigationFriend.setEmailAddress("applicant2LF@email.com");
                caseData.setApplicant2LitigationFriend(applicant2LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                verify(representativeService).getRespondent1Representative(caseData);
                assertThatDqFieldsAreCorrect2v1(templateData, caseData.getRespondent1DQ(), caseData);
            }

            @Test
            void whenNoRequestedCourt_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                Respondent1DQ dq = caseData.getRespondent1DQ();
                dq.setRespondent1DQRequestedCourt(null);
                dq.setResponseClaimCourtLocationRequired(null);
                dq.setRespondToCourtLocation(null);
                caseData.setRespondent1DQ(dq);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
            }

            @Test
            void whenNoExperts_build() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                Respondent1DQ dq = caseData.getRespondent1DQ();
                dq.setRespondent1DQExperts(null);
                caseData.setRespondent1DQ(dq);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
            }

            @Test
            void whenExperts_includeDetails() {
                uk.gov.hmcts.reform.civil.model.dq.Expert expert1 = new uk.gov.hmcts.reform.civil.model.dq.Expert();
                expert1.setFirstName("first");
                expert1.setLastName("last");
                expert1.setPhoneNumber("07123456789");
                expert1.setEmailAddress("test@email.com");
                expert1.setFieldOfExpertise("expertise 1");
                expert1.setWhyRequired("Explanation");
                expert1.setEstimatedCost(BigDecimal.valueOf(10000));
                uk.gov.hmcts.reform.civil.model.dq.Experts experts = new uk.gov.hmcts.reform.civil.model.dq.Experts();
                experts.setExpertRequired(YES);
                experts.setExpertReportsSent(ExpertReportsSent.NOT_OBTAINED);
                experts.setJointExpertSuitable(YES);
                experts.setDetails(ElementUtils.wrapElements(expert1));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQExperts(experts);
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
            void whenIntermediateClaim_shouldUseFixedRecoverableCostsIntermediate() {
                FixedRecoverableCosts frcIntermediate = new FixedRecoverableCosts();
                frcIntermediate.setIsSubjectToFixedRecoverableCostRegime(YES);
                frcIntermediate.setFrcSupportingDocument(new Document());
                frcIntermediate.setComplexityBandingAgreed(YES);
                frcIntermediate.setBand(ComplexityBand.BAND_1);
                frcIntermediate.setReasons("Reasoning");

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.setAllocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM);
                Respondent1DQ dq = caseData.getRespondent1DQ();
                dq.setRespondent1DQFixedRecoverableCosts(null);
                dq.setRespondent1DQFixedRecoverableCostsIntermediate(frcIntermediate);
                caseData.setRespondent1DQ(dq);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                FixedRecoverableCostsSection data = templateData.getFixedRecoverableCosts();
                assertThat(data.getIsSubjectToFixedRecoverableCostRegime()).isEqualTo(YES);
                assertThat(data.getComplexityBandingAgreed()).isEqualTo(YES);
                assertThat(data.getBand()).isEqualTo(ComplexityBand.BAND_1);
                assertThat(data.getBandText()).isEqualTo(ComplexityBand.BAND_1.getLabel());
                assertThat(data.getReasons()).isEqualTo("Reasoning");
            }

            @Test
            void whenDisclosureReport_include() {
                String disclosureOrderNumber = "123";
                DisclosureReport disclosureReport = new DisclosureReport();
                disclosureReport.setDisclosureFormFiledAndServed(YES);
                disclosureReport.setDisclosureProposalAgreed(YES);
                disclosureReport.setDraftOrderNumber(disclosureOrderNumber);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQDisclosureReport(disclosureReport);
                caseData.setAllocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                DisclosureReport extracted = templateData.getDisclosureReport();
                assertThat(extracted.getDraftOrderNumber()).isEqualTo(disclosureOrderNumber);
                assertThat(extracted.getDisclosureProposalAgreed()).isEqualTo(YES);
                assertThat(extracted.getDisclosureFormFiledAndServed()).isEqualTo(YES);
            }

            @Test
            void whenDisclosureReport_include_Minti() {
                when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
                String disclosureOrderNumber = "123";
                DisclosureReport disclosureReport = new DisclosureReport();
                disclosureReport.setDisclosureFormFiledAndServed(YES);
                disclosureReport.setDisclosureProposalAgreed(YES);
                disclosureReport.setDraftOrderNumber(disclosureOrderNumber);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQDisclosureReport(disclosureReport);
                caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                DisclosureReport extracted = templateData.getDisclosureReport();
                assertThat(extracted.getDraftOrderNumber()).isEqualTo(disclosureOrderNumber);
                assertThat(extracted.getDisclosureProposalAgreed()).isEqualTo(YES);
                assertThat(extracted.getDisclosureFormFiledAndServed()).isEqualTo(YES);
            }

            @Test
            void whenDisclosureReport_shouldNotinclude_Minti() {
                when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);
                String disclosureOrderNumber = "123";
                DisclosureReport disclosureReport = new DisclosureReport();
                disclosureReport.setDisclosureFormFiledAndServed(YES);
                disclosureReport.setDisclosureProposalAgreed(YES);
                disclosureReport.setDraftOrderNumber(disclosureOrderNumber);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQDisclosureReport(disclosureReport);
                caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                DisclosureReport extracted = templateData.getDisclosureReport();
                assertThat(extracted).isEqualTo(null);
            }

            @Test
            void whenDisclosureReport_shouldNotinclude_UnspecFast() {
                when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);
                String disclosureOrderNumber = "123";
                DisclosureReport disclosureReport = new DisclosureReport();
                disclosureReport.setDisclosureFormFiledAndServed(YES);
                disclosureReport.setDisclosureProposalAgreed(YES);
                disclosureReport.setDraftOrderNumber(disclosureOrderNumber);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQDisclosureReport(disclosureReport);
                caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
                caseData.setCaseAccessCategory(UNSPEC_CLAIM);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                DisclosureReport extracted = templateData.getDisclosureReport();
                assertThat(extracted).isEqualTo(null);
            }

            @Test
            void whenFurtherInformation_include() {
                FutureApplications futureApplications = new FutureApplications();
                futureApplications.setIntentionToMakeFutureApplications(YES);
                futureApplications.setWhatWillFutureApplicationsBeMadeFor("Reason for future apps");
                FurtherInformation furtherInformation = new FurtherInformation();
                furtherInformation.setOtherInformationForJudge("other info");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQFutureApplications(futureApplications);
                caseData.getRespondent1DQ().setRespondent1DQFurtherInformation(furtherInformation);
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
                caseData.getRespondent1DQ().setRespondent1DQLanguage(null);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWelshLanguageRequirements()).isNotNull();
            }

            @Test
            void whenSmallClaimAndNoWitness() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                uk.gov.hmcts.reform.civil.model.dq.Hearing hearing = new uk.gov.hmcts.reform.civil.model.dq.Hearing();
                hearing.setHearingLength(null);
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                caseData.getRespondent1DQ().setRespondent1DQExperts(null);
                caseData.getRespondent1DQ().setRespondent1DQWitnesses(null);
                caseData.getRespondent1DQ().setRespondent1DQHearing(hearing);
                caseData.getRespondent1DQ().setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(0);
            }

            @Test
            void whenSmallClaimAndWitnesses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                caseData.getRespondent1DQ().setRespondent1DQExperts(null);
                caseData.getRespondent1DQ().setRespondent1DQWitnesses(null);
                caseData.getRespondent1DQ().setRespondent1DQHearing(null);
                caseData.getRespondent1DQ().setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
                int witnessesIncludingDefendant = 2;
                caseData.setResponseClaimWitnesses(Integer.toString(witnessesIncludingDefendant));
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(witnessesIncludingDefendant);
            }

            @Test
            void whenSmallClaimSpecAndWitnessesNoExperts() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                caseData.getRespondent1DQ().setRespondent1DQExperts(null);
                caseData.getRespondent1DQ().setRespondent1DQWitnesses(null);
                caseData.getRespondent1DQ().setRespondent1DQHearing(null);
                caseData.getRespondent1DQ().setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setResponseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM);
                int witnessesIncludingDefendant = 2;
                caseData.setResponseClaimWitnesses(Integer.toString(witnessesIncludingDefendant));
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants())
                    .isEqualTo(witnessesIncludingDefendant);
            }

            @Test
            void whenSmallClaimSpecAndWitnesses() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .applicant1DQWithWitnesses()
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setResponseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM);
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                caseData.getApplicant1DQ().setDeterWithoutHearing(deterWithoutHearing);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnesses())
                    .isEqualTo(applicant1WitnessesMock());
            }

            @Test
            void whenSmallClaimSpecFullAdmissionNoExperts() {
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .atStateRespondent1v1FullAdmissionSpec()
                    .setClaimTypeToSpecClaim()
                    .build();
                caseData.getRespondent1DQ().setRespondent1DQExperts(null);
                caseData.getRespondent1DQ().setRespondent1DQWitnesses(null);
                caseData.getRespondent1DQ().setRespondent1DQHearing(null);
                caseData.getRespondent1DQ().setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
                caseData.setResponseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM);
                caseData.setResponseClaimWitnesses(Integer.toString(2));
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants()).isNull();
            }

            @Test
            void whenSmallClaimSpecFullAdmission() {
                ExpertDetails expertDetails = new ExpertDetails();
                expertDetails.setExpertName("Mr Expert Defendant");
                expertDetails.setFirstName("Expert");
                expertDetails.setLastName("Defendant");
                expertDetails.setPhoneNumber("07123456789");
                expertDetails.setEmailAddress("test@email.com");
                expertDetails.setFieldofExpertise("Roofing");
                expertDetails.setEstimatedCost(new BigDecimal(434));
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .atStateRespondent1v1FullAdmissionSpec()
                    .setClaimTypeToSpecClaim()
                    .build();
                caseData.setResponseClaimExpertSpecRequired(YES);
                caseData.setRespondent1Represented(YES);
                caseData.setSpecRespondent1Represented(YES);
                caseData.getRespondent1DQ().setRespondToClaimExperts(expertDetails);
                caseData.getRespondent1DQ().setRespondent1DQWitnesses(null);
                caseData.getRespondent1DQ().setRespondent1DQHearing(null);
                caseData.getRespondent1DQ().setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
                caseData.setResponseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM);
                caseData.setResponseClaimWitnesses(Integer.toString(2));
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(templateData.getWitnessesIncludingDefendants()).isNull();
                assertThat(!caseData.isRespondent1NotRepresented()).isTrue();
            }

            @Test
            void whenSmallClaimSpecFullAdmissionNotRepresentedDefendant() {

                ExpertDetails expertDetails = new ExpertDetails();
                expertDetails.setExpertName("Mr Expert Defendant");
                expertDetails.setFirstName("Expert");
                expertDetails.setLastName("Defendant");
                expertDetails.setPhoneNumber("07123456789");
                expertDetails.setEmailAddress("test@email.com");
                expertDetails.setFieldofExpertise("Roofing");
                expertDetails.setEstimatedCost(new BigDecimal(434));
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .atStateRespondent1v1FullAdmissionSpec()
                    .setClaimTypeToSpecClaim()
                    .build();
                caseData.setResponseClaimExpertSpecRequired(YES);
                caseData.setRespondent1Represented(NO);
                caseData.setSpecRespondent1Represented(NO);
                caseData.getRespondent1DQ().setRespondToClaimExperts(expertDetails);
                caseData.getRespondent1DQ().setRespondent1DQWitnesses(null);
                caseData.getRespondent1DQ().setRespondent1DQHearing(null);
                caseData.getRespondent1DQ().setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
                caseData.setResponseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM);
                caseData.setResponseClaimWitnesses(Integer.toString(2));
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThat(!caseData.isRespondent1NotRepresented()).isFalse();
            }

            @Test
            void when1V1SpecIntermediate_includeIntermediateFrcDetails() {
                FixedRecoverableCosts frcIntermediate = new FixedRecoverableCosts();
                frcIntermediate.setIsSubjectToFixedRecoverableCostRegime(YES);
                frcIntermediate.setFrcSupportingDocument(new Document());
                frcIntermediate.setComplexityBandingAgreed(YES);
                frcIntermediate.setBand(ComplexityBand.BAND_1);
                frcIntermediate.setReasons("Reasoning");
                DisclosureOfElectronicDocuments disclosureOfElectronic = new DisclosureOfElectronicDocuments();
                disclosureOfElectronic.setReachedAgreement(NO);
                disclosureOfElectronic.setAgreementLikely(NO);
                disclosureOfElectronic.setReasonForNoAgreement("some reasons");
                DisclosureOfNonElectronicDocuments disclosureOfNonElectronic = new DisclosureOfNonElectronicDocuments();
                disclosureOfNonElectronic.setBespokeDirections("non electric stuff");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setResponseClaimTrack("INTERMEDIATE_CLAIM");
                caseData.getRespondent1DQ().setRespondent1DQFixedRecoverableCosts(null);
                caseData.getRespondent1DQ().setRespondent1DQFixedRecoverableCostsIntermediate(frcIntermediate);
                caseData.getRespondent1DQ().setRespondent1DQDisclosureOfElectronicDocuments(disclosureOfElectronic);
                caseData.getRespondent1DQ().setRespondent1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronic);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
                FixedRecoverableCostsSection data = templateData.getFixedRecoverableCosts();
                assertThat(data.getIsSubjectToFixedRecoverableCostRegime()).isEqualTo(YES);
                assertThat(data.getComplexityBandingAgreed()).isEqualTo(YES);
                assertThat(data.getBand()).isEqualTo(ComplexityBand.BAND_1);
                assertThat(data.getBandText()).isEqualTo(ComplexityBand.BAND_1.getLabel());
                assertThat(data.getReasons()).isEqualTo("Reasoning");
            }

            @Test
            void when1V1SpecMulti_DoNotIncludeIntermediateFrcDetails() {
                DisclosureOfElectronicDocuments disclosureOfElectronic = new DisclosureOfElectronicDocuments();
                disclosureOfElectronic.setReachedAgreement(NO);
                disclosureOfElectronic.setAgreementLikely(NO);
                disclosureOfElectronic.setReasonForNoAgreement("some reasons");
                DisclosureOfNonElectronicDocuments disclosureOfNonElectronic = new DisclosureOfNonElectronicDocuments();
                disclosureOfNonElectronic.setBespokeDirections("non electric stuff");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                caseData.setCaseAccessCategory(SPEC_CLAIM);
                caseData.setResponseClaimTrack("MULTI_CLAIM");
                caseData.getRespondent1DQ().setRespondent1DQFixedRecoverableCosts(null);
                caseData.getRespondent1DQ().setRespondent1DQFixedRecoverableCostsIntermediate(null);
                caseData.getRespondent1DQ().setRespondent1DQDisclosureOfElectronicDocuments(disclosureOfElectronic);
                caseData.getRespondent1DQ().setRespondent1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronic);

                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);
                FixedRecoverableCostsSection data = templateData.getFixedRecoverableCosts();
                assertThat(data).isNull();
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
                    () -> assertEquals(templateData.getVulnerabilityQuestions(), dq.getVulnerabilityQuestions()),
                    () -> assertEquals(templateData.getFixedRecoverableCosts(),
                        FixedRecoverableCostsSection.from(dq.getFixedRecoverableCosts()))
                );
            }

            private Party getApplicant(CaseData caseData) {
                var applicant = caseData.getApplicant1();
                return new Party()
                    .setName(applicant.getPartyName())
                    .setEmailAddress(applicant.getPartyEmail())
                    .setPhoneNumber(applicant.getPartyPhone())
                    .setPrimaryAddress(applicant.getPrimaryAddress())
                    .setLitigationFriendName("Applicant LF")
                    .setLitigationFriendFirstName("Applicant")
                    .setLitigationFriendLastName("LF")
                    .setLitigationFriendEmailAddress("applicantLF@email.com")
                    .setLitigationFriendPhoneNumber("1234567890")
                    .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                        ? "Name" : "Organisation name");
            }

            private Party getApplicant2(CaseData caseData) {
                var applicant = caseData.getApplicant2();
                return new Party()
                    .setName(applicant.getPartyName())
                    .setEmailAddress(applicant.getPartyEmail())
                    .setPhoneNumber(applicant.getPartyPhone())
                    .setPrimaryAddress(applicant.getPrimaryAddress())
                    .setLitigationFriendName("ApplicantTwo LF")
                    .setLitigationFriendFirstName("Applicant2")
                    .setLitigationFriendLastName("LF")
                    .setLitigationFriendEmailAddress("applicant2LF@email.com")
                    .setLitigationFriendPhoneNumber("1234567890")
                    .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                        ? "Name" : "Organisation name");
            }

            private List<Party> getRespondents(CaseData caseData) {
                var respondent = caseData.getRespondent1();
                return List.of(new Party()
                    .setName(respondent.getPartyName())
                    .setPhoneNumber(respondent.getPartyPhone())
                    .setEmailAddress(respondent.getPartyEmail())
                    .setPrimaryAddress(respondent.getPrimaryAddress())
                    .setRepresentative(defendant1Representative)
                    .setLitigationFriendName("Respondent LF")
                    .setLitigationFriendFirstName("Respondent")
                    .setLitigationFriendLastName("LF")
                    .setLitigationFriendEmailAddress("respondentLF@email.com")
                    .setLitigationFriendPhoneNumber("1234567890")
                    .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                        ? "Name" : "Organisation name"));
            }

            private Experts getExperts(DQ dq) {
                var experts = dq.getExperts();
                return new Experts()
                    .setExpertRequired(experts.getExpertRequired())
                    .setExpertReportsSent(
                        ofNullable(experts.getExpertReportsSent())
                            .map(ExpertReportsSent::getDisplayedValue)
                            .orElse(""))
                    .setJointExpertSuitable(experts.getJointExpertSuitable())
                    .setDetails(getExpertsDetails(dq));
            }

            private List<Expert> getExpertsDetails(DQ dq) {
                return unwrapElements(dq.getExperts().getDetails())
                    .stream()
                    .map(expert -> new Expert()
                        .setName(expert.getName())
                        .setFirstName(expert.getFirstName())
                        .setLastName(expert.getLastName())
                        .setPhoneNumber(expert.getPhoneNumber())
                        .setEmailAddress(expert.getEmailAddress())
                        .setFieldOfExpertise(expert.getFieldOfExpertise())
                        .setWhyRequired(expert.getWhyRequired())
                        .setFormattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                            .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost()))))
                    .collect(toList());
            }

            private Witnesses getWitnesses(DQ dq) {
                var witnesses = dq.getWitnesses();
                return new Witnesses()
                    .setWitnessesToAppear(witnesses.getWitnessesToAppear())
                    .setDetails(unwrapElements(witnesses.getDetails()));
            }

            private Hearing getHearing(DQ dq) {
                var hearing = dq.getHearing();
                return new Hearing()
                    .setHearingLength(getHearingLength(dq))
                    .setUnavailableDatesRequired(hearing.getUnavailableDatesRequired())
                    .setUnavailableDates(unwrapElements(hearing.getUnavailableDates()));
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
                HearingSupport hearingSupport = new HearingSupport();
                hearingSupport.setRequirements(List.of());
                hearingSupport.setSupportRequirements(YES);
                hearingSupport.setSupportRequirementsAdditional("Additional support needed");
                return hearingSupport;
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
                return new WelshLanguageRequirements()
                    .setEvidence(ofNullable(
                        welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
                    .setCourt(ofNullable(
                        welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
                    .setDocuments(ofNullable(
                        welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""));
            }

            private Experts applicant1ExpertsMock() {
                return new Experts()
                    .setExpertRequired(YES)
                    .setExpertReportsSent(ExpertReportsSent.NO.getDisplayedValue())
                    .setJointExpertSuitable(NO)
                    .setDetails(List.of(
                            new uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert()
                                .setFirstName("Expert")
                                .setLastName("One")
                                .setPhoneNumber("01482764322")
                                .setEmailAddress("fast.claim.expert1@example.com")
                                .setWhyRequired("Good reasons")
                                .setFieldOfExpertise("Some field")
                                .setFormattedCost("£100.00")
                        )
                    );
            }

            private Witnesses applicant1WitnessesMock() {
                return new Witnesses()
                    .setWitnessesToAppear(YES)
                    .setDetails(List.of(
                        new Witness()
                            .setFirstName("Witness")
                            .setLastName("One")
                            .setPhoneNumber("01482764322")
                            .setEmailAddress("witness.one@example.com")
                            .setReasonForWitness("Saw something")));
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
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData.setRespondent2Represented(YES);
            }
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent2Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                eq(DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateClaimantDQ_for1v2_DS() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V2_DS)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V2_DS.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT_1v2, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_DEFENDANT);

            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                .respondent2SameLegalRepresentative(NO)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData.setRespondent2Represented(YES);
            }
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_DEFENDANT);

            verify(representativeService).getRespondent2Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT_1v2, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                eq(DQ_RESPONSE_1V2_DS));
        }

        @Test
        void shouldGenerateClaimantCertificateOfService_whenStateFlowIsRespondToDefenceAndProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));

            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .applicant1DQWithLocation()
                .build();

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(representativeService).getRespondent1Representative(caseData);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(
                DQ_RESPONSE_1V1));
        }

        @Nested
        class GetTemplateData {

            @Test
            void whenRespondent2Response_shouldGetRespondentDQData() {
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2DQWithFixedRecoverableCosts()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder()
                                     .individual()
                                     .legalRepHeading()
                                     .build())
                    .build();
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent 2 LF");
                respondent2LitigationFriend.setFirstName("Respondent2");
                respondent2LitigationFriend.setLastName("LF");
                respondent2LitigationFriend.setPhoneNumber("123456789");
                respondent2LitigationFriend.setEmailAddress("respondent2LF@email.com");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent2DQ(), caseData);
            }

            @Test
            void whenRespondent2LaterResponse_shouldGetRespondentDQData() {
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2DQWithFixedRecoverableCosts()
                    .respondent1ResponseDate(null)
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder()
                                     .individual()
                                     .legalRepHeading()
                                     .build())
                    .build();
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent 2 LF");
                respondent2LitigationFriend.setFirstName("Respondent2");
                respondent2LitigationFriend.setLastName("LF");
                respondent2LitigationFriend.setPhoneNumber("123456789");
                respondent2LitigationFriend.setEmailAddress("respondent2LF@email.com");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent2DQ(), caseData);
            }

            @Test
            void whenRespondent2SameLegalRepAndRespondentResponseSame_shouldGetRespondentDQData() {
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent1DQWithFixedRecoverableCosts()
                    .build();
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("Respondent LF");
                respondent1LitigationFriend.setFirstName("Respondent");
                respondent1LitigationFriend.setLastName("LF");
                respondent1LitigationFriend.setPhoneNumber("1234567890");
                respondent1LitigationFriend.setEmailAddress("respondentLF@email.com");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent 2 LF");
                respondent2LitigationFriend.setFirstName("Respondent2");
                respondent2LitigationFriend.setLastName("LF");
                respondent2LitigationFriend.setPhoneNumber("123456789");
                respondent2LitigationFriend.setEmailAddress("respondent2LF@email.com");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                caseData.setRespondent1ResponseDate(LocalDateTime.now());
                caseData.setRespondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build());
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondentResponseIsSame(YES);
                DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

                assertEquals(templateData.getRespondents(), getRespondents(caseData));
                assertEquals(
                    FixedRecoverableCostsSection.from(caseData.getRespondent1DQ().getFixedRecoverableCosts()),
                    templateData.getFixedRecoverableCosts()
                );
            }

            @Test
            void when1v2SolRespondsTo2ndDefendantWithDivergentResponse_shouldGetRespondentDQData() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData.setRespondent2Represented(YES);
                }
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                    "TWO"
                );

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo2ndDefendantWithDivergentResponse_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData.setRespondent2Represented(YES);
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "TWO"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent1ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData.setRespondent2Represented(YES);
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "ONE"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData_Minti() {
                when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1_FAST_TRACK_INT)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1_FAST_TRACK_INT.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent1ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData.setRespondent2Represented(YES);
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "ONE"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1_FAST_TRACK_INT)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo1stDefendantWithDivergentResponseSmallClaim_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent1ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .responseClaimTrack("SMALL_CLAIM")
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                DeterWithoutHearing deterWithoutHearing = new DeterWithoutHearing();
                deterWithoutHearing.setDeterWithoutHearingYesNo(YES);
                Respondent1DQ respondent1DQ = new Respondent1DQ();
                respondent1DQ.setDeterWithoutHearingRespondent1(deterWithoutHearing);
                caseData.setRespondent1DQ(respondent1DQ);
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData.setRespondent2Represented(YES);
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "ONE"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo2stDefendantWithDivergentResponse_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant2LitigationFriend = new LitigationFriend();
                applicant2LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant2LitigationFriend(applicant2LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData.setRespondent2Represented(YES);
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "TWO"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2DiffSolRespondsTo2ndDefendantWithDivergentResponseSmallClaim_shouldGetRespondentDQData() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .responseClaimTrack("SMALL_CLAIM")
                    .build();
                LitigationFriend applicant2LitigationFriend = new LitigationFriend();
                applicant2LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant2LitigationFriend(applicant2LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                DeterWithoutHearing deterWithoutHearing1 = new DeterWithoutHearing();
                deterWithoutHearing1.setDeterWithoutHearingYesNo(YES);
                Respondent1DQ respondent1DQ = new Respondent1DQ();
                respondent1DQ.setDeterWithoutHearingRespondent1(deterWithoutHearing1);
                caseData.setRespondent1DQ(respondent1DQ);
                DeterWithoutHearing deterWithoutHearing2 = new DeterWithoutHearing();
                deterWithoutHearing2.setDeterWithoutHearingYesNo(YES);
                Respondent2DQ respondent2DQ = new Respondent2DQ();
                respondent2DQ.setDeterWithoutHearingRespondent2(deterWithoutHearing2);
                caseData.setRespondent2DQ(respondent2DQ);
                if (caseData.getRespondent2OrgRegistered() != null) {
                    caseData.setRespondent2Represented(YES);
                }
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "TWO"
                );

                assertThat(caseDocument.get()).isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2DiffSol_shouldAcceptOneOrTwo() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(createdDate)
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant2LitigationFriend = new LitigationFriend();
                applicant2LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant2LitigationFriend(applicant2LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN, null)
                );
            }

            @Test
            void when1v2DiffSol2withoutResponseDate_shouldFail() {

                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2(PartyBuilder.builder()
                        .individual()
                        .legalRepHeading()
                        .build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .respondent2ResponseDate(null)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant2LitigationFriend = new LitigationFriend();
                applicant2LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant2LitigationFriend(applicant2LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);
                Assertions.assertThrows(
                    NullPointerException.class,
                    () -> generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN, "TWO")
                );
            }

            @Test
            void when1v2DiffSolDocAlreadyGenerated_shouldNotRegenerate() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                LocalDateTime createdDate = LocalDateTime.parse("2020-07-16T14:05:15.000550439");
                CaseData caseData = CaseDataBuilder.builder()
                    .legacyCaseReference("reference")
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent1ResponseDate(createdDate)
                    .respondent1(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .systemGeneratedCaseDocuments(new ArrayList<>())
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                caseData.getSystemGeneratedCaseDocuments().add(element(
                    new CaseDocument()
                        .setCreatedDatetime(createdDate)
                        .setDocumentName(
                            format(
                                DQ_RESPONSE_1V1.getDocumentTitle(),
                                "defendant",
                                caseData.getLegacyCaseReference()
                            )
                        )));
                Optional<CaseDocument> caseDocument = generator.generateDQFor1v2DiffSol(caseData, BEARER_TOKEN,
                    "ONE"
                );

                assertThat(caseDocument.isPresent()).isEqualTo(false);
            }

            @Test
            void when1v2SolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData() {
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData.setRespondent2Represented(YES);
                }
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                    "ONE"
                );

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1)
                );
            }

            @Test
            void when1v2SolRespondsTo1stDefendantWithDivergentResponse_shouldGetRespondentDQData_Minti() {
                when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
                when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1_FAST_TRACK_INT)))
                    .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1_FAST_TRACK_INT.getDocumentTitle(), bytes));
                when(documentManagementService.uploadDocument(
                    BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE))
                ).thenReturn(CASE_DOCUMENT_DEFENDANT);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .respondent2ResponseDate(LocalDateTime.now())
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .respondentResponseIsSame(YesOrNo.NO)
                    .build();
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("applicant LF");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent1LitigationFriend = new LitigationFriend();
                respondent1LitigationFriend.setFullName("respondent LF");
                caseData.setRespondent1LitigationFriend(respondent1LitigationFriend);
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData.setRespondent2Represented(YES);
                }
                CaseDocument caseDocument = generator.generateDQFor1v2SingleSolDiffResponse(caseData, BEARER_TOKEN,
                    "ONE"
                );

                assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_DEFENDANT);

                verify(documentManagementService)
                    .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DEFENDANT, bytes, DIRECTIONS_QUESTIONNAIRE));
                verify(documentGeneratorService).generateDocmosisDocument(
                    any(DirectionsQuestionnaireForm.class),
                    eq(DQ_RESPONSE_1V1_FAST_TRACK_INT)
                );
            }

            @Test
            void whenCaseStateIsFullDefence1v2ApplicantProceedsAgainstRes2Only_shouldGetRespondentDQData() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2()
                    .respondent2(PartyBuilder.builder().company().build())
                    .build();
                BusinessProcess businessProcess = new BusinessProcess();
                businessProcess.setCamundaEvent("CLAIMANT_RESPONSE");
                caseData.setBusinessProcess(businessProcess);
                LitigationFriend applicant1LitigationFriend = new LitigationFriend();
                applicant1LitigationFriend.setFullName("Applicant LF");
                applicant1LitigationFriend.setFirstName("Applicant");
                applicant1LitigationFriend.setLastName("LF");
                applicant1LitigationFriend.setPhoneNumber("1234567890");
                applicant1LitigationFriend.setEmailAddress("applicantLF@email.com");
                caseData.setApplicant1LitigationFriend(applicant1LitigationFriend);
                LitigationFriend respondent2LitigationFriend = new LitigationFriend();
                respondent2LitigationFriend.setFullName("respondent 2 LF");
                respondent2LitigationFriend.setFirstName("Respondent2");
                respondent2LitigationFriend.setLastName("LF");
                respondent2LitigationFriend.setPhoneNumber("123456789");
                respondent2LitigationFriend.setEmailAddress("respondent2LF@email.com");
                caseData.setRespondent2LitigationFriend(respondent2LitigationFriend);

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
                    () -> assertEquals(templateData.getVulnerabilityQuestions(), dq.getVulnerabilityQuestions()),
                    () -> assertEquals(templateData.getFixedRecoverableCosts(), FixedRecoverableCostsSection.from(dq.getFixedRecoverableCosts()))
                );
            }

            private Party getApplicant(CaseData caseData) {
                var applicant = caseData.getApplicant1();
                return new Party()
                    .setName(applicant.getPartyName())
                    .setEmailAddress(applicant.getPartyEmail())
                    .setPhoneNumber(applicant.getPartyPhone())
                    .setPrimaryAddress(applicant.getPrimaryAddress())
                    .setLitigationFriendName("Applicant LF")
                    .setLitigationFriendFirstName("Applicant")
                    .setLitigationFriendLastName("LF")
                    .setLitigationFriendEmailAddress("applicantLF@email.com")
                    .setLitigationFriendPhoneNumber("1234567890")
                    .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                        ? "Name" : "Organisation name");
            }

            private List<Party> getRespondent(CaseData caseData) {
                var respondent = caseData.getRespondent2();
                return List.of(new Party()
                    .setName(respondent.getPartyName())
                    .setPrimaryAddress(respondent.getPrimaryAddress())
                    .setPhoneNumber(respondent.getPartyPhone())
                    .setEmailAddress(respondent.getPartyEmail())
                    .setRepresentative(defendant2Representative)
                    .setLitigationFriendName("respondent 2 LF")
                    .setLitigationFriendFirstName("Respondent2")
                    .setLitigationFriendLastName("LF")
                    .setLitigationFriendPhoneNumber("123456789")
                    .setLitigationFriendEmailAddress("respondent2LF@email.com")
                    .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                        ? "Name" : "Organisation name"));
            }

            private List<Party> getRespondents(CaseData caseData) {
                var respondent1 = caseData.getRespondent1();
                var respondent2 = caseData.getRespondent2();
                return List.of(
                    new Party()
                        .setName(respondent1.getPartyName())
                        .setPhoneNumber(respondent1.getPartyPhone())
                        .setEmailAddress(respondent1.getPartyEmail())
                        .setPrimaryAddress(respondent1.getPrimaryAddress())
                        .setRepresentative(defendant1Representative)
                        .setLitigationFriendName("Respondent LF")
                        .setLitigationFriendFirstName("Respondent")
                        .setLitigationFriendLastName("LF")
                        .setLitigationFriendEmailAddress("respondentLF@email.com")
                        .setLitigationFriendPhoneNumber("1234567890")
                        .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                            ? "Name" : "Organisation name"),
                    new Party()
                        .setName(respondent2.getPartyName())
                        .setPhoneNumber(respondent2.getPartyPhone())
                        .setEmailAddress(respondent2.getPartyEmail())
                        .setPrimaryAddress(respondent2.getPrimaryAddress())
                        .setRepresentative(defendant2Representative)
                        .setLitigationFriendName("respondent 2 LF")
                        .setLitigationFriendFirstName("Respondent2")
                        .setLitigationFriendLastName("LF")
                        .setLitigationFriendPhoneNumber("123456789")
                        .setLitigationFriendEmailAddress("respondent2LF@email.com")
                        .setLegalRepHeading(caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
                            ? "Name" : "Organisation name")
                );
            }

            private Experts getExperts(DQ dq) {
                var experts = dq.getExperts();
                return new Experts()
                    .setExpertRequired(experts.getExpertRequired())
                    .setExpertReportsSent(
                        ofNullable(experts.getExpertReportsSent())
                            .map(ExpertReportsSent::getDisplayedValue)
                            .orElse(""))
                    .setJointExpertSuitable(experts.getJointExpertSuitable())
                    .setDetails(getExpertsDetails(dq));
            }

            private List<Expert> getExpertsDetails(DQ dq) {
                return unwrapElements(dq.getExperts().getDetails())
                    .stream()
                    .map(expert -> new Expert()
                        .setName(expert.getName())
                        .setFirstName(expert.getFirstName())
                        .setLastName(expert.getLastName())
                        .setPhoneNumber(expert.getPhoneNumber())
                        .setEmailAddress(expert.getEmailAddress())
                        .setFieldOfExpertise(expert.getFieldOfExpertise())
                        .setWhyRequired(expert.getWhyRequired())
                        .setFormattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                            .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost()))))
                    .collect(toList());
            }

            private Witnesses getWitnesses(DQ dq) {
                var witnesses = dq.getWitnesses();
                return new Witnesses()
                    .setWitnessesToAppear(witnesses.getWitnessesToAppear())
                    .setDetails(unwrapElements(witnesses.getDetails()));
            }

            private Hearing getHearing(DQ dq) {
                var hearing = dq.getHearing();
                return new Hearing()
                    .setHearingLength(getHearingLength(dq))
                    .setUnavailableDatesRequired(hearing.getUnavailableDatesRequired())
                    .setUnavailableDates(unwrapElements(hearing.getUnavailableDates()));
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
                return new WelshLanguageRequirements()
                    .setEvidence(ofNullable(
                        welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
                    .setCourt(ofNullable(
                        welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
                    .setDocuments(ofNullable(
                        welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""));
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
        void shouldGenerateResponseDocument_whenTwoApplicantRespondWithOnlyFirstIntendsToProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .applicantsProceedIntention(YES)
                .applicant1ProceedWithClaimMultiParty2v1(YES)
                .applicant2ProceedWithClaimMultiParty2v1(NO)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(HNL_CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(HNL_FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class),
                eq(DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateResponseDocument_whenTwoApplicantRespondWithOnlySecondIntendsToProceed() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
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
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(
                DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateResponseDocument_whenOneApplicantIntendsToProceedAgainstOnlyFirstDefendant() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(
                DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateResponseDocument_whenOneApplicantIntendsToProceedAgainstOnlySecondDefendant() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DQ_RESPONSE_1V1)))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V1.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .applicantsProceedIntention(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);

            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(
                DQ_RESPONSE_1V1));
        }

        @Test
        void shouldGenerateResponseDocument_whenOneApplicantIntendsToProceedAgainstBothDefendant() {
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class),
                eq(DQ_RESPONSE_1V2_SS)
            ))
                .thenReturn(new DocmosisDocument(DQ_RESPONSE_1V2_SS.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(FILE_NAME_CLAIMANT, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(HNL_CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimOneDefendantSolicitor()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
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
                eq(DQ_RESPONSE_1V2_SS)
            );
        }

        @Test
        void specGenerateClaimantDQ_MultiTrack_MintiEnabled() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(),
                                          "claimant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .responseClaimTrack("MULTI_CLAIM")
                .atStateApplicantRespondToDefenceAndProceed()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT)
            );
        }

        @Test
        void specGenerateClaimantDQ_IntTrack_MintiEnabled() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class), eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(), bytes));

            String expectedTitle = format(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT.getDocumentTitle(),
                                          "claimant", REFERENCE_NUMBER
            );
            when(documentManagementService.uploadDocument(
                BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE))
            ).thenReturn(CASE_DOCUMENT_CLAIMANT);

            CaseData caseData = CaseDataBuilder.builder()
                .responseClaimTrack("INTERMEDIATE_CLAIM")
                .atStateApplicantRespondToDefenceAndProceed()
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
                .build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);

            CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

            assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT_CLAIMANT);
            verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(expectedTitle, bytes, DIRECTIONS_QUESTIONNAIRE));
            verify(documentGeneratorService).generateDocmosisDocument(
                any(DirectionsQuestionnaireForm.class),
                eq(DocmosisTemplates.CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT)
            );
        }

        @Test
        void whenIntermediateClaim_shouldUseFixedRecoverableCosts_ClaimantDQ() {
            FixedRecoverableCosts frcIntermediate = new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setFrcSupportingDocument(new Document())
                .setComplexityBandingAgreed(YES)
                .setBand(ComplexityBand.BAND_1)
                .setReasons("Reasoning");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            caseData.setAllocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM);
            caseData.getApplicant1DQ().setApplicant1DQFixedRecoverableCosts(null);
            caseData.getApplicant1DQ().setApplicant1DQFixedRecoverableCostsIntermediate(frcIntermediate);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
            caseData.setBusinessProcess(businessProcess);

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

            FixedRecoverableCostsSection data = templateData.getFixedRecoverableCosts();
            assertThat(data.getIsSubjectToFixedRecoverableCostRegime()).isEqualTo(YES);
            assertThat(data.getComplexityBandingAgreed()).isEqualTo(YES);
            assertThat(data.getBand()).isEqualTo(ComplexityBand.BAND_1);
            assertThat(data.getBandText()).isEqualTo(ComplexityBand.BAND_1.getLabel());
            assertThat(data.getReasons()).isEqualTo("Reasoning");
        }

        @Test
        void whenMultiClaim_shouldNotUseFixedRecoverableCosts_ClaimantDQ() {
            FixedRecoverableCosts frcIntermediate = new FixedRecoverableCosts();
            frcIntermediate.setIsSubjectToFixedRecoverableCostRegime(YES);
            frcIntermediate.setFrcSupportingDocument(new Document());
            frcIntermediate.setComplexityBandingAgreed(YES);
            frcIntermediate.setBand(ComplexityBand.BAND_1);
            frcIntermediate.setReasons("Reasoning");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
            caseData.getApplicant1DQ().setApplicant1DQFixedRecoverableCosts(null);
            caseData.getApplicant1DQ().setApplicant1DQFixedRecoverableCostsIntermediate(frcIntermediate);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
            caseData.setBusinessProcess(businessProcess);

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

            FixedRecoverableCostsSection data = templateData.getFixedRecoverableCosts();
            assertThat(data).isNull();
        }

        @Test
        void shouldIncludeDisclosureDocInfo_ClaimantDQ_MultiTrack_Minti() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            DisclosureOfElectronicDocuments disclosureOfElectronic = new DisclosureOfElectronicDocuments();
            disclosureOfElectronic.setReachedAgreement(NO);
            disclosureOfElectronic.setAgreementLikely(NO);
            disclosureOfElectronic.setReasonForNoAgreement("some reasons");
            DisclosureOfNonElectronicDocuments disclosureOfNonElectronic = new DisclosureOfNonElectronicDocuments();
            disclosureOfNonElectronic.setBespokeDirections("non electric stuff");
            DisclosureReport disclosureReportObj = new DisclosureReport();
            disclosureReportObj.setDisclosureFormFiledAndServed(YES);
            disclosureReportObj.setDisclosureProposalAgreed(YES);

            String disclosureOrderNumber = "123";
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            disclosureReportObj.setDraftOrderNumber(disclosureOrderNumber);
            caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
            caseData.getApplicant1DQ().setApplicant1DQDisclosureOfElectronicDocuments(disclosureOfElectronic);
            caseData.getApplicant1DQ().setApplicant1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronic);
            caseData.getApplicant1DQ().setApplicant1DQDisclosureReport(disclosureReportObj);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
            caseData.setBusinessProcess(businessProcess);

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

            DisclosureOfElectronicDocuments disclosureElecDocs = templateData.getDisclosureOfElectronicDocuments();
            DisclosureOfNonElectronicDocuments disclosureNonElecDocs = templateData.getDisclosureOfNonElectronicDocuments();
            DisclosureReport disclosureReport = templateData.getDisclosureReport();

            assertThat(disclosureElecDocs.getReachedAgreement()).isEqualTo(NO);
            assertThat(disclosureElecDocs.getAgreementLikely()).isEqualTo(NO);
            assertThat(disclosureElecDocs.getReasonForNoAgreement()).isEqualTo("some reasons");
            assertThat(disclosureNonElecDocs.getBespokeDirections()).isEqualTo("non electric stuff");
            assertThat(disclosureReport.getDisclosureFormFiledAndServed()).isEqualTo(YES);
            assertThat(disclosureReport.getDisclosureProposalAgreed()).isEqualTo(YES);
            assertThat(disclosureReport.getDraftOrderNumber()).isEqualTo(disclosureOrderNumber);
        }

        @Test
        void shouldNotIncludeDicslosureReport_ClaimantDQ_MultiTrack_MintiNotEnabled() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

            DisclosureOfElectronicDocuments disclosureOfElectronic = new DisclosureOfElectronicDocuments();
            disclosureOfElectronic.setReachedAgreement(NO);
            disclosureOfElectronic.setAgreementLikely(NO);
            disclosureOfElectronic.setReasonForNoAgreement("some reasons");
            DisclosureOfNonElectronicDocuments disclosureOfNonElectronic = new DisclosureOfNonElectronicDocuments();
            disclosureOfNonElectronic.setBespokeDirections("non electric stuff");
            DisclosureReport disclosureReportObj = new DisclosureReport();
            disclosureReportObj.setDisclosureFormFiledAndServed(YES);
            disclosureReportObj.setDisclosureProposalAgreed(YES);

            String disclosureOrderNumber = "123";
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            disclosureReportObj.setDraftOrderNumber(disclosureOrderNumber);
            caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
            caseData.getApplicant1DQ().setApplicant1DQDisclosureOfElectronicDocuments(disclosureOfElectronic);
            caseData.getApplicant1DQ().setApplicant1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronic);
            caseData.getApplicant1DQ().setApplicant1DQDisclosureReport(disclosureReportObj);
            BusinessProcess businessProcess = new BusinessProcess();
            businessProcess.setCamundaEvent("CLAIMANT_RESPONSE_SPEC");
            caseData.setBusinessProcess(businessProcess);

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData, BEARER_TOKEN);

            DisclosureOfElectronicDocuments disclosureElecDocs = templateData.getDisclosureOfElectronicDocuments();
            DisclosureOfNonElectronicDocuments disclosureNonElecDocs = templateData.getDisclosureOfNonElectronicDocuments();
            DisclosureReport disclosureReport = templateData.getDisclosureReport();

            assertThat(disclosureElecDocs.getReachedAgreement()).isEqualTo(NO);
            assertThat(disclosureElecDocs.getAgreementLikely()).isEqualTo(NO);
            assertThat(disclosureElecDocs.getReasonForNoAgreement()).isEqualTo("some reasons");
            assertThat(disclosureNonElecDocs.getBespokeDirections()).isEqualTo("non electric stuff");
            assertThat(disclosureReport).isEqualTo(null);
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
                .businessProcess(new BusinessProcess().setCamundaEvent("CLAIMANT_RESPONSE"))
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
                .businessProcess(new BusinessProcess().setCamundaEvent("DEFENDANT_RESPONSE"))
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
