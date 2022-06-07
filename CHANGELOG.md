# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- enable loading/saving of curation states using storage of Gurobi model

## [0.5.1] - 2022-06-07

### Fixed

- Fix issue with cell masks not being stored to `ExportedCellMasks__*.tif`, when the cell track contains only one cell.
- Fix issue with end of tracking identifier not being output to the CSV file during export.

[unreleased]: https://github.com/michaelmell/moma/compare/v0.5.1...dev
[0.5.1]: https://github.com/michaelmell/moma/compare/v0.5.0...v0.5.1
