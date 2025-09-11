# Changelog

## [v1.0.0] - 2025-09-11
### ⚠️ Breaking changes
- Refactor internal architecture by removing god-class and restructuring core components (PR #3)
- Remove legacy WorldGuard protection classes (`BreakProtection`, `InteractProtection`)

### Added
- Introduce port interfaces for application layer dependencies
- Add use cases for custom block interactions, placement, removal, and queries
- New adapter implementations for action running, chunk indexing, link querying, and placement

### Changed
- Rename and restructure several infrastructure adapters to align with hexagonal architecture
- Update dependency injection module and service implementations
- Refactor controller layers for events and commands

### Removed
- Legacy WorldGuard integration classes replaced with updated versions

### Internal
- Major refactor to ascend god-class logic into modular use cases and ports
- Update version to 1.0.0 in gradle.properties
- Adjust detekt configuration

Thanks @M0ch0 for leading the architectural refactor (PR #3)

## [v0.3.2] - 2025-09-11
### Added
- Add `forceRemoveBy` method to `ClevelandCustomBlocksService` API (#2)

### Changed
- Refactor internal architecture for better consistency across controllers and Bukkit infrastructure (#2)
- Improve dependency structure in event controllers (#2)

### Fixed
- Fix typo in README.md (#2)

### Internal
- Bump version in gradle.properties (#2)
- Rename service classes to adaptors in Bukkit infrastructure for clearer architecture (#2)
- Update MEMO.md to reflect completed automation tasks (#2)
- Update CHANGELOG.md for v0.3.1 (chore)

## [v0.3.1] - 2025-09-11

### Fixed
- Fix hard dependency on Paper 1.21.8 in AdventureI18nBootstrap (#1)

### Changed
- Degrade Paper API build version in gradle/libs.versions.toml
- Bump api-version in plugin.yml
- Change GitHub workflows to use automated manual release method

### Internal
- Update gradle.properties version
- Remove autobuild and generate-changelog workflows
- Add manual-release workflow

Thanks @M0ch0

## [v0.3.0] - 2025-09-10
### Added
- Add WorldEdit integration bootstrap and event handling (PR #79fb820, #927ae18, #608598f)
- Add test cases for core domain entities and use cases (#52032b9)
- Introduce automated changelog generation workflow and script (#f2b45d4, #fb4cdcb, #8016ff1)
- Add provisioning script for Ubuntu 24 runners (#2f89768)

### Changed
- Bump project version in gradle.properties (#c66b5c1)
- Update MEMO.md to reflect completed tasks (#60b0249, #608598f)
- Adjust detekt configuration and Gradle setup (#detekt.yml, #build.gradle.kts)

### Fixed
- Fix environment issues in autobuild workflow (#eeedaba, #964c968)
- Resolve CA issue and skip detektClasses in runner setup (#8c09c5e, #8d56fc2)
- Correct missed instruction path in changelog workflow (#219654f)
- Fix logic in changelog generation script (#787555a)

### Removed
- Remove outdated TODO.md in favor of MEMO.md (#608598f)
- Delete legacy CodeQL workflow (#93f13cb)

### Internal
- Refactor DI module to include WorldEditBootstrap (#0cfc158)
- Migrate CI workflows to consolidated autobuild system (#a950486)
- Update Gradle wrapper and dependencies (#gradle-wrapper.jar)
- Merge branch updates and internal documentation tweaks (#8829dbb)

All notable changes to this project will be documented in this file.
