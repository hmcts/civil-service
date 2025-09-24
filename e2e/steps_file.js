  // in this file you can append custom step methods to 'I' object

const output = require('codeceptjs').output;

const config = require('./config.js');
const parties = require('./helpers/party.js');
const loginPage = require('./pages/login.page');
const continuePage = require('./pages/continuePage.page');
const caseViewPage = require('./pages/caseView.page');
const stayAndLiftCasePage = require('./pages/stayAndLiftCase/stayAndLiftCase.page');
const createCasePage = require('./pages/createClaim/createCase.page');
const solicitorReferencesPage = require('./pages/createClaim/solicitorReferences.page');
const claimantSolicitorOrganisation = require('./pages/createClaim/claimantSolicitorOrganisation.page');
const claimantSolicitorOrganisationLRspec = require('./pages/createClaim/claimantSolicitorOrganisationLRspec.page');
const claimantSolicitorServiceAddress = require('./pages/createClaim/claimantSolicitorServiceAddress.page');
const addAnotherClaimant = require('./pages/createClaim/addAnotherClaimant.page');
const claimantSolicitorIdamDetailsPage = require('./pages/createClaim/idamEmail.page');
const defendantSolicitorOrganisation = require('./pages/createClaim/defendantSolicitorOrganisation.page');
const defendantSolicitorOrganisationLRspec = require('./pages/createClaim/defendantSolicitorOrganisationLRspec.page');
const defendantSolicitorServiceAddress = require('./pages/createClaim/defendantSolicitorServiceAddress.page');
const secondDefendantSolicitorServiceAddress = require('./pages/createClaim/secondDefendantSolicitorServiceAddress.page');
const defendantSolicitorEmail = require('./pages/createClaim/defendantSolicitorEmail.page');
const chooseCourtPage = require('./pages/createClaim/chooseCourt.page');
const claimantLitigationDetails = require('./pages/createClaim/claimantLitigationDetails.page');
const addAnotherDefendant = require('./pages/createClaim/addAnotherDefendant.page');
const respondent2SameLegalRepresentative = require('./pages/createClaim/respondent2SameLegalRepresentative.page');
const secondDefendantSolicitorReference = require('./pages/createClaim/secondDefendantSolicitorReference.page');
const claimTypePage = require('./pages/createClaim/claimType.page');
const respondentRepresentedPage = require('./pages/createClaim/isRespondentRepresented.page');
const personalInjuryTypePage = require('./pages/createClaim/personalInjuryType.page');
const detailsOfClaimPage = require('./pages/createClaim/detailsOfClaim.page');
const uploadParticularsOfClaimQuestion = require('./pages/createClaim/uploadParticularsOfClaimQuestion.page');
const uploadParticularsOfClaim = require('./pages/createClaim/uploadParticularsOfClaim.page');
const claimValuePage = require('./pages/createClaim/claimValue.page');
const pbaNumberPage = require('./pages/createClaim/pbaNumber.page');
const paymentReferencePage = require('./pages/createClaim/paymentReference.page');

const selectDefendantSolicitorToNotifyPage = require('./pages/notifyClaim/selectDefendantSolicitorToNotify.page');
const cosNotifyClaimPage = require('./pages/notifyClaim/certificateOfServiceNotifyClaim.page');
const cosNotifyClaimDetailsPage = require('./pages/notifyClaimDetails/certificateOfServiceNotifyClaimDetails.page');

const cosNotifyClaimCYAPage = require('./pages/cosNotifyClaimCYA.page');
const cosTab = require('./pages/cosTab.page');
const bundlesTab = require('./pages/bundlesTab.page');
const queriesTab = require('./pages/queriesTab.page');


const selectDefendantSolicitorPage = require('./pages/notifyClaimDetails/selectDefendantSolicitor.page');
const unspecifiedSelectCaseNote = require('./pages/addCaseNotes/selectCaseNote.js');
const unspecifiedAddDocumentAndNotes = require('./pages/addCaseNotes/addDocumentAndNotes.js');

const responseIntentionPage = require('./pages/acknowledgeClaim/responseIntention.page');

const caseProceedsInCasemanPage = require('./pages/caseProceedsInCaseman/caseProceedsInCaseman.page');
const takeCaseOffline = require('./pages/caseProceedsInCaseman/takeCaseOffline.page');

const responseTypePage = require('./pages/respondToClaim/responseType.page');
const uploadResponsePage = require('./pages/respondToClaim/uploadResponseDocument.page');

const proceedPage = require('./pages/respondToDefence/proceed.page');
const raiseQueryPage = require('./pages/query/raiseQuery.page');
const raiseAQueryFormPage = require('./pages/query/raiseAQueryForm.page');


const uploadResponseDocumentPage = require('./pages/respondToDefence/uploadResponseDocument.page');

const defendantLitigationFriendPage = require('./pages/addDefendantLitigationFriend/defendantLitigationDetails.page');

const statementOfTruth = require('./fragments/statementOfTruth');
const party = require('./fragments/party');
const event = require('./fragments/event');
const respondentDetails = require('./fragments/respondentDetails.page');
const confirmDetailsPage = require('./fragments/confirmDetails.page');
const singleResponse = require('./fragments/singleResponse.page');

const unRegisteredDefendantSolicitorOrganisationPage = require('./pages/createClaim/unRegisteredDefendantSolicitorOrganisation.page');
const sumOfDamagesToBeDecidedPage = require('./pages/selectSDO/sumOfDamagesToBeDecided.page');
const allocateSmallClaimsTrackPage = require('./pages/selectSDO/allocateSmallClaimsTrack.page');
const allocateClaimPage = require('./pages/selectSDO/allocateClaimType.page');
const sdoOrderTypePage = require('./pages/selectSDO/sdoOrderType.page');
const smallClaimsSDOOrderDetailsPage = require('./pages/selectSDO/unspecClaimsSDOOrderDetails.page');
const orderTrackAllocationPage = require('./pages/directionsOrder/orderTrackAllocation.page');
const intermediateTrackComplexityBandPage = require('./pages/directionsOrder/intermediateTrackComplexityBand.page');
const selectOrderTemplatePage = require('./pages/directionsOrder/selectOrderTemplate.page');
const downloadOrderTemplatePage = require('./pages/directionsOrder/downloadOrderTemplate.page');
const uploadOrderPage = require('./pages/directionsOrder/uploadOrder.page');

const requestNewHearingPage = require('./pages/hearing/requestHearing.page');
const updateHearingPage = require('./pages/hearing/updateHearing.page');
const cancelHearingPage = require('./pages/hearing/cancelHearing.page');

