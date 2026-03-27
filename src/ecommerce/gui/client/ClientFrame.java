package ecommerce.gui.client;

import ecommerce.gui.auth.LoginFrame;
import ecommerce.gui.components.AppTheme;
import ecommerce.Models.*;
import ecommerce.service.ServiceLocator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientFrame extends JFrame {

    private final ServiceLocator sl = ServiceLocator.getInstance();
    private final Utilisateur    client;

    private DefaultTableModel catalogueModel;
    private DefaultTableModel panierModel;
    private DefaultTableModel commandesModel;
    private JLabel            totalLabel;
    private JTextField        searchField;

    public ClientFrame(Utilisateur client) {
        super("E-Commerce — Espace Client");
        this.client = client;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
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
        bar.setBackground(AppTheme.PRIMARY);
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("E-Commerce");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel user = new JLabel("Bonjour, " + client.getPrenom() + " !");
        user.setFont(AppTheme.FONT_BODY);
        user.setForeground(new Color(199, 210, 254));

        JButton btnLogout = new JButton("Déconnexion");
        btnLogout.setFont(AppTheme.FONT_SMALL);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(AppTheme.PRIMARY_DARK);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { sl.authService.deconnecter(); dispose(); new LoginFrame(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(btnLogout);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BODY);
        tabs.addTab("  Catalogue  ",  buildCatalogueTab());
        tabs.addTab("  Mon panier  ", buildPanierTab());
        tabs.addTab("  Mes commandes  ", buildCommandesTab());
        tabs.addTab("  Mon profil  ", buildProfilTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) refreshPanier();
            if (tabs.getSelectedIndex() == 2) refreshCommandes();
        });
        return tabs;
    }

    // ── CATALOGUE ─────────────────────────────────────────────────────────────

    private JPanel buildCatalogueTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Barre de recherche
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchField = AppTheme.textField("Rechercher un produit...");
        searchField.setPreferredSize(new Dimension(280, 34));
        JButton btnSearch = AppTheme.btnPrimary("Rechercher");
        JButton btnAll    = AppTheme.btnSecondary("Tout afficher");
        JComboBox<String> catCombo = new JComboBox<>();
        catCombo.addItem("Toutes les catégories");
        sl.produitService.toutesLesCategories()
                .forEach(c -> catCombo.addItem(c.getNom()));

        searchBar.add(new JLabel("Recherche :"));
        searchBar.add(searchField);
        searchBar.add(btnSearch);
        searchBar.add(catCombo);
        searchBar.add(btnAll);

        // Table catalogue
        String[] cols = {"ID", "Produit", "Catégorie", "Prix (DT)", "Stock", "Note"};
        catalogueModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(catalogueModel);
        table.setFont(AppTheme.FONT_BODY);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        // Boutons actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JSpinner qteSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qteSpinner.setPreferredSize(new Dimension(60, 30));
        JButton btnAjouter   = AppTheme.btnSuccess("+ Ajouter au panier");
        JButton btnAvis      = AppTheme.btnSecondary("Voir les avis");
        actions.add(new JLabel("Qté :"));
        actions.add(qteSpinner);
        actions.add(btnAjouter);
        actions.add(btnAvis);

        // Events
        btnSearch.addActionListener(e -> {
            String kw = searchField.getText().trim();
            loadCatalogue(sl.produitService.rechercherParNom(kw));
        });
        btnAll.addActionListener(e -> loadCatalogue(sl.produitService.produitsEnStock()));
        catCombo.addActionListener(e -> {
            int idx = catCombo.getSelectedIndex();
            if (idx == 0) { loadCatalogue(sl.produitService.produitsEnStock()); return; }
            String catNom = (String) catCombo.getSelectedItem();
            sl.produitService.toutesLesCategories().stream()
                    .filter(c -> c.getNom().equals(catNom)).findFirst()
                    .ifPresent(c -> loadCatalogue(sl.produitService.parCategorie(c.getId())));
        });
        btnAjouter.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un produit."); return; }
            String id  = (String) catalogueModel.getValueAt(row, 0);
            int    qte = (int) qteSpinner.getValue();
            try {
                sl.commandeService.ajouterAuPanier(client.getId(), id, qte);
                showSuccess("Produit ajouté au panier !");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        btnAvis.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez un produit."); return; }
            String id = (String) catalogueModel.getValueAt(row, 0);
            showAvisDialog(id);
        });

        p.add(searchBar, BorderLayout.NORTH);
        p.add(scroll,    BorderLayout.CENTER);
        p.add(actions,   BorderLayout.SOUTH);

        loadCatalogue(sl.produitService.produitsEnStock());
        return p;
    }

    private void loadCatalogue(List<Produit> produits) {
        catalogueModel.setRowCount(0);
        List<Categorie> cats = sl.produitService.toutesLesCategories();
        for (Produit prod : produits) {
            String catNom = cats.stream()
                    .filter(c -> c.getId().equals(prod.getCategorieId()))
                    .map(Categorie::getNom).findFirst().orElse("-");
            catalogueModel.addRow(new Object[]{
                    prod.getId(), prod.getNom(), catNom,
                    String.format("%.3f", prod.getPrix()),
                    prod.getQuantiteStock(),
                    String.format("%.1f/5", prod.getNoteMoyenne())
            });
        }
    }

    // ── PANIER ────────────────────────────────────────────────────────────────

    private JPanel buildPanierTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Produit", "Prix unitaire", "Qté", "Sous-total"};
        panierModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(panierModel);
        styleTable(table);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        totalLabel = AppTheme.label("Total : 0.000 DT", new Font("Segoe UI", Font.BOLD, 16), AppTheme.PRIMARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnRetirer  = AppTheme.btnDanger("Retirer");
        JButton btnVider    = AppTheme.btnSecondary("Vider le panier");
        JButton btnCommande = AppTheme.btnPrimary("Passer la commande");

        btnPanel.add(btnRetirer);
        btnPanel.add(btnVider);
        btnPanel.add(btnCommande);
        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(btnPanel,   BorderLayout.EAST);

        btnRetirer.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez une ligne."); return; }
            String nom = (String) panierModel.getValueAt(row, 0);
            sl.produitService.produitsEnStock().stream()
                    .filter(pr -> pr.getNom().equals(nom)).findFirst()
                    .ifPresent(pr -> {
                        sl.commandeService.retirerDuPanier(client.getId(), pr.getId());
                        refreshPanier();
                    });
        });
        btnVider.addActionListener(e -> {
            sl.commandeService.viderPanier(client.getId());
            refreshPanier();
        });
        btnCommande.addActionListener(e -> showCheckoutDialog());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private void refreshPanier() {
        panierModel.setRowCount(0);
        Panier panier = sl.commandeService.getPanier(client.getId());
        for (LigneCommande l : panier.getLignes()) {
            panierModel.addRow(new Object[]{
                    l.getNomProduit(),
                    String.format("%.3f DT", l.getPrixUnitaire()),
                    l.getQuantite(),
                    String.format("%.3f DT", l.getSousTotal())
            });
        }
        totalLabel.setText(String.format("Total : %.3f DT", panier.getTotal()));
    }

    private void showCheckoutDialog() {
        Panier panier = sl.commandeService.getPanier(client.getId());
        if (panier.estVide()) { showError("Votre panier est vide."); return; }

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JTextField rue  = AppTheme.textField("Rue");
        JTextField ville= AppTheme.textField("Ville");
        JTextField cp   = AppTheme.textField("Code postal");
        JTextField pays = AppTheme.textField("Pays");
        pays.setText("Tunisie");
        JTextField promo = AppTheme.textField("Code promo (facultatif)");

        String[] modes = {"CARTE_BANCAIRE", "VIREMENT", "COUPON", "PAIEMENT_A_LA_LIVRAISON"};
        JComboBox<String> modePaiement = new JComboBox<>(modes);

        panel.add(new JLabel("Adresse de livraison :")); panel.add(rue);
        panel.add(ville); panel.add(cp); panel.add(pays);
        panel.add(new JLabel("Code promo :")); panel.add(promo);
        panel.add(new JLabel("Mode de paiement :")); panel.add(modePaiement);

        int res = JOptionPane.showConfirmDialog(this, panel,
                "Finaliser la commande — Total : " + String.format("%.3f DT", panier.getTotal()),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) return;
        if (rue.getText().isBlank() || ville.getText().isBlank()) {
            showError("Adresse incomplète."); return;
        }

        try {
            Adresse adr = new Adresse(rue.getText(), ville.getText(), cp.getText(), pays.getText());
            Commande cmd = sl.commandeService.passerCommande(
                    client.getId(), adr, promo.getText().trim());
            ModePaiement mode = ModePaiement.valueOf((String) modePaiement.getSelectedItem());
            Paiement pay = sl.paiementService.effectuerPaiement(cmd.getId(), mode);
            showSuccess("Commande " + cmd.getNumeroCommande() + " confirmée !\nRef. paiement : " + pay.getReference());
            refreshPanier();
            refreshCommandes();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    // ── COMMANDES ─────────────────────────────────────────────────────────────

    private JPanel buildCommandesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"N° Commande", "Date", "Montant (DT)", "Statut", "Adresse livraison"};
        commandesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(commandesModel);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnRefresh  = AppTheme.btnSecondary("Actualiser");
        JButton btnAnnuler  = AppTheme.btnDanger("Annuler la commande");
        JButton btnAvis     = AppTheme.btnSecondary("Laisser un avis");
        btnPanel.add(btnRefresh);
        btnPanel.add(btnAnnuler);
        btnPanel.add(btnAvis);

        btnRefresh.addActionListener(e -> refreshCommandes());
        btnAnnuler.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("Sélectionnez une commande."); return; }
            String num = (String) commandesModel.getValueAt(row, 0);
            List<Commande> cmds = sl.commandeService.historiqueClient(client.getId());
            cmds.stream().filter(c -> c.getNumeroCommande().equals(num)).findFirst().ifPresent(c -> {
                try {
                    sl.commandeService.annulerCommande(c.getId(), client.getId());
                    showSuccess("Commande annulée.");
                    refreshCommandes();
                } catch (Exception ex) { showError(ex.getMessage()); }
            });
        });
        btnAvis.addActionListener(e -> showLaisserAvisDialog());

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        refreshCommandes();
        return p;
    }

    private void refreshCommandes() {
        commandesModel.setRowCount(0);
        for (Commande c : sl.commandeService.historiqueClient(client.getId())) {
            String adr = c.getAdresseLivraison() != null ? c.getAdresseLivraison().getVille() : "-";
            commandesModel.addRow(new Object[]{
                    c.getNumeroCommande(),
                    c.getDateCommande().toLocalDate(),
                    String.format("%.3f", c.getMontantAPayer()),
                    c.getStatut().getLibelle(),
                    adr
            });
        }
    }

    // ── PROFIL ────────────────────────────────────────────────────────────────

    private JPanel buildProfilTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppTheme.BG);
        p.setBorder(new EmptyBorder(24, 40, 24, 40));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridx = 0; gc.weightx = 1;

        JTextField fNom    = AppTheme.textField("Nom");    fNom.setText(client.getNom());
        JTextField fPrenom = AppTheme.textField("Prénom"); fPrenom.setText(client.getPrenom());
        JTextField fEmail  = AppTheme.textField("Email");  fEmail.setText(client.getEmail());
        JTextField fTel    = AppTheme.textField("Tél.");   fTel.setText(client.getTelephone());
        JPasswordField fOld  = AppTheme.passwordField();
        JPasswordField fNew  = AppTheme.passwordField();

        Object[][] rows = {
                {"Nom", fNom}, {"Prénom", fPrenom}, {"Email", fEmail}, {"Téléphone", fTel},
                {"Ancien mot de passe", fOld}, {"Nouveau mot de passe", fNew}
        };
        for (int i = 0; i < rows.length; i++) {
            gc.gridy = i * 2;
            JLabel l = new JLabel((String) rows[i][0]);
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setForeground(AppTheme.TEXT_SECONDARY);
            p.add(l, gc);
            gc.gridy = i * 2 + 1;
            p.add((Component) rows[i][1], gc);
        }

        JButton btnSave = AppTheme.btnPrimary("Enregistrer les modifications");
        gc.gridy = rows.length * 2; gc.insets = new Insets(20, 0, 0, 0);
        p.add(btnSave, gc);

        btnSave.addActionListener(e -> {
            try {
                String mdp = new String(fOld.getPassword());
                String nvmdp = new String(fNew.getPassword());
                if (!mdp.isEmpty() && !nvmdp.isEmpty()) {
                    sl.authService.changerMotDePasse(mdp, nvmdp);
                }
                showSuccess("Profil mis à jour !");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        return p;
    }

    // ── Avis dialogs ──────────────────────────────────────────────────────────

    private void showAvisDialog(String produitId) {
        List<ecommerce.model.AvisClient> avis = sl.avisService.avisParProduit(produitId);
        StringBuilder sb = new StringBuilder();
        if (avis.isEmpty()) sb.append("Aucun avis pour ce produit.");
        else avis.forEach(a -> sb.append(a.getNomClient())
                .append(" — ").append(a.getEtoiles())
                .append("\n").append(a.getCommentaire()).append("\n\n"));
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(AppTheme.FONT_BODY); ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Avis clients", JOptionPane.PLAIN_MESSAGE);
    }

    private void showLaisserAvisDialog() {
        JTextField produitId = AppTheme.textField("ID produit");
        JSpinner note = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        JTextArea commentaire = new JTextArea(3, 20);
        commentaire.setFont(AppTheme.FONT_BODY);

        JPanel pan = new JPanel(new GridLayout(0, 1, 0, 6));
        pan.add(new JLabel("ID du produit :")); pan.add(produitId);
        pan.add(new JLabel("Note (1-5) :")); pan.add(note);
        pan.add(new JLabel("Commentaire :")); pan.add(new JScrollPane(commentaire));

        if (JOptionPane.showConfirmDialog(this, pan, "Laisser un avis",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                sl.avisService.laisserUnAvis(produitId.getText().trim(), client.getId(),
                        client.getNomComplet(), (int) note.getValue(), commentaire.getText());
                showSuccess("Avis publié !");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    // ── Utilitaires UI ────────────────────────────────────────────────────────

    private void styleTable(JTable t) {
        t.setFont(AppTheme.FONT_BODY);
        t.setRowHeight(28);
        t.setGridColor(AppTheme.BORDER_COLOR);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(AppTheme.PRIMARY_LIGHT);
        t.getTableHeader().setForeground(AppTheme.PRIMARY_DARK);
        t.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        t.setSelectionForeground(AppTheme.PRIMARY_DARK);
        t.setFillsViewportHeight(true);
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