package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SdoExpertEvidenceFieldsServiceTest {

    private SdoExpertEvidenceFieldsService service;

    @BeforeEach
    void setUp() {
        service = new SdoExpertEvidenceFieldsService();
    }

    @Test
    void shouldPopulateExpertEvidenceFieldsWithExpectedDates() {
        CaseData caseData = CaseDataBuilder.builder().build();
        service.populateFastTrackExpertEvidence(caseData);

        FastTrackPersonalInjury personalInjury = caseData.getFastTrackPersonalInjury();
        assertThat(personalInjury).isNotNull();
        assertThat(personalInjury.getDate1()).isNull();
        assertThat(personalInjury.getDate2()).isEqualTo(LocalDate.now().plusWeeks(7));
        assertThat(personalInjury.getDate3()).isEqualTo(LocalDate.now().plusWeeks(9));
        assertThat(personalInjury.getDate4()).isEqualTo(LocalDate.now().plusWeeks(10));
        assertThat(personalInjury.getInput1()).isEqualTo(
            "The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                + " Digital Portal with the particulars of claim"
        );
        assertThat(personalInjury.getInput2()).isEqualTo(
            "The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert "
                + "directly and uploaded to the Digital Portal by 4pm on"
        );
        assertThat(personalInjury.getInput3()).isEqualTo("The answers to the questions shall be answered by the Expert by");
        assertThat(personalInjury.getInput4()).isEqualTo("and uploaded to the Digital Portal by the party who has asked the question by");
    }
}
