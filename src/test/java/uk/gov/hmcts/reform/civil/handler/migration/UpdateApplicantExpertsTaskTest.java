package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateApplicantExpertsTaskTest {

    private UpdateApplicantExpertsTask task;

    @BeforeEach
    void setUp() {
        task = new UpdateApplicantExpertsTask();
    }

    @Test
    void shouldUpdateApplicantExpertsWithTBCWhenNamesAreNull() {
        PartyFlagStructure expert1 = PartyFlagStructure.builder().firstName(null).lastName(null).build();
        PartyFlagStructure expert2 = PartyFlagStructure.builder().firstName("John").lastName(null).build();

        CaseData caseData = CaseData.builder()
            .applicantExperts(List.of(
                Element.<PartyFlagStructure>builder().value(expert1).build(),
                Element.<PartyFlagStructure>builder().value(expert2).build()
            ))
            .build();

        CaseReference caseRef = CaseReference.builder().caseReference("12345").build();

        CaseData updatedCaseData = task.migrateCaseData(caseData, caseRef);

        List<Element<PartyFlagStructure>> updatedExperts = updatedCaseData.getApplicantExperts();

        assertThat(updatedExperts).hasSize(2);
        assertThat(updatedExperts.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(updatedExperts.get(0).getValue().getLastName()).isEqualTo("TBC");
        assertThat(updatedExperts.get(1).getValue().getFirstName()).isEqualTo("John");
        assertThat(updatedExperts.get(1).getValue().getLastName()).isEqualTo("TBC");
    }

    @Test
    void shouldUpdateApplicant1DQExpertsWithTBCWhenNamesAreNull() {
        // Build DQ Experts
        Expert dqExpert1 = Expert.builder().firstName(null).lastName(null).build();
        Expert dqExpert2 = Expert.builder().firstName("Jane").lastName(null).build();

        List<Element<Expert>> dqExpertElements = List.of(
            Element.<Expert>builder().value(dqExpert1).build(),
            Element.<Expert>builder().value(dqExpert2).build()
        );

        Experts applicant1DQExperts = Experts.builder()
            .details(dqExpertElements)
            .build();

        Applicant1DQ applicant1DQ = Applicant1DQ.builder()
            .applicant1DQExperts(applicant1DQExperts)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1DQ(applicant1DQ)
            .build();

        CaseReference caseRef = CaseReference.builder().caseReference("12345").build();

        CaseData updatedCaseData = task.migrateCaseData(caseData, caseRef);

        List<Element<Expert>> updatedDQExperts = updatedCaseData.getApplicant1DQ()
            .getApplicant1DQExperts()
            .getDetails();

        assertThat(updatedDQExperts).hasSize(2);
        assertThat(updatedDQExperts.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(updatedDQExperts.get(0).getValue().getLastName()).isEqualTo("TBC");
        assertThat(updatedDQExperts.get(1).getValue().getFirstName()).isEqualTo("Jane");
        assertThat(updatedDQExperts.get(1).getValue().getLastName()).isEqualTo("TBC");
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, null));
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceValueIsNull() {
        CaseData caseData = CaseData.builder().build();
        CaseReference caseRef = CaseReference.builder().caseReference(null).build();
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, caseRef));
    }
}
