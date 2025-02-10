package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.HearingScheduledDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HearingScheduledDefendantScenarioTest extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private HearingScheduledDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_hearing_scheduled_scenario() throws Exception {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Name").courtAddress("Loc").postcode("1").build());
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locations);

        DynamicListElement location = DynamicListElement.builder().label("Name - Loc - 1").build();
        DynamicList list = DynamicList.builder().value(location).listItems(List.of(location)).build();
        String caseId = "8123456783";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingDueDate(LocalDate.of(2024, 4, 1))
            .hearingDate(LocalDate.of(2024, 4, 1))
            .respondent1Represented(YesOrNo.NO)
            .hearingLocation(list).build();

        CallbackParams callbackParams = callbackParams(caseData);
        callbackParams = callbackParams.toBuilder().request(CallbackRequest.builder()
                                                                .eventId("CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT")
                                                                .build()).build();

        // When
        handler.handle(callbackParams);

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[1].titleEn").value("A hearing has been scheduled"),
                jsonPath("$[1].descriptionEn").value(
                    "<p class=\"govuk-body\">Your hearing has been scheduled for 1 April 2024 at "
                        + "Name. Please keep your contact details and anyone you wish to rely on in court up" +
                        " to date. You can update contact details by telephoning the court at 0300 123 7050.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" " +
                        "target=\"_blank\" class=\"govuk-link\">View the hearing notice</a></p>"),
                jsonPath("$[1].titleCy").value("Mae gwrandawiad wedi'i drefnu"),
                jsonPath("$[1].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae eich gwrandawiad wedi'i drefnu ar gyfer 1 Ebrill 2024 yn "
                        + "Name. Cadwch eich manylion cyswllt chi a manylion cyswllt unrhyw un yr hoffech ddibynnu arnynt yn y llys yn gyfredol." +
                        " Gallwch ddiweddaru manylion cyswllt drwy ffonio'r llys ar 0300 303 5174.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" " +
                        "target=\"_blank\" class=\"govuk-link\">Gweld yr hysbysiad o wrandawiad</a></p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the hearing</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld y gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName()),
                jsonPath("$[3].reference").value(caseId.toString()),
                jsonPath("$[3].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[3].taskNameCy").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld gorchmynion a rhybuddion</a>"),
                jsonPath("$[3].currentStatusCy").value(TaskStatus.AVAILABLE.getWelshName())

            );
    }
}
