package ecommerce.gui.auth;

import ecommerce.gui.admin.AdminFrame;
import ecommerce.gui.client.ClientFrame;
import ecommerce.gui.components.AppTheme;
import ecommerce.gui.vendeur.VendeurFrame;
import ecommerce.model.Role;
import ecommerce.model.Utilisateur;
import ecommerce.service.ServiceLocator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final ServiceLocator sl = ServiceLocator.getInstance();

    private JTabbedPane tabs;

    // Onglet connexion
    private JTextField    loginEmail;
    private JPasswordField loginPass;

    // Onglet inscription
    private JTextField regNom, regPrenom, regEmail, regTel;
    private JPasswordField regPass, regPassConf;

    public LoginFrame() {
        super("E-Commerce — Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BG);
        build();
        setVisible(true);
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppTheme.BG);

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.PRIMARY);
        header.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel logo = new JLabel("E-Commerce");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Votre boutique en ligne");
        sub.setFont(AppTheme.FONT_SMALL);
        sub.setForeground(new Color(199, 210, 254));

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(logo);
        headerText.add(sub);
        header.add(headerText, BorderLayout.CENTER);

        // ── Tabs ─────────────────────────────────────────────────────────────
        tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BODY);
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));
        tabs.addTab("  Connexion  ", buildLoginPanel());
        tabs.addTab("  Inscription  ", buildRegisterPanel());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(AppTheme.BG);
        center.setBorder(new EmptyBorder(0, 32, 32, 32));
        center.add(tabs, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridx = 0; gc.weightx = 1;

        loginEmail = AppTheme.textField("Email");
        loginPass  = AppTheme.passwordField();

        gc.gridy = 0; p.add(labelFor("Adresse email"), gc);
        gc.gridy = 1; p.add(loginEmail, gc);
        gc.gridy = 2; p.add(labelFor("Mot de passe"), gc);
        gc.gridy = 3; p.add(loginPass,  gc);

        JButton btnLogin = AppTheme.btnPrimary("Se connecter");
        btnLogin.setPreferredSize(new Dimension(0, 40));
        gc.gridy = 4; gc.insets = new Insets(18, 0, 0, 0);
        p.add(btnLogin, gc);

        btnLogin.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> doLogin());
        return p;
    }

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(new EmptyBorder(16, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);
        gc.gridx = 0; gc.weightx = 1;

        regNom      = AppTheme.textField("Nom");
        regPrenom   = AppTheme.textField("Prénom");
        regEmail    = AppTheme.textField("Email");
        regTel      = AppTheme.textField("Téléphone");
        regPass     = AppTheme.passwordField();
        regPassConf = AppTheme.passwordField();

        String[][] fields = {
                {"Nom *", null}, {"Prénom *", null},
                {"Email *", null}, {"Téléphone", null},
                {"Mot de passe *", null}, {"Confirmer le mot de passe *", null}
        };
        Component[] inputs = { regNom, regPrenom, regEmail, regTel, regPass, regPassConf };

        for (int i = 0; i < inputs.length; i++) {
            gc.gridy = i * 2;     p.add(labelFor(fields[i][0]), gc);
            gc.gridy = i * 2 + 1; p.add(inputs[i], gc);
        }

        JButton btnReg = AppTheme.btnSuccess("Créer mon compte");
        btnReg.setPreferredSize(new Dimension(0, 40));
        gc.gridy = inputs.length * 2;
        gc.insets = new Insets(14, 0, 0, 0);
        p.add(btnReg, gc);
        btnReg.addActionListener(e -> doRegister());
        return p;
    }

    private void doLogin() {
        String email = loginEmail.getText().trim();
        String pass  = new String(loginPass.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            showError("Veuillez remplir tous les champs."); return;
        }
        try {
            Utilisateur u = sl.authService.connecter(email, pass);
            dispose();
            if      (u.getRole() == Role.ADMIN)   new AdminFrame(u);
            else if (u.getRole() == Role.VENDEUR)  new VendeurFrame(u);
            else                                    new ClientFrame(u);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void doRegister() {
        String nom   = regNom.getText().trim();
        String prenom= regPrenom.getText().trim();
        String email = regEmail.getText().trim();
        String tel   = regTel.getText().trim();
        String pass  = new String(regPass.getPassword());
        String conf  = new String(regPassConf.getPassword());

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("Les champs marqués * sont obligatoires."); return;
        }
        if (!pass.equals(conf)) {
            showError("Les mots de passe ne correspondent pas."); return;
        }
        try {
            sl.authService.inscrire(nom, prenom, email, pass, tel);
            JOptionPane.showMessageDialog(this,
                    "Compte créé avec succès ! Vous pouvez vous connecter.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            tabs.setSelectedIndex(0);
            loginEmail.setText(email);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private JLabel labelFor(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        return l;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}