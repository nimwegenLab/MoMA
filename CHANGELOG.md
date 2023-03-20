# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Add new assignment type: enter-assignment. The enter-assignment allows MoMA to handle cells that enter the GL during
the measurement. It is needed for Mothermachine devices with media flowing through the GL, which causes cells to be
pulled into the GL _after_ loading and starting the experiment.
  - New functionality:
    - The parameter `ENTER_ASSIGNMENT_COST` sets the cost for the enter-assignment which is the same for each assignment.
    - A new assignment-view allows the manual selection/modification of enter-assignments. It can be selected by:
      - Pressing the number key `5` with the mouse hovered above the assignment panel changes only the hovered
      panel or pressing `5` with the mouse outside any panel changes all assignment-panels.
    - Cell-lineages starting from an enter-assignment are exported to the CellStats CSV-file.

### Changed

- Make the cost of the exit-assignment configurable with parameter `EXIT_ASSIGNMENT_COST`. Until now, it was hard-coded
with value `0.0`. ([ff51ea28](https://github.com/michaelmell/moma/commit/ff51ea28))
- Storage locks are now only removed, when the user manually runs optimization with the 'Optimize' button in the
UI. This is done to maintain the Gurobi model in th OPTIMAL state, where we can query the variable states
(any modifications to the Gurobi model changes its state it to LOADED, where we cannot query variable state). ([5c11abbf](https://github.com/michaelmell/moma/commit/5c11abbf))
- Change parameter name of the feature flag for the migration cost to `FEATURE_FLAG_MIGRATION_COST` (previously it was called `FEATURE_FLAG_MIGRATION_COSTS`). ([598ae56b](https://github.com/michaelmell/moma/commit/598ae56b))

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
    - Ctrl + R: Removed all constraints on the selected hypotheses and the assignments between them.
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

[unreleased]: https://github.com/michaelmell/moma/compare/v0.9.0...dev
[0.9.0]: https://github.com/michaelmell/moma/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/michaelmell/moma/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/michaelmell/moma/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/michaelmell/moma/compare/v0.5.1...v0.6.0
[0.5.1]: https://github.com/michaelmell/moma/compare/v0.5.0...v0.5.1
