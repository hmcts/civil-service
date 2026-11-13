package uk.gov.hmcts.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class CoreCaseDataApiMockHelperConfiguration {

    @Bean
    public CoreCaseDataApi coreCaseDataApi() {
        return mock(CoreCaseDataApi.class);
    }

    @Bean
    public IdamClient idamClient() {
        return mock(IdamClient.class);
    }

    @Bean
    public AuthTokenGenerator authTokenGenerator() {
        return mock(AuthTokenGenerator.class);
    }

    @Bean
    public CoreCaseDataApiMockHelper coreCaseDataApiMockHelper(CoreCaseDataApi coreCaseDataApi,
                                                               IdamClient idamClient,
                                                               AuthTokenGenerator authTokenGenerator) {
        return new CoreCaseDataApiMockHelper(coreCaseDataApi, idamClient, authTokenGenerator);
    }
}
