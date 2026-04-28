package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertReportLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DisabilityRequirement;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DocumentsToBeConsideredSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.ExpertReportTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.HearingLipSupportRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExperts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQ;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.LipExtraDQEvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DocumentsToBeConsidered;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class DQLipDefendantFormMapperTest {

    private static final String NAME = "Defendant";
    @Mock
    private CaseData caseData;
    @Mock
    private Party respondent1;
    @InjectMocks
    private DQLipDefendantFormMapper dqLipDefendantFormMapper;
    private DirectionsQuestionnaireForm form;

    @BeforeEach
    void setUp() {
        form = new DirectionsQuestionnaireForm();
    }

    @Test
    void shouldPopulateDefendantLipSpecificDetails() {
        CaseDataLiP caseDataLiP = new CaseDataLiP().setRespondent1LiPResponse(
            new RespondentLiPResponse()
                .setRespondent1DQExtraDetails(defendantExtraDetails())
                .setRespondent1DQEvidenceConfirmDetails(defendantEvidenceConfirmDetails())
                .setRespondent1DQHearingSupportLip(defendantHearingSupport())
        );

        DirectionsQuestionnaireForm resultForm = dqLipDefendantFormMapper.addLipDQs(form, Optional.of(caseDataLiP));

        assertThat(resultForm.getHearingLipSupportRequirements()).containsExactly(
            new HearingLipSupportRequirements()
                .setName("Interpreter support")
                .setOtherSupport("Screen reader")
                .setRequirements(List.of(
                    new DisabilityRequirement(SupportRequirements.SIGN_INTERPRETER.getDisplayedValue()),
                    new DisabilityRequirement("Screen reader")
                ))
        );
        assertThat(resultForm.getLipExtraDQ()).isEqualTo(new LipExtraDQ()
            .setWantPhoneOrVideoHearing(YesOrNo.YES)
            .setWhyPhoneOrVideoHearing("Travel difficulties")
            .setGiveEvidenceYourSelf(YesOrNo.NO)
            .setTriedToSettle(YesOrNo.YES)
            .setDeterminationWithoutHearingRequired(YesOrNo.NO)
            .setDeterminationWithoutHearingReason("Oral evidence required")
            .setRequestExtra4weeks(YesOrNo.YES)
            .setConsiderClaimantDocuments(YesOrNo.YES)
            .setConsiderClaimantDocumentsDetails("Review bank statements")
            .setGiveEvidenceConfirmDetails(new LipExtraDQEvidenceConfirmDetails()
                .setFirstName("Jane")
                .setLastName("Doe")
                .setEmail("jane.doe@example.com")
                .setPhone("07000000000")
                .setJobTitle("Director")));
        assertThat(resultForm.getLipExperts()).isEqualTo(new LipExperts()
            .setCaseNeedsAnExpert(YesOrNo.YES)
            .setExpertCanStillExamineDetails("Neurologist")
            .setExpertReportRequired(YesOrNo.YES)
            .setDetails(List.of(
                new ExpertReportTemplate().setExpertName("Dr One").setReportDate(LocalDate.of(2024, 1, 10)),
                new ExpertReportTemplate().setExpertName("Dr Two").setReportDate(LocalDate.of(2024, 2, 20))
            )));
    }

    @Test
    void shouldPopulateLipDQ_whenDQIsNotNullMinti() {
        //Given
        given(caseData.getRespondent1DQ()).willReturn(new Respondent1DQ()
                                                         .setRespondent1DQFixedRecoverableCostsIntermediate(
                                                             new FixedRecoverableCosts()
                                                                 .setIsSubjectToFixedRecoverableCostRegime(YesOrNo.NO)
                                                                 .setReasons("reasons"))
                                                         .setSpecRespondent1DQDisclosureOfElectronicDocuments(
                                                             new DisclosureOfElectronicDocuments()
                                                                 .setReachedAgreement(YesOrNo.NO)
                                                                 .setAgreementLikely(YesOrNo.NO)
                                                                 .setReasonForNoAgreement("no"))
                                                         .setSpecRespondent1DQDisclosureOfNonElectronicDocuments(
                                                             new DisclosureOfNonElectronicDocuments()
                                                                 .setBespokeDirections("directions"))
                                                         .setRespondent1DQClaimantDocumentsToBeConsidered(
                                                             new DocumentsToBeConsidered()
                                                                 .setHasDocumentsToBeConsidered(YesOrNo.NO)
                                                                 .setDetails("details")));
        //When
        final FixedRecoverableCostsSection expectedFrc = dqLipDefendantFormMapper.getFixedRecoverableCostsIntermediate(caseData);
        final DisclosureOfElectronicDocuments expectedEletronicDisclosure = dqLipDefendantFormMapper.getDisclosureOfElectronicDocuments(caseData);
        final DisclosureOfNonElectronicDocuments expectedNonEletronicDisclosure = dqLipDefendantFormMapper.getDisclosureOfNonElectronicDocuments(caseData);
        final DocumentsToBeConsideredSection expectedDocsToBeConsidered = dqLipDefendantFormMapper.getDocumentsToBeConsidered(caseData);
        //Then
        FixedRecoverableCostsSection expectedFrcValue = new FixedRecoverableCostsSection();
        expectedFrcValue.setIsSubjectToFixedRecoverableCostRegime(YesOrNo.NO);
        expectedFrcValue.setReasons("reasons");
        assertThat(expectedFrc).isEqualTo(expectedFrcValue);

        assertThat(expectedEletronicDisclosure).isEqualTo(new DisclosureOfElectronicDocuments()
                                                              .setReachedAgreement(YesOrNo.NO)
                                                              .setAgreementLikely(YesOrNo.NO)
                                                              .setReasonForNoAgreement("no"));

        assertThat(expectedNonEletronicDisclosure).isEqualTo(new DisclosureOfNonElectronicDocuments()
                                                                 .setBespokeDirections("directions"));

        DocumentsToBeConsideredSection expectedDocsToBeConsideredValue = new DocumentsToBeConsideredSection();
        expectedDocsToBeConsideredValue.setHasDocumentsToBeConsidered(YesOrNo.NO);
        expectedDocsToBeConsideredValue.setDetails("details");
        expectedDocsToBeConsideredValue.setSectionHeading("Claimants documents to be considered");
        expectedDocsToBeConsideredValue.setQuestion(
            "Are there any documents the claimants have that you want the court to consider?"
        );
        assertThat(expectedDocsToBeConsidered).isEqualTo(expectedDocsToBeConsideredValue);
    }

    @Test
    void shouldReturnClaimantSignature_whenGetStatementOfTruth() {
        //Given
        given(caseData.getRespondent1()).willReturn(respondent1);
        given(respondent1.getPartyName()).willReturn(NAME);
        //When
        String result = dqLipDefendantFormMapper.getStatementOfTruthName(caseData);
        //Then
        assertThat(result).isEqualTo(NAME);
        verify(caseData).getRespondent1();
    }

    private static DQExtraDetailsLip defendantExtraDetails() {
        return new DQExtraDetailsLip()
            .setWantPhoneOrVideoHearing(YesOrNo.YES)
            .setWhyPhoneOrVideoHearing("Travel difficulties")
            .setGiveEvidenceYourSelf(YesOrNo.NO)
            .setTriedToSettle(YesOrNo.YES)
            .setDeterminationWithoutHearingRequired(YesOrNo.NO)
            .setDeterminationWithoutHearingReason("Oral evidence required")
            .setRequestExtra4weeks(YesOrNo.YES)
            .setConsiderClaimantDocuments(YesOrNo.YES)
            .setConsiderClaimantDocumentsDetails("Review bank statements")
            .setRespondent1DQLiPExpert(new ExpertLiP()
                .setCaseNeedsAnExpert(YesOrNo.YES)
                .setExpertCanStillExamineDetails("Neurologist")
                .setExpertReportRequired(YesOrNo.YES)
                .setDetails(wrapElements(
                    new ExpertReportLiP("Dr One", LocalDate.of(2024, 1, 10)),
                    new ExpertReportLiP("Dr Two", LocalDate.of(2024, 2, 20))
                )));
    }

    private static EvidenceConfirmDetails defendantEvidenceConfirmDetails() {
        return new EvidenceConfirmDetails()
            .setFirstName("Jane")
            .setLastName("Doe")
            .setEmail("jane.doe@example.com")
            .setPhone("07000000000")
            .setJobTitle("Director");
    }

    private static HearingSupportLip defendantHearingSupport() {
        return new HearingSupportLip()
            .setSupportRequirementLip(YesOrNo.YES)
            .setRequirementsLip(wrapElements(
                new RequirementsLip()
                    .setName("Interpreter support")
                    .setRequirements(List.of(
                        SupportRequirements.SIGN_INTERPRETER,
                        SupportRequirements.OTHER_SUPPORT
                    ))
                    .setOtherSupport("Screen reader")
            ));
    }
}
