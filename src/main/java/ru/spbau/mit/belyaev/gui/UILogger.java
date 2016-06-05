package ru.spbau.mit.belyaev.gui;

import javax.swing.*;

/**
 * Created by belaevstanislav on 05.06.16.
 * SPBAU Java practice.
 */

public class UILogger extends JTextArea {
    public UILogger() {
        setEditable(false);
    }

    public void log(String string) {
        append(string);
    }
}
