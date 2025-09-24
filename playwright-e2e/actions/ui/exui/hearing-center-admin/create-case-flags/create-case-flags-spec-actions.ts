import BaseTestData from '../../../../../base/base-test-data';
import caseFlagLocations from '../../../../../constants/case-flags/case-flag-locations';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CaseLevelFlags from '../../../../../enums/case-flags/case-level-flags';
import PartyCaseFlags from '../../../../../enums/case-flags/party-case-flags';
import SpecialMeasureFlags from '../../../../../enums/case-flags/special-measure-flags';
import TestData from '../../../../../models/test-data';
import CreateCaseFlagsPageFactory from '../../../../../pages/exui/hearing-center-admin/create-case-flags/create-case-flags-page-factory';

@AllMethodsStep()
export default class CreateCaseFlagsSpecActions extends BaseTestData {
  private createCaseFlagsPageFactory: CreateCaseFlagsPageFactory;

  constructor(createCaseFlagsPageFactory: CreateCaseFlagsPageFactory, testData: TestData) {
    super(testData);
    this.createCaseFlagsPageFactory = createCaseFlagsPageFactory;
  }

  async selectCaseLevel() {
    const { createCaseFlagsLocationSpecPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsLocationSpecPage.verifyContent(
      super.ccdCaseData,
      super.claimant1PartyType,
      super.defendant1PartyType,
    );
    await createCaseFlagsLocationSpecPage.selectLocation(caseFlagLocations.CASE_LEVEL);
    await createCaseFlagsLocationSpecPage.submit();
  }

  async selectCaseLevel1v2DS() {
    const { createCaseFlagsLocation1v2DSSpecPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsLocation1v2DSSpecPage.verifyContent(
      super.ccdCaseData,
      super.claimant1PartyType,
      super.defendant1PartyType,
      super.defendant2PartyType,
    );
    await createCaseFlagsLocation1v2DSSpecPage.selectLocation(caseFlagLocations.CASE_LEVEL);
    await createCaseFlagsLocation1v2DSSpecPage.submit();
  }

  async selectClaimant1() {
    const { createCaseFlagsLocationSpecPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsLocationSpecPage.verifyContent(
      super.ccdCaseData,
      super.claimant1PartyType,
      super.defendant1PartyType,
    );
    await createCaseFlagsLocationSpecPage.selectLocation(
      caseFlagLocations.CLAIMANT_1(this.claimant1PartyType),
    );
    await createCaseFlagsLocationSpecPage.submit();
  }

  async selectClaimant11v2DS() {
    const { createCaseFlagsLocation1v2DSSpecPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsLocation1v2DSSpecPage.verifyContent(
      super.ccdCaseData,
      super.claimant1PartyType,
      super.defendant1PartyType,
      super.defendant2PartyType,
    );
    await createCaseFlagsLocation1v2DSSpecPage.selectLocation(
      caseFlagLocations.CLAIMANT_1(this.claimant1PartyType),
    );
    await createCaseFlagsLocation1v2DSSpecPage.submit();
  }

  async caseLevelComplexTypeCaseFlag() {
    const caseFlagComment = 'The case is very complex';
    const { createCaseFlagsFlagTypeCaseLevelPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsFlagTypeCaseLevelPage.verifyContent(this.ccdCaseData);
    await createCaseFlagsFlagTypeCaseLevelPage.selectFlag(CaseLevelFlags.COMPLEX);
    await createCaseFlagsFlagTypeCaseLevelPage.submit();

    const { createCaseFlagsCommentsPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsCommentsPage.verifyContent(this.ccdCaseData);
    await createCaseFlagsCommentsPage.addCaseFlagComment(caseFlagComment);
    await createCaseFlagsCommentsPage.submit();

    const { submitCreateCaseFlagsPage } = this.createCaseFlagsPageFactory;
    await submitCreateCaseFlagsPage.verifyContent(this.ccdCaseData);
    await submitCreateCaseFlagsPage.submit();
    super.addCaseFlag({
      caseFlagLocation: caseFlagLocations.CASE_LEVEL,
      caseFlagType: CaseLevelFlags.COMPLEX,
      caseFlagComment,
    });
  }

  async claimant1SepcialMeasureCaseFlag() {
    const caseFlagComment = 'Claimant 1 needs to screen a witness';
    const { createCaseFlagsFlagTypePartyPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsFlagTypePartyPage.verifyContent(this.ccdCaseData);
    await createCaseFlagsFlagTypePartyPage.selectFlag(PartyCaseFlags.SPECIAL_MEASURE);
    await createCaseFlagsFlagTypePartyPage.submit();

    const { createCaseFlagsSpecialMeasurePage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsSpecialMeasurePage.verifyContent(this.ccdCaseData);
    await createCaseFlagsSpecialMeasurePage.selectFlag(SpecialMeasureFlags.SCREENING_WITNESS);
    await createCaseFlagsSpecialMeasurePage.submit();

    const { createCaseFlagsCommentsPage } = this.createCaseFlagsPageFactory;
    await createCaseFlagsCommentsPage.verifyContent(this.ccdCaseData);
    await createCaseFlagsCommentsPage.addCaseFlagComment(caseFlagComment);
    await createCaseFlagsCommentsPage.submit();

    const { submitCreateCaseFlagsPage } = this.createCaseFlagsPageFactory;
    await submitCreateCaseFlagsPage.verifyContent(this.ccdCaseData);
    await submitCreateCaseFlagsPage.submit();
    super.addCaseFlag({
      caseFlagLocation: caseFlagLocations.CLAIMANT_1(super.claimant1PartyType),
      caseFlagType: SpecialMeasureFlags.SCREENING_WITNESS,
      caseFlagComment,
    });
  }
}
