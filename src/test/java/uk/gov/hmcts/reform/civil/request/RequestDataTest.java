package uk.gov.hmcts.reform.civil.request;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.HandlerMapping;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RequestDataTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String CACHED_AUTH_TOKEN = "Cached bearer token";
    private static final String USER_ID = "example.gov.uk";
    private static final String CACHED_USER_ID = "cached.gov.uk";
    private static final Set<String> CACHED_USER_ROLES = Set.of("role1", "role2");

    private static final String CACHED_CASE_ID = "1234567891023456";

    private static final String CCD_CASE_IDENTIFIER_KEY = "ccd-case-identifier";

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CacheAwareRequestData requestData;

    @Nested
    class NotCachedRequestData {

        @Test
        void shouldReturnAuthorisationHeaderFromRequest() {
            when(httpServletRequest.getHeader("authorization")).thenReturn(AUTH_TOKEN);
            assertThat(requestData.authorisation()).isEqualTo(AUTH_TOKEN);
        }

        @Test
        void shouldReturnUserIdFromRequest() {
            when(httpServletRequest.getHeader("user-id")).thenReturn(USER_ID);
            assertThat(requestData.userId()).isEqualTo(USER_ID);
        }

        @Test
        void shouldReturnEmptySetOfUserRolesIfHeaderNotPresent() {
            when(httpServletRequest.getHeader("user-roles")).thenReturn(null);
            assertThat(requestData.userRoles()).isEmpty();
        }

        @Test
        void shouldReturnEmptySetOfUserRolesIfHeaderIsEmpty() {
            when(httpServletRequest.getHeader("user-roles")).thenReturn("");
            assertThat(requestData.userRoles()).isEmpty();
        }

        @Test
        void shouldReturnEmptySetOfUserRolesIfHeaderIsBlank() {
            when(httpServletRequest.getHeader("user-roles")).thenReturn(" ");
            assertThat(requestData.userRoles()).isEmpty();
        }

        @Test
        void shouldReturnUserRolesIfSingleRolePresent() {
            when(httpServletRequest.getHeader("user-roles")).thenReturn("role1");
            assertThat(requestData.userRoles()).containsExactly("role1");
        }

        @Test
        void shouldReturnAllUserRoles() {
            when(httpServletRequest.getHeader("user-roles")).thenReturn("role1,role2");
            assertThat(requestData.userRoles()).containsExactly("role1", "role2");
        }

        @Test
        void shouldReturnNormalisedUserRoles() {
            when(httpServletRequest.getHeader("user-roles")).thenReturn(" role1 , role2");
            assertThat(requestData.userRoles()).containsExactly("role1", "role2");
        }

        @Test
        void shouldReturnCaseIdWhenHttpMethodIsPost() throws IOException {
            SimpleCallbackRequest request = new SimpleCallbackRequest(SimpleCaseDetails.builder()
                                                                          .id(1234567891023456L)
                                                                          .build());
            String requestString = new ObjectMapper().writeValueAsString(request);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    IOUtils.toInputStream(requestString, StandardCharsets.UTF_8)
                )
            );
            when(httpServletRequest.getReader()).thenReturn(reader);
            when(httpServletRequest.getMethod()).thenReturn(HttpMethod.POST.name());

            assertThat(requestData.caseId()).isEqualTo(CACHED_CASE_ID);
        }

        @Test
        void shouldReturnCaseIdWhenHttpMethodIsNotPost() {
            when(httpServletRequest.getMethod()).thenReturn(HttpMethod.GET.name());
            when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of(CCD_CASE_IDENTIFIER_KEY, CACHED_CASE_ID));

            assertThat(requestData.caseId()).isEqualTo(CACHED_CASE_ID);
        }
    }

    @Nested
    class CachedRequestData {

        final SimpleRequestData cachedRequestData = new SimpleRequestData(CACHED_AUTH_TOKEN, CACHED_USER_ID,
                                                                          CACHED_USER_ROLES, CACHED_CASE_ID);

        @BeforeEach
        void setup() {
            RequestDataCache.add(cachedRequestData);
        }

        @AfterEach
        void cleanUp() {
            RequestDataCache.remove();
        }

        @Test
        void shouldReturnAuthorisationHeaderFromCache() {
            assertThat(requestData.authorisation()).isEqualTo(CACHED_AUTH_TOKEN);
            verifyNoInteractions(httpServletRequest);
        }

        @Test
        void shouldReturnUserIdFromCache() {
            assertThat(requestData.userId()).isEqualTo(CACHED_USER_ID);
            verifyNoInteractions(httpServletRequest);
        }

        @Test
        void shouldReturnUserRolesFromCache() {
            assertThat(requestData.userRoles()).isEqualTo(CACHED_USER_ROLES);
            verifyNoInteractions(httpServletRequest);
        }

        @Test
        void shouldReturnCaseIdFromCache() {
            assertThat(requestData.caseId()).isEqualTo(CACHED_CASE_ID);
        }
    }
}
