package uk.gov.hmcts.reform.civil.service.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.EvidenceManagementApiClient;
import uk.gov.hmcts.reform.civil.exceptions.RetryableStitchingException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.nio.charset.Charset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BundleRequestExecutorTest {

    @Mock
    EvidenceManagementApiClient evidenceManagementApiClient;
    @Mock
    AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    CaseDetailsConverter caseDetailsConverter;

    BundleRequestExecutor bundleRequestExecutor;

    @BeforeEach
    void setup() {
        bundleRequestExecutor = new BundleRequestExecutor(
            evidenceManagementApiClient,
            serviceAuthTokenGenerator,
            caseDetailsConverter,
            new ObjectMapper()
        );
    }

    @Test
    void whenPostIsCalledAndEndpointWorks_thenTheCallSucceeds() {
        // Given
        String endpoint = "some url";
        CaseData expectedCaseData = CaseData.builder().build();
        CaseDetails responseCaseDetails = CaseDetails.builder().build();
        ResponseEntity<CaseDetails> responseEntity = new ResponseEntity<>(responseCaseDetails, HttpStatus.OK);
        given(caseDetailsConverter.toCaseData(responseCaseDetails)).willReturn(expectedCaseData);
        given(evidenceManagementApiClient.stitchBundle(any(), any(), any(BundleRequest.class)))
            .willReturn(responseEntity);

        // When
        BundleRequest request = BundleRequest.builder().build();
        var result = bundleRequestExecutor.post(request, endpoint, "not important");

        // Then
        assertThat(result.get()).isEqualTo(expectedCaseData);
    }

    @Test
    void whenPostIsCalledAndEndpointFails_thenReturnsEmpty() {
        // Given
        String endpoint = "some url";
        String errorData = "{\"data\":{\"respondentClaimResponseTypeForSpecGeneric\":\"FULL_ADMISSION\"},\"errors\":"
            + "[\"Stitching failed: prl-ccd-definitions-pr-662-cdam executing GET "
            + "http://prl-ccd-definitions-pr-662-cdam/cases/documents/5ce8143a-9a0a-45f2-9735-6b6f9236e4d3/binary\"],"
            + "\"warnings\":[],\"documentTaskId\":0}";
        given(evidenceManagementApiClient.stitchBundle(any(), any(), any(BundleRequest.class)))
            .willThrow(new RestClientResponseException("random exception", 500, "Internal server error",
                                                       HttpHeaders.EMPTY, errorData.getBytes(),
                                                       Charset.defaultCharset()
            ));
        BundleRequest request = BundleRequest.builder().build();

        RetryableStitchingException exception = assertThrows(
            RetryableStitchingException.class,
            () -> bundleRequestExecutor.post(request, endpoint, "not important")
        );

        assertEquals("Stitching failed, retrying...", exception.getMessage());
    }

    @Test
    void whenPostIsCalledAndEndpointReturnsNon200_thenReturnsEmpty() {
        // Given
        String endpoint = "some url";
        CaseDetails responseCaseDetails = CaseDetails.builder().build();
        ResponseEntity<CaseDetails> responseEntity = new ResponseEntity<>(
            responseCaseDetails,
            HttpStatus.NOT_ACCEPTABLE
        );
        given(evidenceManagementApiClient.stitchBundle(any(), any(), any(BundleRequest.class)))
            .willReturn(responseEntity);

        // When
        BundleRequest request = BundleRequest.builder().build();
        RetryableStitchingException exception = assertThrows(
            RetryableStitchingException.class,
            () -> bundleRequestExecutor.post(request, endpoint, "not important")
        );

        assertEquals("Stitching failed, retrying...", exception.getMessage());
    }

    @Test
    public void whenRecoveryNeededReturnEmptyOptional() {
        assertThat(bundleRequestExecutor.recover(new RetryableStitchingException(), null, null, null))
            .isEqualTo(Optional.empty());
    }

    @Test
    public void whenRuntimeRecoveryNeededReturnEmptyOptional() {
        assertThat(bundleRequestExecutor.recover(new RuntimeException(), null, null, null))
            .isEqualTo(Optional.empty());
    }
}
