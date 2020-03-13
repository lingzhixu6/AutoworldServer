package Database;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.util.Random;

public class DataBridge {

    public DataBridge() {
        connectToDb();
    }


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

    //This method should be called once only. I.e. when the server starts/restarts. So it should only be called in main method.
    public void createSqlTables() throws SQLException {
        try {
            Statement cmnd = c.createStatement();
            String q_createTableCompanyInfo = "CREATE TABLE IF NOT EXISTS CompanyInfo (CompanyId INTEGER PRIMARY KEY, Company VARCHAR, EmployeeHappiness INTEGER, BrandLoyalty INTEGER, Funds INTEGER, LoanAmount INTEGER, Revenue INTEGER, Costs INTEGER, CarsSold INTEGER)";
            String q_createTablePlayerDetails = "CREATE TABLE IF NOT EXISTS PlayerDetails (PlayerId INTEGER PRIMARY KEY, CompanyId INTEGER, Email VARCHAR, Password CHAR(128), Salt VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(CompanyId))";
            String q_createTableFactory = "CREATE TABLE IF NOT EXISTS Factory (PlayerId INTEGER, CarInManu INTEGER, CarManued INTEGER, FOREIGN KEY(PlayerId) REFERENCE PlayerDetails(PlayerId), carInManu INTEGER, carManued INTEGER, );";
            String q_createTableMaterial = "CREATE TABLE IF NOT EXISTS Material (PlayerId INTEGER, SteelStock INTEGER, GlassStock INTEGER, AluminumStock INTEGER, RubberStock INTEGER, FOREIGN KEY PlayerId REFERENCE PlayerDetails(PlayerId))";
            String q_createTableEmployee = "CREATE TABLE IF NOT EXISTS Employee (PlayerId INTEGER, WorkerAtWork INTEGER, WorkerAvailable INTEGER, FOREIGN KEY(PlayerId) REFERENCE PlayerDetails(PlayerId))";

            cmnd.execute(q_createTableCompanyInfo);
            cmnd.execute(q_createTablePlayerDetails);
            cmnd.execute(q_createTableFactory);
            cmnd.execute(q_createTableMaterial);
            cmnd.execute(q_createTableEmployee);

            c.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            c.close();
        }
    }




    public boolean WritePlayer(String emailstr, String companystr, String password, String salt) throws SQLException {
        connectToDb();
        String InitialiseCompanyDetailsstmt = "INSERT INTO CompanyInfo (Company, EmployeeHappiness, BrandLoyalty, Funds, LoanAmount, Revenue, Costs, CarsSold) VALUES (?, 0, 0, 0, 0, 0, 0, 0)";
        String GetCompanyIdstmt = "SELECT id FROM CompanyInfo WHERE Company = ?";
        String InitialisePlayerDetailsstmt = "INSERT INTO PlayerDetails (CompanyId, Email, Password, Salt) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = c.prepareStatement(InitialiseCompanyDetailsstmt);
            pstmt.setString(1, companystr);
            pstmt.executeUpdate(); //Initialise Company Details

            PreparedStatement stmt  = c.prepareStatement(GetCompanyIdstmt);
            stmt.setString(1, companystr);
            ResultSet rs    = stmt.executeQuery();
            int companyId = -1;
            // loop through the result set
            while (rs.next()) {
                companyId = rs.getInt("id");
            }

            PreparedStatement createPlayerDetails = c.prepareStatement(InitialisePlayerDetailsstmt);
            createPlayerDetails.setInt(1, companyId);
            createPlayerDetails.setString(2, emailstr);
            createPlayerDetails.setString(3, password);
            createPlayerDetails.setString(4, salt);
            createPlayerDetails.executeUpdate(); //Initialise Company DetailsPreparedStatement pstmt = c.prepareStatement(InitialiseCompanyDetailsstmt);
            c.close();
            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            c.close();
            return false;
        }


    }

    public boolean AuthPlayer(String emailstr, String inputPassword) throws SQLException {
        connectToDb();
        String SelectPasswordstmt = "SELECT Password, Salt FROM PlayerDetails where Email = ?";
        try {
            // Read and print all values in table
            PreparedStatement stmt  = c.prepareStatement(SelectPasswordstmt);
            stmt.setString(1, emailstr);
            ResultSet rs = stmt.executeQuery();
            int companyId = -1;
            // loop through the result set
            while (rs.next()) {
                String salt = rs.getString("Salt");
                String userPassword = rs.getString("Password");
                System.out.println(userPassword);
                inputPassword = Hash(inputPassword, salt);
                System.out.println(inputPassword);
                if (inputPassword.equals(userPassword)){
                    c.close();
                    return true;
                }
            }

            c.close();
            return false;


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
            return false;
        }
    }
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


    public static String CreateSalt() {
        String salt = "";
        String saltset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+=][}{<>"; //Choose random letters
        Random rnd = new Random();
        for (int i = 1; i <= 100; i++) {
            int random = rnd.nextInt(82);
            salt += Character.toString(saltset.charAt(random));  //Create Salt
        }

        return salt;
    }

    public static String Hash(String password, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
//        Byte[] toBytes = Encoding.UTF8.GetBytes(password + salt);
//        using(HashAlgorithm TypeOfHash = new SHA512Managed())
//        {
//            Byte[] hashbytes = TypeOfHash.ComputeHash(toBytes);
//            string HashedPassword = Convert.ToBase64String(hashbytes);
//            return HashedPassword;
//        }
        String input = password + salt;
//        MessageDigest md = MessageDigest.getInstance("SHA-512");
//        md.update(str.getBytes(StandardCharsets.UTF_8));
//        byte byteData[] = md.digest();
//
//        //convert the byte to hex format method 1
//        StringBuffer hashCodeBuffer = new StringBuffer();
//        for (int i = 0; i < byteData.length; i++) {
//            hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
//        }
//        return hashCodeBuffer.toString();

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte [] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] digest = md.digest(inputBytes);
        return Base64.getEncoder().encodeToString(digest);
    }


    public void testSqlStmt() {
        String SelectAllTableStmt = "SELECT id FROM PlayerDetails;";
        try {
            PreparedStatement sql  = c.prepareStatement(SelectAllTableStmt);
//            ResultSet rs = sql.executeQuery();
//            while (rs.next()) {
//                System.out.println(rs.getString("id"));
//            }
        } catch (Exception e) {
            NotifyUnity.notifyDbError();
            e.printStackTrace();
        }
    }


}