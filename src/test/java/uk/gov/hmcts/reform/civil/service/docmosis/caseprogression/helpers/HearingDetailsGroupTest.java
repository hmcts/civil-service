package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.CaseHearingLengthElement;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel.IN_PERSON;

@ExtendWith(SpringExtension.class)
public class HearingDetailsGroupTest {

    @InjectMocks
    private HearingDetailsPopulator hearingDetailsPopulator;

    @Mock
    private LocationReferenceDataService locationRefDataService;
    List<FinalOrderToggle> toggleList = new ArrayList<FinalOrderToggle>(Arrays.asList(FinalOrderToggle.SHOW));

    @Test
    void shouldPopulateHearingDetails_whenAllFieldsArePresent() {
        LocalDate datesToAvoid = LocalDate.of(2023, 11, 25);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                 .hearingMethodList(IN_PERSON)
                                                 .hearingNotesText("These are hearing notes.")
                                                 .datesToAvoidDateDropdown(DatesFinalOrders.builder()
                                                                               .datesToAvoidDates(datesToAvoid)
                                                                               .build())
                                                 .lengthList(HearingLengthFinalOrderList.HOUR_1).build())
                                                 .build();

        LocationRefData caseManagementLocationDetails = LocationRefData.builder().build();

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();

        builder = hearingDetailsPopulator.populateHearingDetails(builder, caseData, caseManagementLocationDetails);

        JudgeFinalOrderForm result = builder.build();

        assertEquals("1 hour", result.getFurtherHearingLength());
        assertEquals(datesToAvoid, result.getDatesToAvoid());
        assertEquals("IN_PERSON", result.getFurtherHearingMethod());
        assertEquals("These are hearing notes.", result.getHearingNotes());

    }

    @Test
    void testGetFurtherHearingLength() {
        for (HearingLengthFinalOrderList hearingLengthFinalOrderList : List.of(HearingLengthFinalOrderList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(
                        hearingLengthFinalOrderList)
                                                     .lengthListOther(CaseHearingLengthElement.builder()
                                                                          .lengthListOtherDays("12")
                                                                          .lengthListOtherHours("1")
                                                                          .lengthListOtherMinutes("30")
                                                                          .build()).build()).build();
            String response = hearingDetailsPopulator.getFurtherHearingLength(caseData);
            switch (hearingLengthFinalOrderList) {
                case MINUTES_15 -> Assertions.assertEquals("15 minutes", response);
                case MINUTES_30 -> Assertions.assertEquals("30 minutes", response);
                case HOUR_1 -> Assertions.assertEquals("1 hour", response);
                case HOUR_1_5 -> Assertions.assertEquals("1.5 hours", response);
                case HOUR_2 -> Assertions.assertEquals("2 hours", response);
                case OTHER -> Assertions.assertEquals("12 days 1 hours 30 minutes", response);
                default -> {
                }
            }
        }
    }

    @Test
    void testGetFurtherHearingLengthOther() {
        CaseData minCaseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                                                 .lengthListOther(CaseHearingLengthElement.builder()
                                                                      //.lengthListOtherDays("12")
                                                                      //.lengthListOtherHours("1")
                                                                      .lengthListOtherMinutes("30")
                                                                      .build()).build()).build();
        String response = hearingDetailsPopulator.getFurtherHearingLength(minCaseData);
        Assertions.assertEquals("30 minutes", response);

        CaseData hourCaseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                                                 .lengthListOther(CaseHearingLengthElement.builder()
                                                                      //.lengthListOtherDays("12")
                                                                      .lengthListOtherHours("1")
                                                                      //.lengthListOtherMinutes("30")
                                                                      .build()).build()).build();
        response = hearingDetailsPopulator.getFurtherHearingLength(hourCaseData);
        Assertions.assertEquals("1 hours ", response);

        CaseData dayCaseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(HearingLengthFinalOrderList.OTHER)
                                                 .lengthListOther(CaseHearingLengthElement.builder()
                                                                      .lengthListOtherDays("12")
                                                                      //.lengthListOtherHours("1")
                                                                      //.lengthListOtherMinutes("30")
                                                                      .build()).build()).build();
        response = hearingDetailsPopulator.getFurtherHearingLength(dayCaseData);
        Assertions.assertEquals("12 days ", response);
    }

    @Test
    void testGetFurtherHearingLengthWhenNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingComplex(null).build();

        String response = hearingDetailsPopulator.getFurtherHearingLength(caseData);
        Assertions.assertEquals("", response);
    }

    @Test
    void testGetFurtherHearingFromDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().listFromDate(LocalDate.of(2022, 12,
                                                                                                          12
            )).build()).build();
        LocalDate response = hearingDetailsPopulator.getFurtherHearingDate(caseData, true);
        Assertions.assertEquals(LocalDate.of(2022, 12,
                                             12
        ), response);
    }

    @Test
    void testGetFurtherHearingToDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().dateToDate(LocalDate.of(2022, 12,
                                                                                                        12
            )).build()).build();
        LocalDate response = hearingDetailsPopulator.getFurtherHearingDate(caseData, false);
        Assertions.assertEquals(LocalDate.of(2022, 12,
                                             12
        ), response);
    }

    @Test
    void testIsDefaultCourt() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing
                                                 .builder().hearingLocationList(DynamicList
                                                                                    .builder().value(DynamicListElement
                                                                                                         .builder()
                                                                                                         .code("LOCATION_LIST")
                                                                                                         .build())
                                                                                    .build()).build())
            .build();
        CaseData caseDataWhenFalse = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing
                                                 .builder().hearingLocationList(DynamicList
                                                                                    .builder().value(DynamicListElement
                                                                                                         .builder()
                                                                                                         .code("OTHER_LOCATION")
                                                                                                         .build())
                                                                                    .build()).build())
            .build();
        Boolean response = hearingDetailsPopulator.isDefaultCourt(caseData);
        Boolean responseFalse = hearingDetailsPopulator.isDefaultCourt(caseDataWhenFalse);
        Assertions.assertEquals(true, response);
        Assertions.assertEquals(false, responseFalse);

    }

}
