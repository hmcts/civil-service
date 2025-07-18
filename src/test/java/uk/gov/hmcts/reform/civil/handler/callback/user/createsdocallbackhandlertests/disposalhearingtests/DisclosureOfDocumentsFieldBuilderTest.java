package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.DisclosureOfDocumentsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisclosureOfDocumentsFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private DisclosureOfDocumentsFieldBuilder disclosureOfDocumentsFieldBuilder;

    @Test
    void shouldSetDisclosureOfDocuments() {
        LocalDate now = LocalDate.now();
        LocalDate expectedDate = now.plusWeeks(10);
        when(workingDayIndicator.getNextWorkingDay(expectedDate)).thenReturn(expectedDate);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        disclosureOfDocumentsFieldBuilder.build(caseDataBuilder);
        CaseData caseData = caseDataBuilder.build();

        DisposalHearingDisclosureOfDocuments disclosureOfDocuments = caseData.getDisposalHearingDisclosureOfDocuments();
        assertEquals("The parties shall serve on each other copies of the documents upon which reliance is to be placed at the disposal hearing by 4pm on",
                disclosureOfDocuments.getInput1());
        assertEquals(expectedDate, disclosureOfDocuments.getDate1());
        assertEquals("The parties must upload to the Digital Portal copies of those documents which they wish the court to consider when deciding the amount of damages," +
                " by 4pm on", disclosureOfDocuments.getInput2());
        assertEquals(expectedDate, disclosureOfDocuments.getDate2());
    }
}