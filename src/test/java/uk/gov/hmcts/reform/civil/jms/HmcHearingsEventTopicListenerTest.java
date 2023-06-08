package uk.gov.hmcts.reform.civil.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hmc.exception.HmcTopicEventProcessingException;
import uk.gov.hmcts.reform.hmc.model.jms.HearingUpdate;
import uk.gov.hmcts.reform.hmc.model.jms.HmcMessage;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.jms.HmcStatus.EXCEPTION;

@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    public static final String UNSPEC_SERVICE_ID = "AAA7";
    public static final String SPEC_SERVICE_ID = "AAA6";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<String, Object> map;

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private TextMessage textMessage;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @BeforeEach
    void setUp() {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(mockObjectMapper, paymentsConfiguration, coreCaseDataService);
    }

    // @Test
    void shouldIgnoreMessage_whenServiceIdIsNotRelevant() throws Exception {
        HmcMessage hmcMessage = createHmcMessage("ABAB");

        when(textMessage.getText()).thenReturn(OBJECT_MAPPER.writeValueAsString(hmcMessage));
        when(mockObjectMapper.readValue(anyString(), eq(HmcMessage.class))).thenReturn(hmcMessage);
        when(paymentsConfiguration.getSiteId()).thenReturn(UNSPEC_SERVICE_ID);
        when(paymentsConfiguration.getSpecSiteId()).thenReturn(SPEC_SERVICE_ID);

        hmcHearingsEventTopicListener.onMessage(textMessage);

        verify(coreCaseDataService, never()).startUpdate(anyString(), eq(REVIEW_HEARING_EXCEPTION));
    }

    // @ParameterizedTest
    // @ValueSource(strings = {"AAA7", "AAA6"})
    void shouldConsumeMessage_whenServiceIdIsSpecAndHearingInException(String serviceId) throws Exception {
        HmcMessage hmcMessage = createHmcMessage(serviceId);

        // prevent UnnecessaryStubbingException
        if (serviceId.equals(SPEC_SERVICE_ID)) {
            when(paymentsConfiguration.getSpecSiteId()).thenReturn(SPEC_SERVICE_ID);
        } else {
            when(paymentsConfiguration.getSpecSiteId()).thenReturn(SPEC_SERVICE_ID);
            when(paymentsConfiguration.getSiteId()).thenReturn(UNSPEC_SERVICE_ID);
        }

        when(textMessage.getText()).thenReturn(OBJECT_MAPPER.writeValueAsString(hmcMessage));
        when(mockObjectMapper.readValue(anyString(), eq(HmcMessage.class))).thenReturn(hmcMessage);
        when(coreCaseDataService.startUpdate(any(), any()))
            .thenReturn(StartEventResponse.builder()
                            .token("")
                            .eventId("")
                            .caseDetails(CaseDetails.builder()
                                             .data(map)
                                             .build())
                            .build());

        hmcHearingsEventTopicListener.onMessage(textMessage);
        verify(coreCaseDataService).startUpdate(anyString(), eq(REVIEW_HEARING_EXCEPTION));
        verify(coreCaseDataService).submitUpdate(anyString(), any());
    }

    // @Test
    void shouldThrowJsonProcessingException() throws JsonProcessingException, JMSException {
        HmcMessage hmcMessage = createHmcMessage(UNSPEC_SERVICE_ID);

        when(textMessage.getText()).thenReturn(OBJECT_MAPPER.writeValueAsString(hmcMessage));
        when(mockObjectMapper.readValue(anyString(), eq(HmcMessage.class))).thenThrow(JsonProcessingException.class);

        Exception exception = assertThrows(HmcTopicEventProcessingException.class,
                                           () -> hmcHearingsEventTopicListener.onMessage(textMessage));

        String expectedMessage = "Unable to successfully deliver HMC message:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    private HmcMessage createHmcMessage(String serviceID) {
        return HmcMessage.builder()
            .hmctsServiceCode(serviceID)
            .caseId(1234L)
            .hearingId("testId")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(EXCEPTION)
                               .build())
            .build();
    }
}
