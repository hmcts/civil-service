package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;

import java.util.Optional;

/**
 * Encapsulates disposal-hearing specific derived fields (location labels, attendance strings,
 * and duration text) so the docmosis template builder remains an orchestration layer only.
 */
@Service
public class DjDisposalTemplateFieldService {

    public String getCourtLocation(CaseData caseData) {
        return Optional.ofNullable(caseData.getDisposalHearingMethodInPersonDJ())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse(null);
    }

    public String getAttendanceLabel(DisposalHearingMethodDJ method) {
        if (method == null) {
            return null;
        }
        return switch (method) {
            case disposalHearingMethodTelephoneHearing -> "by telephone";
            case disposalHearingMethodInPerson -> "in person";
            case disposalHearingMethodVideoConferenceHearing -> "by video conference";
        };
    }

    public String getHearingDuration(CaseData caseData) {
        return Optional.ofNullable(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .map(DisposalHearingFinalDisposalHearingDJ::getTime)
            .map(this::mapDuration)
            .orElse(null);
    }

    private String mapDuration(DisposalHearingFinalDisposalHearingTimeEstimate estimate) {
        if (estimate == null) {
            return null;
        }
        return switch (estimate) {
            case FIFTEEN_MINUTES -> "15 minutes";
            case THIRTY_MINUTES -> "30 minutes";
            default -> null;
        };
    }
}
