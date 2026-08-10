const {element} = require('../api/dataHelper');
module.exports = {
  caseFlagsForParty: [
      element({
        name: 'Complex Case',
        flagComment: 'test comment',
        dateTimeCreated: '2023-02-06T13:11:52.466Z',
        path: [
          {
            value: 'Case'
          }
        ],
        hearingRelevant: 'Yes',
        flagCode: 'CF0002',
        status: 'Active'
      })
    ]
};
