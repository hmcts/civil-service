package uk.gov.hmcts.reform.civil.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.enums.UserRole;
import uk.gov.hmcts.reform.civil.model.PartySpec;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.UserRole.CASE_WORKER;
import static uk.gov.hmcts.reform.civil.enums.UserRole.SOLICITOR;

@Component
public class CreateClaimSpec implements CCDConfig<CaseData, CaseState, UserRole> {

    @Override
    public void configure(ConfigBuilder<CaseData, CaseState, UserRole> configBuilder) {
        configBuilder
                .event("CREATE_CLAIM_SPEC")
                .initialState(PENDING_CASE_ISSUED)
                .name("Create claim - Specified")
                .description("Case created, post actions triggered")
                .showSummary()
                .endButtonLabel("Submit")
                .grant(CRU, SOLICITOR)
                .grantHistoryOnly(CASE_WORKER)
                .fields()
                    .page("References")
                    .label("solicitorReferences", "## Your File Reference")
                    .complex(CaseData::getSolicitorReferences)
                        .optionalWithLabel(SolicitorReferences::getApplicantSolicitor1Reference, "Claimant's legal representative's reference")
                        .optionalWithLabel(SolicitorReferences::getRespondentSolicitor1Reference, "Defendant's legal representative's reference")
                        .done()

                    .page("Claimant")
                    .label("claimantsDetails", "## Claimant's details")
                    .complex(CaseData::getApplicant1Spec)
                        .mandatoryWithLabel(PartySpec::getTypeSpec, "Claimant type")
                        .optionalWithoutDefaultValue(PartySpec::getIndividualTitleSpec, "type=\"individual\"", "Title")
                        .mandatoryWithoutDefaultValue(PartySpec::getIndividualFirstNameSpec, "type=\"individual\"", "First Name")
                        .mandatoryWithoutDefaultValue(PartySpec::getIndividualLastNameSpec, "type=\"individual\"", "Last Name")
                        .mandatoryWithoutDefaultValue(PartySpec::getIndividualDateOfBirthSpec, "type=\"individual\"", "Date of birth")
                        .mandatoryWithoutDefaultValue(PartySpec::getCompanyNameSpec, "type=\"company\"", "Company Name")
                        .mandatoryWithoutDefaultValue(PartySpec::getOrganisationNameSpec, "type=\"organisation\"", "Organisation Name")
                        .done()
                .done();
    }

    /*public AboutToStartOrSubmitResponse<CaseData, CaseState> aboutToSubmit(
            CaseDetails<CaseData, CaseState> details, CaseDetails<CaseData, CaseState> beforeDetails) {

        CaseData caseData = details.getData();
        details.setState(PENDING_CASE_ISSUED);

        return AboutToStartOrSubmitResponse.<CaseData, CaseState>builder()
                .data(details.getData())
                .state(details.getState())
                .build();
    }

    public SubmittedCallbackResponse submitted(
            final CaseDetails<CaseData, CaseState> details,
            final CaseDetails<CaseData, CaseState> beforeDetails) {

        return SubmittedCallbackResponse.builder().build();
    }*/
}
