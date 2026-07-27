const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const pixelmatch = require('pixelmatch');
const { PNG } = require('pngjs');
const codeceptjs = require('codeceptjs');

const PIXELMATCH_THRESHOLD = 0.15;
const MAX_MISMATCH_PERCENT = 0.01;

// Controls whether PDF baselines can be created/updated.
// Default behaviour:
// - Tests FAIL if baseline is missing (prevents silent regressions)
// To update/create baselines locally:
//   UPDATE_PDF_BASELINE=true npx codeceptjs run <test-file>
const UPDATE_PDF_BASELINE = process.env.UPDATE_PDF_BASELINE === 'true';

function ensureCleanDir(dir) {
  if (fs.existsSync(dir)) {
    fs.rmSync(dir, { recursive: true, force: true });
  }
  fs.mkdirSync(dir, { recursive: true });
}

function renderPdfPagesToPng(pdfFile, outputDir) {
  try {
    execFileSync('pdftoppm', ['-v'], { stdio: 'ignore' });
  } catch {
    throw new Error('pdftoppm not found. Install poppler: brew install poppler / apt-get install poppler-utils');
  }

  ensureCleanDir(outputDir);

  const outputPrefix = path.join(outputDir, 'page');

  execFileSync('pdftoppm', ['-png', '-r', '150', pdfFile, outputPrefix]);

  return fs.readdirSync(outputDir)
    .filter(file => file.endsWith('.png'))
    .sort()
    .map(file => path.join(outputDir, file));
}

function compareRenderedPdfPages(actualPngs, expectedPngs, diffDir) {
  ensureCleanDir(diffDir);

  const failures = [];

  if (actualPngs.length !== expectedPngs.length) {
    failures.push({
      page: 'all',
      reason: 'PDF page count mismatch',
      actualPageCount: actualPngs.length,
      expectedPageCount: expectedPngs.length
    });
  }

  const comparablePages = Math.min(actualPngs.length, expectedPngs.length);

  for (let i = 0; i < comparablePages; i++) {
    const actualImg = PNG.sync.read(fs.readFileSync(actualPngs[i]));
    const expectedImg = PNG.sync.read(fs.readFileSync(expectedPngs[i]));

    if (
      actualImg.width !== expectedImg.width ||
      actualImg.height !== expectedImg.height
    ) {
      failures.push({
        page: i + 1,
        reason: 'Image dimensions differ',
        actual: `${actualImg.width}x${actualImg.height}`,
        expected: `${expectedImg.width}x${expectedImg.height}`
      });
      continue;
    }

    const diff = new PNG({
      width: actualImg.width,
      height: actualImg.height
    });

    const mismatchPixels = pixelmatch(
      actualImg.data,
      expectedImg.data,
      diff.data,
      actualImg.width,
      actualImg.height,
      { threshold: PIXELMATCH_THRESHOLD }
    );

    const totalPixels = actualImg.width * actualImg.height;
    const mismatchPercent = mismatchPixels / totalPixels;

    console.log(
      `PDF page ${i + 1}: ${mismatchPixels} pixels different (${(mismatchPercent * 100).toFixed(4)}%)`
    );

    if (mismatchPercent > MAX_MISMATCH_PERCENT) {
      const diffPath = path.join(diffDir, `page-${i + 1}-diff.png`);
      fs.writeFileSync(diffPath, PNG.sync.write(diff));

      failures.push({
        page: i + 1,
        mismatchPixels,
        mismatchPercent: `${(mismatchPercent * 100).toFixed(4)}%`,
        diffPath
      });
    }
  }

  return failures;
}

function copyPdfDiffsToFunctionalArtifacts(failures, artifactName) {
  const artifactDir = path.resolve('test-results/functional/pdf-diffs');
  fs.mkdirSync(artifactDir, { recursive: true });

  failures.forEach(f => {
    if (f.diffPath && fs.existsSync(f.diffPath)) {
      const target = path.join(
        artifactDir,
        `${artifactName}-page-${f.page}-diff.png`
      );

      fs.copyFileSync(f.diffPath, target);
      console.log('Saved diff to artifact:', target);
    }
  });
}

function getAllurePlugin() {
  return codeceptjs.container.plugins('allure');
}

function attachDiffsToAllure(failures, actualPngs, expectedPngs) {
  const allure = getAllurePlugin();
  if (!allure || typeof allure.addAttachment !== 'function') return;

  failures.forEach(f => {
    if (f.page === 'all') return;

    const index = f.page - 1;

    [
      [`Page ${f.page} - Actual`, actualPngs[index]],
      [`Page ${f.page} - Expected`, expectedPngs[index]],
      [`Page ${f.page} - Diff`, f.diffPath]
    ].forEach(([name, filePath]) => {
      if (filePath && fs.existsSync(filePath)) {
        allure.addAttachment(name, fs.readFileSync(filePath), 'image/png');
      }
    });
  });
}

