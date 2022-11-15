package uk.gov.hmcts.reform.civil.service.stitching;

import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BundleRequestExecutorTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    CaseDetailsConverter caseDetailsConverter;

    BundleRequestExecutor bundleRequestExecutor;

    @BeforeEach
    void setup() {
        bundleRequestExecutor = new BundleRequestExecutor(restTemplate, serviceAuthTokenGenerator, caseDetailsConverter,
                                                          new ObjectMapper());
    }

    @Test
    void whenPostIsCalledAndEndpointWorks_thenTheCallSucceeds() {
        // Given
        String endpoint = "some url";
        CaseData expectedCaseData = CaseData.builder().build();
        CaseDetails responseCaseDetails = CaseDetails.builder().build();
        ResponseEntity<CaseDetails> responseEntity = new ResponseEntity<>(responseCaseDetails, HttpStatus.OK);
        given(caseDetailsConverter.toCaseData(responseCaseDetails)).willReturn(expectedCaseData);
        given(restTemplate.exchange(eq(endpoint), eq(HttpMethod.POST), any(), eq(CaseDetails.class)))
            .willReturn(responseEntity);

        // When
        BundleRequest request = BundleRequest.builder().build();
        CaseData result = bundleRequestExecutor.post(request, endpoint, "not important");

        // Then
        assertThat(result).isEqualTo(expectedCaseData);
    }

    @Test
    void whenPostIsCalledAndEndpointFails_thenReturnsNull() {
        // Given
        String endpoint = "some url";
        String errorData = "{\"data\":{\"respondentClaimResponseTypeForSpecGeneric\":\"FULL_ADMISSION\"},\"errors\":"
            + "[\"Stitching failed: prl-ccd-definitions-pr-662-cdam executing GET "
            + "http://prl-ccd-definitions-pr-662-cdam/cases/documents/5ce8143a-9a0a-45f2-9735-6b6f9236e4d3/binary\"],"
            + "\"warnings\":[],\"documentTaskId\":0}";
        given(restTemplate.exchange(eq(endpoint), eq(HttpMethod.POST), any(), eq(CaseDetails.class)))
            .willThrow(new RestClientResponseException("random exception", 500, "Internal server error",
                                                       HttpHeaders.EMPTY, errorData.getBytes(),
                                                       Charset.defaultCharset()));
        BundleRequest request = BundleRequest.builder().build();

        // When
        CaseData result = bundleRequestExecutor.post(request, endpoint, "not important");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void whenPostIsCalledAndEndpointReturnsNon200_thenReturnsNull() {
        // Given
        String endpoint = "some url";
        CaseDetails responseCaseDetails = CaseDetails.builder().build();
        ResponseEntity<CaseDetails> responseEntity = new ResponseEntity<>(responseCaseDetails,
                                                                          HttpStatus.NOT_ACCEPTABLE);
        given(restTemplate.exchange(eq(endpoint), eq(HttpMethod.POST), any(), eq(CaseDetails.class)))
            .willReturn(responseEntity);

        // When
        BundleRequest request = BundleRequest.builder().build();
        CaseData result = bundleRequestExecutor.post(request, endpoint, "not important");

        // Then
        assertThat(result).isNull();
    }

}
