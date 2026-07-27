const axios = require('axios');

const {
  CONFLUENCE_PERSONAL_ACCESS_TOKEN,
  CONFLUENCE_BASE_URL,
  CONFLUENCE_PAGE_ID
} = process.env;

const headers = {
  Authorization: `Bearer ${CONFLUENCE_PERSONAL_ACCESS_TOKEN}`,
  'Content-Type': 'application/json'
};

// Match ANY <h2>…</h2>
const H2_BLOCK_REGEX = /<h2[^>]*>[\s\S]*?<\/h2>/gi;

// Match ONLY the expand macro containing the table
const EXPAND_BLOCK_REGEX =
  /<ac:structured-macro[^>]*ac:name="expand"[\s\S]*?<ac:rich-text-body>[\s\S]*?<\/ac:rich-text-body>[\s\S]*?<\/ac:structured-macro>/i;

// Match content until next heading
const CONTENT_AFTER_HEADING_REGEX = /^([\s\S]*?)(?=<h[1-6][^>]*>|$)/i;

// Find the <h2> whose *text content* matches the target
function findTargetHeadingBlock(html, targetText) {
  const targetLower = targetText.toLowerCase();
  let match;

  while ((match = H2_BLOCK_REGEX.exec(html)) !== null) {
    const fullH2 = match[0];

    let inner = fullH2
      .replace(/<h2[^>]*>/i, '')
      .replace(/<\/h2>/i, '');

    const textOnly = inner
      .replace(/<[^>]+>/g, '')
      .replace(/\s+/g, ' ')
      .trim()
      .toLowerCase();

    if (textOnly.includes(targetLower)) {
      return { html: fullH2, index: match.index };
    }
  }

  return null;
}

async function updateConfluencePage({ jsonPath, targetHeadingText, generateConfluenceTable }) {
  try {
    if (!jsonPath) {
      throw new Error('jsonPath argument is required');
    }
    if (!targetHeadingText) {
      throw new Error('targetHeadingText argument is required');
    }
    if (typeof generateConfluenceTable !== 'function') {
      throw new Error('generateConfluenceTable function is required');
    }

    const tableHtml = generateConfluenceTable(jsonPath);

    const res = await axios.get(
      `${CONFLUENCE_BASE_URL}/confluence/rest/api/content/${CONFLUENCE_PAGE_ID}?expand=body.storage,version`,
      { headers }
    );

    const page = res.data;
    const content = page.body.storage.value;
    const version = page.version.number;

    const heading = findTargetHeadingBlock(content, targetHeadingText);
    if (!heading) {
      throw new Error(`Could not find <h2> with text: ${targetHeadingText}`);
    }

    const headingHtml = heading.html;
    const headingEndIndex = heading.index + headingHtml.length;

    const before = content.slice(0, headingEndIndex);
    let after = content.slice(headingEndIndex);

    // Isolate the block under this heading
    const sectionMatch = after.match(CONTENT_AFTER_HEADING_REGEX);
    const section = sectionMatch ? sectionMatch[1] : after;

    // Replace or insert the expand block
    let newSection;

    if (section.match(EXPAND_BLOCK_REGEX)) {
      console.log(`Replacing existing expand block under ${targetHeadingText}.`);
      newSection = section.replace(EXPAND_BLOCK_REGEX, tableHtml);
    } else {
      console.log('No expand block found — inserting new one.');
      newSection = `\n${tableHtml}\n${section}`;
    }

    // Remaining content after the section
    const afterSectionIndex =
      sectionMatch ? sectionMatch.index + section.length : after.length;

    const rest = after.slice(afterSectionIndex);

    const updatedContent = before + newSection + rest;

    const payload = {
      id: CONFLUENCE_PAGE_ID,
      type: 'page',
      title: page.title,
      version: { number: version + 1 },
      body: {
        storage: {
          value: updatedContent,
          representation: 'storage'
        }
      }
    };

    const updateRes = await axios.put(
      `${CONFLUENCE_BASE_URL}/confluence/rest/api/content/${CONFLUENCE_PAGE_ID}`,
      payload,
      { headers }
    );

    console.log('Page updated successfully:', updateRes.status);

  } catch (err) {
    console.error('Error:', err.response?.data || err.message);
    throw err;
  }
}

module.exports = { updateConfluencePage };
