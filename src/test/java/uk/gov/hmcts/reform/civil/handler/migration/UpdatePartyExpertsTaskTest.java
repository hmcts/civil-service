package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePartyExpertsTaskTest {

    private UpdatePartyExpertsTask task;

    @BeforeEach
    void setUp() {
        task = new UpdatePartyExpertsTask();
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, null));
    }

    @Test
    void shouldThrowExceptionWhenCaseReferenceValueIsNull() {
        CaseData caseData = CaseData.builder().build();
        CaseReference caseRef = caseReference(null);
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(caseData, caseRef));
    }

    @Test
    void shouldUpdateApplicantExpertsWithTBCWhenNamesAreNull() {
        PartyFlagStructure expert1 = new PartyFlagStructure().setFirstName(null).setLastName(null);
        PartyFlagStructure expert2 = new PartyFlagStructure().setFirstName("John").setLastName(null);

        CaseData caseData = CaseData.builder()
            .applicantExperts(List.of(
                Element.<PartyFlagStructure>builder().value(expert1).build(),
                Element.<PartyFlagStructure>builder().value(expert2).build()
            ))
            .build();

        CaseReference caseRef = caseReference("12345");

        CaseData updated = task.migrateCaseData(caseData, caseRef);

        List<Element<PartyFlagStructure>> experts = updated.getApplicantExperts();
        assertThat(experts).hasSize(2);
        assertThat(experts.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(experts.get(0).getValue().getLastName()).isEqualTo("TBC");
        assertThat(experts.get(1).getValue().getFirstName()).isEqualTo("John");
        assertThat(experts.get(1).getValue().getLastName()).isEqualTo("TBC");
    }

    @Test
    void shouldUpdateRespondent1ExpertsWithTBCWhenNamesAreNull() {
        PartyFlagStructure expert = new PartyFlagStructure().setFirstName(null).setLastName("Smith");

        CaseData caseData = CaseData.builder()
            .respondent1Experts(List.of(Element.<PartyFlagStructure>builder().value(expert).build()))
            .build();

        CaseReference ref = caseReference("123");

        CaseData updated = task.migrateCaseData(caseData, ref);

        assertThat(updated.getRespondent1Experts()).hasSize(1);
        assertThat(updated.getRespondent1Experts().get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(updated.getRespondent1Experts().get(0).getValue().getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldUpdateRespondent2ExpertsWithTBCWhenNamesAreNull() {
        PartyFlagStructure expert = new PartyFlagStructure().setFirstName("Bob").setLastName(null);

        CaseData caseData = CaseData.builder()
            .respondent2Experts(List.of(Element.<PartyFlagStructure>builder().value(expert).build()))
            .build();

        CaseReference ref = caseReference("123");

        CaseData updated = task.migrateCaseData(caseData, ref);

        assertThat(updated.getRespondent2Experts()).hasSize(1);
        assertThat(updated.getRespondent2Experts().get(0).getValue().getFirstName()).isEqualTo("Bob");
        assertThat(updated.getRespondent2Experts().get(0).getValue().getLastName()).isEqualTo("TBC");
    }

    @Test
    void shouldUpdateDQExpertsWithTBCForApplicantsAndRespondents() {
        Expert expert1 = new Expert().setFirstName(null).setLastName("Doe");
        Expert expert2 = new Expert().setFirstName("Jane").setLastName(null);

        List<Element<Expert>> dqExpertElements = List.of(
            Element.<Expert>builder().value(expert1).build(),
            Element.<Expert>builder().value(expert2).build()
        );
        Experts dqExperts = new Experts().setDetails(dqExpertElements);

        CaseData caseData = CaseData.builder()
            .applicant1DQ(new Applicant1DQ().setApplicant1DQExperts(dqExperts))
            .applicant2DQ(new Applicant2DQ().setApplicant2DQExperts(dqExperts))
            .respondent1DQ(new Respondent1DQ().setRespondent1DQExperts(dqExperts))
            .respondent2DQ(new Respondent2DQ().setRespondent2DQExperts(dqExperts))
            .build();

        CaseReference ref = caseReference("999");

        CaseData updated = task.migrateCaseData(caseData, ref);

        // Verify Applicant 1
        List<Element<Expert>> applicant1Experts = updated.getApplicant1DQ().getApplicant1DQExperts().getDetails();
        assertThat(applicant1Experts.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(applicant1Experts.get(1).getValue().getLastName()).isEqualTo("TBC");

        // Verify Respondent 1
        List<Element<Expert>> respondent1Experts = updated.getRespondent1DQ().getRespondent1DQExperts().getDetails();
        assertThat(respondent1Experts.get(0).getValue().getFirstName()).isEqualTo("TBC");
        assertThat(respondent1Experts.get(1).getValue().getLastName()).isEqualTo("TBC");
    }

    @Test
    void shouldAssignNewPartyIdWhenMissing() {
        PartyFlagStructure expert = new PartyFlagStructure()
            .setFirstName("Test")
            .setLastName("User")
            .setPartyID(null)
            ;

        CaseData caseData = CaseData.builder()
            .applicantExperts(List.of(Element.<PartyFlagStructure>builder().value(expert).build()))
            .build();

        CaseReference ref = caseReference("111");

        CaseData updated = task.migrateCaseData(caseData, ref);

        String newId = updated.getApplicantExperts().get(0).getValue().getPartyID();
        assertThat(newId).isNotBlank().isNotEqualTo(expert.getPartyID());
    }

    @Test
    void shouldNotChangePartyIdIfAlreadyPresent() {
        PartyFlagStructure expert = new PartyFlagStructure()
            .setFirstName("John")
            .setLastName("Doe")
            .setPartyID("existing-id-123")
            ;

        CaseData caseData = CaseData.builder()
            .applicantExperts(List.of(Element.<PartyFlagStructure>builder().value(expert).build()))
            .build();

        CaseReference ref = caseReference("12345");

        CaseData updated = task.migrateCaseData(caseData, ref);

        assertThat(updated.getApplicantExperts().get(0).getValue().getPartyID())
            .isEqualTo("existing-id-123");
    }

    @Test
    void shouldHandleNullListsGracefully() {
        CaseData caseData = CaseData.builder()
            .applicantExperts(null)
            .respondent1Experts(null)
            .respondent2Experts(null)
            .applicant1DQ(null)
            .applicant2DQ(null)
            .respondent1DQ(null)
            .respondent2DQ(null)
            .build();

        CaseReference caseRef = caseReference("12345");

        CaseData updated = task.migrateCaseData(caseData, caseRef);

        assertThat(updated.getApplicantExperts()).isEmpty();
        assertThat(updated.getRespondent1Experts()).isEmpty();
        assertThat(updated.getRespondent2Experts()).isEmpty();
        assertThat(updated.getApplicant1DQ()).isNull();
        assertThat(updated.getRespondent1DQ()).isNull();
    }

    @Test
    void shouldReturnExpectedMetadataValues() {
        assertThat(task.getTaskName()).isEqualTo("UpdatePartyExpertsTask");
        assertThat(task.getEventSummary()).contains("Update case party experts");
        assertThat(task.getEventDescription()).contains("UpdatePartyExpertsTask updates experts");
    }

    private CaseReference caseReference(String value) {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(value);
        return caseReference;
    }
}
