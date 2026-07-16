package uk.gov.hmcts.reform.dashboard.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimResponse;
import uk.gov.hmcts.reform.dashboard.entities.DraftStoreEntity;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftStoreService;

import java.util.UUID;

@RestController
@RequestMapping(path = "/dashboard/draft-claims", produces = MediaType.APPLICATION_JSON_VALUE)
public class DraftClaimController {

    private final DraftStoreService draftStoreService;
    private final UserService userService;

    @Autowired
    public DraftClaimController(DraftStoreService draftStoreService, UserService userService) {
        this.draftStoreService = draftStoreService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftClaimResponse> createDraftClaim(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftClaimRequest request
    ) {
        DraftStoreEntity draftClaim = draftStoreService.createDraftClaim(
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return new ResponseEntity<>(DraftClaimResponse.from(draftClaim), HttpStatus.CREATED);
    }

    @GetMapping("/active")
    public ResponseEntity<DraftClaimResponse> getActiveDraftClaim(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftClaim = draftStoreService.getActiveDraftClaimForUser(getUserId(authorisation))
            .orElseThrow(DraftClaimNotFoundException::new);
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @GetMapping("/{draft-id}")
    public ResponseEntity<DraftClaimResponse> getDraftClaim(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftClaim = draftStoreService.getDraftClaim(draftId, getUserId(authorisation))
            .orElseThrow(() -> new DraftClaimNotFoundException(draftId));
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @PutMapping(path = "/{draft-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftClaimResponse> updateDraftClaim(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftClaimRequest request
    ) {
        DraftStoreEntity draftClaim = draftStoreService.updateDraftClaim(
            draftId,
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @DeleteMapping("/{draft-id}")
    public ResponseEntity<Void> deleteDraftClaim(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftStoreService.deleteDraftClaim(draftId, getUserId(authorisation));
        return ResponseEntity.noContent().build();
    }

    private String getUserId(String authorisation) {
        return userService.getUserInfo(authorisation).getUid();
    }
}
