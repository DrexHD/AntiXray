# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Improved chunk batch sending

## [1.4.0] - 2024-05-30
### Added
- Neoforge support

### Changed
- Mod publishing
- Multiloader setup

### Fixed
- Packet delaying causing packet order issues

### Removed
- Forge support (temporary)

## [1.3.3] - 2024-02-18
### Added
- Antixray can now be used client-side (useful for LAN)

## [1.3.2] - 2024-02-08
### Fixed
- Incompatibility with Immersive Portals (Fabric only)

## [1.3.1] - 2023-07-14
### Added
- Allow custom dimension id's to be specified in the config

## [1.3.0] - 2023-01-13
### Added
- Block tag support (in configs)
- Layer obfuscation (Engine Mode 3)

### Removed
- Bypass permission: "antixray.bypass"

### Changed
- Relocate forge dependencies
- Improved default config

## [1.2.8] - 2022-12-19
### Fixed
- NPE server crash

## [1.2.7] - 2022-12-14
### Added
- Bypass permission: "antixray.bypass"

### Fixed
- Typo in forge release names

## [1.2.6] - 2022-09-08
### Added
- Automatic Modrinth, CurseForge and GitHub releases

### Changed
- Update to 1.19.2

## [1.2.5] - 2022-08-02
### Changed
- Update to 1.19.1

## [1.2.4] - 2022-07-19
### Fixed
- Mod incompatibility with C2ME

## [1.2.3] - 2022-07-15
### Changed
- Update to 1.19
- Update dependencies

### Fixed
- Forge 1.19 version

## [1.2.2] - 2022-03-19
### Changed
- Rework build script
- Update to 22w11a

## [1.2.1] - 2022-03-01
### Changed
- Update to 1.18.2
- Include and update forge

## [1.2.0] - 2022-01-19
### Added
- Architectury and project modules

### Fixed
- Mod incompatibility with Architectury API

## [1.1.2] - 2021-12-04
### Changed
- Improve build script
- Rewrite for 1.18

### Fixed
- GitHub actions

## [1.1.1] - 2021-11-23
# Added
- Polymer mod compatibility

### Changed
- Updated to 1.18
- Optimize preset palette
- Simplify project name

### Fixed
- quick-carpet compatibility

## [1.1.0] - 2021-09-05
### Added
- Default config values

### Changed
- Reformat code
- Update gradle, loom and fabric api
- Include Minecraft version in the file name
- Rework mixins for better mod compatibility

## [1.0.2] - 2021-08-25
### Changed
- Optimize imports
- Update Minecraft's netty version

## [1.0.1] - 2021-08-13
### Changed
- Rework some mixins

### Fixed
- Mod not working if config file isn't present

## [1.0.0] - 2021-08-05
### Added
- Ported Papers Antixray engine to Fabric