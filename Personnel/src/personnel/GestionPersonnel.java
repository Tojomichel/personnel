package personnel;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Gestion du personnel. Un seul objet de cette classe existe.
 * Il n'est pas possible d'instancier directement cette classe,
 * la méthode {@link #getGestionPersonnel getGestionPersonnel}
 * le fait automatiquement et retourne toujours le même objet.
 * Dans le cas où {@link #sauvegarder()} a été appelé lors
 * d'une exécution précédente, c'est l'objet sauvegardé qui est
 * retourné.
 */
public void update(Ligue ligue)throws SauvegardeImpossible{passerelle.update(ligue);}

public class GestionPersonnel implements Serializable {
	private static final long serialVersionUID = -105283113987886425L;
	private static GestionPersonnel gestionPersonnel = null;
	private SortedSet<Ligue> ligues;
	private Employe root = new Employe(this, null, "root", "", "", "toor", null, null);
	public final static int SERIALIZATION = 1, JDBC = 2,
			TYPE_PASSERELLE = JDBC;
	private static Passerelle passerelle = TYPE_PASSERELLE == JDBC ? new jdbc.JDBC()
			: new serialisation.Serialization();

	public void addRoot(String nom, String motDePasse) {
		root = new Employe(this, null, nom, "", "", motDePasse, null, null);
	}

	/**
	 * Retourne l'unique instance de cette classe.
	 * Crée cet objet s'il n'existe déjà.
	 * @return l'unique objet de type {@link GestionPersonnel}.
	 */
	
	 public static GestionPersonnel getGestionPersonnel() {
		if (gestionPersonnel == null) {
			gestionPersonnel = passerelle.getGestionPersonnel();

			// Vérifier que des ligues et employés sont bien récupérés
			if (gestionPersonnel.getLigues().isEmpty()) {
				System.out.println("Aucune ligue récupérée depuis la base.");
			} else {
				System.out.println("Ligues récupérées : " + gestionPersonnel.getLigues().size());
				for (Ligue ligue : gestionPersonnel.getLigues()) {
					System.out.println(
							"Ligue : " + ligue.getNom() + ", Nombre d'employés : " + ligue.getEmployes().size());
				}
			}

	public GestionPersonnel() {
		if (gestionPersonnel != null)
			throw new RuntimeException("Vous ne pouvez créer qu'une seuls instance de cet objet.");
		ligues = new TreeSet<>();
		gestionPersonnel = this;
	}

	public void sauvegarder() throws SauvegardeImpossible {
		passerelle.sauvegarderGestionPersonnel(this);
	}

	/**
	 * Retourne la ligue dont administrateur est l'administrateur,
	 * null s'il n'est pas un administrateur.
	 * 
	 * @param administrateur l'administrateur de la ligue recherchée.
	 * @return la ligue dont administrateur est l'administrateur.
	 */

	public void update(Employe employe) throws SauvegardeImpossible {
		passerelle.update(employe);
	}

	public void remove(Employe employe) throws SauvegardeImpossible {
		passerelle.delete(employe);
	}

	public Ligue getLigue(Employe administrateur) {
		if (administrateur.estAdmin(administrateur.getLigue()))
			return administrateur.getLigue();
		else
			return null;
	}

	/**
	 * Retourne toutes les ligues enregistrées.
	 * 
	 * @return toutes les ligues enregistrées.
	 */

	public SortedSet<Ligue> getLigues() {
		return Collections.unmodifiableSortedSet(ligues);
	}

	public Ligue addLigue(String nom) throws SauvegardeImpossible {
		Ligue ligue = new Ligue(this, nom);
		ligues.add(ligue);
		return ligue;
	}

	public Ligue addLigue(int id, String nom) {
		Ligue ligue = new Ligue(this, id, nom);
		ligues.add(ligue);
		return ligue;
	}

	void remove(Ligue ligue) {
		ligues.remove(ligue);
	}

	int insert(Ligue ligue) throws SauvegardeImpossible {
		return passerelle.insert(ligue);
	}

	int insert(Employe employe) throws SauvegardeImpossible {
		return passerelle.insert(employe);
	}

	public void removeEmploye(Employe employe) {
		try {
			passerelle.deleteEmploye(employe);
		} catch (SauvegardeImpossible e) {
			System.out.println("Erreur lors de la suppression de l'employé : " + e.getMessage());
		}
	}

	/**
	 * Retourne le root (super-utilisateur).
	 * 
	 * @return le root.
	 */

	public Employe getRoot() {
		return root;
	}
}
