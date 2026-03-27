package ecommerce.dao.impl;

import ecommerce.dao.interfaces.IPaiementDAO;
import ecommerce.Models.ModePaiement;
import ecommerce.Models.Paiement;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class PaiementDAO extends AbstractDAO implements IPaiementDAO {

    private static final String INSERT =
            "INSERT INTO paiements (id, commande_id, montant, mode, valide, reference, date_paiement) VALUES (?,?,?,?,?,?,?)";
    private static final String DELETE      = "DELETE FROM paiements WHERE id=?";
    private static final String SELECT_ID   = "SELECT * FROM paiements WHERE id=?";
    private static final String SELECT_ALL  = "SELECT * FROM paiements ORDER BY date_paiement DESC";
    private static final String SELECT_CMD  = "SELECT * FROM paiements WHERE commande_id=?";

    @Override
    public void ajouter(Paiement p) {
        try (PreparedStatement ps = getConnection().prepareStatement(INSERT)) {
            ps.setString(1,  p.getId());
            ps.setString(2,  p.getCommandeId());
            ps.setDouble(3,  p.getMontant());
            ps.setString(4,  p.getMode().name());
            ps.setBoolean(5, p.isValide());
            ps.setString(6,  p.getReference());
            ps.setTimestamp(7, Timestamp.valueOf(p.getDatePaiement()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout paiement : " + e.getMessage(), e);
        }
    }

    @Override public void modifier(Paiement p) { /* Les paiements sont immuables */ }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE)) {
            ps.setString(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    @Override
    public Optional<Paiement> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParId paiement", e); }
        return Optional.empty();
    }

    @Override
    public List<Paiement> trouverTous() {
        List<Paiement> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { AppLogger.error("Erreur trouverTous paiements", e); }
        return liste;
    }

    @Override
    public List<Paiement> trouverParCommande(String commandeId) {
        List<Paiement> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_CMD)) {
            ps.setString(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParCommande paiement", e); }
        return liste;
    }

    @Override public void sauvegarder() {}

    private Paiement mapper(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setId(rs.getString("id"));
        p.setCommandeId(rs.getString("commande_id"));
        p.setMontant(rs.getDouble("montant"));
        p.setMode(ModePaiement.valueOf(rs.getString("mode")));
        p.setValide(rs.getBoolean("valide"));
        p.setReference(rs.getString("reference"));
        Timestamp ts = rs.getTimestamp("date_paiement");
        if (ts != null) p.setDatePaiement(ts.toLocalDateTime());
        return p;
    }
}