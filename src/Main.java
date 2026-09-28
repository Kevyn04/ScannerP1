import javax.swing.SwingUtilities;

/**
 * @author Kevyn Victor Salonga
 * @author Domenic doyle
 */
public class Main {
    public static void main(String[] args) {
        // run the editor on the Swing event thread
        SwingUtilities.invokeLater(() -> new TextEditor().setVisible(true));
    }
}