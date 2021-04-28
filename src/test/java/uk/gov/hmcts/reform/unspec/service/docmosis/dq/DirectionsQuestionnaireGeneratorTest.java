package uk.gov.hmcts.reform.unspec.service.docmosis.dq;

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
import uk.gov.hmcts.reform.unspec.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.unspec.enums.dq.Language;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.common.MappableObject;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Applicant;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Respondent;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.model.dq.DQ;
import uk.gov.hmcts.reform.unspec.model.dq.HearingSupport;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.utils.MonetaryConversions;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.unwrapElements;

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
    private static final String fileName = format(N181.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(DIRECTIONS_QUESTIONNAIRE)
        .build();

    private final Representative representative = Representative.builder().organisationName("test org").build();

    @MockBean
    private DocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @Autowired
    private DirectionsQuestionnaireGenerator generator;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondentRepresentative(any())).thenReturn(representative);
    }

    @Test
    void shouldGenerateCertificateOfService_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N181)))
            .thenReturn(new DocmosisDocument(N181.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DIRECTIONS_QUESTIONNAIRE)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondentRepresentative(caseData);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DIRECTIONS_QUESTIONNAIRE));
        verify(documentGeneratorService).generateDocmosisDocument(any(DirectionsQuestionnaireForm.class), eq(N181));
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenCaseStateIsRespondedToClaim_shouldGetRespondentDQData() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                .build();
            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

            verify(representativeService).getRespondentRepresentative(caseData);
            assertThatDqFieldsAreCorrect(templateData, caseData.getRespondent1DQ(), caseData);
        }

        @Test
        void whenCaseStateIsFullDefence_shouldGetRespondentDQData() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build()
                .toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .respondent1LitigationFriend(LitigationFriend.builder().fullName("respondent LF").build())
                .build();

            DirectionsQuestionnaireForm templateData = generator.getTemplateData(caseData);

            verify(representativeService).getRespondentRepresentative(caseData);
            assertThatDqFieldsAreCorrect(templateData, caseData.getApplicant1DQ(), caseData);
        }

        private void assertThatDqFieldsAreCorrect(DirectionsQuestionnaireForm templateData, DQ dq, CaseData caseData) {
            Assertions.assertAll(
                "DQ data should be as expected",
                () -> assertEquals(templateData.getFileDirectionsQuestionnaire(), dq.getFileDirectionQuestionnaire()),
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
                () -> assertEquals(templateData.getStatementOfTruth(), dq.getStatementOfTruth())
            );
        }

        private Applicant getApplicant(CaseData caseData) {
            Party applicant = caseData.getApplicant1();
            return Applicant.builder()
                .name(applicant.getPartyName())
                .primaryAddress(applicant.getPrimaryAddress())
                .litigationFriendName("applicant LF")
                .build();
        }

        private List<Respondent> getRespondents(CaseData caseData) {
            Party respondent = caseData.getRespondent1();
            return List.of(Respondent.builder()
                               .name(respondent.getPartyName())
                               .primaryAddress(respondent.getPrimaryAddress())
                               .representative(representative)
                               .litigationFriendName("respondent LF")
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
