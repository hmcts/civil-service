package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
public class SetUploadTimelineTypeFlagTest {

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
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.UPLOAD)
            .showConditionFlags(new HashSet<>(EnumSet.of(TIMELINE_MANUALLY)))
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldAddTimelineUploadFlagWhenTimelineIsUpload() {
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);

        Set<DefendantResponseShowTag> expectedFlags = new HashSet<>(EnumSet.of(TIMELINE_UPLOAD));
        Set<DefendantResponseShowTag> actualFlags = objectMapper.convertValue(
            response.getData().get("showConditionFlags"), objectMapper.getTypeFactory().constructCollectionType(Set.class, DefendantResponseShowTag.class)
        );
        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Test
    void shouldAddTimelineManualFlagWhenTimelineIsManual() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.MANUAL)
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);

        Set<DefendantResponseShowTag> expectedFlags = new HashSet<>(EnumSet.of(TIMELINE_MANUALLY));
        Set<DefendantResponseShowTag> actualFlags = objectMapper.convertValue(
            response.getData().get("showConditionFlags"), objectMapper.getTypeFactory().constructCollectionType(Set.class, DefendantResponseShowTag.class)
        );
        assertThat(actualFlags).isEqualTo(expectedFlags);
    }

    @Test
    void shouldRemoveExistingTimelineFlags() {
        caseData = caseData.toBuilder()
            .specClaimResponseTimelineList(TimelineUploadTypeSpec.UPLOAD)
            .showConditionFlags(new HashSet<>(EnumSet.of(TIMELINE_MANUALLY, TIMELINE_UPLOAD)))
            .build();

        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setUploadTimelineTypeFlag.execute(callbackParams);

        Set<DefendantResponseShowTag> expectedFlags = new HashSet<>(EnumSet.of(TIMELINE_UPLOAD));
        Set<DefendantResponseShowTag> actualFlags = objectMapper.convertValue(
            response.getData().get("showConditionFlags"), objectMapper.getTypeFactory().constructCollectionType(Set.class, DefendantResponseShowTag.class)
        );
        assertThat(actualFlags).isEqualTo(expectedFlags);
    }
}
