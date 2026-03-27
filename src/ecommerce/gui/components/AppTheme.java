package ecommerce.gui.components;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AppTheme {

    // ── Couleurs ──────────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(79, 70, 229);   // indigo
    public static final Color PRIMARY_DARK   = new Color(55, 48, 163);
    public static final Color PRIMARY_LIGHT  = new Color(224, 231, 255);
    public static final Color SUCCESS        = new Color(22, 163, 74);
    public static final Color DANGER         = new Color(220, 38, 38);
    public static final Color WARNING        = new Color(217, 119, 6);
    public static final Color BG             = new Color(249, 250, 251);
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color TEXT_PRIMARY   = new Color(17, 24, 39);
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    public static final Color BORDER_COLOR   = new Color(229, 231, 235);
    public static final Color TABLE_STRIPE   = new Color(249, 250, 251);

    // ── Polices ───────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,   22);
    public static final Font FONT_H2      = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN,  12);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD,   12);

    // ── Espacements ───────────────────────────────────────────────────────────
    public static final Border PADDING_SM  = new EmptyBorder(4,  8,  4,  8);
    public static final Border PADDING_MD  = new EmptyBorder(8,  16, 8,  16);
    public static final Border PADDING_LG  = new EmptyBorder(16, 24, 16, 24);

    private AppTheme() {}

    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",           BG);
        UIManager.put("OptionPane.background",       BG_CARD);
        UIManager.put("Table.font",                  FONT_BODY);
        UIManager.put("Table.rowHeight",             28);
        UIManager.put("Table.gridColor",             BORDER_COLOR);
        UIManager.put("TableHeader.font",            new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("TableHeader.background",      PRIMARY_LIGHT);
        UIManager.put("TableHeader.foreground",      PRIMARY_DARK);
        UIManager.put("TextField.font",              FONT_BODY);
        UIManager.put("PasswordField.font",          FONT_BODY);
        UIManager.put("ComboBox.font",               FONT_BODY);
        UIManager.put("Label.font",                  FONT_BODY);
        UIManager.put("Button.font",                 FONT_BUTTON);
        UIManager.put("TabbedPane.font",             FONT_BODY);
        UIManager.put("ScrollPane.border",           BorderFactory.createLineBorder(BORDER_COLOR));
    }

    // ── Composants pré-stylés ─────────────────────────────────────────────────

    public static JButton btnPrimary(String texte) {
        JButton b = new JButton(texte);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        return b;
    }

    public static JButton btnDanger(String texte) {
        JButton b = btnPrimary(texte);
        b.setBackground(DANGER);
        return b;
    }

    public static JButton btnSuccess(String texte) {
        JButton b = btnPrimary(texte);
        b.setBackground(SUCCESS);
        return b;
    }

    public static JButton btnSecondary(String texte) {
        JButton b = new JButton(texte);
        b.setBackground(BG_CARD);
        b.setForeground(TEXT_PRIMARY);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(7, 17, 7, 17)
        ));
        return b;
    }

    public static JTextField textField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setToolTipText(placeholder);
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    public static JLabel label(String texte, Font font, Color color) {
        JLabel l = new JLabel(texte);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 20, 16, 20)
        ));
        return p;
    }

    public static JPanel statCard(String titre, String valeur, Color couleur) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, couleur),
                new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel lTitre = new JLabel(titre);
        lTitre.setFont(FONT_SMALL);
        lTitre.setForeground(TEXT_SECONDARY);
        JLabel lVal = new JLabel(valeur);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lVal.setForeground(couleur);
        p.add(lTitre, BorderLayout.NORTH);
        p.add(lVal,   BorderLayout.CENTER);
        return p;
    }
}