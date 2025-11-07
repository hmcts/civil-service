package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import java.util.Map;

class LitigationFriendCaseReferenceTest {

    private LitigationFriendCaseReference caseReference;

    @BeforeEach
    void setUp() {
        caseReference = new LitigationFriendCaseReference();
    }

    @Test
    void shouldSetCaseReferenceFromRowValues() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("caseReference", "CASE-123");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("CASE-123");
    }

    @Test
    void shouldMapApplicant1LitigationFriendCorrectly() throws Exception {
        LitigationFriend friend = LitigationFriend.builder()
            .firstName("Alice")
            .lastName("Smith")
            .emailAddress("alice@example.com")
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("applicant1LitigationFriend", friend);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(friend);
        assertThat(caseReference.getLitigationFriend()).isEqualTo(friend);
        assertThat(caseReference.isApplicant1()).isTrue();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapApplicant2LitigationFriendCorrectly() throws Exception {
        LitigationFriend friend = LitigationFriend.builder()
            .firstName("Bob")
            .lastName("Johnson")
            .emailAddress("bob@example.com")
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("applicant2LitigationFriend", friend);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(friend);
        assertThat(caseReference.getLitigationFriend()).isEqualTo(friend);
        assertThat(caseReference.isApplicant2()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapRespondent1LitigationFriendCorrectly() throws Exception {
        LitigationFriend friend = LitigationFriend.builder()
            .firstName("Charlie")
            .lastName("Brown")
            .emailAddress("charlie@example.com")
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("respondent1LitigationFriend", friend);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(friend);
        assertThat(caseReference.getLitigationFriend()).isEqualTo(friend);
        assertThat(caseReference.isRespondent1()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }

    @Test
    void shouldMapRespondent2LitigationFriendCorrectly() throws Exception {
        LitigationFriend friend = LitigationFriend.builder()
            .firstName("Diana")
            .lastName("Prince")
            .emailAddress("diana@example.com")
            .build();

        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("respondent2LitigationFriend", friend);

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getDataObject()).isEqualTo(friend);
        assertThat(caseReference.getLitigationFriend()).isEqualTo(friend);
        assertThat(caseReference.getLitigationFriend()).isEqualTo(friend);
        assertThat(caseReference.isRespondent2()).isTrue();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
    }

    @Test
    void shouldIgnoreUnknownKeysGracefully() throws Exception {
        Map<String, Object> rowValues = new HashMap<>();
        rowValues.put("unknownKey", "irrelevant");
        rowValues.put("caseReference", "CASE-999");

        caseReference.fromExcelRow(rowValues);

        assertThat(caseReference.getCaseReference()).isEqualTo("CASE-999");
        assertThat(caseReference.getDataObject()).isNull();
        assertThat(caseReference.getLitigationFriend()).isNull();
        assertThat(caseReference.isApplicant1()).isFalse();
        assertThat(caseReference.isApplicant2()).isFalse();
        assertThat(caseReference.isRespondent1()).isFalse();
        assertThat(caseReference.isRespondent2()).isFalse();
    }
}
