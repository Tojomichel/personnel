package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

 // import personnel.GestionPersonnel;
import personnel.*;
import personnel.Ligue; // Ajout de l'import pour Credentials
import personnel.Passerelle;
import personnel.SauvegardeImpossible;
import personnel.Employe;

public class JDBC implements Passerelle 
{
	Connection connection;

	public JDBC()
	{
		try
		{
			Class.forName(Credentials.getDriverClassName());
			connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Pilote JDBC non installé.");
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}
	
	@Override
	public GestionPersonnel getGestionPersonnel() 
	{
		GestionPersonnel gestionPersonnel = new GestionPersonnel();
		try 
		{
			// Lecture du root dans la base de données
						String requeteRoot = "SELECT * FROM utilisateur WHERE admin = 1 LIMIT 1";
						Statement instructionRoot = connection.createStatement();
						ResultSet resultRoot = instructionRoot.executeQuery(requeteRoot);
						if (resultRoot.next()) {
							LocalDate dateArrivee = resultRoot.getDate("date_arrivee") != null ? 
								resultRoot.getDate("date_arrivee").toLocalDate() : null;
							LocalDate dateDepart = resultRoot.getDate("date_depart") != null ? 
								resultRoot.getDate("date_depart").toLocalDate() : null;
								
							gestionPersonnel.addRoot(
								resultRoot.getInt("idUtilisateur"),
								resultRoot.getString("nomUtil"),
								resultRoot.getString("prenomUtil"),
								resultRoot.getString("mailUtil"),
								resultRoot.getString("passwordUtil"),
								dateArrivee,
								dateDepart
							);
						}

						// Lecture des ligues
						String requeteLigues = "SELECT * FROM ligue";
						Statement instructionLigues = connection.createStatement();
						ResultSet resultLigues = instructionLigues.executeQuery(requeteLigues);
						while (resultLigues.next()) {
							int idLigue = resultLigues.getInt("numLigue");
							String nomLigue = resultLigues.getString("nomLigue");
							Ligue ligue = gestionPersonnel.addLigue(idLigue, nomLigue);

							// Lecture de l'administrateur de la ligue
							String requeteAdmin = "SELECT * FROM utilisateur WHERE numLigue = ? AND admin = 1";
							PreparedStatement instructionAdmin = connection.prepareStatement(requeteAdmin);
							instructionAdmin.setInt(1, idLigue);
							ResultSet resultAdmin = instructionAdmin.executeQuery();
							
							if (resultAdmin.next()) {
								LocalDate dateArriveeAdmin = resultAdmin.getDate("date_arrivee") != null ? 
									resultAdmin.getDate("date_arrivee").toLocalDate() : null;
								LocalDate dateDepartAdmin = resultAdmin.getDate("date_depart") != null ? 
									resultAdmin.getDate("date_depart").toLocalDate() : null;

								Employe admin = ligue.addEmploye(
									resultAdmin.getInt("idUtilisateur"),
									resultAdmin.getString("nomUtil"),
									resultAdmin.getString("prenomUtil"),
									resultAdmin.getString("mailUtil"),
									resultAdmin.getString("passwordUtil"),
									dateArriveeAdmin,
									dateDepartAdmin
								);
								ligue.setAdministrateur(admin);
							}

							// Lecture des employés non administrateurs de la ligue
							String requeteEmployes = "SELECT * FROM utilisateur WHERE numLigue = ? AND admin = 0";
							PreparedStatement instructionEmployes = connection.prepareStatement(requeteEmployes);
							instructionEmployes.setInt(1, idLigue);
							ResultSet resultEmployes = instructionEmployes.executeQuery();
							
							while (resultEmployes.next()) {
								LocalDate dateArriveeEmp = resultEmployes.getDate("date_arrivee") != null ? 
									resultEmployes.getDate("date_arrivee").toLocalDate() : null;
								LocalDate dateDepartEmp = resultEmployes.getDate("date_depart") != null ? 
									resultEmployes.getDate("date_depart").toLocalDate() : null;

								ligue.addEmploye(
									resultEmployes.getInt("idUtilisateur"),
									resultEmployes.getString("nomUtil"),
									resultEmployes.getString("prenomUtil"),
									resultEmployes.getString("mailUtil"),
									resultEmployes.getString("passwordUtil"),
									dateArriveeEmp,
									dateDepartEmp
								);
							}
						}
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		return gestionPersonnel;
	}

	@Override
	public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible 
	{
		close();
	}
	
	public void close() throws SauvegardeImpossible
	{
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (SQLException e)
		{
			throw new SauvegardeImpossible(e);
		}
	}
	
	@Override
	public int insert(Ligue ligue) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction;
			instruction = connection.prepareStatement("insert into ligue (nomLigue) values(?)", Statement.RETURN_GENERATED_KEYS);
			instruction.setString(1, ligue.getNom());		
			instruction.executeUpdate();
			ResultSet id = instruction.getGeneratedKeys();
			id.next();
			return id.getInt(1);
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}		
	}

