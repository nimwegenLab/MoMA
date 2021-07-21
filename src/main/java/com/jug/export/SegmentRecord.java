package com.jug.export;

import com.jug.MoMA;
import com.jug.lp.AbstractAssignment;
import com.jug.lp.GrowthLineTrackingILP;
import com.jug.lp.Hypothesis;
import com.jug.lp.MappingAssignment;
import gurobi.GRBException;
import net.imglib2.IterableInterval;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SegmentRecord {

    static final int ENDOFTRACKING = 1234;
    static final int USER_PRUNING = 4321;


    boolean exists = true;

    /**
     * The ID of this cell.
     */
    int id;

    /**
     * The parent ID.
     */
    int pid;

    /**
     * Frame in which this cell was first observed (i.e. born).
     */
    int tbirth;

    /**
     * The type of daughter-cell this is. This index corresponds to either TOP, BOTTOM or UNKNOWN as defined below.
     * UNKNOWN is returned for cells that already existed in the first frame.
     */
    int daughterTypeOrPosition;
    // Note: if daughterTypeOrPosition is set to a positive value $i$ -- the given cell is the i-th cell in the growth line (with the mother cell being i=1.
    static final int LOWER = -1;
    static final int UPPER = -2;

    /**
     * The frame that this segments belongs to.
     */
    int frame;

    final List< Integer > genealogy;

    Hypothesis<Component<FloatType, ? >> hyp;
    int terminated_by = Integer.MIN_VALUE;

    SegmentRecord(
            final Hypothesis<Component<FloatType, ?>> hyp,
            final int id,
            final int pid,
            final int tbirth,
            final int daughterTypeOrPosition,
            final List<Integer> genealogy) {
        this.hyp = hyp;
        this.id = id;
        this.pid = pid;
        this.tbirth = tbirth;
        this.daughterTypeOrPosition = daughterTypeOrPosition;
        this.genealogy = genealogy;
        this.frame = 0;
    }

    SegmentRecord(
            final Hypothesis<Component<FloatType, ?>> hyp,
            final int id,
            final int pid,
            final int tbirth,
            final int daughterTypeOrPosition) {
        this.hyp = hyp;
        this.id = id;
        this.pid = pid;
        this.tbirth = tbirth;
        this.daughterTypeOrPosition = daughterTypeOrPosition;
        this.genealogy = new ArrayList<>();
        genealogy.add( daughterTypeOrPosition );
        this.frame = 0;
    }

    SegmentRecord(final SegmentRecord point, final int frameOffset) {
        this.hyp = point.hyp;
        this.id = point.id;
        this.pid = point.pid;
        this.tbirth = point.tbirth;
        this.daughterTypeOrPosition = point.daughterTypeOrPosition;
        this.frame = point.frame + frameOffset;
        this.genealogy = new ArrayList<>(point.genealogy);
    }

    @Override
    public SegmentRecord clone() {
        final SegmentRecord ret =
                new SegmentRecord(this.hyp, this.id, this.pid, this.tbirth, this.daughterTypeOrPosition, this.genealogy);
        ret.exists = this.exists;
        ret.frame = this.frame;
        ret.terminated_by = this.terminated_by;
        return ret;
    }

    @Override
    public String toString() {
        String dt = "UNKNOWN";
        if ( daughterTypeOrPosition == SegmentRecord.UPPER ) dt = "TOP";
        if ( daughterTypeOrPosition == SegmentRecord.LOWER ) dt = "BOTTOM";
        if ( daughterTypeOrPosition >= 0 ) dt = "CELL#" + daughterTypeOrPosition;
        return String.format( "id=%d; pid=%d; birth_frame=%d; daughter_type=%s", id, pid, tbirth, dt );
    }

    public int getId() {
        return id;
    }

    public int getParentId(){
        return pid;
    }

    public String getTerminationIdentifier(){
        if (terminated_by == GrowthLineTrackingILP.ASSIGNMENT_EXIT) {
            return "exit";
        } else if (terminated_by == GrowthLineTrackingILP.ASSIGNMENT_LYSIS) {
            return "lysis";
        } else if (terminated_by == GrowthLineTrackingILP.ASSIGNMENT_DIVISION) {
            return "div";
        } else if (terminated_by == SegmentRecord.USER_PRUNING) {
            return "pruned";
        } else if (terminated_by == SegmentRecord.ENDOFTRACKING) {
            return "eod";
        } else {
            return "";
        }
    }

    /**
     * @return
     */
    SegmentRecord nextSegmentInTime(final GrowthLineTrackingILP ilp) {
        SegmentRecord ret = this;

        exists = true;
        try {
            final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> rightAssmt = ilp.getOptimalRightAssignment( this.hyp );
            if ( rightAssmt == null ) {
                exists = false;
                terminated_by = SegmentRecord.ENDOFTRACKING;
            } else if ( rightAssmt.getType() == GrowthLineTrackingILP.ASSIGNMENT_MAPPING ) {
                final MappingAssignment ma = ( MappingAssignment ) rightAssmt;
                if ( !ma.isPruned() ) {
                    ret = new SegmentRecord(this, 1);
                    ret.hyp = ma.getDestinationHypothesis();
                } else {
                    terminated_by = SegmentRecord.USER_PRUNING;
                    exists = false;
                }
            } else {
                terminated_by = rightAssmt.getType();
                exists = false;
            }
        } catch ( final GRBException ge ) {
            exists = false;
            System.err.println( ge.getMessage() );
        }
        return ret;
    }

    /**
     * @return true if the current segment is valid.
     */
    boolean exists() {
        return exists;
    }

    /**
     * @return
     */
    long[] computeChannelHistogram(final IterableInterval<FloatType> view, final float min, final float max) {
        final Histogram1d< FloatType > histogram = new Histogram1d<>(view, new Real1dBinMapper<>(min, max, 20, false));
        return histogram.toLongArray();
    }

    /**
     * @param channel
     * @return
     */
    float[] computeChannelPercentile(final IterableInterval<FloatType> channel) {
        final List< Float > pixelVals = new ArrayList<>();
        for ( final FloatType ftPixel : channel ) {
            pixelVals.add( ftPixel.get() );
        }
        Collections.sort( pixelVals );

        final int numPercentiles = 20;
        final float[] ret = new float[ numPercentiles - 1 ];
        for ( int i = 1; i < numPercentiles; i++ ) {
            final int index = ( i * pixelVals.size() / numPercentiles ) - 1;
            ret[ i - 1 ] = pixelVals.get( index );
        }
        return ret;
    }

    /**
     * @return
     */
    float[] computeChannelColumnIntensities(final IntervalView<FloatType> columnBoxInChannel) {
        if ( MoMA.INTENSITY_FIT_RANGE_IN_PIXELS != columnBoxInChannel.dimension( 0 ) ) {
            System.out.println( "EXPORT WARNING: intensity columns to be exported are " + columnBoxInChannel.dimension( 0 ) + " instead of " + MoMA.INTENSITY_FIT_RANGE_IN_PIXELS );
        }

        final float[] ret = new float[ ( int ) columnBoxInChannel.dimension( 0 ) ];
        int idx = 0;
        for ( int i = ( int ) columnBoxInChannel.min( 0 ); i <= columnBoxInChannel.max( 0 ); i++ ) {
            final IntervalView< FloatType > column = Views.hyperSlice( columnBoxInChannel, 0, i );
            ret[ idx ] = 0f;
            for ( final FloatType ftPixel : Views.iterable( column ) ) {
                ret[ idx ] += ftPixel.get();
            }
            idx++;
        }
        return ret;
    }

    /**
     * @param columnBoxInChannel
     * @return
     */
    float[][] getIntensities(final IntervalView<FloatType> columnBoxInChannel) {
        final float[][] ret = new float[ ( int ) columnBoxInChannel.dimension( 0 ) ][ ( int ) columnBoxInChannel.dimension( 1 ) ];
        int y = 0;
        for ( int j = ( int ) columnBoxInChannel.min( 1 ); j <= columnBoxInChannel.max( 1 ); j++ ) {
            final IntervalView< FloatType > row = Views.hyperSlice( columnBoxInChannel, 1, j );
            int x = 0;
            for ( final FloatType ftPixel : Views.iterable( row ) ) {
                ret[ x ][ y ] = ftPixel.get();
                x++;
            }
            y++;
        }
        return ret;
    }

    /**
     * @return
     */
    String getGenealogyString() {
        StringBuilder ret = new StringBuilder();
        for ( final int dt : genealogy ) {
            if ( dt == SegmentRecord.UPPER ) {
                ret.append("T");
            } else
            if ( dt == SegmentRecord.LOWER ) {
                ret.append("B");
            } else {
                ret.append(dt);
            }
        }
        return ret.toString();
    }
}

