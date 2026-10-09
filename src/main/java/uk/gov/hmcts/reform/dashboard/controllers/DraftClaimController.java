package uk.gov.hmcts.reform.dashboard.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftClaimService;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/dashboard/draft-claims", produces = MediaType.APPLICATION_JSON_VALUE)
public class DraftClaimController {

    private final DraftClaimService draftClaimService;
    private final UserService userService;

    public DraftClaimController(DraftClaimService draftClaimService, UserService userService) {
        this.draftClaimService = draftClaimService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftClaimResponse> createDraftClaim(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftClaimRequest request
    ) {
        // New drafts are always blank; a draft is linked to a case by updating it after submission
        if (StringUtils.isNotBlank(request.getCaseId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "caseId cannot be set when creating a draft claim"
            );
        }
        DraftStoreEntity draftClaim = draftClaimService.createDraftClaim(
            getUserId(authorisation),
            request.getPayload()
        );
        return new ResponseEntity<>(DraftClaimResponse.from(draftClaim), HttpStatus.CREATED);
    }

    @GetMapping("/active")
    public ResponseEntity<DraftClaimResponse> getActiveDraftClaim(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftClaim = draftClaimService.getActiveDraftClaimForUser(getUserId(authorisation))
            .orElseThrow(DraftClaimNotFoundException::new);
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @GetMapping("/case/{case-id}")
    public ResponseEntity<DraftClaimResponse> getDraftClaimForCase(
        @PathVariable("case-id") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftClaim = draftClaimService.getDraftClaimForCase(getUserId(authorisation), caseId)
            .orElseThrow(() -> new DraftClaimNotFoundException(caseId));
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @GetMapping("/{draft-id}")
    public ResponseEntity<DraftClaimResponse> getDraftClaim(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftClaim = draftClaimService.getDraftClaim(draftId, getUserId(authorisation))
            .orElseThrow(() -> new DraftClaimNotFoundException(draftId));
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @PutMapping(path = "/{draft-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftClaimResponse> updateDraftClaim(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftClaimRequest request
    ) {
        DraftStoreEntity draftClaim = draftClaimService.updateDraftClaim(
            draftId,
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @PutMapping("/{draft-id}/payment-retention")
    public ResponseEntity<DraftClaimResponse> applyPaymentRetention(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftClaim = draftClaimService.applyPaymentRetention(
            draftId,
            getUserId(authorisation)
        );
        return ResponseEntity.ok(DraftClaimResponse.from(draftClaim));
    }

    @DeleteMapping("/{draft-id}")
    public ResponseEntity<Void> deleteDraftClaim(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftClaimService.deleteDraftClaim(draftId, getUserId(authorisation));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DraftClaimAlreadyExistsException.class)
    public ResponseEntity<String> draftClaimAlreadyExists(DraftClaimAlreadyExistsException exception) {
        log.info(exception.getMessage());
        return new ResponseEntity<>("An active draft claim already exists", HttpStatus.CONFLICT);
    }

    private String getUserId(String authorisation) {
        return userService.getUserInfo(authorisation).getUid();
    }
}
