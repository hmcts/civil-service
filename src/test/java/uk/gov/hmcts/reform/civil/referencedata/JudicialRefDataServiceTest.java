package uk.gov.hmcts.reform.civil.referencedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.model.JudgeRefData;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(classes = {RestTemplate.class})
class JudicialRefDataServiceTest {

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JRDConfiguration jrdConfiguration;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private JudicialRefDataService refDataService;

    @BeforeEach
    void setUp() {
        when(jrdConfiguration.getUrl()).thenReturn("dummy_url");
        when(jrdConfiguration.getEndpoint()).thenReturn("/fees-register/fees/lookup");
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsAllLocations() {

        when(authTokenGenerator.generate()).thenReturn("service_token");

        List<JudgeRefData> judgeRefData = Arrays.asList(
            JudgeRefData.builder().title("Mr").surname("Murphy").emailId("mr.murphy@email.com").build(),
            JudgeRefData.builder().title("Mr").surname("McGee").emailId("mr.mcgee@email.com").build(),
            JudgeRefData.builder().title("Mr").surname("Brad").emailId("mr.brad@email.com").build(),
            JudgeRefData.builder().title("Mrs").surname("Lee").emailId("mrs.lee@email.com").build()
        );

        when(restTemplate.exchange(
            uriCaptor.capture(),
            httpMethodCaptor.capture(),
            httpEntityCaptor.capture(),
            ArgumentMatchers.<ParameterizedTypeReference<List<JudgeRefData>>>any()
        ))
            .thenReturn(getJudgeRefDataResponse());

        List<JudgeRefData> judgeRefDataReturn = refDataService.getJudgeReferenceData("ABC", "user_token");

        assertEquals(4, judgeRefDataReturn.size());
        assertThat(judgeRefDataReturn).isEqualTo(judgeRefData);
        verify(jrdConfiguration, times(1)).getUrl();
        verify(jrdConfiguration, times(1)).getEndpoint();
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
        assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
            .isEqualTo("service_token");
    }

    private ResponseEntity<List<JudgeRefData>> getJudgeRefDataResponse() {
        List<JudgeRefData> responseData = new ArrayList<>();
        responseData.add(getJudicialRefData("Mr", "Murphy", "mr.murphy@email.com"));
        responseData.add(getJudicialRefData("Mr", "McGee", "mr.mcgee@email.com"));
        responseData.add(getJudicialRefData("Mr", "Brad", "mr.brad@email.com"));
        responseData.add(getJudicialRefData("Mrs", "Lee", "mrs.lee@email.com"));

        return new ResponseEntity<>(responseData, OK);
    }

    private JudgeRefData getJudicialRefData(String title, String surname, String emailId) {
        return JudgeRefData.builder().title(title).surname(surname).emailId(emailId).build();
    }

    @Test
    void shouldReturnEmptyList_whenRestTemplateThrowsException() {
        when(authTokenGenerator.generate()).thenReturn("service_token");

        when(restTemplate.exchange(
            any(URI.class),
            any(HttpMethod.class),
            any(HttpEntity.class),
            ArgumentMatchers.<ParameterizedTypeReference<List<JudgeRefData>>>any()
        )).thenThrow(new RuntimeException("Connection failed"));

        List<JudgeRefData> result = refDataService.getJudgeReferenceData("ABC", "user_token");

        assertThat(result).isEmpty();
        verify(jrdConfiguration, times(1)).getUrl();
        verify(jrdConfiguration, times(1)).getEndpoint();
    }
}
