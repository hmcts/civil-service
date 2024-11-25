package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.submitsdotests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.HearingMethodsFieldUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SubmitSdoUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingMethodsFieldUpdaterTest {

    @Mock
    private SubmitSdoUtils submitSdoUtils;

    @InjectMocks
    private HearingMethodsFieldUpdater hearingMethodsFieldUpdater;

    @Test
    void shouldUpdateHearingMethods() {
        DynamicList options = DynamicList.builder()
                .listItems(List.of(
                        DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                        DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                        DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                ))
                .build();

        DynamicListElement selectedCourt = DynamicListElement.builder()
                .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(123L)
                .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                .fastTrackMethodInPerson(options)
                .smallClaimsMethodInPerson(options)
                .build();

        DynamicList updatedOptions1 = DynamicList.builder()
                .listItems(List.of(
                        DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                        DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                        DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                ))
                .value(null)
                .build();

        DynamicList updatedOptions2 = DynamicList.builder()
                .listItems(List.of(
                        DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                        DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                        DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                ))
                .value(null)
                .build();

        DynamicList updatedOptions3 = DynamicList.builder()
                .listItems(List.of(
                        DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                        DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                        DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                ))
                .value(null)
                .build();

        when(submitSdoUtils.deleteLocationList(caseData.getDisposalHearingMethodInPerson())).thenReturn(updatedOptions1);
        when(submitSdoUtils.deleteLocationList(caseData.getFastTrackMethodInPerson())).thenReturn(updatedOptions2);
        when(submitSdoUtils.deleteLocationList(caseData.getSmallClaimsMethodInPerson())).thenReturn(updatedOptions3);

        CaseData.CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();
        hearingMethodsFieldUpdater.update(caseData, dataBuilder);

        CaseData updatedCaseData = dataBuilder.build();
        assertThat(updatedCaseData.getDisposalHearingMethodInPerson()).isEqualTo(updatedOptions1);
        assertThat(updatedCaseData.getFastTrackMethodInPerson()).isEqualTo(updatedOptions2);
        assertThat(updatedCaseData.getSmallClaimsMethodInPerson()).isEqualTo(updatedOptions3);
    }
}