	@Override
	public int insert(Employe employe) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction = connection.prepareStatement(
				"INSERT INTO utilisateur (nomUtil, prenomUtil, mailUtil, passwordUtil, date_arrivee, date_depart, numLigue, admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
				Statement.RETURN_GENERATED_KEYS);
			instruction.setString(1, employe.getNom());
			instruction.setString(2, employe.getPrenom());
			instruction.setString(3, employe.getMail());
			instruction.setString(4, employe.getPassword());
            instruction.setDate(5, employe.getDateArrivee() != null ? java.sql.Date.valueOf(employe.getDateArrivee()) : null);
            instruction.setDate(6, employe.getDateDepart() != null ? java.sql.Date.valueOf(employe.getDateDepart()) : null);
			instruction.setInt(7, employe.getLigue().getIdLigue());
			instruction.setBoolean(8, employe.estAdmin(employe.getLigue()));
			instruction.executeUpdate();
			ResultSet id = instruction.getGeneratedKeys();
			id.next();
			return id.getInt(1);
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}
	}
	
	@Override
	public void update(Ligue ligue) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction;
			instruction = connection.prepareStatement("UPDATE ligue SET nomLigue = ? WHERE numLigue = ?");
			instruction.setString(1, ligue.getNom());
			instruction.setInt(2, ligue.getIdLigue());
			instruction.executeUpdate();
			// Mise à jour des droits d'administrateur
			// D'abord, on retire les droits d'admin à tous les employés de la ligue
			PreparedStatement resetAdmin = connection.prepareStatement(
				"UPDATE utilisateur SET admin = 0 WHERE numLigue = ?");
			resetAdmin.setInt(1, ligue.getIdLigue());
			resetAdmin.executeUpdate();

			// On définit le nouvel administrateur
			if (ligue.getAdministrateur() != null && !ligue.getAdministrateur().estRoot())
			{
				PreparedStatement setAdmin = connection.prepareStatement(
					"UPDATE utilisateur SET admin = 1 WHERE idUtilisateur = ?");
				setAdmin.setInt(1, ligue.getAdministrateur().getId());
				setAdmin.executeUpdate();
			}
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}       
	}
	
	@Override
	public void update(Employe employe) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction = connection.prepareStatement(
				"UPDATE utilisateur SET nomUtil = ?, prenomUtil = ?, mailUtil = ?, passwordUtil = ?, " +
				"date_arrivee = ?, date_depart = ?, numLigue = ?, admin = ? WHERE idUtilisateur = ?");
			instruction.setString(1, employe.getNom());
			instruction.setString(2, employe.getPrenom());
			instruction.setString(3, employe.getMail());
			instruction.setString(4, employe.getPassword());
			instruction.setDate(5, employe.getDateArrivee() != null ? java.sql.Date.valueOf(employe.getDateArrivee()) : null);
			instruction.setDate(6, employe.getDateDepart() != null ? java.sql.Date.valueOf(employe.getDateDepart()) : null);
			instruction.setObject(7, employe.getLigue() != null ? employe.getLigue().getIdLigue() : null);
			instruction.setBoolean(8, employe.estRoot());
			instruction.setInt(9, employe.getId());
			instruction.executeUpdate();
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}
	}
	
	@Override
	public void delete(Employe employe) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction = connection.prepareStatement(
				"DELETE FROM utilisateur WHERE idUtilisateur = ?");
			instruction.setInt(1, employe.getId());
			instruction.executeUpdate();
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}
	}
	
	@Override
	public void delete(Ligue ligue) throws SauvegardeImpossible 
	{
		try 
		{
			// D'abord, supprimer tous les employés de la ligue
			PreparedStatement deleteEmployes = connection.prepareStatement(
				"DELETE FROM utilisateur WHERE numLigue = ?");
			deleteEmployes.setInt(1, ligue.getIdLigue());
			deleteEmployes.executeUpdate();

			// Ensuite, supprimer la ligue
			PreparedStatement deleteLigue = connection.prepareStatement(
				"DELETE FROM ligue WHERE numLigue = ?");
			deleteLigue.setInt(1, ligue.getIdLigue());
			deleteLigue.executeUpdate();
		} 
		catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SauvegardeImpossible(exception);
		}
	}
}
