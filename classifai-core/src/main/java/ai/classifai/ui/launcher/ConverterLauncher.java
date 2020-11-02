/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.ui.launcher;

import ai.classifai.selector.annotation.ToolFileSelector;
import ai.classifai.selector.annotation.ToolFolderSelector;
import ai.classifai.selector.conversion.ConverterFolderSelector;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Random;

/**
 * Converter to convert files from Welcome Launcher
 *
 * @author codenamewei
 */
public class ConverterLauncher extends JPanel
        implements ActionListener,
        PropertyChangeListener
{

    private ConverterFolderSelector inputFolderSelector;
    private ConverterFolderSelector outputFolderSelector;

    private final int MAX_PAGE = 20;

    private final String FONT_TYPE = "Serif";//Serif, SansSerif, Monospaced, Dialog, and DialogInput.

    private final int ELEMENT_HEIGHT = 40;
    private final int TEXT_FIELD_LENGTH = 25;

    private final String DEFAULT_OUTPUT_PATH = "Same as source file";

    private JLabel inputFolderLabel =  new JLabel("Input Folder     : ");
    private JLabel outputFolderLabel = new JLabel("Output Folder  : ");

    private JTextField inputFolderField = new JTextField(TEXT_FIELD_LENGTH);
    private JTextField outputFolderField = new JTextField(TEXT_FIELD_LENGTH);

    private JButton inputBrowserButton = new JButton("Browse");
    private JButton outputBrowserButton = new JButton("Browse");

    private JComboBox inputFormatCombo = new JComboBox(new String[]{"   PDF", "   TIFF"});

    private JComboBox outputFormatCombo = new JComboBox(new String[]{"   JPG", "   PNG"});

    private JLabel maxPage = new JLabel("Maximum Page: ");
    private JTextField maxPageTextField = new JTextField("   " + MAX_PAGE);

    private JTextArea taskOutput;
    private JScrollPane progressPane;

    private JProgressBar progressBar = new JProgressBar(0, 100);
    private JButton convertButton = new JButton("Convert");

    private Task task;
    private JFrame frame;

    public ConverterLauncher()
    {
        Thread threadFile = new Thread(() -> inputFolderSelector = new ConverterFolderSelector());
        threadFile.start();

        Thread threadFolder = new Thread(() -> outputFolderSelector = new ConverterFolderSelector());
        threadFolder.start();
    }

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            Random random = new Random();
            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            //Sleep for at least one second to simulate "startup".
            try {
                Thread.sleep(1000 + random.nextInt(2000));
            } catch (InterruptedException ignore) {}
            while (progress < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress += random.nextInt(10);
                setProgress(Math.min(progress, 100));
            }
            return null;
        }

        /**
         * Invoked when task's progress property changes.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress" == evt.getPropertyName()) {
                int progress = (Integer) evt.getNewValue();
                progressBar.setIndeterminate(false);
                progressBar.setValue(progress);
                taskOutput.append(String.format(
                        "Completed %d%% of task.\n", progress));
            }
        }

        /*
         * Executed in event dispatch thread
         */
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            convertButton.setForeground(Color.BLACK);
            convertButton.setEnabled(true);

            taskOutput.append("Done!\n");
        }
    }

    public void start()
    {
        frame = new JFrame("Files Format Converter");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(640, 420));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 10, 5, 10);

        constraints.gridx = 0; constraints.gridy = 0;
        panel.add(inputFormatCombo, constraints);

        constraints.gridx = 1;
        panel.add(inputFolderLabel, constraints);

        constraints.gridx = 2;
        panel.add(inputFolderField, constraints);

        constraints.gridx = 3;
        panel.add(inputBrowserButton, constraints);

        constraints.gridx = 0; constraints.gridy = 1;
        panel.add(outputFormatCombo, constraints);

        constraints.gridx = 1;
        panel.add(outputFolderLabel, constraints);

        constraints.gridx = 2;
        panel.add(outputFolderField, constraints);

        constraints.gridx = 3;
        panel.add(outputBrowserButton, constraints);

        constraints.gridwidth = 2;

        constraints.gridx = 1; constraints.gridy = 2;
        panel.add(maxPage, constraints);

        constraints.gridx = 2;
        panel.add(maxPageTextField, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridwidth = 4;

        constraints.gridx = 0; constraints.gridy = 3;
        panel.add(progressPane, constraints);

        progressBar.setValue(0);


        constraints.gridwidth = 3;
        constraints.gridx = 0; constraints.gridy = 4;
        panel.add(progressBar, constraints);


        constraints.gridwidth = 1;
        constraints.gridx = 3; constraints.gridy = 4;
        panel.add(convertButton, constraints);

        frame.add(panel);//, BorderLayout.PAGE_START);

        frame.setIconImage(LogoHandler.getClassifaiIcon());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void configure()
    {
        outputFolderField.setText(DEFAULT_OUTPUT_PATH);

        inputFormatCombo.setSelectedIndex(0);
        outputFormatCombo.setSelectedIndex(0);

        convertButton.setOpaque(true);

        design(inputFolderLabel);
        design(outputFolderLabel);

        design(inputFolderField);
        inputFolderField.setEditable(false);
        inputFolderField.setText((new File(System.getProperty("user.home")).getAbsolutePath()));//FIXME: Replace with path to start with
        inputFolderField.setPreferredSize(new Dimension(100, ELEMENT_HEIGHT - 10));

        design(outputFolderField);
        outputFolderField.setEditable(false);
        outputFolderField.setPreferredSize(new Dimension(100, ELEMENT_HEIGHT - 10));

        design(inputBrowserButton);
        inputBrowserButton.addActionListener(new InputFolderListener());

        design(outputBrowserButton);
        outputBrowserButton.addActionListener(new OutputFolderListener());

        design(inputFormatCombo);
        design(outputFormatCombo);

        design(maxPage);
        design(maxPageTextField);
        maxPageTextField.setEditable(false);
        maxPageTextField.setPreferredSize(new Dimension(50, ELEMENT_HEIGHT - 10));

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        progressPane = new JScrollPane(taskOutput);
        design(progressPane);

        progressBar.setValue(0);
        progressBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.DARK_GRAY; }
            protected Color getSelectionForeground() { return Color.BLACK; }
        });

        //Call setStringPainted now so that the progress bar height
        //stays the same whether or not the string is shown.
        progressBar.setStringPainted(true);

        design(progressBar);
        design(convertButton);

        convertButton.addActionListener(this);
    }

    private void design(Object obj)
    {

        if(obj instanceof JLabel)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 14);

            Dimension dimension = new Dimension(120, ELEMENT_HEIGHT);

            JLabel label = (JLabel) obj;
            label.setFont(font);
            label.setPreferredSize(dimension);
        }
        else if(obj instanceof JTextField)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 12);

            JTextField textField = (JTextField) obj;
            textField.setFont(font);
        }
        else if(obj instanceof JButton)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 14);

            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT);

            JButton button = (JButton) obj;
            button.setFont(font);
            button.setPreferredSize(dimension);
        }
        else if(obj instanceof JComboBox)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 14);

            JComboBox comboBox = (JComboBox) obj;
            comboBox.setFont(font);

            Dimension dimension = new Dimension(80, ELEMENT_HEIGHT);

            comboBox.setPreferredSize(dimension);
        }
        else if(obj instanceof JProgressBar)
        {
            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT / 2);

            Font font = new Font(FONT_TYPE, Font.BOLD, 14);


            JProgressBar progressBar = (JProgressBar) obj;
            progressBar.setFont(font);

            progressBar.setForeground(Color.GREEN);
            progressBar.setPreferredSize(dimension);
        }
        else if(obj instanceof JComponent)
        {
            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT * 5);

            Font font = new Font(FONT_TYPE, Font.BOLD, 8);

            JComponent scrollPane = (JComponent) obj;
            scrollPane.setFont(font);
            scrollPane.setPreferredSize(dimension);

        }

        if(obj == null)
        {
            System.out.println("Object is null");
        }
    }


    public void launch()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                configure();
                start();
                frame.setVisible(true);
            }
        });
    }

    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {

        convertButton.setEnabled(false);
        convertButton.setForeground(Color.LIGHT_GRAY);
        progressBar.setIndeterminate(true);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                    "Completed %d%% of task.\n", progress));
        }
    }

    class InputFolderListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            inputFolderSelector.run();
        }
    }

    class OutputFolderListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            outputFolderSelector.run();
        }
    }
}