package uk.gov.hmcts.reform.dashboard.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimantResponseRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftClaimantResponseResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftClaimantResponseNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftClaimantResponseService;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/dashboard/draft-claimant-responses", produces = MediaType.APPLICATION_JSON_VALUE)
public class DraftClaimantResponseController {

    private final DraftClaimantResponseService draftResponseService;
    private final UserService userService;

    public DraftClaimantResponseController(DraftClaimantResponseService draftResponseService,
                                           UserService userService) {
        this.draftResponseService = draftResponseService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftClaimantResponseResponse> createDraftResponse(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftClaimantResponseRequest request
    ) {
        DraftStoreEntity draftResponse = draftResponseService.createDraftResponse(
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return new ResponseEntity<>(DraftClaimantResponseResponse.from(draftResponse), HttpStatus.CREATED);
    }

    @GetMapping("/case/{case-id}")
    public ResponseEntity<DraftClaimantResponseResponse> getDraftResponseForCase(
        @PathVariable("case-id") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftResponse = draftResponseService.getDraftResponseForCase(getUserId(authorisation), caseId)
            .orElseThrow(() -> new DraftClaimantResponseNotFoundException(caseId));
        return ResponseEntity.ok(DraftClaimantResponseResponse.from(draftResponse));
    }

    @GetMapping("/{draft-id}")
    public ResponseEntity<DraftClaimantResponseResponse> getDraftResponse(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftResponse = draftResponseService.getDraftResponse(draftId, getUserId(authorisation))
            .orElseThrow(() -> new DraftClaimantResponseNotFoundException(draftId));
        return ResponseEntity.ok(DraftClaimantResponseResponse.from(draftResponse));
    }

    @PutMapping(path = "/{draft-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftClaimantResponseResponse> updateDraftResponse(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftClaimantResponseRequest request
    ) {
        DraftStoreEntity draftResponse = draftResponseService.updateDraftResponse(
            draftId,
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return ResponseEntity.ok(DraftClaimantResponseResponse.from(draftResponse));
    }

    @DeleteMapping("/{draft-id}")
    public ResponseEntity<Void> deleteDraftResponse(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftResponseService.deleteDraftResponse(draftId, getUserId(authorisation));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DraftClaimantResponseAlreadyExistsException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<String> draftClaimantResponseAlreadyExists(
        DraftClaimantResponseAlreadyExistsException exception
    ) {
        log.info(exception.getMessage());
        return new ResponseEntity<>("An active draft claimant response already exists", HttpStatus.CONFLICT);
    }

    private String getUserId(String authorisation) {
        return userService.getUserInfo(authorisation).getUid();
    }
}
