package ecommerce.dao.impl;

import ecommerce.dao.interfaces.IPromotionDAO;
import ecommerce.Models.Promotion;
import ecommerce.util.AppLogger;

import java.sql.*;
import java.util.*;

public class PromotionDAO extends AbstractDAO implements IPromotionDAO {

    private static final String INSERT =
            "INSERT INTO promotions (id, code, pourcentage_remise, montant_minimum, utilisations_max, utilisations_actuelles, date_debut, date_fin, active) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE =
            "UPDATE promotions SET utilisations_actuelles=?, active=? WHERE id=?";
    private static final String DELETE     = "DELETE FROM promotions WHERE id=?";
    private static final String SELECT_ID  = "SELECT * FROM promotions WHERE id=?";
    private static final String SELECT_ALL = "SELECT * FROM promotions ORDER BY date_fin DESC";
    private static final String SELECT_CODE= "SELECT * FROM promotions WHERE UPPER(code)=UPPER(?)";

    @Override
    public void ajouter(Promotion p) {
        try (PreparedStatement ps = getConnection().prepareStatement(INSERT)) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getCode());
            ps.setDouble(3, p.getPourcentageRemise());
            ps.setDouble(4, p.getMontantMinimum());
            ps.setInt(5,    p.getUtilisationsMax());
            ps.setInt(6,    p.getUtilisationsActuelles());
            ps.setTimestamp(7, Timestamp.valueOf(p.getDateDebut()));
            ps.setTimestamp(8, Timestamp.valueOf(p.getDateFin()));
            ps.setBoolean(9, p.isActive());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Erreur ajout promotion : " + e.getMessage(), e); }
    }

    @Override
    public void modifier(Promotion p) {
        try (PreparedStatement ps = getConnection().prepareStatement(UPDATE)) {
            ps.setInt(1,     p.getUtilisationsActuelles());
            ps.setBoolean(2, p.isActive());
            ps.setString(3,  p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Erreur modification promotion : " + e.getMessage(), e); }
    }

    @Override
    public void supprimer(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(DELETE)) {
            ps.setString(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    @Override
    public Optional<Promotion> trouverParId(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapper(rs)); }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParId promotion", e); }
        return Optional.empty();
    }

    @Override
    public Optional<Promotion> trouverParCode(String code) {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_CODE)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapper(rs)); }
        } catch (SQLException e) { AppLogger.error("Erreur trouverParCode promotion", e); }
        return Optional.empty();
    }

    @Override
    public List<Promotion> trouverTous() {
        List<Promotion> liste = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { AppLogger.error("Erreur trouverTous promotions", e); }
        return liste;
    }

    @Override public void sauvegarder() {}

    private Promotion mapper(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setId(rs.getString("id"));
        p.setCode(rs.getString("code"));
        p.setPourcentageRemise(rs.getDouble("pourcentage_remise"));
        p.setMontantMinimum(rs.getDouble("montant_minimum"));
        p.setUtilisationsMax(rs.getInt("utilisations_max"));
        p.setUtilisationsActuelles(rs.getInt("utilisations_actuelles"));
        Timestamp td = rs.getTimestamp("date_debut");
        Timestamp tf = rs.getTimestamp("date_fin");
        if (td != null) p.setDateDebut(td.toLocalDateTime());
        if (tf != null) p.setDateFin(tf.toLocalDateTime());
        p.setActive(rs.getBoolean("active"));
        return p;
    }
}