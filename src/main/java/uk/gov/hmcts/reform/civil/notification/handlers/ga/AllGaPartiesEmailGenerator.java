package uk.gov.hmcts.reform.civil.notification.handlers.ga;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The General Application counterpart of
 * {@link uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator}.
 *
 * <p>It owns the preamble every GA notification service currently repeats: fetch the parent civil
 * case, then validate the solicitor emails against it. Doing it once here rather than once per
 * generator is the point, because a generator per recipient would otherwise mean a CCD fetch per
 * recipient.</p>
 *
 * <p>Fan out over a collection of recipients belongs in the generator, through
 * {@link EmailDTOGenerator#buildEmailDTOs}, not here. General Application respondents are a list
 * rather than the fixed respondent1 and respondent2 slots civil has, so this class deliberately
 * knows nothing about GA party shapes.</p>
 */
@Slf4j
public class AllGaPartiesEmailGenerator implements PartiesEmailGenerator<GeneralApplicationCaseData> {

    private final List<? extends EmailDTOGenerator<GaNotificationContext>> generators;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final SolicitorEmailValidation solicitorEmailValidation;

    public AllGaPartiesEmailGenerator(List<? extends EmailDTOGenerator<GaNotificationContext>> generators,
                                      CaseDetailsConverter caseDetailsConverter,
                                      CoreCaseDataService coreCaseDataService,
                                      SolicitorEmailValidation solicitorEmailValidation) {
        this.generators = generators;
        this.caseDetailsConverter = caseDetailsConverter;
        this.coreCaseDataService = coreCaseDataService;
        this.solicitorEmailValidation = solicitorEmailValidation;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(GeneralApplicationCaseData caseData, String taskId) {
        GaNotificationContext context = buildContext(caseData);

        Set<EmailDTO> partiesToEmail = new HashSet<>();
        for (EmailDTOGenerator<GaNotificationContext> generator : generators) {
            if ((generator != null) && generator.getShouldNotify(context)) {
                log.info("Generating email for party [{}] for case ID: {}",
                         generator.getClass().getSimpleName(), context.getCcdCaseReference());
                partiesToEmail.addAll(generator.buildEmailDTOs(context, taskId));
            }
        }
        return partiesToEmail;
    }

    private GaNotificationContext buildContext(GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData mainCaseData = caseDetailsConverter.toGeneralApplicationCaseData(
            coreCaseDataService.getCase(
                Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));
        // the GA case carries solicitor emails that can be stale, so they are patched from the
        // parent case before any generator reads them. This mirrors what the GA notification
        // services do today.
        return new GaNotificationContext(
            solicitorEmailValidation.validateSolicitorEmail(mainCaseData, caseData), mainCaseData);
    }
}
