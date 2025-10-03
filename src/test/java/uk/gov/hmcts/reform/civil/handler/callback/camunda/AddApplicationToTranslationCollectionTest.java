package uk.gov.hmcts.reform.civil.handler.callback.camunda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess.AddApplicationToTranslationCollection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_APPLICATION_TO_TRANSLATION_COLLECTION;

@ExtendWith(MockitoExtension.class)
public class AddApplicationToTranslationCollectionTest extends BaseCallbackHandlerTest {

    private AddApplicationToTranslationCollection handler;

    @Mock
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @BeforeEach
    void setUp() {

        handler = new AddApplicationToTranslationCollection(parentCaseUpdateHelper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(ADD_APPLICATION_TO_TRANSLATION_COLLECTION);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void should_updateTranslationCollectionWithApplicationDetails() {
            CaseData caseData = CaseData.builder().ccdCaseReference(Long.valueOf(123456)).build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            handler.handle(params);
            verify(parentCaseUpdateHelper, times(1))
                .updateCollectionForWelshApplication(caseData);
        }
    }
}
