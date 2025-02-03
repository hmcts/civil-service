package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class OrderDetailsGroupTest {

    @InjectMocks
    private OrderDetailsPopulator orderDetailsPopulator;

    @Test
    void shouldPopulateOrderDetails_WhenAllFieldsArePresent() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .orderOnCourtInitiative(FreeFormOrderValues.builder().onInitiativeSelectionTextArea("On initiative text").onInitiativeSelectionDate(
                LocalDate.now()).build())
            .freeFormRecordedTextArea("Recorded text")
            .freeFormOrderedTextArea("Ordered text")
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeText("On initiative text").build())
            .orderWithoutNotice(FreeFormOrderValues.builder().withoutNoticeSelectionTextArea("Without notice text")
                                    .withoutNoticeSelectionDate(LocalDate.now()).build())
            .build();
        String freeFormRecordedText = "Recorded text";
        String freeFormOrderedText = "Ordered text";

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder()
            .freeFormOrderedText("Ordered Text")
            .freeFormRecordedText("Recorded text");

        builder = orderDetailsPopulator.populateOrderDetails(builder, caseData);

        String onInitiativeText = "On initiative text";
        LocalDate onInitiativeDate = LocalDate.now();
        String withoutNoticeText = "Without notice text";
        LocalDate withoutNoticeDate = LocalDate.now();

        JudgeFinalOrderForm result = builder.build();
        Assertions.assertEquals(freeFormRecordedText, result.getFreeFormRecordedText());
        Assertions.assertEquals(freeFormOrderedText, result.getFreeFormOrderedText());
        Assertions.assertEquals(onInitiativeText, result.getOnInitiativeSelectionText());
        Assertions.assertEquals(onInitiativeDate, result.getOnInitiativeSelectionDate());
        Assertions.assertEquals(withoutNoticeText, result.getWithoutNoticeSelectionText());
        Assertions.assertEquals(withoutNoticeDate, result.getWithoutNoticeSelectionDate());
    }

    @ParameterizedTest
    @MethodSource("testData")
    void orderMadeDateBuilder(CaseData caseData, String expectedResponse) {
        String response = orderDetailsPopulator.orderMadeDateBuilder(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder()
                                                                                            .singleDate(LocalDate.of(
                                                                                                2023,
                                                                                                9,
                                                                                                15
                                                                                            ))
                                                                                            .build()).build()).build(),
                "on 15 September 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                           .dateRangeFrom(LocalDate.of(
                                                                                               2023,
                                                                                               9,
                                                                                               13
                                                                                           ))
                                                                                           .dateRangeTo(LocalDate.of(
                                                                                               2023,
                                                                                               9,
                                                                                               14
                                                                                           ))
                                                                                           .build()).build()).build(),
                "between 13 September 2023 and 14 September 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().bespokeRangeSelection(DatesFinalOrders.builder()
                                                                                              .bespokeRangeTextArea(
                                                                                                  "date between 12 feb 2023, and 14 feb 2023")
                                                                                              .build()).build()).build(),
                "on date between 12 feb 2023, and 14 feb 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().bespokeRangeSelection(null)
                                                    .build()).build(),
                null
            )
        );
    }

}
