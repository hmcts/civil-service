package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertReportLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DisabilityRequirement;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.ExpertReportTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExperts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQ;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQEvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DocumentsToBeConsidered;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DQLipClaimantFormMapperTest {

    @Mock
    private CaseData caseData;
    @Mock
    private Party applicant1;
    @InjectMocks
    private DQLipClaimantFormMapper dqLipClaimantFormMapper;
    private DirectionsQuestionnaireForm form;
    private static final String NAME = "Claimant";

    @BeforeEach
    void setUp() {
        form = new DirectionsQuestionnaireForm();
    }

    @Test
    void shouldNotPopulateDtoWithLipData_whenCaseDataLipIsNull() {
        Optional<CaseDataLiP> emptyOptional = Optional.empty();

        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, emptyOptional);

        assertThat(resultForm.getLipExtraDQ()).isNull();
    }

    @Test
    void shouldNotPopulateLipExtraDQ_whenDQExtraDetailsLipIsNull() {
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(new CaseDataLiP()
                                                                    .setApplicant1LiPResponse(new ClaimantLiPResponse()));

        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, caseDataLiPOptional);

        assertThat(resultForm.getLipExtraDQ()).isNull();
    }

    @Test
    void shouldPopulateClaimantLipSpecificDetails() {
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(new CaseDataLiP().setApplicant1LiPResponse(
            new ClaimantLiPResponse()
                .setApplicant1DQExtraDetails(claimantExtraDetails())
                .setApplicant1DQEvidenceConfirmDetails(claimantEvidenceConfirmDetails())
                .setApplicant1DQHearingSupportLip(claimantHearingSupport())
        ));

        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, caseDataLiPOptional);

        assertThat(resultForm.getHearingLipSupportRequirements()).containsExactly(
            new HearingLipSupportRequirements()
                .setName("Accessible hearing")
                .setOtherSupport("Wheelchair space")
                .setRequirements(List.of(
                    new DisabilityRequirement(SupportRequirements.DISABLED_ACCESS.getDisplayedValue()),
                    new DisabilityRequirement("Wheelchair space")
                ))
        );
        assertThat(resultForm.getLipExtraDQ()).isEqualTo(new LipExtraDQ()
            .setWantPhoneOrVideoHearing(YesOrNo.NO)
            .setWhyPhoneOrVideoHearing("Prefers in-person hearing")
            .setGiveEvidenceYourSelf(YesOrNo.YES)
            .setTriedToSettle(YesOrNo.NO)
            .setDeterminationWithoutHearingRequired(YesOrNo.YES)
            .setDeterminationWithoutHearingReason("Documents are sufficient")
            .setRequestExtra4weeks(YesOrNo.NO)
            .setConsiderClaimantDocuments(YesOrNo.NO)
            .setConsiderClaimantDocumentsDetails("No further documents")
            .setGiveEvidenceConfirmDetails(new LipExtraDQEvidenceConfirmDetails()
                .setFirstName("Alex")
                .setLastName("Smith")
                .setEmail("alex.smith@example.com")
                .setPhone("07111111111")
                .setJobTitle("Owner")));
        assertThat(resultForm.getLipExperts()).isEqualTo(new LipExperts()
            .setCaseNeedsAnExpert(YesOrNo.NO)
            .setExpertCanStillExamineDetails("Accountant")
            .setExpertReportRequired(YesOrNo.NO)
            .setDetails(List.of(
                new ExpertReportTemplate().setExpertName("Ms Expert").setReportDate(LocalDate.of(2024, 3, 15))
            )));
    }

    @Test
    void shouldPopulateLipDQ_whenDQIsNotNullMinti() {
        given(caseData.getApplicant1DQ()).willReturn(new Applicant1DQ()
                                                         .setApplicant1DQFixedRecoverableCostsIntermediate(new FixedRecoverableCosts()
                                                                                                              .setIsSubjectToFixedRecoverableCostRegime(YesOrNo.YES)
                                                                                                              .setComplexityBandingAgreed(YesOrNo.YES)
                                                                                                              .setBand(ComplexityBand.BAND_1)
                                                                                                              .setReasons("reasons"))
                                                         .setSpecApplicant1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                                                                                                                 .setReachedAgreement(YesOrNo.YES))
                                                         .setSpecApplicant1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                                                                                                                   .setBespokeDirections("directions"))
                                                         .setApplicant1DQDefendantDocumentsToBeConsidered(new DocumentsToBeConsidered()
                                                                                                              .setHasDocumentsToBeConsidered(YesOrNo.YES)
                                                                                                              .setDetails("details")));

        final FixedRecoverableCostsSection expectedFrc = dqLipClaimantFormMapper.getFixedRecoverableCostsIntermediate(caseData);
        final DisclosureOfElectronicDocuments expectedEletronicDisclosure = dqLipClaimantFormMapper.getDisclosureOfElectronicDocuments(caseData);
        final DisclosureOfNonElectronicDocuments expectedNonEletronicDisclosure = dqLipClaimantFormMapper.getDisclosureOfNonElectronicDocuments(caseData);
        final DocumentsToBeConsideredSection expectedDocsToBeConsidered = dqLipClaimantFormMapper.getDocumentsToBeConsidered(caseData);

        FixedRecoverableCostsSection expectedFrcValue = new FixedRecoverableCostsSection();
        expectedFrcValue.setIsSubjectToFixedRecoverableCostRegime(YesOrNo.YES);
        expectedFrcValue.setComplexityBandingAgreed(YesOrNo.YES);
        expectedFrcValue.setReasons("reasons");
        expectedFrcValue.setBand(ComplexityBand.BAND_1);
        expectedFrcValue.setBandText("Band 1");
        assertThat(expectedFrc).isEqualTo(expectedFrcValue);

        assertThat(expectedEletronicDisclosure).isEqualTo(new DisclosureOfElectronicDocuments()
                                                              .setReachedAgreement(YesOrNo.YES));

        assertThat(expectedNonEletronicDisclosure).isEqualTo(new DisclosureOfNonElectronicDocuments()
                                                                 .setBespokeDirections("directions"));

        DocumentsToBeConsideredSection expectedDocsToBeConsideredValue = getDocumentsToBeConsideredSection();
        assertThat(expectedDocsToBeConsidered).isEqualTo(expectedDocsToBeConsideredValue);
    }

    private static @NotNull DocumentsToBeConsideredSection getDocumentsToBeConsideredSection() {
        DocumentsToBeConsideredSection expectedDocsToBeConsideredValue = new DocumentsToBeConsideredSection();
        expectedDocsToBeConsideredValue.setHasDocumentsToBeConsidered(YesOrNo.YES);
        expectedDocsToBeConsideredValue.setDetails("details");
        expectedDocsToBeConsideredValue.setSectionHeading("Defendants documents to be considered");
        expectedDocsToBeConsideredValue.setQuestion(
            "Are there any documents the defendants have that you want the court to consider?"
        );
        return expectedDocsToBeConsideredValue;
    }

    @Test
    void shouldReturnNullDocumentsToBeConsidered_whenClaimantDocumentsToBeConsideredIsNull() {
        given(caseData.getApplicant1DQ()).willReturn(new Applicant1DQ());

        final DocumentsToBeConsideredSection expectedDocsToBeConsidered = dqLipClaimantFormMapper.getDocumentsToBeConsidered(caseData);

        assertThat(expectedDocsToBeConsidered).isNull();
    }

    @Test
    void shouldReturnClaimantSignature_whenGetStatementOfTruth() {
        given(caseData.getApplicant1()).willReturn(applicant1);
        given(applicant1.getPartyName()).willReturn(NAME);

        String result = dqLipClaimantFormMapper.getStatementOfTruthName(caseData);

        assertThat(result).isEqualTo(NAME);
        verify(caseData).getApplicant1();
    }

    private static DQExtraDetailsLip claimantExtraDetails() {
        return new DQExtraDetailsLip()
            .setWantPhoneOrVideoHearing(YesOrNo.NO)
            .setWhyPhoneOrVideoHearing("Prefers in-person hearing")
            .setGiveEvidenceYourSelf(YesOrNo.YES)
            .setTriedToSettle(YesOrNo.NO)
            .setDeterminationWithoutHearingRequired(YesOrNo.YES)
            .setDeterminationWithoutHearingReason("Documents are sufficient")
            .setRequestExtra4weeks(YesOrNo.NO)
            .setConsiderClaimantDocuments(YesOrNo.NO)
            .setConsiderClaimantDocumentsDetails("No further documents")
            .setApplicant1DQLiPExpert(new ExpertLiP()
                .setCaseNeedsAnExpert(YesOrNo.NO)
                .setExpertCanStillExamineDetails("Accountant")
                .setExpertReportRequired(YesOrNo.NO)
                .setDetails(wrapElements(
                    new ExpertReportLiP("Ms Expert", LocalDate.of(2024, 3, 15))
                )));
    }

    private static EvidenceConfirmDetails claimantEvidenceConfirmDetails() {
        return new EvidenceConfirmDetails()
            .setFirstName("Alex")
            .setLastName("Smith")
            .setEmail("alex.smith@example.com")
            .setPhone("07111111111")
            .setJobTitle("Owner");
    }

    private static HearingSupportLip claimantHearingSupport() {
        return new HearingSupportLip()
            .setSupportRequirementLip(YesOrNo.YES)
            .setRequirementsLip(wrapElements(
                new RequirementsLip()
                    .setName("Accessible hearing")
                    .setRequirements(List.of(
                        SupportRequirements.DISABLED_ACCESS,
                        SupportRequirements.OTHER_SUPPORT
                    ))
                    .setOtherSupport("Wheelchair space")
            ));
    }
}
