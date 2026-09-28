import lexer.Lexer;
import lexer.Token;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

// text editor part of the assignment - new/open/save, cut/copy/paste, undo/redo
// also has a Tools menu that runs the scanner from part 2 on whatever is typed in
/**
 * @author Kevyn Victor Salonga
 */
public class TextEditor extends JFrame {

    private final JTextArea textArea = new JTextArea();
    private final UndoManager undoManager = new UndoManager();
    private final JFileChooser fileChooser = new JFileChooser();

    private File currentFile = null;
    private boolean dirty = false;

    public TextEditor() {
        super("ScannerP1 Text Editor - Untitled");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setLineWrap(false);
        // uhh this has to be attached to the Document not the JTextArea itself,
        // spent like 20 min confused why undo wasnt working before i figured that out
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });
        // yeah i know this is 3 basically identical methods but DocumentListener
        // makes you implement all 3 even if they all just do the same thing here
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
        });

        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        setJMenuBar(buildMenuBar());
        add(buildToolBar(), BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // gotta override the close button too or it just closes without asking
        // about unsaved changes, DO_NOTHING_ON_CLOSE above + this combo is what does it
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (confirmDiscardUnsaved()) dispose();
            }
        });
    }

    // setting up the menu bar and toolbar

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItem("New", e -> newFile()));
        fileMenu.add(menuItem("Open...", e -> openFile()));
        fileMenu.add(menuItem("Save", e -> saveFile()));
        fileMenu.add(menuItem("Save As...", e -> saveFileAs()));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Exit", e -> { if (confirmDiscardUnsaved()) dispose(); }));

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(menuItem("Undo", e -> undo()));
        editMenu.add(menuItem("Redo", e -> redo()));
        editMenu.addSeparator();
        editMenu.add(menuItem("Cut", e -> textArea.cut()));
        editMenu.add(menuItem("Copy", e -> textArea.copy()));
        editMenu.add(menuItem("Paste", e -> textArea.paste()));

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(menuItem("Tokenize (MatrixLang Scanner)", e -> tokenizeCurrentText()));

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(toolsMenu);
        return menuBar;
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(toolButton("New", e -> newFile()));
        toolBar.add(toolButton("Open", e -> openFile()));
        toolBar.add(toolButton("Save", e -> saveFile()));
        toolBar.addSeparator();
        toolBar.add(toolButton("Cut", e -> textArea.cut()));
        toolBar.add(toolButton("Copy", e -> textArea.copy()));
        toolBar.add(toolButton("Paste", e -> textArea.paste()));
        toolBar.addSeparator();
        toolBar.add(toolButton("Undo", e -> undo()));
        toolBar.add(toolButton("Redo", e -> redo()));
        toolBar.addSeparator();
        toolBar.add(toolButton("Tokenize", e -> tokenizeCurrentText()));
        return toolBar;
    }

    private JMenuItem menuItem(String label, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(action);
        return item;
    }

    private JButton toolButton(String label, java.awt.event.ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        return button;
    }

    // new / open / save

    private void newFile() {
        if (!confirmDiscardUnsaved()) return;
        textArea.setText("");
        undoManager.discardAllEdits();
        currentFile = null;
        setDirty(false);
    }

    private void openFile() {
        if (!confirmDiscardUnsaved()) return;
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            textArea.setText(content);
            undoManager.discardAllEdits();
            currentFile = file;
            setDirty(false);
        } catch (IOException ex) {
            showError("Could not open file:\n" + ex.getMessage());
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        writeToFile(currentFile);
    }

    private void saveFileAs() {
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }
        writeToFile(file);
    }

    private void writeToFile(File file) {
        try {
            Files.writeString(file.toPath(), textArea.getText(), StandardCharsets.UTF_8);
            currentFile = file;
            setDirty(false);
        } catch (IOException ex) {
            showError("Could not save file:\n" + ex.getMessage());
        }
    }
    // this for the buttons of
    // undo / redo

    private void undo() {
        try {
            if (undoManager.canUndo()) undoManager.undo();
        } catch (CannotUndoException ignored) {

        }
    }

    private void redo() {
        try {
            if (undoManager.canRedo()) undoManager.redo();
        } catch (CannotRedoException ignored) {

        }
    }

    // runs the scanner on whatever text is currently in the editor
    // and shows the tokens in a popup

    private void tokenizeCurrentText() {
        // pulls whatever's in the text area right now and feeds it straight
        // into the part 2 scanner, kind of a nice way to test both parts at once tbh
        List<Token> tokens = new Lexer(textArea.getText()).scanTokens();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-6s %-14s %s%n", "LINE", "COL", "TYPE", "LEXEME"));
        for (Token token : tokens) {
            sb.append(token).append('\n');
        }

        JTextArea resultArea = new JTextArea(sb.toString(), 25, 55);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setEditable(false);

        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(resultArea),
            "MatrixLang Tokens",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    private void markDirty() {
        setDirty(true);
    }

    // updates the title bar, adds a * when there are unsaved changes
    private void setDirty(boolean dirty) {
        this.dirty = dirty;
        String name = (currentFile != null) ? currentFile.getName() : "Untitled";
        setTitle("ScannerP1 Text Editor - " + name + (dirty ? " *" : ""));
    }

    // asks to discard unsaved changes, returns false if the user cancels
    private boolean confirmDiscardUnsaved() {
        if (!dirty) return true;
        int choice = JOptionPane.showConfirmDialog(
            this,
            "You have unsaved changes. Discard them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return choice == JOptionPane.YES_OPTION;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
