package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackDisclosureOfDocumentsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastTrackDisclosureOfDocumentsFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private FastTrackDisclosureOfDocumentsFieldBuilder fastTrackDisclosureOfDocumentsFieldBuilder;

    @Test
    void shouldBuildFastTrackDisclosureOfDocumentsFields() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = now.plusWeeks(4);
        LocalDate date2 = now.plusWeeks(6);
        LocalDate date3 = now.plusWeeks(8);
        when(workingDayIndicator.getNextWorkingDay(date1)).thenReturn(date1);
        when(workingDayIndicator.getNextWorkingDay(date2)).thenReturn(date2);
        when(workingDayIndicator.getNextWorkingDay(date3)).thenReturn(date3);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackDisclosureOfDocumentsFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackDisclosureOfDocuments disclosureOfDocuments = caseData.getFastTrackDisclosureOfDocuments();
        assertThat(disclosureOfDocuments).isNotNull();
        assertThat(disclosureOfDocuments.getInput1()).isEqualTo("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents " +
                "by 4pm on");
        assertThat(disclosureOfDocuments.getDate1()).isEqualTo(date1);
        assertThat(disclosureOfDocuments.getInput2()).isEqualTo("Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm" +
                " on");
        assertThat(disclosureOfDocuments.getDate2()).isEqualTo(date2);
        assertThat(disclosureOfDocuments.getInput3()).isEqualTo("Requests will be complied with within 7 days of the receipt of the request.");
        assertThat(disclosureOfDocuments.getInput4()).isEqualTo("Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial by " +
                "4pm on");
        assertThat(disclosureOfDocuments.getDate3()).isEqualTo(date3);
    }
}