package com.jug.gui;

import com.jug.Growthlane;
import com.jug.GrowthlaneFrame;
import com.jug.MoMA;


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
    final MoMA mm;

    private int currentGLidx;
    private int currentGLFidx;

    // -------------------------------------------------------------------------------------
    // construction
    // -------------------------------------------------------------------------------------

    /**
     * Construction
     *
     * @param mm the instance of MotherMachine to be wrapped by this GUI model
     */
    public MoMAModel(final MoMA mm) {
        this.mm = mm;
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
        assert (idx <= getCurrentGL().size());
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
        if (currentGLFidx >= getCurrentGL().size()) {
            currentGLidx = 0;
        }
        return getCurrentGLF();
    }

    public GrowthlaneFrame switchToPrevGLF() {
        currentGLFidx--;
        if (currentGLFidx < 0) {
            currentGLFidx = getCurrentGL().size() - 1;
        }
        return getCurrentGLF();
    }

    /**
     * @return the time-point of the current GLF within the current GL.
     */
    public int getCurrentTime() {
        return getCurrentGL().getFrames().indexOf(getCurrentGLF());
    }

    public int getTimeStepMaximum() {
        return getCurrentGL().getFrames().size() - 1;
    }
}
