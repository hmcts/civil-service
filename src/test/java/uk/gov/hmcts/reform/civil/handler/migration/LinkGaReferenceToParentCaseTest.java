package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.LinkGaCaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class LinkGaReferenceToParentCaseTest {

    private final CoreCaseDataService service = mock(CoreCaseDataService.class);
    private final CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
    private final LinkGaReferenceToParentCase task = new LinkGaReferenceToParentCase(service, converter);

    @Test
    void shouldAppendApplicationAndPreserveExistingEntries() {
        Element<GeneralApplicationsDetails> existing = element(new GeneralApplicationsDetails().setCaseLink(new CaseLink("111")));
        CaseData parent = CaseData.builder()
            .gaDetailsMasterCollection(List.of(existing)).build();

        CaseDetails details = CaseDetails.builder().build();
        StartEventResponse start = StartEventResponse.builder().caseDetails(details).build();
        when(service.startUpdate("222", UPDATE_CASE_DATA)).thenReturn(start);
        when(converter.toCaseData(details)).thenReturn(parent);
        CaseDataContent content = CaseDataContent.builder().event(Event.builder().build()).build();
        when(service.caseDataContentFromStartEventResponse(eq(start), anyMap())).thenAnswer(invocation -> {
            Map<String, List<Element<GeneralApplicationsDetails>>> updates = invocation.getArgument(1);
            List<Element<GeneralApplicationsDetails>> applications = updates.get("gaDetailsMasterCollection");
            assertThat(applications).hasSize(2);
            assertThat(applications.getFirst()).isSameAs(existing);
            assertThat(applications.getLast().getValue().getCaseLink().getCaseReference()).isEqualTo("333");
            assertThat(applications.getLast().getValue().getGeneralApplicationType()).isEqualTo("Stay the claim");
            return content;
        });
        GeneralApplicationCaseData ga = new GeneralApplicationCaseData();
        ga.setGeneralAppType(new GAApplicationType(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)));
        ga.setCcdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
        CaseData original = CaseData.builder().build();

        assertThat(task.migrateGeneralApplicationCaseData(original, ga, reference())).isSameAs(original);
        verify(service).submitUpdate("222", content);
        assertThat(parent.getGaDetailsMasterCollection()).containsExactly(existing);
    }

    @Test
    void shouldCreateMasterCollectionWhenMissingFromParentCase() {
        CaseData parent = CaseData.builder().build();
        CaseDetails details = CaseDetails.builder().build();
        StartEventResponse start = StartEventResponse.builder().caseDetails(details).build();
        when(service.startUpdate("222", UPDATE_CASE_DATA)).thenReturn(start);
        when(converter.toCaseData(details)).thenReturn(parent);

        GeneralApplicationCaseData ga = new GeneralApplicationCaseData();
        ga.setGeneralAppType(new GAApplicationType(List.of(GeneralApplicationTypes.STAY_THE_CLAIM)));
        ga.setCcdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
        GeneralApplicationsDetails expected = new GeneralApplicationsDetails()
            .setCaseLink(new CaseLink("333"))
            .setGeneralApplicationType("Stay the claim")
            .setCaseState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.getDisplayedValue());
        CaseDataContent content = CaseDataContent.builder().event(Event.builder().build()).build();
        when(service.caseDataContentFromStartEventResponse(eq(start), anyMap())).thenAnswer(invocation -> {
            Map<String, List<Element<GeneralApplicationsDetails>>> updates = invocation.getArgument(1);
            assertThat(updates.get("gaDetailsMasterCollection"))
                .extracting(Element::getValue).containsExactly(expected);
            return content;
        });

        assertThat(parent.getGaDetailsMasterCollection()).isNull();
        task.migrateGeneralApplicationCaseData(CaseData.builder().build(), ga, reference());

        verify(service).caseDataContentFromStartEventResponse(eq(start), anyMap());
        verify(service).submitUpdate("222", content);
    }

    @Test
    void shouldSkipApplicationAlreadyLinked() {
        CaseDetails details = CaseDetails.builder().build();
        StartEventResponse start = StartEventResponse.builder().caseDetails(details).build();
        when(service.startUpdate("222", UPDATE_CASE_DATA)).thenReturn(start);
        when(converter.toCaseData(details)).thenReturn(CaseData.builder().gaDetailsMasterCollection(List.of(
            element(new GeneralApplicationsDetails().setCaseLink(new CaseLink("333"))))).build());

        task.migrateGeneralApplicationCaseData(CaseData.builder().build(), null, reference());

        verify(service, never()).submitUpdate(eq("222"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectMissingParentReference() {
        LinkGaCaseReference reference = reference();
        reference.setParentCaseReference(null);
        CaseData caseData = CaseData.builder().build();
        assertThrows(IllegalArgumentException.class,
                     () -> task.migrateCaseData(caseData, reference));
    }

    private LinkGaCaseReference reference() {
        LinkGaCaseReference reference = new LinkGaCaseReference();
        reference.setCaseReference("333");
        reference.setParentCaseReference("222");
        return reference;
    }
}
