package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public class CaseMigratonUtility {

    // Applicable for both spec and unspec
    public static void migrateCaseManagementLocation(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                              CaseLocation caseLocation) {
        caseDataBuilder.caseManagementLocation(caseLocation);

    }

    public static void migrateUnspecCoutLocation(String authToken, CaseData oldCaseData,
                                          CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                          LocationRefDataService locationRefDataService) {

        CourtLocation location = oldCaseData.getCourtLocation();
        // To fetch Location ref data based om preferred court
        LocationRefData refdata = locationRefDataService.getCourtLocation(
            authToken,
            location.getApplicantPreferredCourt()
        );
        CaseLocation caseLocation = CaseLocation.builder().baseLocation(refdata.getEpimmsId())
            .region(refdata.getRegionId()).build();
        caseDataBuilder.courtLocation(CourtLocation.builder().caseLocation(caseLocation)
                                          .applicantPreferredCourtLocationList(
                                              location.getApplicantPreferredCourtLocationList())
                                          .applicantPreferredCourt(location.getApplicantPreferredCourt()).build());


    }

    public static void migrateRespondentAndApplicantDQ(String authToken, CaseData oldCaseData,
                                           CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                           LocationRefDataService locationRefDataService) {
        migrateRespondent1DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService);
        migrateRespondent2DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService);
        migrateApplicant1DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService);
        migrateApplicant2DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService);

    }
    // Applicable for Respondent1 and Respondent2
    private static void migrateRespondent1DQ(String authToken, CaseData oldCaseData,
                                     CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                     LocationRefDataService locationRefDataService) {

        Respondent1DQ respondent1DQ = oldCaseData.getRespondent1DQ();
        if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt()).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent1DQ.getRespondent1DQRequestedCourt()
                    .getResponseCourtCode()

            );

            caseDataBuilder.respondent1DQ(respondent1DQ.builder()
                                              .respondent1DQRequestedCourt(respondent1DQ
                                                                               .getRespondent1DQRequestedCourt()
                                                                               .builder()
                                                                               .caseLocation(CaseLocation.builder()
                                                                                                 .baseLocation(
                                                                                                     refdata.getEpimmsId())
                                                                                                 .region(refdata
                                                                                                             .getRegion()).build())
                                                                               .build()).build());

        }
        }

    private static void migrateRespondent2DQ(String authToken, CaseData oldCaseData,
                                     CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                     LocationRefDataService locationRefDataService) {

        Respondent2DQ respondent2DQ = oldCaseData.getRespondent2DQ();
        if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt()).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent2DQ.getRespondent2DQRequestedCourt()
                    .getResponseCourtCode()

            );

            caseDataBuilder.respondent2DQ(respondent2DQ.builder()
                                              .respondent2DQRequestedCourt(respondent2DQ
                                                                               .getRespondent2DQRequestedCourt()
                                                                               .builder()
                                                                               .caseLocation(CaseLocation.builder()
                                                                                                 .baseLocation(
                                                                                                     refdata.getEpimmsId())
                                                                                                 .region(
                                                                                                     refdata.getRegion())
                                                                                                 .build()).build()).build());

        }
        }

    // Applicable for Respondent1 and Respondent2
    private static void migrateApplicant1DQ(String authToken, CaseData oldCaseData,
                                    CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                    LocationRefDataService locationRefDataService) {

        Applicant1DQ applicant1DQ = oldCaseData.getApplicant1DQ();

        if (ofNullable(applicant1DQ).isPresent()
            && ofNullable(applicant1DQ.getApplicant1DQRequestedCourt()).isPresent()
            && ofNullable(applicant1DQ.getApplicant1DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                applicant1DQ.getApplicant1DQRequestedCourt()
                    .getResponseCourtCode()

            );

            caseDataBuilder.applicant1DQ(applicant1DQ.builder()
                                             .applicant1DQRequestedCourt(applicant1DQ
                                                                             .getApplicant1DQRequestedCourt()
                                                                             .builder()
                                                                             .caseLocation(CaseLocation.builder()
                                                                                               .baseLocation(
                                                                                                   refdata.getEpimmsId())
                                                                                               .region(refdata.getRegion())
                                                                                               .build()).build()).build());
        }

    }

    // Applicable for Respondent1 and Respondent2
    private static void migrateApplicant2DQ(String authToken, CaseData oldCaseData,
                                    CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                    LocationRefDataService locationRefDataService) {

        Applicant2DQ applicant2DQ = oldCaseData.getApplicant2DQ();
        if (ofNullable(applicant2DQ).isPresent()
            && ofNullable(applicant2DQ.getApplicant2DQRequestedCourt()).isPresent()
            && ofNullable(applicant2DQ.getApplicant2DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                applicant2DQ.getApplicant2DQRequestedCourt()
                    .getResponseCourtCode()

            );

            caseDataBuilder.applicant2DQ(applicant2DQ.builder()
                                             .applicant2DQRequestedCourt(applicant2DQ
                                                                             .getApplicant2DQRequestedCourt()
                                                                             .builder()
                                                                             .caseLocation(CaseLocation.builder()
                                                                                               .baseLocation(refdata.getEpimmsId())
                                                                                               .region(refdata.getRegion())
                                                                                               .build()).build()).build());

        }
        }

    // Case management category,caseNameHmctsInternal, and supplementaryData
    public static void migrateGS(CaseData oldCaseData, String specSiteId,
                          CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                          CoreCaseDataService coreCaseDataService) {

        caseDataBuilder.caseNameHmctsInternal(getCaseParticipants(oldCaseData).toString());
        CaseManagementCategoryElement civil =
            CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        caseDataBuilder.caseManagementCategory(
            CaseManagementCategory.builder().value(civil).list_items(itemList).build());
        setSupplementaryData(oldCaseData.getCcdCaseReference(), coreCaseDataService, specSiteId);

    }

    //get  specSiteId from   PaymentsConfiguration paymentsConfiguration;
    private static void setSupplementaryData(Long caseId, CoreCaseDataService coreCaseDataService, String specSiteId) {
        Map<String, Map<String, Map<String, Object>>> supplementaryDataCivil = new HashMap<>();
        supplementaryDataCivil.put("supplementary_data_updates",
                                   singletonMap("$set", singletonMap("HMCTSServiceId",
                                                                     specSiteId)));
        coreCaseDataService.setSupplementaryData(caseId, supplementaryDataCivil);

    }
    private static StringBuilder getCaseParticipants(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" v ").append(caseData.getRespondent1().getPartyName())
                .append(" and ").append(caseData.getRespondent2().getPartyName());

        } else if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" and ").append(caseData.getApplicant2().getPartyName()).append(" v ")
                .append(caseData.getRespondent1()
                            .getPartyName());

        } else {
            participantString.append(caseData.getApplicant1().getPartyName()).append(" v ")
                .append(caseData.getRespondent1()
                            .getPartyName());
        }
        return participantString;

    }

}
