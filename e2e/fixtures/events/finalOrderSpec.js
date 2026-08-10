const {listElement} = require('../../api/dataHelper');
const finalOrderDocument = {FinalOrderPreview: {
  finalOrderDocument: {
    documentLink: {
      document_url: '${TEST_DOCUMENT_URL}',
        document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
        document_filename: '${TEST_DOCUMENT_FILENAME}'
    },
    documentName: 'test document',
      documentSize: 1234,
      createdDatetime: '2023-02-06T13:11:52.466Z',
      createdBy: 'CIVIL',
  }
}};

module.exports = {
  requestFinalOrder: (finalOrderRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderType) => {
    const requestFinalOrder = {
    };
    switch (finalOrderRequestType) {
      case 'ASSISTED_ORDER':
        requestFinalOrder.userInput = {
          ...requestFinalOrder.userInput,
          FinalOrderSelect: {
            finalOrderSelection: 'ASSISTED_ORDER',
            assistedOrderMakeAnOrderForCosts: {
              assistedOrderCostsFirstDropdownDate: dayPlus14,
              assistedOrderAssessmentThirdDropdownDate: dayPlus14,
              makeAnOrderForCostsQOCSYesOrNo: 'No',
            },
            finalOrderRepresentation: {
              typeRepresentationComplex: {
                typeRepresentationClaimantOneDynamic: 'Test Inc',
                typeRepresentationDefendantOneDynamic: 'Sir John Doe',
              }
            },
            publicFundingCostsProtection: 'No',
            finalOrderAppealComplex: {
              appealGrantedRefusedDropdown: {
                appealChoiceSecondDropdownA: {
                  appealGrantedRefusedDate: dayPlus21,
                },
                appealChoiceSecondDropdownB: {
                  appealGrantedRefusedDate: dayPlus21,
                }
              }
            },
            finalOrderDateHeardComplex: {
              singleDateSelection: {
                singleDate: dayPlus0
              }
            },
            orderMadeOnDetailsOrderCourt: {
              ownInitiativeDate: dayPlus0,
              ownInitiativeText: 'As this order was made on the court\'s own initiative any party affected by the order' +
                ' may apply to set aside, vary or stay the order. Any such application must be made by 4pm on'
            },
            orderMadeOnDetailsOrderWithoutNotice: {
              withOutNoticeDate: dayPlus0,
              withOutNoticeText: 'If you were not notified of the application before this order was made, you may apply to' +
                ' set aside, vary or stay the order. Any such application must be made by 4pm on'
            },
          },
          ...finalOrderDocument
        };
        break;

      case 'FREE_FORM_ORDER':
        requestFinalOrder.userInput = {
          ...requestFinalOrder.userInput,
          FinalOrderSelect: {
            finalOrderSelection: 'FREE_FORM_ORDER',
            orderOnCourtInitiative: {
              onInitiativeSelectionDate: dayPlus0,
              onInitiativeSelectionTextArea: 'As this order was made on the court\'s own initiative any party affected ' +
                'by the order may apply to set aside, vary or stay the order. Any such application must be made by 4pm on'
            },
            orderWithoutNotice: {
              withoutNoticeSelectionDate: dayPlus0,
              withoutNoticeSelectionTextArea: 'If you were not notified of the application before this order was made,' +
                ' you may apply to set aside, vary or stay the order. Any such application must be made by 4pm on'

            }
          },
          ...finalOrderDocument
        };
        break;
      case 'DOWNLOAD_ORDER_TEMPLATE':
          if (orderType === 'INTERMEDIATE') {
              requestFinalOrder.userInput = {
              ...createIntermediateDownloadOrder()
            };
          }
          if (orderType === 'MULTI') {
            requestFinalOrder.userInput = {
                ...createMultiDownloadOrder()
            };
          }
        break;
    }
    return requestFinalOrder;
  }
};

const createIntermediateDownloadOrder = () => {
  return {
    TrackAllocation: {
      finalOrderTrackToggle: 'INTERMEDIATE_CLAIM',
      finalOrderAllocateToTrack: 'Yes',
      finalOrderTrackAllocation: 'INTERMEDIATE_CLAIM'
    },
    IntermediateTrackComplexityBand: {
      finalOrderIntermediateTrackComplexityBand: {
        assignComplexityBand: 'Yes',
        band: 'BAND_1',
        reasons: 'Test reasons'
      }
    },
    SelectTemplate: {
      finalOrderDownloadTemplateOptions: {
        list_items: [
          listElement('Blank template to be used after a hearing'),
          listElement('Blank template to be used before a hearing/box work'),
          listElement('Fix a date for CMC')
        ],
        value: listElement('Fix a date for CMC')
      }
    },
    UploadOrder: {
      uploadOrderDocumentFromTemplate: {
        document_url: '${TEST_DOCUMENT_URL}',
        document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
        document_filename: '${TEST_DOCUMENT_FILENAME}'
      }
    }
  };
};

const createMultiDownloadOrder = () => {
  return {
    TrackAllocation: {
      finalOrderTrackToggle: 'MULTI_CLAIM',
      finalOrderAllocateToTrack: 'Yes',
      finalOrderTrackAllocation: 'MULTI_CLAIM'
    },
    SelectTemplate: {
      finalOrderDownloadTemplateOptions: {
        list_items: [
          listElement('Blank template to be used after a hearing'),
          listElement('Blank template to be used before a hearing/box work'),
          listElement('Fix a date for CMC')
        ],
        value: listElement('Fix a date for CMC')
      }
    },
    UploadOrder: {
      uploadOrderDocumentFromTemplate: {
        document_url: '${TEST_DOCUMENT_URL}',
        document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
        document_filename: '${TEST_DOCUMENT_FILENAME}'
      }
    }
  };
};
