package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DQLipDefendantFormMapperTest {

    private static final String NAME = "Defendant";
    @Mock
    private CaseDataLiP caseDataLiPMock;
    @Mock
    private DQExtraDetailsLip dqExtraDetailsLipMock;
    @Mock
    private ExpertLiP expertLiPMock;
    @Mock
    private RespondentLiPResponse respondentLiPResponse;
    @Mock
    private CaseData caseData;
    @Mock
    private Party respondent1;
    @InjectMocks
    private DQLipDefendantFormMapper dqLipDefendantFormMapper;
    private DirectionsQuestionnaireForm form;

    @BeforeEach
    void setUp() {
        form = DirectionsQuestionnaireForm.builder().build();
    }

    @Test
    void shouldPopulateLipExtraDQ_whenDQExtraDetailsLipIsNotNull() {
        //Given
        Optional<CaseDataLiP> caseDataLiPOptional = Optional.of(caseDataLiPMock);
        given(caseDataLiPMock.getRespondent1LiPResponse()).willReturn(respondentLiPResponse);
        given(respondentLiPResponse.getRespondent1DQExtraDetails()).willReturn(dqExtraDetailsLipMock);
        given(dqExtraDetailsLipMock.getRespondent1DQLiPExpert()).willReturn(expertLiPMock);
        given(dqExtraDetailsLipMock.getTriedToSettle()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getRequestExtra4weeks()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getGiveEvidenceYourSelf()).willReturn(YesOrNo.YES);
        given(dqExtraDetailsLipMock.getConsiderClaimantDocumentsDetails()).willReturn("test");
        given(dqExtraDetailsLipMock.getDeterminationWithoutHearingRequired()).willReturn(YesOrNo.NO);
        //When
        DirectionsQuestionnaireForm resultForm = dqLipDefendantFormMapper.addLipDQs(form, caseDataLiPOptional);
        //Then
        assertThat(resultForm.getLipExtraDQ()).isNotNull();
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
}
