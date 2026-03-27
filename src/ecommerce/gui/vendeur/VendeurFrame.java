package ecommerce.gui.vendeur;

import ecommerce.gui.auth.LoginFrame;
import ecommerce.gui.components.AppTheme;
import ecommerce.Models.*;
import ecommerce.service.ServiceLocator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VendeurFrame extends JFrame {

    private final ServiceLocator sl = ServiceLocator.getInstance();
    private final Utilisateur    vendeur;

    private DefaultTableModel mesProdModel;
    private DefaultTableModel mesVentesModel;

    public VendeurFrame(Utilisateur vendeur) {
        super("E-Commerce — Espace Vendeur");
        this.vendeur = vendeur;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
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
        bar.setBackground(AppTheme.SUCCESS);
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Espace Vendeur");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel info = new JLabel(vendeur.getNomComplet());
        info.setFont(AppTheme.FONT_SMALL);
        info.setForeground(new Color(187, 247, 208));

        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.setBackground(new Color(21, 128, 61));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { sl.authService.deconnecter(); dispose(); new LoginFrame(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(info); right.add(btnLogout);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BODY);
        tabs.addTab("  Mes produits  ", buildMesProduits());
        tabs.addTab("  Mes ventes    ", buildMesVentes());
        tabs.addTab("  Avis recus    ", buildAvisTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) refreshProduits();
            if (tabs.getSelectedIndex() == 1) refreshVentes();
        });
        return tabs;
    }

    // ── MES PRODUITS ──────────────────────────────────────────────────────────

    private JPanel buildMesProduits() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Nom", "Prix (DT)", "Stock", "Disponible", "Catégorie"};
        mesProdModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(mesProdModel);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnAjouter   = AppTheme.btnSuccess("+ Nouveau produit");
        JButton btnModifier  = AppTheme.btnPrimary("Modifier");
        JButton btnStock     = AppTheme.btnSecondary("Ajuster le stock");
        JButton btnRefresh   = AppTheme.btnSecondary("Actualiser");
        btnPanel.add(btnAjouter); btnPanel.add(btnModifier);
        btnPanel.add(btnStock);   btnPanel.add(btnRefresh);

        btnAjouter.addActionListener(e -> showProduitDialog(null));
        btnModifier.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un produit."); return; }
            String id = (String) mesProdModel.getValueAt(row, 0);
            sl.produitService.parVendeur(vendeur.getId()).stream()
                    .filter(pr -> pr.getId().equals(id)).findFirst()
                    .ifPresent(this::showProduitDialog);
        });
        btnStock.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un produit."); return; }
            String id = (String) mesProdModel.getValueAt(row, 0);
            String delta = JOptionPane.showInputDialog(this,
                    "Entrez la variation de stock (ex: +10 ou -5) :", "Ajuster le stock",
                    JOptionPane.PLAIN_MESSAGE);
            if (delta == null || delta.isBlank()) return;
            try {
                int d = Integer.parseInt(delta.trim());
                sl.produitService.ajusterStock(id, d);
                refreshProduits(); showSuccess("Stock mis à jour.");
            } catch (NumberFormatException ex) { showError("Valeur invalide."); }
        });
        btnRefresh.addActionListener(e -> refreshProduits());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        refreshProduits();
        return p;
    }

    private void showProduitDialog(Produit existant) {
        boolean isNew = existant == null;
        JTextField fNom   = AppTheme.textField("Nom du produit");
        JTextField fDesc  = AppTheme.textField("Description");
        JTextField fPrix  = AppTheme.textField("Prix en DT");
        JTextField fStock = AppTheme.textField("Stock initial");
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
                isNew ? "Nouveau produit" : "Modifier produit",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            String catId = cats.get(fCat.getSelectedIndex()).getId();
            double prix  = Double.parseDouble(fPrix.getText().replace(",", "."));
            int    stock = Integer.parseInt(fStock.getText());
            if (isNew) {
                sl.produitService.ajouterProduit(fNom.getText(), fDesc.getText(),
                        prix, stock, catId, vendeur.getId());
            } else {
                sl.produitService.modifierProduit(existant.getId(), fNom.getText(),
                        fDesc.getText(), prix, stock, true);
            }
            refreshProduits();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void refreshProduits() {
        mesProdModel.setRowCount(0);
        List<Categorie> cats = sl.produitService.toutesLesCategories();
        sl.produitService.parVendeur(vendeur.getId()).forEach(pr -> {
            String cat = cats.stream().filter(c -> c.getId().equals(pr.getCategorieId()))
                    .map(Categorie::getNom).findFirst().orElse("-");
            mesProdModel.addRow(new Object[]{
                    pr.getId(), pr.getNom(),
                    String.format("%.3f", pr.getPrix()),
                    pr.getQuantiteStock(),
                    pr.isDisponible() ? "Oui" : "Non", cat
            });
        });
    }

    // ── MES VENTES ────────────────────────────────────────────────────────────

    private JPanel buildMesVentes() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Produit", "Qté vendue", "CA généré (DT)"};
        mesVentesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(mesVentesModel);
        styleTable(table);

        JButton btnRefresh = AppTheme.btnSecondary("Actualiser");
        btnRefresh.addActionListener(e -> refreshVentes());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false); bottom.add(btnRefresh);
        p.add(bottom, BorderLayout.SOUTH);
        refreshVentes();
        return p;
    }

    private void refreshVentes() {
        mesVentesModel.setRowCount(0);
        List<String> mesProdIds = sl.produitService.parVendeur(vendeur.getId())
                .stream().map(Produit::getId).toList();

        java.util.Map<String, double[]> stats = new java.util.LinkedHashMap<>();
        sl.commandeService.toutesLesCommandes().stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .flatMap(c -> c.getLignes().stream())
                .filter(l -> mesProdIds.contains(l.getProduitId()))
                .forEach(l -> {
                    stats.computeIfAbsent(l.getNomProduit(), k -> new double[]{0, 0});
                    stats.get(l.getNomProduit())[0] += l.getQuantite();
                    stats.get(l.getNomProduit())[1] += l.getSousTotal();
                });
        stats.forEach((nom, vals) ->
                mesVentesModel.addRow(new Object[]{
                        nom, (int) vals[0], String.format("%.3f", vals[1])
                }));
    }

    // ── AVIS REÇUS ────────────────────────────────────────────────────────────

    private JPanel buildAvisTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Produit", "Client", "Note", "Commentaire", "Date"};
        DefaultTableModel avisModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(avisModel);
        styleTable(table);

        sl.produitService.parVendeur(vendeur.getId()).forEach(pr ->
                sl.avisService.avisParProduit(pr.getId()).forEach(a ->
                        avisModel.addRow(new Object[]{
                                pr.getNom(), a.getNomClient(), a.getEtoiles(),
                                a.getCommentaire(), a.getDateAvis().toLocalDate()
                        })
                )
        );

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private void styleTable(JTable t) {
        t.setFont(AppTheme.FONT_BODY); t.setRowHeight(28);
        t.setGridColor(AppTheme.BORDER_COLOR);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(209, 250, 229));
        t.getTableHeader().setForeground(new Color(6, 95, 70));
        t.setSelectionBackground(new Color(209, 250, 229));
        t.setFillsViewportHeight(true);
    }

    private void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Succès", JOptionPane.INFORMATION_MESSAGE); }
    private void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE); }
    private void showInfo(String msg)    { JOptionPane.showMessageDialog(this, msg, "Info",   JOptionPane.INFORMATION_MESSAGE); }
}