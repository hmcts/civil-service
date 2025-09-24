const {listElement, date} = require('../../api/dataHelper');
const selectedOptionApp = listElement('Claimants 1 and 2');

module.exports = {
  createApplicantSmallClaimsEvidenceUpload: (mpScenario) => {
    switch (mpScenario) {
      case 'TWO_V_ONE': {
        console.log('Applicant small claims');
        return {
          valid: {
            EvidenceUpload: {
              caseProgAllocatedTrack: 'SMALL_CLAIM'
            },
            SelectUploadOptions: {
              evidenceUploadOptions: {
                list_items: [
                  selectedOptionApp,
                  listElement('Claimant 1: Sir John Doe'),
                  listElement('Claimant 2: Dr Foo Bar')
                ],
                value: selectedOptionApp
              }
            },
            DocumentSelectionSmallClaim: {
              witnessSelectionEvidenceSmallClaim: ['WITNESS_STATEMENT'],
              expertSelectionEvidenceSmallClaim: ['EXPERT_REPORT'],
              trialSelectionEvidenceSmallClaim: ['AUTHORITIES']
            },
            DocumentUpload: {
              documentWitnessStatement: [{
                value: {
                  witnessOptionName: 'test name',
                  witnessOptionUploadDate: '2023-02-06',
                  witnessOptionDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentExpertReport: [{
                value: {
                  expertOptionName: 'test name',
                  expertOptionExpertise: 'expertise',
                  expertOptionUploadDate: '2023-02-06',
                  expertDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentAuthorities: [{
                value: {
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }]
            }
          }
        };
      }
      default:
      {
        console.log('Applicant small claims');
        return {
          valid: {
            EvidenceUpload: {
              caseProgAllocatedTrack: 'SMALL_CLAIM'
            },
            DocumentSelectionSmallClaim: {
              witnessSelectionEvidenceSmallClaim: ['WITNESS_STATEMENT'],
              expertSelectionEvidenceSmallClaim: ['EXPERT_REPORT'],
              trialSelectionEvidenceSmallClaim: ['AUTHORITIES']
            },
            DocumentUpload: {
              documentWitnessStatement: [{
                value: {
                  witnessOptionName: 'test name',
                  witnessOptionUploadDate: '2023-02-06',
                  witnessOptionDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentExpertReport: [{
                value: {
                  expertOptionName: 'test name',
                  expertOptionExpertise: 'expertise',
                  expertOptionUploadDate: '2023-02-06',
                  expertDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentAuthorities: [{
                value: {
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }]
            }
          }
        };
      }
    }
  },

  createApplicantSmallClaimsEvidenceUploadFlightDelay: (mpScenario) => {
    switch (mpScenario) {
      case 'ONE_V_ONE': {
        console.log('Applicant small claims for ONE_V_ONE');
        return {
          valid: {
            EvidenceUpload: {
              caseProgAllocatedTrack: 'SMALL_CLAIM'
            },
            DocumentSelectionSmallClaim: {
              caseTypeFlag: 'do_not_show',
              witnessSelectionEvidenceSmallClaim: ['WITNESS_STATEMENT'],
              expertSelectionEvidenceSmallClaim: ['EXPERT_REPORT'],
              trialSelectionEvidenceSmallClaim: ['AUTHORITIES']
            },
            DocumentUpload: {
              documentWitnessStatement: [{
                value: {
                  witnessOptionName: 'test name',
                  witnessOptionUploadDate: '2023-02-06',
                  witnessOptionDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentExpertReport: [{
                value: {
                  expertOptionName: 'test name',
                  expertOptionExpertise: 'expertise',
                  expertOptionUploadDate: '2023-02-06',
                  expertDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentAuthorities: [{
                value: {
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }]
            }
          }
        };
      }
    }
  },

  createApplicantEvidenceUploadDRH: () => {
        console.log('Applicant small claims');
        return {
          valid: {
            EvidenceUpload: {
              caseProgAllocatedTrack: 'SMALL_CLAIM'
            },
            DocumentSelectionSmallClaim: {
              caseTypeFlag: 'do_not_show',
              witnessSelectionEvidenceSmallClaim: ['WITNESS_STATEMENT'],
              expertSelectionEvidenceSmallClaim: ['EXPERT_REPORT'],
              trialSelectionEvidenceSmallClaim: ['AUTHORITIES']
            },
            DocumentUpload: {
              documentWitnessStatement: [{
                value: {
                  witnessOptionName: 'test name',
                  witnessOptionUploadDate: '2023-02-06',
                  witnessOptionDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentExpertReport: [{
                value: {
                  expertOptionName: 'test name',
                  expertOptionExpertise: 'expertise',
                  expertOptionUploadDate: '2023-02-06',
                  expertDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentAuthorities: [{
                value: {
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }]
            }
          }
      };
  },

  createApplicantFastClaimsEvidenceUpload: (mpScenario, claimTrack) => {
    console.log('Applicant fast claims1');
    switch (mpScenario) {
      case 'TWO_V_ONE': {
        return {
          valid: {
            EvidenceUpload: {
              caseProgAllocatedTrack: 'FAST_CLAIM'
            },
            SelectUploadOptions: {
              caseTypeFlag: 'do_not_show',
              evidenceUploadOptions: {
                list_items: [
                  selectedOptionApp,
                  listElement('Claimant 1: Sir John Doe'),
                  listElement('Claimant 2: Dr Foo Bar')
                ],
                value: selectedOptionApp
              }
            },
            DocumentSelectionFastTrack: {
              disclosureSelectionEvidence: ['DISCLOSURE_LIST'],
              witnessSelectionEvidence: ['WITNESS_SUMMARY'],
              expertSelectionEvidence: ['JOINT_STATEMENT'],
              trialSelectionEvidence: ['DOCUMENTARY']
            },
            DocumentUpload: {
              documentDisclosureList: [{
                value: {
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentWitnessSummary: [{
                value: {
                  witnessOptionName: 'test name',
                  witnessOptionUploadDate: '2023-02-06',
                  witnessOptionDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentJointStatement: [{
                value: {
                  expertOptionName: 'test name',
                  expertOptionExpertises: 'expertise',
                  expertOptionUploadDate: '2023-02-06',
                  expertDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentEvidenceForTrial: [{
                value: {
                  typeOfDocument: 'images etc',
                  documentIssuedDate: '2023-02-06',
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              ...(claimTrack === 'INTERMEDIATE_CLAIM' || claimTrack === 'MULTI_CLAIM'? {
                bundleEvidence: [{
                  value: {
                    bundleName: 'applicant bundle for trial',
                    documentIssuedDate: date(30),
                    documentUpload: {
                      document_url: '${TEST_DOCUMENT_URL}',
                      document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                      document_filename: '${TEST_DOCUMENT_FILENAME}'
                    },
                    createdDatetime: '2023-02-06T13:11:52.466Z'
                  }
                }]
              } : {}),
            }
          }
        };
      }
      default:
      {
        return {
          valid: {
            EvidenceUpload: {
              caseProgAllocatedTrack: 'FAST_CLAIM'
            },
            DocumentSelectionFastTrack: {
              caseTypeFlag: 'do_not_show',
              disclosureSelectionEvidence: ['DISCLOSURE_LIST'],
              witnessSelectionEvidence: ['WITNESS_SUMMARY'],
              expertSelectionEvidence: ['JOINT_STATEMENT'],
              trialSelectionEvidence: ['DOCUMENTARY']
            },
            DocumentUpload: {
              documentDisclosureList: [{
                value: {
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentWitnessSummary: [{
                value: {
                  witnessOptionName: 'test name',
                  witnessOptionUploadDate: '2023-02-06',
                  witnessOptionDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentJointStatement: [{
                value: {
                  expertOptionName: 'test name',
                  expertOptionExpertises: 'expertise',
                  expertOptionUploadDate: '2023-02-06',
                  expertDocument: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              documentEvidenceForTrial: [{
                value: {
                  typeOfDocument: 'images etc',
                  documentIssuedDate: '2023-02-06',
                  documentUpload: {
                    document_url: '${TEST_DOCUMENT_URL}',
                    document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                    document_filename: '${TEST_DOCUMENT_FILENAME}'
                  },
                  createdDatetime: '2023-02-06T13:11:52.466Z'
                }
              }],
              ...(claimTrack === 'INTERMEDIATE_CLAIM' || claimTrack === 'MULTI_CLAIM'? {
                bundleEvidence: [{
                  value: {
                    bundleName: 'applicant bundle for trial',
                    documentIssuedDate: date(30),
                    documentUpload: {
                      document_url: '${TEST_DOCUMENT_URL}',
                      document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                      document_filename: '${TEST_DOCUMENT_FILENAME}'
                    },
                    createdDatetime: '2023-02-06T13:11:52.466Z'
                  }
                }]
              } : {}),
            }
          }
        };
      }
    }
  },

  createClaimantSmallClaimsEvidenceUpload: (document) => {
    return {
      event: 'EVIDENCE_UPLOAD_APPLICANT',
      caseDataUpdate: {
        documentWitnessStatement: [
          {
            id: 'd5d0e1c4-ce3b-4eeb-8baa-4ec3f95bd504',
            value: {
              witnessOptionName: 'John Doe',
              witnessOptionUploadDate: '2000-02-02T00:00:00.000Z',
                witnessOptionDocument: {
                  document_url: document.document_url,
                  document_binary_url: document.document_binary_url,
                  document_filename: document.document_filename,
                  document_hash: document.document_hash
                },
            },
            createdDatetime: '2024-08-07T08:26:23.000Z'
          }
        ],
        caseDocumentUploadDate: '2024-08-07T08:27:11.018Z'
      }
    };
  }
};
