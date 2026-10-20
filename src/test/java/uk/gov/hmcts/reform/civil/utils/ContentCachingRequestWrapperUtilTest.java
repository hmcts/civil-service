package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ContentCachingRequestWrapperUtil.getCaseId;

@ExtendWith(MockitoExtension.class)
class ContentCachingRequestWrapperUtilTest {

    @Mock
    private ContentCachingRequestWrapper request;

    @Test
    void getCaseId_readsQueryParamWhenPathHasNoCaseId() {
        when(request.getRequestURI()).thenReturn("/case/document/downloadDocument/undefined");
        when(request.getParameter("caseId")).thenReturn("1767636822302602");

        assertThat(getCaseId(request)).isEqualTo("1767636822302602");
    }

    @Test
    void getCaseId_prefersPathSegmentOverQueryParam() {
        when(request.getRequestURI()).thenReturn("/cases/caseId/1111222233334444");

        assertThat(getCaseId(request)).isEqualTo("1111222233334444");
    }

    @Test
    void getCaseId_fallsBackToBodyWhenQueryParamAbsent() {
        when(request.getRequestURI()).thenReturn("/case/document/downloadDocument/undefined");
        when(request.getParameter("caseId")).thenReturn(null);
        when(request.getContentAsByteArray())
            .thenReturn("{\"id\":\"body-case\"}".getBytes(StandardCharsets.UTF_8));

        assertThat(getCaseId(request)).isEqualTo("body-case");
    }

    @Test
    void getCaseId_doesNotTreatDocumentPathAsCaseId() {
        when(request.getRequestURI()).thenReturn("/case/document/downloadDocument/undefined");
        when(request.getParameter("caseId")).thenReturn(null);
        when(request.getContentAsByteArray()).thenReturn(new byte[0]);

        assertThat(getCaseId(request)).isEmpty();
    }

    @Test
    void getCaseId_returnsEmptyWhenRequestIsNull() {
        assertThat(getCaseId(null)).isEmpty();
    }
}
