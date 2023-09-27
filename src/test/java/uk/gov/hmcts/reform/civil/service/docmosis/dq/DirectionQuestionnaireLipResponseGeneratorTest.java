package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DQ_LIP_RESPONSE;

@ExtendWith(MockitoExtension.class)
class DirectionQuestionnaireLipResponseGeneratorTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseData caseData;
    @InjectMocks
    private DirectionQuestionnaireLipResponseGenerator generator;

    @Test
    void shouldReturnLipTemplate_whenLipVLipEnabledAndClaimantLip() {
        //Given
        given(featureToggleService.isLipVLipEnabled()).willReturn(true);
        given(caseData.isApplicantNotRepresented()).willReturn(true);
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isEqualTo(DQ_LIP_RESPONSE);
    }

    @Test
    void shouldReturnLipTempate_whenLipVLipEnabledAndDefendantLip() {
        //Given
        given(featureToggleService.isLipVLipEnabled()).willReturn(true);
        given(caseData.isRespondent1NotRepresented()).willReturn(true);
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isEqualTo(DQ_LIP_RESPONSE);
    }


    @Test
    void shouldNotReturnLipTemplateWhenLipvLipIsNotEnabled() {
        //Given
        given(featureToggleService.isLipVLipEnabled()).willReturn(false);
        given(caseData.isRespondent1NotRepresented()).willReturn(true);
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isNotEqualTo(DQ_LIP_RESPONSE);
    }

    @Test
    void shouldNotReturnLipTemplateWhenLrvLr() {
        //When
        DocmosisTemplates docmosisTemplate = generator.getTemplateId(caseData);
        //Then
        assertThat(docmosisTemplate).isNotEqualTo(DQ_LIP_RESPONSE);
    }
}
