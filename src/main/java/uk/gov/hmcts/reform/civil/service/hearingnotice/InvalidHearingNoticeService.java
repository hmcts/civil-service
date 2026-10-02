package uk.gov.hmcts.reform.civil.service.hearingnotice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.model.InvalidHearingNoticeProcessed;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class InvalidHearingNoticeService {

    private static final String FIELD = "invalidHearingNoticeProcessed";
    private final CoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;

    public boolean hasProcessed(CaseDetails caseDetails, InvalidHearingNoticeProcessed response) {
        if (response.getHearingId() == null || response.getHearingId().isBlank()
            || response.getRequestVersion() == null || response.getResponseReceivedDateTime() == null) {
            return false;
        }
        return records(caseDetails).stream().filter(Objects::nonNull)
            .map(Element::getValue).anyMatch(response::equals);
    }

    public void recordAndTriggerEvent(String caseId, InvalidHearingNoticeProcessed response, String description) {
        // A conflict must re-read and merge the latest collection, not resubmit a stale payload.
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                var start = coreCaseDataService.startUpdate(caseId, INVALID_HEARING_NOTICE);
                if (hasProcessed(start.getCaseDetails(), response)) {
                    return;
                }
                var records = new ArrayList<>(records(start.getCaseDetails()));
                records.add(element(response));
                var content = coreCaseDataService.caseDataContentFromStartEventResponse(
                    start, Map.of(FIELD, records));
                content.getEvent().setSummary("Invalid hearing notice");
                content.getEvent().setDescription(description);
                // The record and the event are committed together; this does not assert WA delivery.
                coreCaseDataService.submitUpdate(caseId, content);
                return;
            } catch (FeignException.Conflict conflict) {
                if (attempt == 2) {
                    throw conflict;
                }
            }
        }
    }

    private List<Element<InvalidHearingNoticeProcessed>> records(CaseDetails caseDetails) {
        Object value = caseDetails.getData() == null ? null : caseDetails.getData().get(FIELD);
        return value == null ? List.of() : mapper.convertValue(value, new TypeReference<>() {});
    }
}
