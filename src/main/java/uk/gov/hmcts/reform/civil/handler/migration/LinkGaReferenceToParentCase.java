package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.LinkGaCaseReference;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Component
public class LinkGaReferenceToParentCase extends MigrationTask<LinkGaCaseReference> {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public LinkGaReferenceToParentCase(CoreCaseDataService coreCaseDataService,
                                      CaseDetailsConverter caseDetailsConverter) {
        super(LinkGaCaseReference.class);
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected String getEventSummary() {
        return "Link Ga reference to Parent Case";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, LinkGaCaseReference caseReference) {
        validate(caseData, caseReference);
        GeneralApplicationCaseData gaCaseData = caseDetailsConverter.toGeneralApplicationCaseData(
            coreCaseDataService.getCase(Long.valueOf(caseReference.getCaseReference())));
        return migrateGeneralApplicationCaseData(caseData, gaCaseData, caseReference);
    }

    @Override
    protected CaseData migrateGeneralApplicationCaseData(CaseData caseData,
                                                        GeneralApplicationCaseData gaCaseData,
                                                        LinkGaCaseReference caseReference) {
        validate(caseData, caseReference);
        StartEventResponse startEvent = coreCaseDataService.startUpdate(caseReference.getParentCaseReference(), UPDATE_CASE_DATA);
        CaseData parentCase = caseDetailsConverter.toCaseData(startEvent.getCaseDetails());

        List<Element<GeneralApplicationsDetails>> applications = parentCase.getGaDetailsMasterCollection() == null
            ? new ArrayList<>() : new ArrayList<>(parentCase.getGaDetailsMasterCollection());

        boolean alreadyLinked = applications.stream().anyMatch(application ->
            application.getValue().getCaseLink() != null
                && caseReference.getCaseReference().equals(application.getValue().getCaseLink().getCaseReference()));
        if (alreadyLinked) {
            return caseData;
        }

        applications.add(element(new GeneralApplicationsDetails()
            .setCaseLink(new CaseLink(caseReference.getCaseReference()))
            .setGeneralApplicationType(gaCaseData.getGeneralAppType().getTypes().stream()
                .map(GeneralApplicationTypes::getDisplayedValue).collect(Collectors.joining(", ")))
            .setGeneralAppSubmittedDateGAspec(gaCaseData.getGeneralAppSubmittedDateGAspec())
            .setCaseState(gaCaseData.getCcdState().getDisplayedValue())
            .setParentClaimantIsApplicant(gaCaseData.getParentClaimantIsApplicant())));

        CaseDataContent content = coreCaseDataService.caseDataContentFromStartEventResponse(
            startEvent, Map.of("gaDetailsMasterCollection", applications));
        content.getEvent().setSummary(getEventSummary());
        content.getEvent().setDescription(getEventDescription());

        coreCaseDataService.submitUpdate(caseReference.getParentCaseReference(), content);

        return caseData;
    }

    private void validate(CaseData caseData, LinkGaCaseReference caseReference) {
        if (caseData == null || caseReference == null || caseReference.getCaseReference() == null
            || caseReference.getCaseReference().isBlank() || caseReference.getParentCaseReference() == null
            || caseReference.getParentCaseReference().isBlank()) {
            throw new IllegalArgumentException("CaseData, GA case reference and parent case reference must be provided");
        }
    }

    @Override
    protected String getEventDescription() {
        return "Link Ga reference to Parent Case";
    }

    @Override
    protected String getTaskName() {
        return "LinkGaReferenceToParentCase";
    }
}
