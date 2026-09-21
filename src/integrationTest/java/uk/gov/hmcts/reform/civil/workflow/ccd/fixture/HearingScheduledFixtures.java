package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public final class HearingScheduledFixtures {

    private static final String TEMPLATE = "case-progression";

    private HearingScheduledFixtures() {
    }

    public static CaseData fastTrackListing() {
        CaseData caseData = baseCase();
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setHearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL);
        caseData.setListingOrRelisting(ListingOrRelisting.LISTING);
        caseData.setHearingDate(LocalDate.now().plusDays(30));
        caseData.setHearingTimeHourMinute("1000");
        caseData.setDateOfApplication(LocalDate.now().minusDays(1));
        return caseData;
    }

    public static CaseData fastTrackRelisting() {
        CaseData caseData = baseCase();
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setHearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL);
        caseData.setListingOrRelisting(ListingOrRelisting.RELISTING);
        caseData.setHearingDate(LocalDate.now().plusDays(30));
        caseData.setHearingTimeHourMinute("1000");
        caseData.setDateOfApplication(LocalDate.now().minusDays(1));
        return caseData;
    }

    public static CaseData otherHearingType() {
        CaseData caseData = baseCase();
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setHearingNoticeList(HearingNoticeList.OTHER);
        caseData.setListingOrRelisting(ListingOrRelisting.LISTING);
        caseData.setHearingDate(LocalDate.now().plusDays(30));
        caseData.setHearingTimeHourMinute("1000");
        caseData.setDateOfApplication(LocalDate.now().minusDays(1));
        return caseData;
    }

    private static CaseData baseCase() {
        DynamicListElement locationElement = DynamicListElement.dynamicElementFromCode(
            "uuid-hearing-court", "Test Court - 1 Court Road - AB1 2CD");
        CaseData caseData = CaseDataTemplates.load(TEMPLATE).toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();
        caseData.setHearingLocation(new DynamicList()
                                        .setValue(locationElement)
                                        .setListItems(List.of(locationElement)));
        return caseData;
    }
}
