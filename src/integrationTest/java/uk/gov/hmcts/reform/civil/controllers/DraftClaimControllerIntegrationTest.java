package uk.gov.hmcts.reform.civil.controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimRequest;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
public class DraftClaimControllerIntegrationTest extends BaseIntegrationTest {

    private static final String DRAFT_CLAIMS_URL = "/dashboard/draft-claims";
    private static final String DRAFT_CLAIM_BY_ID_URL = "/dashboard/draft-claims/{draft-id}";
    private static final String ACTIVE_DRAFT_CLAIM_URL = "/dashboard/draft-claims/active";
    private static final String USER_ID = "user1";
    private static final Map<String, Object> PAYLOAD = Map.of("step", "claimant-details");
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIM;

    @Autowired
    private DraftStoreRepository draftStoreRepository;

    private UUID draftId;

    @BeforeEach
    void setUp() {
        draftStoreRepository.deleteAll();
        draftId = UUID.randomUUID();

        given(userService.getUserInfo(anyString()))
            .willReturn(
                UserInfo.builder()
                    .uid(USER_ID)
                    .sub("test@test.com")
                    .build()
            );

        OffsetDateTime now = OffsetDateTime.now();
        DraftStoreEntity draftClaim = new DraftStoreEntity();
        draftClaim.setId(draftId);
        draftClaim.setUserId(USER_ID);
        draftClaim.setCaseId("12345");
        draftClaim.setDraftTypeId(DRAFT_TYPE.getId());
        draftClaim.setPayload(new HashMap<>(Map.of("step", "active-test")));
        draftClaim.setCreatedAt(now);
        draftClaim.setUpdatedAt(now);
        draftClaim.setExpiresAt(DRAFT_TYPE.calculateExpiry(now));

        draftStoreRepository.save(draftClaim);
    }

    @AfterEach
    void tearDown() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldCreateAndPersistDraftWhenNoActiveDraftExists() throws Exception {
        draftStoreRepository.deleteAll();
        DraftClaimRequest request = new DraftClaimRequest("123", PAYLOAD);

        MvcResult result = doPost(BEARER_TOKEN, request, DRAFT_CLAIMS_URL)
            .andExpect(status().isCreated())
            .andExpectAll(
                jsonPath("$.draftId").exists(),
                jsonPath("$.payload.step").value("claimant-details")
            )
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UUID createdDraftId = UUID.fromString(JsonPath.read(responseBody, "$.draftId"));

        DraftStoreEntity draftInDb = draftStoreRepository.findById(createdDraftId)
            .orElseThrow(() -> new AssertionError("Draft claim should be persisted in database"));

        assertThat(draftInDb.getPayload()).extracting("step").isEqualTo("claimant-details");
        assertThat(draftInDb.getUserId()).isEqualTo(USER_ID);
        assertThat(draftInDb.getExpiresAt()).isEqualTo(draftInDb.getCreatedAt().plusDays(180));
    }

    @Test
    void shouldReturnExistingDraftWithoutChangesWhenActiveDraftExists() throws Exception {
        DraftStoreEntity originalDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        OffsetDateTime originalCreatedAt = originalDraft.getCreatedAt();
        OffsetDateTime originalExpiresAt = originalDraft.getExpiresAt();

        DraftClaimRequest request = new DraftClaimRequest("new-case-id", PAYLOAD);

        doPost(BEARER_TOKEN, request, DRAFT_CLAIMS_URL)
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.caseId").value("12345"),
                jsonPath("$.payload.step").value("active-test")
            );

        assertThat(draftStoreRepository.count()).isOne();
        DraftStoreEntity unchangedDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        assertThat(unchangedDraft.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(unchangedDraft.getExpiresAt()).isEqualTo(originalExpiresAt);
    }

