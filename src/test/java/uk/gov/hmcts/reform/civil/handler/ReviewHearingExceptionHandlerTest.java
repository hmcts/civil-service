package uk.gov.hmcts.reform.civil.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.hmc.model.messaging.HearingUpdate;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REVIEW_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.HEARING_REQUESTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ReviewHearingExceptionHandler.class
})
class ReviewHearingExceptionHandlerTest {

    @Autowired
    private ReviewHearingExceptionHandler handler;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @BeforeEach
    void setUp() {
        when(paymentsConfiguration.getSpecSiteId()).thenReturn("AAA6");
        when(paymentsConfiguration.getSiteId()).thenReturn("AAA7");
    }

    @Test
    void shouldReturnTrue_whenMessageRelevantForServiceAndHearingException() {
        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(1234L)
            .hearingId("HER1234")
            .hmctsServiceCode("AAA7")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(EXCEPTION)
                               .build())
            .build();

        boolean actual = handler.handleExceptionEvent(hmcMessage);

        verify(coreCaseDataService).triggerEvent(1234L, REVIEW_HEARING_EXCEPTION);
        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenMessageRelevantForServiceNoHearingException() {
        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(1234L)
            .hearingId("HER1234")
            .hmctsServiceCode("AAA7")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(HEARING_REQUESTED)
                               .build())
            .build();

        boolean actual = handler.handleExceptionEvent(hmcMessage);

        assertThat(actual).isTrue();
        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldReturnTrue_whenMessageNotRelevantForServiceHearingException() {
        HmcMessage hmcMessage = HmcMessage.builder()
            .caseId(1234L)
            .hearingId("HER1234")
            .hmctsServiceCode("AAA8")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(EXCEPTION)
                               .build())
            .build();

        boolean actual = handler.handleExceptionEvent(hmcMessage);

        assertThat(actual).isTrue();
        verifyNoInteractions(coreCaseDataService);
    }
}
