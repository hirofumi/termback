<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# termback Changelog

## [Unreleased]

### Changed

- Bind terminal sessions from `startupOptionsDeferred` instead of selecting a `Content` tab heuristically
- Require IntelliJ 2026.1 or later

## [0.0.3] - 2026-02-08

### Added

- macOS native notification support for better noticeability when IDE is in background

### Removed

- **Breaking:** `broadcast` field (notifications now target all projects)

## [0.0.2] - 2026-01-16

### Added

- Keyboard shortcut (Alt+Shift+T) to pick a notification and jump to its terminal tab

## [0.0.1] - 2026-01-04

### Added

- Terminal-to-IDE notification system

[Unreleased]: https://github.com/hirofumi/termback/compare/v0.0.3...HEAD
[0.0.3]: https://github.com/hirofumi/termback/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/hirofumi/termback/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/hirofumi/termback/commits/v0.0.1