function attachPdfsToAllure(actualFile, expectedFile) {
  const allure = getAllurePlugin();
  if (!allure || typeof allure.addAttachment !== 'function') return;

  if (fs.existsSync(actualFile)) {
    allure.addAttachment(
      'Actual PDF',
      fs.readFileSync(actualFile),
      'application/pdf'
    );
  }

  if (fs.existsSync(expectedFile)) {
    allure.addAttachment(
      'Expected PDF (baseline)',
      fs.readFileSync(expectedFile),
      'application/pdf'
    );
  }
}

function waitForFileStable(file, timeout = 10000) {
  return new Promise((resolve, reject) => {
    let lastSize = 0;
    let stableCount = 0;
    const start = Date.now();

    const interval = setInterval(() => {
      try {
        if (Date.now() - start > timeout) {
          clearInterval(interval);
          return reject(new Error(`Timeout waiting for file: ${file}`));
        }

        if (!fs.existsSync(file)) return;

        const size = fs.statSync(file).size;

        if (size > 0 && size === lastSize) {
          stableCount++;
        } else {
          stableCount = 0;
        }

        lastSize = size;

        if (stableCount >= 3) {
          clearInterval(interval);
          resolve();
        }
      } catch (e) {
        console.warn('waitForFileStable error:', e);
      }
    }, 300);
  });
}

async function downloadPdf(I, actualFile) {
  fs.mkdirSync(path.dirname(actualFile), { recursive: true });

  if (fs.existsSync(actualFile)) {
    fs.unlinkSync(actualFile);
  }

  await I.usePlaywrightTo('download pdf', async ({ page }) => {
    const [download] = await Promise.all([
      page.waitForEvent('download', { timeout: 60000 }),

      (async () => {
        await page.locator('#mvMoreOptionsBtn').click();
        await page.waitForTimeout(500);

        const btn = page.getByRole('button', { name: 'Download' }).first();
        await btn.waitFor({ state: 'visible', timeout: 20000 });
        await btn.click();
      })()
    ]);

    await download.saveAs(actualFile);
  });

  await waitForFileStable(actualFile);
}

async function downloadPdfAndAssertVisualMatch({
  I,
  actualFile,
  expectedFile,
  actualPngDir,
  expectedPngDir,
  diffPngDir
}) {
  await downloadPdf(I, actualFile);

  if (!fs.existsSync(expectedFile)) {
    if (!UPDATE_PDF_BASELINE) {
      throw new Error(
        `Missing PDF baseline: ${expectedFile}. Run with UPDATE_PDF_BASELINE=true to create it.`
      );
    }

    fs.mkdirSync(path.dirname(expectedFile), { recursive: true });
    fs.copyFileSync(actualFile, expectedFile);
    console.log('Baseline PDF created');
    return;
  }

  if (UPDATE_PDF_BASELINE) {
    fs.copyFileSync(actualFile, expectedFile);
    console.log('Baseline PDF updated');
    return;
  }

  const actualPngs = renderPdfPagesToPng(actualFile, actualPngDir);
  const expectedPngs = renderPdfPagesToPng(expectedFile, expectedPngDir);

  const failures = compareRenderedPdfPages(actualPngs, expectedPngs, diffPngDir);

  if (failures.length > 0) {
    const artifactName = path.basename(expectedFile, path.extname(expectedFile));

    copyPdfDiffsToFunctionalArtifacts(failures, artifactName);
    attachDiffsToAllure(failures, actualPngs, expectedPngs);
    attachPdfsToAllure(actualFile, expectedFile);

    throw new Error(
      `PDF visual mismatch detected: ${JSON.stringify(failures, null, 2)}`
    );
  }
}

function getPdfPaths(testDir, baselineDir, pdfName) {
  return {
    actualFile: path.join(testDir, 'downloads/actual', pdfName),
    expectedFile: path.join(baselineDir, pdfName),
    actualPngDir: path.join(testDir, 'data/actualPngs', pdfName),
    expectedPngDir: path.join(testDir, 'data/expectedPngs', pdfName),
    diffPngDir: path.join(testDir, 'data/diffPngs', pdfName)
  };
}

module.exports = {
  downloadPdfAndAssertVisualMatch,
  getPdfPaths
};
