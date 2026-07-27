const fs = require('fs');

const COLUMN_ORDER = [
  { key: 'functionalTestGroup', label: 'Functional Test Group' },
  { key: 'folderPath', label: 'Folder Path' },
  { key: 'githubLabelName', label: 'GitHub Label Name' },
  { key: 'tag', label: 'Tag' },
  { key: 'pipeline', label: 'Pipeline' },
  { key: 'steps', label: 'Steps Covered' },
  { key: 'testNames', label: 'Test Names' }
];

function safeValue(value, key) {
  if (value === null || value === undefined) {
    return '';
  }

  if (key === 'steps' && Array.isArray(value)) {
    return value.map((v, i) => `${i + 1}. ${safeValue(v)}`).join('<br/>');
  }

  if (key === 'testNames') {
    if (Array.isArray(value)) {
      return value.map(v => safeValue(v)).join('<br/><br/>');
    }
    return String(value).split(/,\s*/g).join('<br/><br/>');
  }

  if (Array.isArray(value)) {
    return value.map(v => safeValue(v)).join('<br/>');
  }

  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2).replace(/\n/g, '<br/>');
  }

  return String(value);
}

function generateMarkdownTable(jsonPath) {
  const items = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
  let markdown = '';
  markdown += `| ${COLUMN_ORDER.map(col => col.label).join(' | ')} |\n`;
  markdown += `| ${COLUMN_ORDER.map(() => '---').join(' | ')} |\n`;
  items.forEach(item => {
    const row = COLUMN_ORDER.map(({ key }) => safeValue(item[key], key));
    markdown += `| ${row.join(' | ')} |\n`;
  });
  return markdown.trim();
}

module.exports = { generateMarkdownTable };
