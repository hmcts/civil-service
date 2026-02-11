package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DocumentUtilsTest {

    @Nested
    class GetDynamicListValueLabel {

        @Test
        void shouldReturnNull_whenGivenDynamicListIsNull() {
            assertNull(DocumentUtils.getDynamicListValueLabel(null));
        }

        @Test
        void shouldReturnNull_whenGivenDynamicListValueIsNull() {
            assertNull(DocumentUtils.getDynamicListValueLabel(DynamicList.builder().build()));
        }

        @Test
        void shouldReturnExpectedValueLabel_whenValidDynamicListIsGiven() {
            var expected = "expected";
            var dynamicList =  DynamicList.builder().value(
                DynamicListElement.builder().label(expected).build()
            ).build();

            var actual = DocumentUtils.getDynamicListValueLabel(dynamicList);
            assertEquals(expected, actual);
        }
    }

    @Nested
    class GetHearingTimeEstimateLabel {

        @Test
        void shouldReturnNull_whenGivenHearingTimeIsNull() {
            assertNull(DocumentUtils.getHearingTimeEstimateLabel(null));
        }

        @Test
        void shouldReturnNull_whenGivenHearingTimeEstimateIsNull() {
            var hearingTime = new TrialHearingTimeDJ();

            var actual = DocumentUtils.getHearingTimeEstimateLabel(hearingTime);

            assertNull(actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenValidHearingTimeEstimateIsGiven() {
            var expected = "1 hour";
            var hearingTime = new TrialHearingTimeDJ()
                .setHearingTimeEstimate(TrialHearingTimeEstimateDJ.ONE_HOUR);

            var actual = DocumentUtils.getHearingTimeEstimateLabel(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateIsGiven() {
            var expected = "1 hours 20 minutes";
            var hearingTime = new TrialHearingTimeDJ()
                .setHearingTimeEstimate(TrialHearingTimeEstimateDJ.OTHER)
                .setOtherHours("1")
                .setOtherMinutes("20");

            var actual = DocumentUtils.getHearingTimeEstimateLabel(hearingTime);

            assertEquals(expected, actual);
        }
    }

    @Nested
    class GetDJHearingTimeEstimateLabel {

        @Test
        void shouldReturnNull_whenGivenHearingTimeIsNull() {
            assertNull(DocumentUtils.getDisposalHearingTimeEstimateDJ(null));
        }

        @Test
        void shouldReturnNull_whenGivenHearingTimeEstimateIsNull() {
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ();

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertNull(actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenValidHearingTimeEstimateIsGiven() {
            var expected = "15 minutes";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.FIFTEEN_MINUTES);

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateIsGivenAs1Hour() {
            var expected = "1 hour 20 minutes";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("1")
                .setOtherMinutes("20");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateIsGiven() {
            var expected = "6 hours 20 minutes";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("6")
                .setOtherMinutes("20");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateIsGivenAsOneHourOneMin() {
            var expected = "1 hour 1 minute";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("1")
                .setOtherMinutes("1");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateWithZeroMinIsGivenAs1Hour() {
            var expected = "1 hour";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("1")
                .setOtherMinutes("0");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateWithZeroMinIsGiven() {
            var expected = "6 hours";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("6")
                .setOtherMinutes("0");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateWithZeroHourIsGiven() {
            var expected = "20 minutes";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("0")
                .setOtherMinutes("20");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenOtherHearingTimeEstimateWithZeroHourOneMinIsGiven() {
            var expected = "1 minute";
            var hearingTime = new DisposalHearingFinalDisposalHearingTimeDJ()
                .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.OTHER)
                .setOtherHours("0")
                .setOtherMinutes("1");

            var actual = DocumentUtils.getDisposalHearingTimeEstimateDJ(hearingTime);

            assertEquals(expected, actual);
        }
    }

}
