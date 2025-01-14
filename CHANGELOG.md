# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.9.8] - 2023-09-04

### Fixed
- Fixed issue that occurred when running with option `-reload`. In this situation, we would not have an `mm.properties` 
and the `moma.py` would try to load the default `mm.properties`, which is not what we want (we want to load the 
`mm.properties` file from the path that is being loaded). 

## [0.9.7] - 2023-08-17

### Changed
- First containerized version that can be run using Docker and Singularity.

## [0.9.6] - 2023-08-07

## Added

- Add logic to handle the case, when the first frame of a GL is empty:
  - Output indicator file `WARNING_GROWTHLANE_EMPTY` to the tracking-data directory to indicate that the first frame of
    the GL is empty and therefore the the GL cannot be tracked. This indicator file is used subsequently, when loading
    the GL to not perform any further operations after the "TRACKING" stage of the batch-tracking workflow.
  - Show error message during interactive run, when optimizing GLs with no cells in the first frame.

## [0.9.5] - 2023-07-26

## Added

- Add setting `GROWTHLANE_ID_REGEX` to `mm.properties` to set the regular expression, which is used to extract the
  growthlane id from the filename of  the image. Default value: `_(GL[0-9]*)\\.tif`
- Add setting `POSITION_ID_REGEX` to `mm.properties` to set the regular expression, which is used to extract the
  position id from the filename of  the image. Default value: `([-0-9]*Pos\\d+)_`

## [0.9.4] - 2023-07-20

### Added

- Add command line flag `multithreaded` to enable multithreading. This is disabled by default. Using e.g.
  `moma -multithreaded ...` will use multi-threading, when generating the component-trees.

### Changed

- Disable all multithreading by default.

### Fixed

- Fixed parsing of version string from the Git.

## [0.9.3] - 2023-06-01

### Changed

- Add parameter `MAXIMUM_DOWNWARD_MOVEMENT` which sets the maximum allowed downward movement for cells.
  Default value [pixel]: 50

## [0.9.2] - 2023-05-26

### Changed

- Add basic logging of user interactions.

## [0.9.1] - 2023-05-02

### Changed

- Fix crashing issue exporting cell area using the probability map. 

## [0.9.0-alpha6] - 2023-01-21

### Added

- Add filtering of assignments based on the intensity of the target components using the image background intensity. This feature is controlled by the setting values `FEATURE_FLAG_FLUORESCENCE_ASSIGNMENT_FILTERING`, `FEATURE_FLUORESCENCE_ASSIGNMENT_FILTERING_CHANNEL_NUMBER` and `FEATURE_FLUORESCENCE_ASSIGNMENT_FILTERING_SIGMA_FACTOR`.
- Add functionality to select a range of hypotheses that have assignments between them and modify that selection. Current functionality is:
  - Selecting hypotheses:
    - Ctrl + LeftClick: Set first hypothesis of the selection range.
    - Ctrl + RightClick: Set last hypothesis of the selection range.
    - ESC: Deselect currently selected hypotheses.
  - Modifying hypotheses:
    - Ctrl + F: Force currently active assignments between selected hypotheses. This helps avoid changes to a correctly tracked lineage, when performing further modification on the same time-steps later on.
    - Ctrl + M: Force mapping-assignments between selected hypotheses. This will force the currently active mapping-assignments and replace active division assignments with forced mapping-assignments.
    - Ctrl + Shift + D: Force-ignore all division assignments that start from selected hypotheses.
    - Ctrl + I: Force-ignore selected hypotheses.
    - Ctrl + R: Remove all constraints on the selected hypotheses and the assignments between them.
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

[unreleased]: https://github.com/michaelmell/moma/compare/v0.9...dev
[0.9.8]: https://github.com/michaelmell/moma/compare/v0.9.7...v0.9.8
[0.9.7]: https://github.com/michaelmell/moma/compare/v0.9.6...v0.9.7
[0.9.6]: https://github.com/michaelmell/moma/compare/v0.9.5...v0.9.6
[0.9.5]: https://github.com/michaelmell/moma/compare/v0.9.4...v0.9.5
[0.9.4]: https://github.com/michaelmell/moma/compare/v0.9.3...v0.9.4
[0.9.3]: https://github.com/michaelmell/moma/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/michaelmell/moma/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/michaelmell/moma/compare/v0.9.0-alpha6...v0.9.1
[0.9.0-alpha6]: https://github.com/michaelmell/moma/compare/v0.8.0...v0.9.0-alpha6
[0.8.0]: https://github.com/michaelmell/moma/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/michaelmell/moma/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/michaelmell/moma/compare/v0.5.1...v0.6.0
[0.5.1]: https://github.com/michaelmell/moma/compare/v0.5.0...v0.5.1
