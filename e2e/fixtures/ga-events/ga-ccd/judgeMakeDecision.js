const {date, listElement} = require('../../../api/dataHelper');
const config = require('../../../config');
module.exports = {
  judgeMakesDecisionData: () => {
    return {
      judicialDecision : {
        decision: 'REQUEST_MORE_INFO'
      },
      judicialDecisionRequestMoreInfo: {
        requestMoreInfoOption: 'REQUEST_MORE_INFORMATION',
        judgeRequestMoreInfoText: 'sample data',
        judgeRequestMoreInfoByDate: '2026-05-04'
      }
    };
  },
  judgeMakeOrderWrittenRep: (current_date) => {
    return {
      judicialDecision : {
        decision: 'MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS'
      },
      judicialDecisionMakeAnOrderForWrittenRepresentations: {
        writtenConcurrentRepresentationsBy: current_date,
        makeAnOrderForWrittenRepresentations: 'CONCURRENT_REPRESENTATIONS'
      },
      judicialByCourtsInitiativeForWrittenRep: 'OPTION_3'
    };
  },
  judgeMakeOrderWrittenRep_On_Uncloaked_Appln: (current_date) => {
    return {
      applicationIsUncloakedOnce: 'Yes',
      generalAppInformOtherParty: {
        isWithNotice: 'Yes',
        reasonsForWithoutNotice: 'Test'
      },
      judicialDecision : {
        decision: 'MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS'
      },
      judicialDecisionMakeAnOrderForWrittenRepresentations: {
        writtenConcurrentRepresentationsBy: current_date,
        makeAnOrderForWrittenRepresentations: 'CONCURRENT_REPRESENTATIONS'
      },
      judicialByCourtsInitiativeForWrittenRep: 'OPTION_3'
    };
  },
  judgeMakeDecisionDirectionOrder: (current_date) => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'GIVE_DIRECTIONS_WITHOUT_HEARING',
        judicialByCourtsInitiative: 'OPTION_3',
        directionsText: 'sample text',
        showReasonForDecision: 'Yes',
        reasonForDecisionText: 'sample text',
        directionsResponseByDate: current_date,
        displayjudgeApproveEditOptionDoc: 'No',
        displayjudgeApproveEditOptionDate: 'No',
        isOrderProcessedByStayScheduler: 'No'
      }
    };
  },
  judgeApprovesStrikeOutAppl: () => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'APPROVE_OR_EDIT',
        orderText: 'sample text',
        judgeApproveEditOptionDoc: 'DEFENCE_FORM',
        judgeApproveEditOptionDate: '2023-06-05',
        showReasonForDecision: 'Yes',
        reasonForDecisionText: 'sample text',
        isOrderProcessedByStayScheduler: 'No',
        judicialByCourtsInitiative: 'OPTION_3'
      }
    };
  },
  judgeApprovesStayClaimAppl: (current_date) => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'APPROVE_OR_EDIT',
        orderText: 'sample text',
        judgeApproveEditOptionDoc: 'DEFENCE_FORM',
        judgeApproveEditOptionDate: current_date,
        showReasonForDecision: 'Yes',
        reasonForDecisionText: 'sample text',
        isOrderProcessedByStayScheduler: 'No',
        judicialByCourtsInitiative: 'OPTION_3'
      }
    };
  },
  judgeApprovesUnlessOrderAppl: (current_date) => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'APPROVE_OR_EDIT',
        orderText: 'sample text',
        judgeApproveEditOptionDoc: 'DEFENCE_FORM',
        displayjudgeApproveEditOptionDateForUnlessOrder: 'Yes',
        judgeApproveEditOptionDateForUnlessOrder: current_date,
        showReasonForDecision: 'Yes',
        reasonForDecisionText: 'sample text',
        isOrderProcessedByUnlessScheduler: 'No',
        judicialByCourtsInitiative: 'OPTION_3'
      }
    };
  },
  freeFormOrder: () => {
    return {
      judicialDecision : {
        decision: 'FREE_FORM_ORDER'
      },
      caseNameHmctsInternal: 'Test Inc v Sir John Doe, Dr Foo Bar',
      caseParticipantsFreeForm: 'Test Inc v Sir John Doe, Dr Foo Bar',
      freeFormRecitalText: 'Recital of who attended',
      freeFormOrderedText: 'Orders that were made',
      orderOnCourtsList: 'ORDER_ON_COURT_INITIATIVE',
      orderOnCourtInitiative: {
        onInitiativeSelectionTextArea: 'As this order was made on the court\'s own '
          + 'initiative any party affected by the order '
          + 'may apply to set aside, vary or stay the order. '
          + 'Any such application must be made by 4pm on',
        onInitiativeSelectionDate: date()
      }
    };
  },
  listingForHearing: () => {
    return {
      judicialDecision : {
        decision: 'LIST_FOR_A_HEARING'
      },
      judicialDecisionMakeOrder: {
        directionsText: 'sample text',
        reasonForDecisionText: 'sample text',
        directionsResponseByDate: '2023-06-05',
        displayjudgeApproveEditOptionDoc: 'No',
        displayjudgeApproveEditOptionDate: 'No',
        judicialByCourtsInitiativeListForHearing: 'OPTION_3'
      },
      judicialListForHearing: {
        hearingPreferencesPreferredType: 'TELEPHONE',
        hearingPreferredLocation: null,
        judicialTimeEstimate: 'MINUTES_30',
        judgeSignLanguage: 'sample text',
        judgeLanguageInterpreter: 'sample text',
        judgeOtherSupport: 'sample text'
      },
      judicialByCourtsInitiativeListForHearing: 'OPTION_3'
    };
  },
  listingForHearingInPerson: () => {
    return {
      judicialDecision : {
        decision: 'LIST_FOR_A_HEARING'
      },
      judicialDecisionMakeOrder: {
        directionsText: 'sample text',
        reasonForDecisionText: 'sample text',
        directionsResponseByDate: '2023-06-05',
        displayjudgeApproveEditOptionDoc: 'No',
        displayjudgeApproveEditOptionDate: 'No',
        judicialByCourtsInitiativeListForHearing: 'OPTION_3'
      },
      judicialListForHearing: {
        hearingPreferencesPreferredType: 'IN_PERSON',
        hearingPreferredLocation: {
          list_items: [
            listElement(config.defendant2SelectedCourt)
          ],
          value: listElement(config.defendant2SelectedCourt)
        },
        judicialTimeEstimate: 'MINUTES_30',
        judgeSignLanguage: 'sample text',
        judgeLanguageInterpreter: 'sample text',
        judgeOtherSupport: 'sample text'
      },
      judicialByCourtsInitiativeListForHearing: 'OPTION_3',
      judicialGOHearingDirections:'Test Hearing Directions'
    };
  },
  applicationsDismiss: () => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'DISMISS_THE_APPLICATION',
        showReasonForDecision: 'Yes',
        reasonForDecisionText: 'sample text',
        directionsResponseByDate: '2023-06-05',
        displayjudgeApproveEditOptionDoc: 'No',
        displayjudgeApproveEditOptionDate: 'No',
        isOrderProcessedByStayScheduler: 'No',
        judicialByCourtsInitiative: 'OPTION_3'
      }
    };
  },
  judgeMakeDecisionDismissed: () => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'DISMISS_THE_APPLICATION',
        judgeRecitalText:'sample text',
        orderText:'sample text',
        dismissalOrderText:'sample text',
        showReasonForDecision: 'Yes',
        reasonForDecisionText:'sample text',
        displayjudgeApproveEditOptionDoc: 'No',
        displayjudgeApproveEditOptionDate: 'No',
        isOrderProcessedByStayScheduler: 'No',
        judicialByCourtsInitiative: 'OPTION_3'
      }
    };

  },
  judgeMakeOrderUncloakApplication: () => {
    return {
      judicialDecision : {
        decision: 'MAKE_AN_ORDER'
      },
      judicialDecisionMakeOrder: {
        makeAnOrder: 'APPROVE_OR_EDIT',
        judgeApproveEditOptionDate : '2023-06-05',
        judgeRecitalText:'sample text',
        orderText: 'order sample text',
        showReasonForDecision: 'Yes',
        reasonForDecisionText: 'sample text',
        isOrderProcessedByStayScheduler: 'No',
        judicialByCourtsInitiative: 'OPTION_3'
      },
      makeAppVisibleToRespondents: {
        makeAppAvailableCheck: [
          'CONSENT_AGREEMENT_CHECKBOX'
        ]
      }
    };
  },
  judgeRequestMoreInfomationUncloakData: (other) => {
    return {
      judicialDecision : {
        decision: 'REQUEST_MORE_INFO'
      },
      judicialDecisionRequestMoreInfo: {
        requestMoreInfoOption: other ? 'SEND_APP_TO_OTHER_PARTY':'REQUEST_MORE_INFORMATION',
        judgeRequestMoreInfoText: other ? null : 'sample data',
        judgeRequestMoreInfoByDate: other ? null : '2026-05-04'
      }
    };
  },
  serviceUpdateDto:(gaCaseId,paymentStatus)=> {
    return {
      service_request_reference: '1324646546456',
      ccd_case_number: gaCaseId,
      service_request_amount: '167.00',
      service_request_status: paymentStatus,
      payment: {
        _links: null,
        account_number: null,
        amount: 0,
        case_reference: null,
        ccd_case_number: null,
        channel: null,
        currency: null,
        customer_reference: '13246546',
        date_created: '2022-07-26T19:21:50.141Z',
        date_updated: '2022-07-26T19:21:50.141Z',
        description: null,
        external_provider: null,
        external_reference: null,
        fees: null,
        giro_slip_no: '',
        id: '',
        method: '',
        organisation_name: null,
        payment_group_reference: null,
        payment_reference: '13213223',
        reference: null,
        reported_date_offline: null,
        service_name: null,
        site_id: null,
        status: null,
        status_histories: null
      }
    };
  },
  serviceUpdateDtoWithNotice:(gaCaseId,paymentStatus)=> {
    return {
      service_request_reference: '1324646546456',
      ccd_case_number: gaCaseId,
      service_request_amount: '303.00',
      service_request_status: paymentStatus,
      payment: {
        _links: null,
        account_number: null,
        amount: 0,
        case_reference: null,
        ccd_case_number: null,
        channel: null,
        currency: null,
        customer_reference: '13246546',
        date_created: '2022-07-26T19:21:50.141Z',
        date_updated: '2022-07-26T19:21:50.141Z',
        description: null,
        external_provider: null,
        external_reference: null,
        fees: null,
        giro_slip_no: '',
        id: '',
        method: '',
        organisation_name: null,
        payment_group_reference: null,
        payment_reference: '13213223',
        reference: null,
        reported_date_offline: null,
        service_name: null,
        site_id: null,
        status: null,
        status_histories: null
      }
    };
  },
  serviceUpdateDtoWithoutNotice:(gaCaseId,paymentStatus)=> {
    return {
      service_request_reference: '1324646546456',
      ccd_case_number: gaCaseId,
      service_request_amount: '119.00',
      service_request_status: paymentStatus,
      payment: {
        _links: null,
        account_number: null,
        amount: 0,
        case_reference: null,
        ccd_case_number: null,
        channel: null,
        currency: null,
        customer_reference: '13246546',
        date_created: '2022-07-26T19:21:50.141Z',
        date_updated: '2022-07-26T19:21:50.141Z',
        description: null,
        external_provider: null,
        external_reference: null,
        fees: null,
        giro_slip_no: '',
        id: '',
        method: '',
        organisation_name: null,
        payment_group_reference: null,
        payment_reference: '13213223',
        reference: null,
        reported_date_offline: null,
        service_name: null,
        site_id: null,
        status: null,
        status_histories: null
      }
    };
  }
};
