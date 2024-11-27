package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.orderdetailspagestests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages.DisclosureOfDocumentFieldsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisclosureOfDocumentFieldsFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private DisclosureOfDocumentFieldsFieldBuilder fieldBuilder;

    @BeforeEach
    void setUp() {
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .thenReturn(LocalDate.now().plusWeeks(4).plusDays(1));
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)))
                .thenReturn(LocalDate.now().plusWeeks(5).plusDays(1));
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .thenReturn(LocalDate.now().plusWeeks(8).plusDays(1));
    }

    @Test
    void shouldBuildFastTrackDisclosureOfDocuments() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackDisclosureOfDocuments disclosureOfDocuments = caseData.getFastTrackDisclosureOfDocuments();

        assertThat(disclosureOfDocuments).isNotNull();
        assertThat(disclosureOfDocuments.getInput1()).isEqualTo("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents " +
                "by 4pm on");
        assertThat(disclosureOfDocuments.getDate1()).isEqualTo(LocalDate.now().plusWeeks(4).plusDays(1));
        assertThat(disclosureOfDocuments.getInput2()).isEqualTo("Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm" +
                " on");
        assertThat(disclosureOfDocuments.getDate2()).isEqualTo(LocalDate.now().plusWeeks(5).plusDays(1));
        assertThat(disclosureOfDocuments.getInput3()).isEqualTo("Requests will be complied with within 7 days of the receipt of the request.");
        assertThat(disclosureOfDocuments.getInput4()).isEqualTo("Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial by " +
                "4pm on");
        assertThat(disclosureOfDocuments.getDate3()).isEqualTo(LocalDate.now().plusWeeks(8).plusDays(1));
    }
}