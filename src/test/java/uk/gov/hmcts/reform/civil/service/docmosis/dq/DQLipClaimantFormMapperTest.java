package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DQLipClaimantFormMapperTest {

    @Mock
    private CaseDataLiP caseDataLiPMock;
    @Mock
    private DQExtraDetailsLip dqExtraDetailsLipMock;
    @Mock
    private ExpertLiP expertLiPMock;
    @Mock
    private ClaimantLiPResponse claimantLiPResponse;
    @InjectMocks
    private DQLipClaimantFormMapper dqLipClaimantFormMapper;
    private DirectionsQuestionnaireForm form;

    @BeforeEach
    void setUp() {
        form = DirectionsQuestionnaireForm.builder().build();
    }

    @Test
    void shouldNotPopulateDtoWithLipData_whenCaseDataLipIsNull() {
        //Given
        Optional<CaseDataLiP> emptyOptional = Optional.empty();
        //When
        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, emptyOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNull();
    }

    @Test
    void shouldNotPopulateLipExtraDQ_whenDQExtraDetailsLipIsNull() {
        //Given
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(caseDataLiPMock);
        given(caseDataLiPMock.getApplicant1LiPResponse()).willReturn(claimantLiPResponse);
        given(claimantLiPResponse.getApplicant1DQExtraDetails()).willReturn(null);
        //When
        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, caseDataLiPOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNull();
    }

    @Test
    void shouldPopulateLipExtraDQ_whenDQExtraDetailsLipIsNotNull() {
        //Given
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(caseDataLiPMock);
        given(caseDataLiPMock.getApplicant1LiPResponse()).willReturn(claimantLiPResponse);
        given(claimantLiPResponse.getApplicant1DQExtraDetails()).willReturn(dqExtraDetailsLipMock);
        given(dqExtraDetailsLipMock.getApplicant1DQLiPExpert()).willReturn(expertLiPMock);
        given(dqExtraDetailsLipMock.getTriedToSettle()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getRequestExtra4weeks()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getGiveEvidenceYourSelf()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getConsiderClaimantDocumentsDetails()).willReturn("test");
        given(dqExtraDetailsLipMock.getDeterminationWithoutHearingRequired()).willReturn(YesOrNo.NO);
        //When
        DirectionsQuestionnaireForm resultForm = dqLipClaimantFormMapper.addLipDQs(form, caseDataLiPOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNotNull();
    }

}
