package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_MANUALLY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_UPLOAD;

@ExtendWith(MockitoExtension.class)
class SetUploadTimelineTypeFlagTest {

    private ObjectMapper objectMapper;

    @Mock
    private CallbackParams callbackParams;

    private SetUploadTimelineTypeFlag setUploadTimelineTypeFlag;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        setUploadTimelineTypeFlag = new SetUploadTimelineTypeFlag(objectMapper);
        caseData = CaseData.builder()
            .isRespondent1(YES)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    private void assertFlags(AboutToStartOrSubmitCallbackResponse response, Set<DefendantResponseShowTag> expectedFlags) {
        Set<DefendantResponseShowTag> actualFlags = objectMapper.convertValue(
            response.getData().get("showConditionFlags"),
            objectMapper.getTypeFactory().constructCollectionType(Set.class, DefendantResponseShowTag.class)
        );
        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Test
    void shouldAddTimelineUploadFlagWhenTimelineIsUpload() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.UPLOAD)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, EnumSet.of(TIMELINE_UPLOAD));
    }

    @Test
    void shouldAddTimelineManualFlagWhenTimelineIsManual() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.MANUAL)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, EnumSet.of(TIMELINE_MANUALLY));
    }

    @Test
    void shouldRemoveExistingTimelineFlags() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.UPLOAD)
            .showConditionFlags(new HashSet<>(EnumSet.of(TIMELINE_MANUALLY, TIMELINE_UPLOAD)))
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, EnumSet.of(TIMELINE_UPLOAD));
    }

    @Test
    void shouldNotAddTimelineManualFlagWhenTimelineIsNotManual() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(null)
            .isRespondent1(YES)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, new HashSet<>());
    }

    @Test
    void shouldNotAddTimelineUploadFlagWhenRespondent1IsNotYes() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.UPLOAD)
            .isRespondent1(null)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, new HashSet<>());
    }

    @Test
    void shouldAddTimelineUploadFlagForRespondent2WhenTimelineIsUpload() {
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specClaimResponseTimelineList2(TimelineUploadTypeSpec.UPLOAD)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, EnumSet.of(TIMELINE_UPLOAD));
    }

    @Test
    void shouldAddTimelineManualFlagForRespondent2WhenTimelineIsManual() {
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specClaimResponseTimelineList2(TimelineUploadTypeSpec.MANUAL)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, EnumSet.of(TIMELINE_MANUALLY));
    }

    @Test
    void shouldAddTimelineManualFlagWhenRespondent1TimelineIsManual() {
        caseData = caseData.toBuilder()
            .isRespondent1(YES)
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.MANUAL)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, EnumSet.of(TIMELINE_MANUALLY));
    }

    @Test
    void shouldNotAddAnyTimelineFlagForRespondent2WhenTimelineIsNull() {
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specClaimResponseTimelineList2(null)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);
        assertFlags(response, new HashSet<>());
    }
}
