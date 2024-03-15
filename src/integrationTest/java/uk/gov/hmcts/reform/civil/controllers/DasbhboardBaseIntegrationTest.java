package uk.gov.hmcts.reform.civil.controllers;

import feign.Client;
import org.junit.runner.RunWith;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@RunWith(SpringRunner.class)
@EnableFeignClients(defaultConfiguration = DasbhboardBaseIntegrationTest.MockMvcFeignConfiguration.class)
public class DasbhboardBaseIntegrationTest extends BaseIntegrationTest {

    public static class MockMvcFeignConfiguration {
        @Bean
        Client feignClient() {
            return new MockMvcFeignClient();
        }
    }

    protected static CallbackParams callbackParams(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
