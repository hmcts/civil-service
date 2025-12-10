package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.event.BundleCreationTriggerEventHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.bundle.Bundle;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.bundle.BundleData;
import uk.gov.hmcts.reform.civil.model.bundle.BundleDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_RESTITCH_BUNDLE;

@ExtendWith(MockitoExtension.class)
class AmendRestitchBundleCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private AmendRestitchBundleCallbackHandler handler;
    @Mock
    private BundleCreationService bundleCreationService;
    @Mock
    private BundleCreationTriggerEventHandler bundleCreationTriggerEventHandler;
    @Mock
    private FeatureToggleService featureToggleService;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    @BeforeEach
    public void setup() {
        mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
        handler = new AmendRestitchBundleCallbackHandler(
            mapper,
            bundleCreationService,
            bundleCreationTriggerEventHandler,
            featureToggleService
        );

    }

    private List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> prepareCaseBundles() {
        uk.gov.hmcts.reform.civil.model.Bundle bundle1 = new uk.gov.hmcts.reform.civil.model.Bundle(
            "1",
            "Trial Bundle - Static",
            "Trial Bundle - Static",
            null,
            null,
            Optional.of("NEW"),
            Optional.empty(),
            uk.gov.hmcts.reform.civil.enums.YesOrNo.NO,
            uk.gov.hmcts.reform.civil.enums.YesOrNo.NO,
            null
        );
        bundle1.setCreatedOn(Optional.of(LocalDateTime.now()));
        bundle1.setBundleHearingDate(Optional.of(LocalDate.of(2023, 12, 12)));
        List<IdValue<uk.gov.hmcts.reform.civil.model.Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", bundle1));

        uk.gov.hmcts.reform.civil.model.Bundle bundle2 = new uk.gov.hmcts.reform.civil.model.Bundle(
            "1",
            "Trial Bundle - Old",
            "Trial Bundle - Old",
            null,
            null,
            Optional.of("NEW"),
            Optional.empty(),
            uk.gov.hmcts.reform.civil.enums.YesOrNo.NO,
            uk.gov.hmcts.reform.civil.enums.YesOrNo.NO,
            null
        );
        bundle2.setCreatedOn(Optional.of(LocalDateTime.now()));
        bundle2.setBundleHearingDate(Optional.of(LocalDate.now().plusWeeks(5).plusDays(5)));
        caseBundles.add(new IdValue<>("2", bundle2));

        return caseBundles;
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnNoError_WhenMidIsInvoked() {
            when(featureToggleService.isAmendBundleEnabled()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnBundle_AndOverwriteExistingBundleForHearingDate() {
            when(featureToggleService.isAmendBundleEnabled()).thenReturn(true);

            Document stitchedDocument = new Document();
            stitchedDocument.setDocumentUrl(TEST_URL);
            stitchedDocument.setDocumentFileName(TEST_FILE_NAME);

            BundleDetails bundleDetails = new BundleDetails();
            bundleDetails.setTitle("Trial Bundle - New");
            bundleDetails.setId("2");
            bundleDetails.setStitchStatus("new");
            bundleDetails.setFileName("Trial Bundle.pdf");
            bundleDetails.setDescription("Trial Bundle - New");
            bundleDetails.setBundleHearingDate(LocalDate.of(2023, 12, 12));
            bundleDetails.setStitchedDocument(stitchedDocument);
            bundleDetails.setCreatedOn(LocalDateTime.of(2023, 11, 12, 1, 1, 1));

            Bundle bundle = new Bundle(bundleDetails);

            IdValue<Bundle> packedBundle = new IdValue<>("2", bundle);

            BundleData bundleData = new BundleData(List.of(bundle));

            BundleCreateResponse bundleCreateResponse = new BundleCreateResponse(bundleData, null);
            CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome()
                .caseBundles(prepareCaseBundles())
                .build();

            when(bundleCreationService.createBundle(anyLong())).thenReturn(bundleCreateResponse);
            when(bundleCreationTriggerEventHandler.prepareNewBundle(any(), any())).thenAnswer(x -> packedBundle);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            List<IdValue<Bundle>> actualData = mapper.convertValue(response.getData().get("caseBundles"), new TypeReference<List<IdValue<Bundle>>>() {});
            assertThat(actualData.get(1).getValue()).isEqualTo(packedBundle.getValue());
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(AMEND_RESTITCH_BUNDLE);
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnNoError_WhenSubmittedIsInvoked() {
            when(featureToggleService.isAmendBundleEnabled()).thenReturn(false);
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            when(featureToggleService.isAmendBundleEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# The bundle is being restitched")
                    .confirmationBody("### What happens next\nCheck the Bundles tab to see if the restitch has been successful. "
                                          + "\nRestitching can take up to 5 minutes. "
                                          + "\n\nAll parties will be notified when the new bundle is ready to view.")
                    .build());
        }
    }
}
