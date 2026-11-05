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
import uk.gov.hmcts.reform.dashboard.data.DraftDefendantResponseRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftDefendantResponseResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftDefendantResponseNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftDefendantResponseService;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/dashboard/draft-responses", produces = MediaType.APPLICATION_JSON_VALUE)
public class DraftDefendantResponseController {

    private final DraftDefendantResponseService draftResponseService;
    private final UserService userService;

    public DraftDefendantResponseController(DraftDefendantResponseService draftResponseService, UserService userService) {
        this.draftResponseService = draftResponseService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftDefendantResponseResponse> createDraftResponse(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftDefendantResponseRequest request
    ) {
        DraftStoreEntity draftResponse = draftResponseService.createDraftResponse(
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return new ResponseEntity<>(DraftDefendantResponseResponse.from(draftResponse), HttpStatus.CREATED);
    }

    @GetMapping("/case/{case-id}")
    public ResponseEntity<DraftDefendantResponseResponse> getDraftResponseForCase(
        @PathVariable("case-id") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftResponse = draftResponseService.getDraftResponseForCase(getUserId(authorisation), caseId)
            .orElseThrow(() -> new DraftDefendantResponseNotFoundException(caseId));
        return ResponseEntity.ok(DraftDefendantResponseResponse.from(draftResponse));
    }

    @GetMapping("/{draft-id}")
    public ResponseEntity<DraftDefendantResponseResponse> getDraftResponse(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draftResponse = draftResponseService.getDraftResponse(draftId, getUserId(authorisation))
            .orElseThrow(() -> new DraftDefendantResponseNotFoundException(draftId));
        return ResponseEntity.ok(DraftDefendantResponseResponse.from(draftResponse));
    }

    @PutMapping(path = "/{draft-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftDefendantResponseResponse> updateDraftResponse(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftDefendantResponseRequest request
    ) {
        DraftStoreEntity draftResponse = draftResponseService.updateResponseDraft(
            draftId,
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return ResponseEntity.ok(DraftDefendantResponseResponse.from(draftResponse));
    }

    @DeleteMapping("/{draft-id}")
    public ResponseEntity<Void> deleteDraftResponse(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftResponseService.deleteDraftResponse(draftId, getUserId(authorisation));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DraftDefendantResponseAlreadyExistsException.class)
    @SuppressWarnings("unused") // invoked by Spring when a duplicate draft response is created
    public ResponseEntity<String> draftDefendantResponseAlreadyExists(DraftDefendantResponseAlreadyExistsException exception) {
        log.info(exception.getMessage());
        return new ResponseEntity<>("An active draft response already exists", HttpStatus.CONFLICT);
    }

    private String getUserId(String authorisation) {
        return userService.getUserInfo(authorisation).getUid();
    }
}
