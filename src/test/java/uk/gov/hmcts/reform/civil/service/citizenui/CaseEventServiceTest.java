package uk.gov.hmcts.reform.civil.service.citizenui;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.exceptions.InvalidGeneralApplicationTypeException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@ExtendWith(SpringExtension.class)
public class CaseEventServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CaseEventService caseEventService;

    private static final String EVENT_TOKEN = "jM4OWUxMGRkLWEyMzYt";
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .id(1L)
        .data(Map.of())
        .build();
    private static final String AUTHORISATION = "authorisation";
    private static final String USER_ID = "123";
    private static final String CASE_ID = "123";
    private static final String DRAFT = "draft";
    private static final String EVENT_ID = "1";
    private static final StartEventResponse RESPONSE = StartEventResponse
        .builder()
        .eventId(EVENT_ID)
        .token(EVENT_TOKEN)
        .caseDetails(CASE_DETAILS)
        .build();

    @BeforeEach
    void setUp() {
        given(authTokenGenerator.generate()).willReturn(EVENT_TOKEN);
        given(coreCaseDataApi.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(RESPONSE);
        given(coreCaseDataApi.startForCitizen(any(), any(), any(), any(), any(), any()))
            .willReturn(RESPONSE);
        given(coreCaseDataApi.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
        given(coreCaseDataApi.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
        ReflectionTestUtils.setField(caseEventService, "caseFlagsLoggingEnabled", false);
    }

    @Test
    void shouldSubmitEventForExistingClaimSuccessfully() {
        InOrder orderVerifier = inOrder(coreCaseDataApi);
        CaseDetails caseDetails = caseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .setCaseId(CASE_ID)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.DEFENDANT_RESPONSE_SPEC.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(AUTHORISATION, EVENT_TOKEN, USER_ID, JURISDICTION,
                                                                    CASE_TYPE, CASE_ID, true,
                                                                    caseDataContentFromStartEventResponse(
                                                                        RESPONSE,
                                                                        Map.of()
                                                                    )
        );
    }

    @Test
    void shouldSubmitValidSingleGeneralApplicationTypeSuccessfully() {
        Map<String, Object> updates = generalApplicationTypeUpdates("EXTEND_TIME");
        InOrder orderVerifier = inOrder(coreCaseDataApi);

        CaseDetails caseDetails = caseEventService.submitEvent(initiateGeneralApplicationParams(updates));

        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.INITIATE_GENERAL_APPLICATION.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            true,
            caseDataContentFromStartEventResponse(RESPONSE, updates)
        );
    }

    @Test
    void shouldSubmitValidMultiGeneralApplicationTypesSuccessfully() {
        Map<String, Object> updates = generalApplicationTypeUpdates("EXTEND_TIME", "STRIKE_OUT", "OTHER");
        InOrder orderVerifier = inOrder(coreCaseDataApi);

        CaseDetails caseDetails = caseEventService.submitEvent(initiateGeneralApplicationParams(updates));

        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.INITIATE_GENERAL_APPLICATION.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            true,
            caseDataContentFromStartEventResponse(RESPONSE, updates)
        );
    }

    @Test
    void shouldSubmitValidGeneralApplicationTypeLrSuccessfully() {
        Map<String, Object> updates = generalApplicationTypeLrUpdates("EXTEND_TIME", "STRIKE_OUT", "OTHER");
        InOrder orderVerifier = inOrder(coreCaseDataApi);

        CaseDetails caseDetails = caseEventService.submitEvent(initiateGeneralApplicationParams(updates));

        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.INITIATE_GENERAL_APPLICATION.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            true,
            caseDataContentFromStartEventResponse(RESPONSE, updates)
        );
    }

    @Test
    void shouldSubmitValidCoscGeneralApplicationTypeSuccessfully() {
        Map<String, Object> updates = generalApplicationTypeUpdates("CONFIRM_CCJ_DEBT_PAID");
        InOrder orderVerifier = inOrder(coreCaseDataApi);

        CaseDetails caseDetails = caseEventService.submitEvent(
            initiateGeneralApplicationParams(updates, CaseEvent.INITIATE_GENERAL_APPLICATION_COSC)
        );

        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.INITIATE_GENERAL_APPLICATION_COSC.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            true,
            caseDataContentFromStartEventResponse(RESPONSE, updates)
        );
    }

    @Test
    void shouldRejectOtherOptionBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(
            generalApplicationTypeUpdates("EXTEND_TIME", "OTHER_OPTION"),
            1,
            "UI_ONLY_OTHER_OPTION"
        );
    }

    @Test
    void shouldRejectStaleSummaryJudgmentCodeBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(generalApplicationTypeUpdates("SUMMARY_JUDGMENT"), 1, "STALE_CODE");
    }

    @Test
    void shouldRejectInvalidGeneralApplicationTypeLrBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(
            generalApplicationTypeLrUpdates("EXTEND_TIME", "OTHER_OPTION"),
            1,
            "UI_ONLY_OTHER_OPTION"
        );
    }

    @Test
    void shouldRejectProceedsInHeritageBecauseCuiCannotInitiateThatGeneralApplicationType() {
        assertInvalidGaPayloadRejected(generalApplicationTypeUpdates("PROCEEDS_IN_HERITAGE"), 1, "CUI_UNSUPPORTED_TYPE");
    }

    @Test
    void shouldRejectInvalidCoscGeneralApplicationTypeBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(
            generalApplicationTypeUpdates("OTHER_OPTION"),
            1,
            "UI_ONLY_OTHER_OPTION",
            CaseEvent.INITIATE_GENERAL_APPLICATION_COSC
        );
    }

    @Test
    void shouldRejectDisplayLabelsBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(generalApplicationTypeUpdates("Summary judgment"), 1, "DISPLAY_LABEL");
    }

    @Test
    void shouldRejectBlankNullAndNonStringTypesBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(generalApplicationTypeUpdates("", null, 123), 3, "BLANK_VALUE");
    }

    @Test
    void shouldRejectInvalidThirdApplicationTypeBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(
            generalApplicationTypeUpdates("EXTEND_TIME", "STRIKE_OUT", "OTHER_OPTION"),
            1,
            "UI_ONLY_OTHER_OPTION"
        );
    }

    @Test
    void shouldRejectMissingGeneralApplicationTypesBeforeSubmittingToCcd() {
        assertInvalidGaPayloadRejected(Map.of("generalAppType", Map.of()), 0, "MISSING_TYPES");
    }

    @Test
    void shouldSubmitEventForNewClaimSuccessfully() {
        InOrder orderVerifier = inOrder(coreCaseDataApi);
        CaseDetails caseDetails = caseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .setCaseId(DRAFT)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CaseEvent.DEFENDANT_RESPONSE_SPEC.name()
        );

        orderVerifier.verify(coreCaseDataApi).submitForCitizen(AUTHORISATION, EVENT_TOKEN, USER_ID, JURISDICTION,
                                                               CASE_TYPE, true,
                                                               caseDataContentFromStartEventResponse(
                                                                   RESPONSE,
                                                                   Map.of()
                                                               )
        );
    }

    @Test
    void shouldSubmitEventForExistingClaimSuccessfully_whenCaseFlagsLoggingIsEnabled() {
        ReflectionTestUtils.setField(caseEventService, "caseFlagsLoggingEnabled", true);
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .respondent1(new Party()
                             .setFlags(new Flags()
                                        .setPartyName("Mr test")
                                        .setRoleOnCase("Defendant 1")
                                        .setDetails(List.of()))
                             .setType(Party.Type.INDIVIDUAL))
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        StartEventResponse eventResponse = StartEventResponse
            .builder()
            .eventId(EVENT_ID)
            .token(EVENT_TOKEN)
            .caseDetails(caseDetails)
            .build();
        given(coreCaseDataApi.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(eventResponse);
        given(caseDetailsConverter.toCaseData(anyMap())).willReturn(caseData);

        CaseDetails response = caseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .setCaseId(CASE_ID)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(response).isEqualTo(CASE_DETAILS);
    }

    private EventSubmissionParams initiateGeneralApplicationParams(Map<String, Object> updates) {
        return initiateGeneralApplicationParams(updates, CaseEvent.INITIATE_GENERAL_APPLICATION);
    }

    private EventSubmissionParams initiateGeneralApplicationParams(Map<String, Object> updates, CaseEvent event) {
        return new EventSubmissionParams()
            .setUpdates(updates)
            .setEvent(event)
            .setCaseId(CASE_ID)
            .setUserId(USER_ID)
            .setAuthorisation(AUTHORISATION);
    }

    private Map<String, Object> generalApplicationTypeUpdates(Object... typeValues) {
        return Map.of("generalAppType", Map.of("types", Arrays.asList(typeValues)));
    }

    private Map<String, Object> generalApplicationTypeLrUpdates(Object... typeValues) {
        return Map.of("generalAppTypeLR", Map.of("types", Arrays.asList(typeValues)));
    }

    private void assertInvalidGaPayloadRejected(Map<String, Object> updates, int invalidCount, String expectedReason) {
        assertInvalidGaPayloadRejected(updates, invalidCount, expectedReason, CaseEvent.INITIATE_GENERAL_APPLICATION);
    }

    private void assertInvalidGaPayloadRejected(
        Map<String, Object> updates,
        int invalidCount,
        String expectedReason,
        CaseEvent event
    ) {
        InvalidGeneralApplicationTypeException exception = assertThrows(
            InvalidGeneralApplicationTypeException.class,
            () -> caseEventService.submitEvent(initiateGeneralApplicationParams(updates, event))
        );

        assertThat(exception.getMessage()).isEqualTo("Invalid general application type");
        assertThat(exception.getInvalidValueCount()).isEqualTo(invalidCount);
        assertThat(exception.getReasonCategories()).contains(expectedReason);
        verifyNoInteractions(coreCaseDataApi, authTokenGenerator);
    }
}
