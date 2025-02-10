package jdbc;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import personnel.*;

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

	public interface Passerelle {
		public GestionPersonnel getGestionPersonnel();

		public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible;

		public int insert(Ligue ligue) throws SauvegardeImpossible;

		public void update(Employe employe) throws SauvegardeImpossible;
	}
	
	@Override
	public GestionPersonnel getGestionPersonnel() 
	{
		GestionPersonnel gestionPersonnel = new GestionPersonnel();
		try 
		{
			// Charger les ligues depuis la base de données
			String requeteLigue = "SELECT * FROM LIGUE";
			Statement instructionLigue = connection.createStatement();
			ResultSet ligues = instructionLigue.executeQuery(requeteLigue);
			while (ligues.next())
				gestionPersonnel.addLigue(ligues.getInt("numLigue"), ligues.getString("nom"));
	
			// Charger l'utilisateur root depuis la base de données
			String requeteRoot = "SELECT * FROM UTILISATEUR WHERE idType = 1"; //idType de root (exemple)
			Statement instructionRoot = connection.createStatement();
			ResultSet rootResult = instructionRoot.executeQuery(requeteRoot);
	
			if (rootResult.next()) {
				// Instancier le root avec les données récupérées
				String nom = rootResult.getString("nomUtil");
				String prenom = rootResult.getString("prenomUtil");
				String mail = rootResult.getString("mailUtil");
				String password = rootResult.getString("passwordUtil");
	
				// Créer un objet Employe pour le root et l'affecter à la gestion du personnel
				Employe root = new Employe(gestionPersonnel, null, nom, prenom, mail, password);
				gestionPersonnel.setRoot(root);
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
			instruction = connection.prepareStatement("insert into ligue (nom) values(?)", Statement.RETURN_GENERATED_KEYS);
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
}
	@Override
public int insert(Employe employe) throws SauvegardeImpossible 
{
    try 
    {
        PreparedStatement instruction;
        instruction = connection.prepareStatement(
            "INSERT INTO employe (nom, prenom, mail, password, idLigue) VALUES (?, ?, ?, ?, ?)", 
            Statement.RETURN_GENERATED_KEYS
        );

        instruction.setString(1, employe.getNom());
        instruction.setString(2, employe.getPrenom());
        instruction.setString(3, employe.getMail());
        instruction.setString(4, employe.checkPassword(employe.getMail()) ? employe.getMail() : employe.getMail()); // À modifier si besoin
        instruction.setInt(5, employe.getLigue() != null ? employe.getLigue().getId() : null);

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
