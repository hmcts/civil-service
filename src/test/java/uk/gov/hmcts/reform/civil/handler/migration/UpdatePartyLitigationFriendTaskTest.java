package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.LitigationFriendCaseReference;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePartyLitigationFriendTaskTest {

    private UpdatePartyLitigationFriendTask task;

    @BeforeEach
    void setUp() {
        task = new UpdatePartyLitigationFriendTask();
    }

    @Test
    void migrateCaseData_shouldUpdateApplicant1LitigationFriend() {
        LitigationFriend existing = new LitigationFriend().setFirstName("OldFirst")
            .setLastName("OldLast")
            .setHasSameAddressAsLitigant(YesOrNo.NO)
            ;

        CaseData caseData = CaseData.builder()
            .applicant1LitigationFriend(existing)
            .build();

        LitigationFriend updatedLitFriend = new LitigationFriend().setFirstName("NewFirst")
            .setLastName("NewLast")
            .setEmailAddress("new@example.com")
            ;

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
        LitigationFriend existing = new LitigationFriend().setFirstName("OldA2");
        LitigationFriend update = new LitigationFriend().setFirstName("NewA2");

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
        LitigationFriend existing = new LitigationFriend().setFirstName("OldR1");
        LitigationFriend update = new LitigationFriend().setFirstName("NewR1");

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
        LitigationFriend existing = new LitigationFriend().setFirstName("OldR2");
        LitigationFriend update = new LitigationFriend().setFirstName("NewR2");

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
    void migrateCaseData_shouldThrow_whenLitigationFriendNotFound() {
        CaseData caseData = CaseData.builder().build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("123");
        caseRef.setApplicant1(true);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> task.migrateCaseData(caseData, caseRef)
        );

        assertEquals("Failed to determine Party to update", ex.getMessage());
    }

    @Test
    void migrateCaseData_shouldThrow_whenNoPartyFlagsSet() {
        LitigationFriend existing = new LitigationFriend().setFirstName("Old");
        CaseData caseData = CaseData.builder().applicant1LitigationFriend(existing).build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("XYZ123");
        caseRef.setLitigationFriend(new LitigationFriend().setFirstName("X"));
        // no party flags set

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> task.migrateCaseData(caseData, caseRef)
        );

        assertEquals("Failed to determine Party to update", ex.getMessage());
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
}
