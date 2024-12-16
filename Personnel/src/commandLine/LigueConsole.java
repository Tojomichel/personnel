package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import commandLineMenus.List;
import commandLineMenus.Menu;
import commandLineMenus.Option;

import personnel.*;

public class LigueConsole 
{
	private GestionPersonnel gestionPersonnel;
	private EmployeConsole employeConsole;

	public LigueConsole(GestionPersonnel gestionPersonnel, EmployeConsole employeConsole)
	{
		this.gestionPersonnel = gestionPersonnel;
		this.employeConsole = employeConsole;
	}

	Menu menuLigues()
	{
		Menu menu = new Menu("Gérer les ligues", "l");
		menu.add(afficherLigues());
		menu.add(ajouterLigue());
		menu.add(selectionnerLigue());
		menu.addBack("q");
		return menu;
	}

	private Option afficherLigues()
	{
		return new Option("Afficher les ligues", "l", () -> {System.out.println(gestionPersonnel.getLigues());});
	}

	private Option afficher(final Ligue ligue)
	{
		return new Option("Afficher la ligue", "l", 
				() -> 
				{
					System.out.println(ligue);
					System.out.println("administrée par " + ligue.getAdministrateur());
				}
		);
	}
	private Option afficherEmployes(final Ligue ligue)
	{
		return new Option("Afficher les employes", "l", () -> {System.out.println(ligue.getEmployes());});
	}

	private Option ajouterLigue()
	{
		return new Option("Ajouter une ligue", "a", () -> 
		{
			try
			{
				gestionPersonnel.addLigue(getString("nom : "));
			}
			catch(SauvegardeImpossible exception)
			{
				System.err.println("Impossible de sauvegarder cette ligue");
			}
		});
	}
	
	private Menu editerLigue(Ligue ligue)
	{
		Menu menu = new Menu("Editer " + ligue.getNom());
		menu.add(afficher(ligue));
		menu.add(gererEmployes(ligue));
		menu.add(changerAdministrateur(ligue));
		menu.add(changerNom(ligue));
		menu.add(supprimer(ligue));
		menu.addBack("q");
		return menu;
	}

	private Option changerNom(final Ligue ligue)
	{
		return new Option("Renommer", "r", 
				() -> {ligue.setNom(getString("Nouveau nom : "));});
	}

	private List<Ligue> selectionnerLigue()
	{
		return new List<Ligue>("Sélectionner une ligue", "e", 
				() -> new ArrayList<>(gestionPersonnel.getLigues()),
				(element) -> editerLigue(element)
				);
	}
	
	private Option ajouterEmploye(final Ligue ligue)
	{
	    return new Option("ajouter un employé", "a",
	        () -> {
	            String nom = getString("Nom : ");
	            String prenom = getString("Prénom : ");
	            String mail = getString("Mail : ");
	            String password = getString("Password : ");
	            
	            // Demande la date d'arrivée
	            LocalDate dateArrivee = null;
	            
	                try {
	                    String dateArriveeStr = getString("Date d'arrivée (format yyyy-MM-dd) : ");
	                    dateArrivee = LocalDate.parse(dateArriveeStr);
	                } catch (DateTimeParseException e) {
	                    System.out.println("Format de date invalide. Veuillez réessayer.");
	                }
	            
	            
	            // Demande la date de départ
	            LocalDate dateDepart = null;
	            
	                try {
	                    String dateDepartStr = getString("Date de départ (format yyyy-MM-dd) : ");
	                    dateDepart = LocalDate.parse(dateDepartStr);
	                } catch (DateTimeParseException e) {
	                    System.out.println("Format de date invalide. Veuillez réessayer.");
	                }
	            
	            
	            // Vérifiez si les dates sont cohérentes
	            try {
	                if (dateDepart.isBefore(dateArrivee)) {
	                    throw new DateIncoherente("La date de départ ne peut pas être avant la date d'arrivée.");
	                }
	                // Ajoute l'employé avec les dates
	                ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
	            } catch (DateIncoherente e) {
	                System.out.println("Attention! Erreur : " + e.getMessage());
	            }
	        }
	    );
	}
	
	 private Menu gererEmployes(Ligue ligue)
	    {
	        Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "e");
	        menu.add(afficherEmployes(ligue));
	        menu.add(ajouterEmploye(ligue));
	        menu.add(selectionnerEmploye(ligue));
	        menu.addBack("q");
	        return menu;
	    }

	    private List<Employe> selectionnerEmploye(final Ligue ligue)
	    {
	        return new List<>("Sélectionner un employé", "s", 
	            () -> new ArrayList<>(ligue.getEmployes()),
	            this::menuEmploye
	        );
	    }

	    private Menu menuEmploye(Employe employe)
	    {
	        Menu menu = new Menu("Gérer " + employe.getNom() + " " + employe.getPrenom(), "g");
	        menu.add(employeConsole.editerEmploye(employe));
	        menu.add(supprimerEmploye(employe)); 
	        menu.addBack("q");
	        return menu;
	    }

	    private Option supprimerEmploye(final Employe employe)
	    {
	        return new Option("Supprimer cet employé", "d", 
	            () -> { employe.remove(); });
	    }
	
		private List<Employe> changerAdministrateur(final Ligue ligue) {
			return new List<>("Changer d'administrateur", "c", () -> new ArrayList<>(ligue.getEmployes()),
					(index, element) -> {ligue.setAdministrateur(element); System.out.println(element + " est le nouvel administrateur");});}	

	private List<Employe> modifierEmploye(final Ligue ligue)
	{
		return new List<>("Modifier un employé", "e", 
				() -> new ArrayList<>(ligue.getEmployes()),
				employeConsole.editerEmploye()
				);
	}
	
	private Option supprimer(Ligue ligue)
	{
		return new Option("Supprimer", "d", () -> {ligue.remove();});
	}
	
}
