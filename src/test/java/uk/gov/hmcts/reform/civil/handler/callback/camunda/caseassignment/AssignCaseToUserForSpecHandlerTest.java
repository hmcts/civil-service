package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    AssignCaseToUserForSpecHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class AssignCaseToUserForSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AssignCaseToUserForSpecHandler assignCaseToUserForSpecHandler;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureToggleService toggleService;

    private CallbackParams params;

    @Test
    public void ldBlock() {
        when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(assignCaseToUserForSpecHandler.handledEvents().isEmpty());
        Assert.assertFalse(assignCaseToUserForSpecHandler.handledEvents().isEmpty());
    }

    @Nested
    class AssignHmctsServiceId {
        @BeforeEach
        void setup() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            when(paymentsConfiguration.getSpecSiteId()).thenReturn("AAA6");

            Map<String, Object> dataMap = objectMapper.convertValue(caseData, new TypeReference<>() {
            });
            params = callbackParamsOf(dataMap, CallbackType.SUBMITTED);
        }

        @Test
        void shouldReturnSupplementaryDataOnSubmitted() {
            assignCaseToUserForSpecHandler.handle(params);
            verify(coreCaseDataService).setSupplementaryData(any(), any(), eq(supplementaryData()));
        }

        private Map<String, Map<String, Map<String, Object>>> supplementaryData() {
            Map<String, Object> hmctsServiceIdMap = new HashMap<>();
            hmctsServiceIdMap.put("HMCTSServiceId", "AAA6");

            Map<String, Map<String, Object>> supplementaryDataRequestMap = new HashMap<>();
            supplementaryDataRequestMap.put("$set", hmctsServiceIdMap);

            Map<String, Map<String, Map<String, Object>>> supplementaryDataUpdates = new HashMap<>();
            supplementaryDataUpdates.put("supplementary_data_updates", supplementaryDataRequestMap);

            return supplementaryDataUpdates;
        }

    }

}

