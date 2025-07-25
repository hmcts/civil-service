package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.generatesdoordertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.FastTrackHearingMethodCaseDataMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FastTrackHearingMethodCaseDataMapperTest {

    @InjectMocks
    private FastTrackHearingMethodCaseDataMapper mapper;

    @Test
    void shouldMapHearingMethodFieldsToInPerson() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesFastTrack(DynamicList.builder()
                        .value(DynamicListElement.builder().label(HearingMethod.IN_PERSON.getLabel()).build())
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getFastTrackMethod())
                .isEqualTo(FastTrackMethod.fastTrackMethodInPerson);
    }

    @Test
    void shouldMapHearingMethodFieldsToVideoConference() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesFastTrack(DynamicList.builder()
                        .value(DynamicListElement.builder().label(HearingMethod.VIDEO.getLabel()).build())
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getFastTrackMethod())
                .isEqualTo(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
    }

    @Test
    void shouldMapHearingMethodFieldsToTelephone() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesFastTrack(DynamicList.builder()
                        .value(DynamicListElement.builder().label(HearingMethod.TELEPHONE.getLabel()).build())
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getFastTrackMethod())
                .isEqualTo(FastTrackMethod.fastTrackMethodTelephoneHearing);
    }

    @Test
    void shouldNotMapHearingMethodFieldsWhenValueIsNull() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesFastTrack(DynamicList.builder().build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getFastTrackMethod()).isNull();
    }

    @Test
    void shouldNotMapHearingMethodFieldsWhenDynamicListIsNull() {
        CaseData caseData = CaseData.builder().build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getFastTrackMethod()).isNull();
    }
}