import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by ДаниленкоСП on 17.05.2019.
 */
public class MainForm extends JFrame implements ActionListener, PropertyChangeListener {
    private JPanel rootPanel;
    private JTextField textFieldID;
    private JButton buttonEditID;
    private JTextField textFieldPath;
    private JButton buttonEditPath;
    private JButton buttonSaveID;
    private JButton buttonSavePath;
    private JProgressBar progressBar1;
    private JButton buttonUpload;
    private JTextArea textArea1;
    private JPanel panelTextArea;
    private JCheckBox auto_upload;
    private JTextField textFieldExt;
    private JButton buttonEditExt;
    private JButton buttonSaveExt;
    private JFileChooser chooser;
    private ReadConfig readConfig;

    public MainForm() {
        readConfig = new ReadConfig("config\\config.ini");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            if (readConfig.getAutoUpload()) {
                GoogleDriveUtils.writeToLog(e.getMessage());
            } else {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
            }
        }
        URL urlCasAp = MainForm.class.getResource("resources/prog.png");
        ImageIcon payIco = new ImageIcon(urlCasAp);
        setIconImage(payIco.getImage());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Google Drive Backups");
        setSize(new Dimension(600, 400));
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setContentPane(rootPanel);

        textFieldID.setText(readConfig.getID_GD_FOLDER());
        textFieldPath.setText(readConfig.getPATH_FROM());
        textFieldExt.setText(Arrays.toString(readConfig.getFileExtArray()).replace("[", "").replace("]", "").replace(",", ";"));

        auto_upload.setSelected(readConfig.getAutoUpload());

        buttonEditID.addActionListener(e -> {
            DriveFolders driveFolders = new DriveFolders();
            if (driveFolders.getID_value() != null) {
                textFieldID.setText(driveFolders.getID_value());
            }

            buttonSaveID.setEnabled(true);
        });

        buttonSaveID.addActionListener(e -> {
            if (textFieldID.getText().length() > 0) {
                readConfig.updateProperty("ID_GD_FOLDER", textFieldID.getText());
                readConfig = new ReadConfig("config\\config.ini");
            }

            buttonSaveID.setEnabled(false);
        });

