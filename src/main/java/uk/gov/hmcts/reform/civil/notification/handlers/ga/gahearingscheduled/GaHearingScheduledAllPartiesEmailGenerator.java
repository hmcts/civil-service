package uk.gov.hmcts.reform.civil.notification.handlers.ga.gahearingscheduled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.AllGaPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

/**
 * Applicant and respondents for the General Application hearing notice.
 *
 * <p>The two retired handlers each fetched the parent case and revalidated the solicitor emails,
 * so a scheduled hearing did that work twice. {@link AllGaPartiesEmailGenerator} does it once and
 * hands the result to both generators.</p>
 */
@Component
public class GaHearingScheduledAllPartiesEmailGenerator extends AllGaPartiesEmailGenerator {

    public GaHearingScheduledAllPartiesEmailGenerator(
        GaHearingScheduledApplicantEmailGenerator applicantEmailGenerator,
        GaHearingScheduledRespondentsEmailGenerator respondentsEmailGenerator,
        CaseDetailsConverter caseDetailsConverter,
        CoreCaseDataService coreCaseDataService,
        SolicitorEmailValidation solicitorEmailValidation) {
        super(List.of(applicantEmailGenerator, respondentsEmailGenerator),
              caseDetailsConverter, coreCaseDataService, solicitorEmailValidation);
    }
}
