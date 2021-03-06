/*
 * Copyright (c) 2021 Manoj Randeni. All rights reserved.
 * Licensed under the Apache license. See License.txt in the project root for license information
 */

package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import util.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorFormController {

    private final List<Index> findings = new ArrayList();
    private PrinterJob printerJob;
    private boolean printerAvailable;
    private Label stbarCharCount;
    private Label stbarCarrotPosition;
    private Label stbarWordCount;
    private Label stbarfontSize;
    private File saveFile = null;
    private File saveFileDir = null;
    private boolean isModified = false;
    private boolean matchCase = false;
    @FXML
    private VBox baseVBox;
    @FXML
    private MenuBar mnuBar;
    @FXML
    private MenuItem mnuItemNew;
    @FXML
    private MenuItem mnuItemOpen;
    @FXML
    private MenuItem mnuItemSave;
    @FXML
    private MenuItem mnuItemSaveAs;
    @FXML
    private MenuItem mnuItemPrint;
    @FXML
    private MenuItem mnuItemPageSetup;
    @FXML
    private MenuItem mnuItemExit;
    @FXML
    private MenuItem mnuItemUndo;
    @FXML
    private MenuItem mnuItemRedo;
    @FXML
    private MenuItem mnuItemCut;
    @FXML
    private MenuItem mnuItemCopy;
    @FXML
    private MenuItem mnuItemPaste;
    @FXML
    private MenuItem mnuItemFind;
    @FXML
    private MenuItem mnuItemReplace;
    @FXML
    private MenuItem mnuItemSelectAll;
    @FXML
    private MenuItem mnuItemWrap;
    @FXML
    private MenuItem mnuItemZoomIn;
    @FXML
    private MenuItem mnuItemZoomOut;
    @FXML
    private MenuItem mnuItemDefaultZoom;
    @FXML
    private MenuItem mnuItemStatusBar;
    @FXML
    private MenuItem mnuItemAbout;
    @FXML
    private AnchorPane pneFind;
    @FXML
    private TextField txtFind;
    @FXML
    private RadioButton rdbUp;
    @FXML
    private ToggleGroup FindDirGrp;
    @FXML
    private RadioButton rdbDown;
    @FXML
    private CheckBox cbCaseMatchInFind;
    @FXML
    private Button btnFindNext;
    @FXML
    private Button btnFindClose;
    @FXML
    private AnchorPane pneReplace;
    @FXML
    private TextField txtFindInReplace;
    @FXML
    private TextField txtReplace;
    @FXML
    private CheckBox cbCaseMatchInReplace;
    @FXML
    private Button btnFindNextInReplace;
    @FXML
    private Button btnReplace;
    @FXML
    private Button btnReplaceAll;
    @FXML
    private Button btnReplaceClose;
    @FXML
    private TextArea txtEditor;
    @FXML
    private StatusBar stsbrBottom;
    private String findText = "";
    @FXML
    private AnchorPane root;

    public void initialize() {

        Platform.runLater(() -> {
            init();
            setListeners();
        });
    }

    private void setListeners() {

        txtEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            isModified = true;

            stbarCharCount.setText("Chars: " + getCharCount());
            stbarWordCount.setText("Words: " + getWordCount());

            findText(findText, matchCase);

            setWindowTitle();
        });

        txtEditor.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            stbarCarrotPosition.setText("Carrot at: " + getCaretPosition());
        });

        txtEditor.wrapTextProperty().addListener((observable, oldValue, newValue) -> {
            // Set user preference on wrapTextProperty change
            Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putBoolean("wrap-text", txtEditor.isWrapText());
        });

        stsbrBottom.visibleProperty().addListener((observable, oldValue, newValue) -> {
            // Set user preference on statusbar visibleProperty change
            Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putBoolean("show-statusbar", stsbrBottom.isVisible());
        });

        txtEditor.fontProperty().addListener((observable, oldValue, newValue) -> {
            stbarfontSize.setText("Font size: " + getEditorFontSize());
            // Set user preference on editor fontProperty change
            Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putDouble("font-size", txtEditor.getFont().getSize());
        });

        txtFind.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                findText = txtFind.getText();
                findText(findText, matchCase);
            }
        });

        txtFindInReplace.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                findText = txtFindInReplace.getText();
                findText(findText, matchCase);
            }
        });

        Printer.defaultPrinterProperty().addListener((observable, oldValue, newValue) -> {
            // Listen to printers change
            if (newValue == null) {
                printerAvailable = false;
                mnuItemPrint.setDisable(true);
                mnuItemPageSetup.setDisable(true);
            } else {
                mnuItemPrint.setDisable(false);
                mnuItemPageSetup.setDisable(false);
            }
        });

        cbCaseMatchInReplace.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!matchCase && newValue) {
                matchCase = true;
            }

            if (matchCase && !newValue) {
                matchCase = false;
            }

            findTextInVisiblePane();
        });

        cbCaseMatchInFind.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!matchCase && newValue) {
                matchCase = true;
            }

            if (matchCase && !newValue) {
                matchCase = false;
            }
            findTextInVisiblePane();
        });

        pneFind.visibleProperty().addListener((observable, oldValue, newValue) -> {
            txtFind.requestFocus();

        });

        pneReplace.visibleProperty().addListener((observable, oldValue, newValue) -> {
            txtFindInReplace.requestFocus();
        });


    }

    private void init() {

        // Set initial visibility of Find and Replace panes
        pneFind.setVisible(false);
        pneFind.setManaged(false);

        pneReplace.setVisible(false);
        pneReplace.setManaged(false);

        // Set text editor wrapping status to according to user preference
        txtEditor.setWrapText(Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").getBoolean("wrap-text", true));

        // Set text editor statusbar visibility according to user preference
        stsbrBottom.setVisible(Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").getBoolean("show-statusbar", true));
        stsbrBottom.setManaged(Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").getBoolean("show-statusbar", true));

        // Set text editor font size according to user preference
        StringBuilder sbFontSize = new StringBuilder();
        sbFontSize.append("-fx-font-size: ").append(Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").getDouble("font-size", 16)).append(";");
        txtEditor.setStyle(sbFontSize.toString());

        // Set saveFileDir according to user preference
        saveFileDir = new File(Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").get("last-directory", String.valueOf(new File(System.getProperty("user.home")))));

        // Add dummy text
       /* {
            txtEditor.setText("What is Lorem Ipsum?\n" +
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
                    "\n" +
                    "Why do we use it?\n" +
                    "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).\n" +
                    "\n" +
                    "\n" +
                    "Where does it come from?\n" +
                    "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.\n" +
                    "\n" +
                    "The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from \"de Finibus Bonorum et Malorum\" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham.What is Lorem Ipsum?\n" +
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
                    "\n" +
                    "Why do we use it?\n" +
                    "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).\n" +
                    "\n" +
                    "\n" +
                    "Where does it come from?\n" +
                    "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.\n" +
                    "\n" +
                    "The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from \"de Finibus Bonorum et Malorum\" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham.");
        }*/
        printerAvailable = Printer.getDefaultPrinter() != null;

        if (!printerAvailable) {
            mnuItemPrint.setDisable(true);
            mnuItemPageSetup.setDisable(true);
        }

        stbarCharCount = new Label("Chars: " + getCharCount());
        stbarCharCount.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-padding:0 0 0 20");
        stbarCharCount.prefHeightProperty().bind(stsbrBottom.heightProperty());

        stbarCarrotPosition = new Label("Carrot at: " + getCaretPosition());
        stbarCarrotPosition.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-padding:0 0 0 20");
        stbarCarrotPosition.prefHeightProperty().bind(stsbrBottom.heightProperty());

        stbarWordCount = new Label("Words: " + getWordCount());
        stbarWordCount.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-padding:0 0 0 20");
        stbarWordCount.prefHeightProperty().bind(stsbrBottom.heightProperty());

        stbarfontSize = new Label("Font size: " + getEditorFontSize());
        stbarfontSize.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-padding:0 0 0 20");
        stbarfontSize.prefHeightProperty().bind(stsbrBottom.heightProperty());

        HBox stbRightHBox = new HBox();
        stbRightHBox.setSpacing(10);
        stbRightHBox.getChildren().addAll(stbarfontSize, stbarCarrotPosition, stbarCharCount, stbarWordCount);
        stsbrBottom.getRightItems().add(stbRightHBox);

        setWindowTitle();
    }

    private int getCharCount() {
        return txtEditor.getText().length();
    }

    private int getWordCount() {
        return (txtEditor.getText().isEmpty() ? 0 : txtEditor.getText().split("\\s").length);
    }

    private int getCaretPosition() {
        return txtEditor.getText().isEmpty() ? 0 : txtEditor.getCaretPosition();
    }

    private int getEditorFontSize() {
        return ((int) txtEditor.getFont().getSize());
    }

    private String getFileName() {
        if (saveFile != null) {
            return saveFile.getName();
        }
        return "Untitled";
    }

    private String getWindowTitle() {
        StringBuilder sb = new StringBuilder();
        if (isModified) {
            sb.append("*").append(getFileName()).append(" - ");
        } else {
            sb.append(getFileName()).append(" - ");
        }
        return sb.append("Simple Text Editor").toString();
    }

    private void setWindowTitle() {
        ((Stage) this.txtEditor.getScene().getWindow()).setTitle(getWindowTitle());
    }

    private void save(boolean isSaveAs) {

        if (isSaveAs || saveFile == null) {
            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("Save File");

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All File", "*"));
            fileChooser.setInitialFileName(getFileName());
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.txt"));
            fileChooser.setInitialDirectory(saveFileDir != null ? new File(String.valueOf(saveFileDir)) : null);

            saveFile = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());
            if (saveFile == null) return;

            saveFileDir = saveFile.getParentFile();
            Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").put("last-directory", saveFileDir.toString());
        }
//        System.out.println(saveFile != null ? saveFile.getParentFile() : null);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            bw.write(txtEditor.getText());
            isModified = false;
            setWindowTitle();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "File save error!", ButtonType.OK).show();
            e.printStackTrace();
        }
    }

    private boolean askToSave() {
        if (isModified) {
            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't Save");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save changes to " + getFileName() + "?", save, dontSave, ButtonType.CANCEL);

            alert.setTitle("Simple Text Editor");
            alert.showAndWait();
            ButtonType result = alert.getResult();

            if (result == save) {

                save(true);
                return true;

            } else if (result == dontSave) {

                txtEditor.clear();
                isModified = false;
                saveFile = null;
                setWindowTitle();
                return true;

            }
            return false;
        } else {
            return true;
        }

    }

    private void findTextInVisiblePane() {
        if (pneFind.isVisible()) {
            findText(txtFind.getText(), matchCase);
        }

        if (pneReplace.isVisible()) {
            findText(txtFindInReplace.getText(), matchCase);
        }
    }

    // Find previous matching text
    public Index findPrevious(String findText, boolean matchCase) {
        findText(findText, matchCase);

        int findCount = findings.size();

        if (findCount > 0) {
            for (int i = (findCount - 1); i >= 0; i--) {
                int endID = findings.get(i).getEndIndex();

                if (getCaretPosition() > endID) {
                    return findings.get(i);
                }
            }
            return findings.get(findCount - 1);
        } else {
            return null;
        }
    }

    // Find next matching text
    public Index findNext(String findText, boolean matchCase) {
        findText(findText, matchCase);
        int findCount = findings.size();

        if (findCount > 0) {
            for (int i = 0; i < findCount; i++) {
                int startID = findings.get(i).getStartIndex();

                if (getCaretPosition() <= startID) {
                    return findings.get(i);
                }
            }
            return findings.get(0);
        } else {
            return null;
        }
    }

    public void findText(String findText, boolean matchCase) {
        String regExp = matchCase ? findText : findText.toLowerCase(Locale.ROOT);

        findings.clear();
        txtEditor.deselect();

        if (!regExp.equals("")) {
            String text = matchCase ? txtEditor.getText() : txtEditor.getText().toLowerCase(Locale.ROOT);

            Pattern pattern = Pattern.compile(regExp);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                findings.add(new Index(matcher.start(), matcher.end()));
            }
            int findingCount = findings.size();
            stsbrBottom.setText(findingCount != 0 ? "Matches: " + findingCount : "No match!");
        }
    }

    public void replaceText(String findText, boolean matchCase, String replaceText) {
        findText(findText, matchCase);
        try {
            Index next = findNext(findText, matchCase);
            if (next != null) {
                txtEditor.replaceText(next.getStartIndex(), next.getEndIndex(), replaceText);
                findText(findText, matchCase);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceAllText(String findText, boolean matchCase, String replaceText) {

        try {
            if (!matchCase) {
                String newText = txtEditor.getText().replaceAll("(?i)" + findText, replaceText);
                txtEditor.setText(newText);
            } else {
                String newText = txtEditor.getText().replaceAll(findText, replaceText);
                txtEditor.setText(newText);
            }

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            stsbrBottom.setText("No match!");
        }
    }

    private void open(File file, Event event) {
        if (askToSave()) {
            if (file == null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open File");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All File", "*"));
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.txt"));
                fileChooser.setInitialDirectory(saveFileDir != null ? new File(String.valueOf(saveFileDir)) : null);


                saveFile = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());

                if (saveFile == null) {
                    return;
                }
            } else {
                saveFile = file;
            }


            saveFileDir = file != null ? file.getParentFile() : saveFile.getParentFile();
            Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").put("last-directory", saveFileDir.toString());

            try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                txtEditor.clear();
                txtEditor.setText(sb.toString());
                isModified = false;
                setWindowTitle();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void exit(Event event) {
        if (askToSave()) {
            txtEditor.clear();
            isModified = false;
            saveFile = null;
            setWindowTitle();
            Platform.exit();
        } else {
            event.consume();
        }

        Stage primaryStage = (Stage) txtEditor.getScene().getWindow();
        Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putBoolean("is-maximized", primaryStage.isMaximized());
        Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putDouble("width", root.getWidth());
        Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putDouble("height", root.getHeight());
        Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putDouble("xPos", primaryStage.getX());
        Preferences.userRoot().node("lk").node("ijse").node("simple-text-editor").putDouble("yPos", primaryStage.getY());
    }

    @FXML
    private void mnuItemNew_onAction(ActionEvent actionEvent) {
        if (askToSave()) {
            txtEditor.clear();
            isModified = false;
            saveFile = null;
            setWindowTitle();
        }
    }

    @FXML
    private void mnuItemOpen_onAction(ActionEvent actionEvent) {

        open(null, actionEvent);
    }

    @FXML
    private void mnuItemSave_onAction(ActionEvent actionEvent) {
        save(false);
    }

    @FXML
    private void mnuItemSaveAs_onAction(ActionEvent actionEvent) {
        save(true);
    }

    @FXML
    private void mnuItemPrint_onAction(ActionEvent actionEvent) {
        // Show printDialog and add page to printer job queue if a default printer is available
        printerJob = PrinterJob.createPrinterJob();

        if (printerAvailable && printerJob != null && printerJob.showPrintDialog(txtEditor.getScene().getWindow())) {
            printerJob.printPage(txtEditor.lookup("Text"));
        }

        if (printerJob != null) {
            printerJob.endJob();
        }
    }

    @FXML
    private void mnuItemPageSetup_onAction(ActionEvent actionEvent) {
        // Show print pageSetupDialog if a default printer is available
        printerJob = PrinterJob.createPrinterJob();

        if (printerAvailable) {
            printerJob.showPageSetupDialog(txtEditor.getScene().getWindow());
        }

        if (printerJob != null) {
            printerJob.endJob();
        }
    }


    @FXML
    private void mnuItemExit_onAction(ActionEvent actionEvent) {
        exit(actionEvent);
    }

    @FXML
    private void mnuItemUndo_onAction(ActionEvent actionEvent) {
        txtEditor.undo();
    }

    @FXML
    private void mnuItemRedo_onAction(ActionEvent actionEvent) {
        txtEditor.redo();
    }

    @FXML
    private void mnuItemCut_onAction(ActionEvent actionEvent) {
        txtEditor.cut();
    }

    @FXML
    private void mnuItemCopy_onAction(ActionEvent actionEvent) {
        txtEditor.copy();
    }

    @FXML
    private void mnuItemPaste_onAction(ActionEvent actionEvent) {
        txtEditor.paste();
    }

    @FXML
    private void mnuItemFind_onAction(ActionEvent actionEvent) {
        if (pneReplace.isVisible()) {
            pneReplace.setManaged(false);
            pneReplace.setVisible(false);
        }

        pneFind.setManaged(true);
        pneFind.setVisible(true);

        txtFind.setText(findText);
        txtFind.requestFocus();
        txtFind.selectAll();

        cbCaseMatchInFind.setSelected(matchCase);
    }

    @FXML
    private void mnuItemReplace_onAction(ActionEvent actionEvent) {

        if (pneFind.isVisible()) {
            pneFind.setManaged(false);
            pneFind.setVisible(false);
        }

        pneReplace.setManaged(true);
        pneReplace.setVisible(true);

        txtFindInReplace.setText(findText);
        txtFindInReplace.requestFocus();
        txtFindInReplace.selectAll();

        cbCaseMatchInReplace.setSelected(matchCase);
    }

    @FXML
    private void mnuItemSelectAll_onAction(ActionEvent actionEvent) {
        txtEditor.selectAll();
    }

    @FXML
    private void mnuItemWrap_onAction(ActionEvent actionEvent) {
        txtEditor.setWrapText(!txtEditor.isWrapText());
    }

    @FXML
    private void mnuItemZoomIn_onAction(ActionEvent actionEvent) {
        // Check for zoom out disability
        if (mnuItemZoomOut.isDisable()) {
            mnuItemZoomOut.setDisable(false);
        }

        double editorFont = txtEditor.getFont().getSize();
        if (editorFont <= 36.0) {
            // Increase editor font size by 1px
            StringBuilder sbFontSize = new StringBuilder();
            double newFontValue = editorFont + 1;
            sbFontSize.append("-fx-font-size: ").append(newFontValue).append(";");
            txtEditor.setStyle(sbFontSize.toString());

            // Disable zoom-out at editor font size 10px
            if (newFontValue == 36.0) {
                mnuItemZoomIn.setDisable(true);
            }
        }
    }

    @FXML
    private void mnuItemZoomOut_onAction(ActionEvent actionEvent) {

        // Check for zoom in disability
        if (mnuItemZoomIn.isDisable()) {
            mnuItemZoomIn.setDisable(false);
        }

        double editorFont = txtEditor.getFont().getSize();
        if (editorFont >= 10.0) {
            // Decrease editor font size by 1px
            StringBuilder sbFontSize = new StringBuilder();
            double newFontValue = editorFont - 1;
            sbFontSize.append("-fx-font-size: ").append(newFontValue).append(";");
            txtEditor.setStyle(sbFontSize.toString());

            // Disable zoom-out at editor font size 10px
            if (newFontValue == 10.0) {
                mnuItemZoomOut.setDisable(true);
            }
        }

    }

    @FXML
    private void mnuItemDefaultZoom_onAction(ActionEvent actionEvent) {
        // Check for zoom out disability
        if (mnuItemZoomOut.isDisable()) {
            mnuItemZoomOut.setDisable(false);
        }

        // Check for zoom in disability
        if (mnuItemZoomIn.isDisable()) {
            mnuItemZoomIn.setDisable(false);
        }

        // Set editor font size to default value of 16.0px
        double editorFont = txtEditor.getFont().getSize();
        StringBuilder sbFontSize = new StringBuilder();
        sbFontSize.append("-fx-font-size: ").append(16).append(";");
        txtEditor.setStyle(sbFontSize.toString());
    }

    @FXML
    private void mnuItemStatusBar_onAction(ActionEvent actionEvent) {
        if (stsbrBottom.isVisible()) {
            stsbrBottom.setManaged(false);
            stsbrBottom.setVisible(false);
        } else {
            stsbrBottom.setManaged(true);
            stsbrBottom.setVisible(true);
        }
    }

    @FXML
    private void mnuItemAbout_onAction(ActionEvent actionEvent) throws IOException {
        Parent load = FXMLLoader.load(this.getClass().getResource("../view/AboutForm.fxml"));
        Scene scene = new Scene(load);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("About");
//        stage.initOwner(txtEditor.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.show();
        stage.centerOnScreen();

    }

    @FXML
    private void btnFindNext_onAction(ActionEvent actionEvent) {
        Toggle selectedToggle = FindDirGrp.getSelectedToggle();

        try {
            Index range = null;
            if (selectedToggle.equals(rdbDown)) {
                range = findNext(txtFind.getText(), matchCase);
            } else if (selectedToggle.equals(rdbUp)) {
                range = findPrevious(txtFind.getText(), matchCase);
            }

            if (range != null) {
                txtEditor.selectRange(range.getStartIndex(), range.getEndIndex());
            }

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnFindClose_onAction(ActionEvent actionEvent) {
        pneFind.setManaged(false);
        pneFind.setVisible(false);
        stsbrBottom.setText("Ok");

    }

    @FXML
    private void btnFindNextInReplace_onAction(ActionEvent actionEvent) {
        try {
            Index range = findNext(txtFindInReplace.getText(), matchCase);
            if (range != null) {
                txtEditor.selectRange(range.getStartIndex(), range.getEndIndex());
            }

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnReplace_onAction(ActionEvent actionEvent) {
        if (!txtFindInReplace.getText().equals("")) {
            replaceText(txtFindInReplace.getText(), matchCase, txtReplace.getText());
        }
    }

    @FXML
    private void btnReplaceAll_onAction(ActionEvent actionEvent) {
        if (!txtFindInReplace.getText().equals("")) {
            replaceAllText(txtFindInReplace.getText(), matchCase, txtReplace.getText());
        }
    }

    @FXML
    private void btnReplaceClose_onAction(ActionEvent actionEvent) {
        pneReplace.setManaged(false);
        pneReplace.setVisible(false);
        stsbrBottom.setText("Ok");

    }

    @FXML
    private void txtEditor_onClicked(MouseEvent mouseEvent) {
    }

    @FXML
    private void txtEditor_onDragOver(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    private void txtEditor_onDragDropped(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            File file = dragEvent.getDragboard().getFiles().get(0);
            open(file, dragEvent);
            dragEvent.setDropCompleted(true);
        }
    }
}
