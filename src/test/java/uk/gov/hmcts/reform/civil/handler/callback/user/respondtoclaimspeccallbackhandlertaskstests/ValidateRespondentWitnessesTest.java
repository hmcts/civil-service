package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Witness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class ValidateRespondentWitnessesTest {

    @InjectMocks
    private ValidateRespondentWitnesses validateRespondentWitnesses;


    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsWhenWitnessDetailsAreMissing() {
        caseData = CaseData.builder()
            .respondent1DQWitnessesRequiredSpec(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertThat(response.getErrors()).contains("Witness details required");
    }

    @Test
    void shouldReturnNoErrorsWhenWitnessDetailsArePresent() {
        List<Element<Witness>> witnessDetails = List.of(element(Witness.builder().name("Details").build()));
        caseData = CaseData.builder()
            .respondent1DQWitnessesRequiredSpec(YES)
            .respondent1DQWitnessesDetailsSpec(witnessDetails)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldHandleMultiPartyScenario() {
        try (MockedStatic<MultiPartyScenario> mockedStatic = Mockito.mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_ONE);
            when(callbackParams.getCaseData()).thenReturn(caseData);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }
}
