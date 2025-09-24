import filePaths from '../config/file-paths';
import FileType from '../enums/file-type';
import { AxeResults, PageResult } from '../models/axe-results';
import FileSystemHelper from './file-system-helper';

//TODO: Could be a potential concurrency issue when caching page results when multiple workers are running but will fix later.
//e.g. Two workers could be caching at the same time.

export default class AxeCacheHelper {
  private static getResultsFilePath(projectName: string) {
    return `${filePaths.axe}/${projectName}/accessibility-results.json`;
  }

  static async getAxePageResult(projectName: string, pageName: string): Promise<PageResult | null> {
    try {
      const axeResults: AxeResults = await FileSystemHelper.readFileAsync(
        this.getResultsFilePath(projectName),
        FileType.JSON,
      );
      return axeResults?.[projectName]?.[pageName] ?? null;
    } catch (error) {
      if (error.name !== 'FileError') {
        throw error;
      }
      return null;
    }
  }

  static async writeAxePageResult(
    projectName: string,
    pageName: string,
    testName: string,
    pass: boolean,
    violations: any[],
    screenshot?: Buffer,
  ): Promise<PageResult> {
    const resultsFilePath = this.getResultsFilePath(projectName);
    let axeResults: AxeResults = {};
    try {
      axeResults = await FileSystemHelper.readFileAsync(resultsFilePath, FileType.JSON);
    } catch (error) {
      if (error.name !== 'FileError') {
        throw error;
      }
    }

    let screenshotInfo: { fileName: string; filePath: string } | undefined;
    let violationsInfo: { length: number; fileName: string; filePath: string } | undefined;

    if (violations.length) {
      const violationsFileName = `${pageName}-accessibility-violations.json`;
      const violationsFilePath = `${filePaths.axe}/${projectName}/violations/${violationsFileName}`;
      await FileSystemHelper.writeFileAsync(violations, violationsFilePath, FileType.JSON);
      violationsInfo = {
        length: violations.length,
        fileName: violationsFileName,
        filePath: violationsFilePath,
      };
    }
    if (screenshot) {
      const screenshotFileName = `${pageName}-accessibility-failure.png`;
      const screenshotFilePath = `${filePaths.axe}/${projectName}/screenshots/${screenshotFileName}`;
      await FileSystemHelper.writeFileAsync(screenshot, screenshotFilePath, FileType.PNG);
      screenshotInfo = { fileName: screenshotFileName, filePath: screenshotFilePath };
    }
    const pageResults = {
      testName,
      pass,
      violationsInfo,
      screenshotInfo,
    };

    axeResults[pageName] = pageResults;
    await FileSystemHelper.writeFileAsync(axeResults, resultsFilePath, FileType.JSON);
    return pageResults;
  }

  static deleteAllCache() {
    FileSystemHelper.delete(`${filePaths.axe}/`);
  }
}
