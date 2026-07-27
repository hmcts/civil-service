const {date} = require('../../../api/dataHelper');

const finalOrderDocument = {
  FinalOrderPreview: {
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
  },
};

const createAssistedOrder = () => {
  return  {
    FinalOrderSelect: {
      finalOrderSelection: 'ASSISTED_ORDER',
      assistedOrderMakeAnOrderForCosts: {
        assistedOrderCostsFirstDropdownDate: date(14),
        assistedOrderAssessmentThirdDropdownDate: date(14),
        makeAnOrderForCostsQOCSYesOrNo: 'No',
      },
      finalOrderRepresentation: {
        typeRepresentationComplex:{
          typeRepresentationClaimantOneDynamic: 'Test Inc',
          typeRepresentationDefendantOneDynamic: 'Sir John Doe',
        }
      },
      publicFundingCostsProtection: 'No',
      finalOrderAppealComplex: {
        appealGrantedDropdown: {
          appealChoiceSecondDropdownA: {
            appealGrantedRefusedDate: date(21),
          },
          appealChoiceSecondDropdownB: {
            appealGrantedRefusedDate: date(21),
          }
        },
        appealRefusedDropdown: {
          appealChoiceSecondDropdownA: {
            appealGrantedRefusedDate: date(21),
          },
          appealChoiceSecondDropdownB: {
            appealGrantedRefusedDate: date(21),
          }
        }
      },
      finalOrderDateHeardComplex: {
        singleDateSelection: {
          singleDate: date(0)
        }
      },
      orderMadeOnDetailsOrderCourt: {
        ownInitiativeDate: date(7),
        ownInitiativeText: 'As this order was made on the court\'s own initiative any party affected by the order' +
          ' may apply to set aside, vary or stay the order. Any such application must be made by 4pm on'
      },
      orderMadeOnDetailsOrderWithoutNotice: {
        withOutNoticeDate: date(7),
        withOutNoticeText: 'If you were not notified of the application before this order was made, you may apply to' +
          ' set aside, vary or stay the order. Any such application must be made by 4pm on'
      },
      finalOrderGiveReasonsYesNo: 'No'
    },
    ...finalOrderDocument
  };
};

const createFreeFormOrder = () => {
  return {
    FinalOrderSelect: {
      finalOrderSelection: 'FREE_FORM_ORDER',
      orderOnCourtInitiative: {
        onInitiativeSelectionDate: date(7),
        onInitiativeSelectionTextArea: 'As this order was made on the court\'s own initiative any party affected ' +
          'by the order may apply to set aside, vary or stay the order. Any such application must be made by 4pm on'
      },
      orderWithoutNotice: {
        withoutNoticeSelectionDate: date(7),
        withoutNoticeSelectionTextArea: 'If you were not notified of the application before this order was made,' +
          ' you may apply to set aside, vary or stay the order. Any such application must be made by 4pm on'

      }
    },
    ...finalOrderDocument
  };
};

module.exports = {
  requestFinalOrder: (finalOrderRequestType) => {
    if (finalOrderRequestType === 'ASSISTED_ORDER') {
      return {
        valid: createAssistedOrder()
      };
    } else {
      return {
        valid: createFreeFormOrder()
      };
    }

  }
};
