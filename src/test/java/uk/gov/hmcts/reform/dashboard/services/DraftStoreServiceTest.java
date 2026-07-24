package uk.gov.hmcts.reform.dashboard.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.dashboard.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftStoreServiceTest {

    private static final String USER_ID = "user1";
    private static final String CASE_ID = "ccd1";
    private static final String NEW_CASE_ID = "ccd2";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final long DRAFT_EXPIRY_DAYS = 180;
    private static final int DRAFT_CLAIM_TYPE_ID = 1;

    @Mock
    private DraftStoreRepository draftStoreRepository;

    @InjectMocks
    private DraftStoreService draftStoreService;

    private DraftStoreEntity draftClaim;

    @Nested
    class CreateDraftClaimTest {

        @Test
        void shouldCreateDraftClaimWithFixedExpiry() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details"));
            when(draftStoreRepository.save(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraftClaim(USER_ID, CASE_ID, payload);

            ArgumentCaptor<DraftStoreEntity> captor = ArgumentCaptor.forClass(DraftStoreEntity.class);
            verify(draftStoreRepository).save(captor.capture());
            DraftStoreEntity savedDraft = captor.getValue();

            assertThat(result).isSameAs(savedDraft);
            assertThat(savedDraft.getId()).isNotNull();
            assertThat(savedDraft.getUserId()).isEqualTo(USER_ID);
            assertThat(savedDraft.getCaseId()).isEqualTo(CASE_ID);
            assertThat(savedDraft.getDraftTypeId()).isEqualTo(DRAFT_CLAIM_TYPE_ID);
            assertThat(savedDraft.getPayload()).isEqualTo(payload).isNotSameAs(payload);
            assertThat(savedDraft.getDraftClaimCreatedAt()).isEqualTo(savedDraft.getCreatedAt());
            assertThat(savedDraft.getUpdatedAt()).isEqualTo(savedDraft.getCreatedAt());
            assertThat(savedDraft.getExpiresAt()).isEqualTo(savedDraft.getDraftClaimCreatedAt().plusDays(DRAFT_EXPIRY_DAYS));
        }

        @Test
        void shouldRejectNullUserIdWhenCreatingDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraftClaim(null, CASE_ID, Map.of()))
                .withMessage("userId must not be null");

            verify(draftStoreRepository, never()).save(any());
        }

        @Test
        void shouldRejectNullPayloadWhenCreatingDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraftClaim(USER_ID, CASE_ID, null))
                .withMessage("payload must not be null");

            verify(draftStoreRepository, never()).save(any());
        }

    }

    @Nested
    class GetDraftClaimTests {

        @Test
        void shouldReturnDraftClaimUsingIdUserIdDraftIdAndExpiresAtAfter() {
            draftClaim = mock(DraftStoreEntity.class);

            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                any(OffsetDateTime.class)))
                .thenReturn(Optional.of(draftClaim));

            Optional<DraftStoreEntity> result = draftStoreService.getDraftClaim(DRAFT_ID, USER_ID);

            ArgumentCaptor<OffsetDateTime> dateCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

            verify(draftStoreRepository).findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                dateCaptor.capture());

            assertThat(result).contains(draftClaim);
            assertThat(dateCaptor.getValue()).isBeforeOrEqualTo(OffsetDateTime.now());
        }

        @Test
        void shouldRejectNullDraftIdWhenGettingDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.getDraftClaim(null, USER_ID))
                .withMessage("draftId must not be null");

            verify(draftStoreRepository, never()).save(any());

        }

        @Test
        void shouldRejectNullUserIdWhenGettingDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.getDraftClaim(DRAFT_ID, null))
                .withMessage("userId must not be null");

            verify(draftStoreRepository, never()).save(any());
        }

    }

    @Nested
    class GetActiveDraftTests {

        @Test
        void shouldReturnActiveDraftClaimUsingUserId() {
            draftClaim = mock(DraftStoreEntity.class);

            when(draftStoreRepository.findFirstByUserIdAndDraftTypeIdAndExpiresAtAfterOrderByUpdatedAtDesc(
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                any(OffsetDateTime.class)))
                .thenReturn(Optional.of(draftClaim));

            Optional<DraftStoreEntity> result = draftStoreService.getActiveDraftClaimForUser(USER_ID);

            ArgumentCaptor<OffsetDateTime> dateCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

            verify(draftStoreRepository).findFirstByUserIdAndDraftTypeIdAndExpiresAtAfterOrderByUpdatedAtDesc(
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                dateCaptor.capture());

            assertThat(result).contains(draftClaim);
            assertThat(dateCaptor.getValue()).isBeforeOrEqualTo(OffsetDateTime.now());
        }

        @Test
        void shouldRejectNullUserIdWhenGettingActiveDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.getActiveDraftClaimForUser(null))
                .withMessage("userId must not be null");

            verify(draftStoreRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateDraftClaimTests {

        @Test
        void shouldUpdateDraftClaimWhenItExists() {
            DraftStoreEntity existingDraft = new DraftStoreEntity();
            existingDraft.setId(DRAFT_ID);
            existingDraft.setUserId(USER_ID);
            existingDraft.setCaseId(CASE_ID);
            existingDraft.setDraftTypeId(DRAFT_CLAIM_TYPE_ID);

            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                any(OffsetDateTime.class)))
                .thenReturn(Optional.of(existingDraft));

            when(draftStoreRepository.save(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Map<String, Object> newPayload = new HashMap<>(Map.of("step", "updated-claimant-details"));
            DraftStoreEntity result = draftStoreService.updateDraftClaim(DRAFT_ID, USER_ID, NEW_CASE_ID, newPayload);

            ArgumentCaptor<DraftStoreEntity> captor = ArgumentCaptor.forClass(DraftStoreEntity.class);

            verify(draftStoreRepository).save(captor.capture());
            DraftStoreEntity updatedDraft = captor.getValue();

            assertThat(result).isSameAs(updatedDraft);
            assertThat(updatedDraft.getCaseId()).isEqualTo(NEW_CASE_ID);
            assertThat(updatedDraft.getPayload()).isEqualTo(newPayload);
            assertThat(updatedDraft.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldThrowExceptionIfDraftClaimDoesNotExist() {
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                any(OffsetDateTime.class)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> draftStoreService.updateDraftClaim(DRAFT_ID, USER_ID, CASE_ID, Map.of()))
                .isInstanceOf(DraftClaimNotFoundException.class);

            verify(draftStoreRepository, never()).save(any());
        }

        @Test
        void shouldKeepExistingCaseIdWhenCaseIdIsNull() {
            DraftStoreEntity existingDraft = new DraftStoreEntity();
            existingDraft.setCaseId("og-case-id");

            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeIdAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_CLAIM_TYPE_ID),
                any(OffsetDateTime.class)))
                .thenReturn(Optional.of(existingDraft));

            when(draftStoreRepository.save(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.updateDraftClaim(
                DRAFT_ID,
                USER_ID,
                null,
                Map.of("step", "updated"));

            assertThat(result.getCaseId()).isEqualTo("og-case-id");
            assertThat(result.getPayload()).containsEntry("step", "updated");
            assertThat(result.getUpdatedAt()).isNotNull();
        }

    }

    @Nested
    class DeleteDraftClaimTests {

        @Test
        void shouldDeleteDraftClaimByIdAndUserIdAndDraftTypeId() {
            when(draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
                DRAFT_ID,
                USER_ID,
                DRAFT_CLAIM_TYPE_ID))
                .thenReturn(1L);

            draftStoreService.deleteDraftClaim(DRAFT_ID, USER_ID);

            verify(draftStoreRepository).deleteByIdAndUserIdAndDraftTypeId(DRAFT_ID, USER_ID, DRAFT_CLAIM_TYPE_ID);
        }

        @Test
        void shouldThrowExceptionWhenDraftClaimNotFound() {
            when(draftStoreRepository.deleteByIdAndUserIdAndDraftTypeId(
                DRAFT_ID,
                USER_ID,
                DRAFT_CLAIM_TYPE_ID))
                .thenReturn(0L);

            assertThatThrownBy(() -> draftStoreService.deleteDraftClaim(DRAFT_ID, USER_ID))
                .isInstanceOf(DraftClaimNotFoundException.class);

            verify(draftStoreRepository).deleteByIdAndUserIdAndDraftTypeId(DRAFT_ID, USER_ID, DRAFT_CLAIM_TYPE_ID);
        }

        @Test
        void shouldRejectNullDraftIdWhenDeletingDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.deleteDraftClaim(null, USER_ID))
                .withMessage("draftId must not be null");

            verify(draftStoreRepository, never()).save(any());
        }

        @Test
        void shouldRejectNullUserIdWhenDeletingDraftClaim() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.deleteDraftClaim(DRAFT_ID, null))
                .withMessage("userId must not be null");

            verify(draftStoreRepository, never()).save(any());
        }
    }

}
