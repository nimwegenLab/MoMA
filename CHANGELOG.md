# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Add filtering of assignments based on the intensity of the target components using the image background intensity. This feature is controlled by the setting values `FEATURE_FLAG_FLUORESCENCE_ASSIGNMENT_FILTERING`, `FEATURE_FLUORESCENCE_ASSIGNMENT_FILTERING_CHANNEL_NUMBER` and `FEATURE_FLUORESCENCE_ASSIGNMENT_FILTERING_SIGMA_FACTOR`.
- Add functionality to select a range of hypotheses that have a assignments between them and modify that selection. Current functionality is:
  - Ctrl + LeftClick: Set first hypothesis of the selection range.
  - Ctrl + RightClick: Set last hypothesis of the selection range.
  - Ctrl + m: Force mapping-assignments between selected hypotheses. 
  - Ctrl + i: Force-ignore selected hypotheses. 
  - Ctrl + c: Clear all constraints on the select hypotheses and assignments between them.
- The user-selection for the checkbox "Run optimization on change" is now persisted to `GUI_OPTIMIZE_ON_ILP_CHANGE` in `mm.properties`.

### Changed
- Label-Editor is now opened using Ctrl+Shift+RightClick (previously it was Ctrl+LeftClick).

## [0.8.0] - 2022-11-04

### Added

- Rework formulation and calculation of crossing-constraint.

## [0.7.0] - 2022-10-28

### Added

- Add crossing-constraints to the optimization problem, which make it
- impossible for assignments to cross each other. This feature can be turned off in `mm.properties` by setting `FEATURE_FLAG_CROSSING_CONSTRAINTS=0`. It is on by default.
- Add feature flag `FEATURE_FLAG_MIGRATION_COSTS` in `mm.properties` flag to enable/disable the migration cost. Disable it by default, because we do not need them anymore thanks to crossing-constraints.

## [0.6.0] - 2022-10-25

### Added

- Add saving/loading of probability maps.
- Add saving/loading of curation states using storage of Gurobi model.
- Add saving/loading of component trees.
- Add ability to save/load previously optimized/curated GLs.

### Changed

- Add setting `GUROBI_TIME_LIMIT_DURING_CURATION`, which is used during interactive curation. This can be set to a smaller value than `GUROBI_TIME_LIMIT` to enable more responsiveness during curation. 
- Behavior of the optimization range was changed: Changing the slider for the end of the optimization range does not reset any curations made after the end of the optimization range. Previously, any changes that were made after the end of the optimization range would be lost. 

### Removed

- Remove setting `GUROBI_MAX_OPTIMALITY_GAP`, which was used to ensure that the ILP solution converged at least to the set value. But this confused users, when optimization took much longer than the timeout set by the user in some GLs.

## [0.5.1] - 2022-06-07

### Fixed

- Fix issue with cell masks not being stored to `ExportedCellMasks__*.tif`, when the cell track contains only one cell.
- Fix issue with end of tracking identifier not being output to the CSV file during export.

[unreleased]: https://github.com/michaelmell/moma/compare/v0.6.0...dev
[0.6.0]: https://github.com/michaelmell/moma/compare/v0.5.1...v0.6.0
[0.5.1]: https://github.com/michaelmell/moma/compare/v0.5.0...v0.5.1
