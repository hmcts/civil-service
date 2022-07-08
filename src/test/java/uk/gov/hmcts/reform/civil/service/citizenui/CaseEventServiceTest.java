package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class CaseEventServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CaseEventService caseEventService;

    private static final String EVENT_TOKEN = "jM4OWUxMGRkLWEyMzYt";

    @BeforeEach
    void setUp() {
        given(coreCaseDataApi.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(StartEventResponse.builder().token(EVENT_TOKEN).build());
        given(authTokenGenerator.generate()).willReturn("token");
    }

    @Test
    void shouldReturnEventTokenSuccessfully() {
        String eventToken = caseEventService.getDefendantResponseSpecEventToken("authorisation", "123", "123");
        assertThat(eventToken).isEqualTo(EVENT_TOKEN);
    }
}
