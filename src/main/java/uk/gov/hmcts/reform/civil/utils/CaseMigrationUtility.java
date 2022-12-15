package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseMigrationUtility {

    private final LocationRefDataService locationRefDataService;
    private final CoreCaseDataService coreCaseDataService;

    // Applicable for both spec and unspec
    public void migrateCaseManagementLocation(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                              CaseLocation caseLocation) {
        log.info("Migrate Case Management location for spec and unspec");

        caseDataBuilder.caseManagementLocation(caseLocation);

        log.info("Migrated Case Management location for spec and unspec");

    }

    public void migrateUnspecCourtLocation(String authToken, CaseData oldCaseData,
                                           CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        log.info("Migrate Case  location for  unspec: {}", oldCaseData.getCcdCaseReference());

        CourtLocation location = oldCaseData.getCourtLocation();
        if (ofNullable(location).isPresent()) {
            log.info(" migrateUnspecCourtLocation Going to fetch data from LRD preferred code  : {}, case reference {} ",
                     location.getApplicantPreferredCourt(),
                     oldCaseData.getCcdCaseReference());
            LocationRefData refData = locationRefDataService.getCourtLocation(
                authToken,
                location.getApplicantPreferredCourt()
            );
            validateLocation(refData, location.getApplicantPreferredCourt());
            log.info(
                "migrateUnspecCourtLocation Location details found:: " +
                    "court code : {} region : {} , EpimmsId {} ,case reference {}",
                refData.getCourtLocationCode(),
                refData.getRegionId(),
                refData.getEpimmsId(),
                oldCaseData.getCcdCaseReference()
            );

            CaseLocation caseLocation = CaseLocation.builder()
                .baseLocation(refData.getEpimmsId())
                .region(refData.getRegionId())
                .build();
            caseDataBuilder.courtLocation(oldCaseData.getCourtLocation().toBuilder()
                                              .caseLocation(caseLocation)
                                              .applicantPreferredCourtLocationList(
                                                  location.getApplicantPreferredCourtLocationList())
                                              .applicantPreferredCourt(location.getApplicantPreferredCourt()).build());
        } else {
            log.error(
                "migrateUnspecCourtLocation Case location is not present for the case {}," +
                    " can not make call to reference data.",
                oldCaseData.getCcdCaseReference()
            );
        }

    }

    public void migrateRespondentAndApplicantDQUnSpec(String authToken, CaseData oldCaseData,
                                                      CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                      CaseLocation caseLocation) {
        log.info("CaseCategory is : {}", oldCaseData.getCaseAccessCategory());

        if (CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            log.info("Spec DQ Migration: {}", oldCaseData.getCcdCaseReference());

            migrateRespondent1DQ(authToken, oldCaseData, caseDataBuilder, caseLocation);
            migrateRespondent2DQ(authToken, oldCaseData, caseDataBuilder, caseLocation);
        } else {
            log.info("UNSpec DQ Migration: {}", oldCaseData.getCcdCaseReference());
            migrateRespondent1DQUnspec(authToken, oldCaseData, caseDataBuilder, caseLocation);
            migrateRespondent2DQUnSpec(authToken, oldCaseData, caseDataBuilder, caseLocation);
        }
        migrateApplicant1DQ(authToken, oldCaseData, caseDataBuilder, caseLocation);
        //migrateApplicant2DQ(authToken, oldCaseData, caseDataBuilder, locationRefDataService, caseLocation);

    }

    // Applicable for Respondent1 and Respondent2
    private void migrateRespondent1DQ(String authToken, CaseData oldCaseData,
                                      CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                      CaseLocation caseLocation) {
        log.info("Migrate respondent 1 DQ start: {}", oldCaseData.getCcdCaseReference());
        Respondent1DQ respondent1DQ = oldCaseData.getRespondent1DQ();
        if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt()).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode()).isPresent()) {
            log.info("migrateRespondent1DQFetch data from LRD preferred code  : {} ",
                     respondent1DQ.getRespondent1DQRequestedCourt()
                .getResponseCourtCode());
            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent1DQ.getRespondent1DQRequestedCourt()
                    .getResponseCourtCode()
            );
            validateLocation(refdata, respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode());
            log.info("migrateRespondent1DQ Location details ," +
                         "courtcode : {} region : {} ,baseLocation {}, case reference {} ",
                     refdata.getCourtLocationCode(),
                     refdata.getRegionId(), refdata.getEpimmsId(), oldCaseData.getCcdCaseReference()
            );
            CaseLocation location = CaseLocation.builder()
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
        log.info("Migrate respondent 1 DQ end");
    }

    private void migrateRespondent2DQ(String authToken, CaseData oldCaseData,
                                      CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                      CaseLocation caseLocation) {
        log.info("migrateRespondent2DQ Migrate respondent 2 DQ start: {}", oldCaseData.getCcdCaseReference());
        Respondent2DQ respondent2DQ = oldCaseData.getRespondent2DQ();
        if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt()).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode()).isPresent()) {
            log.info("migrateRespondent2DQFetch data from LRD preferred code  : {} ",
                     respondent2DQ.getRespondent2DQRequestedCourt()
                .getResponseCourtCode());
            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent2DQ.getRespondent2DQRequestedCourt()
                    .getResponseCourtCode()
            );
            validateLocation(refdata, respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode());
            log.info("migrateRespondent2DQ Location details ,courtcode : {} region : {} ,baseLocation {}, case ref {} ",
                     refdata.getCourtLocationCode(),
                     refdata.getRegionId(), refdata.getEpimmsId(),
                     oldCaseData.getCcdCaseReference()
            );
            CaseLocation location = CaseLocation.builder()
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
        log.info("Migrate respondent 2 DQ end");
    }

    private void migrateRespondent1DQUnspec(String authToken, CaseData oldCaseData,
                                            CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                            CaseLocation caseLocation) {
        log.info("Migrate respondent 1 DQ start unpec: {}", oldCaseData.getCcdCaseReference());
        Respondent1DQ respondent1DQ = oldCaseData.getRespondent1DQ();
        if (ofNullable(respondent1DQ).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt()).isPresent()
            && ofNullable(respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode()).isPresent()) {
            log.info(
                "migrateRespondent1DQUnspec Fetch data from LRD preferred code  : {}, ref {} ",
                respondent1DQ.getRespondent1DQRequestedCourt()
                    .getResponseCourtCode(),
                oldCaseData.getCcdCaseReference()
            );
            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent1DQ.getRespondent1DQRequestedCourt()
                    .getResponseCourtCode()
            );
            validateLocation(refdata, respondent1DQ.getRespondent1DQRequestedCourt().getResponseCourtCode());
            log.info("migrateRespondent1DQUnspec Location details ,courtcode :" +
                         " {} region : {} ,baseLocation {}, ref {} ", refdata.getCourtLocationCode(),
                     refdata.getRegionId(), refdata.getEpimmsId(), oldCaseData.getCcdCaseReference()
            );
            CaseLocation location = CaseLocation.builder()
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
        log.info("Migrate respondent 1 DQ end unpec end");
    }

    private void migrateRespondent2DQUnSpec(String authToken, CaseData oldCaseData,
                                            CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                            CaseLocation caseLocation) {
        log.info("migrateRespondent2DQUnSpec: Migrate respondent 1 DQ start unpec: {}",
                 oldCaseData.getCcdCaseReference());
        Respondent2DQ respondent2DQ = oldCaseData.getRespondent2DQ();
        if (ofNullable(respondent2DQ).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt()).isPresent()
            && ofNullable(respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode()).isPresent()) {
            log.info("migrateRespondent2DQUnSpec Fetch data from LRD preferred code  : {}, Ref : {} ",
                     respondent2DQ.getRespondent2DQRequestedCourt()
                .getResponseCourtCode(), oldCaseData.getCcdCaseReference());
            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                respondent2DQ.getRespondent2DQRequestedCourt()
                    .getResponseCourtCode()
            );
            validateLocation(refdata, respondent2DQ.getRespondent2DQRequestedCourt().getResponseCourtCode());
            log.info("migrateRespondent2DQUnSpec Location details ,courtcode :" +
                         " {} region : {} ,baseLocation {}, ref {} ", refdata.getCourtLocationCode(),
                     refdata.getRegionId(), refdata.getEpimmsId(), oldCaseData.getCcdCaseReference()
            );
            CaseLocation location = CaseLocation.builder()
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
        } else {
            log.warn("migrateRespondent2DQUnSpec: No if condition succeeded.");
        }
    }

    // Applicable for Respondent1 and Respondent2
    private void migrateApplicant1DQ(String authToken, CaseData oldCaseData,
                                     CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                     CaseLocation caseLocation) {

        Applicant1DQ applicant1DQ = oldCaseData.getApplicant1DQ();
        log.info("migrateApplicant1DQ Migrate applicant 1 DQ start : {}", oldCaseData.getCcdCaseReference());
        if (ofNullable(applicant1DQ).isPresent()
            && ofNullable(applicant1DQ.getApplicant1DQRequestedCourt()).isPresent()
            && ofNullable(applicant1DQ.getApplicant1DQRequestedCourt().getResponseCourtCode()).isPresent()) {
            log.info("migrateApplicant1DQ Fetch data from LRD preferred code  : {}, Ref : {} ",
                     applicant1DQ.getApplicant1DQRequestedCourt()
                .getResponseCourtCode(), oldCaseData.getCcdCaseReference());
            LocationRefData refdata = locationRefDataService.getCourtLocation(
                authToken,
                applicant1DQ.getApplicant1DQRequestedCourt()
                    .getResponseCourtCode()
            );
            validateLocation(refdata, applicant1DQ.getApplicant1DQRequestedCourt().getResponseCourtCode());
            log.info("migrateApplicant1DQ Location details ,courtcode : {} region : {} ,baseLocation {}, ref {} ",
                     refdata.getCourtLocationCode(),
                     refdata.getRegionId(), refdata.getEpimmsId(), oldCaseData.getCcdCaseReference()
            );
            CaseLocation location = CaseLocation.builder()
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
            log.info("migrateApplicant1DQ Fetch data from LRD preferred code  : {} , Ref : {}",
                     oldCaseData.getCourtLocation()
                .getApplicantPreferredCourt(), oldCaseData.getCcdCaseReference());
            LocationRefData refData = locationRefDataService.getCourtLocation(
                authToken,
                oldCaseData.getCourtLocation().getApplicantPreferredCourt()
            );
            validateLocation(refData, applicant1DQ.getApplicant1DQRequestedCourt().getResponseCourtCode());
            log.info("migrateApplicant1DQ Location details ,courtcode : {} region : {} ,baseLocation {}, Ref {} ",
                     refData.getCourtLocationCode(),
                     refData.getRegionId(), refData.getEpimmsId(), oldCaseData.getCcdCaseReference()
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
    public void migrateGS(CaseData oldCaseData,
                          CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        log.info("Migrate GS related data for case {}", oldCaseData.getCcdCaseReference());
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
    public void setSupplementaryData(Long caseId, String specSiteId) {
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

    private void validateLocation(LocationRefData refData, String courtCode) {
        if(ofNullable(refData.getRegionId()).isEmpty()) {
            log.error("No court Location Found for three digit court code : {}", courtCode);
            throw new RuntimeException("No court Location Found for three digit court code :"+ courtCode);
        }

    }

}
