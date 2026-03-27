package ecommerce.dao.impl;

import ecommerce.dao.interfaces.IProduitDAO;
import ecommerce.Models.Produit;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class ProduitDAO extends AbstractDAO implements IProduitDAO {

    private static final String INSERT =
            "INSERT INTO produits (id, nom, description, prix, quantite_stock, categorie_id, vendeur_id, disponible, note_moyenne, nombre_avis, image_url, date_ajout) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE =
            "UPDATE produits SET nom=?, description=?, prix=?, quantite_stock=?, categorie_id=?, disponible=?, note_moyenne=?, nombre_avis=?, image_url=? WHERE id=?";
    private static final String DELETE      = "DELETE FROM produits WHERE id=?";
    private static final String SELECT_ID   = "SELECT * FROM produits WHERE id=?";
    private static final String SELECT_ALL  = "SELECT * FROM produits ORDER BY date_ajout DESC";
    private static final String SELECT_CAT  = "SELECT * FROM produits WHERE categorie_id=? ORDER BY nom";
    private static final String SELECT_VENDEUR = "SELECT * FROM produits WHERE vendeur_id=?";
    private static final String SELECT_STOCK   = "SELECT * FROM produits WHERE disponible=TRUE AND quantite_stock>0";
    private static final String SEARCH_NOM  =
            "SELECT * FROM produits WHERE LOWER(nom) LIKE ? OR LOWER(description) LIKE ? ORDER BY nom";

    @Override
    public void ajouter(Produit p) {
        try (PreparedStatement ps = getConnection().prepareStatement(INSERT)) {
            ps.setString(1,  p.getId());
            ps.setString(2,  p.getNom());
            ps.setString(3,  p.getDescription());
            ps.setDouble(4,  p.getPrix());
            ps.setInt(5,     p.getQuantiteStock());
            ps.setString(6,  p.getCategorieId());
            ps.setString(7,  p.getVendeurId());
            ps.setBoolean(8, p.isDisponible());
            ps.setDouble(9,  p.getNoteMoyenne());
            ps.setInt(10,    p.getNombreAvis());
            ps.setString(11, p.getImageUrl());
            ps.setTimestamp(12, Timestamp.valueOf(p.getDateAjout()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout produit : " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(Produit p) {
        try (PreparedStatement ps = getConnection().prepareStatement(UPDATE)) {
            ps.setString(1,  p.getNom());
            ps.setString(2,  p.getDescription());
            ps.setDouble(3,  p.getPrix());
            ps.setInt(4,     p.getQuantiteStock());
            ps.setString(5,  p.getCategorieId());
            ps.setBoolean(6, p.isDisponible());
            ps.setDouble(7,  p.getNoteMoyenne());
            ps.setInt(8,     p.getNombreAvis());
            ps.setString(9,  p.getImageUrl());
            ps.setString(10, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification produit : " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression produit : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Produit> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParId produit", e); }
        return Optional.empty();
    }

    @Override
    public List<Produit> trouverTous() {
        return executer(SELECT_ALL);
    }

    @Override
    public List<Produit> trouverParCategorie(String categorieId) {
        List<Produit> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_CAT)) {
            ps.setString(1, categorieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParCategorie", e); }
        return liste;
    }

    @Override
    public List<Produit> rechercherParNom(String motCle) {
        List<Produit> liste = new ArrayList<>();
        String mc = "%" + motCle.toLowerCase() + "%";
        try (PreparedStatement ps = getConnection().prepareStatement(SEARCH_NOM)) {
            ps.setString(1, mc);
            ps.setString(2, mc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur rechercherParNom", e); }
        return liste;
    }

    @Override
    public List<Produit> trouverParVendeur(String vendeurId) {
        List<Produit> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_VENDEUR)) {
            ps.setString(1, vendeurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParVendeur", e); }
        return liste;
    }

    @Override
    public List<Produit> trouverEnStock() {
        return executer(SELECT_STOCK);
    }

    @Override
    public void sauvegarder() {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<Produit> executer(String sql) {
        List<Produit> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { AppLogger.error("Erreur requête produits", e); }
        return liste;
    }

    private Produit mapper(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getString("id"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setPrix(rs.getDouble("prix"));
        p.setQuantiteStock(rs.getInt("quantite_stock"));
        p.setCategorieId(rs.getString("categorie_id"));
        p.setVendeurId(rs.getString("vendeur_id"));
        p.setDisponible(rs.getBoolean("disponible"));
        p.setNoteMoyenne(rs.getDouble("note_moyenne"));
        p.setNombreAvis(rs.getInt("nombre_avis"));
        p.setImageUrl(rs.getString("image_url"));
        Timestamp ts = rs.getTimestamp("date_ajout");
        if (ts != null) p.setDateAjout(ts.toLocalDateTime());
        return p;
    }
}