// DQ fragments
const fileDirectionsQuestionnairePage = require('./fragments/dq/fileDirectionsQuestionnaire.page');
const fixedRecoverableCostsPage = require('./fragments/dq/fixedRecoverableCosts.page');
const disclosureOfElectronicDocumentsPage = require('./fragments/dq/disclosureOfElectrionicDocuments.page');
const disclosureOfNonElectronicDocumentsPage = require('./fragments/dq/disclosureOfNonElectrionicDocuments.page');
const expertsPage = require('./fragments/dq/experts.page');
const witnessPage = require('./fragments/dq/witnesses.page');
const hearingPage = require('./fragments/dq/hearing.page');
const draftDirectionsPage = require('./fragments/dq/draftDirections.page');
const requestedCourtPage = require('./fragments/dq/requestedCourt.page');
const hearingSupportRequirementsPage = require('./fragments/dq/hearingSupportRequirements.page');
const vulnerabilityQuestionsPage = require('./fragments/dq/vulnerabilityQuestions.page');
const furtherInformationPage = require('./fragments/dq/furtherInformation.page');
const welshLanguageRequirementsPage = require('./fragments/dq/language.page');
const address = require('./fixtures/address.js');
const specCreateCasePage = require('./pages/createClaim/createCaseLRspec.page');
const specPartyDetails = require('./fragments/claimantDetailsLRspec');
const specParty = require('./fragments/partyLRspec');
const specClaimantLRPostalAddress = require('./fixtures/claimantLRPostalAddressLRspec');
const specRespondentRepresentedPage = require('./pages/createClaim/isRespondentRepresentedLRspec.page');
const specDefendantSolicitorEmailPage = require('./pages/createClaim/defendantSolicitorEmailLRspec.page');
const specDefendantLRPostalAddress = require('./fixtures/defendantLRPostalAddressLRspec');
const specTimelinePage = require('./pages/createClaim/claimTimelineLRspec.page');
const specAddTimelinePage = require('./pages/createClaim/addTimelineLRspec.page');
const specListEvidencePage = require('./pages/createClaim/claimListEvidenceLRspec.page');
const specClaimAmountPage = require('./pages/createClaim/claimAmountLRspec.page');
const specInterestPage = require('./pages/createClaim/interestLRspec.page');
const specInterestValuePage = require('./pages/createClaim/interestValueLRspec.page');
const specInterestRatePage = require('./pages/createClaim/interestRateLRspec.page');
const specInterestDateStartPage = require('./pages/createClaim/interestDateStartLRspec.page');
const specInterestDateEndPage = require('./pages/createClaim/interestDateEndLRspec.page');
const specConfirmDefendantsDetails = require('./fragments/confirmDefendantsDetailsLRspec');
const specConfirmLegalRepDetails = require('./fragments/confirmLegalRepDetailsLRspec');
const responseTypeSpecPage = require('./pages/respondToClaimLRspec/responseTypeLRspec.page');
const defenceTypePage = require('./pages/respondToClaimLRspec/defenceTypeLRspec.page');
const freeMediationPage = require('./pages/respondToClaimLRspec/freeMediationLRspec.page');
const chooseCourtSpecPage = require('./pages/respondToClaimLRspec/chooseCourtLRspec.page');
const smallClaimsHearingPage = require('./pages/respondToClaimLRspec/hearingSmallClaimsLRspec.page');
const useExpertPage = require('./pages/respondToClaimLRspec/useExpertLRspec.page');
const respondentCheckListPage = require('./pages/respondToClaimLRspec/respondentCheckListLRspec.page');
const enterWitnessesPage = require('./pages/respondToClaimLRspec/enterWitnessesLRspec.page');
const disputeClaimDetailsPage = require('./pages/respondToClaimLRspec/disputeClaimDetailsLRspec.page');
const claimResponseTimelineLRspecPage = require('./pages/respondToClaimLRspec/claimResponseTimelineLRspec.page');
const hearingLRspecPage = require('./pages/respondToClaimLRspec/hearingLRspec.page');
const furtherInformationLRspecPage = require('./pages/respondToClaimLRspec/furtherInformationLRspec.page');
const disclosureReportPage = require('./fragments/dq/disclosureReport.page');
const hearingNoticeListPage = require('./pages/caseProgression/hearingNoticeList.page');
const hearingNoticeListTypePage = require('./pages/caseProgression/hearingNoticeListingType.page');
const hearingScheduledChooseDetailsPage = require('./pages/caseProgression/hearingScheduledChooseDetails.page');
const hearingScheduledMoreInfoPage = require('./pages/caseProgression/hearingScheduledMoreInfo.page');
const confirmTrialReadinessPage = require('./pages/caseProgression/confirmTrialReadiness.page');

const transferCaseOnline = require('./pages/transferOnlineCase/newHearingCentreLocation.page');

const selectLitigationFriendPage = require('./pages/selectLitigationFriend/selectLitigationFriend.page.ts');
const unspecifiedDefaultJudmentPage = require('./pages/defaultJudgment/requestDefaultJudgmentforUnspecifiedClaims');
const unspecifiedEvidenceUpload = require('./pages/evidenceUpload/uploadDocument');
const specifiedDefaultJudmentPage = require('./pages/defaultJudgment/requestDefaultJudgmentforSpecifiedClaims');

const addUnavailableDatesPage = require('./pages/addUnavailableDates/unavailableDates.page');

const createCaseFlagPage = require('./pages/caseFlags/createCaseFlags.page');
const manageCaseFlagsPage = require('./pages/caseFlags/manageCaseFlags.page');
const noticeOfChange = require('./pages/noticeOfChange.page');
const partySelection = require('./pages/manageContactInformation/partySelection.page');
const manageWitnesses = require('./pages/manageContactInformation/manageWitnesses.page');
const manageOrganisationIndividuals = require('./pages/manageContactInformation/manageOrganisationIndividuals.page');
const manageLitigationFriend = require('./pages/manageContactInformation/manageLitigationFriend.page');
const manageDefendant1 = require('./pages/manageContactInformation/manageDefendant1.page');
const { waitForFinishedBusinessProcess } = require('./api/testingSupport.js');
const events = require('./fixtures/ccd/events.js');
//const serviceRequest = require('./pages/createClaim/serviceRequest.page');

const SIGNED_IN_SELECTOR = 'exui-header';
const SIGNED_OUT_SELECTOR = '#global-header';
const CASE_HEADER = 'ccd-markdown >> h1';
const SUMMARY_TAB = 'div[role=\'tab\'] >> \'Summary\'';

const TEST_FILE_PATH = './e2e/fixtures/examplePDF.pdf';
const TEST_FILE_PATH_DOC = './e2e/fixtures/exampleDOC.docx';
const CLAIMANT_NAME = 'Test Inc';
const DEFENDANT1_NAME = 'Sir John Doe';
const DEFENDANT2_NAME = 'Dr Foo Bar';


const CONFIRMATION_MESSAGE = {
  online:  'Please now pay your claim fee\nusing the link below',
  offline: 'Your claim has been received and will progress offline',
};

let caseId, screenshotNumber, eventName, currentEventName, loggedInUser;
let eventNumber = 0;

const isTestEnv = ['preview', 'demo'].includes(config.runningEnv);

const getScreenshotName = () => eventNumber + '.' + screenshotNumber + '.' + eventName.split(' ').join('_') + '.jpg';
const conditionalSteps = (condition, steps) => condition ? steps : [];

const firstClaimantSteps = () => [
  () => party.enterParty(parties.APPLICANT_SOLICITOR_1, address),
  () => claimantLitigationDetails.enterLitigantFriend(parties.APPLICANT_SOLICITOR_1, false, TEST_FILE_PATH),
  () => claimantSolicitorIdamDetailsPage.enterUserEmail(),
  () => claimantSolicitorOrganisation.enterOrganisationDetails(),
  () => claimantSolicitorServiceAddress.enterOrganisationServiceAddress()
];
const secondClaimantSteps = (claimant2) => [
  () => addAnotherClaimant.enterAddAnotherClaimant(claimant2),
  ...conditionalSteps(claimant2, [
    () => party.enterParty(parties.APPLICANT_SOLICITOR_2, address),
    () => claimantLitigationDetails.enterLitigantFriend(parties.APPLICANT_SOLICITOR_2, false, TEST_FILE_PATH),]
  )
];
const firstDefendantSteps = (respondent1) => [
  () => party.enterParty(parties.RESPONDENT_SOLICITOR_1, address),
  () => respondentRepresentedPage.enterRespondentRepresented(parties.RESPONDENT_SOLICITOR_1, respondent1.represented),
  ...conditionalSteps(respondent1.represented, [
    () => defendantSolicitorOrganisation.enterOrganisationDetails('1', respondent1.representativeOrgNumber),
    ...conditionalSteps(!respondent1.representativeRegistered, [
      () => unRegisteredDefendantSolicitorOrganisationPage.enterDefendantSolicitorDetails('1')
    ]),
    ...conditionalSteps(respondent1.representativeRegistered, [
      () => defendantSolicitorServiceAddress.enterOrganisationServiceAddress(),
      () => defendantSolicitorEmail.enterSolicitorEmail('1')
    ]),
  ]),
];
const secondDefendantSteps = (respondent2, respondent1Represented, twoVOneScenario = false) => [
  ...conditionalSteps(!twoVOneScenario, [
    () => addAnotherDefendant.enterAddAnotherDefendant(!!respondent2)
  ]),
  ...conditionalSteps(respondent2, [
    () => party.enterParty('respondent2', address),
    () => respondentRepresentedPage.enterRespondentRepresented(parties.RESPONDENT_SOLICITOR_2, respondent2.represented),
    ...conditionalSteps(respondent2 && respondent2.represented, [
      ...conditionalSteps(respondent1Represented, [
        () => respondent2SameLegalRepresentative.enterRespondent2SameLegalRepresentative(respondent2.sameLegalRepresentativeAsRespondent1),
      ]),
      ...conditionalSteps(respondent2 && !respondent2.sameLegalRepresentativeAsRespondent1, [
        () => defendantSolicitorOrganisation.enterOrganisationDetails('2',
          respondent2.representativeOrgNumber),
        () => secondDefendantSolicitorServiceAddress.enterOrganisationServiceAddress(),
        () => secondDefendantSolicitorReference.enterReference(),
        () => defendantSolicitorEmail.enterSolicitorEmail('2')
      ])
    ])
  ])
];

