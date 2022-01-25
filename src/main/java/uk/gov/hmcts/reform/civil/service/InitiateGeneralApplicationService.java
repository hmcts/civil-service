package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationService {

    public CaseData buildCaseData(CaseData.CaseDataBuilder dataBuilder, CaseData caseData, UserInfo userInfo) {
        List<Element<GeneralApplication>> applications = addApplication(buildApplication(caseData, userInfo),
                                                                        caseData.getGeneralApplications());
        return dataBuilder
            .generalApplications(applications)
            .generalAppType(GAApplicationType.builder().build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .generalAppDetailsOfOrder(EMPTY)
            .generalAppReasonsOfOrder(EMPTY)
            .generalAppInformOtherParty(GAInformOtherParty.builder().build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder().build())
            .generalAppEvidenceDocument(java.util.Collections.emptyList())
            .build();
    }

    private IdamUserDetails getApplicant1SolicitorUserDetails(CaseData caseData, UserInfo userInfo) {
        IdamUserDetails applicantDetails = IdamUserDetails.builder().build();

        if (caseData.getApplicantSolicitor1UserDetails() != null) {
            if (isApplicantSolicitorEmailExits(caseData.getApplicantSolicitor1UserDetails(), userInfo)) {
                return caseData.getApplicantSolicitor1UserDetails();
            } else {
                applicantDetails.toBuilder().email(userInfo.getGivenName())
                    .id(userInfo.getUid()).build();
                return applicantDetails;
            }
        }
        return caseData.getApplicantSolicitor1UserDetails();
        // throw new IllegalArgumentException("ApplicantSolicitor1UserDetails::emailData cannot be null");
    }

    private OrganisationPolicy getApplicant1OrganisationPolicy(CaseData caseData, UserInfo userInfo) {

        if (caseData.getApplicant1OrganisationPolicy() != null
            && caseData.getApplicant1OrganisationPolicy() != null) {
            if (isApplicantSolicitorEmailExits(caseData.getApplicantSolicitor1UserDetails(), userInfo)) {
                return caseData.getApplicant1OrganisationPolicy();
            } else {
                return caseData.getRespondent1OrganisationPolicy();
            }
        }
        return caseData.getApplicant1OrganisationPolicy();
        // throw new IllegalArgumentException("OrganisationPolicy and Applicant1SolicitorUserDetails are required");
    }

    private String getRespondent1SolicitorEmail(CaseData caseData, UserInfo userInfo) {

        if (caseData.getRespondentSolicitor1EmailAddress() != null) {
            return caseData.getRespondentSolicitor1EmailAddress();
        } else {
            return userInfo.getGivenName();
        }
        // throw new IllegalArgumentException("RespondentSolicitor1EmailAddress cannot be null");
    }

    private boolean isApplicantSolicitorEmailExits(IdamUserDetails applicantSolicitor, UserInfo userInfo) {
        return StringUtils.isNotBlank(applicantSolicitor.getEmail())
            && userInfo.getGivenName().equals(applicantSolicitor.getEmail());
    }

    private GeneralApplication buildApplication(CaseData caseData, UserInfo userInfo) {
        GeneralApplication.GeneralApplicationBuilder applicationBuilder = GeneralApplication.builder();
        if (caseData.getGeneralAppEvidenceDocument() != null) {
            applicationBuilder.generalAppEvidenceDocument(caseData.getGeneralAppEvidenceDocument());
        }
        if (MultiPartyScenario.isMultiPartyScenario(caseData)) {
            applicationBuilder.isMultiParty(YES);
        } else {
            applicationBuilder.isMultiParty(NO);
        }

        return applicationBuilder
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .applicantSolicitor1UserDetails(getApplicant1SolicitorUserDetails(caseData, userInfo))
            .applicant1OrganisationPolicy(getApplicant1OrganisationPolicy(caseData, userInfo))
            .respondent1OrganisationPolicy(getApplicant1OrganisationPolicy(caseData, userInfo))
            .respondentSolicitor1EmailAddress(getRespondent1SolicitorEmail(caseData, userInfo))
            .generalAppType(caseData.getGeneralAppType())
            .generalAppRespondentAgreement(caseData.getGeneralAppRespondentAgreement())
            .generalAppPBADetails(caseData.getGeneralAppPBADetails())
            .generalAppDetailsOfOrder(caseData.getGeneralAppDetailsOfOrder())
            .generalAppReasonsOfOrder(caseData.getGeneralAppReasonsOfOrder())
            .generalAppInformOtherParty(caseData.getGeneralAppInformOtherParty())
            .generalAppUrgencyRequirement(caseData.getGeneralAppUrgencyRequirement())
            .generalAppStatementOfTruth(caseData.getGeneralAppStatementOfTruth())
            .generalAppHearingDetails(caseData.getGeneralAppHearingDetails())
            .build();
    }

    private List<Element<GeneralApplication>> addApplication(GeneralApplication application,
                                                            List<Element<GeneralApplication>>
                                                                generalApplicationDetails) {
        List<Element<GeneralApplication>> newApplication = ofNullable(generalApplicationDetails).orElse(newArrayList());
        newApplication.add(element(application));

        return newApplication;
    }
}
