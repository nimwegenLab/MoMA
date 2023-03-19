package com.jug.export;

import com.jug.GrowthlaneFrame;
import com.jug.lp.*;
import com.jug.util.componenttree.AdvancedComponent;
import gurobi.GRBException;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.isNull;

public class CellTrackBuilder {
    private List<SegmentRecord> startingPoints = new ArrayList<>();

    public void buildSegmentTracks(List<Hypothesis<AdvancedComponent<FloatType>>> hypotheses,
                                   GrowthlaneFrame firstGlf,
                                   final GrowthlaneTrackingILP ilp,
                                   int userRangeMaximum) throws GRBException {

        final LinkedList<SegmentRecord> queue = new LinkedList<>();

        int nextCellId = 1; /* start at 1 instead 0; we do this, because this value is used to write the cell mask to the exported TIFF-stack, where 0 is indicates the background value */

        startingPoints = new ArrayList<>();

        for (final Hypothesis<AdvancedComponent<FloatType>> hypothesis : hypotheses) {
            final int cellRank = firstGlf.getSolutionStats_cellRank(hypothesis);

            final SegmentRecord point =
                    new SegmentRecord(hypothesis, nextCellId++, -1, -1, cellRank, ilp);
            startingPoints.add(point);

            final SegmentRecord prepPoint = new SegmentRecord(point, 1, ilp);
            prepPoint.hyp = point.hyp;

            if (!prepPoint.hyp.isPruned()) {
                queue.add(prepPoint);
            }
        }
        while (!queue.isEmpty()) {
            final SegmentRecord prepPoint = queue.poll();

            final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> rightAssmt = ilp.getOptimalRightAssignment(prepPoint.hyp);

            if (isNull(rightAssmt)) {
                throw new AssertionError("The optimal right-assigment should never be null here!");
            }

            // MAPPING -- JUST DROP SEGMENT STATS
            if (rightAssmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_MAPPING) {
                final MappingAssignment ma = (MappingAssignment) rightAssmt;
                final SegmentRecord next = new SegmentRecord(prepPoint, 1, ilp);
                next.hyp = ma.getDestinationHypothesis();
                if (!prepPoint.hyp.isPruned()) {
                    queue.add(next);
                }
            }
            // DIVISON -- NEW CELLS ARE BORN CURRENT ONE ENDS
            if (rightAssmt.getType() == GrowthlaneTrackingILP.ASSIGNMENT_DIVISION) {
                final DivisionAssignment da = (DivisionAssignment) rightAssmt;

                prepPoint.parentId = prepPoint.id;
//                prepPoint.setParentId(prepPoint.id);
                prepPoint.timeOfBirth = prepPoint.timestep;

                prepPoint.id = nextCellId;
                prepPoint.hyp = da.getLowerDestinationHypothesis();
                prepPoint.daughterTypeOrPosition = SegmentRecord.LOWER;
                if (!prepPoint.hyp.isPruned() && !(prepPoint.timeOfBirth > userRangeMaximum)) {
                    final SegmentRecord newPoint = new SegmentRecord(prepPoint, 0, ilp); // NOTE: this is not a bug, due to the call below to 'newPoint.timestep++'; but this is extremely convoluted!!
                    newPoint.genealogy.add(SegmentRecord.LOWER);
                    startingPoints.add(newPoint.clone());
                    newPoint.timestep++;
                    queue.add(newPoint);
                    nextCellId++;
                }

                prepPoint.id = nextCellId;
                prepPoint.hyp = da.getUpperDestinationHypothesis();
                prepPoint.daughterTypeOrPosition = SegmentRecord.UPPER;
                if (!prepPoint.hyp.isPruned() && !(prepPoint.timeOfBirth > userRangeMaximum)) {
                    final SegmentRecord newPoint = new SegmentRecord(prepPoint, 0, ilp); // NOTE: this is not a bug, due to the call below to 'newPoint.timestep++'; but this is extremely convoluted!!
                    newPoint.genealogy.add(SegmentRecord.UPPER);
                    startingPoints.add(newPoint.clone());
                    newPoint.timestep++;
                    queue.add(newPoint);
                    nextCellId++;
                }
            }
        }
    }

    public List<SegmentRecord> getStartingPoints() {
        return startingPoints;
    }
}
