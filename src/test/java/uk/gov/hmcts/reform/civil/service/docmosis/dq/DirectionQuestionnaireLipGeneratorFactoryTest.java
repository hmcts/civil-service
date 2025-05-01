package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DirectionQuestionnaireLipGeneratorFactoryTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DirectionsQuestionnaireLipGenerator directionsQuestionnaireLipGenerator;
    @Mock
    private DirectionQuestionnaireLipResponseGenerator directionQuestionnaireLipResponseGenerator;
    @InjectMocks
    private DirectionQuestionnaireLipGeneratorFactory factory;

    @Test
    void shouldReturnDirectionQuestionnaireLipGenerator_whenLipVLipIsDisabled() {

        //When
        DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator = factory.getDirectionQuestionnaire();
        //Then
        assertThat(directionsQuestionnaireGenerator).isInstanceOf(DirectionsQuestionnaireLipGenerator.class);
    }

    @Test
    void shouldReturnDirectionQuestionnaireLipResposeGenerator_whenLipVLipIsEnabled() {
        //Given
        given(featureToggleService.isLipVLipEnabled()).willReturn(true);
        //When
        DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator = factory.getDirectionQuestionnaire();
        //Then
        assertThat(directionsQuestionnaireGenerator).isInstanceOf(DirectionQuestionnaireLipResponseGenerator.class);
    }

}
