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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
        CaseData caseData = CaseDataBuilder.builder().build();
        DynamicList hearingMethodList = dynamicList("initial", HearingMethod.TELEPHONE.getLabel());
        RequestedCourt requestedCourt = new RequestedCourt();

        service.populateDrhFields(caseData, Optional.of(requestedCourt),
            hearingMethodList, Collections.emptyList());

        assertThat(caseData.getSdoR2SmallClaimsJudgesRecital()).isNotNull();
        assertThat(caseData.getSdoR2SmallClaimsHearing()).isNotNull();
        assertThat(caseData.getSdoR2SmallClaimsHearing().getMethodOfHearing().getValue().getLabel())
            .isEqualTo(HearingMethod.TELEPHONE.getLabel());
        assertThat(caseData.getSdoR2SmallClaimsUploadDocToggle()).containsExactly(IncludeInOrderToggle.INCLUDE);
    }

    @Test
    void shouldDelegateCarmFieldsToJourneyService() {
        CaseData caseData = CaseDataBuilder.builder().build();

        service.populateDrhFields(caseData, Optional.empty(),
            dynamicList("initial", HearingMethod.TELEPHONE.getLabel()), Collections.emptyList());

        verify(journeyToggleService).applyR2SmallClaimsMediation(caseData,
            List.of(IncludeInOrderToggle.INCLUDE));
    }

    private DynamicList dynamicList(String code, String label) {
        DynamicListElement element = new DynamicListElement();
        element.setCode(code);
        element.setLabel(label);
        DynamicListElement telephone = new DynamicListElement();
        telephone.setCode("telephone");
        telephone.setLabel(HearingMethod.TELEPHONE.getLabel());
        DynamicList list = new DynamicList();
        list.setValue(element);
        list.setListItems(List.of(element, telephone));
        return list;
    }
}
