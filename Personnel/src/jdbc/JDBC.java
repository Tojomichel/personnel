package jdbc;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import personnel.*;

public class JDBC implements Passerelle {
    Connection connection;

    public JDBC() {
        try {
            Class.forName(Credentials.getDriverClassName());
            connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(),
                    Credentials.getPassword());
        } catch (ClassNotFoundException e) {
            System.out.println("Pilote JDBC non installé.");
        } catch (SQLException e) {
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
    public GestionPersonnel getGestionPersonnel() {
        GestionPersonnel gestionPersonnel = new GestionPersonnel();
        try {
            // Charger les ligues depuis la base de données
            String requeteLigue = "SELECT * FROM LIGUE";
            Statement instructionLigue = connection.createStatement();
            ResultSet ligues = instructionLigue.executeQuery(requeteLigue);

            while (ligues.next()) {
                int idLigue = ligues.getInt("numLigue");
                String nomLigue = ligues.getString("nom");
                Ligue ligue = gestionPersonnel.addLigue(idLigue, nomLigue);

                // Récupérer l'administrateur de la ligue (idType = 2)
                String requeteAdmin = "SELECT * FROM UTILISATEUR WHERE numLigue = ? AND idType = 2";
                PreparedStatement instructionAdmin = connection.prepareStatement(requeteAdmin);
                instructionAdmin.setInt(1, idLigue);
                ResultSet adminResult = instructionAdmin.executeQuery();

                if (adminResult.next()) {
                    int idAdmin = adminResult.getInt("idUtilisateur");
                    String nom = adminResult.getString("nomUtil");
                    String prenom = adminResult.getString("prenomUtil");
                    String mail = adminResult.getString("mailUtil");
                    String password = adminResult.getString("passwordUtil");
                    LocalDate dateArrivee = adminResult.getDate("date_arrivee") != null
                            ? adminResult.getDate("date_arrivee").toLocalDate()
                            : null;
                    LocalDate dateDepart = adminResult.getDate("date_depart") != null
                            ? adminResult.getDate("date_depart").toLocalDate()
                            : null;

                    // Créer l'employé et l'affecter comme administrateur
                    Employe admin = new Employe(gestionPersonnel, idAdmin, ligue, nom, prenom, mail, password,
                            dateArrivee, dateDepart);
                    ligue.setAdministrateur(admin);
                }
            }

            // Charger l'utilisateur root depuis la base de données (idType = 1)
            String requeteRoot = "SELECT * FROM UTILISATEUR WHERE idType = 1";
            Statement instructionRoot = connection.createStatement();
            ResultSet rootResult = instructionRoot.executeQuery(requeteRoot);

            if (rootResult.next()) {
                int idRoot = rootResult.getInt("idUtilisateur");
                String nom = rootResult.getString("nomUtil");
                String prenom = rootResult.getString("prenomUtil");
                String mail = rootResult.getString("mailUtil");
                String password = rootResult.getString("passwordUtil");
                LocalDate dateArrivee = rootResult.getDate("date_arrivee") != null
                        ? rootResult.getDate("date_arrivee").toLocalDate()
                        : null;
                LocalDate dateDepart = rootResult.getDate("date_depart") != null
                        ? rootResult.getDate("date_depart").toLocalDate()
                        : null;

                // Créer un objet Employe pour le root et l'affecter à la gestion du personnel
                Employe root = new Employe(gestionPersonnel, idRoot, null, nom, prenom, mail, password, dateArrivee,
                        dateDepart);
                gestionPersonnel.setRoot(root);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return gestionPersonnel;
    }

    @Override
    public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible {
        close();
    }

    public void close() throws SauvegardeImpossible {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            throw new SauvegardeImpossible(e);
        }
    }

    @Override
    public int insert(Ligue ligue) throws SauvegardeImpossible {
        try {
            PreparedStatement instruction;
            instruction = connection.prepareStatement("insert into ligue (nom) values(?)",
                    Statement.RETURN_GENERATED_KEYS);
            instruction.setString(1, ligue.getNom());
            instruction.executeUpdate();
            ResultSet id = instruction.getGeneratedKeys();
            id.next();
            return id.getInt(1);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public int insert(Employe employe) throws SauvegardeImpossible {
        try {
            PreparedStatement instruction;
            instruction = connection.prepareStatement(
                    "INSERT INTO UTILISATEUR (nomUtil, prenomUtil, mailUtil, passwordUtil, date_arrivee, date_depart, idType, numLigue) "
                            +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            instruction.setString(1, employe.getNom()); // nomUtil
            instruction.setString(2, employe.getPrenom()); // prenomUtil
            instruction.setString(3, employe.getMail()); // mailUtil
            instruction.setString(4, employe.checkPassword(employe.getMail()) ? employe.getMail() : employe.getMail()); // passwordUtil
                                                                                                                        // (à
                                                                                                                        // modifier
                                                                                                                        // si
                                                                                                                        // besoin)
            instruction.setDate(5,
                    employe.getDateArrivee() != null ? new java.sql.Date(employe.getDateArrivee().getTime()) : null); // date_arrivee
            instruction.setDate(6,
                    employe.getDateDepart() != null ? new java.sql.Date(employe.getDateDepart().getTime()) : null); // date_depart
            instruction.setInt(7, employe.getType() != null ? employe.getType().getId() : null); // idType
            instruction.setInt(8, employe.getLigue() != null ? employe.getLigue().getId() : null); // numLigue

            instruction.executeUpdate();

            ResultSet id = instruction.getGeneratedKeys();
            id.next();
            return id.getInt(1);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public void update(Ligue ligue) throws SauvegardeImpossible {
        try {

            String query = "UPDATE ligue SET nom = ? WHERE numLigue = ?";
            PreparedStatement instruction = connection.prepareStatement(query);

            instruction.setString(1, ligue.getNom());
            instruction.setInt(2, ligue.getId());

            int rowsAffected = instruction.executeUpdate();

            if (rowsAffected == 0) {
                throw new SauvegardeImpossible("Aucune ligue trouvée à mettre à jour.");
            }
        } catch (SQLException exception) {

            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public void update(Employe employe) throws SauvegardeImpossible {
        try {
            String requete = "UPDATE employe SET nom = ?, prenom = ?, mail = ?, password = ?, idLigue = ?, dateArrivee = ?, dateDepart = ? WHERE id = ?";
            PreparedStatement instruction = connection.prepareStatement(requete);

            instruction.setString(1, employe.getNom());
            instruction.setString(2, employe.getPrenom());
            instruction.setString(3, employe.getMail());
            instruction.setString(4, employe.getPassword());
            instruction.setInt(5, employe.getLigue() != null ? employe.getLigue().getId() : null);
            instruction.setObject(6, employe.getDateArrivee()); // Utiliser setObject pour LocalDate
            instruction.setObject(7, employe.getDateDepart());
            instruction.setInt(8, employe.getId());

            instruction.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    // changement d'admin dans la BDD
    public void updateAdministrateur(Ligue ligue, Employe nouvelAdmin) throws SauvegardeImpossible {
        try {
            // Étape 1 : Désactiver l'ancien administrateur de la ligue
            String resetAdminSQL = "UPDATE UTILISATEUR SET idType = 3 WHERE numLigue = ? AND idType = 2";
            PreparedStatement resetAdminStmt = connection.prepareStatement(resetAdminSQL);
            resetAdminStmt.setInt(1, ligue.getId());
            resetAdminStmt.executeUpdate();

            // Étape 2 : Nommer le nouvel administrateur
            String updateAdminSQL = "UPDATE UTILISATEUR SET idType = 2 WHERE idUtilisateur = ?";
            PreparedStatement updateAdminStmt = connection.prepareStatement(updateAdminSQL);
            updateAdminStmt.setInt(1, nouvelAdmin.getId());
            updateAdminStmt.executeUpdate();

            // Étape 3 : Mettre à jour l'objet en mémoire
            ligue.setAdministrateur(nouvelAdmin);
        } catch (SQLException e) {
            throw new SauvegardeImpossible(e);
        }
    }
}
