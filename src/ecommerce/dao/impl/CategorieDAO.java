package ecommerce.dao.impl;

import ecommerce.dao.interfaces.ICategorieDAO;
import ecommerce.Models.Categorie;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class CategorieDAO extends AbstractDAO implements ICategorieDAO {

    private static final String INSERT     = "INSERT INTO categories (id, nom, description) VALUES (?,?,?)";
    private static final String UPDATE     = "UPDATE categories SET nom=?, description=? WHERE id=?";
    private static final String DELETE     = "DELETE FROM categories WHERE id=?";
    private static final String SELECT_ID  = "SELECT * FROM categories WHERE id=?";
    private static final String SELECT_NOM = "SELECT * FROM categories WHERE LOWER(nom)=LOWER(?)";
    private static final String SELECT_ALL = "SELECT * FROM categories ORDER BY nom";

    @Override
    public void ajouter(Categorie c) {
        try (PreparedStatement ps = getConnection().prepareStatement(INSERT)) {
            ps.setString(1, c.getId()); ps.setString(2, c.getNom()); ps.setString(3, c.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Erreur ajout catégorie : " + e.getMessage(), e); }
    }

    @Override
    public void modifier(Categorie c) {
        try (PreparedStatement ps = getConnection().prepareStatement(UPDATE)) {
            ps.setString(1, c.getNom()); ps.setString(2, c.getDescription()); ps.setString(3, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Erreur modification catégorie : " + e.getMessage(), e); }
    }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE)) {
            ps.setString(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    @Override
    public Optional<Categorie> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapper(rs)); }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParId catégorie", e); }
        return Optional.empty();
    }

    @Override
    public Optional<Categorie> trouverParNom(String nom) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_NOM)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapper(rs)); }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParNom catégorie", e); }
        return Optional.empty();
    }

    @Override
    public List<Categorie> trouverTous() {
        List<Categorie> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { AppLogger.error("Erreur trouverTous catégories", e); }
        return liste;
    }

    @Override public void sauvegarder() {}

    private Categorie mapper(ResultSet rs) throws SQLException {
        return new Categorie(rs.getString("nom"), rs.getString("description")) {{
            setId(rs.getString("id"));
        }};
    }
}