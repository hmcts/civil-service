package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import lombok.Getter;

@Getter
public enum DashboardScenarios {

    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT("Scenario.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT("Scenario.AAA6.GeneralApps.UrgentApplicationMade.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_UNCLOAKED_RESPONDENT("Scenario.AAA6.GeneralApps.NonUrgentApplicationUncloaked.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_UNCLOAKED_RESPONDENT("Scenario.AAA6.GeneralApps.UrgentApplicationUncloaked.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_DELETE_RESPONDENT("Scenario.AAA6.GeneralApps.ApplicationMade.Delete.Respondent"),
    SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT("Scenario.AAA6.GeneralApps.ApplicationFeeRequired.Applicant"),
    SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT("Scenario.AAA6.GeneralApps.OtherPartyUploadedDocuments.Applicant"),
    SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT("Scenario.AAA6.GeneralApps.OtherPartyUploadedDocuments.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_APPLICANT("Scenario.AAA6.GeneralApps.MoreInfoRequired.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT("Scenario.AAA6.GeneralApps.ApplicationUncloaked.OrderMade.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT("Scenario.AAA6.GeneralApps.ApplicationSubmitted.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT("Scenario.AAA6.GeneralApps.MoreInfoRequired.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT("Scenario.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT("Scenario.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_APPLICANT("Scenario.AAA6.GeneralApps.DeleteWrittenRepresentationRequired.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_DELETE_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT("Scenario.AAA6.GeneralApps.DeleteWrittenRepresentationRequired.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_SWITCH_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT_APPLICANT(
        "Scenario.AAA6.GeneralApps.SwitchWrittenRepresentationRequired.RespondentApplicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_APPLICANT("Scenario.AAA6.GeneralApps.HearingScheduled.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT("Scenario.AAA6.GeneralApps.HearingScheduled.Respondent"),
    SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT("Scenario.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT("Scenario.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT("Scenario.AAA6.GeneralApps.HwF.PartRemission.Applicant"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT("Scenario.AAA6.GeneralApps.HwF.InvalidRef.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_APPLICANT("Scenario.AAA6.GeneralApps.OrderMade.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT("Scenario.AAA6.GeneralApps.OrderMade.Respondent"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT("Scenario.AAA6.GeneralApps.HwFRejected.Applicant"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_REQUESTED_APPLICANT("Scenario.AAA6.GeneralApps.HwFRequested.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_ADDITIONAL_PAYMENT_APPLICANT("Scenario.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT("Scenario.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_FEE_PAID_APPLICANT("Scenario.AAA6.GeneralApps.HwF.FeePaid.Applicant"),
    SCENARIO_AAA6_GENERAL_APPS_HWF_FULL_REMISSION_APPLICANT("Scenario.AAA6.GeneralApps.HwF.FullRemission.Applicant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_CLAIMANT("Scenario.AAA6.GeneralApplication.ViewApplicationActionNeeded.Claimant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_CLAIMANT("Scenario.AAA6.GeneralApplication.ViewApplicationInProgress.Claimant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT("Scenario.AAA6.GeneralApplication.ViewApplicationAvailable.Claimant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_ACTION_NEEDED_DEFENDANT("Scenario.AAA6.GeneralApplication.ViewApplicationActionNeeded.Defendant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_IN_PROGRESS_DEFENDANT("Scenario.AAA6.GeneralApplication.ViewApplicationInProgress.Defendant"),
    SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT("Scenario.AAA6.GeneralApplication.ViewApplicationAvailable.Defendant"),
    SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_APPLICANT("Scenario.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant"),
    SCENARIO_AAA6_GENERAL_APPS_TRANSLATED_DOCUMENT_UPLOADED_RESPONDENT("Scenario.AAA6.GeneralApps.TranslatedDocumentUploaded.Respondent"),
    SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT("Scenario.AAA6.GeneralApps.ApplicationProceedsOffline.Applicant"),
    SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT("Scenario.AAA6.GeneralApps.ApplicationProceedsOffline.Respondent");

    private final String scenario;

    DashboardScenarios(String scenario) {
        this.scenario = scenario;
    }

    public String getScenario() {
        return scenario;
    }
}
