package uk.gov.hmcts.reform.unspec.service;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class OrganisationServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-token";

    private FeignException notFoundFeignException = new FeignException.NotFound(
        "not found message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "not found response body".getBytes(UTF_8));
    private Organisation expectedOrganisation = Organisation.builder()
        .organisationIdentifier("ORG ID")
        .build();

    @Mock
    private OrganisationApi organisationApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        given(organisationApi.findUserOrganisation(any(), any())).willReturn(expectedOrganisation);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Nested
    class FindOrganisation {

        @Test
        void shouldReturnOrganisation_whenInvoked() {
            var organisation = organisationService.findOrganisation(AUTH_TOKEN);

            verify(organisationApi).findUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN);
            assertThat(organisation).isEqualTo(Optional.of(expectedOrganisation));
        }

        @Test
        void shouldReturnEmptyOptional_whenOrganisationNotFound() {
            given(organisationApi.findUserOrganisation(any(), any())).willThrow(notFoundFeignException);
            var organisation = organisationService.findOrganisation(AUTH_TOKEN);

            verify(organisationApi).findUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN);
            assertThat(organisation).isEmpty();
        }
    }
}
