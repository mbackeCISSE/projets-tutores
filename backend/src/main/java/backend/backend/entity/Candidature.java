package backend.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "candidature", uniqueConstraints = @UniqueConstraint(name = "uk_candidature_etudiant_sujet", columnNames = {"etudiant_id", "sujet_id"}))
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sujet_id", nullable = false)
    private Sujet sujet;

    @Column(nullable = false, columnDefinition = "text")
    private String motivation;

    @Column(nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.TIMESTAMP)
    private java.time.Instant dateCandidature;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false)
    private Statut statut;

    public Long getId() { return id; }
    public Utilisateur getEtudiant() { return etudiant; }
    public void setEtudiant(Utilisateur etudiant) { this.etudiant = etudiant; }
    public Sujet getSujet() { return sujet; }
    public void setSujet(Sujet sujet) { this.sujet = sujet; }
    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public java.time.Instant getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(java.time.Instant dateCandidature) { this.dateCandidature = dateCandidature; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
}
