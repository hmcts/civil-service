package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.SpecifiedParty;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.ResponseRepaymentDetailsForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.TimelineEventDetailsDocmosis;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGeneratorWithAuth;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers.ReferenceNumberAndCourtDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers.StatementOfTruthPopulator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1V2_LR_ADMISSION_BULK;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS_LR_ADMISSION_BULK;

@Slf4j
@Service
@RequiredArgsConstructor
public class SealedClaimResponseFormGeneratorForSpec implements TemplateDataGeneratorWithAuth<SealedClaimResponseFormForSpec> {

    private final RepresentativeService representativeService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final FeatureToggleService featureToggleService;
    private final ReferenceNumberAndCourtDetailsPopulator referenceNumberPopulator;
    private final StatementOfTruthPopulator statementOfTruthPopulator;

    @Override
    public SealedClaimResponseFormForSpec getTemplateData(CaseData caseData, String authorisation) {
        log.info("GetTemplateData for case ID {}", caseData.getCcdCaseReference());
        SealedClaimResponseFormForSpec form = new SealedClaimResponseFormForSpec();

        referenceNumberPopulator.populateReferenceNumberDetails(form, caseData, authorisation);
        statementOfTruthPopulator.populateStatementOfTruthDetails(form, caseData);

        addCarmMediationDetails(form, caseData);
        addRepaymentPlanDetails(form, caseData);
        handleRespondents(form, caseData);

        Optional.ofNullable(caseData.getSolicitorReferences()).ifPresent(form::setSolicitorReferences);

        handleClaimResponse(form, caseData);      // sets defendantResponse/submittedOn
        mapWhyDisputeTheClaim(form, caseData);    // <— missing before; fixes failing test

        handleTimeline(form, caseData);
        handleDefenceResponseDocument(form, caseData);
        handlePayments(caseData, form);

        return form;
    }

    private void handleRespondents(SealedClaimResponseFormForSpec form, CaseData caseData) {
        if (MultiPartyScenario.getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            SpecifiedParty currentRespondent = getDefendant1v2ds(caseData);
            form.setRespondent1(currentRespondent);

            Optional.ofNullable(currentRespondent)
                .map(SpecifiedParty::getRepresentative)
                .map(Representative::getOrganisationName)
                .ifPresent(form::setRespondentRepresentativeOrganisationName);
        } else {
            Representative respondent1Representative = representativeService.getRespondent1Representative(caseData);
            form.setRespondent1(getSpecifiedParty(caseData.getRespondent1(), respondent1Representative));
            Optional.ofNullable(respondent1Representative)
                .map(Representative::getOrganisationName)
                .ifPresent(form::setRespondentRepresentativeOrganisationName);
            Optional.ofNullable(caseData.getRespondent2()).ifPresent(
                respondent2 -> form.setRespondent2(
                    getSpecifiedParty(respondent2, representativeService.getRespondent2Representative(caseData))
                )
            );
        }
    }

    private void handleClaimResponse(SealedClaimResponseFormForSpec form, CaseData caseData) {
        if (isRespondent2(caseData) && !YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
            Optional.ofNullable(caseData.getRespondent2ClaimResponseTypeForSpec())
                .map(RespondentResponseTypeSpec::getDisplayedValue)
                .ifPresent(form::setDefendantResponse);
            form.setSubmittedOn(caseData.getRespondent2ResponseDate().toLocalDate());
        } else if (caseData.getRespondent2() != null && YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
            Optional.ofNullable(caseData.getRespondent1ClaimResponseTypeForSpec())
                .map(RespondentResponseTypeSpec::getDisplayedSingularValue)
                .ifPresent(form::setDefendantResponse);
            form.setSubmittedOn(caseData.getRespondent1ResponseDate().toLocalDate());
        } else {
            Optional.ofNullable(caseData.getRespondent1ClaimResponseTypeForSpec())
                .map(RespondentResponseTypeSpec::getDisplayedValue)
                .ifPresent(form::setDefendantResponse);
            form.setSubmittedOn(caseData.getRespondent1ResponseDate().toLocalDate());
        }
    }

