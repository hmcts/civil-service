package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateorderdetailspagestests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages.OrderDetailsPagesExpertEvidenceFieldsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailsPagesExpertEvidenceFieldsFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private OrderDetailsPagesExpertEvidenceFieldsFieldBuilder orderDetailsPagesExpertEvidenceFieldsFieldBuilder;

    @Test
    void shouldBuildExpertEvidenceFields() {
        LocalDate now = LocalDate.now();
        when(workingDayIndicator.getNextWorkingDay(now.plusDays(14))).thenReturn(now.plusDays(16));
        when(workingDayIndicator.getNextWorkingDay(now.plusDays(42))).thenReturn(now.plusDays(44));
        when(workingDayIndicator.getNextWorkingDay(now.plusDays(49))).thenReturn(now.plusDays(51));

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        orderDetailsPagesExpertEvidenceFieldsFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackPersonalInjury fastTrackPersonalInjury = caseData.getFastTrackPersonalInjury();

        assertEquals("The Claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the particulars of claim",
                fastTrackPersonalInjury.getInput1());
        assertEquals("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert directly and uploaded to the Digital Portal by 4pm on",
                fastTrackPersonalInjury.getInput2());
        assertEquals(now.plusDays(16), fastTrackPersonalInjury.getDate2());
        assertEquals("The answers to the questions shall be answered by the Expert by", fastTrackPersonalInjury.getInput3());
        assertEquals(now.plusDays(44), fastTrackPersonalInjury.getDate3());
        assertEquals("and uploaded to the Digital Portal by the party who has asked the question by", fastTrackPersonalInjury.getInput4());
        assertEquals(now.plusDays(51), fastTrackPersonalInjury.getDate4());
    }
}