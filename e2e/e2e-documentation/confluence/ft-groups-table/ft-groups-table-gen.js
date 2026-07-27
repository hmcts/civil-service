const fs = require('fs');

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

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
  if (value === null || value === undefined) return '';

  if (key === 'steps' && Array.isArray(value)) {
    return value.map((v, i) => `${i + 1}. ${safeValue(v)}`).join('<br/>');
  }

  if (key === 'testNames') {
    if (Array.isArray(value)) {
      return value.map(v => `<p>${safeValue(v)}</p>`).join('');
    }
    const parts = escapeHtml(String(value)).split(/,\\s*/g);
    return parts.map(v => `<p>${v}</p>`).join('');
  }

  if (key === 'pipeline' && Array.isArray(value)) {
    return value.map(v => safeValue(v)).join('<br/><br/>');
  }

  if (Array.isArray(value)) {
    return value.map(v => safeValue(v)).join('<br/>');
  }

  if (typeof value === 'object') {
    return escapeHtml(JSON.stringify(value, null, 2)).replace(/\n/g, '<br/>');
  }

  return escapeHtml(value);
}

function wrapInExpand(html) {
  return `
<ac:structured-macro ac:name="expand">
  <ac:parameter ac:name="title">Click here to expand...</ac:parameter>
  <ac:rich-text-body>
    ${html}
  </ac:rich-text-body>
</ac:structured-macro>
`.trim();
}

function generateConfluenceTable(jsonPath) {
  const items = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));

  let html = '<table>\n';

  html += '  <tr>\n';
  COLUMN_ORDER.forEach(({ label }) => {
    html += `    <th>${label}</th>\n`;
  });
  html += '  </tr>\n';

  items.forEach((obj) => {
    html += '  <tr>\n';
    COLUMN_ORDER.forEach(({ key }) => {
      html += `    <td>${safeValue(obj[key], key)}</td>\n`;
    });
    html += '  </tr>\n';
  });

  html += '</table>';

  return wrapInExpand(html);
}

module.exports = { generateConfluenceTable };
