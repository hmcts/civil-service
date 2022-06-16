package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.config.referencedata.JRDConfiguration;
import uk.gov.hmcts.reform.civil.model.referencedata.response.JudgeRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.JudicialRefDataService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(classes = {GeneralAppFeesService.class, RestTemplate.class, GeneralAppFeesConfiguration.class})
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
        List<JudgeRefData> judgeRefData = Arrays.asList(
                JudgeRefData.builder().title("Mr").surname("Murphy").emailId("mr.murphy@email.com").build(),
                JudgeRefData.builder().title("Mr").surname("McGee").emailId("mr.mcgee@email.com").build(),
                JudgeRefData.builder().title("Mr").surname("Brad").emailId("mr.brad@email.com").build(),
                JudgeRefData.builder().title("Mrs").surname("Lee").emailId("mrs.lee@email.com").build()
        );

        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<JudgeRefData>>>any()))
                .thenReturn(getJudgeRefDataResponse());

        List<JudgeRefData> judgeRefDataReturn = refDataService.getJudgeReferenceData("ABC", "user_token");

        assertThat(judgeRefDataReturn.size()).isEqualTo(4);
        assertThat(judgeRefDataReturn).isEqualTo(judgeRefData);
        verify(jrdConfiguration, times(1)).getUrl();
        verify(jrdConfiguration, times(1)).getEndpoint();
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
        assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
    }

    private ResponseEntity<List<JudgeRefData>> getJudgeRefDataResponse() {
        List<JudgeRefData> responseData = new ArrayList<JudgeRefData>();
        responseData.add(getJudicialRefData("Mr", "Murphy", "mr.murphy@email.com"));
        responseData.add(getJudicialRefData("Mr", "McGee", "mr.mcgee@email.com"));
        responseData.add(getJudicialRefData("Mr", "Brad", "mr.brad@email.com"));
        responseData.add(getJudicialRefData("Mrs", "Lee", "mrs.lee@email.com"));

        return new ResponseEntity<List<JudgeRefData>>(responseData, OK);
    }

    private JudgeRefData getJudicialRefData(String title, String surname, String emailId) {
        return JudgeRefData.builder().title(title).surname(surname).emailId(emailId).build();
    }
}
