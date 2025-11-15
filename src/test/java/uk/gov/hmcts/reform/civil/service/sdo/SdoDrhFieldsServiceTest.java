package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoDrhFieldsServiceTest {

    @Mock
    private SdoLocationService locationService;
    @Mock
    private SdoTrackDefaultsService trackDefaultsService;
    @Mock
    private SdoJourneyToggleService journeyToggleService;
    @Mock
    private SdoDeadlineService deadlineService;

    private SdoDrhFieldsService service;

    @BeforeEach
    void setUp() {
        service = new SdoDrhFieldsService(locationService, trackDefaultsService, journeyToggleService, deadlineService);

        when(trackDefaultsService.defaultIncludeInOrderToggle()).thenReturn(List.of(IncludeInOrderToggle.INCLUDE));
        when(locationService.buildCourtLocationForSdoR2(any(), any())).thenReturn(dynamicList("court", "Court A"));
        when(locationService.buildLocationList(any(), anyBoolean(), any())).thenReturn(dynamicList("alt", "Alt Court"));
        when(deadlineService.calendarDaysFromNow(anyInt())).thenAnswer(invocation -> {
            int days = invocation.getArgument(0, Integer.class);
            return LocalDate.of(2024, 1, 1).plusDays(days);
        });
    }

    @Test
    void shouldPopulateDrhFields() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder().build().toBuilder();
        DynamicList hearingMethodList = dynamicList("initial", HearingMethod.TELEPHONE.getLabel());

        service.populateDrhFields(CaseData.builder().build(), builder, Optional.of(RequestedCourt.builder().build()),
            hearingMethodList, Collections.emptyList());

        CaseData result = builder.build();

        assertThat(result.getSdoR2SmallClaimsJudgesRecital()).isNotNull();
        assertThat(result.getSdoR2SmallClaimsHearing()).isNotNull();
        assertThat(result.getSdoR2SmallClaimsHearing().getMethodOfHearing().getValue().getLabel())
            .isEqualTo(HearingMethod.TELEPHONE.getLabel());
        assertThat(result.getSdoR2SmallClaimsUploadDocToggle()).containsExactly(IncludeInOrderToggle.INCLUDE);
    }

    @Test
    void shouldDelegateCarmFieldsToJourneyService() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.populateDrhFields(caseData, builder, Optional.empty(),
            dynamicList("initial", HearingMethod.TELEPHONE.getLabel()), Collections.emptyList());

        verify(journeyToggleService).applyR2SmallClaimsMediation(eq(caseData), eq(builder),
            eq(List.of(IncludeInOrderToggle.INCLUDE)));
    }

    private DynamicList dynamicList(String code, String label) {
        DynamicListElement element = DynamicListElement.builder()
            .code(code)
            .label(label)
            .build();
        return DynamicList.builder()
            .value(element)
            .listItems(List.of(
                element,
                DynamicListElement.builder().code("telephone").label(HearingMethod.TELEPHONE.getLabel()).build()
            ))
            .build();
    }
}