    @Test
    void shouldCreateDraftWhenDifferentUserHasActiveDraft() throws Exception {
        String otherUserToken = "Bearer other-user";
        given(userService.getUserInfo(otherUserToken))
            .willReturn(UserInfo.builder().uid("user2").build());

        doPost(otherUserToken, new DraftClaimRequest("case2", PAYLOAD), DRAFT_CLAIMS_URL)
            .andExpect(status().isCreated());

        assertThat(draftStoreRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldRejectDuplicateDraftWhenUserAlreadyHasDraftClaim() {
        OffsetDateTime now = OffsetDateTime.now();
        DraftStoreEntity duplicateDraft = draftClaim(
            UUID.randomUUID(),
            now,
            DRAFT_TYPE.calculateExpiry(now),
            "duplicate"
        );

        assertThatThrownBy(() -> draftStoreRepository.saveAndFlush(duplicateDraft))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldReturnDraftWhenDraftExists() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldReturnDraftWhenActiveDraftExists() throws Exception {
        doGet(BEARER_TOKEN, ACTIVE_DRAFT_CLAIM_URL)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldUpdatePayloadAndTimestampWhenDraftExists() throws Exception {
        DraftStoreEntity draftClaim = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        OffsetDateTime initialUpdatedAt = draftClaim.getUpdatedAt();

        Map<String, Object> updatedPayload = Map.of("step", "updated-step");
        DraftClaimRequest updatedRequest = new DraftClaimRequest("12345", updatedPayload);

        doDraftPut(BEARER_TOKEN, updatedRequest, draftId)
            .andExpectAll(
                status().isOk(),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("updated-step"));

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(updatedEntity.getPayload()).extracting("step").isEqualTo("updated-step");
        assertThat(updatedEntity.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    void shouldPreserveCreationAndExpiryTimestampsWhenDraftIsUpdated() throws Exception {
        DraftStoreEntity initialDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in db"));
        OffsetDateTime initialCreatedAt = initialDraft.getCreatedAt();
        OffsetDateTime initialExpiresAt = initialDraft.getExpiresAt();

        Map<String, Object> updatedPayload = Map.of("step", "updated-step");
        DraftClaimRequest updatedRequest = new DraftClaimRequest("123", updatedPayload);

        doDraftPut(BEARER_TOKEN, updatedRequest, draftId)
            .andExpect(status().isOk());

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(updatedEntity.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updatedEntity.getExpiresAt()).isEqualTo(initialExpiresAt);
    }

    @Test
    void shouldReturnNotFoundWhenDraftIsExpired() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(181);

        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            OffsetDateTime.now().minusDays(1),
            "expired-test"
        ));

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        doGet(BEARER_TOKEN, ACTIVE_DRAFT_CLAIM_URL)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingExpiredDraft() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(181);
        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            DRAFT_TYPE.calculateExpiry(expiredDate),
            "expired-test"
        ));

        DraftClaimRequest updateRequest = new DraftClaimRequest(
            "updated-case-id",
            Map.of("step", "updated-step")
        );

        doDraftPut(BEARER_TOKEN, updateRequest, draftId)
            .andExpect(status().isNotFound());

        assertThat(draftStoreRepository.count()).isOne();
        DraftStoreEntity expiredDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Expired draft claim should remain in DB"));
        assertThat(expiredDraft.getCaseId()).isEqualTo("1234");
        assertThat(expiredDraft.getPayload()).containsEntry("step", "expired-test");
    }

    @Test
    void shouldReplaceExpiredDraftWhenCreatingDraftClaim() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(181);
        draftStoreRepository.save(draftClaim(
            draftId,
            expiredDate,
            DRAFT_TYPE.calculateExpiry(expiredDate),
            "expired-test"
        ));

        MvcResult result = doPost(
            BEARER_TOKEN,
            new DraftClaimRequest("new-case-id", PAYLOAD),
            DRAFT_CLAIMS_URL
        )
            .andExpect(status().isCreated())
            .andReturn();

        UUID newDraftId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.draftId"));
        assertThat(newDraftId).isNotEqualTo(draftId);
        assertThat(draftStoreRepository.findById(draftId)).isEmpty();
        assertThat(draftStoreRepository.count()).isOne();

        DraftStoreEntity newDraft = draftStoreRepository.findById(newDraftId)
            .orElseThrow(() -> new AssertionError("New draft claim should exist in DB"));
        assertThat(newDraft.getExpiresAt()).isEqualTo(DRAFT_TYPE.calculateExpiry(newDraft.getCreatedAt()));
    }

    @Test
    void shouldReturnNotFoundWhenDifferentUserAccessesDraft() throws Exception {
        String bearerToken2 = "Bearer jgiofdjbinaiogokfabinnaojpefjeapb.user2";

        given(userService.getUserInfo(bearerToken2))
            .willReturn(UserInfo.builder().uid("2").build());

        doGet(bearerToken2, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftClaimRequest updateRequest = new DraftClaimRequest("1234", Map.of("step", "unauthorised-step"));
        doDraftPut(bearerToken2, updateRequest, draftId)
            .andExpect(status().isNotFound());

        doDelete(bearerToken2, null, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftStoreEntity draftClaim = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should still exist in DB"));

        assertThat(draftClaim.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void shouldDeleteDraftWhenOwnedByUser() throws Exception {
        assertThat(draftStoreRepository.findById(draftId)).isPresent();

        doDelete(BEARER_TOKEN, null, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNoContent());

        assertThat(draftStoreRepository.findById(draftId)).isEmpty();

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());
    }

    private ResultActions doDraftPut(String authorisation, DraftClaimRequest request, UUID draftClaimId)
        throws Exception {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(DRAFT_CLAIM_BY_ID_URL, draftClaimId)
                .header(HttpHeaders.AUTHORIZATION, authorisation)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );
    }

    private DraftStoreEntity draftClaim(UUID id,
                                        OffsetDateTime createdAt,
                                        OffsetDateTime expiresAt,
                                        String step) {
        return new DraftStoreEntity(
            id,
            DraftClaimControllerIntegrationTest.USER_ID,
            "1234",
            DRAFT_TYPE.getId(),
            new HashMap<>(Map.of("step", step)),
            createdAt,
            createdAt,
            expiresAt
        );
    }
}
