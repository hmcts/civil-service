package uk.gov.hmcts.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.civil.scheduler.CoreCaseDataApiMockHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@TestConfiguration
public class CoreCaseDataApiMockHelperConfiguration {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Bean
    public CoreCaseDataApiMockHelper coreCaseDataApiMockHelper() {
        return new CoreCaseDataApiMockHelper(coreCaseDataApi, idamClient, authTokenGenerator);
    }
}
