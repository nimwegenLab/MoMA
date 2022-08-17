package com.jug.gui;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.datahandling.GlDataLoader;


/**
 * This class wraps an MotherMachine-instance and makes a GUI model out of it.
 *
 * @author jug
 */
public class MoMAModel {

    // -------------------------------------------------------------------------------------
    // fields
    // -------------------------------------------------------------------------------------
    /**
     * The MotherMachine instance we wrap here
     */
    final GlDataLoader mm;

    private int currentGLidx;
    private int currentGLFidx;

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------

    /**
     * Construction
     *
     * @param glDataLoader the instance of GlDataLoader, which provides access to loaded data.
     */
    public MoMAModel(final GlDataLoader glDataLoader) {
        this.mm = glDataLoader;
        currentGLidx = 0;
        currentGLFidx = 0;
    }

    // -------------------------------------------------------------------------------------
    // getters and setters
    // -------------------------------------------------------------------------------------
    public Growthlane getCurrentGL() {
        return mm.getGrowthlanes().get(currentGLidx);
    }

    public void setCurrentGL(final int idx) {
        setCurrentGL(idx, 0);
    }

    public void setCurrentGL(final int idx, final int glfIdx) {
        assert (idx >= 0);
        assert (idx < mm.getGrowthlanes().size());
        currentGLidx = idx;
        currentGLFidx = glfIdx;
    }

    public GrowthlaneFrame getGrowthlaneFrame(int index) {
        return getCurrentGL().get(index);
    }

    public GrowthlaneFrame getCurrentGLF() {
        return getCurrentGL().get(currentGLFidx);
    }

    public GrowthlaneFrame getGlfAtTimeStep(int timestep) {
        return getCurrentGL().get(timestep);
    }

    public void setCurrentGLF(final int idx) {
        assert (idx >= 0);
        assert (idx <= getCurrentGL().numberOfFrames());
        currentGLFidx = idx;
    }

    // -------------------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------------------
    public Growthlane switchToNextGL() {
        currentGLidx++;
        if (currentGLidx >= mm.getGrowthlanes().size()) {
            currentGLidx = 0;
        }
        return getCurrentGL();
    }

    public Growthlane switchToPrevGL() {
        currentGLidx--;
        if (currentGLidx < 0) {
            currentGLidx = mm.getGrowthlanes().size() - 1;
        }
        return getCurrentGL();
    }

    public GrowthlaneFrame switchToNextGLF() {
        currentGLFidx++;
        if (currentGLFidx >= getCurrentGL().numberOfFrames()) {
            currentGLidx = 0;
        }
        return getCurrentGLF();
    }

    public GrowthlaneFrame switchToPrevGLF() {
        currentGLFidx--;
        if (currentGLFidx < 0) {
            currentGLFidx = getCurrentGL().numberOfFrames() - 1;
        }
        return getCurrentGLF();
    }

    /**
     * @return the time-point of the current GLF within the current GL.
     */
    public int getCurrentTimeOfCurrentGl() {
        return getCurrentGL().getFrames().indexOf(getCurrentGLF());
    }

    public int getTimeStepMaximumOfCurrentGl() {
        return getCurrentGL().getTimeStepMaximum();
    }
}
