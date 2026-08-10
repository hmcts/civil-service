const fs = require('fs');

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// Column ordering AND human-friendly labels
const COLUMN_ORDER = [
  { key: 'testName', label: 'Test Name' },
  { key: 'steps', label: 'Steps' },
  { key: 'tags', label: 'Tags' },
  { key: 'pipelines', label: 'Pipelines' },
  { key: 'functionalTestGroups', label: 'Functional Test Groups' },
  { key: 'featureName', label: 'Feature Name' },
  { key: 'filePath', label: 'File Path' },
  { key: 'skipped', label: 'Skipped' },
  { key: 'independentScenario', label: 'Independent Scenario' },
];

// LIST FIELDS that get DOUBLE newline separation
const DOUBLE_NEWLINE_FIELDS = new Set([
  'tags',
  'pipelineTags',
  'pipelines',
  'functionalTestGroups'
]);

// Generic formatter with special rules
function safeValue(value, key) {
  if (value === null || value === undefined) return '';

  // SPECIAL CASE: numbered steps
  if (key === 'steps' && Array.isArray(value)) {
    return value
      .map((v, i) => `${i + 1}. ${safeValue(v)}`)
      .join('<br/>');
  }

  // SPECIAL CASE: fields that get two newlines
  if (DOUBLE_NEWLINE_FIELDS.has(key) && Array.isArray(value)) {
    if (value.length === 0) return '';
    return value.map(v => safeValue(v)).join('<br/><br/>');
  }

  // Standard arrays → one newline
  if (Array.isArray(value)) {
    return value.map(v => safeValue(v)).join('<br/>');
  }

  // Objects → pretty JSON with breaks
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

  // Header row
  html += '  <tr>\n';
  COLUMN_ORDER.forEach(({ label }) => {
    html += `    <th>${label}</th>\n`;
  });
  html += '  </tr>\n';

  // Data rows
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
