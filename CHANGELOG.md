# Changelog

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