    /**
     * Explicitly map the free-text “why dispute” field expected by the Docmosis DTO.
     * Tests expect:
     * - 1v1 → caseData.detailsOfWhyDoesYouDisputeTheClaim
     * - 1v2 latest is respondent2 → detailsOfWhyDoesYouDisputeTheClaim2
     * - 1v2 latest is respondent1 → detailsOfWhyDoesYouDisputeTheClaim
     */
    private void mapWhyDisputeTheClaim(SealedClaimResponseFormForSpec form,
                                       CaseData caseData) {
        String why;
        if (caseData.getRespondent2() != null && isRespondent2(caseData)) {
            // respondent 2 answered last
            why = caseData.getDetailsOfWhyDoesYouDisputeTheClaim2();
        } else {
            // single defendant or respondent 1 is the latest
            why = caseData.getDetailsOfWhyDoesYouDisputeTheClaim();
        }
        form.setWhyDisputeTheClaim(why);
    }

    private void handleTimeline(SealedClaimResponseFormForSpec form, CaseData caseData) {
        if (caseData.getSpecResponseTimelineDocumentFiles() != null) {
            form.setTimelineUploaded(true)
                .setSpecResponseTimelineDocumentFiles(
                    caseData.getSpecResponseTimelineDocumentFiles().getDocumentFileName()
                );
        } else {
            form.setTimelineUploaded(false)
                .setTimeline(getTimeLine(caseData));
        }
    }

    private void handleDefenceResponseDocument(SealedClaimResponseFormForSpec form, CaseData caseData) {
        if (caseData.getRespondent1SpecDefenceResponseDocument() != null && !isRespondent2(caseData)) {
            form.setRespondent1SpecDefenceResponseDocument(
                caseData.getRespondent1SpecDefenceResponseDocument().getFile().getDocumentFileName()
            );
        } else if (caseData.getRespondent2SpecDefenceResponseDocument() != null && isRespondent2(caseData)) {
            form.setRespondent1SpecDefenceResponseDocument(
                caseData.getRespondent2SpecDefenceResponseDocument().getFile().getDocumentFileName()
            );
        }
    }

    private void handlePayments(CaseData caseData, SealedClaimResponseFormForSpec form) {
        Stream.of(caseData.getRespondToClaim(), caseData.getRespondToAdmittedClaim())
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(response -> form
                .setPoundsPaid(MonetaryConversions.penniesToPounds(response.getHowMuchWasPaid()).toString())
                .setPaymentDate(response.getWhenWasThisAmountPaid())
                .setPaymentMethod(getPaymentMethod(response)));
    }

