package uk.gov.hmcts.reform.civil.interceptors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdPersistenceInterceptorTest {

    @Mock
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Test
    void letsDataStoreThrough() throws Exception {
        when(serviceAuthorisationApi.getServiceName("Bearer data-store-token")).thenReturn("ccd_data");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean handled = interceptor().preHandle(request("data-store-token"), response, new Object());

        assertThat(handled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsAnotherService() throws Exception {
        when(serviceAuthorisationApi.getServiceName("Bearer citizen-ui-token")).thenReturn("civil-citizen-ui");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean handled = interceptor().preHandle(request("Bearer citizen-ui-token"), response, new Object());

        assertThat(handled).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void rejectsAnInvalidServiceToken() throws Exception {
        when(serviceAuthorisationApi.getServiceName("Bearer expired")).thenThrow(new IllegalStateException("401"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean handled = interceptor().preHandle(request("expired"), response, new Object());

        assertThat(handled).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void rejectsARequestWithoutAServiceToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean handled = interceptor().preHandle(new MockHttpServletRequest(), response, new Object());

        assertThat(handled).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        verifyNoInteractions(serviceAuthorisationApi);
    }

    private CcdPersistenceInterceptor interceptor() {
        return new CcdPersistenceInterceptor(serviceAuthorisationApi);
    }

    private static MockHttpServletRequest request(String serviceAuthorization) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ccd-persistence/cases");
        request.addHeader("ServiceAuthorization", serviceAuthorization);
        return request;
    }
}
