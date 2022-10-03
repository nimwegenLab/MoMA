package com.jug.gui.progress;

import java.awt.event.ActionListener;

public interface IDialogGurobiProgress extends ActionListener {
    void setVisible(boolean isVisible);

    void notifyGurobiTermination();

    void pushStatus(String s);

    void dispose();
}
