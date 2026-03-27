package ecommerce.dao.impl;

import ecommerce.dao.interfaces.ICommandeDAO;
import ecommerce.Models.*;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class CommandeDAO extends AbstractDAO implements ICommandeDAO {

    private static final String INSERT_CMD =
            "INSERT INTO commandes (id, client_id, montant_total, montant_remise, statut, date_commande, code_promo, notes, livraison_rue, livraison_ville, livraison_code_postal, livraison_pays) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_LIGNE =
            "INSERT INTO lignes_commande (commande_id, produit_id, nom_produit, prix_unitaire, quantite) VALUES (?,?,?,?,?)";
    private static final String UPDATE_CMD =
            "UPDATE commandes SET statut=?, date_livraison=?, notes=? WHERE id=?";
    private static final String DELETE_CMD  = "DELETE FROM commandes WHERE id=?";
    private static final String SELECT_ID   = "SELECT * FROM commandes WHERE id=?";
    private static final String SELECT_ALL  = "SELECT * FROM commandes ORDER BY date_commande DESC";
    private static final String SELECT_CLIENT = "SELECT * FROM commandes WHERE client_id=? ORDER BY date_commande DESC";
    private static final String SELECT_STATUT = "SELECT * FROM commandes WHERE statut=? ORDER BY date_commande DESC";
    private static final String SELECT_LIGNES = "SELECT * FROM lignes_commande WHERE commande_id=?";

    @Override
    public void ajouter(Commande c) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_CMD)) {
                Adresse adr = c.getAdresseLivraison();
                ps.setString(1,  c.getId());
                ps.setString(2,  c.getClientId());
                ps.setDouble(3,  c.getMontantTotal());
                ps.setDouble(4,  c.getMontantRemise());
                ps.setString(5,  c.getStatut().name());
                ps.setTimestamp(6, Timestamp.valueOf(c.getDateCommande()));
                ps.setString(7,  c.getCodePromo());
                ps.setString(8,  c.getNotes());
                ps.setString(9,  adr != null ? adr.getRue() : null);
                ps.setString(10, adr != null ? adr.getVille() : null);
                ps.setString(11, adr != null ? adr.getCodePostal() : null);
                ps.setString(12, adr != null ? adr.getPays() : null);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(INSERT_LIGNE)) {
                for (LigneCommande l : c.getLignes()) {
                    ps.setString(1, c.getId());
                    ps.setString(2, l.getProduitId());
                    ps.setString(3, l.getNomProduit());
                    ps.setDouble(4, l.getPrixUnitaire());
                    ps.setInt(5,    l.getQuantite());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            AppLogger.info("Commande créée : " + c.getNumeroCommande());
        } catch (SQLException e) {
            annulerTransaction(conn);
            throw new RuntimeException("Erreur création commande : " + e.getMessage(), e);
        } finally {
            restaurerAutoCommit(conn);
        }
    }

    @Override
    public void modifier(Commande c) {
        try (PreparedStatement ps = getConnection().prepareStatement(UPDATE_CMD)) {
            ps.setString(1, c.getStatut().name());
            ps.setTimestamp(2, c.getDateLivraison() != null ? Timestamp.valueOf(c.getDateLivraison()) : null);
            ps.setString(3, c.getNotes());
            ps.setString(4, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification commande : " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE_CMD)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression commande : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Commande> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParId commande", e); }
        return Optional.empty();
    }

    @Override
    public List<Commande> trouverTous() {
        return executer(SELECT_ALL, null);
    }

    @Override
    public List<Commande> trouverParClient(String clientId) {
        return executer(SELECT_CLIENT, clientId);
    }

    @Override
    public List<Commande> trouverParStatut(StatutCommande statut) {
        return executer(SELECT_STATUT, statut.name());
    }

    @Override
    public void sauvegarder() {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<Commande> executer(String sql, String param) {
        List<Commande> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur requête commandes", e); }
        return liste;
    }

    private Commande mapper(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setId(rs.getString("id"));
        c.setClientId(rs.getString("client_id"));
        c.setMontantTotal(rs.getDouble("montant_total"));
        c.setMontantRemise(rs.getDouble("montant_remise"));
        c.setStatut(StatutCommande.valueOf(rs.getString("statut")));
        Timestamp dtCmd = rs.getTimestamp("date_commande");
        if (dtCmd != null) c.setDateCommande(dtCmd.toLocalDateTime());
        Timestamp dtLiv = rs.getTimestamp("date_livraison");
        if (dtLiv != null) c.setDateLivraison(dtLiv.toLocalDateTime());
        c.setCodePromo(rs.getString("code_promo"));
        c.setNotes(rs.getString("notes"));

        // Reconstituer l'adresse de livraison depuis les colonnes dénormalisées
        String rue = rs.getString("livraison_rue");
        if (rue != null) {
            Adresse adr = new Adresse(rue,
                    rs.getString("livraison_ville"),
                    rs.getString("livraison_code_postal"),
                    rs.getString("livraison_pays"));
            c.setAdresseLivraison(adr);
        }
        c.setLignes(chargerLignes(c.getId()));
        return c;
    }

    private List<LigneCommande> chargerLignes(String commandeId) {
        List<LigneCommande> lignes = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_LIGNES)) {
            ps.setString(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LigneCommande l = new LigneCommande(
                            rs.getString("produit_id"),
                            rs.getString("nom_produit"),
                            rs.getDouble("prix_unitaire"),
                            rs.getInt("quantite")
                    );
                    lignes.add(l);
                }
            }
        } catch (SQLException e) { AppLogger.error("Erreur chargement lignes commande", e); }
        return lignes;
    }
}