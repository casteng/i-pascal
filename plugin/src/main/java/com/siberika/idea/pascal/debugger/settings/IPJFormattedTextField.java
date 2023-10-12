package com.siberika.idea.pascal.debugger.settings;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.text.ParseException;

public class IPJFormattedTextField extends JFormattedTextField {

    private Color textColor;
    private Color errorColor;

    public IPJFormattedTextField(Format aFormat) {
        super(aFormat);
        setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        addListener();
        addFocusListener(new MousePositionCorrectorListener());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        textColor = getForeground();
        errorColor = new JBColor(new Color(255, 96, 96), new Color(192, 64, 64));
    }

    private boolean isTextValid() {
        AbstractFormatter formatter = getFormatter();
        if (formatter != null) {
            try {
                formatter.stringToValue(getText());
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setValue(Object value) {
        boolean valueValid = true;
        try {
            AbstractFormatter formatter = getFormatter();
            if (formatter != null) {
                formatter.valueToString(value);
            }
        } catch (ParseException e) {
            valueValid = false;
            updateColor();
        }
        if (valueValid) {
            int old_caret_position = getCaretPosition();
            super.setValue(value);
            setCaretPosition(Math.min(old_caret_position, getText().length()));
        }
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (isTextValid()) {
            return super.processKeyBinding(ks, e, condition, pressed) && ks != KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        } else {
            return super.processKeyBinding(ks, e, condition, pressed);
        }
    }

    private void addListener() {
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateColor();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateColor();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateColor();
            }
        });
    }

    private void updateColor() {
        boolean valid = isTextValid();
        setForeground(valid ? textColor : errorColor);
    }

    private static class MousePositionCorrectorListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            JTextField field = (JTextField) e.getSource();
            int dot = field.getCaret().getDot();
            int mark = field.getCaret().getMark();
            if (field.isEnabled() && field.isEditable()) {
                SwingUtilities.invokeLater(() -> {
                    if (dot == mark) {
                        field.getCaret().setDot(dot);
                    }
                });
            }
        }
    }

}
