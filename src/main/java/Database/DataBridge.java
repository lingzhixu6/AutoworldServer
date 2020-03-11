package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBridge {
    Connection c = null;
    private void connectToDb() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.c = DriverManager.getConnection("jdbc:sqlite:AutoWorldDb.db");
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    public void createTables(){
        connectToDb();
        try {
            Statement cmnd = c.createStatement();
            String q_createTableCompanyInfo = "CREATE TABLE IF NOT EXISTS CompanyInfo (id INTEGER PRIMARY KEY, Company VARCHAR, EmployeeHappiness INTEGER, BrandLoyalty INTEGER, Funds INTEGER, LoanAmount INTEGER, Revenue INTEGER, Costs INTEGER, CarsSold INTEGER)";
            String q_createTablePlayerDetails = "CREATE TABLE IF NOT EXISTS PlayerDetails (id INTEGER PRIMARY KEY, CompanyId INTEGER, Email VARCHAR, Password CHAR(128), Salt VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";

            cmnd.execute(q_createTableCompanyInfo);
            cmnd.execute(q_createTablePlayerDetails);

            c.close();

        }
        catch (Exception e){

        }
    }


    public DataBridge() {

//        // Open connect
//        dbcon.Open();
//
//        // Create table
//        IDbCommand dbcmd;
//        dbcmd = dbcon.CreateCommand();
//        string q_createTable = "CREATE TABLE IF NOT EXISTS CompanyInfo (id INTEGER PRIMARY KEY, Company VARCHAR, EmployeeHappiness INTEGER, BrandLoyalty INTEGER, Funds REAL, LoanAmount REAL, Revenue REAL, Costs REAL, CarsSold INTEGER)";
//        dbcmd.CommandText = q_createTable;
//        dbcmd.ExecuteReader();
//        q_createTable = "CREATE TABLE IF NOT EXISTS PlayerDetails (id INTEGER PRIMARY KEY, CompanyId INTEGER, Email VARCHAR, Password CHAR(128), Salt VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
//        dbcmd.CommandText = q_createTable;
//        dbcmd.ExecuteReader();
//        dbcon.Close();
    }
//
//    public static DataBridge GetInstance() {
//        return new DataBridge();
//    }
//
//
//    public static bool WritePlayer(string emailstr, string companystr, string password) {
//        try {
//            string saltstr = CreateSalt();
//
//            password = Hash(password, saltstr);
//
//            dbcon.Open();
//            var cmnd = new SqliteCommand(dbcon);
//            cmnd.CommandText = "INSERT INTO (Company, EmployeeHappiness, BrandLoyalty, Funds, LoanAmount, Revenue, Costs, CarsSold) VALUES (@companyname, 0, 0, 0.0, 0.0, 0.0, 0.0, 0)";
//            cmnd.Parameters.AddWithValue("@companyname", companystr);
//            cmnd.Prepare();
//            cmnd.ExecuteNonQuery(); //Initialise Company Details
//            cmnd.CommandText = "SELECT id FROM CompanyInfo WHERE Company = @companyname";
//            cmnd.Parameters.AddWithValue("@companyname", companystr);
//            cmnd.Prepare();
//            IDataReader reader = cmnd.ExecuteReader();
//            int companyId = -1;
//            while (reader.Read()) {
//                companyId = Convert.ToInt32(reader[0]); //Get Company Id
//            }
//            cmnd.CommandText = "INSERT INTO PlayerDetails (CompanyId, Email, Password, Salt) VALUES (@Id, @email, @password, @salt)";
//            cmnd.Parameters.AddWithValue("@Id", companyId);
//            cmnd.Parameters.AddWithValue("@email", emailstr);
//            cmnd.Parameters.AddWithValue("@password", password);
//            cmnd.Parameters.AddWithValue("@salt", saltstr);
//            cmnd.Prepare();
//            cmnd.ExecuteNonQuery(); //Create Player
//            dbcon.Close();
//            return true;
//        } catch (Exception ex) {
//            EditorUtility.DisplayDialog("Error", ex.Message, "Close");
//            return false;
//        }
//
//
//    }
//
//    public static bool AuthPlayer(string emailstr, string inputPassword) {
//        try {
//            dbcon.Open();
//            // Read and print all values in table
//            IDataReader reader;
//            string query = "SELECT Password, Salt FROM PlayerDetails where Email = @email";
//            var cmnd = new SqliteCommand(dbcon);
//            cmnd.CommandText = query;
//            cmnd.Parameters.AddWithValue("@email", emailstr);
//            cmnd.Prepare();
//            reader = cmnd.ExecuteReader();
//
//            while (reader.Read()) {
//                string salt = reader[1].ToString();
//                string userPassword = reader[0].ToString();
//                inputPassword = Hash(inputPassword, salt);
//                if (inputPassword.Equals(userPassword)) {
//                    dbcon.Close();
//                    return true;
//                }
//            }
//            dbcon.Close();
//            return false;
//
//        } catch (Exception ex) {
//            EditorUtility.DisplayDialog("Error", ex.Message, "Close");
//            dbcon.Close();
//            return false;
//        }
//    }
//
//    public static Player ReadPlayer(string emailstr) {
//        try {
//            string id = "";
//            string company = "";
//            string email = "";
//            dbcon.Open();
//            // Read and print all values in table
//            IDataReader reader;
//            string query = "SELECT * FROM PlayerDetails where Email = @email";
//            var cmnd = new SqliteCommand(dbcon);
//            cmnd.CommandText = query;
//            cmnd.Parameters.AddWithValue("@email", emailstr);
//            cmnd.Prepare();
//            reader = cmnd.ExecuteReader();
//            while (reader.Read()) {
//                id = reader[0].ToString();
//                company = reader[1].ToString();
//                email = reader[2].ToString();
//            }
//
//            currentPlayer = new Player(id, company, email);
//            dbcon.Close();
//            return currentPlayer;
//        } catch (Exception ex) {
//            EditorUtility.DisplayDialog("Error", ex.Message, "Close");
//            return null;
//        }
//    }
//
//
//
//
//

}
