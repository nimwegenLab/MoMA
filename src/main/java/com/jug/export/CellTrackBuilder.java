package com.jug.export;

import com.jug.lp.*;
import gurobi.GRBException;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class CellTrackBuilder {
    private List<SegmentRecord> startingPoints = new ArrayList<>();

    public void buildSegmentTracks(Vector<ValuePair<Integer, Hypothesis<Component<FloatType, ?>>>> segmentsInFirstFrameSorted,
                                   final GrowthLineTrackingILP ilp,
                                   int userRangeMaximum) throws GRBException {
        final LinkedList< SegmentRecord > queue = new LinkedList<>();

        int nextCellId = 0;

        startingPoints = new ArrayList<>();

        int cellNum = 0;
        for ( final ValuePair< Integer, Hypothesis<Component<FloatType, ? >>> valuePair : segmentsInFirstFrameSorted ) {

            cellNum++;
            final SegmentRecord point =
                    new SegmentRecord(valuePair.b, nextCellId++, -1, -1, cellNum);
            startingPoints.add( point );

            final SegmentRecord prepPoint = new SegmentRecord(point, 1);
            prepPoint.hyp = point.hyp;

            if ( !prepPoint.hyp.isPruned() ) {
                queue.add( prepPoint );
            }
        }
        while ( !queue.isEmpty() ) {
            final SegmentRecord prepPoint = queue.poll();

            final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> rightAssmt = ilp.getOptimalRightAssignment( prepPoint.hyp );

            if ( rightAssmt == null ) {
                continue;
            }
            // MAPPING -- JUST DROP SEGMENT STATS
            if ( rightAssmt.getType() == GrowthLineTrackingILP.ASSIGNMENT_MAPPING ) {
                final MappingAssignment ma = ( MappingAssignment ) rightAssmt;
                final SegmentRecord next = new SegmentRecord(prepPoint, 1);
                next.hyp = ma.getDestinationHypothesis();
                if ( !prepPoint.hyp.isPruned() ) {
                    queue.add( next );
                }
            }
            // DIVISON -- NEW CELLS ARE BORN CURRENT ONE ENDS
            if ( rightAssmt.getType() == GrowthLineTrackingILP.ASSIGNMENT_DIVISION ) {
                final DivisionAssignment da = ( DivisionAssignment ) rightAssmt;

                prepPoint.pid = prepPoint.id;
                prepPoint.tbirth = prepPoint.frame;

                prepPoint.id = nextCellId;
                prepPoint.hyp = da.getLowerDesinationHypothesis();
                prepPoint.daughterTypeOrPosition = SegmentRecord.LOWER;
                if ( !prepPoint.hyp.isPruned() && !( prepPoint.tbirth > userRangeMaximum ) ) {
                    final SegmentRecord newPoint = new SegmentRecord(prepPoint, 0);
                    newPoint.genealogy.add( SegmentRecord.LOWER );
                    startingPoints.add( newPoint.clone() );
                    newPoint.frame++;
                    queue.add( newPoint );
                    nextCellId++;
                }

                prepPoint.id = nextCellId;
                prepPoint.hyp = da.getUpperDesinationHypothesis();
                prepPoint.daughterTypeOrPosition = SegmentRecord.UPPER;
                if ( !prepPoint.hyp.isPruned() && !( prepPoint.tbirth > userRangeMaximum ) ) {
                    final SegmentRecord newPoint = new SegmentRecord(prepPoint, 0);
                    newPoint.genealogy.add( SegmentRecord.UPPER );
                    startingPoints.add( newPoint.clone() );
                    newPoint.frame++;
                    queue.add( newPoint );
                    nextCellId++;
                }
            }
        }
    }

    public List<SegmentRecord> getStartingPoints() {
        return startingPoints;
    }
}
