package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HearingScheduledClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;

public class HearingScheduledClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HearingScheduledClaimantNotificationHandler handler;

    @Test
    void should_create_hearing_scheduled_scenario() throws Exception {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Name").courtAddress("Loc").postcode("1").build());
        when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);
        when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);

        DynamicListElement location = DynamicListElement.builder().label("Name - Loc - 1").build();
        DynamicList list = DynamicList.builder().value(location).listItems(List.of(location)).build();
        String caseId = "8123456781";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingDueDate(LocalDate.of(2024, 4, 1))
            .hearingDate(LocalDate.of(2024, 4, 1))
            .hearingLocation(list).build();

        // When
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A hearing has been scheduled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your hearing has been scheduled for 1 April 2024 at "
                        + "Name. Please keep your contact details and anyone you wish to rely on in court up" +
                        " to date. You can update contact details by telephoning the court at 0300 123 7050." +
                        " <a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">View the hearing notice</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae gwrandawiad wedi'i drefnu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae eich gwrandawiad wedi'i drefnu ar gyfer 1 Ebrill 2024 yn Name. Cadwch eich manylion cyswllt chi a manylion cyswllt unrhyw un yr hoffech ddibynnu arnynt yn y llys yn gyfredol. Gallwch ddiweddaru manylion cyswllt drwy ffonio'r llys ar 0300 303 5174. <a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">Gweld yr hysbysiad o wrandawiad</a>.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the hearing</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the hearing</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].reference").value(caseId),
                jsonPath("$[1].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].taskNameCy").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.AVAILABLE.getName())

            );
    }

    @Test
    void should_create_hearing_fee_required_scenario() throws Exception {

        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Name").courtAddress("Loc").postcode("1").build());
        when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);
        when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);

        DynamicListElement location = DynamicListElement.builder().label("Name - Loc - 1").build();
        DynamicList list = DynamicList.builder().value(location).listItems(List.of(location)).build();
        String caseId = "503206541654";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingDate(LocalDate.of(2024, 4, 1))
            .hearingDueDate(LocalDate.of(2024, 4, 1))
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(20000)).build())
            .ccdState(HEARING_READINESS)
            .listingOrRelisting(LISTING)
            .hearingLocation(list).build();

        // When
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),

                jsonPath("$[0].titleEn").value("You must pay the hearing fee"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You must either <a href=\"{PAY_HEARING_FEE_URL_REDIRECT}\" class=\"govuk-link\">pay the hearing fee</a> of £200 "
                        + "or <a href=\"{APPLY_HELP_WITH_FEES_START}\" class=\"govuk-link\"> apply for help with fees</a>. " +
                        "You must do this by 1 April 2024. If you do not take one of these actions, your claim will be struck out."),
                jsonPath("$[0].titleCy").value("Rhaid i chi dalu ffi'r gwrandawiad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rhaid i chi naill ai <a href=\"{PAY_HEARING_FEE_URL_REDIRECT}\" class=\"govuk-link\">dalu ffi'r gwrandawiad</a> o £200 neu <a href=\"{APPLY_HELP_WITH_FEES_START}\" class=\"govuk-link\">wneud cais am help i dalu ffioedd</a>. Mae'n rhaid i chi wneud hyn erbyn 1 Ebrill 2024. Os na fyddwch yn cymryd un o'r camau hyn, bydd eich hawliad yn cael ei ddileu.")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[1].reference").value(caseId),
                jsonPath("$[1].taskNameEn").value(
                    "<a href={PAY_HEARING_FEE} class=\"govuk-link\">Pay the hearing fee</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.ACTION_NEEDED.getName()),
                jsonPath("$[1].taskNameCy").value(
                    "<a href={PAY_HEARING_FEE} class=\"govuk-link\">Pay the hearing fee</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.ACTION_NEEDED.getName()),
                jsonPath("$[1].hintTextEn").value("Deadline is 12am on 1 April 2024"),
                jsonPath("$[1].hintTextCy").value("Deadline is 12am on 1 Ebrill 2024")
            );
    }
}
