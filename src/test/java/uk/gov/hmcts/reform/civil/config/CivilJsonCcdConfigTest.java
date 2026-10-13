package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.config.CcdCaseDataMapperConfiguration;
import uk.gov.hmcts.ccd.sdk.impl.json.JsonCallbackBridge;
import uk.gov.hmcts.ccd.sdk.json.JsonCCDConfigSupport;
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.callback.DecentralisedCallbackHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.http.HttpResponseHeadersService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

/**
 * Loads the civil-ccd-definition JSON the build packages, through the decentralised runtime and this service's
 * callback dispatcher, as an environment would.
 */
class CivilJsonCcdConfigTest {

    @Test
    void loadsTheNonProdDefinitions() {
        context("*-prod.json").run(context -> {
            assertThat(context).hasNotFailed();
            var configs = resolved(context.getBeansOfType(CCDConfig.class).values());

            var civil = configs.get(CASE_TYPE);
            assertThat(civil.getEvents()).containsKeys("CREATE_CLAIM", "DISCONTINUE_CLAIM", "WITHDRAW_CLAIM");
            assertThat(civil.getEvents().get("CREATE_CLAIM").getAboutToSubmitCallback()).isNotNull();
            assertThat(configs.get(GENERALAPPLICATION_CASE_TYPE).getEvents()).isNotEmpty();
        });
    }

    @Test
    void loadsTheProdDefinitions() {
        context("*-nonprod.json,*-testing*.json").run(context -> {
            assertThat(context).hasNotFailed();
            var configs = resolved(context.getBeansOfType(CCDConfig.class).values());

            assertThat(configs.get(CASE_TYPE).getEvents())
                .containsKey("CREATE_CLAIM")
                .doesNotContainKeys("DISCONTINUE_CLAIM", "WITHDRAW_CLAIM");
            assertThat(configs.get(GENERALAPPLICATION_CASE_TYPE).getEvents()).isNotEmpty();
        });
    }

    private static ApplicationContextRunner context(String excludedFiles) {
        return new ApplicationContextRunner()
            .withUserConfiguration(CivilJsonCcdConfig.class, JsonCCDConfigSupport.class, JsonCallbackBridge.class)
            .withBean(DecentralisedCallbackHandler.class, () -> new DecentralisedCallbackHandler(
                mock(CallbackHandlerFactory.class),
                mock(CaseDetailsConverter.class),
                new HttpResponseHeadersService(new ObjectMapper())
            ))
            .withBean(CcdCaseDataMapperConfiguration.CCD_CASE_DATA_OBJECT_MAPPER, ObjectMapper.class, ObjectMapper::new)
            .withBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class,
                      RequestMappingHandlerMapping::new)
            .withPropertyValues(
                "civil.decentralised.enabled=true",
                "civil.decentralised.definitions-root=classpath:ccd-definitions",
                "civil.decentralised.excluded-files=" + excludedFiles,
                "decentralisation.local-callback-placeholder=CCD_DEF_CASE_SERVICE_BASE_URL"
            );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, ResolvedCCDConfig<?, ?, ?>> resolved(Collection<CCDConfig> configs) {
        return new CCDDefinitionGenerator(List.copyOf((Collection) configs), null).loadConfigs().stream()
            .collect(Collectors.toMap(ResolvedCCDConfig::getCaseType, Function.identity()));
    }
}