const defenceSteps = ({party, twoDefendants = false, sameResponse = false, defendant1Response, defendant2Response, defendant1ResponseToApplicant2}) =>
  [() => respondentDetails.verifyDetails(
    defendant1Response ? parties.RESPONDENT_SOLICITOR_1 : null,
    defendant2Response ? parties.RESPONDENT_SOLICITOR_2 : null),
    ...conditionalSteps(twoDefendants, [
      () => singleResponse.defendantsHaveSameResponse(sameResponse),
    ]),
    () => responseTypePage.selectResponseType({defendant1Response, defendant2Response, defendant1ResponseToApplicant2}),
    () => confirmDetailsPage.confirmReferences(defendant1Response, defendant2Response, sameResponse),
    ...conditionalSteps(['partAdmission', 'fullDefence'].includes(defendant1Response) || ['partAdmission', 'fullDefence'].includes(defendant2Response), [
      () => uploadResponsePage.uploadResponseDocuments(party, TEST_FILE_PATH)
    ])
  ];

module.exports = function () {
  return actor({

    fields: {
      pbaNumber: {
        id: '#pbaAccountNumber',
        options: {
          activeAccount1: 'PBA0088192',
          activeAccount2: 'PBA0078095'
        }
      },
      reviewLinks: '.govuk-table__body td a'
    },

    navigateToRefundsList: async function (user) {
      await this.login(user);
      this.amOnPage(config.url.manageCase + '/refunds');
      this.waitForInvisible('.spinner-container', 60);
    },


    navigateToServiceRequest: async function (user, caseId) {
      await this.login(user);
      this.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
      //await this.forceClick(locate('div.mat-tab-label-content').withText('Service Request'));
      let urlBefore = await this.grabCurrentUrl();
      console.log('openServiceRequestTab urlBefore ..', urlBefore);
      this.refreshPage();
      this.waitForVisible(locate('div.mat-tab-label-content').withText('Service Request'), 60);

      await this.retryUntilUrlChanges(async () => {
        this.forceClick(locate('div.mat-tab-label-content').withText('Service Request'));
        this.waitForInvisible('.spinner-container', 60);
      }, urlBefore);
    },

    // Define custom steps here, use 'this' to access default methods of I.
    // It is recommended to place a general 'login' function here.
    async login(user) {
        if (loggedInUser !== user) {
          if (await this.hasSelector(SIGNED_IN_SELECTOR)) {
            await this.signOut();
          }
        }
        await this.retryUntilExists(async () => {
          this.amOnPage(config.url.manageCase, 90);

          if (!config.idamStub.enabled || config.idamStub.enabled === 'false') {
            console.log(`Signing in user: ${user.type}`);
            await loginPage.signIn(user);
          }
          await this.waitForSelector(SIGNED_IN_SELECTOR);
        }, SIGNED_IN_SELECTOR);

        loggedInUser = user;
        console.log('Logged in user..', loggedInUser);
    },

    grabCaseNumber: async function () {
      this.waitForElement(CASE_HEADER);
      const caseHeader = await this.grabTextFrom(CASE_HEADER);
      return caseHeader.split(' ')[0].split('-').join('').substring(1);
    },

    async signOut() {
      await this.retryUntilExists(() => {
        this.click('Sign out');
      }, SIGNED_OUT_SELECTOR);
    },

    async takeScreenshot() {
      if (currentEventName !== eventName) {
        currentEventName = eventName;
        eventNumber++;
        screenshotNumber = 0;
      }
      screenshotNumber++;
      await this.saveScreenshot(getScreenshotName(), true);
    },

    triggerStepsWithScreenshot: async function (steps) {
      for (let i = 0; i < steps.length; i++) {
        //commenting this out, this will give us few minutes back
        /*try {
          await this.takeScreenshot();
        } catch {
          output.log(`Error taking screenshot: ${getScreenshotName()}`);
        }*/
        await steps[i]();
      }
    },

    async createCase(claimant1, claimant2, respondent1, respondent2, claimValue = 30000) {
      eventName = 'Create case';

      const twoVOneScenario = claimant1 && claimant2;
      await createCasePage.createCase(config.definition.jurisdiction);

      let steps = [
        () => continuePage.continue(),
        () => solicitorReferencesPage.enterReferences(),
        () => chooseCourtPage.selectCourt(),
        ...firstClaimantSteps(),
        ...secondClaimantSteps(claimant2),
        ...firstDefendantSteps(respondent1),
        ...secondDefendantSteps(respondent2, respondent1.represented, twoVOneScenario),
        () => claimTypePage.selectClaimType(),
        () => personalInjuryTypePage.selectPersonalInjuryType(),
        () => detailsOfClaimPage.enterDetailsOfClaim(),
        () => uploadParticularsOfClaimQuestion.chooseYesUploadParticularsOfClaim(),
        () => config.runningEnv !== 'aat' ? uploadParticularsOfClaim.upload(TEST_FILE_PATH)
          : uploadParticularsOfClaim.enterParticularsOfClaim(), //Uploading files to aat is causing loading on page to timeout.
        () => claimValuePage.enterClaimValue(claimValue),
        () => pbaNumberPage.clickContinue(),
        () => statementOfTruth.enterNameAndRole('claim'),
        () => event.submit('Submit', CONFIRMATION_MESSAGE.online),
        () => event.returnToCaseDetails(),
      ];

      await this.triggerStepsWithScreenshot(steps);

      caseId = await this.grabCaseNumber();
      await waitForFinishedBusinessProcess(caseId);
    },

    async checkForCaseFlagsEvent() {
      eventName = 'Create case flags';
      const eventNames = ['Create case flags', 'Manage case flags'];

      await this.triggerStepsWithScreenshot([
          () => caseViewPage.assertEventsAvailable(eventNames),
      ]);
    },

    async notifyClaim(solicitorToNotify) {
      eventName = events.NOTIFY_DEFENDANT_OF_CLAIM.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.NOTIFY_DEFENDANT_OF_CLAIM, caseId),
        ...conditionalSteps(!!solicitorToNotify, [
          () => selectDefendantSolicitorToNotifyPage.selectSolicitorToNotify(solicitorToNotify),
        ]),
        () => continuePage.continue(),
        () => event.submit('Submit', 'Notification of claim sent'),
        () => event.returnToCaseDetails()
      ]);
    },

    async notifyClaimDetails(solicitorToNotify) {
      eventName = events.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS, caseId),
        ...conditionalSteps(!!solicitorToNotify, [
          () => selectDefendantSolicitorPage.selectSolicitorToNotify(solicitorToNotify),
        ]),
        () => continuePage.continue(),
        () => event.submit('Submit', 'Defendant notified'),
        () => event.returnToCaseDetails()
      ]);
    },

    async initiateDJUnspec(caseNumber, scenario) {
      eventName = events.DEFAULT_JUDGEMENT.name;
      caseId = caseNumber;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.DEFAULT_JUDGEMENT, caseId),
        () => unspecifiedDefaultJudmentPage.againstWhichDefendant(scenario),
        () => unspecifiedDefaultJudmentPage.statementToCertify(scenario),
        () => unspecifiedDefaultJudmentPage.hearingSelection(),
        () => unspecifiedDefaultJudmentPage.hearingRequirements(),
        () => event.submit('Submit', 'Judgment for damages to be decided Granted'),
        () => event.returnToCaseDetails()
      ]);
    },

    async initiateDJSpec(caseId, scenario, caseCategory = 'UNSPEC') {
      eventName = events.DEFAULT_JUDGEMENT_SPEC.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.DEFAULT_JUDGEMENT_SPEC, caseId),
        () => specifiedDefaultJudmentPage.againstWhichDefendant(scenario),
        () => specifiedDefaultJudmentPage.statementToCertify(scenario),
        () => specifiedDefaultJudmentPage.hasDefendantMadePartialPayment(),
        ...conditionalSteps(caseCategory === 'SPEC', [
          () => specifiedDefaultJudmentPage.claimForFixedCostsOnEntry()
        ]),
        ...conditionalSteps(caseCategory === 'UNSPEC', [
          () => specifiedDefaultJudmentPage.claimForFixedCosts()
        ]),
        () => specifiedDefaultJudmentPage.repaymentSummary(),
        () => specifiedDefaultJudmentPage.paymentTypeSelection(),
        () => event.submit('Submit', 'Default Judgment Granted'),
        () => event.returnToCaseDetails()
      ]);
    },

    async judgePerformDJDirectionOrder() {
      eventName = events.STANDARD_DIRECTION_ORDER_DJ.name;
      await this.triggerStepsWithScreenshot([
        () => unspecifiedDefaultJudmentPage.selectCaseManagementOrder('DisposalHearing'),
        () => unspecifiedDefaultJudmentPage.selectOrderAndHearingDetailsForDJTask('DisposalHearing'),
        () => unspecifiedDefaultJudmentPage.verifyOrderPreview(),
        () => event.submit('Submit', 'Your order has been issued')
      ]);
    },

    async judgeAddsCaseNotes() {
      eventName = events.EVIDENCE_UPLOAD_JUDGE.name;
      await this.triggerStepsWithScreenshot([
        () => unspecifiedSelectCaseNote.selectCaseNotes(),
        () => unspecifiedAddDocumentAndNotes.addDocumentAndNotes(TEST_FILE_PATH),
        () => event.submit('Submit', 'Document uploaded and note added')
      ]);
    },

    async staffPerformDJCaseTransferCaseOffline(caseId) {
      eventName = events.TAKE_CASE_OFFLINE.name;
      await this.triggerStepsWithScreenshot([
        () => unspecifiedDefaultJudmentPage.performAndVerifyTransferCaseOffline(caseId)
      ]);
    },

    async acknowledgeClaim(respondent1Intention, respondent2Intention, respondent1ClaimIntentionApplicant2, sameSolicitor = false) {
      eventName = events.ACKNOWLEDGE_CLAIM.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.ACKNOWLEDGE_CLAIM, caseId),
        () => respondentDetails.verifyDetails(),
        () => responseIntentionPage.selectResponseIntention(respondent1Intention, respondent2Intention, respondent1ClaimIntentionApplicant2),
        () => confirmDetailsPage.confirmReferences(!!respondent1Intention, !!respondent2Intention, sameSolicitor),
        // temporarily commenting out whilst change is Fmade to service repo
        () => event.submit('Acknowledge claim', ''),
        () => event.returnToCaseDetails()
      ]);
    },

    async informAgreedExtensionDate() {
      eventName = events.INFORM_AGREED_EXTENSION_DATE.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.INFORM_AGREED_EXTENSION_DATE, caseId),
        () => event.submit('Submit', 'Extension deadline submitted'),
        () => event.returnToCaseDetails()
      ]);
    },

    async createHearingScheduled() {
      eventName = events.HEARING_SCHEDULED.name;
      await this.triggerStepsWithScreenshot([
        () => hearingNoticeListPage.hearingType('fastTrack'),
        () => hearingNoticeListTypePage.listingOrRelistingSelect('Listing'),
        () => hearingScheduledChooseDetailsPage.selectCourt(),
        () => hearingScheduledMoreInfoPage.enterMoreInfo(),
        () => event.submit('Submit', ''),
        () => event.returnToCaseDetails()
      ]);
    },

    async confirmTrialReadiness(user, hearingDateIsLessThan3Weeks = false, readyForTrial = 'yes') {
      eventName = events.TRIAL_READINESS.name;
      const confirmationMessage = readyForTrial == 'yes' ? 'You have said this case is ready for trial or hearing' : 'You have said this case is not ready for trial or hearing';
      await this.triggerStepsWithScreenshot([
        ...conditionalSteps(hearingDateIsLessThan3Weeks == false, [
          () => caseViewPage.startEvent(events.TRIAL_READINESS, caseId),
          () => confirmTrialReadinessPage.updateTrialConfirmation(user, readyForTrial, 'yes'),
          () => event.submit('Submit', confirmationMessage),
          () => event.returnToCaseDetails()
        ]),
        ...conditionalSteps(hearingDateIsLessThan3Weeks == true, [
          () => caseViewPage.verifyErrorMessageOnEvent(eventName, caseId, 'Trial arrangements had to be confirmed more than 3 weeks before the trial')
        ])
      ]);
    },

    async addDefendantLitigationFriend(partyType, selectPartyType = true) {
      eventName = events.ADD_DEFENDANT_LITIGATION_FRIEND.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.ADD_DEFENDANT_LITIGATION_FRIEND, caseId),
        ...conditionalSteps(selectPartyType && partyType, [
            () => selectLitigationFriendPage.selectDefendant(partyType)
          ]),
          () => defendantLitigationFriendPage.enterLitigantFriendWithDifferentAddressToDefendant(partyType, address, TEST_FILE_PATH),
          () => event.submit('Submit', 'You have added litigation friend details'),
          () => event.returnToCaseDetails()
      ]);
    },

    async respondToClaim({party = parties.RESPONDENT_SOLICITOR_1, twoDefendants = false, sameResponse = false, defendant1Response, defendant2Response, defendant1ResponseToApplicant2, claimValue = 25000}) {
      eventName = events.DEFENDANT_RESPONSE.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.DEFENDANT_RESPONSE, caseId),
        ...defenceSteps({party, twoDefendants, sameResponse, defendant1Response, defendant2Response, defendant1ResponseToApplicant2}),
        ...conditionalSteps(defendant1Response === 'fullDefence' || defendant2Response === 'fullDefence', [
          ...conditionalSteps(claimValue >= 10000, [
            () => fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(party),
            () => fixedRecoverableCostsPage.fixedRecoverableCosts(party),
          ]),
          ...conditionalSteps(claimValue > 25000, [
            () => disclosureOfElectronicDocumentsPage.enterDisclosureOfElectronicDocuments(party)
            ]
          ),
          ...conditionalSteps(claimValue >= 10000, [
            () => disclosureOfNonElectronicDocumentsPage.enterDirectionsProposedForDisclosure(party),
            ]
          ),
          () => expertsPage.enterExpertInformation(party),
          () => witnessPage.enterWitnessInformation(party),
          () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(party),
          () => hearingPage.enterHearingInformation(party),
          () => draftDirectionsPage.upload(party, TEST_FILE_PATH),
          () => requestedCourtPage.selectSpecificCourtForHearing(party),
          () => hearingSupportRequirementsPage.selectRequirements(party),
          () => vulnerabilityQuestionsPage.vulnerabilityQuestions(party),
          () => furtherInformationPage.enterFurtherInformation(party),
          () => statementOfTruth.enterNameAndRole(party + 'DQ'),
        ]),
        () => event.submit('Submit', ''),
        () => event.returnToCaseDetails()
      ]);
    },

    async respondToDefence(mpScenario = 'ONE_V_ONE', claimValue = 30000) {
      eventName = events.CLAIMANT_RESPONSE.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.CLAIMANT_RESPONSE, caseId),
        () => proceedPage.proceedWithClaim(mpScenario),
        () => uploadResponseDocumentPage.uploadResponseDocuments(TEST_FILE_PATH, mpScenario),
        ...conditionalSteps(claimValue >= 10000, [
          () => fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.APPLICANT_SOLICITOR_1),
          () => fixedRecoverableCostsPage.fixedRecoverableCosts(parties.APPLICANT_SOLICITOR_1),
        ]),
        ...conditionalSteps(claimValue > 25000, [
            () => disclosureOfElectronicDocumentsPage.
                            enterDisclosureOfElectronicDocuments(parties.APPLICANT_SOLICITOR_1)
          ]
        ),
        ...conditionalSteps(claimValue >= 10000, [
          () => disclosureOfNonElectronicDocumentsPage.enterDirectionsProposedForDisclosure(parties.APPLICANT_SOLICITOR_1),
        ]),
        () => expertsPage.enterExpertInformation(parties.APPLICANT_SOLICITOR_1),
        () => witnessPage.enterWitnessInformation(parties.APPLICANT_SOLICITOR_1),
        () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.APPLICANT_SOLICITOR_1),
        () => hearingPage.enterHearingInformation(parties.APPLICANT_SOLICITOR_1),
        () => draftDirectionsPage.upload(parties.APPLICANT_SOLICITOR_1, TEST_FILE_PATH),
        () => hearingSupportRequirementsPage.selectRequirements(parties.APPLICANT_SOLICITOR_1),
        () => vulnerabilityQuestionsPage.vulnerabilityQuestions(parties.APPLICANT_SOLICITOR_1),
        () => furtherInformationPage.enterFurtherInformation(parties.APPLICANT_SOLICITOR_1),
        () => statementOfTruth.enterNameAndRole(parties.APPLICANT_SOLICITOR_1 + 'DQ'),
        () => event.submit('Submit your response', 'You have chosen to proceed with the claim\nClaim number: '),
        () => this.click('Close and Return to case details')
      ]);
      await this.takeScreenshot();
    },

    async raiseNewNonHearingQuery(caseId) {
      eventName = events.QUERY_MANAGEMENT.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.raiseNewQuery(caseId),
        () => raiseQueryPage.selectQuery(),
        () => raiseAQueryFormPage.enterQueryDetails(),
        () => event.submitAndGoBackToCase('Submit', 'Query submitted')
      ]);
    },

    async raiseNewHearingQuery(caseId) {
      eventName = events.QUERY_MANAGEMENT.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.raiseNewQuery(caseId),
        () => raiseQueryPage.selectQuery(),
        () => raiseAQueryFormPage.enterHearingQueryDetails(),
        () => event.submitAndGoBackToCase('Submit', 'Query submitted')
      ]);
    },

    async raiseNewQueryInOfflineState(caseId) {
      eventName = events.QUERY_MANAGEMENT.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.raiseNewQuery(caseId),
        () => raiseQueryPage.selectQuery()
      ]);
    },

    async respondToDefenceMinti(caseId, mpScenario = 'ONE_V_ONE', claimValue = 30000) {
      eventName = events.CLAIMANT_RESPONSE_SPEC.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.CLAIMANT_RESPONSE_SPEC, caseId),
        () => proceedPage.proceedWithClaim(mpScenario),
        () => uploadResponseDocumentPage.uploadResponseDocumentsSpec(TEST_FILE_PATH, mpScenario),
        ...conditionalSteps(claimValue > 100000, [
          // Multi: Greater than 100k
          () => fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.APPLICANT_SOLICITOR_1),
        ]),
        ...conditionalSteps(claimValue > 25000 && claimValue <= 100000, [
          // Intermediate: Greater than 25k and less than or equal to 100k
          () => fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.APPLICANT_SOLICITOR_1),
          () => fixedRecoverableCostsPage.fixedRecoverableCostsInt(parties.APPLICANT_SOLICITOR_1),
        ]),
        () => disclosureOfElectronicDocumentsPage.
        enterDisclosureOfElectronicDocuments(parties.SPEC_APPLICANT_SOLICITOR_1),
        // Disclosure of non-electronic documents (Optional)
        () => this.clickContinue(),
        () => disclosureReportPage.enterDisclosureReport(parties.APPLICANT_SOLICITOR_1),
        () => expertsPage.enterExpertInformation(parties.APPLICANT_SOLICITOR_1),
        () => witnessPage.enterWitnessInformation(parties.APPLICANT_SOLICITOR_1),
        () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.APPLICANT_SOLICITOR_1),
        () => hearingPage.enterHearingAvailability(parties.APPLICANT_SOLICITOR_1),
        () => draftDirectionsPage.upload(parties.APPLICANT_SOLICITOR_1, TEST_FILE_PATH),
        () => requestedCourtPage.selectSpecCourtLocation(parties.APPLICANT_SOLICITOR_1),
        () => hearingSupportRequirementsPage.selectRequirements(parties.APPLICANT_SOLICITOR_1),
        () => vulnerabilityQuestionsPage.vulnerabilityQuestions(parties.APPLICANT_SOLICITOR_1),
        () => statementOfTruth.enterNameAndRole(parties.APPLICANT_SOLICITOR_1 + 'DQ'),
        () => event.submit('Submit your response', 'You have decided to proceed with the claim\nClaim number: '),
        () => event.returnToCaseDetails()
      ]);
      await this.takeScreenshot();
    },

    async transferOnlineCase() {
      eventName = events.TRANSFER_ONLINE_CASE.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.TRANSFER_ONLINE_CASE, caseId),
        () => transferCaseOnline.selectCourt(),
        () => this.click('Submit'),
        () => this.click('Close and Return to case details')
      ]);
      await this.takeScreenshot();
    },

    async respondToDefenceDropClaim(mpScenario = 'ONE_V_ONE') {
      eventName = events.CLAIMANT_RESPONSE.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.CLAIMANT_RESPONSE, caseId),
        () => proceedPage.dropClaim(mpScenario),
        () => event.submit('Submit your response', 'You have chosen not to proceed with the claim'),
        () => this.click('Close and Return to case details')
      ]);
      await this.takeScreenshot();
    },

    async fillNotifyClaimCOSForm(caseId, mpScenario) {
      eventName = events.NOTIFY_DEFENDANT_OF_CLAIM.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.NOTIFY_DEFENDANT_OF_CLAIM, caseId),
        () => cosNotifyClaimPage.fillNotifyClaimCOSForm('Certificate of Service [defendant1]', DEFENDANT1_NAME),
        () => cosNotifyClaimPage.fillNotifyClaimCOSForm('Certificate of Service [defendant2]', DEFENDANT2_NAME),
        () => cosNotifyClaimCYAPage.verifyCOSCheckAnswerForm(CLAIMANT_NAME, DEFENDANT1_NAME, DEFENDANT2_NAME, mpScenario),
        () => event.submit('Submit', 'Certificate of Service - notify claim successful'),
        () => event.returnToCaseDetails()
      ]);
    },

    async fillLRNotifyClaimCOSForm(caseId, mpScenario) {
      eventName = events.NOTIFY_DEFENDANT_OF_CLAIM.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.NOTIFY_DEFENDANT_OF_CLAIM, caseId),
        () => this.clickContinue(),
        () => cosNotifyClaimPage.fillNotifyClaimCOSForm('Certificate of Service [defendant2]', DEFENDANT2_NAME),
        () => cosNotifyClaimCYAPage.verifyCOSCheckAnswerForm(CLAIMANT_NAME, '', DEFENDANT2_NAME, mpScenario),
        () => event.submit('Submit', 'Certificate of Service - notify claim successful'),
        () => event.returnToCaseDetails()
      ]);
    },

    async fillNotifyClaimDetailsCOSForm(caseId) {
      eventName = events.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS, caseId),
        () => cosNotifyClaimDetailsPage.fillNotifyClaimDetailsCOSForm('Certificate of Service [defendant1]',
          DEFENDANT1_NAME, 'NotifyClaimDetails1', TEST_FILE_PATH),
        () => cosNotifyClaimDetailsPage.fillNotifyClaimDetailsCOSForm('Certificate of Service [defendant2]',
          DEFENDANT2_NAME, 'NotifyClaimDetails2', TEST_FILE_PATH),
        () => cosNotifyClaimCYAPage.verifyCOSCheckAnswerForm(CLAIMANT_NAME, DEFENDANT1_NAME, DEFENDANT2_NAME),
        () => cosNotifyClaimCYAPage.verifyCOSSupportingEvidence(),
        () => event.submit('Submit', 'Certificate of Service - notify claim details successful'),
        () => event.returnToCaseDetails()
      ]);
    },

    async fillLRNotifyClaimDetailsCOSForm(caseId) {
      eventName = events.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS, caseId),
        () => this.clickContinue(),
        () => cosNotifyClaimDetailsPage.fillNotifyClaimDetailsCOSForm('Certificate of Service [defendant2]',
          DEFENDANT2_NAME, 'NotifyClaimDetails2', TEST_FILE_PATH),
        () => event.submit('Submit', 'Certificate of Service - notify claim details successful'),
        () => event.returnToCaseDetails()
      ]);
    },

    async verifyCOSTabDetails() {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Certificate of Service'),
        () => cosTab.verifyCOSDetails(CLAIMANT_NAME, DEFENDANT1_NAME, DEFENDANT2_NAME)
      ]);
    },

    async verifyBundleDetails(caseNumber) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Bundles'),
        () => bundlesTab.verifyBundleDetails()
      ]);
    },

    async verifyQueriesDetails(hearing = false) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Queries'),
        () => queriesTab.verifyQueriesDetails(hearing)
      ]);
    },

    async raiseFollowUpQuestionAndVerify(party = false) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Queries'),
        () => queriesTab.askFollowUpQuestion(party),
        () => event.submitAndGoBackToCase('Submit', 'Query submitted'),
        () =>caseViewPage.navigateToTab('Queries'),
        () => queriesTab.verifyFollowUpQuestion(party)
      ]);
    },

    async verifyFollowUpQuestionAsCaseWorker(hearing = false) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Queries'),
        () => queriesTab.verifyFollowUpQuestionAsCourtStaff(hearing)
      ]);
    },

    async verifyFollowUpQuestionAsJudge(hearing = false) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Queries'),
        () => queriesTab.verifyFollowUpQuestion(hearing)
      ]);
    },

    async verifyQueriesDetailsAsCaseWorker(hearing = false) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Queries'),
        () => queriesTab.verifyDetailsAsCaseWorker(hearing)
      ]);
    },

    async navigateToTab(tabName) {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab(tabName),
      ]);
    },

    async verifyCOSTabNotifyClaimDetails() {
      await this.triggerStepsWithScreenshot([
        () =>caseViewPage.navigateToTab('Certificate of Service'),
        () => cosTab.verifyCOSNCDetails(CLAIMANT_NAME, DEFENDANT1_NAME, DEFENDANT2_NAME)
      ]);
    },

    async caseProceedsInCaseman(caseId = false) {
      eventName = events.CASE_PROCEEDS_IN_CASEMAN.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.CASE_PROCEEDS_IN_CASEMAN, caseId),
        () => caseProceedsInCasemanPage.enterTransferDate(),
        () => this.waitForSelector(SUMMARY_TAB, 30),
      ]);
      await this.takeScreenshot();
    },

    async addUnavailableDates(caseId) {
      eventName = events.ADD_UNAVAILABLE_DATES.name;
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.ADD_UNAVAILABLE_DATES, caseId),
        () => addUnavailableDatesPage.enterUnavailableDates(),
        () => event.submit('Submit', 'Availability updated'),
        () => event.returnToCaseDetails(),
        () => addUnavailableDatesPage.confirmSubmission(config.url.manageCase + '/cases/case-details/' + caseId + '#Listing%20notes'),
      ]);
    },

    async stayCase(user = config.ctscAdminUser) {
      eventName = events.STAY_CASE.name;
      await this.login(user);
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.STAY_CASE, caseId),
        () => this.waitForText('All parties will be notified.'),
        () => event.submit('Submit', 'All parties have been notified and any upcoming hearings must be cancelled'),
        () => event.returnToCaseDetails(),
      ]);
    },

    async manageStay(manageStayType = 'LIFT_STAY', caseState = 'JUDICIAL_REFERRAL', user = config.ctscAdminUser) {
      eventName = events.MANAGE_STAY.name;
      await this.login(user);
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.MANAGE_STAY, caseId),
      ]);
      if (manageStayType == 'REQ_UPDATE')  {
        await this.triggerStepsWithScreenshot([
          () => stayAndLiftCasePage.verifyReqUpdateSteps(),
          () => event.submit('Submit', 'You have requested an update on'),
          () => this.waitForText('All parties have been notified'),
          () => event.returnToCaseDetails(),
        ]);
      } else {
        await this.triggerStepsWithScreenshot([
          () => stayAndLiftCasePage.verifyLiftCaseStaySteps(caseState),
          () => event.submit('Submit', 'You have lifted the stay from this'),
          () => this.waitForText('All parties have been notified'),
          () => event.returnToCaseDetails(),
        ]);
      }
      await this.waitForText('Summary');
    },

    async initiateFinalOrder(caseId, trackType, optionText) {
      await this.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
      await this.waitForText('Summary', 20);
      await this.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/trigger/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERTrackAllocation');
      await this.waitForText('Make an order', 10);
      await this.triggerStepsWithScreenshot([
        () => orderTrackAllocationPage.allocationTrack('Yes', trackType),
        ... conditionalSteps(trackType === 'Intermediate Track', [
          () => intermediateTrackComplexityBandPage.selectComplexityBand('Yes', 'Band 2', 'Test reason'),
        ]),
        () => selectOrderTemplatePage.selectTemplateByText(trackType, optionText),
        () => downloadOrderTemplatePage.verifyLabelsAndDownload(),
        () => uploadOrderPage.verifyLabelsAndUploadDocument(TEST_FILE_PATH_DOC),
        () => event.submit('Submit', 'Your order has been issued')
      ]);
    },

    async initiateSDO(damages, allocateSmallClaims, trackType, orderType) {
      eventName = events.CREATE_SDO.name;
      await this.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
      await this.waitForText('Summary');
      if (['demo'].includes(config.runningEnv)) {
        await this.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/tasks');
        await this.wait(20); // I've not been able to find a way to wait for the spinner to disappear - tried multiple things ie detach from DOM , wait for element to be clickable
        await this.click('#action_claim');
      }
      await this.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/trigger/CREATE_SDO/CREATE_SDOSDO');
      await this.waitForText('Standard Direction Order');
      await this.triggerStepsWithScreenshot([
        () => sumOfDamagesToBeDecidedPage.damagesToBeDecided(damages),

        ...conditionalSteps(damages, [
          () => allocateSmallClaimsTrackPage.decideSmallClaimsTrack(allocateSmallClaims),
          ...conditionalSteps(!allocateSmallClaims,[
            () => sdoOrderTypePage.decideOrderType(orderType)])
        ]),

        ...conditionalSteps(trackType, [
        () => allocateClaimPage.selectTrackType(trackType)]),

        () => smallClaimsSDOOrderDetailsPage.selectOrderDetails(allocateSmallClaims, trackType, orderType),
        () => smallClaimsSDOOrderDetailsPage.verifyOrderPreview(),
        () => event.submit('Submit', 'Your order has been issued')
      ]);
    },

    async assertNoEventsAvailable() {
      await caseViewPage.assertNoEventsAvailable();
    },

    async assertHasEvents(events) {
      await caseViewPage.assertEventsAvailable(events);
    },

    async clickContinue() {
      let urlBefore = await this.grabCurrentUrl();
      await this.retryUntilUrlChanges(() => this.forceClick('Continue'), urlBefore);
    },
    async clickHearingHyperLinkOrButton(element) {
      let urlBefore = await this.grabCurrentUrl();
      await this.retryUntilUrlChanges(() => this.forceClick(element), urlBefore);
    },

    async getCaseId(){
      console.log(`case created: ${caseId}`);
      return caseId;
    },

    async setCaseId(argCaseNumber) {
      caseId = argCaseNumber;
    },

    /**
     * Retries defined action util element described by the locator is invisible. If element is not invisible
     * after 4 tries (run + 3 retries) this step throws an error. Use cases include checking no error present on page.
     *
     * Warning: action logic should avoid framework steps that stop test execution upon step failure as it will
     *          stop test execution even if there are retries still available. Catching step error does not help.
     *
     * @param action - an action that will be retried until either condition is met or max number of retries is reached
     * @param locator - locator for an element that is expected to be invisible upon successful execution of an action
     * @param maxNumberOfRetries - maximum number to retry the function for before failing
     * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
     */
    async retryUntilInvisible(action, locator, maxNumberOfRetries = 3) {
      for (let tryNumber = 1; tryNumber <= maxNumberOfRetries; tryNumber++) {
        output.log(`retryUntilInvisible(${locator}): starting try #${tryNumber}`);
        await action();

        if (await this.hasSelector(locator) > 0) {
          output.print(`retryUntilInvisible(${locator}): error present after try #${tryNumber} was executed`);
        } else {
          output.log(`retryUntilInvisible(${locator}): error not present after try #${tryNumber} was executed`);
          break;
        }
        if (tryNumber === maxNumberOfRetries) {
          throw new Error(`Maximum number of tries (${maxNumberOfRetries}) has been reached in search for ${locator}`);
        }
      }
    },

    async addAnotherElementToCollection(button = 'Add new') {
      const numberOfElements = await this.grabNumberOfVisibleElements('.collection-title');
      this.click(button);
      this.waitNumberOfVisibleElements('.collection-title', numberOfElements + 1);
    },

    /**
     * Retries defined action util element described by the locator is present. If element is not present
     * after 4 tries (run + 3 retries) this step throws an error.
     *
     * Warning: action logic should avoid framework steps that stop test execution upon step failure as it will
     *          stop test execution even if there are retries still available. Catching step error does not help.
     *
     * @param action - an action that will be retried until either condition is met or max number of retries is reached
     * @param locator - locator for an element that is expected to be present upon successful execution of an action
     * @param maxNumberOfTries - maximum number to retry the function for before failing
     * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
     */
    async retryUntilExists(action, locator, maxNumberOfTries = 3, timeout) {
      for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
        output.log(`retryUntilExists(${locator}): starting try #${tryNumber}`);
        if (tryNumber > 1 && await this.hasSelector(locator)) {
          console.log(`retryUntilExists(${locator}): element found before try #${tryNumber} was executed`);
          break;
        }
        await action();
        if (await this.waitForSelector(locator, timeout) != null) {
          console.log(`retryUntilExists(${locator}): element found after try #${tryNumber} was executed`);
          break;
        } else {
          console.log(`retryUntilExists(${locator}): element not found after try #${tryNumber} was executed`);
        }
        if (tryNumber === maxNumberOfTries) {
          throw new Error(`Maximum number of tries (${maxNumberOfTries}) has been reached in search for ${locator}`);
        }
      }
    },

    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },

    /**
     * Retries defined action util url is changed by given action. If url does not change
     * after 4 tries (run + 3 retries) this step throws an error. If url is already changed, will exit.
     *
     * Warning: action logic should avoid framework steps that stop test execution upon step failure as it will
     *          stop test execution even if there are retries still available. Catching step error does not help.
     *
     * @param action - an action that will be retried until either condition is met or max number of retries is reached
     * @param urlBefore - the url before the action has occurred
     * @param maxNumberOfTries - maximum number to retry the function for before failing
     * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
     */
    async retryUntilUrlChanges(action, urlBefore, maxNumberOfTries = 6) {
      let urlAfter;
      for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
        output.log(`Checking if URL has changed, starting try #${tryNumber}`);
        await action();
        await this.sleep(3000 * tryNumber);
        urlAfter = await this.grabCurrentUrl();
        if (urlBefore !== urlAfter) {
          output.log(`retryUntilUrlChanges(before: ${urlBefore}, after: ${urlAfter}): url changed after try #${tryNumber} was executed`);
          break;
        } else {
          output.print(`retryUntilUrlChanges(before: ${urlBefore}, after: ${urlAfter}): url did not change after try #${tryNumber} was executed`);
        }
        if (tryNumber === maxNumberOfTries) {
          throw new Error(`Maximum number of tries (${maxNumberOfTries}) has been reached trying to change urls. Before: ${urlBefore}. After: ${urlAfter}`);
        }
      }
    },

    async createCaseSpec(applicantType, defendantType, litigantInPerson = false, claimAmount) {
      this.forceClick('Create case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      await this.retryUntilExists(() => specCreateCasePage.selectCaseType(), 'ccd-markdown');
      await this.clickContinue();
      await solicitorReferencesPage.enterReferences();
      await specPartyDetails.enterDetails('applicant1', address, applicantType);
      await claimantSolicitorIdamDetailsPage.enterUserEmail();
      await claimantSolicitorOrganisationLRspec.enterOrganisationDetails();
      await specParty.enterSpecParty('Applicant', specClaimantLRPostalAddress);
      await specPartyDetails.enterDetails('respondent1', address, defendantType);
      if (litigantInPerson) {
         await specRespondentRepresentedPage.enterRespondentRepresented('no');
      } else {
        await specRespondentRepresentedPage.enterRespondentRepresented('yes');
        await defendantSolicitorOrganisationLRspec.enterOrganisationDetails('respondent1');
        await specDefendantSolicitorEmailPage.enterSolicitorEmail();
      }
      await specParty.enterSpecParty('Respondent', specDefendantLRPostalAddress);
      await detailsOfClaimPage.enterDetailsOfClaim();
      await specTimelinePage.addManually();
      await specAddTimelinePage.addTimeline();
      await specListEvidencePage.addEvidence();
      await specClaimAmountPage.addClaimItem(claimAmount);
      await this.clickContinue();
      await specInterestPage.addInterest();
      await specInterestValuePage.selectInterest();
      await specInterestRatePage.selectInterestRate();
      await specInterestDateStartPage.selectInterestDateStart();
      await specInterestDateEndPage.selectInterestDateEnd();
      await this.clickContinue();
      await pbaNumberPage.selectPbaNumber();
      await paymentReferencePage.updatePaymentReference();
      await statementOfTruth.enterNameAndRole('claim');
      let expectedMessage = litigantInPerson ?
        'Your claim has been received and will progress offline' : 'Your claim has been received\nClaim number: ';
      await event.submit('Submit', expectedMessage);
      await event.returnToCaseDetails();
      caseId = await this.grabCaseNumber();
    },

    async acknowledgeClaimSpec() {
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.ACKNOWLEDGEMENT_OF_SERVICE, caseId),
        () => specConfirmDefendantsDetails.confirmDetails(),
        () => specConfirmLegalRepDetails.confirmDetails(),
        () => event.submit('Acknowledge claim', ''),
        () => event.returnToCaseDetails()
      ]);
    },

    async respondToClaimSpec(responseType,defenceType,paidAmount) {
      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.DEFENDANT_RESPONSE_SPEC, caseId),
        () => respondentCheckListPage.claimTimelineTemplate(),
        () => specConfirmDefendantsDetails.confirmDetails(),
        () => specConfirmLegalRepDetails.confirmDetails(),
        () => responseTypeSpecPage.selectResponseType(responseType),
        ... conditionalSteps(responseType === 'fullDefence', [
          () => defenceTypePage.selectDefenceType(defenceType,paidAmount)
        ]),
        ... conditionalSteps(defenceType === 'hasPaid' && paidAmount === 1000, [
          () => freeMediationPage.selectMediation('yes'),
          () => useExpertPage.claimExpert('no'),
          () => enterWitnessesPage.howManyWitnesses(),
          () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.RESPONDENT_SOLICITOR_1),
          () => smallClaimsHearingPage.selectHearing('no'),
          () => chooseCourtSpecPage.chooseCourt('yes'),
        ]),
        ... conditionalSteps(paidAmount < 1000 && (defenceType === 'dispute' || defenceType === 'hasPaid'), [
          () => disputeClaimDetailsPage.enterReasons(),
          () => claimResponseTimelineLRspecPage.addManually(),
          () => this.clickContinue(),
          () => freeMediationPage.selectMediation('yes'),
          () => useExpertPage.claimExpert('no'),
          () => enterWitnessesPage.howManyWitnesses(),
          () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.RESPONDENT_SOLICITOR_1),
          () => smallClaimsHearingPage.selectHearing('no'),
          () => chooseCourtSpecPage.chooseCourt('yes'),
        ]),
        ... conditionalSteps(defenceType === 'hasPaid' && paidAmount === 15000, [
          () => fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.RESPONDENT_SOLICITOR_1),
          () => disclosureOfElectronicDocumentsPage.enterDisclosureOfElectronicDocuments('specRespondent1'),
          () => this.clickContinue(),
          () => disclosureReportPage.enterDisclosureReport(parties.RESPONDENT_SOLICITOR_1),
          () => expertsPage.enterExpertInformation(parties.RESPONDENT_SOLICITOR_1),
          () => witnessPage.enterWitnessInformation(parties.RESPONDENT_SOLICITOR_1),
          () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.RESPONDENT_SOLICITOR_1),
          () => hearingLRspecPage.enterHearing(parties.RESPONDENT_SOLICITOR_1),
          () => chooseCourtSpecPage.chooseCourt('yes'),
        ]),
        ... conditionalSteps(paidAmount === 10000 && (defenceType === 'dispute' || defenceType === 'hasPaid'),  [
          () => disputeClaimDetailsPage.enterReasons(),
          () => claimResponseTimelineLRspecPage.addManually(),
          () => this.clickContinue(),
          () => fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.RESPONDENT_SOLICITOR_1),
          () => disclosureOfElectronicDocumentsPage.enterDisclosureOfElectronicDocuments('specRespondent1'),
          () => this.clickContinue(),
          () => disclosureReportPage.enterDisclosureReport(parties.RESPONDENT_SOLICITOR_1),
          () => expertsPage.enterExpertInformation(parties.RESPONDENT_SOLICITOR_1),
          () => witnessPage.enterWitnessInformation(parties.RESPONDENT_SOLICITOR_1),
          () => welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.RESPONDENT_SOLICITOR_1),
          () => hearingLRspecPage.enterHearing(parties.RESPONDENT_SOLICITOR_1),
          () => chooseCourtSpecPage.chooseCourt('yes'),
        ]),
        () => hearingSupportRequirementsPage.selectRequirements(parties.RESPONDENT_SOLICITOR_1),
        ... conditionalSteps(paidAmount <= 1000 && (defenceType === 'dispute' || defenceType === 'hasPaid'),  [
          () => furtherInformationPage.enterFurtherInformation(parties.RESPONDENT_SOLICITOR_1),
        ]),
        ... conditionalSteps(paidAmount >= 10000 && (defenceType === 'dispute' || defenceType === 'hasPaid'),  [
          () => furtherInformationLRspecPage.enterFurtherInformation(parties.RESPONDENT_SOLICITOR_1),
        ]),
        () => statementOfTruth.enterNameAndRole(parties.APPLICANT_SOLICITOR_1 + 'DQ'),
        () => event.submit('Submit', ''),
        () => event.returnToCaseDetails()
      ]);
    },

    async evidenceUpload(caseId, defendant, isBundle = false, mpScenario = false, scenario = '') {
      defendant ? eventName = 'EVIDENCE_UPLOAD_RESPONDENT' : eventName = 'EVIDENCE_UPLOAD_APPLICANT';
      await this.triggerStepsWithScreenshot([
        () => unspecifiedEvidenceUpload.uploadADocument(caseId, defendant),
        () => unspecifiedEvidenceUpload.selectType(defendant, isBundle, mpScenario, scenario),
        () => unspecifiedEvidenceUpload.uploadYourDocument(TEST_FILE_PATH, defendant, isBundle, mpScenario),
        () => event.submit('Submit', 'Documents uploaded')
      ]);
    },

    async navigateToCaseDetails(caseNumber) {
      await this.retryUntilExists(async () => {
        const normalizedCaseId = caseNumber.toString().replace(/\D/g, '');
        console.log(`Navigating to case: ${normalizedCaseId}`);
        await this.amOnPage(`${config.url.manageCase}/cases/case-details/${normalizedCaseId}`);
      }, SUMMARY_TAB, undefined, 20);
    },

    async initiateNoticeOfChange(caseId, clientName) {
      eventName = 'NoC Request';
      await this.triggerStepsWithScreenshot([
        () => noticeOfChange.initiateNoticeOfChange(),
        () => noticeOfChange.enterCaseId(caseId),
        () => noticeOfChange.enterClientName(clientName),
        () => noticeOfChange.checkAndSubmit(caseId)
      ]);
    },

    async navigateToCaseFlags(caseNumber) {
      await this.retryUntilExists(async () => {
        const normalizedCaseId = caseNumber.toString().replace(/\D/g, '');
        output.log(`Navigating to case: ${normalizedCaseId}`);
        await this.amOnPage(`${config.url.manageCase}/cases/case-details/${normalizedCaseId}#Case%20Flags`);
      }, SUMMARY_TAB, undefined, 25);
    },

    async manageWitnessesForDefendant(caseId) {

      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.MANAGE_CONTACT_INFORMATION, caseId),
        () => partySelection.selectParty('DEFENDANT_1_WITNESSES'),
        () => manageWitnesses.addWitness(),
        () => event.submit('Submit', 'Contact information changed'),
        () => event.returnToCaseDetails()
      ]);
    },

    async manageOrganisationIndividualsForClaimant(caseId) {

      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.MANAGE_CONTACT_INFORMATION, caseId),
        () => partySelection.selectParty('CLAIMANT_1_ORGANISATION_INDIVIDUALS'),
        () => manageOrganisationIndividuals.addOrgIndividuals(),
        () => event.submit('Submit', 'Contact information changed'),
        () => event.returnToCaseDetails()
      ]);
    },

    async manageLitigationFriendForDefendant(caseId) {

      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.MANAGE_CONTACT_INFORMATION, caseId),
        () => partySelection.selectParty('DEFENDANT_1_LITIGATION_FRIEND'),
        () => manageLitigationFriend.updateLitigationFriend(address),
        () => event.submit('Submit', 'Contact information changed'),
        () => event.returnToCaseDetails()
      ]);
    },

    async manageDefendant(caseId) {

      await this.triggerStepsWithScreenshot([
        () => caseViewPage.startEvent(events.MANAGE_CONTACT_INFORMATION, caseId),
        () => partySelection.selectParty('DEFENDANT_1'),
        () => manageDefendant1.editAddress(address),
        () => event.submit('Submit', 'Contact information changed'),
        () => event.returnToCaseDetails()
      ]);
    },

    async createCaseFlags(caseFlags) {

      for (const {partyName, roleOnCase, details} of caseFlags) {
        for (const {name, flagComment} of details) {
          await this.triggerStepsWithScreenshot([
            () => caseViewPage.startEvent(events.CREATE_CASE_FLAGS, caseId),
            () => createCaseFlagPage.selectFlagLocation(`${partyName} (${roleOnCase})`),
            () => createCaseFlagPage.selectFlag(name),
            () => createCaseFlagPage.inputFlagComment(flagComment),
            () => event.submitWithoutHeader('Submit'),
          ]);
        }
      }
    },

    async validateCaseFlags(caseFlags) {
      eventName = '';

      await this.triggerStepsWithScreenshot([
        () => caseViewPage.goToCaseFlagsTab(caseId),
        () => caseViewPage.rejectCookieBanner(),
        () => caseViewPage.assertCaseFlagsInfo(caseFlags.length),
        () => caseViewPage.assertCaseFlags(caseFlags)
      ]);
      await this.takeScreenshot();
    },

    async manageCaseFlags(caseFlags) {
      eventName = 'Manage case flags';

      for (const {partyName, roleOnCase, flagType, flagComment} of caseFlags) {
        await this.triggerStepsWithScreenshot([
          () => caseViewPage.startEvent(events.MANAGE_CASE_FLAGS, caseId),
          () => manageCaseFlagsPage.selectFlagLocation(partyName, `${partyName} (${roleOnCase}) - ${flagType} (${flagComment})`),
          () => manageCaseFlagsPage.updateFlagComment(`${flagComment} - Updated - ${partyName}`),
          () => event.submitWithoutHeader('Submit')
        ]);
      }
    },

    async validateUpdatedCaseFlags(caseFlags) {
      eventName = '';

      await this.triggerStepsWithScreenshot([
        () => caseViewPage.goToCaseFlagsTab(caseId),
        () => caseViewPage.assertInactiveCaseFlagsInfo(caseFlags.length),
        () => caseViewPage.assertUpdatedCaseFlags(caseFlags)
      ]);
      await this.takeScreenshot();
    },

    async requestNewHearing() {
      eventName = 'Request Hearing';
      await this.triggerStepsWithScreenshot([
        () => requestNewHearingPage.openHearingTab(),
        () => requestNewHearingPage.selectAdditionalFacilities(),
        () => requestNewHearingPage.selectHearingStage(),
        () => requestNewHearingPage.selectParticipantAttendance(),
        () => requestNewHearingPage.selectHearingVenues(),
        () => requestNewHearingPage.selectJudges(),
        () => requestNewHearingPage.selectLengthDatePriority(),
        () => requestNewHearingPage.enterAdditionalInstructions(),
        () => requestNewHearingPage.submitHearing(),
        () => requestNewHearingPage.verifyWaitingForHearingToBeListed()
      ]);
    },

    async updateHearing() {
      eventName = 'Update Hearing';
      await this.triggerStepsWithScreenshot([
        () => updateHearingPage.clickOnViewEditHearing(),
        () => updateHearingPage.clickOnEditHearing(),
        () => updateHearingPage.updateHearingValues(),
        () => updateHearingPage.submitUpdatedHearing(),
        () => updateHearingPage.verifyUpdatedHearingDetails()
      ]);
    },

    async cancelHearing() {
      eventName = 'Cancel Hearing';
      await this.triggerStepsWithScreenshot([
        () => cancelHearingPage.clickCancelHearing(),
        () => cancelHearingPage.verifyHearingCancellation()
      ]);
    },

  });
};
