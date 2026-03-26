package ecommerce.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Paiement {
    private String id;
    private String commandeId;
    private double montant;
    private ModePaiement mode;
    private boolean valide;
    private LocalDateTime datePaiement;
    private String reference;

    public Paiement() {
        this.id = UUID.randomUUID().toString();
        this.datePaiement = LocalDateTime.now();
        this.reference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Paiement(String commandeId, double montant, ModePaiement mode) {
        this();
        this.commandeId = commandeId;
        this.montant = montant;
        this.mode = mode;
        this.valide = true;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCommandeId() { return commandeId; }
    public void setCommandeId(String commandeId) { this.commandeId = commandeId; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public ModePaiement getMode() { return mode; }
    public void setMode(ModePaiement mode) { this.mode = mode; }
    public boolean isValide() { return valide; }
    public void setValide(boolean valide) { this.valide = valide; }
    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}