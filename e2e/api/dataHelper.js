const uuid = require('uuid');
const config = require('../config.js');

module.exports = {
  document: filename => {
    return {
      id: uuid.v1(),
      value: {
        document_url: `${config.url.dmStore}/documents/fakeUrl`,
        document_filename: filename,
        document_binary_url: `${config.url.dmStore}/documents/fakeUrl/binary`
      }
    };
  },
  date: (days = 0) => {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return date.toISOString().slice(0, 10);
  }
};
