package ecommerce.main;

import ecommerce.DataBase.DataBaseConfig;
import ecommerce.gui.auth.LoginFrame;
import ecommerce.gui.components.AppTheme;
import ecommerce.util.AppLogger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Point d'entrée principal — E-Commerce Java / MySQL / Swing
 */
public class MainApp {

    public static void main(String[] args) {
        AppTheme.apply();

        SwingUtilities.invokeLater(() -> {
            AppLogger.info("Démarrage de l'application...");

            if (!testerConnexionDB()) {
                JOptionPane.showMessageDialog(null,
                        "Impossible de se connecter à la base de données.\n\n" +
                                "Vérifiez que :\n" +
                                "  1. MySQL est démarré\n" +
                                "  2. La base '" + DataBaseConfig.DataBaseName + "' existe\n" +
                                "  3. Le fichier schema.sql a été exécuté\n\n" +
                                "URL : " + DataBaseConfig.url_db,
                        "Erreur de connexion MySQL",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            AppLogger.info("Connexion MySQL OK — lancement de l'interface.");
            new LoginFrame();
        });
    }

    private static boolean testerConnexionDB() {
        try {
            Class.forName(DataBaseConfig.Nom_Driver);
            Connection conn = DriverManager.getConnection(
                    DataBaseConfig.url_db,
                    DataBaseConfig.username,
                    DataBaseConfig.PASSWORD
            );
            conn.close();
            return true;
        } catch (Exception e) {
            AppLogger.error("Échec connexion MySQL : " + e.getMessage(), e);
            return false;
        }
    }
}