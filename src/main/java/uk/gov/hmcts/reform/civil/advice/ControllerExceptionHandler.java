package uk.gov.hmcts.reform.civil.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.service.search.exceptions.SearchServiceCaseNotFoundException;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<Object> caseNotFoundBadRequest(CaseNotFoundException caseNotFoundException) {
        log.error(caseNotFoundException.getMessage());
        return new ResponseEntity<>("Case was not found", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SearchServiceCaseNotFoundException.class)
    public ResponseEntity<Object> caseNotFoundUnauthorised(SearchServiceCaseNotFoundException searchServiceCaseNotFoundException) {
        log.error(searchServiceCaseNotFoundException.getMessage());
        return new ResponseEntity<>("UNAUTHORIZED", new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PinNotMatchException.class)
    public ResponseEntity<Object> pinNotMatchedUnauthorised(PinNotMatchException pinNotMatchException) {
        log.error(pinNotMatchException.getMessage());
        return new ResponseEntity<>("BAD_REQUEST", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
