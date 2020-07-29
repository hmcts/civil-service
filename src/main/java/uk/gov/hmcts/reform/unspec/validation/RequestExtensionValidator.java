package uk.gov.hmcts.reform.unspec.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.unspec.handler.callback.RequestExtensionCallbackHandler.PROPOSED_DEADLINE;
import static uk.gov.hmcts.reform.unspec.handler.callback.RequestExtensionCallbackHandler.RESPONSE_DEADLINE;

@Service
@RequiredArgsConstructor
public class RequestExtensionValidator {

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0);
    private final ObjectMapper mapper;

    public List<String> validateProposedDeadline(LocalDate dateToValidate,  LocalDateTime responseDeadline) {
        if (dateToValidate == null) {
            return ImmutableList.of("The proposed deadline must be provided");
        }

        if (!dateToValidate.isAfter(now())) {
            return ImmutableList.of("The proposed deadline must be a date in the future");
        }

        if (!dateToValidate.isAfter(responseDeadline.toLocalDate())) {
            return ImmutableList.of("The proposed deadline must be after the current deadline");
        }

        if (LocalDateTime.of(dateToValidate, END_OF_BUSINESS_DAY).isAfter(responseDeadline.plusDays(28))) {
            return ImmutableList.of("The proposed deadline cannot be more than 28 days after the current deadline");
        }

        return emptyList();
    }

    public List<String> validateProposedDeadline(CaseDetails caseDetails) {
        LocalDate proposedDeadline = mapper.convertValue(
            caseDetails.getData().get(PROPOSED_DEADLINE),
            LocalDate.class
        );

        LocalDateTime responseDeadline = mapper.convertValue(
            caseDetails.getData().get(RESPONSE_DEADLINE),
            LocalDateTime.class
        );

        return validateProposedDeadline(proposedDeadline, responseDeadline);
    }

    public List<String> validateAlreadyRequested(CaseDetails caseDetails) {
        if (isExtensionAlreadyRequested(caseDetails)) {
            return ImmutableList.of("You can only request an extension once");
        }
        return emptyList();
    }

    private boolean isExtensionAlreadyRequested(CaseDetails caseDetailsBefore) {
        return caseDetailsBefore.getData().get(PROPOSED_DEADLINE) != null;
    }
}
