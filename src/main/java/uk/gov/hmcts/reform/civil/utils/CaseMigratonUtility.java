package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
//import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;

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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
public class CaseMigratonUtility {

    private CaseMigratonUtility() {
        //NO-OP
    }

    // Applicable for both spec and unspec
    public static void migrateCaseManagementLocation(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                     CaseLocation caseLocation) {
        log.info("Migrate Case Management location for spec and unspec");

        caseDataBuilder.caseManagementLocation(caseLocation);
    }

    public static void migrateUnspecCoutLocation(String authToken, CaseData oldCaseData,
                                                 CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                 LocationRefDataService locationRefDataService) {
        log.info("Migrate Case  location for  unspec");

        CourtLocation location = oldCaseData.getCourtLocation();
        if (ofNullable(location).isPresent()) {
            LocationRefData refData = locationRefDataService.getCourtLocation(
                authToken,
                location.getApplicantPreferredCourt()
            );
            CaseLocation caseLocation = CaseLocation.builder().baseLocation(refData.getEpimmsId())
                .region(refData.getRegionId()).build();
            caseDataBuilder.courtLocation(oldCaseData.getCourtLocation().toBuilder().caseLocation(caseLocation)
                                              .applicantPreferredCourtLocationList(
                                                  location.getApplicantPreferredCourtLocationList())
                                              .applicantPreferredCourt(location.getApplicantPreferredCourt()).build());
        }

    }

