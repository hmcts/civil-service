package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseNotFoundException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;

import java.time.OffsetDateTime;
import java.util.HashMap;
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
class DraftDefendantResponseServiceTest {

    private static final String USER_ID = "user1";
    private static final String CASE_ID = "12345";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_DEFENDANT_RESPONSE;

    @Mock
    private DraftStoreService draftStoreService;

    @InjectMocks
    private DraftDefendantResponseService draftDefendantResponseService;

    @Nested
    class CreateDraftResponseTests {

        @Test
        void shouldCreateDraftForCase() {
            Map<String, Object> payload = Map.of("step", "response-details");
            DraftStoreEntity created = draft();
            when(draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE)).thenReturn(created);

            DraftStoreEntity result = draftDefendantResponseService.createDraftResponse(USER_ID, " " + CASE_ID + " ", payload);

            assertThat(result).isSameAs(created);
            verify(draftStoreService).createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldRejectCreationWhenResponseAlreadyExistsForCase() {
            Map<String, Object> payload = Map.of("step", "response-details");
            DataIntegrityViolationException uniqueViolation =
                new DataIntegrityViolationException("uq_draft_store_response_user_case");
            when(draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE)).thenThrow(uniqueViolation);

            assertThatThrownBy(() -> draftDefendantResponseService.createDraftResponse(USER_ID, CASE_ID, payload))
                .isInstanceOf(DraftDefendantResponseAlreadyExistsException.class)
                .hasCause(uniqueViolation)
                .hasMessage("An active draft defendant response already exists for this user");
        }

        @Test
        void shouldRejectCreationWhenCaseIdIsBlank() {
            assertThatThrownBy(() -> draftDefendantResponseService.createDraftResponse(USER_ID, " ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("caseId must not be blank");

            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldRejectCreationWhenUserIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftDefendantResponseService.createDraftResponse(null, CASE_ID, Map.of()))
                .withMessage("userId must not be null");

            verifyNoInteractions(draftStoreService);
        }

        @Test
        void shouldRejectCreationWhenPayloadIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftDefendantResponseService.createDraftResponse(USER_ID, CASE_ID, null))
                .withMessage("payload must not be null");

            verifyNoInteractions(draftStoreService);
        }
    }

    @Nested
    class GetDraftResponseTests {

        @Test
        void shouldReturnDraftWhenDraftExists() {
            DraftStoreEntity existing = draft();
            when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(Optional.of(existing));

            assertThat(draftDefendantResponseService.getDraftResponse(DRAFT_ID, USER_ID)).contains(existing);
        }

        @Test
        void shouldReturnEmptyWhenDraftDoesNotExist() {
            when(draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(Optional.empty());

            assertThat(draftDefendantResponseService.getDraftResponse(DRAFT_ID, USER_ID)).isEmpty();
        }

        @Test
        void shouldReturnDraftForCase() {
            DraftStoreEntity existing = draft();
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE)).thenReturn(Optional.of(existing));

            assertThat(draftDefendantResponseService.getDraftResponseForCase(USER_ID, " " + CASE_ID)).contains(existing);
            verify(draftStoreService).getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE);
        }

        @Test
        void shouldReturnEmptyWhenNoDraftExistsForCase() {
            when(draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE)).thenReturn(Optional.empty());

            assertThat(draftDefendantResponseService.getDraftResponseForCase(USER_ID, CASE_ID)).isEmpty();
        }

        @Test
        void shouldReturnEmptyWithoutQueryingWhenCaseIdIsBlank() {
            assertThat(draftDefendantResponseService.getDraftResponseForCase(USER_ID, " ")).isEmpty();
            verifyNoInteractions(draftStoreService);
        }
    }

    @Nested
    class UpdateDraftResponseTests {

        @Test
        void shouldReturnUpdatedDraftWhenDraftExists() {
            Map<String, Object> payload = Map.of("step", "updated");
            DraftStoreEntity updated = draft();
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.of(updated));

            DraftStoreEntity result = draftDefendantResponseService.updateResponseDraft(
                DRAFT_ID,
                USER_ID,
                " " + CASE_ID + " ",
                payload
            );

            assertThat(result).isSameAs(updated);
            verify(draftStoreService).updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE);
        }

        @Test
        void shouldPassNullCaseIdWhenUpdateCaseIdIsBlank() {
            Map<String, Object> payload = Map.of("step", "updated");
            DraftStoreEntity updated = draft();
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, null, payload, DRAFT_TYPE))
                .thenReturn(Optional.of(updated));

            draftDefendantResponseService.updateResponseDraft(DRAFT_ID, USER_ID, "", payload);

            verify(draftStoreService).updateDraft(DRAFT_ID, USER_ID, null, payload, DRAFT_TYPE);
        }

        @Test
        void shouldThrowNotFoundWhenUpdatingMissingDraft() {
            Map<String, Object> payload = Map.of("step", "updated");
            when(draftStoreService.updateDraft(DRAFT_ID, USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> draftDefendantResponseService.updateResponseDraft(DRAFT_ID, USER_ID, CASE_ID, payload))
                .isInstanceOf(DraftDefendantResponseNotFoundException.class)
                .hasMessage("No active draft defendant response found for draftId " + DRAFT_ID);
        }
    }

    @Nested
    class DeleteDraftResponseTests {

        @Test
        void shouldDeleteDraftWhenDraftExists() {
            when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(true);

            draftDefendantResponseService.deleteDraftResponse(DRAFT_ID, USER_ID);

            verify(draftStoreService).deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);
        }

        @Test
        void shouldThrowNotFoundWhenDeletingMissingDraft() {
            when(draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE)).thenReturn(false);

            assertThatThrownBy(() -> draftDefendantResponseService.deleteDraftResponse(DRAFT_ID, USER_ID))
                .isInstanceOf(DraftDefendantResponseNotFoundException.class)
                .hasMessage("No active draft defendant response found for draftId " + DRAFT_ID);
        }
    }

    private DraftStoreEntity draft() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-06T10:00:00Z");
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            DRAFT_TYPE,
            new HashMap<>(Map.of("step", "existing")),
            createdAt,
            createdAt,
            createdAt.plusDays(180),
            null
        );
    }
}
