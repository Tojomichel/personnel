package personnel;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Employé d'une ligue hébergée par la M2L. Certains peuvent être
 * administrateurs des employés de leur ligue. Un seul employé, rattaché à
 * aucune ligue, est le root. Il est impossible d'instancier directement un
 * employé, il faut passer la méthode {@link Ligue#addEmploye addEmploye}.
 */
public class Employe implements Serializable, Comparable<Employe> {

    private static final long serialVersionUID = 4795721718037994734L;
    private String nom, prenom, password, mail;
    private Ligue ligue;
    private int id;
    private GestionPersonnel gestionPersonnel;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;

    Employe(GestionPersonnel gestionPersonnel, Ligue ligue, int id, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) {
        this.gestionPersonnel = gestionPersonnel;
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.mail = mail;
        this.ligue = ligue;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
    }
    
    /**
     * Constructeur pour créer un employé à partir des données de la base MySQL
     */
    Employe(GestionPersonnel gestionPersonnel, int id, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart, Ligue ligue) {
        this(gestionPersonnel, ligue, id, nom, prenom, mail, password, dateArrivee, dateDepart);
    }

    Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) throws SauvegardeImpossible {
        this(gestionPersonnel, ligue, -1, nom, prenom, mail, password, dateArrivee, dateDepart);
        this.id = gestionPersonnel.insert(this);
    }

    /**
     * Retourne vrai ssi l'employé est administrateur de la ligue passée en
     * paramètre.
     *
     * @return vrai ssi l'employé est administrateur de la ligue passée en
     * paramètre.
     * @param ligue la ligue pour laquelle on souhaite vérifier si this est
     * l'admininstrateur.
     */
    public boolean estAdmin(Ligue ligue) {
        return ligue.getAdministrateur() == this;
    }

    /**
     * Retourne vrai ssi l'employé est le root.
     *
     * @return vrai ssi l'employé est le root.
     */
    public boolean estRoot() {
        return gestionPersonnel.getRoot() == this;
    }

    /**
     * Retourne l'ID de l'employé.
     *
     * @return l'ID de l'employé.
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne le nom de l'employé.
     *
     * @return le nom de l'employé.
     */
    public String getNom() {
        return nom;
    }

    /**
     * Change le nom de l'employé.
     *
     * @param nom le nouveau nom.
     */
    public void setNom(String nom) {
        this.nom = nom;
        try {
            gestionPersonnel.update(this);
        } catch (SauvegardeImpossible e) {
            System.err.println("Impossible de sauvegarder la modification : " + e.getMessage());
        }
    }

    /**
     * Retourne le prénom de l'employé.
     *
     * @return le prénom de l'employé.
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * Change le prénom de l'employé.
     *
     * @param prenom le nouveau prénom de l'employé.
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
        try {
            gestionPersonnel.update(this);
        } catch (SauvegardeImpossible e) {
            System.err.println("Impossible de sauvegarder la modification : " + e.getMessage());
        }
    }

    /**
     * Retourne le mail de l'employé.
     *
     * @return le mail de l'employé.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Change le mail de l'employé.
     *
     * @param mail le nouveau mail de l'employé.
     */
    public void setMail(String mail) {
        this.mail = mail;
        try {
            gestionPersonnel.update(this);
        } catch (SauvegardeImpossible e) {
            System.err.println("Impossible de sauvegarder la modification : " + e.getMessage());
        }
    }

    /**
     * Retourne vrai ssi le password passé en paramètre est bien celui de
     * l'employé.
     *
     * @return vrai ssi le password passé en paramètre est bien celui de
     * l'employé.
     * @param password le password auquel comparer celui de l'employé.
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * Change le password de l'employé.
     *
     * @param password le nouveau password de l'employé.
     */
    public void setPassword(String password) {
        this.password = password;
        try {
            gestionPersonnel.update(this);
        } catch (SauvegardeImpossible e) {
            System.err.println("Impossible de sauvegarder la modification : " + e.getMessage());
        }
    }
    
	public String getPassword()
	{
		return password;
	}

    /**
     * Retourne la ligue à laquelle l'employé est affecté.
     *
     * @return la ligue à laquelle l'employé est affecté.
     */
    public Ligue getLigue() {
        return ligue;
    }

    /**
     * Supprime l'employé. Si celui-ci est un administrateur, le root récupère
     * les droits d'administration sur sa ligue.
     */
    public void setDateArrivee(LocalDate dateArrivee) {
        this.dateArrivee = dateArrivee;
        try {
            gestionPersonnel.update(this);
        } catch (SauvegardeImpossible e) {
            System.err.println("Impossible de sauvegarder la modification : " + e.getMessage());
        }
    } // permet d'assigner une valeur de type date pour la date d'arrivée

    public LocalDate getDateArrivee() {
        return dateArrivee;
    } // permet d'obtenir la date d'arrivée

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
        try {
            gestionPersonnel.update(this);
        } catch (SauvegardeImpossible e) {
            System.err.println("Impossible de sauvegarder la modification : " + e.getMessage());
        }
    } // permet d'assigner une valeur de type date pour la date de départ

    public LocalDate getDateDepart() {
        return dateDepart;
    } // permet d'obtenir la date de départ

    public void remove() {
        Employe root = gestionPersonnel.getRoot();
        if (this != root) {
            if (estAdmin(getLigue())) {
                getLigue().setAdministrateur(root);
            }
            try {
                gestionPersonnel.delete(this);
                getLigue().remove(this);
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de supprimer l'employé : " + e.getMessage());
            }
        } else {
            throw new ImpossibleDeSupprimerRoot();
        }
    }

    @Override
    public int compareTo(Employe autre) {
        int cmp = getNom().compareTo(autre.getNom());
        if (cmp != 0) {
            return cmp;
        }
        return getPrenom().compareTo(autre.getPrenom());
    }

    @Override
    public String toString() {
        String res = nom + " " + prenom + " " + mail;
        res += " Date d'arrivée: " + (dateArrivee != null ? dateArrivee.toString() : "Non renseignée");
        res += " Date de départ: " + (dateDepart != null ? dateDepart.toString() : "Non renseignée") + ", ";
        if (estRoot()) {
            res += "super-utilisateur";
        } else {
        	res += " (" + (ligue != null ? ligue.toString() : "Aucune ligue");
        }
        return res + ")";
    }
}
