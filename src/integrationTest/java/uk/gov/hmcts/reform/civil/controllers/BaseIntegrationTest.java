package uk.gov.hmcts.reform.civil.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.service.AuthorisationService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class})
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    protected static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3k"
        + "rV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzb2xpY2l0b3JAZXhhbXBsZS5jb20iLCJhdXRoX2xldmVsIjowLC"
        + "JhdWRpdFRyYWNraW5nSWQiOiJiNGJmMjJhMi02ZDFkLTRlYzYtODhlOS1mY2NhZDY2NjM2ZjgiLCJpc3MiOiJodHRwOi8vZnItYW06ODA4M"
        + "C9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFu"
        + "dElkIjoiZjExMTk3MGQtMzQ3MS00YjY3LTkxMzYtZmYxYzk0MjMzMTZmIiwiYXVkIjoieHVpX3dlYmFwcCIsIm5iZiI6MTU5NDE5NzI3NCw"
        + "iZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2"
        + "VyIiwibWFuYWdlLXVzZXIiXSwiYXV0aF90aW1lIjoxNTk0MTk3MjczMDAwLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU5NDIyNjA3NCwia"
        + "WF0IjoxNTk0MTk3Mjc0LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiYTJmNThmYzgtMmIwMy00M2I0LThkOTMtNmU0NWQyZTU0OTcxIn0."
        + "PTWXIvTTw54Ob1XYdP_x4l5-ITWDdbAY3-IPAPFkHDmjKgEVweabxrDIp2_RSoAcyZgza8LqJSTc00-_RzZ079nyl9pARy08BpljLZCmYdo"
        + "F2RO8CHuEVagF-SQdL37d-4pJPIMRChO0AmplBj1qMtVbuRd3WGNeUvoCtStdviFwlxvzRnLdHKwCi6AQHMaw1V9n9QyU9FxNSbwmNsCDt7"
        + "k02vLJDY9fLCsFYy5iWGCjb8lD1aX1NTv7jz2ttNNv7-smqp6L3LSSD_LCZMpf0h_3n5RXiv-N3vNpWe4ZC9u0AWQdHEE9QlKTZlsqwKSog"
        + "3yJWhyxAamdMepgW7Z8jQ";

    protected static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    protected static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
    protected static final String GET_TASKS_ITEMS_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    protected static final UserInfo USER_INFO = UserInfo.builder()
        .sub("solicitor@example.com")
        .roles(of("caseworker-civil-solicitor"))
        .build();

    protected static final String s2sToken = "s2s AuthToken";
    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;
    @MockBean
    protected UserService userService;
    @MockBean
    protected Authentication authentication;
    @MockBean
    protected SecurityContext securityContext;
    @MockBean
    protected JwtDecoder jwtDecoder;
    @MockBean
    public AuthorisationService authorisationService;
    @MockBean
    public IdamApi idamApi;

    @MockBean
    public RequestAuthorizer<User> userRequestAuthorizerMock;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    public void setUpBase() {
        when(authorisationService.isServiceAuthorized(any())).thenReturn(true);
        when(userService.getAccessToken(any(), any())).thenReturn("arbitrary access token");
        when(userService.getUserInfo(anyString())).thenReturn(USER_INFO);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        setSecurityAuthorities(authentication);
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_app");
        when(jwtDecoder.decode(anyString())).thenReturn(getJwt());
        when(idamApi.retrieveUserDetails(anyString()))
            .thenReturn(UserDetails.builder().forename("Claimant").surname("test").build());
        when(userRequestAuthorizerMock.authorise(any())).thenReturn(new User("1", Set.of("caseworker-civil-solicitor")));
    }

    protected void setSecurityAuthorities(Authentication authenticationMock, String... authorities) {
        when(authenticationMock.getPrincipal()).thenReturn(getJwt());

        Collection<? extends GrantedAuthority> authorityCollection = Stream.of(authorities)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(ArrayList::new));

        when(authenticationMock.getAuthorities()).thenAnswer(invocationOnMock -> authorityCollection);
    }

    private Jwt getJwt() {
        return Jwt.withTokenValue(BEARER_TOKEN)
            .claim("exp", Instant.ofEpochSecond(1585763216))
            .claim("iat", Instant.ofEpochSecond(1585734416))
            .claim("token_type", "Bearer")
            .claim("tokenName", "access_token")
            .claim("expires_in", 28800)
            .header("kid", "b/O6OvVv1+y+WgrH5Ui9WTioLt0=")
            .header("typ", "RS256")
            .header("alg", "RS256")
            .build();
    }

    @SneakyThrows
    protected <T> ResultActions doPost(String auth, T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.post(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }

    @SneakyThrows
    protected <T> ResultActions doFilePost(String auth, MockMultipartFile file, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.multipart(urlTemplate, uriVars)
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE));
    }

    @SneakyThrows
    protected <T> ResultActions doGet(String auth, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.get(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @SneakyThrows
    protected <T> ResultActions doPut(String auth, T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @SneakyThrows
    protected <T> ResultActions doDelete(String auth, T content, String urlTemplate, Object... uriVars) {
        return mockMvc.perform(
            MockMvcRequestBuilders.delete(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(content)));
    }

    protected String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                String.format("Failed to serialize '%s' to JSON", input.getClass().getSimpleName()), e
            );
        }
    }

    protected List<TaskList> toTaskList(String input) {
        try {
            return Arrays.asList(objectMapper.readValue(input, TaskList[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                String.format("Failed to serialize '%s' to JSON", input.getClass().getSimpleName()), e
            );
        }
    }

}
