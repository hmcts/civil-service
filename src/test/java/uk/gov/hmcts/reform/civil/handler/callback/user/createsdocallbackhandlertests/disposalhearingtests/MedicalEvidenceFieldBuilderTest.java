package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.MedicalEvidenceFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalEvidenceFieldBuilderTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private MedicalEvidenceFieldBuilder medicalEvidenceFieldBuilder;

    @Test
    void shouldSetMedicalEvidence() {
        LocalDate nextWorkingDay = LocalDate.now().plusWeeks(4).plusDays(1); // Assuming the next working day is one day after 4 weeks
        when(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4))).thenReturn(nextWorkingDay);

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        medicalEvidenceFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingMedicalEvidence medicalEvidence = result.getDisposalHearingMedicalEvidence();
        assertThat(medicalEvidence).isNotNull();
        assertThat(medicalEvidence.getInput())
                .isEqualTo("The claimant has permission to rely upon the written expert evidence already "
                        + "uploaded to the Digital Portal with the particulars of claim and in addition has "
                        + "permission to rely upon any associated correspondence or updating report which "
                        + "is uploaded to the Digital Portal by 4pm on");
        assertThat(medicalEvidence.getDate()).isEqualTo(nextWorkingDay);
    }
}