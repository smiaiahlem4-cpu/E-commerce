package ecommerce.gui.admin;

import ecommerce.gui.auth.LoginFrame;
import ecommerce.gui.components.AppTheme;
import ecommerce.Models.*;
import ecommerce.service.ServiceLocator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminFrame extends JFrame {

    private final ServiceLocator sl     = ServiceLocator.getInstance();
    private final Utilisateur    admin;

    private DefaultTableModel produitsModel;
    private DefaultTableModel usersModel;
    private DefaultTableModel commandesModel;
    private DefaultTableModel promosModel;

    public AdminFrame(Utilisateur admin) {
        super("E-Commerce — Administration");
        this.admin = admin;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        AppTheme.apply();
        build();
        setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(30, 27, 75));
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Administration E-Commerce");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel role = new JLabel("ADMIN — " + admin.getNomComplet());
        role.setFont(AppTheme.FONT_SMALL);
        role.setForeground(new Color(199, 210, 254));

        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.setFont(AppTheme.FONT_SMALL);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(AppTheme.DANGER);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { sl.authService.deconnecter(); dispose(); new LoginFrame(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(role);
        right.add(btnLogout);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BODY);
        tabs.addTab("  Tableau de bord  ", buildDashboardTab());
        tabs.addTab("  Produits  ",        buildProduitsTab());
        tabs.addTab("  Commandes  ",       buildCommandesTab());
        tabs.addTab("  Utilisateurs  ",    buildUsersTab());
        tabs.addTab("  Promotions  ",      buildPromosTab());
        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i == 1) refreshProduits();
            if (i == 2) refreshCommandes();
            if (i == 3) refreshUsers();
            if (i == 4) refreshPromos();
        });
        return tabs;
    }

    // ── TABLEAU DE BORD ───────────────────────────────────────────────────────

    private JPanel buildDashboardTab() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Stats cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);

        Runnable refreshDash = () -> {
            cards.removeAll();
            cards.add(AppTheme.statCard("CA total",
                    String.format("%.3f DT", sl.statsService.chiffreAffairesTotal()), AppTheme.PRIMARY));
            cards.add(AppTheme.statCard("CA aujourd'hui",
                    String.format("%.3f DT", sl.statsService.chiffreAffairesAujourdhui()), AppTheme.SUCCESS));
            cards.add(AppTheme.statCard("Commandes aujourd'hui",
                    String.valueOf(sl.statsService.nombreCommandesAujourdhui()), AppTheme.WARNING));
            cards.add(AppTheme.statCard("Clients inscrits",
                    String.valueOf(sl.statsService.nombreClientsTotal()), AppTheme.PRIMARY_DARK));
            cards.revalidate(); cards.repaint();
        };
        refreshDash.run();

        // Stock faible
        String[] colsStock = {"Produit", "Stock restant"};
        DefaultTableModel stockModel = new DefaultTableModel(colsStock, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable stockTable = new JTable(stockModel);
        styleTable(stockTable);
        sl.statsService.produitsStockFaible(5).forEach(pr ->
                stockModel.addRow(new Object[]{ pr.getNom(), pr.getQuantiteStock() }));

        // Top produits
        String[] colsTop = {"Produit", "Qté vendue"};
        DefaultTableModel topModel = new DefaultTableModel(colsTop, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topTable = new JTable(topModel);
        styleTable(topTable);
        sl.statsService.topProduitsVendus(5).forEach(e ->
                topModel.addRow(new Object[]{ e.getKey(), e.getValue() }));

        JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
        tables.setOpaque(false);

        JPanel stockPanel = new JPanel(new BorderLayout(0, 8));
        stockPanel.setOpaque(false);
        stockPanel.add(AppTheme.label("Stock faible (≤ 5)", AppTheme.FONT_H2, AppTheme.DANGER), BorderLayout.NORTH);
        stockPanel.add(new JScrollPane(stockTable), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(AppTheme.label("Top 5 produits vendus", AppTheme.FONT_H2, AppTheme.PRIMARY), BorderLayout.NORTH);
        topPanel.add(new JScrollPane(topTable), BorderLayout.CENTER);

        tables.add(stockPanel);
        tables.add(topPanel);

        JButton btnRefresh = AppTheme.btnSecondary("Actualiser");
        btnRefresh.addActionListener(e -> refreshDash.run());

        p.add(cards,      BorderLayout.NORTH);
        p.add(tables,     BorderLayout.CENTER);
        p.add(btnRefresh, BorderLayout.SOUTH);
        return p;
    }

    // ── PRODUITS ──────────────────────────────────────────────────────────────

    private JPanel buildProduitsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Nom", "Catégorie", "Prix (DT)", "Stock", "Disponible", "Note"};
        produitsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(produitsModel);
        hideIdColumn(table);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnAjouter   = AppTheme.btnSuccess("+ Nouveau produit");
        JButton btnModifier  = AppTheme.btnPrimary("Modifier");
        JButton btnSupprimer = AppTheme.btnDanger("Supprimer");
        JButton btnRefresh   = AppTheme.btnSecondary("Actualiser");
        btnPanel.add(btnAjouter); btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer); btnPanel.add(btnRefresh);

        btnAjouter.addActionListener(e   -> showProduitDialog(null));
        btnModifier.addActionListener(e  -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un produit."); return; }
            String id = (String) produitsModel.getValueAt(row, 0);
            sl.produitService.produitsEnStock().stream()
                    .filter(pr -> pr.getId().equals(id)).findFirst()
                    .ifPresent(this::showProduitDialog);
        });
        btnSupprimer.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un produit."); return; }
            if (!confirm("Supprimer ce produit ?")) return;
            sl.produitService.supprimerProduit((String) produitsModel.getValueAt(row, 0));
            refreshProduits();
        });
        btnRefresh.addActionListener(e -> refreshProduits());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        refreshProduits();
        return p;
    }

    private void showProduitDialog(Produit existant) {
        boolean isNew = existant == null;
        JTextField fNom  = AppTheme.textField("Nom");
        JTextField fDesc = AppTheme.textField("Description");
        JTextField fPrix = AppTheme.textField("Prix");
        JTextField fStock= AppTheme.textField("Stock");
        JComboBox<String> fCat = new JComboBox<>();
        List<Categorie> cats = sl.produitService.toutesLesCategories();
        cats.forEach(c -> fCat.addItem(c.getNom()));

        if (!isNew) {
            fNom.setText(existant.getNom());
            fDesc.setText(existant.getDescription());
            fPrix.setText(String.valueOf(existant.getPrix()));
            fStock.setText(String.valueOf(existant.getQuantiteStock()));
            cats.stream().filter(c -> c.getId().equals(existant.getCategorieId()))
                    .findFirst().ifPresent(c -> fCat.setSelectedItem(c.getNom()));
        }

        JPanel pan = new JPanel(new GridLayout(0, 1, 0, 6));
        pan.setBorder(new EmptyBorder(8, 8, 8, 8));
        pan.add(new JLabel("Nom :")); pan.add(fNom);
        pan.add(new JLabel("Description :")); pan.add(fDesc);
        pan.add(new JLabel("Prix (DT) :")); pan.add(fPrix);
        pan.add(new JLabel("Stock :")); pan.add(fStock);
        pan.add(new JLabel("Catégorie :")); pan.add(fCat);

        if (JOptionPane.showConfirmDialog(this, pan,
                isNew ? "Nouveau produit" : "Modifier le produit",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            String catId = cats.get(fCat.getSelectedIndex()).getId();
            double prix  = Double.parseDouble(fPrix.getText().replace(",", "."));
            int    stock = Integer.parseInt(fStock.getText());
            if (isNew) {
                sl.produitService.ajouterProduit(fNom.getText(), fDesc.getText(), prix, stock, catId, admin.getId());
            } else {
                sl.produitService.modifierProduit(existant.getId(), fNom.getText(), fDesc.getText(), prix, stock, true);
            }
            refreshProduits();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void refreshProduits() {
        produitsModel.setRowCount(0);
        List<Categorie> cats = sl.produitService.toutesLesCategories();
        sl.produitService.tousLesProduits().forEach(pr -> {
            String cat = cats.stream().filter(c -> c.getId().equals(pr.getCategorieId()))
                    .map(Categorie::getNom).findFirst().orElse("-");
            produitsModel.addRow(new Object[]{
                    pr.getId(), pr.getNom(), cat,
                    String.format("%.3f", pr.getPrix()), pr.getQuantiteStock(),
                    pr.isDisponible() ? "Oui" : "Non",
                    String.format("%.1f", pr.getNoteMoyenne())
            });
        });
    }

    // ── COMMANDES ─────────────────────────────────────────────────────────────

    private JPanel buildCommandesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "N° Commande", "Client ID", "Montant (DT)", "Statut", "Date"};
        commandesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(commandesModel);
        hideIdColumn(table);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);

        String[] statuts = {"CONFIRMEE", "EN_COURS_LIVRAISON", "LIVREE", "ANNULEE", "REMBOURSEE"};
        JComboBox<String> statutCombo = new JComboBox<>(statuts);
        JButton btnChanger  = AppTheme.btnPrimary("Changer le statut");
        JButton btnRefresh  = AppTheme.btnSecondary("Actualiser");
        btnPanel.add(new JLabel("Nouveau statut :")); btnPanel.add(statutCombo);
        btnPanel.add(btnChanger); btnPanel.add(btnRefresh);

        btnChanger.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez une commande."); return; }
            String id  = (String) commandesModel.getValueAt(row, 0);
            StatutCommande s = StatutCommande.valueOf((String) statutCombo.getSelectedItem());
            sl.commandeService.changerStatut(id, s);
            refreshCommandes();
            showSuccess("Statut mis à jour.");
        });
        btnRefresh.addActionListener(e -> refreshCommandes());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        refreshCommandes();
        return p;
    }

    private void refreshCommandes() {
        commandesModel.setRowCount(0);
        sl.commandeService.toutesLesCommandes().forEach(c ->
                commandesModel.addRow(new Object[]{
                        c.getId(), c.getNumeroCommande(), c.getClientId(),
                        String.format("%.3f", c.getMontantAPayer()),
                        c.getStatut().getLibelle(),
                        c.getDateCommande().toLocalDate()
                }));
    }

    // ── UTILISATEURS ──────────────────────────────────────────────────────────

    private JPanel buildUsersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Nom complet", "Email", "Rôle", "Actif", "Inscrit le"};
        usersModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(usersModel);
        hideIdColumn(table);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCreer     = AppTheme.btnSuccess("+ Créer un compte");
        JButton btnDesactiver= AppTheme.btnDanger("Désactiver");
        JButton btnRefresh   = AppTheme.btnSecondary("Actualiser");
        btnPanel.add(btnCreer); btnPanel.add(btnDesactiver); btnPanel.add(btnRefresh);

        btnRefresh.addActionListener(e -> refreshUsers());
        btnCreer.addActionListener(e   -> showCreerUserDialog());
        btnDesactiver.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un utilisateur."); return; }
            // toggle actif via DAO direct
            showInfo("Fonctionnalité : modifier l'utilisateur via le DAO.");
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        refreshUsers();
        return p;
    }

    private void showCreerUserDialog() {
        JTextField fNom    = AppTheme.textField("Nom");
        JTextField fPrenom = AppTheme.textField("Prénom");
        JTextField fEmail  = AppTheme.textField("Email");
        JPasswordField fPass = AppTheme.passwordField();
        String[] roles = {"CLIENT", "VENDEUR", "ADMIN"};
        JComboBox<String> fRole = new JComboBox<>(roles);

        JPanel pan = new JPanel(new GridLayout(0, 1, 0, 6));
        pan.add(new JLabel("Nom :")); pan.add(fNom);
        pan.add(new JLabel("Prénom :")); pan.add(fPrenom);
        pan.add(new JLabel("Email :")); pan.add(fEmail);
        pan.add(new JLabel("Mot de passe :")); pan.add(fPass);
        pan.add(new JLabel("Rôle :")); pan.add(fRole);

        if (JOptionPane.showConfirmDialog(this, pan, "Créer un compte",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Role r = Role.valueOf((String) fRole.getSelectedItem());
                sl.authService.creerCompte(fNom.getText(), fPrenom.getText(),
                        fEmail.getText(), new String(fPass.getPassword()), r);
                refreshUsers(); showSuccess("Compte créé !");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void refreshUsers() {
        usersModel.setRowCount(0);
        sl.authService != null; // just to reference it
        new ecommerce.dao.impl.UtilisateurDAO().trouverTous().forEach(u ->
                usersModel.addRow(new Object[]{
                        u.getId(), u.getNomComplet(), u.getEmail(),
                        u.getRole().name(), u.isActif() ? "Oui" : "Non",
                        u.getDateInscription().toLocalDate()
                }));
    }

    // ── PROMOTIONS ────────────────────────────────────────────────────────────

    private JPanel buildPromosTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Code", "Remise %", "Montant min.", "Utilisations", "Expire le", "Active"};
        promosModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(promosModel);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnAjouter  = AppTheme.btnSuccess("+ Nouvelle promo");
        JButton btnRefresh  = AppTheme.btnSecondary("Actualiser");
        btnPanel.add(btnAjouter); btnPanel.add(btnRefresh);

        btnRefresh.addActionListener(e -> refreshPromos());
        btnAjouter.addActionListener(e -> showPromoDialog());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        refreshPromos();
        return p;
    }

    private void showPromoDialog() {
        JTextField fCode  = AppTheme.textField("Ex: PROMO20");
        JTextField fPct   = AppTheme.textField("Ex: 20");
        JTextField fMin   = AppTheme.textField("Ex: 100");
        JTextField fMax   = AppTheme.textField("0 = illimité");
        JTextField fJours = AppTheme.textField("Nombre de jours de validité");

        JPanel pan = new JPanel(new GridLayout(0, 1, 0, 6));
        pan.add(new JLabel("Code :")); pan.add(fCode);
        pan.add(new JLabel("Remise (%) :")); pan.add(fPct);
        pan.add(new JLabel("Montant minimum (DT) :")); pan.add(fMin);
        pan.add(new JLabel("Utilisations max :")); pan.add(fMax);
        pan.add(new JLabel("Validité (jours) :")); pan.add(fJours);

        if (JOptionPane.showConfirmDialog(this, pan, "Nouvelle promotion",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double pct  = Double.parseDouble(fPct.getText());
                double min  = Double.parseDouble(fMin.getText());
                int    max  = Integer.parseInt(fMax.getText());
                int    jours= Integer.parseInt(fJours.getText());
                Promotion promo = new Promotion(fCode.getText(), pct, min, max,
                        java.time.LocalDateTime.now().plusDays(jours));
                new ecommerce.dao.impl.PromotionDAO().ajouter(promo);
                refreshPromos(); showSuccess("Promotion créée !");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void refreshPromos() {
        promosModel.setRowCount(0);
        new ecommerce.dao.impl.PromotionDAO().trouverTous().forEach(pr ->
                promosModel.addRow(new Object[]{
                        pr.getCode(),
                        pr.getPourcentageRemise() + "%",
                        String.format("%.3f DT", pr.getMontantMinimum()),
                        pr.getUtilisationsActuelles() + "/" +
                                (pr.getUtilisationsMax() == 0 ? "∞" : pr.getUtilisationsMax()),
                        pr.getDateFin().toLocalDate(),
                        pr.isActive() ? "Oui" : "Non"
                }));
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private void styleTable(JTable t) {
        t.setFont(AppTheme.FONT_BODY); t.setRowHeight(28);
        t.setGridColor(AppTheme.BORDER_COLOR);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(AppTheme.PRIMARY_LIGHT);
        t.getTableHeader().setForeground(AppTheme.PRIMARY_DARK);
        t.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        t.setFillsViewportHeight(true);
    }

    private void hideIdColumn(JTable t) {
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setPreferredWidth(0);
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}