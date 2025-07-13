package javaricci.com.br;

import com.formdev.flatlaf.FlatLightLaf;
import javaricci.com.br.MainFrame;

import javax.swing.*;

public class JavaWinZipApp {
    
    public static void main(String[] args) {
        // Configurar o Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Erro ao definir Look and Feel: " + ex.getMessage());
            }
        }
        
        // Configurações do sistema
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Executar na EDT
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Erro ao iniciar a aplicação: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}