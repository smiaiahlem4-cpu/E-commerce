package ecommerce.dao.impl;

import ecommerce.DataBase.DataBaseConfig;
import ecommerce.util.AppLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de base pour tous les DAOs JDBC.
 * Utilise DataBaseConfig (package ecommerce.DataBase) pour la connexion.
 */
public abstract class AbstractDAO {

    protected Connection getConnection() {
        try {
            Class.forName(DataBaseConfig.Nom_Driver);
            return DriverManager.getConnection(
                    DataBaseConfig.url_db,
                    DataBaseConfig.username,
                    DataBaseConfig.PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Driver MySQL introuvable : " + DataBaseConfig.Nom_Driver, e);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Connexion MySQL échouée sur " + DataBaseConfig.url_db +
                            " — vérifiez que MySQL est démarré et que la base '" +
                            DataBaseConfig.DataBaseName + "' existe.", e);
        }
    }

    protected void fermer(AutoCloseable... ressources) {
        for (AutoCloseable r : ressources) {
            if (r != null) {
                try { r.close(); }
                catch (Exception e) {
                    AppLogger.warn("Erreur fermeture ressource JDBC : " + e.getMessage());
                }
            }
        }
    }

    protected void annulerTransaction(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); }
            catch (SQLException e) { AppLogger.error("Erreur rollback", e); }
        }
    }

    protected void restaurerAutoCommit(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); }
            catch (SQLException e) { AppLogger.error("Erreur restauration autoCommit", e); }
        }
    }
}