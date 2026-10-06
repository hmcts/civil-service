package uk.gov.hmcts.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

/**
 * Registers {@link CoreCaseDataApiMockHelper}. The mocks it drives are supplied by
 * {@link MockCoreCaseDataApiDependencies}, which tests using this configuration must also declare,
 * because {@code @MockitoBean} is not supported on fields of configuration classes.
 */
@TestConfiguration
public class CoreCaseDataApiMockHelperConfiguration {

    @Bean
    public CoreCaseDataApiMockHelper coreCaseDataApiMockHelper(CoreCaseDataApi coreCaseDataApi,
                                                               IdamClient idamClient,
                                                               AuthTokenGenerator authTokenGenerator) {
        return new CoreCaseDataApiMockHelper(coreCaseDataApi, idamClient, authTokenGenerator);
    }
}
