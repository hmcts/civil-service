const {element} = require('../api/dataHelper');
const uuid = require('uuid');
module.exports = {
  PARTY_FLAGS: {
    vulnerableUser: element({
      name: 'Vulnerable user',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    }),
    confidentialPartyAddress: element({
      name: 'Confidential party/address',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    }),
    unacceptableBehaviour: element({
      name: 'Unacceptable/disruptive customer behaviour',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    }),
    vexatiousLitigant: element({
      name: 'Vexatious litigant',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    }),
    civilRestraintOrder: element({
      name: 'Civil restraint order',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    }),
    banningOrder: element({
      name: 'Banning order',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    })
  },
  LANGUAGE_INTERPRETER_FLAG: {
    languageInterpreter: element({
      name: 'Language Interpreter',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        }
      ],
      hearingRelevant: 'Yes',
      flagCode: 'PF0015',
      status: 'Active',
      subTypeKey:'HUT',
      subTypeValue: 'Huttese',
    }),
  },
  WHEELCHAIR_ACCESS_FLAG: {
    wheelchair: element({
      name: 'Step free / wheelchair access',
      path: [
        {
          id: uuid.v1(),
          value: 'Party'
        },
        {
          id: uuid.v1(),
          value: 'Reasonable adjustment'
        },
        {
          id: uuid.v1(),
          value: 'I need adjustments to get to, into and around our buildings'
        }
      ],
      status: 'Active',
      flagCode: 'RA0019',
      flagComment: 'wheelchair',
      dateTimeCreated: '2023-04-12T14:31:52',
      hearingRelevant: 'Yes'
    }),
  },
  SUPPORT_WORKER_FLAG: {
    supportWorker: element({
      'name': 'Support worker or carer with me',
      'path': [
        {
          'id': uuid.v1(),
          'value': 'Party'
        },
        {
          'id': uuid.v1(),
          'value': 'Reasonable adjustment'
        },
        {
          'id': uuid.v1(),
          'value': 'I need to bring support with me to a hearing'
        }
      ],
      'status': 'Active',
      'flagCode': 'RA0026',
      'flagComment': 'support worker comment',
      'dateTimeCreated': '2023-04-13T12:15:36',
      'hearingRelevant': 'Yes'
    }),
  },
  DETAINED_INDIVIDUAL_FLAG: {
    detainedIndividual: element({
      'name': 'Detained individual',
      'flagComment': 'detained comment',
      'path': [
        {
          'id': uuid.v1(),
          'value': 'Party'
        }
      ],
      'dateTimeCreated': '2023-04-13T12:16:14',
      'status': 'Active',
      'flagCode': 'PF0019',
      'hearingRelevant': 'Yes'
    }),
  },
  DISRUPTIVE_INDIVIDUAL: {
    disruptive: element({
      'name': 'Unacceptable/disruptive customer behaviour',
      'path': [
        {
          'id': uuid.v1(),
          'value': 'Party'
        }
      ],
      'status': 'Active',
      'flagCode': 'PF0007',
      'flagComment': 'disruptive comment',
      'dateTimeCreated': '2023-04-13T12:16:43',
      'hearingRelevant': 'Yes'
    }),
  },
  CASE_FLAGS: {
    complexCase: element({
      name: 'Complex Case',
      flagComment: 'test comment',
      dateTimeCreated: '2023-02-06T13:11:52',
      path: [
        {
          id: uuid.v1(),
          value: 'Case'
        }
      ],
      hearingRelevant: 'No',
      flagCode: 'PF0002',
      status: 'Active'
    }),
  }
};
