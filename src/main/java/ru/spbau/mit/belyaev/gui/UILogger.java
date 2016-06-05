package ru.spbau.mit.belyaev.gui;

import javax.swing.*;
import java.io.*;

/**
 * Created by belaevstanislav on 05.06.16.
 * SPBAU Java practice.
 */

public class UILogger extends JTextArea {
    private final StringBuilder fileOutput;

    UILogger() {
        setEditable(false);
        fileOutput = new StringBuilder("");
    }

    void logS(String string) {
        append(string);
    }

    public void logF(String string) {
        append(string);
        fileOutput.append(string);
    }

    void toFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), "utf-8"))) {
            writer.write(fileOutput.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
