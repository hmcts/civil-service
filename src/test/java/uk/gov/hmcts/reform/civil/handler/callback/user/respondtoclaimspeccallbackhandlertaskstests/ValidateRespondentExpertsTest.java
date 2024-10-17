package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ValidateRespondentExpertsTest {

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Mock
    private CallbackParams callbackParams;

    @InjectMocks
    private ValidateRespondentExperts validateRespondentExperts;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
    }

    @Test
    void shouldReturnResponseForOneVOneScenario() {
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_ONE);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleOneVTwoOneLegalRepScenario() {
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(true);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldValidateRespondent2Experts() {
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(true);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleTwoVOneScenario() {
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(TWO_V_ONE);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleOneVTwoTwoLegalRepScenario() {
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldReturnTrueWhenRespondentResponseIsNotSame() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondentResponseIsSame(NO)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldReturnFalseWhenRespondentResponseIsSame() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondentResponseIsSame(YES)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldReturnFalseWhenRespondentResponseIsNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = CaseData.builder()
            .respondentResponseIsSame(null)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

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
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2HasSameLegalRepScenario() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQExperts(null).build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .respondentResponseIsSame(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleRespondent2DQExpertsIsNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQExperts(null).build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldValidateRespondent2ExpertsWhenConditionsAreMet() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondent2DQExperts(new Experts()).build();
        caseData = CaseData.builder()
            .respondentResponseIsSame(NO)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }

    @Test
    void shouldHandleWhenRespondent2DQIsNull() {
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Witnesses respondent2DQWitnesses = Witnesses.builder()
            .witnessesToAppear(YES)
            .build();
        caseData = CaseData.builder()
            .respondent1DQ(respondent1DQ)
            .respondent2DQWitnessesSmallClaim(respondent2DQWitnesses)
            .respondentResponseIsSame(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);
            lenient().when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(false);
            lenient().when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(false);

            CallbackResponse response = validateRespondentExperts.execute(callbackParams);

            assertThat(response).isNotNull();
        }
    }
}
