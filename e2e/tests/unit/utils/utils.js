const load = require;
const fs = require('fs');
const path = require('path');
const { sortBy, uniqBy, map } = require('lodash');
const { expect } = require('chai');

const SHORT_STRING = 30;
const MEDIUM_STRING = 70;
const LONG_STRING = 150;
const CASE_TYPE_PREFIXES = ['CIVIL', 'GENERALAPPLICATION'];

function isFieldDuplicated(field) {
  return function isDuplicated(field1, field2) {
    if (field1.CaseTypeID === field2.CaseTypeID
      && field1[field] === field2[field]
      && field1.UserRoles === field2.UserRoles
      && field1.UserRole === field2.UserRole
      && field1.AccessControl === field2.AccessControl) {
      console.log(`Duplicated field: ${field1[field]}`);
    }
    return field1.CaseTypeID === field2.CaseTypeID
            && field1[field] === field2[field]
            && field1.AccessControl === field2.AccessControl
            && field1.UserRole === field2.UserRole
            && field1.UserRoles === field2.UserRoles;
  };
}

function isCaseEventToFieldDuplicated(field) {
  return function isDuplicated(field1, field2) {
    if (field1.CaseTypeID === field2.CaseTypeID
        && field1.CaseEventID === field2.CaseEventID
        && field1[field] === field2[field]) {
      console.log(`Duplicated field: ${field1[field]}`);
    }
    return field1.CaseTypeID === field2.CaseTypeID
            && field1.CaseEventID === field2.CaseEventID
            && field1[field] === field2[field];
  };
}

function isNotEmpty() {
  return v => {
    return v !== null && v.length > 0;
  };
}

function isNotLongerThan(maxLength) {
  return v => {
    return v !== null && v.length > 0 && v.length <= maxLength;
  };
}

function isPositiveNumber() {
  return v => {
    return typeof v === 'number' && v > 0;
  };
}

function whenPopulated(key, type) {
  const myType = type || 'string';
  return {
    expect: satisfyCallback => {
      if (key) {
        expect(key).to.be.a(myType).and.satisfy(satisfyCallback);
      }
    }
  };
}

function noDuplicateFound(a, b) {
  if (a.CaseTypeID === b.CaseTypeID && a.ID === b.ID) {
    console.log(`Duplicated field: ${a.ID}`);
  }
  return a.CaseTypeID === b.CaseTypeID && a.ID === b.ID;
}

function noDuplicateFoundWB(a, b) {
  return a.CaseTypeID === b.CaseTypeID && a.ID === b.ID && a.CaseFieldID === b.CaseFieldID;
}

function noDuplicateFoundFL(a, b) {
  return a.ID === b.ID && a.ListElementCode === b.ListElementCode;
}

function noDuplicateFoundCT(a, b) {
  return a.CaseTypeID === b.CaseTypeID && a.TabID === b.TabID && a.CaseFieldID === b.CaseFieldID;
}

function noDuplicateFoundEvent(a, b) {
  if (a.AccessControl != null && b.AccessControl != null) {
    return a.CaseTypeID === b.CaseTypeID && a.CaseEventID === b.CaseEventID && a.AccessControl === b.AccessControl;
  } else if (a.UserRole != null && b.UserRole != null) {
    return a.CaseTypeID === b.CaseTypeID && a.CaseEventID === b.CaseEventID && a.UserRole === b.UserRole;
  }
}

function noDuplicateFoundAccessProfiles(a, b) {
  return a.CaseTypeID === b.CaseTypeID && a.AccessProfiles === b.AccessProfiles;
}

function noDuplicateFoundACT(a, b) {
  if (a.UserRole != null &&  b.UserRole != null) {
    return a.CaseTypeID === b.CaseTypeID && a.UserRole === b.UserRole;
  } else {
    return a.CaseTypeID === b.CaseTypeID && a.UserRoles === b.UserRoles ;
  }
}

function noDuplicateFoundCCT(a, b) {
  return a.CaseTypeID === b.CaseTypeID && a.ID === b.ID && a.CaseEventID === b.CaseEventID && a.CaseFieldID === b.CaseFieldID && a.ListElementCode === b.ListElementCode;
}

function loadAllFiles(location) {
  return function loadFeatureFiles(featureFiles) {
    let definitions = [];
    const definitionsRoot = path.resolve(__dirname, '../../../../ccd-definition');
    const definitionVariants = ['civil', 'generalapplication'];

    featureFiles.forEach(featureFile => {
      definitionVariants.forEach(variant => {
        const fullPath = path.join(definitionsRoot, variant, location, `${featureFile}.json`);
        if (fs.existsSync(fullPath)) {
          definitions = definitions.concat(load(fullPath));
        }
      });
    });

    return definitions;
  };
}

function sortCaseTypeTabs(caseTypeTab) {
  return sortBy(caseTypeTab, tab => {
    return tab.TabDisplayOrder;
  });
}

function getUniqValues(objectArray, property) {
  return map(uniqBy(objectArray, property), obj => {
    return obj[property];
  });
}

function byCaseType(caseType) {
  return entry => {
    return entry.CaseTypeID === caseType;
  };
}

function byStateName(stateEntry) {
  return stateAuth => {
    return stateAuth.CaseStateID === stateEntry.ID;
  };
}

function isValidCaseTypeId() {
  return value => {
    return CASE_TYPE_PREFIXES.some(prefix => value.startsWith(prefix));
  };
}

function mapErrorArray(caseType) {
  return entry => {
    return {
      UserRole: entry.UserRole,
      CaseType: caseType
    };
  };
}

function missingAuthorisationsExist(missingAuthCount) {
  return missingAuthCount > 0;
}

module.exports = {
  SHORT_STRING,
  MEDIUM_STRING,
  LONG_STRING,
  isFieldDuplicated,
  isCaseEventToFieldDuplicated,
  loadAllFiles,
  sortCaseTypeTabs,
  noDuplicateFound,
  noDuplicateFoundWB,
  noDuplicateFoundFL,
  noDuplicateFoundCT,
  noDuplicateFoundCCT,
  noDuplicateFoundACT,
  noDuplicateFoundEvent,
  isNotEmpty,
  isNotLongerThan,
  isPositiveNumber,
  whenPopulated,
  getUniqValues,
  byCaseType,
  byStateName,
  mapErrorArray,
  noDuplicateFoundAccessProfiles,
  missingAuthorisationsExist,
  isValidCaseTypeId
};