        buttonEditPath.addActionListener(e -> {
            chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(textFieldPath.getText()));
            chooser.setDialogTitle("Choose directory with backups");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);


            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                textFieldPath.setText(chooser.getSelectedFile() + "\\");
            }

            buttonSavePath.setEnabled(true);
        });

        buttonSavePath.addActionListener(e -> {
            readConfig.updateProperty("PATH_FROM", textFieldPath.getText());
            readConfig = new ReadConfig("config\\config.ini");

            buttonSavePath.setEnabled(false);
        });

        buttonEditExt.addActionListener(e -> {
            FilesExtensions filesExtensions = new FilesExtensions();
            if (filesExtensions.getReturnedExt() != null) {
                textFieldExt.setText(filesExtensions.getReturnedExt());
            }

            buttonSaveExt.setEnabled(true);
        });


        buttonSaveExt.addActionListener(e -> {
            readConfig.updateProperty("FILE_EXT_ARRAY", textFieldExt.getText());
            readConfig = new ReadConfig("config\\config.ini");

            buttonSaveExt.setEnabled(false);
        });

        progressBar1.setStringPainted(true);

        textArea1.setMargin(new Insets(5, 5, 5, 5));

        buttonUpload.addActionListener(this);

        auto_upload.addItemListener(e -> readConfig.updateProperty("AUTO_UPLOAD", String.valueOf(auto_upload.isSelected())));

        rootPanel.setOpaque(true);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        DefaultCaret caret = (DefaultCaret) textArea1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        pack();

        if (auto_upload.isSelected()) {
            doTask();
        } else {
            setVisible(true);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (Objects.equals("progress", evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressBar1.setValue(progress);
        }
    }

    public void doTask() {
        boolean sel_ID = false;
        boolean sel_Path = false;
        boolean sel_Ext = false;
        String errorMsg = "";

        if (readConfig.getFileExtArray().length > 0) {
            if (!readConfig.getFileExtArray()[0].equals("")) {
                sel_Ext = true;
            } else {
                sel_Ext = false;
                errorMsg = "Not filled \"Files extensions\"!";
            }
        }

        if (!readConfig.getPATH_FROM().equals("") && new File(readConfig.getPATH_FROM()).exists()) {
            sel_Path = true;
        } else {
            sel_Path = false;
            errorMsg = errorMsg + (errorMsg.length() > 0 ? "\n" : "") + "Not filled \"Backups directory\"!";
        }

        if (!readConfig.getID_GD_FOLDER().equals("")) {
            sel_ID = true;
        } else {
            sel_ID = false;
            errorMsg = errorMsg + (errorMsg.length() > 0 ? "\n" : "") + "Not filled \"Drive directory ID\"!";
        }

        if (sel_Ext && sel_Path && sel_ID) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Task task = new Task();
            task.addPropertyChangeListener(this);
            task.execute();
        } else {
            errorMsg = errorMsg + "\nPlease fill empty fields.";
            if (readConfig.getAutoUpload()) {
                GoogleDriveUtils.writeToLog(errorMsg);
            } else {
                JOptionPane.showMessageDialog(null, errorMsg, "Google Drive Backups", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        doTask();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(8, 1, new Insets(3, 3, 3, 3), -1, -1));
        rootPanel.setFont(new Font("Times New Roman", rootPanel.getFont().getStyle(), rootPanel.getFont().getSize()));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 5, new Insets(1, 1, 1, 1), -1, -1));
        panel1.setFont(new Font("Times New Roman", panel1.getFont().getStyle(), panel1.getFont().getSize()));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        textFieldID = new JTextField();
        textFieldID.setEditable(false);
        textFieldID.setFont(new Font("Times New Roman", textFieldID.getFont().getStyle(), textFieldID.getFont().getSize()));
        panel1.add(textFieldID, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        buttonEditID = new JButton();
        buttonEditID.setEnabled(true);
        buttonEditID.setFont(new Font("Times New Roman", buttonEditID.getFont().getStyle(), buttonEditID.getFont().getSize()));
        buttonEditID.setIcon(new ImageIcon(getClass().getResource("/resources/edit.png")));
        buttonEditID.setText("");
        panel1.add(buttonEditID, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20), new Dimension(20, 20), 0, false));
        buttonSaveID = new JButton();
        buttonSaveID.setEnabled(false);
        buttonSaveID.setFont(new Font("Times New Roman", buttonSaveID.getFont().getStyle(), buttonSaveID.getFont().getSize()));
        buttonSaveID.setIcon(new ImageIcon(getClass().getResource("/resources/save.png")));
        buttonSaveID.setText("");
        panel1.add(buttonSaveID, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20), new Dimension(20, 20), 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Times New Roman", label1.getFont().getStyle(), label1.getFont().getSize()));
        label1.setText("Drive directory ID");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 4, new Insets(1, 1, 1, 1), -1, -1));
        panel3.setFont(new Font("Times New Roman", panel3.getFont().getStyle(), panel3.getFont().getSize()));
        rootPanel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        textFieldPath = new JTextField();
        textFieldPath.setEditable(false);
        textFieldPath.setFont(new Font("Times New Roman", textFieldPath.getFont().getStyle(), textFieldPath.getFont().getSize()));
        panel3.add(textFieldPath, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        buttonEditPath = new JButton();
        buttonEditPath.setFont(new Font("Times New Roman", buttonEditPath.getFont().getStyle(), buttonEditPath.getFont().getSize()));
        buttonEditPath.setIcon(new ImageIcon(getClass().getResource("/resources/edit.png")));
        buttonEditPath.setText("");
        panel3.add(buttonEditPath, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20), new Dimension(20, 20), 0, false));
        buttonSavePath = new JButton();
        buttonSavePath.setEnabled(false);
        buttonSavePath.setFont(new Font("Times New Roman", buttonSavePath.getFont().getStyle(), buttonSavePath.getFont().getSize()));
        buttonSavePath.setIcon(new ImageIcon(getClass().getResource("/resources/save.png")));
        buttonSavePath.setText("");
        panel3.add(buttonSavePath, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20), new Dimension(20, 20), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFont(new Font("Times New Roman", label2.getFont().getStyle(), label2.getFont().getSize()));
        label2.setText("Backups directory");
        panel4.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setFont(new Font("Times New Roman", panel5.getFont().getStyle(), panel5.getFont().getSize()));
        rootPanel.add(panel5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setFont(new Font("Times New Roman", progressBar1.getFont().getStyle(), progressBar1.getFont().getSize()));
        panel5.add(progressBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(4, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setFont(new Font("Times New Roman", panel6.getFont().getStyle(), panel6.getFont().getSize()));
        rootPanel.add(panel6, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonUpload = new JButton();
        buttonUpload.setFont(new Font("Times New Roman", buttonUpload.getFont().getStyle(), buttonUpload.getFont().getSize()));
        buttonUpload.setText("Upload");
        panel6.add(buttonUpload, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        auto_upload = new JCheckBox();
        auto_upload.setEnabled(true);
        auto_upload.setText("Auto Upload");
        panel6.add(auto_upload, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelTextArea = new JPanel();
        panelTextArea.setLayout(new BorderLayout(0, 0));
        panelTextArea.setFont(new Font("Times New Roman", panelTextArea.getFont().getStyle(), panelTextArea.getFont().getSize()));
        rootPanel.add(panelTextArea, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(600, 200), new Dimension(600, 200), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setFont(new Font("Times New Roman", scrollPane1.getFont().getStyle(), scrollPane1.getFont().getSize()));
        panelTextArea.add(scrollPane1, BorderLayout.CENTER);
        textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setFont(new Font("Times New Roman", textArea1.getFont().getStyle(), textArea1.getFont().getSize()));
        textArea1.setWrapStyleWord(false);
        scrollPane1.setViewportView(textArea1);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel7.setFont(new Font("Times New Roman", panel7.getFont().getStyle(), panel7.getFont().getSize()));
        rootPanel.add(panel7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setFont(new Font("Times New Roman", label3.getFont().getStyle(), label3.getFont().getSize()));
        label3.setText("Files extensions   ");
        panel8.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldExt = new JTextField();
        textFieldExt.setEditable(false);
        textFieldExt.setFont(new Font("Times New Roman", textFieldExt.getFont().getStyle(), textFieldExt.getFont().getSize()));
        panel7.add(textFieldExt, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        buttonEditExt = new JButton();
        buttonEditExt.setFont(new Font("Times New Roman", buttonEditExt.getFont().getStyle(), buttonEditExt.getFont().getSize()));
        buttonEditExt.setIcon(new ImageIcon(getClass().getResource("/resources/edit.png")));
        buttonEditExt.setText("");
        panel7.add(buttonEditExt, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20), new Dimension(20, 20), 0, false));
        buttonSaveExt = new JButton();
        buttonSaveExt.setEnabled(false);
        buttonSaveExt.setFont(new Font("Times New Roman", buttonSaveExt.getFont().getStyle(), buttonSaveExt.getFont().getSize()));
        buttonSaveExt.setIcon(new ImageIcon(getClass().getResource("/resources/save.png")));
        buttonSaveExt.setText("");
        panel7.add(buttonSaveExt, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20), new Dimension(20, 20), 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            setProgress(0);

            ArrayList<File> files = DriveQuickstart.getFiles();

            String[] fileExtArray = readConfig.getFileExtArray();
            ArrayList<File> filesExt = new ArrayList<>();
            for (String aFileExtArray : fileExtArray) {
                if (!aFileExtArray.equals("")) {
                    files.stream().filter(uploadFile -> uploadFile.isFile() && uploadFile.exists()).forEach(uploadFile -> {
                        String filename = uploadFile.getName();
                        String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
                        if (Objects.equals(aFileExtArray, fileExtension)) {
                            filesExt.add(uploadFile);
                        }
                    });
                }
            }

            progressBar1.setMaximum(filesExt.size());

            if (filesExt.size() > 0) {
                for (int i = 0; i < filesExt.size(); i++) {
                    File uploadFile = filesExt.get(i);
                    String filename = uploadFile.getName();
                    String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);

                    String mimeType = getMimeType(fileExtension);
                    com.google.api.services.drive.model.File googleFile = DriveQuickstart.createGoogleFile(readConfig.getID_GD_FOLDER(), mimeType, filename, uploadFile);
                    String txt = "Created Google file!\n" +
                            "WebContentLink: " + googleFile.getWebContentLink() + "\n" +
                            "WebViewLink: " + googleFile.getWebViewLink() + "\n";
                    boolean delFile = uploadFile.delete();
                    if (delFile) {
                        txt = txt + "file: " + filename + " deleted successfully\n";
                    } else {
                        txt = txt + "Can`t delete file: " + filename + "\n";
                    }
                    txt = txt + "--------------------------------------------------------";
                    textArea1.append(txt + "\n");
                    GoogleDriveUtils.writeToLog(txt);

                    setProgress(i + 1);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e1) {
//                        if (readConfig.getAutoUpload()) {
//                            GoogleDriveUtils.writeToLog(e1.getMessage());
//                        } else {
//                            JOptionPane.showMessageDialog(null, e1.getMessage(), "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
//                        }
////                    e1.printStackTrace();
//                    }
                }
            } else {
                if (readConfig.getAutoUpload()) {
                    GoogleDriveUtils.writeToLog("There are no files with the specified extensions!");
                } else {
                    JOptionPane.showMessageDialog(null, "There are no files with the specified extensions!", "Google Drive Backups", JOptionPane.ERROR_MESSAGE);
                }
            }
            return null;
        }

        public String getMimeType(String fileExtension) {
            String mimeType;
            switch (fileExtension) {
                case "zip":
                    mimeType = "application/zip";
                    break;
                case "rar":
                    mimeType = "application/x-rar-compressed";
                    break;
                case "7z":
                    mimeType = "application/x-7z-compressed";
                    break;
                case "xls":
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "xlsx":
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "pdf":
                    mimeType = "application/pdf";
                    break;
                case "doc":
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    break;
                case "docx":
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    break;
                default:
                    mimeType = "application/file";
            }
            return mimeType;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            if (auto_upload.isSelected()) {
                System.exit(0);
            } else {
                Toolkit.getDefaultToolkit().beep();
                setCursor(null); //turn off the wait cursor
                JOptionPane.showMessageDialog(null, "Job Done!", "Google Drive Backups", JOptionPane.INFORMATION_MESSAGE);
                progressBar1.setValue(0);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainForm::new);
    }

}
