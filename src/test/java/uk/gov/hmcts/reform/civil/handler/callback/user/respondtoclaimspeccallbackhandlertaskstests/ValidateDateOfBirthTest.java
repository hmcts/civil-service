package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ValidateDateOfBirthTest {

    @InjectMocks
    private ValidateDateOfBirth validateDateOfBirth;

    @Mock
    private DateOfBirthValidator dateOfBirthValidator;

    @Mock
    private PostcodeValidator postcodeValidator;

    @Mock
    private ObjectMapper objectMapper;

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
    void shouldReturnErrorsWhenDateOfBirthIsInvalid() {
        Party respondent = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent)).thenReturn(Collections.singletonList("Invalid date of birth"));
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent1(respondent).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid date of birth");
    }

    @Test
    void shouldReturnNoErrorsWhenDateOfBirthIsValid() {
        Party respondent = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent)).thenReturn(Collections.emptyList());
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent1(respondent).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenCorrespondenceAddressIsInvalid() {
        when(postcodeValidator.validate(null)).thenReturn(Collections.singletonList("Invalid postcode"));
        caseData = caseData.toBuilder()
            .isRespondent1(YES)
            .specAoSRespondentCorrespondenceAddressRequired(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid postcode");
    }

    @Test
    void shouldReturnNoErrorsWhenCorrespondenceAddressIsValid() {
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().isRespondent1(YES).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenRespondent1IsNullAndRespondent2IsNotNull() {
        Party respondent2 = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent2)).thenReturn(Collections.singletonList("Invalid date of birth"));
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent2(respondent2).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid date of birth");
    }

    @Test
    void shouldReturnNoErrorsWhenRespondent1IsNotNullAndRespondent2IsNull() {
        Party respondent1 = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent1)).thenReturn(Collections.emptyList());
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent1(respondent1).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenBothRespondentsAreNull() {
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenSpecAoSRespondent2CorrespondenceAddressRequiredIsNO() {
        when(postcodeValidator.validate(null)).thenReturn(Collections.singletonList("Invalid postcode"));
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specAoSRespondent2CorrespondenceAddressRequired(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid postcode");
    }

    @Test
    void shouldReturnNoErrorsWhenSpecAoSRespondentCorrespondenceAddressRequiredIsYES() {
        caseData = caseData.toBuilder()
            .isRespondent1(YES)
            .specAoSRespondentCorrespondenceAddressRequired(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenSpecAoSRespondent2CorrespondenceAddressRequiredIsYES() {
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specAoSRespondent2CorrespondenceAddressRequired(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldSetSameSolicitorSameResponseToYesWhenBothSolicitorsRepresentOnlyOneRespondent() {
        caseData = CaseData.builder()
            .addRespondent2(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(true);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldSetSameSolicitorSameResponseToYesWhenOnlySecondSolicitorRepresentsOneRespondent() {
        caseData = CaseData.builder()
            .addRespondent2(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);
            when(respondToClaimSpecUtils.isSolicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldSetSameSolicitorSameResponseToYesWhenNoSolicitorRepresentsOneRespondent() {
        caseData = CaseData.builder()
            .addRespondent2(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_TWO_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();

        }
    }

    @Test
    void shouldSetSameSolicitorSameResponseToNoWhenOneVTwoOneLegalRepAndRespondentResponseIsNotSame() {
        caseData = CaseData.builder()
            .addRespondent2(YES)
            .respondentResponseIsSame(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldNotSetSameSolicitorSameResponseWhenAddRespondent2IsNoAndRespondentResponseIsNotSame() {
        caseData = CaseData.builder()
            .addRespondent2(NO)
            .respondentResponseIsSame(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Test
    void shouldSetSameSolicitorSameResponseToYesWhenOneVTwoOneLegalRepAndRespondentResponseIsSame() {
        caseData = CaseData.builder()
            .addRespondent2(YES)
            .respondentResponseIsSame(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(ONE_V_TWO_ONE_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

            assertThat(response.getErrors()).isEmpty();
        }
    }
}
