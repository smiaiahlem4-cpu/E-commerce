package ecommerce.dao.impl;

import ecommerce.dao.interfaces.IUtilisateurDAO;
import ecommerce.Models.Adresse;
import ecommerce.Models.Role;
import ecommerce.Models.Utilisateur;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class UtilisateurDAO extends AbstractDAO implements IUtilisateurDAO {

    private static final String INSERT_USER =
            "INSERT INTO utilisateurs (id, nom, prenom, email, mot_de_passe_hash, role, actif, telephone, date_inscription) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE utilisateurs SET nom=?, prenom=?, email=?, mot_de_passe_hash=?, role=?, actif=?, telephone=? WHERE id=?";
    private static final String DELETE_USER  = "DELETE FROM utilisateurs WHERE id=?";
    private static final String SELECT_BY_ID = "SELECT * FROM utilisateurs WHERE id=?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM utilisateurs WHERE email=?";
    private static final String SELECT_ALL   = "SELECT * FROM utilisateurs ORDER BY date_inscription DESC";
    private static final String COUNT_EMAIL  = "SELECT COUNT(*) FROM utilisateurs WHERE email=?";
    private static final String INSERT_ADR   =
            "INSERT INTO adresses (id, utilisateur_id, rue, ville, code_postal, pays, principale) VALUES (?,?,?,?,?,?,?)";
    private static final String SELECT_ADR   = "SELECT * FROM adresses WHERE utilisateur_id=?";
    private static final String DELETE_ADR   = "DELETE FROM adresses WHERE utilisateur_id=?";

    @Override
    public void ajouter(Utilisateur u) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_USER)) {
                ps.setString(1, u.getId());
                ps.setString(2, u.getNom());
                ps.setString(3, u.getPrenom());
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getMotDePasseHash());
                ps.setString(6, u.getRole().name());
                ps.setBoolean(7, u.isActif());
                ps.setString(8, u.getTelephone());
                ps.setTimestamp(9, Timestamp.valueOf(u.getDateInscription()));
                ps.executeUpdate();
            }
            insererAdresses(conn, u);
            conn.commit();
            AppLogger.info("Utilisateur ajouté : " + u.getEmail());
        } catch (SQLException e) {
            annulerTransaction(conn);
            throw new RuntimeException("Erreur ajout utilisateur : " + e.getMessage(), e);
        } finally {
            restaurerAutoCommit(conn);
        }
    }

    @Override
    public void modifier(Utilisateur u) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {
                ps.setString(1, u.getNom());
                ps.setString(2, u.getPrenom());
                ps.setString(3, u.getEmail());
                ps.setString(4, u.getMotDePasseHash());
                ps.setString(5, u.getRole().name());
                ps.setBoolean(6, u.isActif());
                ps.setString(7, u.getTelephone());
                ps.setString(8, u.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(DELETE_ADR)) {
                ps.setString(1, u.getId());
                ps.executeUpdate();
            }
            insererAdresses(conn, u);
            conn.commit();
        } catch (SQLException e) {
            annulerTransaction(conn);
            throw new RuntimeException("Erreur modification utilisateur : " + e.getMessage(), e);
        } finally {
            restaurerAutoCommit(conn);
        }
    }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE_USER)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Utilisateur> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BY_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur trouverParId", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilisateur> trouverParEmail(String email) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur trouverParEmail", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Utilisateur> trouverTous() {
        List<Utilisateur> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            AppLogger.error("Erreur trouverTous utilisateurs", e);
        }
        return liste;
    }

    @Override
    public boolean emailExiste(String email) {
        try (PreparedStatement ps = getConnection().prepareStatement(COUNT_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) { return false; }
    }

    @Override
    public void sauvegarder() {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void insererAdresses(Connection conn, Utilisateur u) throws SQLException {
        if (u.getAdresses() == null || u.getAdresses().isEmpty()) return;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_ADR)) {
            for (Adresse a : u.getAdresses()) {
                ps.setString(1, a.getId());
                ps.setString(2, u.getId());
                ps.setString(3, a.getRue());
                ps.setString(4, a.getVille());
                ps.setString(5, a.getCodePostal());
                ps.setString(6, a.getPays());
                ps.setBoolean(7, a.isPrincipale());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getString("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActif(rs.getBoolean("actif"));
        u.setTelephone(rs.getString("telephone"));
        Timestamp ts = rs.getTimestamp("date_inscription");
        if (ts != null) u.setDateInscription(ts.toLocalDateTime());
        u.setAdresses(chargerAdresses(u.getId()));
        return u;
    }

    private List<Adresse> chargerAdresses(String userId) {
        List<Adresse> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ADR)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Adresse a = new Adresse();
                    a.setId(rs.getString("id"));
                    a.setRue(rs.getString("rue"));
                    a.setVille(rs.getString("ville"));
                    a.setCodePostal(rs.getString("code_postal"));
                    a.setPays(rs.getString("pays"));
                    a.setPrincipale(rs.getBoolean("principale"));
                    liste.add(a);
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur chargement adresses", e);
        }
        return liste;
    }
}