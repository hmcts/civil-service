const { expect } = require('chai');
const { uniqWith } = require('lodash');
const {
  isNotEmpty,
  noDuplicateFound
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.BannerEnabled).to.be.a('string').and.satisfy(isNotEmpty());
  expect(row.BannerDescription).to.be.a('string').and.satisfy(isNotEmpty());
}

describe('Banner', () => {
  context('should :', () => {
    let uniqResult = [];

    before(() => {
      uniqResult = uniqWith(dataProvider.ccdData.Banner, noDuplicateFound);
    });

    it('should have only valid definitions', () => {
      uniqResult.forEach(assertFieldDefinitionIsValid);
    });
  });
});
