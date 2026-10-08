package uk.gov.hmcts.reform.draftstore.services;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class DraftStoreServiceTest {

    private static final String USER_ID = "user1";
    private static final String CASE_ID = "ccd1";
    private static final String NEW_CASE_ID = "ccd2";
    private static final UUID DRAFT_ID = UUID.randomUUID();
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIM;
    private static final long RETENTION_DAYS = DRAFT_TYPE.getRetentionDays();

    @Mock
    private DraftStoreRepository draftStoreRepository;

    @Mock
    private DraftStoreTransactionService draftStoreTransactionService;

    @InjectMocks
    private DraftStoreService draftStoreService;

    @Nested
    class CreateDraftTests {

        @Test
        void shouldCreateDraftWithExpiryFromDraftTypeRetention() {
            Map<String, Object> payload = new HashMap<>(Map.of(
                "step", "claimant-details",
                "draftClaimCacheTtlDays", RETENTION_DAYS
            ));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            ArgumentCaptor<DraftStoreEntity> captor = ArgumentCaptor.forClass(DraftStoreEntity.class);
            verify(draftStoreTransactionService).saveInNewTransaction(captor.capture());
            DraftStoreEntity savedDraft = captor.getValue();
            assertThat(result).isSameAs(savedDraft);
            assertThat(savedDraft.getId()).isNotNull();
            assertThat(savedDraft.getUserId()).isEqualTo(USER_ID);
            assertThat(savedDraft.getCaseId()).isEqualTo(CASE_ID);
            assertThat(savedDraft.getDraftType()).isEqualTo(DRAFT_TYPE);
            assertThat(savedDraft.getPayload()).isEqualTo(payload).isNotSameAs(payload);
            assertThat(savedDraft.getCreatedAt()).isNotNull();
            assertThat(savedDraft.getUpdatedAt()).isEqualTo(savedDraft.getCreatedAt());
            assertThat(savedDraft.getExpiresAt()).isEqualTo(
                DraftStoreService.calculateExpiresAt(savedDraft.getCreatedAt(), RETENTION_DAYS)
            );
        }

        @Test
        void shouldCreateDraftWhenPayloadOmitsTtlField() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details"));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            DraftStoreEntity result = draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE);

            assertThat(result.getExpiresAt()).isEqualTo(
                DraftStoreService.calculateExpiresAt(result.getCreatedAt(), RETENTION_DAYS)
            );
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.draftstore.services.DraftStoreServiceTest#outOfPolicyPayloadTtlValues")
        void shouldRejectOutOfPolicyPayloadDraftClaimCacheTtlDays(Object payloadTtl) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("step", "claimant-details");
            payload.put("draftClaimCacheTtlDays", payloadTtl);

            assertThatThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException statusEx = (ResponseStatusException) ex;
                    assertThat(statusEx.getStatusCode()).isEqualTo(BAD_REQUEST);
                    assertThat(statusEx.getReason()).contains("draftClaimCacheTtlDays must be " + RETENTION_DAYS);
                });

            verifyNoInteractions(draftStoreTransactionService);
            verifyNoInteractions(draftStoreRepository);
        }

        @Test
        void shouldRejectCreationWhenUserIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraft(null, CASE_ID, Map.of(), DRAFT_TYPE))
                .withMessage("userId must not be null");

            verifyNoInteractions(draftStoreRepository);
        }

        @Test
        void shouldRejectCreationWhenPayloadIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, null, DRAFT_TYPE))
                .withMessage("payload must not be null");

            verifyNoInteractions(draftStoreRepository);
        }

        @Test
        void shouldPropagateUniqueConstraintViolationWithoutReturningAnotherDraft() {
            Map<String, Object> payload = new HashMap<>(Map.of("step", "claimant-details"));
            DataIntegrityViolationException uniqueViolation =
                new DataIntegrityViolationException("uq_draft_store_user_case");
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class))).thenThrow(uniqueViolation);

            assertThatThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, payload, DRAFT_TYPE))
                .isSameAs(uniqueViolation);
            verifyNoInteractions(draftStoreRepository);
        }

        @Test
        void shouldRejectCreationWhenDraftTypeIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.createDraft(USER_ID, CASE_ID, Map.of(), null))
                .withMessage("draftType must not be null");

            verifyNoInteractions(draftStoreRepository);
        }
    }

    @Nested
    class CalculateExpiresAtTests {

        private static final ZoneId LONDON = ZoneId.of("Europe/London");

        @Test
        void shouldExpireAtLondonStartOfDayAfterTtlPlusOneDay() {
            // 12:00 BST on 1 Oct 2026
            OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-01T11:00:00Z");

            OffsetDateTime expiresAt = DraftStoreService.calculateExpiresAt(createdAt, 30);

            // Displayed deadline day is 31 Oct; access until end of that day → 1 Nov 00:00 London (GMT)
            assertThat(expiresAt).isEqualTo(OffsetDateTime.parse("2026-11-01T00:00:00Z"));
            assertThat(expiresAt.atZoneSameInstant(LONDON).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        void shouldExpireAtLondonMidnightAcrossSpringDstTransition() {
            // UK clocks go forward on 29 Mar 2026; creation still on GMT
            OffsetDateTime createdAt = OffsetDateTime.parse("2026-03-28T12:00:00Z");

            OffsetDateTime expiresAt = DraftStoreService.calculateExpiresAt(createdAt, 1);

            // 28 Mar + 2 days = 30 Mar 00:00 Europe/London (BST, UTC+1)
            assertThat(expiresAt.atZoneSameInstant(LONDON).toLocalDate().toString()).isEqualTo("2026-03-30");
            assertThat(expiresAt.atZoneSameInstant(LONDON).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
            assertThat(expiresAt).isEqualTo(OffsetDateTime.parse("2026-03-29T23:00:00Z"));
        }

        @Test
        void shouldExpireAtLondonMidnightAcrossAutumnDstTransition() {
            // UK clocks go back on 25 Oct 2026
            OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-24T12:00:00+01:00");

            OffsetDateTime expiresAt = DraftStoreService.calculateExpiresAt(createdAt, 1);

            // 24 Oct + 2 days = 26 Oct 00:00 Europe/London (GMT)
            assertThat(expiresAt.atZoneSameInstant(LONDON).toLocalDate().toString()).isEqualTo("2026-10-26");
            assertThat(expiresAt.atZoneSameInstant(LONDON).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
            assertThat(expiresAt).isEqualTo(OffsetDateTime.parse("2026-10-26T00:00:00Z"));
        }
    }

    @Nested
    class GetDraftTests {

        @Test
        void shouldReturnBlankDraftIncludingExpired() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByUserIdAndDraftTypeAndCaseIdIsNull(USER_ID, DRAFT_TYPE))
                .thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftStoreService.getBlankDraft(USER_ID, DRAFT_TYPE);

            assertThat(result).contains(draft);
        }

        @Test
        void shouldReturnActiveBlankDraft() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByUserIdAndDraftTypeAndCaseIdIsNullAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftStoreService.getActiveBlankDraft(USER_ID, DRAFT_TYPE);

            assertThat(result).contains(draft);
        }

        @Test
        void shouldReturnActiveDraftForCase() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByUserIdAndDraftTypeAndCaseIdAndExpiresAtAfter(
                eq(USER_ID),
                eq(DRAFT_TYPE),
                eq(CASE_ID),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftStoreService.getActiveDraftForCase(USER_ID, CASE_ID, DRAFT_TYPE);

            assertThat(result).contains(draft);
        }

        @Test
        void shouldReturnDraftWhenOwnedUnexpiredDraftExists() {
            DraftStoreEntity draft = draft();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(draft));

            Optional<DraftStoreEntity> result = draftStoreService.getDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).contains(draft);
        }

        @Test
        void shouldRejectLookupWhenDraftIdIsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> draftStoreService.getDraft(null, USER_ID, DRAFT_TYPE))
                .withMessage("draftId must not be null");

            verifyNoInteractions(draftStoreRepository);
        }
    }

    @Nested
    class ApplyPaymentRetentionTests {

        @Test
        void shouldShortenExpiryToPaymentRetentionWhenDraftExists() {
            DraftStoreEntity existingDraft = draft();
            OffsetDateTime originalExpiry = existingDraft.getExpiresAt();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreTransactionService.updateExpiresAtInNewTransaction(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class)
            )).thenReturn(1);

            Optional<DraftStoreEntity> result = draftStoreService.applyPaymentRetention(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            );

            assertThat(result).isPresent();
            assertThat(result.get().getExpiresAt()).isBefore(originalExpiry);
            assertThat(result.get().getExpiresAt()).isEqualTo(
                DraftStoreService.calculateExpiresAt(
                    result.get().getUpdatedAt(),
                    DRAFT_TYPE.getPaymentRetentionDays()
                )
            );
            verify(draftStoreTransactionService).updateExpiresAtInNewTransaction(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                eq(result.get().getExpiresAt()),
                eq(result.get().getUpdatedAt())
            );
        }

        @Test
        void shouldNotExtendExpiryWhenPaymentWindowIsLaterThanCurrentExpiry() {
            OffsetDateTime createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC).minusDays(1);
            // Still active, but ends sooner than the 7-day payment window from now
            OffsetDateTime soonExpiry = OffsetDateTime.now(java.time.ZoneOffset.UTC).plusDays(2);
            DraftStoreEntity existingDraft = new DraftStoreEntity(
                DRAFT_ID,
                USER_ID,
                CASE_ID,
                DRAFT_TYPE,
                new HashMap<>(Map.of("step", "existing")),
                createdAt,
                createdAt,
                soonExpiry
            );
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));

            Optional<DraftStoreEntity> result = draftStoreService.applyPaymentRetention(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            );

            assertThat(result).contains(existingDraft);
            assertThat(existingDraft.getExpiresAt()).isEqualTo(soonExpiry);
            verifyNoInteractions(draftStoreTransactionService);
        }

        @Test
        void shouldReturnEmptyWhenDraftMissing() {
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.empty());

            Optional<DraftStoreEntity> result = draftStoreService.applyPaymentRetention(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            );

            assertThat(result).isEmpty();
            verifyNoInteractions(draftStoreTransactionService);
        }
    }

    @Nested
    class UpdateDraftTests {

        @Test
        void shouldUpdateDraftWhenOwnedUnexpiredDraftExists() {
            DraftStoreEntity existingDraft = draft();
            Map<String, Object> payload = new HashMap<>(Map.of("step", "updated"));
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Optional<DraftStoreEntity> result = draftStoreService.updateDraft(
                DRAFT_ID,
                USER_ID,
                NEW_CASE_ID,
                payload,
                DRAFT_TYPE
            );

            assertThat(result).contains(existingDraft);
            assertThat(existingDraft.getCaseId()).isEqualTo(NEW_CASE_ID);
            assertThat(existingDraft.getPayload()).isEqualTo(payload).isNotSameAs(payload);
            assertThat(existingDraft.getUpdatedAt()).isNotNull();
            verify(draftStoreTransactionService).saveInNewTransaction(existingDraft);
        }

        @Test
        void shouldKeepCaseIdWhenUpdateCaseIdIsNull() {
            DraftStoreEntity existingDraft = draft();
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.of(existingDraft));
            when(draftStoreTransactionService.saveInNewTransaction(any(DraftStoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Optional<DraftStoreEntity> result = draftStoreService.updateDraft(
                DRAFT_ID,
                USER_ID,
                null,
                Map.of("step", "updated"),
                DRAFT_TYPE
            );

            assertThat(result).contains(existingDraft);
            assertThat(existingDraft.getCaseId()).isEqualTo(CASE_ID);
        }

        @Test
        void shouldReturnEmptyWhenUpdatingMissingDraft() {
            when(draftStoreRepository.findByIdAndUserIdAndDraftTypeAndExpiresAtAfter(
                eq(DRAFT_ID),
                eq(USER_ID),
                eq(DRAFT_TYPE),
                any(OffsetDateTime.class)
            )).thenReturn(Optional.empty());

            Optional<DraftStoreEntity> result = draftStoreService.updateDraft(
                DRAFT_ID,
                USER_ID,
                CASE_ID,
                Map.of(),
                DRAFT_TYPE
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class DeleteDraftTests {

        @Test
        void shouldReturnTrueWhenDraftIsDeleted() {
            when(draftStoreTransactionService.deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            )).thenReturn(1L);

            boolean result = draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).isTrue();
            verify(draftStoreTransactionService).deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE);
        }

        @Test
        void shouldReturnFalseWhenDraftDoesNotExist() {
            when(draftStoreTransactionService.deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE
            )).thenReturn(0L);

            boolean result = draftStoreService.deleteDraft(DRAFT_ID, USER_ID, DRAFT_TYPE);

            assertThat(result).isFalse();
            verify(draftStoreTransactionService).deleteByIdInNewTransaction(
                DRAFT_ID,
                USER_ID,
                DRAFT_TYPE);
        }

        @Test
        void shouldDeleteAndFlushWhenReplacingExpiredDraft() {
            DraftStoreEntity draft = draft();

            draftStoreService.deleteDraftAndFlush(draft);

            verify(draftStoreTransactionService).deleteInNewTransaction(draft);
        }
    }

    static Stream<Arguments> outOfPolicyPayloadTtlValues() {
        return Stream.of(
            Arguments.of(1L),
            Arguments.of(14),
            Arguments.of(9999L),
            Arguments.of("60"),
            Arguments.of("not-a-number"),
            Arguments.of(0L),
            Arguments.of(-5L)
        );
    }

    private DraftStoreEntity draft() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        return new DraftStoreEntity(
            DRAFT_ID,
            USER_ID,
            CASE_ID,
            DRAFT_TYPE,
            new HashMap<>(Map.of("step", "existing")),
            createdAt,
            createdAt,
            createdAt.plusDays(30)
        );
    }
}
