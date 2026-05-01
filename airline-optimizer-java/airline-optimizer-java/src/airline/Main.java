package airline;

import javax.swing.*;

/**
 * Main.java — entry point.
 * Launches the SkyRoute AI application on the Swing Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // Run on the GUI thread (Swing requirement)
        SwingUtilities.invokeLater(() -> {
            try {
                // Use system look-and-feel for native window decorations
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
