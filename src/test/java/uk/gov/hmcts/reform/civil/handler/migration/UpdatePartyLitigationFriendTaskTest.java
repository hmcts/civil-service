package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.LitigationFriendCaseReference;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePartyLitigationFriendTaskTest {

    private UpdatePartyLitigationFriendTask task;

    @BeforeEach
    void setUp() {
        task = new UpdatePartyLitigationFriendTask();
    }

    @Test
    void migrateCaseData_shouldUpdateApplicant1LitigationFriend() {
        LitigationFriend existing = LitigationFriend.builder()
            .firstName("OldFirst")
            .lastName("OldLast")
            .hasSameAddressAsLitigant(YesOrNo.NO)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1LitigationFriend(existing)
            .build();

        LitigationFriend updatedLitFriend = LitigationFriend.builder()
            .firstName("NewFirst")
            .lastName("NewLast")
            .emailAddress("new@example.com")
            .build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("1234");
        caseRef.setLitigationFriend(updatedLitFriend);
        caseRef.setApplicant1(true);

        CaseData result = task.migrateCaseData(caseData, caseRef);
        LitigationFriend resultLitFriend = result.getApplicant1LitigationFriend();

        assertEquals("NewFirst", resultLitFriend.getFirstName());
        assertEquals("NewLast", resultLitFriend.getLastName());
        assertEquals("new@example.com", resultLitFriend.getEmailAddress());
        assertEquals(YesOrNo.NO, resultLitFriend.getHasSameAddressAsLitigant());
    }

    @Test
    void migrateCaseData_shouldUpdateApplicant2LitigationFriend() {
        LitigationFriend existing = LitigationFriend.builder().firstName("OldA2").build();
        LitigationFriend update = LitigationFriend.builder().firstName("NewA2").build();

        CaseData caseData = CaseData.builder()
            .applicant2LitigationFriend(existing)
            .build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("CASE-A2");
        caseRef.setApplicant2(true);
        caseRef.setLitigationFriend(update);

        CaseData result = task.migrateCaseData(caseData, caseRef);

        assertEquals("NewA2", result.getApplicant2LitigationFriend().getFirstName());
    }

    @Test
    void migrateCaseData_shouldUpdateRespondent1LitigationFriend() {
        LitigationFriend existing = LitigationFriend.builder().firstName("OldR1").build();
        LitigationFriend update = LitigationFriend.builder().firstName("NewR1").build();

        CaseData caseData = CaseData.builder()
            .respondent1LitigationFriend(existing)
            .build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("CASE-R1");
        caseRef.setRespondent1(true);
        caseRef.setLitigationFriend(update);

        CaseData result = task.migrateCaseData(caseData, caseRef);

        assertEquals("NewR1", result.getRespondent1LitigationFriend().getFirstName());
    }

    @Test
    void migrateCaseData_shouldUpdateRespondent2LitigationFriend() {
        LitigationFriend existing = LitigationFriend.builder().firstName("OldR2").build();
        LitigationFriend update = LitigationFriend.builder().firstName("NewR2").build();

        CaseData caseData = CaseData.builder()
            .respondent2LitigationFriend(existing)
            .build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("CASE-R2");
        caseRef.setRespondent2(true);
        caseRef.setLitigationFriend(update);

        CaseData result = task.migrateCaseData(caseData, caseRef);

        assertEquals("NewR2", result.getRespondent2LitigationFriend().getFirstName());
    }

    @Test
    void migrateCaseData_shouldThrow_whenCaseReferenceIsNull() {
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> task.migrateCaseData(caseData, null)
        );

        assertEquals("CaseReference fields must not be null", ex.getMessage());
    }

    @Test
    void migrateCaseData_shouldCreateEmptyLitigationFriend_whenNoneExists() {
        CaseData caseData = CaseData.builder().build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);
        caseRef.setLitigationFriend(LitigationFriend.builder()
                                        .firstName("NewFirst")
                                        .build());

        CaseData result = task.migrateCaseData(caseData, caseRef);
        LitigationFriend resultLf = result.getApplicant1LitigationFriend();

        assertEquals("NewFirst", resultLf.getFirstName());
        // other fields should be null/default
        assertEquals(null, resultLf.getLastName());
    }

    @Test
    void getEventSummary_shouldReturnExpectedText() {
        assertEquals(
            "Update case party litigation friend via migration task",
            task.getEventSummary()
        );
    }

    @Test
    void getTaskName_shouldReturnExpectedText() {
        assertEquals("UpdatePartyLitigationFriendTask", task.getTaskName());
    }

    @Test
    void getEventDescription_shouldReturnExpectedText() {
        assertEquals(
            "This task UpdatePartyLitigationFriendTask updates litigation friend on the case",
            task.getEventDescription()
        );
    }

    @Test
    void migrateCaseData_shouldPreserveExistingFields_whenUpdateFieldsAreNull() {
        LitigationFriend existing = LitigationFriend.builder()
            .firstName("OldFirst")
            .lastName("OldLast")
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1LitigationFriend(existing)
            .build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);
        caseRef.setLitigationFriend(LitigationFriend.builder().build()); // all null fields

        CaseData result = task.migrateCaseData(caseData, caseRef);
        LitigationFriend resultLf = result.getApplicant1LitigationFriend();

        assertEquals("OldFirst", resultLf.getFirstName());
        assertEquals("OldLast", resultLf.getLastName());
    }

    @Test
    void migrateCaseData_shouldGeneratePartyId_ifNull() {
        LitigationFriend existing = LitigationFriend.builder().build();

        CaseData caseData = CaseData.builder()
            .applicant1LitigationFriend(existing)
            .build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);
        caseRef.setLitigationFriend(LitigationFriend.builder().build());

        CaseData result = task.migrateCaseData(caseData, caseRef);
        assertNotNull(result.getApplicant1LitigationFriend().getPartyID());
    }
}
