package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
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
            var hearingTime = TrialHearingTimeDJ.builder().build();

            var actual = DocumentUtils.getHearingTimeEstimateLabel(hearingTime);

            assertNull(actual);
        }

        @Test
        void shouldReturnExpectedLabel_whenValidHearingTimeEstimateIsGiven() {
            var expected = "1 hour";
            var hearingTime = TrialHearingTimeDJ.builder()
                .hearingTimeEstimate(TrialHearingTimeEstimateDJ.ONE_HOUR).build();

            var actual = DocumentUtils.getHearingTimeEstimateLabel(hearingTime);

            assertEquals(expected, actual);
        }
    }

}
