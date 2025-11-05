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
        task = new UpdatePartyLitigationFriendTask(LitigationFriendCaseReference.class);
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

        // Perform migration
        CaseData result = task.migrateCaseData(caseData, caseRef);

        LitigationFriend resultLitFriend = result.getApplicant1LitigationFriend();

        assertEquals("NewFirst", resultLitFriend.getFirstName());
        assertEquals("NewLast", resultLitFriend.getLastName());
        assertEquals("new@example.com", resultLitFriend.getEmailAddress());

        assertEquals(YesOrNo.NO, resultLitFriend.getHasSameAddressAsLitigant());
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
        CaseData caseData = CaseData.builder().build();
        LitigationFriend updatedLF = LitigationFriend.builder().firstName("X").build();

        LitigationFriendCaseReference caseRef = new LitigationFriendCaseReference();
        caseRef.setCaseReference("9999");
        caseRef.setLitigationFriend(updatedLF);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> task.migrateCaseData(caseData, caseRef)
        );

        assertEquals("Failed to set updated litigation friend in CaseData", ex.getMessage());
    }
}
