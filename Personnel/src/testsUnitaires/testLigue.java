package testsUnitaires;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import personnel.*;

class testLigue {
	GestionPersonnel gestionPersonnel = GestionPersonnel.getGestionPersonnel();
//
	@Test
	void createLigue() throws SauvegardeImpossible {
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		assertEquals("Fléchettes", ligue.getNom());
	}

	@Test
	void addEmploye() throws SauvegardeImpossible, DateIncoherente {
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty",
				LocalDate.of(2023, 12, 01), LocalDate.of(2024, 12, 01));
		assertEquals(employe, ligue.getEmployes().first());
	}


	@Test // test choisir employe comme admin
	void setAdmin() throws SauvegardeImpossible , DateIncoherente
	{
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty", LocalDate.of(2023, 12, 01), LocalDate.of(2024, 12, 01));
		
		ligue.setAdministrateur(employe); assertEquals(employe, ligue.getAdministrateur());
	}

	@Test // test lorsqu'on supprimer un employé (ne devrait plus être présent après le
			// remove() donc assertFalse)
	void removeEmploye() throws SauvegardeImpossible, DateIncoherente {
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty",
				LocalDate.of(2023, 12, 01), LocalDate.of(2024, 12, 01));

		assertTrue(ligue.getEmployes().contains(employe));

		ligue.remove(employe);

		assertFalse(ligue.getEmployes().contains(employe));
	}

	@Test // test pour supprimer une ligue
	void removeLigue() throws SauvegardeImpossible, DateIncoherente {
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		Employe employe = ligue.addEmploye("Bouchard", "Gérard", "g.bouchard@gmail.com", "azerty", LocalDate.of(2023, 12, 01), LocalDate.of(2024, 12, 01));
		
		assertTrue(ligue.getEmployes().contains(employe));
		
		ligue.remove();
		
		assertFalse(gestionPersonnel.getLigues().contains(ligue));
	}
	
	@Test // changement d'admin
	void changeAdmin() throws SauvegardeImpossible, DateIncoherente {
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		Employe employeJafar = ligue.addEmploye("Jafar", "Jafar", "jafar@gmail.com", null, LocalDate.of(2022, 01, 20), LocalDate.of(2023, 10, 20));
		
		ligue.setAdministrateur(employeJafar); assertEquals(employeJafar, ligue.getAdministrateur());
		
		Employe employeAngelin = ligue.addEmploye("Angelin", "Angelin", "angelin@gmail.com", null, LocalDate.of(2024, 01, 01), LocalDate.of(2024, 03, 01));
		
		ligue.setAdministrateur(employeAngelin); assertEquals(employeAngelin, ligue.getAdministrateur());
	}
	
	@Test // suppression d'un admin dans la ligue
	void removeAdmin() throws SauvegardeImpossible, DateIncoherente {
		Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
		Employe employeJafar = ligue.addEmploye("Jafar", "Jafar", "jafar@gmail.com", null, LocalDate.of(2022, 01, 20), LocalDate.of(2023, 10, 20));
		
		ligue.setAdministrateur(employeJafar); assertEquals(employeJafar, ligue.getAdministrateur());
		
		Employe employeAngelin = ligue.addEmploye("Angelin", "Angelin", "angelin@gmail.com", null, LocalDate.of(2024, 01, 01), LocalDate.of(2024, 03, 01));
		
		ligue.setAdministrateur(employeAngelin); assertEquals(employeAngelin, ligue.getAdministrateur());
		
		ligue.remove(employeJafar);
		
		assertNotEquals(employeJafar, ligue.getAdministrateur());
		
		assertFalse(ligue.getEmployes().contains(employeJafar));
	}

	@Test // test pour vérifier les dates incohérentes
	void testDate() throws SauvegardeImpossible{
	   Ligue ligue = gestionPersonnel.addLigue("Fléchettes");
	   assertThrows(DateIncoherente.class, () -> {
		   ligue.addEmploye("Tojo", "Tojo", "tojo@gmail.com", "password046", LocalDate.of(2024, 12, 01), LocalDate.of(2022, 12, 01));
	   });
	}
}