    public static void migrateRespondentAndApplicantDQUnSpec(String authToken, CaseData oldCaseData,
                                                             CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                             LocationRefDataService locationRefDataService,
                                                             CaseLocation caseLocation) {
        log.info("CaseCategory is : {}", oldCaseData.getCaseAccessCategory());

        if (CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            log.info("Spec DQ Migration");

            migrateRespondent1DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);
            migrateRespondent2DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);
        } else {
            log.info("UNSpec DQ Migration");
            migrateRespondent1DQUnspec(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);
            migrateRespondent2DQUnSpec(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);
        }
        migrateApplicant1DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);
        //migrateApplicant2DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);

    }

    // Applicable for Respondent1 and Respondent2
    private static void migrateRespondent1DQ(String authToken, CaseData oldCaseData,
                                             CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                             LocationRefDataService locationRefDataService,
                                             CaseLocation caseLocation) {

        Respondent1DQ respondent1DQ = oldCaseData.getRespondent1DQ();
        if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt()).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent1DQ.getRespondent1DQRequestedCourt()
                    .getResponseCourtCode()
            );

            CaseLocation location =  CaseLocation.builder()
                .baseLocation(refdata.getEpimmsId()).region(refdata.getRegionId()).build();

            caseDataBuilder.respondent1DQ(respondent1DQ.toBuilder()
                                              .respondent1DQRequestedCourt(respondent1DQ
                                                                               .getRespondent1DQRequestedCourt()
                                                                               .toBuilder()
                                                                               .caseLocation(location)
                                                                               .build())
                                              .respondToCourtLocation(
                                                  RequestedCourt.builder()
                                                      .responseCourtLocations(null)
                                                      .responseCourtCode(refdata.getCourtLocationCode())

                                                      .build()).responseClaimCourtLocationRequired(YES).build());

        } else if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQExperts()).isPresent()) {

            caseDataBuilder.respondent1DQ(respondent1DQ.toBuilder()
                                              .respondent1DQRequestedCourt(RequestedCourt
                                                                               .builder()
                                                                               .caseLocation(caseLocation)
                                                                               .build())
                                              .respondToCourtLocation(
                                                  RequestedCourt.builder()
                                                      .responseCourtLocations(null)
                                                      .responseCourtCode("335")
                                                      .build()).responseClaimCourtLocationRequired(YES).build());

        } /*else if (ofNullable(respondent1DQ).isPresent()) {
            caseDataBuilder.respondent1DQ(respondent1DQ.toBuilder()
                                              .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                               .caseLocation(caseLocation)
                                                                               .build()).build());
        }*/
    }

    private static void migrateRespondent2DQ(String authToken, CaseData oldCaseData,
                                             CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                             LocationRefDataService locationRefDataService,
                                             CaseLocation caseLocation) {

        Respondent2DQ respondent2DQ = oldCaseData.getRespondent2DQ();
        if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt()).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent2DQ.getRespondent2DQRequestedCourt()
                    .getResponseCourtCode()
            );

            CaseLocation location =  CaseLocation.builder()
                .baseLocation(refdata.getEpimmsId()).region(refdata.getRegionId()).build();

            caseDataBuilder.respondent2DQ(respondent2DQ.builder()
                                              .respondent2DQRequestedCourt(respondent2DQ
                                                                               .getRespondent2DQRequestedCourt()
                                                                               .toBuilder()
                                                                               .caseLocation(location)
                                                                               .build())
                                              .respondToCourtLocation2(RequestedCourt.builder()
                                                                           .responseCourtLocations(null)
                                                                           .responseCourtCode(
                                                                               refdata.getCourtLocationCode())
                                                                           .build())
                                              .build());
            caseDataBuilder.responseClaimCourtLocation2Required(YES);
        } else if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQExperts()).isPresent()) {
            caseDataBuilder.respondent2DQ(respondent2DQ.toBuilder()
                                              .respondent2DQRequestedCourt(RequestedCourt.builder()
                                                                               .caseLocation(caseLocation)
                                                                               .build())
                                              .respondToCourtLocation2(RequestedCourt.builder()
                                                                           .responseCourtLocations(null)
                                                                           .responseCourtCode("335")
                                                                           .build())
                                              .build());
            caseDataBuilder.responseClaimCourtLocation2Required(YES);
        } /*else if (ofNullable(respondent2DQ).isPresent()) {
            caseDataBuilder.respondent2DQ(respondent2DQ.toBuilder()
                                              .respondent2DQRequestedCourt(RequestedCourt.builder()
                                                                               .caseLocation(caseLocation)
                                                                               .build()).build());
        }*/
    }

    private static void migrateRespondent1DQUnspec(String authToken, CaseData oldCaseData,
                                             CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                             LocationRefDataService locationRefDataService,
                                             CaseLocation caseLocation) {

        Respondent1DQ respondent1DQ = oldCaseData.getRespondent1DQ();
        if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt()).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent1DQ.getRespondent1DQRequestedCourt()
                    .getResponseCourtCode()
            );

            CaseLocation location =  CaseLocation.builder()
                .baseLocation(refdata.getEpimmsId()).region(refdata.getRegionId()).build();

            caseDataBuilder.respondent1DQ(respondent1DQ.toBuilder()
                                              .respondent1DQRequestedCourt(respondent1DQ
                                                                               .getRespondent1DQRequestedCourt()
                                                                               .toBuilder()
                                                                               .caseLocation(location)
                                                                               .build())
                                             .build());

        } else if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQExperts()).isPresent()) {

            caseDataBuilder.respondent1DQ(respondent1DQ.toBuilder()
                                              .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                               .caseLocation(caseLocation)
                                                                               .build()).build());

        }
    }

    private static void migrateRespondent2DQUnSpec(String authToken, CaseData oldCaseData,
                                             CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                             LocationRefDataService locationRefDataService,
                                             CaseLocation caseLocation) {

        Respondent2DQ respondent2DQ = oldCaseData.getRespondent2DQ();
        if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt()).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent2DQ.getRespondent2DQRequestedCourt()
                    .getResponseCourtCode()
            );

            CaseLocation location =  CaseLocation.builder()
                .baseLocation(refdata.getEpimmsId()).region(refdata.getRegionId()).build();

            caseDataBuilder.respondent2DQ(respondent2DQ.builder()
                                              .respondent2DQRequestedCourt(respondent2DQ
                                                                               .getRespondent2DQRequestedCourt()
                                                                               .toBuilder()
                                                                               .caseLocation(location)
                                                                               .build()).build());
        } else if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQExperts()).isPresent()) {
            caseDataBuilder.respondent2DQ(respondent2DQ.toBuilder()
                                              .respondent2DQRequestedCourt(RequestedCourt.builder()
                                                                               .caseLocation(caseLocation)
                                                                               .build()).build());
        }
    }

    // Applicable for Respondent1 and Respondent2
    private static void migrateApplicant1DQ(String authToken, CaseData oldCaseData,
                                            CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                            LocationRefDataService locationRefDataService,
                                            CaseLocation caseLocation) {

        Applicant1DQ applicant1DQ = oldCaseData.getApplicant1DQ();

        if (ofNullable(applicant1DQ).isPresent()
            && ofNullable(applicant1DQ.getApplicant1DQRequestedCourt()).isPresent()
            && ofNullable(applicant1DQ.getApplicant1DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                applicant1DQ.getApplicant1DQRequestedCourt()
                    .getResponseCourtCode()
            );

            CaseLocation location =  CaseLocation.builder()
                .baseLocation(refdata.getEpimmsId()).region(refdata.getRegionId()).build();

            caseDataBuilder.applicant1DQ(applicant1DQ.toBuilder()
                                             .applicant1DQRequestedCourt(applicant1DQ
                                                                             .getApplicant1DQRequestedCourt()
                                                                             .toBuilder()
                                                                             .caseLocation(location)
                                                                             .responseCourtCode(
                                                                                 refdata.getCourtLocationCode())
                                                                             .build()).build());
        } else if (ofNullable(applicant1DQ).isPresent()
            && CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())
            && ofNullable(applicant1DQ.getExperts()).isPresent()) {

            caseDataBuilder.applicant1DQ(applicant1DQ.toBuilder()
                                             .applicant1DQRequestedCourt(RequestedCourt.builder()
                                                                             .caseLocation(caseLocation)
                                                                             .responseCourtCode("335")
                                                                             .build()).build());

        } else if (ofNullable(applicant1DQ).isPresent() && ofNullable(oldCaseData.getCourtLocation()).isPresent()
            && ofNullable(applicant1DQ.getExperts()).isPresent()) {
            LocationRefData refData = locationRefDataService.getCourtLocation(
                authToken,
                oldCaseData.getCourtLocation().getApplicantPreferredCourt()
            );
            caseLocation = CaseLocation.builder().baseLocation(refData.getEpimmsId())
                .region(refData.getRegionId()).build();
            caseDataBuilder.applicant1DQ(applicant1DQ.toBuilder()
                                             .applicant1DQRequestedCourt(
                                                 RequestedCourt.builder()
                                                     .caseLocation(caseLocation)
                                                     .responseCourtCode(oldCaseData.getCourtLocation()
                                                         .getApplicantPreferredCourt())
                                                     .build()).build());

        }
    }

    // Applicable for Respondent1 and Respondent2
    /*private static void migrateApplicant2DQ(String authToken, CaseData oldCaseData,
                                            CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                            LocationRefDataService locationRefDataService,
                                            CaseLocation caseLocation) {

        Applicant2DQ applicant2DQ = oldCaseData.getApplicant2DQ();
        if (ofNullable(applicant2DQ).isPresent()
            && ofNullable(applicant2DQ.getApplicant2DQRequestedCourt()).isPresent()
            && ofNullable(applicant2DQ.getApplicant2DQRequestedCourt().getResponseCourtCode()).isPresent()) {

            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                applicant2DQ.getApplicant2DQRequestedCourt()
                    .getResponseCourtCode()
            );

            CaseLocation location =  CaseLocation.builder()
                .baseLocation(refdata.getEpimmsId()).region(refdata.getRegionId()).build();

            caseDataBuilder.applicant2DQ(applicant2DQ.toBuilder()
                                             .applicant2DQRequestedCourt(applicant2DQ
                                                                             .getApplicant2DQRequestedCourt()
                                                                             .toBuilder()
                                                                             .caseLocation(location)
                                                                             .build()).build());

        } else if (ofNullable(applicant2DQ).isPresent()
            && CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            caseDataBuilder.applicant2DQ(applicant2DQ.toBuilder()
                                             .applicant2DQRequestedCourt(RequestedCourt.builder()
                                                                             .caseLocation(caseLocation)
                                                                             .build()).build());
        } else if (ofNullable(applicant2DQ).isPresent() && ofNullable(oldCaseData.getCourtLocation()).isPresent()) {

                LocationRefData refData = locationRefDataService.getCourtLocation(
                    authToken,
                    oldCaseData.getCourtLocation().getApplicantPreferredCourt()
                );
                 caseLocation = CaseLocation.builder().baseLocation(refData.getEpimmsId())
                    .region(refData.getRegionId()).build();

                caseDataBuilder.applicant2DQ(applicant2DQ.toBuilder()
                                             .applicant2DQRequestedCourt(RequestedCourt.builder()
                                                                             .caseLocation(caseLocation)
                                                                             .build()).build());
        }

    }*/

    // Case management category,caseNameHmctsInternal, and supplementaryData
    public static void migrateGS(CaseData oldCaseData,
                                 CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        log.info("Migrate GS related data");
        caseDataBuilder.caseNameHmctsInternal(getCaseParticipants(oldCaseData).toString());

        CaseManagementCategoryElement civil =
            CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        caseDataBuilder.caseManagementCategory(
            CaseManagementCategory.builder().value(civil).list_items(itemList).build());
        //  setSupplementaryData(oldCaseData.getCcdCaseReference(), coreCaseDataService, specSiteId);

    }

    //get  specSiteId from   PaymentsConfiguration paymentsConfiguration;
    public static void setSupplementaryData(Long caseId, CoreCaseDataService coreCaseDataService, String specSiteId) {
        log.info("GS Site ID is : {}", specSiteId);
        Map<String, Map<String, Map<String, Object>>> supplementaryDataCivil = new HashMap<>();

        supplementaryDataCivil.put(
            "supplementary_data_updates",
            singletonMap("$set", singletonMap(
                "HMCTSServiceId",
                specSiteId
            ))
        );
        CaseDetails details = coreCaseDataService.setSupplementaryData(caseId, supplementaryDataCivil);
        log.info("GS Site After submission  : {}", details);
    }

    private static StringBuilder getCaseParticipants(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
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
