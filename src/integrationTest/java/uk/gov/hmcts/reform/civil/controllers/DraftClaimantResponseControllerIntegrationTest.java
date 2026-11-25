package uk.gov.hmcts.reform.civil.controllers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimantResponseRequest;
import uk.gov.hmcts.reform.draftstore.DraftType;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;
import uk.gov.hmcts.reform.draftstore.services.DraftStoreService;
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
public class DraftClaimantResponseControllerIntegrationTest extends BaseIntegrationTest {

    private static final String DRAFT_RESPONSES_URL = "/dashboard/draft-claimant-responses";
    private static final String DRAFT_RESPONSE_BY_ID_URL = "/dashboard/draft-claimant-responses/{draft-id}";
    private static final String DRAFT_RESPONSE_BY_CASE_URL = "/dashboard/draft-claimant-responses/case/{case-id}";
    private static final String USER_ID = "user1";
    private static final String CASE_ID = "1234567890123456";
    private static final String OTHER_CASE_ID = "1234567890123457";
    private static final DraftType DRAFT_TYPE = DraftType.DRAFT_CLAIMANT_RESPONSE;
    private static final long RETENTION_DAYS = 30L;
    private static final Map<String, Object> PAYLOAD = Map.of(
        "step", "intention",
        "draftClaimCacheTtlDays", RETENTION_DAYS
    );

    @Autowired
    private DraftStoreRepository draftStoreRepository;

    @BeforeEach
    void setUp() {
        draftStoreRepository.deleteAll();
        given(userService.getUserInfo(anyString()))
            .willReturn(UserInfo.builder().uid(USER_ID).sub("test@test.com").build());
    }

    @AfterEach
    void tearDown() {
        draftStoreRepository.deleteAll();
    }

    @Test
    void shouldCreateDraftWithThirtyDayRetention() throws Exception {
        MvcResult result = doPost(BEARER_TOKEN, new DraftClaimantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.caseId").value(CASE_ID))
            .andExpect(jsonPath("$.payload.step").value("intention"))
            .andReturn();

        UUID createdDraftId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.draftId"));
        DraftStoreEntity stored = draftStoreRepository.findById(createdDraftId).orElseThrow();
        assertThat(stored.getDraftType()).isEqualTo(DRAFT_TYPE);
        assertThat(stored.getUserId()).isEqualTo(USER_ID);
        assertThat(stored.getExpiresAt()).isEqualTo(
            DraftStoreService.calculateExpiresAt(stored.getCreatedAt(), RETENTION_DAYS)
        );
    }

    @Test
    void shouldReturnConflictWhenDraftAlreadyExistsForUserAndCase() throws Exception {
        doPost(BEARER_TOKEN, new DraftClaimantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated());

        doPost(BEARER_TOKEN, new DraftClaimantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isConflict());

        assertThat(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE)).hasSize(1);
    }

    @Test
    void shouldKeepDraftsForDifferentCasesSeparate() throws Exception {
        doPost(BEARER_TOKEN, new DraftClaimantResponseRequest(CASE_ID, PAYLOAD), DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated());
        doPost(BEARER_TOKEN, new DraftClaimantResponseRequest(OTHER_CASE_ID, otherPayload()), DRAFT_RESPONSES_URL)
            .andExpect(status().isCreated());

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payload.step").value("intention"));
        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, OTHER_CASE_ID)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payload.step").value("other-case"));

        assertThat(draftStoreRepository.findByUserIdAndDraftType(USER_ID, DRAFT_TYPE)).hasSize(2);
    }

    @Test
    void shouldReturnNotFoundWhenDraftIsMissing() throws Exception {
        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotReturnExpiredDraft() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(RETENTION_DAYS + 2);
        draftStoreRepository.save(storedDraft(
            UUID.randomUUID(),
            CASE_ID,
            createdAt,
            createdAt.plusDays(RETENTION_DAYS)
        ));

        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_CASE_URL, CASE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAndDeleteDraft() throws Exception {
        MvcResult created = doPost(
            BEARER_TOKEN,
            new DraftClaimantResponseRequest(CASE_ID, PAYLOAD),
            DRAFT_RESPONSES_URL
        ).andExpect(status().isCreated()).andReturn();
        UUID draftId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.draftId"));

        doDraftPut(new DraftClaimantResponseRequest(CASE_ID, Map.of(
            "step", "updated",
            "draftClaimCacheTtlDays", RETENTION_DAYS
        )), draftId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payload.step").value("updated"));

        doDelete(BEARER_TOKEN, null, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNoContent());

        assertThat(draftStoreRepository.findById(draftId)).isEmpty();
        doGet(BEARER_TOKEN, DRAFT_RESPONSE_BY_ID_URL, draftId)
            .andExpect(status().isNotFound());
    }

    private ResultActions doDraftPut(DraftClaimantResponseRequest request, UUID draftId) throws Exception {
        return mockMvc.perform(
            MockMvcRequestBuilders.put(DRAFT_RESPONSE_BY_ID_URL, draftId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        );
    }

    private static Map<String, Object> otherPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("step", "other-case");
        payload.put("draftClaimCacheTtlDays", RETENTION_DAYS);
        return payload;
    }

    private static DraftStoreEntity storedDraft(UUID id, String caseId, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        return new DraftStoreEntity(
            id,
            USER_ID,
            caseId,
            DRAFT_TYPE,
            new HashMap<>(PAYLOAD),
            createdAt,
            createdAt,
            expiresAt,
            null
        );
    }
}
