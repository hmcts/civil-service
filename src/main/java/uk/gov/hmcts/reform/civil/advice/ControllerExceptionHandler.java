package uk.gov.hmcts.reform.civil.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.exceptions.*;
import uk.gov.hmcts.reform.civil.request.RequestData;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private final RequestData requestData;

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<Object> caseNotFoundBadRequest(CaseNotFoundException caseNotFoundException) {
        String errorMessage = "Case not found with message: %s for case %s run by user %s";
        log.error(errorMessage
                      .formatted(caseNotFoundException.getMessage(), requestData.caseId(), requestData.userId())
        );
        return new ResponseEntity<>("Case was not found", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SearchServiceCaseNotFoundException.class)
    public ResponseEntity<Object> caseNotFoundUnauthorised(SearchServiceCaseNotFoundException searchServiceCaseNotFoundException) {
        String errorMessage = "Search service case not found with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(searchServiceCaseNotFoundException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>("UNAUTHORIZED", new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PinNotMatchException.class)
    public ResponseEntity<Object> pinNotMatchedUnauthorised(PinNotMatchException pinNotMatchException) {
        String errorMessage = "Pin not matched unauthorized error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(pinNotMatchException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>("BAD_REQUEST", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DocumentUploadException.class)
    public ResponseEntity<Object> documentUploadException(DocumentUploadException documentUploadException) {
        String errorMessage = "Document upload error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(documentUploadException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>("Document upload unsuccessful", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> documentUploadException(MaxUploadSizeExceededException maxUploadSizeExceededException) {
        String errorMessage = "Max upload size exceeded error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(maxUploadSizeExceededException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>("Document upload unsuccessful", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingFieldsUpdatedException.class)
    public ResponseEntity<Object> partyIdsUpdatedException(MissingFieldsUpdatedException missingFieldsUpdatedException) {
        String errorMessage = "Error when attempting to update missing hearing values fields with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(missingFieldsUpdatedException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>("Missing fields updated", new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CaseDataInvalidException.class)
    public ResponseEntity<Object> caseDataInvalidException(CaseDataInvalidException caseDataInvalidException) {
        String errorMessage = "Case data is invalid error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(caseDataInvalidException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>("Submit claim unsuccessful, Invalid Case data", new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(UserNotFoundOnCaseException.class)
    public ResponseEntity<Object> userNotFoundOnCaseException(UserNotFoundOnCaseException userNotFoundOnCaseException) {
        String errorMessage = "User not found error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(userNotFoundOnCaseException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>(userNotFoundOnCaseException.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncludesLitigantInPersonException.class)
    public ResponseEntity<Object> userNotFoundOnCaseException(IncludesLitigantInPersonException includesLitigantInPersonException) {
        String errorMessage = "Action not accepted on case with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(includesLitigantInPersonException.getMessage(), requestData.caseId(), requestData.userId()));
        return new ResponseEntity<>(includesLitigantInPersonException.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }
}
