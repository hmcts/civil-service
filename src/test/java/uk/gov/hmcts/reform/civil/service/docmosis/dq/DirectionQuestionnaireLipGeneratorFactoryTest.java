package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DirectionQuestionnaireLipGeneratorFactoryTest {

    @Mock
    private DirectionQuestionnaireLipResponseGenerator directionQuestionnaireLipResponseGenerator;
    @InjectMocks
    private DirectionQuestionnaireLipGeneratorFactory factory;

    @Test
    void shouldReturnDirectionQuestionnaireLipResponseGenerator() {
        DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator = factory.getDirectionQuestionnaire();
        assertThat(directionsQuestionnaireGenerator).isInstanceOf(DirectionQuestionnaireLipResponseGenerator.class);
    }
}
