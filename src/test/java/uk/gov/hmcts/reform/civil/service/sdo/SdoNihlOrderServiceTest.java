package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;

@ExtendWith(MockitoExtension.class)
class SdoNihlOrderServiceTest {

    @Mock
    private SdoDeadlineService deadlineService;

    private SdoNihlOrderService service;

    @BeforeEach
    void setUp() {
        service = new SdoNihlOrderService(deadlineService);
        lenient().when(deadlineService.calendarDaysFromNow(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 1, 1)
                .plusDays(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldPopulateSharedSectionsUsingLibraryText() {
        CaseData caseData = CaseDataBuilder.builder().build();
        DynamicList hearingList = singleOptionList("IN_PERSON");
        DynamicList trialCourt = singleOptionList("Trial Court");
        DynamicList altCourt = singleOptionList("Alt Court");

        service.populateStandardDirections(caseData, hearingList, trialCourt, altCourt);

        assertThat(caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureTxt())
            .isEqualTo(FAST_TRACK_DISCLOSURE_STANDARD_SDO);
        assertThat(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantText())
            .isEqualTo(SCHEDULE_OF_LOSS_CLAIMANT);
        assertThat(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantText())
            .isEqualTo(SCHEDULE_OF_LOSS_DEFENDANT);
        assertThat(caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertTxt())
            .isEqualTo(PERMISSION_TO_RELY_ON_EXPERT);
        assertThat(caseData.getSdoR2Trial().getMethodOfHearing()).isEqualTo(hearingList);
        assertThat(caseData.getSdoR2Trial().getHearingCourtLocationList()).isEqualTo(trialCourt);
        assertThat(caseData.getSdoR2Trial().getAltHearingCourtLocationList()).isEqualTo(altCourt);
        assertThat(caseData.getSdoR2NihlUseOfWelshLanguage()).isNotNull();
    }

    private DynamicList singleOptionList(String label) {
        DynamicListElement element = new DynamicListElement();
        element.setCode(label);
        element.setLabel(label);
        DynamicList list = new DynamicList();
        list.setValue(element);
        list.setListItems(List.of(element));
        return list;
    }
}
