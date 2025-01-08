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
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataInvalidException;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.IncludesLitigantInPersonException;
import uk.gov.hmcts.reform.civil.exceptions.MissingFieldsUpdatedException;
import uk.gov.hmcts.reform.civil.exceptions.UserNotFoundOnCaseException;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;

import static uk.gov.hmcts.reform.civil.utils.ContentCachingRequestWrapperUtil.getCaseId;
import static uk.gov.hmcts.reform.civil.utils.ContentCachingRequestWrapperUtil.getUserId;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<Object> caseNotFoundBadRequest(CaseNotFoundException caseNotFoundException,
                                                         ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Case not found with message: %s for case %s run by user %s";
        log.error(errorMessage
                      .formatted(caseNotFoundException.getMessage(), getCaseId(contentCachingRequestWrapper),
                                 getUserId(contentCachingRequestWrapper)
                      )
        );
        return new ResponseEntity<>("Case was not found", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SearchServiceCaseNotFoundException.class)
    public ResponseEntity<Object> caseNotFoundUnauthorised(SearchServiceCaseNotFoundException searchServiceCaseNotFoundException,
                                                           ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Search service case not found with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(searchServiceCaseNotFoundException.getMessage(),
                                         getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>("UNAUTHORIZED", new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PinNotMatchException.class)
    public ResponseEntity<Object> pinNotMatchedUnauthorised(PinNotMatchException pinNotMatchException,
                                                            ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Pin not matched unauthorized error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(pinNotMatchException.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>("BAD_REQUEST", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DocumentUploadException.class)
    public ResponseEntity<Object> documentUploadException(DocumentUploadException documentUploadException,
                                                          ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Document upload error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(documentUploadException.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>("Document upload unsuccessful", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> documentUploadException(MaxUploadSizeExceededException maxUploadSizeExceededException,
                                                          ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Max upload size exceeded error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(maxUploadSizeExceededException.getMessage(),
                                         getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>("Document upload unsuccessful", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingFieldsUpdatedException.class)
    public ResponseEntity<Object> partyIdsUpdatedException(MissingFieldsUpdatedException missingFieldsUpdatedException,
                                                           ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Error when attempting to update missing hearing values fields with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(missingFieldsUpdatedException.getMessage(),
                                         getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>("Missing fields updated", new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CaseDataInvalidException.class)
    public ResponseEntity<Object> caseDataInvalidException(CaseDataInvalidException caseDataInvalidException,
                                                           ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Case data is invalid error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(caseDataInvalidException.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>(
            "Submit claim unsuccessful, Invalid Case data",
            new HttpHeaders(),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(UserNotFoundOnCaseException.class)
    public ResponseEntity<Object> userNotFoundOnCaseException(UserNotFoundOnCaseException userNotFoundOnCaseException,
                                                              ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "User not found error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(userNotFoundOnCaseException.getMessage(),
                                         getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>(userNotFoundOnCaseException.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncludesLitigantInPersonException.class)
    public ResponseEntity<Object> litigantInPersonException(IncludesLitigantInPersonException includesLitigantInPersonException,
                                                            ContentCachingRequestWrapper contentCachingRequestWrapper) {
        String errorMessage = "Action not accepted on case with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(includesLitigantInPersonException.getMessage(),
                                         getCaseId(contentCachingRequestWrapper),
                                         getUserId(contentCachingRequestWrapper)
        ));
        return new ResponseEntity<>(
            includesLitigantInPersonException.getMessage(),
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
