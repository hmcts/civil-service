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
import uk.gov.hmcts.reform.dashboard.data.DraftGeneralApplicationCaseResponse;
import uk.gov.hmcts.reform.dashboard.data.DraftGeneralApplicationRequest;
import uk.gov.hmcts.reform.dashboard.data.DraftGeneralApplicationResponse;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationAlreadyExistsException;
import uk.gov.hmcts.reform.dashboard.exceptions.DraftGeneralApplicationNotFoundException;
import uk.gov.hmcts.reform.dashboard.services.DraftGeneralApplicationService;
import uk.gov.hmcts.reform.draftstore.entities.DraftStoreEntity;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/dashboard/draft-general-applications", produces = MediaType.APPLICATION_JSON_VALUE)
public class DraftGeneralApplicationController {

    private final DraftGeneralApplicationService draftGeneralApplicationService;
    private final UserService userService;

    public DraftGeneralApplicationController(DraftGeneralApplicationService draftGeneralApplicationService,
                                             UserService userService) {
        this.draftGeneralApplicationService = draftGeneralApplicationService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftGeneralApplicationResponse> create(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftGeneralApplicationRequest request
    ) {
        DraftStoreEntity draft = draftGeneralApplicationService.create(
            getUserId(authorisation),
            request.getCaseId(),
            request.getApplicationId(),
            request.getPayload()
        );
        return new ResponseEntity<>(DraftGeneralApplicationResponse.from(draft), HttpStatus.CREATED);
    }

    @GetMapping("/case/{case-id}")
    public ResponseEntity<DraftGeneralApplicationCaseResponse> getForCase(
        @PathVariable("case-id") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        String userId = getUserId(authorisation);
        DraftGeneralApplicationResponse application = draftGeneralApplicationService
            .getApplicationForCase(userId, caseId)
            .map(DraftGeneralApplicationResponse::from)
            .orElse(null);
        var responses = draftGeneralApplicationService.getResponsesForCase(userId, caseId).stream()
            .map(DraftGeneralApplicationResponse::from)
            .toList();
        return ResponseEntity.ok(new DraftGeneralApplicationCaseResponse(application, responses));
    }

    @GetMapping("/application/{application-id}")
    public ResponseEntity<DraftGeneralApplicationResponse> getForApplication(
        @PathVariable("application-id") String applicationId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draft = draftGeneralApplicationService
            .getResponseForApplication(getUserId(authorisation), applicationId)
            .orElseThrow(() -> new DraftGeneralApplicationNotFoundException(applicationId));
        return ResponseEntity.ok(DraftGeneralApplicationResponse.from(draft));
    }

    @GetMapping("/{draft-id}")
    public ResponseEntity<DraftGeneralApplicationResponse> get(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        DraftStoreEntity draft = draftGeneralApplicationService.get(draftId, getUserId(authorisation))
            .orElseThrow(() -> new DraftGeneralApplicationNotFoundException(draftId));
        return ResponseEntity.ok(DraftGeneralApplicationResponse.from(draft));
    }

    @PutMapping(path = "/{draft-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DraftGeneralApplicationResponse> update(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @Valid @RequestBody DraftGeneralApplicationRequest request
    ) {
        DraftStoreEntity draft = draftGeneralApplicationService.update(
            draftId,
            getUserId(authorisation),
            request.getCaseId(),
            request.getPayload()
        );
        return ResponseEntity.ok(DraftGeneralApplicationResponse.from(draft));
    }

    @DeleteMapping("/user-applications")
    public ResponseEntity<Void> deleteApplicationDraftsForUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftGeneralApplicationService.deleteApplicationDraftsForUser(getUserId(authorisation));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/case/{case-id}")
    public ResponseEntity<Void> deleteApplicationForCase(
        @PathVariable("case-id") String caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftGeneralApplicationService.deleteApplicationForCase(getUserId(authorisation), caseId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/application/{application-id}")
    public ResponseEntity<Void> deleteResponseForApplication(
        @PathVariable("application-id") String applicationId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftGeneralApplicationService.deleteResponseForApplication(getUserId(authorisation), applicationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{draft-id}")
    public ResponseEntity<Void> delete(
        @PathVariable("draft-id") UUID draftId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        draftGeneralApplicationService.delete(draftId, getUserId(authorisation));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DraftGeneralApplicationNotFoundException.class)
    public ResponseEntity<String> notFound(DraftGeneralApplicationNotFoundException exception) {
        log.info(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DraftGeneralApplicationAlreadyExistsException.class)
    public ResponseEntity<String> alreadyExists(DraftGeneralApplicationAlreadyExistsException exception) {
        log.info(exception.getMessage());
        return new ResponseEntity<>("An active general application draft already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException exception) {
        log.info(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private String getUserId(String authorisation) {
        return userService.getUserInfo(authorisation).getUid();
    }
}
