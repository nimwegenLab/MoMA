# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.10.2] - 2023-09-11

- Merge code-changes from v0.9 branch (up to v0.9.8) into v0.10 branch ([fde1e05d](https://github.com/michaelmell/moma/commit/fde1e05d)).
This includes the following changes (check the respective changelog-entries below for more details):
  - adds containerization of MoMA using Docker and Singularity
  - includes a number of fixes

## [0.10.1] - 2023-05-05

- Fix error calculating the cell area using probability map. ([a37e71ee](https://github.com/michaelmell/moma/commit/a37e71ee))

## [0.10.0] - 2023-05-01

- Add new method for calculating the assignment cost by considering the component lengths of source and target components as
given by the size of the oriented bounding box. The legacy way of calculating the assignment cost is maintained as DEFAULT.
  - New functionality:
    - `FEATURE_FLAG_ASSIGNMENT_COST_CALCULATION`: Parameter that sets the method for calculating the assignment cost:
      - `legacy` (DEFAULT): Calculates the assignment cost based on the height of the vertically oriented bounding-box. This corresponds to the legacy behavior.
      - `oriented_bounding_box`: Calculates the assignment cost based on the size of the oriented bounding box of each component.
- Add new assignment type: enter-assignment. The enter-assignment allows MoMA to handle cells that enter the GL during
the measurement. It is needed for Mothermachine devices with media flowing through the GL, which causes cells to be
pulled into the GL _after_ loading and starting the experiment.
  - New functionality:
    - `ENTER_ASSIGNMENT_COST` (DEFAULT=10.0): Parameter that sets the cost for the enter-assignment which is the same for each assignment.
    - A new assignment-view allows the manual selection/modification of enter-assignments. It can be selected by:
      - Pressing the number key `5` with the mouse hovered above the assignment panel changes only the hovered
      panel or pressing `5` with the mouse outside any panel changes all assignment-panels.
    - Cell-lineages starting from an enter-assignment are exported to the CellStats CSV-file.
- Add new way of calculating the migration cost, which uses the total length of components below the source- and target-
components under consideration.
  - New functionality:
    - The parameter `FEATURE_FLAG_MIGRATION_COST_CALCULATION` controls, which method should be use for calculating the 
    migration cost:
      - `absolute_position` (DEFAULT): Calculates the migration cost based on the absolute position of the bottom edge of the
      component bounding-box. This corresponds to the legacy behavior.
      - `total_component_length_below`: calculates the migration cost based on the total length of components below the 
      source- and target-components under consideration.

### Changed

- Make the cost of the exit-assignment configurable with parameter `EXIT_ASSIGNMENT_COST`. Until now, it was hard-coded
with value `0.0`. ([ff51ea28](https://github.com/michaelmell/moma/commit/ff51ea28))
- Storage locks are now only removed, when the user manually runs optimization with the 'Optimize' button in the
UI. This is done to maintain the Gurobi model in th OPTIMAL state, where we can query the variable states
(any modifications to the Gurobi model changes its state it to LOADED, where we cannot query variable state). ([5c11abbf](https://github.com/michaelmell/moma/commit/5c11abbf))
- Change parameter name of the feature flag for the migration cost to `FEATURE_FLAG_MIGRATION_COST` to be consistent in using singular form as in other parameter names (previously it was called `FEATURE_FLAG_MIGRATION_COSTS`). ([598ae56b](https://github.com/michaelmell/moma/commit/598ae56b))

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

## [0.9.0] - 2023-03-20

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

[unreleased]: https://github.com/michaelmell/moma/compare/v0.10.2...dev
[0.10.2]: https://github.com/michaelmell/moma/compare/v0.10.1...v0.10.2
[0.10.1]: https://github.com/michaelmell/moma/compare/v0.10.0...v0.10.1
[0.10.0]: https://github.com/michaelmell/moma/compare/v0.9.0...v0.10.0
[0.9.8]: https://github.com/michaelmell/moma/compare/v0.9.7...v0.9.8
[0.9.7]: https://github.com/michaelmell/moma/compare/v0.9.6...v0.9.7
[0.9.6]: https://github.com/michaelmell/moma/compare/v0.9.5...v0.9.6
[0.9.5]: https://github.com/michaelmell/moma/compare/v0.9.4...v0.9.5
[0.9.4]: https://github.com/michaelmell/moma/compare/v0.9.3...v0.9.4
[0.9.3]: https://github.com/michaelmell/moma/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/michaelmell/moma/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/michaelmell/moma/compare/v0.9.0-alpha6...v0.9.1
[0.9.0]: https://github.com/michaelmell/moma/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/michaelmell/moma/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/michaelmell/moma/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/michaelmell/moma/compare/v0.5.1...v0.6.0
[0.5.1]: https://github.com/michaelmell/moma/compare/v0.5.0...v0.5.1
