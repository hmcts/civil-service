package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;

@ExtendWith(MockitoExtension.class)
public class StitchingCompleteCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private StitchingCompleteCallbackHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.findAndRegisterModules();
        handler = new StitchingCompleteCallbackHandler(mapper, featureToggleService);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(asyncStitchingComplete);
    }

    @Test
    void shouldSetCategoryId() {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
            .stitchedDocument(Optional.of(Document.builder()
                                              .documentUrl("url")
                                              .documentFileName("name")
                                              .build()))
            .build()));
        CaseData caseData = CaseData.builder().caseBundles(caseBundles).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getCaseBundles().get(0)
                       .getValue().getStitchedDocument().get().getCategoryID()).isEqualTo("bundles");
    }

    @Test
    void shouldSetBundleCreationBusinessProcessWhenBundleEventIsNotNull() {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
            .title("Trial Bundle")
            .stitchStatus(Optional.of("SUCCESS")).description("Trial Bundle")
            .stitchedDocument(Optional.of(Document.builder()
                                              .documentUrl("url")
                                              .documentFileName("name")
                                              .build()))
            .build()));
        CaseData caseData = CaseData.builder().caseBundles(caseBundles)
            .bundleEvent("BUNDLE_CREATED_NOTIFICATION").build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(featureToggleService.isAmendBundleEnabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
        assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo("BUNDLE_CREATION_NOTIFICATION");
    }

    @ParameterizedTest
    @MethodSource("provideBundlesForLatestBundleTest")
    void shouldReturnLatestBundle(List<IdValue<Bundle>> bundles, Optional<Bundle> expectedLatestBundle) {
        CaseData caseData = CaseData.builder().caseBundles(bundles).build();
        Optional<Bundle> latestBundle = StitchingCompleteCallbackHandler.getLatestBundle(caseData);
        assertThat(latestBundle).isEqualTo(expectedLatestBundle);
    }

    private static Stream<Arguments> provideBundlesForLatestBundleTest() {
        Bundle bundle1 = Bundle.builder().id("1").createdOn(Optional.of(LocalDateTime.of(2023, 1, 1, 0, 0))).build();
        Bundle bundle2 = Bundle.builder().id("2").createdOn(Optional.of(LocalDateTime.of(2023, 2, 1, 0, 0))).build();
        Bundle bundle3 = Bundle.builder().id("3").createdOn(Optional.of(LocalDateTime.of(2023, 3, 1, 0, 0))).build();

        return Stream.of(
            Arguments.of(List.of(), Optional.empty()),
            Arguments.of(List.of(new IdValue<>("1", bundle1)), Optional.of(bundle1)),
            Arguments.of(List.of(new IdValue<>("1", bundle1), new IdValue<>("2", bundle2)), Optional.of(bundle2)),
            Arguments.of(
                List.of(new IdValue<>("1", bundle1), new IdValue<>("2", bundle2), new IdValue<>("3", bundle3)),
                Optional.of(bundle3)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataForTriggerUpdateBundleCategoryIdTest")
    void shouldUpdateBundleCategoryId(CaseData caseData, YesOrNo expectedBundleError, String expectedBusinessProcessEvent) {
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(featureToggleService.isAmendBundleEnabled()).thenReturn(true);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getBundleError()).isEqualTo(expectedBundleError);
        if (expectedBusinessProcessEvent != null) {
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(expectedBusinessProcessEvent);
            assertThat(updatedData.getApplicantDocsUploadedAfterBundle().size()).isEqualTo(1);
            assertThat(updatedData.getRespondentDocsUploadedAfterBundle().size()).isEqualTo(1);
        } else {
            assertThat(updatedData.getBusinessProcess()).isNull();
            assertThat(updatedData.getApplicantDocsUploadedAfterBundle().size()).isEqualTo(caseData.getApplicantDocsUploadedAfterBundle().size());
            assertThat(updatedData.getRespondentDocsUploadedAfterBundle().size()).isEqualTo(caseData.getRespondentDocsUploadedAfterBundle().size());
        }
    }

    private static Stream<Arguments> provideCaseDataForTriggerUpdateBundleCategoryIdTest() {
        Bundle bundleSuccess = Bundle.builder().id("1").stitchStatus(Optional.of("SUCCESS")).build();
        Bundle bundleFailed = Bundle.builder().id("2").stitchStatus(Optional.of("FAILED")).build();
        List<Element<UploadEvidenceDocumentType>> docsUploadedAfterBundle = Stream.generate(() ->
                                                                                                ElementUtils.element(
                                                                                                    UploadEvidenceDocumentType.builder().build())
        ).limit(3).collect(Collectors.toList());

        return Stream.of(
            Arguments.of(
                CaseData.builder().applicantDocsUploadedAfterBundle(docsUploadedAfterBundle)
                    .respondentDocsUploadedAfterBundle(docsUploadedAfterBundle)
                    .caseBundles(List.of(new IdValue<>("1", bundleSuccess))).build(),
                null,
                null
            ),
            Arguments.of(
                CaseData.builder().applicantDocsUploadedAfterBundle(docsUploadedAfterBundle)
                    .respondentDocsUploadedAfterBundle(docsUploadedAfterBundle)
                    .caseBundles(List.of(new IdValue<>("1", bundleFailed))).build(),
                YesOrNo.YES,
                null
            ),
            Arguments.of(CaseData.builder().applicantDocsUploadedAfterBundle(docsUploadedAfterBundle)
                             .respondentDocsUploadedAfterBundle(docsUploadedAfterBundle)
                             .caseBundles(List.of(new IdValue<>("1", bundleSuccess))).bundleEvent(
                    "BUNDLE_CREATED_NOTIFICATION").build(), null, "BUNDLE_CREATION_NOTIFICATION"),
            Arguments.of(CaseData.builder().applicantDocsUploadedAfterBundle(docsUploadedAfterBundle)
                             .respondentDocsUploadedAfterBundle(docsUploadedAfterBundle)
                             .caseBundles(List.of(new IdValue<>("1", bundleSuccess))).bundleEvent(
                    "AMEND_RESTITCH_BUNDLE").build(), null, "AMEND_RESTITCH_BUNDLE")
        );
    }
}
