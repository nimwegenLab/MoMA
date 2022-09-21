package com.jug.util.componenttree;

import net.imglib2.Localizable;

public class LocalizableImpl implements Localizable {
    private final int numDimensions;
    private final int[] position;

    public LocalizableImpl(Localizable localizable) {
        if (localizable.numDimensions() != 2) {
            throw new RuntimeException("Input localizable must be two-dimensional.");
        }
        numDimensions = localizable.numDimensions();
        position = new int[]{localizable.getIntPosition(0), localizable.getIntPosition(1)};
    }

    @Override
    public long getLongPosition(int i) {
        if (i > 2) {
            throw new RuntimeException("LocalizableImpl is two-dimensional.");
        }
        return position[i];
    }

    @Override
    public int numDimensions() {
        return numDimensions;
    }
}
