package uk.gov.hmcts.reform.civil.controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimRequest;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DraftStoreRepository;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        draftClaim.setDraftTypeId(1);
        draftClaim.setPayload(new HashMap<>(Map.of("step", "active-test")));
        draftClaim.setDraftClaimCreatedAt(now);
        draftClaim.setCreatedAt(now);
        draftClaim.setUpdatedAt(now);
        draftClaim.setExpiresAt(now.plusDays(180));

        draftStoreRepository.save(draftClaim);
    }

    @AfterEach
    void tearDown() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldCreateDraftAndStorePayload() throws Exception {
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
    }

    @Test
    void shouldReturnDraftClaimByIdWhenExists() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void shouldReturnDraftClaimByIdThroughActiveEndpoint() throws Exception {
        doGet(BEARER_TOKEN, ACTIVE_DRAFT_CLAIM_URL)
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.draftId").value(draftId.toString()),
                jsonPath("$.payload.step").value("active-test")
            );
    }

    @Test
    void updatingDraftUpdatesPayloadAndUpdatedAtTime() throws Exception {
        DraftStoreEntity draftClaim = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));
        OffsetDateTime initialUpdatedAt = draftClaim.getUpdatedAt();

        Map<String, Object> updatedPayload = Map.of("step", "updated-step");
        DraftClaimRequest updatedRequest = new DraftClaimRequest("12345", updatedPayload);

        doPut(BEARER_TOKEN, updatedRequest, DRAFT_CLAIM_BY_ID_URL, draftId)
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
    void updatePreservesCreationAndExpiryTimestamps() throws Exception {
        DraftStoreEntity initialDraft = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in db"));
        OffsetDateTime initialDraftClaimCreatedAt = initialDraft.getDraftClaimCreatedAt();
        OffsetDateTime initialCreatedAt = initialDraft.getCreatedAt();
        OffsetDateTime initialExpiresAt = initialDraft.getExpiresAt();

        Map<String, Object> updatedPayload = Map.of("step", "updated-step");
        DraftClaimRequest updatedRequest = new DraftClaimRequest("123", updatedPayload);

        doPut(BEARER_TOKEN, updatedRequest, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isOk());

        DraftStoreEntity updatedEntity = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(updatedEntity.getDraftClaimCreatedAt()).isEqualTo(initialDraftClaimCreatedAt);
        assertThat(updatedEntity.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updatedEntity.getExpiresAt()).isEqualTo(initialExpiresAt);
    }

    @Test
    void expiryIsEqualTo180DaysAfterCreatedAtTime() {
        DraftStoreEntity draftInDB = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should exist in DB"));

        assertThat(draftInDB.getExpiresAt()).isEqualTo(draftInDB.getCreatedAt().plusDays(180));
    }

    @Test
    void expiredDraftsReturn404AndAreExcludedFromActiveLookup() throws Exception {
        draftStoreRepository.deleteById(draftId);
        OffsetDateTime expiredDate = OffsetDateTime.now().minusDays(181);

        DraftStoreEntity expiredDraft = new DraftStoreEntity();
        expiredDraft.setId(draftId);
        expiredDraft.setUserId(USER_ID);
        expiredDraft.setDraftTypeId(1);
        expiredDraft.setCaseId("1234");
        expiredDraft.setPayload(new HashMap<>(Map.of("step", "expired-test")));
        expiredDraft.setDraftClaimCreatedAt(expiredDate);
        expiredDraft.setCreatedAt(expiredDate);
        expiredDraft.setExpiresAt(OffsetDateTime.now().minusDays(1));
        expiredDraft.setUpdatedAt(expiredDate);

        draftStoreRepository.save(expiredDraft);

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        doGet(BEARER_TOKEN, ACTIVE_DRAFT_CLAIM_URL)
            .andExpect(status().isNotFound());
    }

    @Test
    void differentUserCannotRetrieveUpdateOrDeleteDraft() throws Exception {
        String bearerToken2 = "Bearer jgiofdjbinaiogokfabinnaojpefjeapb.user2";

        given(userService.getUserInfo(bearerToken2))
            .willReturn(UserInfo.builder().uid("2").build());

        doGet(bearerToken2, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftClaimRequest updateRequest = new DraftClaimRequest("1234", Map.of("step", "unauthorised-step"));
        doPut(bearerToken2, updateRequest, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        doDelete(bearerToken2, null, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());

        DraftStoreEntity draftClaim = draftStoreRepository.findById(draftId)
            .orElseThrow(() -> new AssertionError("Draft claim should still exist in DB"));

        assertThat(draftClaim.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void deleteRemovesDraftFromDataBaseAndReturns404IfCalled() throws Exception {
        assertThat(draftStoreRepository.findById(draftId)).isPresent();

        doDelete(BEARER_TOKEN, null, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNoContent());

        assertThat(draftStoreRepository.findById(draftId)).isEmpty();

        doGet(BEARER_TOKEN, DRAFT_CLAIM_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());
    }
}
