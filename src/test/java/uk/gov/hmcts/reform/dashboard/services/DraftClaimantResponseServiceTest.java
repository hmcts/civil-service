package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimantResponseNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftClaimantResponseServiceTest {

    private static final String USER_ID = "user1";
    private static final String CASE_ID = "1234567890123456";
    private static final String OTHER_CASE_ID = "1234567890123457";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIMANT_RESPONSE;

    @Mock
    private DraftStoreService draftStoreService;

    @InjectMocks
    private DraftClaimantResponseService draftClaimantResponseService;

    @Test
    void shouldKeepClaimantResponseDraftsForThirtyDays() {
        assertThat(DRAFT_TYPE.getRetentionDays()).isEqualTo(30L);
        assertThat(DRAFT_TYPE.getPaymentRetentionDays()).isZero();
    }

    @Nested
    class CreateDraftResponseTests {

        @Test
        void shouldCreateDraftForCase() {
            Map<String, Object> payload = Map.of("step", "intention");
            DraftStoreEntity created = draft(CASE_ID);
            when(draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE)).thenReturn(created);

            DraftStoreEntity result = draftClaimantResponseService.createDraftResponse(USER_ID, CASE_ID, payload);

            assertThat(result).isSameAs(created);
            verify(draftStoreService).createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldKeepDraftsForDifferentCasesSeparate() {
            Map<String, Object> payload = Map.of("step", "intention");
            DraftStoreEntity otherCaseDraft = draft(OTHER_CASE_ID);
            when(draftStoreService.createDraft(USER_ID, OTHER_CASE_ID, payload, DRAFT_TYPE)).thenReturn(otherCaseDraft);

            DraftStoreEntity result = draftClaimantResponseService.createDraftResponse(USER_ID, OTHER_CASE_ID, payload);

            assertThat(result.getCaseId()).isEqualTo(OTHER_CASE_ID);
            verify(draftStoreService).createDraft(USER_ID, OTHER_CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldRejectDuplicateDraftForSameUserAndCase() {
            Map<String, Object> payload = Map.of("step", "intention");
            DataIntegrityViolationException uniqueViolation =
                new DataIntegrityViolationException("uq_draft_store_claimant_response_user_case");
            when(draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE)).thenThrow(uniqueViolation);

            assertThatThrownBy(() -> draftClaimantResponseService.createDraftResponse(USER_ID, CASE_ID, payload))
                .isInstanceOf(DraftClaimantResponseAlreadyExistsException.class)
                .hasCause(uniqueViolation);
        }

        @Test
        void shouldRejectBlankCaseId() {
            assertThatThrownBy(() -> draftClaimantResponseService.createDraftResponse(USER_ID, "  ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("caseId must not be blank");

            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldRejectNullUserId() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftClaimantResponseService.createDraftResponse(null, CASE_ID, Map.of()))
                .withMessage("userId must not be null");

            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldRejectNullPayload() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftClaimantResponseService.createDraftResponse(USER_ID, CASE_ID, null))
                .withMessage("payload must not be null");

            verifyNoInteractions(draftStoreService);
        }
    }

    @Nested
    class ReadDraftResponseTests {

        @Test
        void shouldReturnActiveDraftForCase() {
            DraftStoreEntity stored = draft(CASE_ID);
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE))
                .thenReturn(Optional.of(stored));

            assertThat(draftClaimantResponseService.getDraftResponseForCase(USER_ID, CASE_ID))
                .contains(stored);
        }

        @Test
        void shouldReturnEmptyWhenCaseDraftIsExpiredOrMissing() {
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE))
                .thenReturn(Optional.empty());

            assertThat(draftClaimantResponseService.getDraftResponseForCase(USER_ID, CASE_ID)).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenCaseIdIsBlank() {
            assertThat(draftClaimantResponseService.getDraftResponseForCase(USER_ID, " ")).isEmpty();
            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldReturnDraftById() {
            DraftStoreEntity stored = draft(CASE_ID);
            when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(stored));

            assertThat(draftClaimantResponseService.getDraftResponse(DRAFT_ID, USER_ID)).contains(stored);
        }
    }

    @Nested
    class UpdateAndDeleteTests {

        @Test
        void shouldUpdateDraft() {
            Map<String, Object> payload = Map.of("step", "updated");
            DraftStoreEntity updated = draft(CASE_ID);
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.of(updated));

            assertThat(draftClaimantResponseService.updateDraftResponse(DRAFT_ID, USER_ID, CASE_ID, payload))
                .isSameAs(updated);
        }

        @Test
        void shouldRejectUpdateWhenDraftIsMissing() {
            Map<String, Object> payload = Map.of("step", "updated");
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                draftClaimantResponseService.updateDraftResponse(DRAFT_ID, USER_ID, CASE_ID, payload))
                .isInstanceOf(DraftClaimantResponseNotFoundException.class);
        }

        @Test
        void shouldDeleteDraft() {
            when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(true);

            draftClaimantResponseService.deleteDraftResponse(DRAFT_ID, USER_ID);

            verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldRejectDeleteWhenDraftIsMissing() {
            when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(false);

            assertThatThrownBy(() -> draftClaimantResponseService.deleteDraftResponse(DRAFT_ID, USER_ID))
                .isInstanceOf(DraftClaimantResponseNotFoundException.class);
        }
    }

    private static DraftStoreEntity draft(String caseId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            caseId,
            DRAFT_TYPE,
            Map.of("step", "intention"),
            now,
            now,
            now.plusDays(30),
            null
        );
    }
}
