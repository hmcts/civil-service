package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ValidateRespondentWitnessesTest {

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
        List<Element<Witness>> witnessDetails = List.of(element(Witness.builder().firstName("First").build()));
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
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_ONE);
            when(callbackParams.getCaseData()).thenReturn(caseData);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldHandleMultiPartyScenarioWhenNotOneVOne() {
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            when(callbackParams.getCaseData()).thenReturn(caseData);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldValidateRespondent2Experts() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(true);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleOneVTwoOneLegalRepScenario() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(true);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2HasDifferentLegalRepScenario() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Witnesses respondent2DQWitnesses = Witnesses.builder()
            .witnessesToAppear(YES)
            .build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder()
            .respondent2DQWitnesses(respondent2DQWitnesses)
            .build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondentResponseIsSame(NO)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondentResponseIsSameIsNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondentResponseIsSame(null)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2DQWitnessesIsNotNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Witnesses respondent2DQWitnesses = Witnesses.builder().witnessesToAppear(YES).build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQWitnesses(respondent2DQWitnesses).build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2HasDifferentLegalRepScenarioWhenWitnessesAreNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondentResponseIsSame(NO)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2HasDifferentLegalRepScenarioWhenResponseIsSame() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Witnesses respondent2DQWitnesses = Witnesses.builder()
            .witnessesToAppear(YES)
            .build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder()
            .respondent2DQWitnesses(respondent2DQWitnesses)
            .build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondentResponseIsSame(YES)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2DQIsNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(null)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2DQIsNotNullButWitnessesAreNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Witnesses respondent2DQWitnesses = Witnesses.builder()
            .witnessesToAppear(YES)
            .build();

        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondentResponseIsSame(NO)
            .respondent2DQWitnessesSmallClaim(respondent2DQWitnesses)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentWitnesses.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }
}
