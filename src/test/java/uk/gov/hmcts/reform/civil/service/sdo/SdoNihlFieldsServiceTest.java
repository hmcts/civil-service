package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoNihlFieldsServiceTest {

    @Mock
    private SdoLocationService locationService;
    @Mock
    private SdoNihlOrderService orderService;

    private SdoNihlFieldsService service;

    @BeforeEach
    void setUp() {
        service = new SdoNihlFieldsService(locationService, orderService);
        when(locationService.buildCourtLocationForSdoR2(any(), any())).thenReturn(dynamicList("trial", "Trial Court"));
        when(locationService.buildAlternativeCourtLocations(any())).thenReturn(dynamicList("alt", "Alt Court"));
    }

    @Test
    void shouldSetHearingMethodAndDelegateToOrderService() {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder().build().toBuilder();
        DynamicList hearingList = hearingMethodList();

        service.populateNihlFields(
            builder,
            hearingList,
            Optional.of(RequestedCourt.builder().build()),
            Collections.emptyList()
        );

        ArgumentCaptor<DynamicList> hearingCaptor = ArgumentCaptor.forClass(DynamicList.class);
        verify(orderService).populateStandardDirections(
            org.mockito.Mockito.eq(builder),
            hearingCaptor.capture(),
            org.mockito.Mockito.any(DynamicList.class),
            org.mockito.Mockito.any(DynamicList.class)
        );

        DynamicList forwarded = hearingCaptor.getValue();
        assertThat(forwarded.getValue().getLabel()).isEqualTo(HearingMethod.IN_PERSON.getLabel());
        assertThat(forwarded.getValue()).isEqualTo(forwarded.getListItems().get(0));
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
