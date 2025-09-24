import fs from 'fs';
import fsAsync from 'fs/promises';
import path from 'path';
import FileError from '../errors/file-error';
import FileType from '../enums/file-type';
import filePaths from '../config/file-paths';

export default class FileSystemHelper {
  private static writePaths = [
    `${filePaths.users}/`,
    `${filePaths.userAssignedCases}/`,
    `${filePaths.userCookies}/`,
    `${filePaths.axe}/`,
    filePaths.bankHolidaysJson,
  ];

  static exists = (filePath: string) => fs.existsSync(filePath);

  private static mkDir = (filePath: string) => {
    const dirPath = path.dirname(filePath);
    if (!this.exists(filePath)) {
      fs.mkdirSync(dirPath, { recursive: true });
    }
  };

  private static encode = (data: any, fileType: FileType): any => {
    switch (fileType) {
      case FileType.JSON:
        return JSON.stringify(data, null, 2);
      case FileType.PNG:
        return data;
    }
  };

  private static decode = (data: Buffer, fileType: FileType): any => {
    switch (fileType) {
      case FileType.JSON:
        return JSON.parse(data.toString('utf8'));
    }
  };

  private static canWrite = (filePath: string) => {
    let canWrite = false;
    for (const writeFileDir of this.writePaths) {
      if (filePath.startsWith(writeFileDir)) {
        canWrite = true;
        break;
      }
    }
    return canWrite;
  };

  private static validateFilePath = (filePath: string, fileType: FileType) => {
    if (!filePath) {
      throw new FileError('File path must be a string with a length greater than 0');
    }
    if (!filePath.endsWith(`.${fileType.toLowerCase()}`)) {
      throw new FileError(`File path ${filePath} should end with .${fileType}`);
    }
    if (!this.canWrite(filePath)) {
      throw new FileError(`Cannot write ${fileType} to file path ${filePath}`);
    }
  };

  private static validateFile = (filePath = '', fileType: FileType) => {
    if (!this.exists(filePath)) {
      throw new FileError(
        `Failed to read ${fileType} with path ${filePath}. File path is invalid or does not exist.`,
      );
    }
  };

  static readFile = (filePath = '', fileType: FileType): any => {
    this.validateFile(filePath, fileType);
    const data = fs.readFileSync(filePath);
    return this.decode(data, fileType);
  };

  static readFileAsync = async (filePath = '', fileType: FileType): Promise<any> => {
    this.validateFile(filePath, fileType);
    const data = await fsAsync.readFile(filePath);
    return this.decode(data, fileType);
  };

  static writeFile = (
    data: any,
    filePath = '',
    fileType: FileType,
    { force }: { force?: boolean } = { force: false },
  ): void => {
    if (!force) this.validateFilePath(filePath, fileType);
    data = this.encode(data, fileType);
    this.mkDir(filePath);
    fs.writeFileSync(filePath, data);
  };

  static writeFileAsync = async (
    data: any,
    filePath = '',
    fileType: FileType,
    { force }: { force?: boolean } = { force: false },
  ): Promise<void> => {
    if (!force) this.validateFilePath(filePath, fileType);
    data = this.encode(data, fileType);
    this.mkDir(filePath);
    await fsAsync.writeFile(filePath, data);
  };

  static delete = (
    path = '',
    { force, quiet }: { force?: boolean; quiet?: boolean } = { force: false, quiet: false },
  ) => {
    try {
      if (!force) {
        if (!path) {
          throw new FileError('Folder/File path cannot be an empty string');
        }
        if (!this.canWrite(path)) {
          throw new FileError(`Cannot delete folder/file from path ${path}`);
        }
      }
      const stats = fs.lstatSync(path);

      if (stats.isDirectory()) {
        fs.rmSync(path, { recursive: true, force: true });
        if (!quiet) console.log(`Successfully deleted folder with path ${path}`);
      } else {
        fs.unlinkSync(path);
        if (!quiet) console.log(`Successfully deleted file with path ${path}`);
      }
    } catch (error: any) {
      if (error.code === 'ENOENT') {
        console.log(error.message);
      } else {
        console.log(error);
      }
    }
  };
}
