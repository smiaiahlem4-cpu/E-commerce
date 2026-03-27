package ecommerce.dao.impl;

import ecommerce.dao.interfaces.IAvisDAO;
import ecommerce.Models.AvisClient;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class AvisDAO extends AbstractDAO implements IAvisDAO {

    private static final String INSERT =
            "INSERT INTO avis_clients (id, produit_id, client_id, nom_client, note, commentaire, date_avis) VALUES (?,?,?,?,?,?,?)";
    private static final String DELETE       = "DELETE FROM avis_clients WHERE id=?";
    private static final String SELECT_ID    = "SELECT * FROM avis_clients WHERE id=?";
    private static final String SELECT_ALL   = "SELECT * FROM avis_clients ORDER BY date_avis DESC";
    private static final String SELECT_PROD  = "SELECT * FROM avis_clients WHERE produit_id=? ORDER BY date_avis DESC";
    private static final String CHECK_EXISTE = "SELECT COUNT(*) FROM avis_clients WHERE client_id=? AND produit_id=?";

    @Override
    public void ajouter(AvisClient a) {
        try (PreparedStatement ps = getConnection().prepareStatement(INSERT)) {
            ps.setString(1, a.getId());
            ps.setString(2, a.getProduitId());
            ps.setString(3, a.getClientId());
            ps.setString(4, a.getNomClient());
            ps.setInt(5,    a.getNote());
            ps.setString(6, a.getCommentaire());
            ps.setTimestamp(7, Timestamp.valueOf(a.getDateAvis()));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Erreur ajout avis : " + e.getMessage(), e); }
    }

    @Override public void modifier(AvisClient a) { /* Avis immuables */ }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE)) {
            ps.setString(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    @Override
    public Optional<AvisClient> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapper(rs)); }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParId avis", e); }
        return Optional.empty();
    }

    @Override
    public List<AvisClient> trouverTous() {
        List<AvisClient> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { AppLogger.error("Erreur trouverTous avis", e); }
        return liste;
    }

    @Override
    public List<AvisClient> trouverParProduit(String produitId) {
        List<AvisClient> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_PROD)) {
            ps.setString(1, produitId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) liste.add(mapper(rs)); }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParProduit avis", e); }
        return liste;
    }

    @Override
    public boolean aDejaCommente(String clientId, String produitId) {
        try (PreparedStatement ps = getConnection().prepareStatement(CHECK_EXISTE)) {
            ps.setString(1, clientId); ps.setString(2, produitId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) { return false; }
    }

    @Override public void sauvegarder() {}

    private AvisClient mapper(ResultSet rs) throws SQLException {
        AvisClient a = new AvisClient();
        a.setId(rs.getString("id"));
        a.setProduitId(rs.getString("produit_id"));
        a.setClientId(rs.getString("client_id"));
        a.setNomClient(rs.getString("nom_client"));
        a.setNote(rs.getInt("note"));
        a.setCommentaire(rs.getString("commentaire"));
        Timestamp ts = rs.getTimestamp("date_avis");
        if (ts != null) a.setDateAvis(ts.toLocalDateTime());
        return a;
    }
}