    private void addRepaymentPlanDetails(SealedClaimResponseFormForSpec form, CaseData caseData) {
        form.setCommonDetails(ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData));
    }

    private void addCarmMediationDetails(SealedClaimResponseFormForSpec form, CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_ONE, TWO_V_ONE, ONE_V_TWO_ONE_LEGAL_REP ->
                populateCarmMediationFieldsForRespondent1(form, caseData);
            case ONE_V_TWO_TWO_LEGAL_REP -> populateCarmMediationFieldsForRelevantRespondent(form, caseData);
            default -> throw new CallbackException("Cannot populate CARM fields");
        }
    }

    private void populateCarmMediationFieldsForRespondent1(SealedClaimResponseFormForSpec form, CaseData caseData) {
        getCarmMediationFields(
            form,
            getRespondent1MediationFirstName(caseData),
            getRespondent1MediationLastName(caseData),
            getRespondent1MediationContactNumber(caseData),
            getRespondent1MediationEmail(caseData),
            checkRespondent1MediationHasUnavailabilityDates(caseData),
            getRespondent1FromDateUnavailableList(caseData)
        );
    }

    private void populateCarmMediationFieldsForRelevantRespondent(SealedClaimResponseFormForSpec form, CaseData caseData) {
        if (shouldUseRespondent2MediationDetails(caseData)) {
            getCarmMediationFields(
                form,
                getRespondent2MediationFirstName(caseData),
                getRespondent2MediationLastName(caseData),
                getRespondent2MediationContactNumber(caseData),
                getRespondent2MediationEmail(caseData),
                checkRespondent2MediationHasUnavailabilityDates(caseData),
                getRespondent2FromDateUnavailableList(caseData)
            );
        } else {
            populateCarmMediationFieldsForRespondent1(form, caseData);
        }
    }

    private boolean shouldUseRespondent2MediationDetails(CaseData caseData) {
        return caseData.getRespondent1ResponseDate() == null
            || (caseData.getRespondent2ResponseDate() != null
            && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }

    private void getCarmMediationFields(SealedClaimResponseFormForSpec form,
                                        String firstName, String lastName, String contactNumber, String email,
                                        Boolean unavailableDatesExists, List<Element<UnavailableDate>> unavailableDatesList) {
        form
            .setMediationFirstName(firstName)
            .setMediationLastName(lastName)
            .setMediationContactNumber(contactNumber)
            .setMediationEmail(email)
            .setMediationUnavailableDatesExists(unavailableDatesExists)
            .setMediationUnavailableDatesList(unavailableDatesList);
    }

    private SpecifiedParty getDefendant1v2ds(CaseData caseData) {
        if (caseData.getRespondent1ResponseDate() == null
            || (caseData.getRespondent2ResponseDate() != null
            && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()))) {
            return getSpecifiedParty(
                caseData.getRespondent2(),
                representativeService.getRespondent2Representative(caseData)
            );
        } else {
            return getSpecifiedParty(
                caseData.getRespondent1(),
                representativeService.getRespondent1Representative(caseData)
            );
        }
    }

    private String getPaymentMethod(RespondToClaim response) {
        if (response.getHowWasThisAmountPaid() == PaymentMethod.OTHER) {
            return response.getHowWasThisAmountPaidOther();
        } else {
            return response.getHowWasThisAmountPaid().getHumanFriendly();
        }
    }

    private boolean isRespondent2(CaseData caseData) {
        return (caseData.getRespondent2ResponseDate() != null)
            && (caseData.getRespondent1ResponseDate() == null
            || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }

    private SpecifiedParty getSpecifiedParty(Party party, Representative representative) {
        SpecifiedParty form = new SpecifiedParty()
            .setName(party.getPartyName())
            .setPrimaryAddress(party.getPrimaryAddress());
        Optional.ofNullable(representative).ifPresent(form::setRepresentative);
        return form;
    }

    private List<TimelineEventDetailsDocmosis> getTimeLine(CaseData caseData) {
        if (caseData.getSpecResponseTimelineOfEvents() != null && !isRespondent2(caseData)) {
            return mapTimelineToDocmosis(caseData.getSpecResponseTimelineOfEvents());
        } else if (caseData.getSpecResponseTimelineOfEvents2() != null && isRespondent2(caseData)) {
            return mapTimelineToDocmosis(caseData.getSpecResponseTimelineOfEvents2());
        } else {
            return Collections.emptyList();
        }
    }

    private List<TimelineEventDetailsDocmosis> mapTimelineToDocmosis(List<TimelineOfEvents> timelineOfEvents) {
        List<TimelineEventDetailsDocmosis> timelineOfEventDetails = new ArrayList<>();
        for (TimelineOfEvents event : timelineOfEvents) {
            TimelineOfEventDetails timelineOfEventDetail = new TimelineOfEventDetails(
                event.getValue().getTimelineDate(),
                event.getValue().getTimelineDescription()
            );
            timelineOfEventDetails.add(new TimelineEventDetailsDocmosis(timelineOfEventDetail));
        }
        return timelineOfEventDetails;
    }

    private String getRespondent1MediationFirstName(CaseData caseData) {
        String mediationFirstname = caseData.getRespondent1().getPartyName();
        if (caseData.getResp1MediationContactInfo() != null
            && caseData.getResp1MediationContactInfo().getFirstName() != null) {
            mediationFirstname = caseData.getResp1MediationContactInfo().getFirstName();
        }
        return mediationFirstname;
    }

    private String getRespondent1MediationLastName(CaseData caseData) {
        String mediationLastName = caseData.getRespondent1().getPartyName();
        if (caseData.getResp1MediationContactInfo() != null
            && caseData.getResp1MediationContactInfo().getLastName() != null) {
            mediationLastName = caseData.getResp1MediationContactInfo().getLastName();
        }
        return mediationLastName;
    }

    private String getRespondent1MediationContactNumber(CaseData caseData) {
        String mediationContactNumber = caseData.getRespondent1().getPartyPhone();
        if (caseData.getResp1MediationContactInfo() != null
            && caseData.getResp1MediationContactInfo().getTelephoneNumber() != null) {
            mediationContactNumber = caseData.getResp1MediationContactInfo().getTelephoneNumber();
        }
        return mediationContactNumber;
    }

    private String getRespondent1MediationEmail(CaseData caseData) {
        String mediationEmail = caseData.getRespondent1().getPartyEmail();
        if (caseData.getResp1MediationContactInfo() != null
            && caseData.getResp1MediationContactInfo().getEmailAddress() != null) {
            mediationEmail = caseData.getResp1MediationContactInfo().getEmailAddress();
        }
        return mediationEmail;
    }

    private boolean checkRespondent1MediationHasUnavailabilityDates(CaseData caseData) {
        return caseData.getResp1MediationAvailability() != null
            && caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists() != null
            && caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists().equals(YesOrNo.YES);
    }

    private List<Element<UnavailableDate>> getRespondent1FromDateUnavailableList(CaseData caseData) {
        List<Element<UnavailableDate>> datesUnavailableList = null;
        if (caseData.getResp1MediationAvailability() != null
            && caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists() != null
            && caseData.getResp1MediationAvailability().getIsMediationUnavailablityExists().equals(YesOrNo.YES)) {
            datesUnavailableList = caseData.getResp1MediationAvailability().getUnavailableDatesForMediation();
        }
        return datesUnavailableList;
    }

    private String getRespondent2MediationFirstName(CaseData caseData) {
        String mediationFirstname = caseData.getRespondent2().getPartyName();
        if (caseData.getResp2MediationContactInfo() != null
            && caseData.getResp2MediationContactInfo().getFirstName() != null) {
            mediationFirstname = caseData.getResp2MediationContactInfo().getFirstName();
        }
        return mediationFirstname;
    }

    private String getRespondent2MediationLastName(CaseData caseData) {
        String mediationLastName = caseData.getRespondent2().getPartyName();
        if (caseData.getResp2MediationContactInfo() != null
            && caseData.getResp2MediationContactInfo().getLastName() != null) {
            mediationLastName = caseData.getResp2MediationContactInfo().getLastName();
        }
        return mediationLastName;
    }

    private String getRespondent2MediationContactNumber(CaseData caseData) {
        String mediationContactNumber = caseData.getRespondent2().getPartyPhone();
        if (caseData.getResp2MediationContactInfo() != null
            && caseData.getResp2MediationContactInfo().getTelephoneNumber() != null) {
            mediationContactNumber = caseData.getResp2MediationContactInfo().getTelephoneNumber();
        }
        return mediationContactNumber;
    }

    private String getRespondent2MediationEmail(CaseData caseData) {
        String mediationEmail = caseData.getRespondent2().getPartyEmail();
        if (caseData.getResp2MediationContactInfo() != null
            && caseData.getResp2MediationContactInfo().getEmailAddress() != null) {
            mediationEmail = caseData.getResp2MediationContactInfo().getEmailAddress();
        }
        return mediationEmail;
    }

    private boolean checkRespondent2MediationHasUnavailabilityDates(CaseData caseData) {
        return caseData.getResp2MediationAvailability() != null
            && caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists() != null
            && caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists().equals(YesOrNo.YES);
    }

    private List<Element<UnavailableDate>> getRespondent2FromDateUnavailableList(CaseData caseData) {
        List<Element<UnavailableDate>> datesUnavailableList = null;
        if (caseData.getResp2MediationAvailability() != null
            && caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists() != null
            && caseData.getResp2MediationAvailability().getIsMediationUnavailablityExists().equals(YesOrNo.YES)) {
            datesUnavailableList = caseData.getResp2MediationAvailability().getUnavailableDatesForMediation();
        }
        return datesUnavailableList;
    }

    public CaseDocument generate(CaseData caseData, String authorization) {
        SealedClaimResponseFormForSpec templateData = getTemplateData(caseData, authorization);
        DocmosisTemplates docmosisTemplate = getTemplate(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData, docmosisTemplate
        );
        String fileName = String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());

        return documentManagementService.uploadDocument(
            authorization,
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }

    private DocmosisTemplates getTemplate(CaseData caseData) {
        if (caseData.getRespondent2() != null && YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
            // when respondent 2 is present and the responses match use 1v2 LR admission bulk
            return DEFENDANT_RESPONSE_SPEC_SEALED_1V2_LR_ADMISSION_BULK;
        }
        // otherwise use the 1v1 LR admission bulk template
        return getDocmosisTemplateForSingleParty();
    }

    private DocmosisTemplates getDocmosisTemplateForSingleParty() {
        return DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS_LR_ADMISSION_BULK;
    }
}
