package com.event.model.entities;

import com.event.model.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Event evenement;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit être au moins 1")
    @Column(nullable = false)
    private Integer nombrePlaces;

    @Column(nullable = false)
    private Double montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus statut;

    @Column(nullable = false, unique = true, length = 20)
    private String codeReservation;

    @Column(length = 500)
    private String commentaire;

    // Constructors
    public Reservation() {
        this.dateReservation = LocalDateTime.now();
        this.statut = ReservationStatus.EN_ATTENTE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Event getEvenement() {
        return evenement;
    }

    public void setEvenement(Event evenement) {
        this.evenement = evenement;
    }

    public Integer getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(Integer nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public ReservationStatus getStatut() {
        return statut;
    }

    public void setStatut(ReservationStatus statut) {
        this.statut = statut;
    }

    public String getCodeReservation() {
        return codeReservation;
    }

    public void setCodeReservation(String codeReservation) {
        this.codeReservation = codeReservation;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    // Business methods
    public void calculateMontantTotal() {
        if (evenement != null && nombrePlaces != null) {
            this.montantTotal = evenement.getPrixUnitaire() * nombrePlaces;
        }
    }

    public boolean canBeCancelled() {
        if (statut.isCancelled()) {
            return false;
        }

        LocalDateTime eventStart = evenement.getDateDebut();
        LocalDateTime cancellationDeadline = eventStart.minusHours(48);

        return LocalDateTime.now().isBefore(cancellationDeadline);
    }

    @PrePersist
    protected void onCreate() {
        if (dateReservation == null) {
            dateReservation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = ReservationStatus.EN_ATTENTE;
        }
        calculateMontantTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateMontantTotal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(codeReservation, that.codeReservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codeReservation);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", codeReservation='" + codeReservation + '\'' +
                ", nombrePlaces=" + nombrePlaces +
                ", statut=" + statut +
                '}';
    }
}