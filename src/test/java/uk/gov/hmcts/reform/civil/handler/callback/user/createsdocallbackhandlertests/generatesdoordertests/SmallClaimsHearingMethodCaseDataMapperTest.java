package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.generatesdoordertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.SmallClaimsHearingMethodCaseDataMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SmallClaimsHearingMethodCaseDataMapperTest {

    @InjectMocks
    private SmallClaimsHearingMethodCaseDataMapper mapper;

    @Test
    void shouldMapHearingMethodToInPerson() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesSmallClaims(DynamicList.builder()
                        .value(DynamicListElement.builder().label(HearingMethod.IN_PERSON.getLabel()).build())
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getSmallClaimsMethod()).isEqualTo(SmallClaimsMethod.smallClaimsMethodInPerson);
    }

    @Test
    void shouldMapHearingMethodToVideoConference() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesSmallClaims(DynamicList.builder()
                        .value(DynamicListElement.builder().label(HearingMethod.VIDEO.getLabel()).build())
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getSmallClaimsMethod()).isEqualTo(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
    }

    @Test
    void shouldMapHearingMethodToTelephone() {
        CaseData caseData = CaseData.builder()
                .hearingMethodValuesSmallClaims(DynamicList.builder()
                        .value(DynamicListElement.builder().label(HearingMethod.TELEPHONE.getLabel()).build())
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getSmallClaimsMethod()).isEqualTo(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
    }

    @Test
    void shouldNotMapHearingMethodIfNull() {
        CaseData caseData = CaseData.builder().build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        mapper.mapHearingMethodFields(caseData, updatedData);

        assertThat(updatedData.build().getSmallClaimsMethod()).isNull();
    }
}