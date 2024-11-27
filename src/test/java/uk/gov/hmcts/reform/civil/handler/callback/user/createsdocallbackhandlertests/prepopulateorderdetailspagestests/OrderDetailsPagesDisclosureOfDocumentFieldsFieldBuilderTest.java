package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateorderdetailspagestests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages.OrderDetailsPagesDisclosureOfDocumentFieldsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailsPagesDisclosureOfDocumentFieldsFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private OrderDetailsPagesDisclosureOfDocumentFieldsFieldBuilder orderDetailsPagesDisclosureOfDocumentFieldsFieldBuilder;

    @Test
    void shouldBuildDisclosureOfDocumentFields() {
        LocalDate now = LocalDate.now();
        when(workingDayIndicator.getNextWorkingDay(now.plusWeeks(4))).thenReturn(now.plusWeeks(4).plusDays(2));
        when(workingDayIndicator.getNextWorkingDay(now.plusWeeks(5))).thenReturn(now.plusWeeks(5).plusDays(2));
        when(workingDayIndicator.getNextWorkingDay(now.plusWeeks(8))).thenReturn(now.plusWeeks(8).plusDays(2));

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        orderDetailsPagesDisclosureOfDocumentFieldsFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackDisclosureOfDocuments disclosureOfDocuments = caseData.getFastTrackDisclosureOfDocuments();

        assertEquals("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents by 4pm on",
                disclosureOfDocuments.getInput1());
        assertEquals(now.plusWeeks(4).plusDays(2), disclosureOfDocuments.getDate1());
        assertEquals("Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm on", disclosureOfDocuments.getInput2());
        assertEquals(now.plusWeeks(5).plusDays(2), disclosureOfDocuments.getDate2());
        assertEquals("Requests will be complied with within 7 days of the receipt of the request.", disclosureOfDocuments.getInput3());
        assertEquals("Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial by 4pm on", disclosureOfDocuments.getInput4());
        assertEquals(now.plusWeeks(8).plusDays(2), disclosureOfDocuments.getDate3());
    }
}