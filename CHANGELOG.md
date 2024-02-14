<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# game-cache-config-plugin Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- N/A

## [1.3.2] - 2024-02-13

### Added

- Bump IntelliJ version support to 241.*

## [1.3.1] - 2024-02-13

### Added

- Bump IntelliJ version support to 233.*

## [1.3.0] - 2024-02-03

### Added

- Implement support for mod2, bas2, enum2, hunt2, inv2, loc2, 
maparea2, maplabel2, mel2, npc2, obj2, param2, seq2, struct2, varbit2, 
varclient2, vardouble2, varlong2, varp2, and varstring2 file types.
  - Implement indexing for new mod2 file format. 
  - Implement resolutions for mod2 files to be at config-level rather than file-level.
- Refactor mapfunc to mel.
- Add chart icon to SNT file results instead of property icons.

## [1.2.1] - 2023-10-22

### Added

- Fix initial struct creation autocompletion

## [1.2.0] - 2023-10-22

### Added

- Add struct autocompletion for params.
- Add error verification of struct param values.
- Fix enum error verification.
- Add more descriptive error messages for error verification.

## [1.1.3] - 2023-09-14

### Added

- Update plugin support build number

## [1.1.2] - 2023-05-12

### Added

- Update plugin support build number

## [1.1.1] - 2023-03-19

### Added

- Add hunt, varclient, varclan, and varclansettings to the list of supported cache config files.

## [1.1.0] - 2023-03-13

### Added

- Fix for renaming adding file extension for mod files. 
- Fix for renaming formatting. 
- Add duplicate SNT entry checking.

## [1.0.0] - 2023-03-13

### Added

- Initial scaffold created from IntelliJ Platform Plugin Template. 
- Implement Mod file lookup with CTRL + Click. 
- Implement SNT entry lookup with CTRL + Click.

### Known Issues

- Renaming adds file extension for mod files. 
- Renaming does not format correctly.

[unreleased]: https://github.com/shantyps/game-cache-config-plugin/compare/v1.1.1...HEAD
[1.1.1]: https://github.com/shantyps/game-cache-config-plugin/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/shantyps/game-cache-config-plugin/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/shantyps/game-cache-config-plugin/releases/tag/v1.0.0