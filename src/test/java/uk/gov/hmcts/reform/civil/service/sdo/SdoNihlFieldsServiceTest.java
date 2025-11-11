package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoNihlFieldsServiceTest {

    @Mock
    private SdoLocationService locationService;
    @Mock
    private SdoDeadlineService deadlineService;

    private SdoNihlFieldsService service;

    @BeforeEach
    void setUp() {
        service = new SdoNihlFieldsService(locationService, deadlineService);
        when(locationService.buildCourtLocationForSdoR2(any(), any())).thenReturn(dynamicList("trial", "Trial Court"));
        when(locationService.buildAlternativeCourtLocations(any())).thenReturn(dynamicList("alt", "Alt Court"));
        when(deadlineService.calendarDaysFromNow(anyInt())).thenAnswer(invocation -> {
            int days = invocation.getArgument(0, Integer.class);
            return LocalDate.of(2024, 1, 1).plusDays(days);
        });
    }

    @Test
    void shouldPopulateNihlSpecificFields() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder().build().toBuilder();

        service.populateNihlFields(builder,
            hearingMethodList(),
            Optional.of(RequestedCourt.builder().build()),
            Collections.emptyList());

        CaseData result = builder.build();

        assertThat(result.getSdoFastTrackJudgesRecital()).isNotNull();
        assertThat(result.getSdoR2Trial()).isNotNull();
        assertThat(result.getSdoR2NihlUseOfWelshLanguage()).isNotNull();
    }

    private DynamicList hearingMethodList() {
        DynamicListElement inPerson = DynamicListElement.builder()
            .code("inPerson")
            .label(HearingMethod.IN_PERSON.getLabel())
            .build();
        return DynamicList.builder()
            .value(inPerson)
            .listItems(List.of(inPerson))
            .build();
    }

    private DynamicList dynamicList(String code, String label) {
        DynamicListElement element = DynamicListElement.builder()
            .code(code)
            .label(label)
            .build();
        return DynamicList.builder()
            .value(element)
            .listItems(List.of(element))
            .build();
    }
}
