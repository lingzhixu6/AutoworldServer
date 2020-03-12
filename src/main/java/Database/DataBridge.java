package Database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

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

    public void createTables() throws SQLException {
        connectToDb();
        try {
            Statement cmnd = c.createStatement();
            String q_createTableCompanyInfo = "CREATE TABLE IF NOT EXISTS CompanyInfo (id INTEGER PRIMARY KEY, Company VARCHAR, EmployeeHappiness INTEGER, BrandLoyalty INTEGER, Funds INTEGER, LoanAmount INTEGER, Revenue INTEGER, Costs INTEGER, CarsSold INTEGER, CompanyLevel INTEGER, XP INTEGER)";
            String q_createTablePlayerDetails = "CREATE TABLE IF NOT EXISTS PlayerDetails (id INTEGER PRIMARY KEY, CompanyId INTEGER, Email VARCHAR, Password CHAR(128), Salt VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";

            cmnd.execute(q_createTableCompanyInfo);
            cmnd.execute(q_createTablePlayerDetails);

            c.close();

        }
        catch (Exception e){
            c.close();
        }
    }

    public boolean WritePlayer(String emailstr, String companystr, String password, String salt) throws SQLException {
        connectToDb();
        String InitialiseCompanyDetailsstmt = "INSERT INTO CompanyInfo (Company, EmployeeHappiness, BrandLoyalty, Funds, LoanAmount, Revenue, Costs, CarsSold, CompanyLevel, XP) VALUES (?, 0, 0, 10000, 0, 0, 0, 0, 1, 0)";
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
                inputPassword = Hash(inputPassword, salt);
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

    public String companyDetails(String email){
        connectToDb();
        String getCompanyIDQuery = "SELECT CompanyId, id FROM PlayerDetails where Email = ?";
        String getCompanyDetails = "SELECT * FROM CompanyInfo where id = ?";
        try {
            PreparedStatement statement = c.prepareStatement(getCompanyIDQuery);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            String companyId = resultSet.getString("CompanyId");
            String userID = resultSet.getString("id");


            statement = c.prepareStatement(getCompanyDetails);
            statement.setString(1, companyId);
            ResultSet companyResultSet = statement.executeQuery();

            companyResultSet.next();

            String companyID = companyResultSet.getString(1);
            String companyName = companyResultSet.getString(2); // company name
            String empHappiness =  companyResultSet.getString(3); // employee happ
            String brandLoyalty = companyResultSet.getString(4); // brand loyalty
            String funds = companyResultSet.getString(5); // funds
            String loans = companyResultSet.getString(6); // loans
            String revenue = companyResultSet.getString(7); // revenue
            String costs = companyResultSet.getString(8); // costs
            String carsSold = companyResultSet.getString(9); // cars sold
            String currentLevel = companyResultSet.getString((10)); // level
            String xp = companyResultSet.getString(11); // xp

            HashMap<String, String> companyDetailsMap = new HashMap<>();
            companyDetailsMap.put("Company Name", companyName);
            companyDetailsMap.put("Company ID", companyID);
            companyDetailsMap.put("Employee Happiness", empHappiness);
            companyDetailsMap.put("Brand Loyalty", brandLoyalty);
            companyDetailsMap.put("Funds", funds);
            companyDetailsMap.put("Loans", loans);
            companyDetailsMap.put("Revenue", revenue);
            companyDetailsMap.put("Costs", costs);
            companyDetailsMap.put("Cars Sold", carsSold);
            companyDetailsMap.put("User ID", userID);
            companyDetailsMap.put("Current Level", currentLevel);
            companyDetailsMap.put("XP", xp);

            return new Gson().toJson(companyDetailsMap);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

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

    public HashMap<Integer, String[]> GetEmployeeInfo() throws SQLException { //Get Employee Types and Salaries
        connectToDb();
        String SelectEmployeeInfostmt = "SELECT * FROM Employees";
        HashMap<Integer, String[]> EmployeeDetails = new HashMap<>();
        try{
            Statement Employeestmt = c.createStatement();
            ResultSet ResultSet = Employeestmt.executeQuery(SelectEmployeeInfostmt);
            while(ResultSet.next()){
                int id = ResultSet.getInt("id");
                String type = ResultSet.getString("JobType");
                String pay = ResultSet.getString("HourlyPay");
                String[] details = {type, pay};
                EmployeeDetails.put(id, details);
            }
            c.close();
        }
        catch(Exception ex){
            c.close();
            System.out.println(ex.getMessage());
        }
        return EmployeeDetails;

    }

    public String GetEmployeeRecords(int companyId) throws SQLException {
        HashMap<Integer, String[]> EmployeeDetails = GetEmployeeInfo();

        connectToDb();

        String SelectUserstmt = "SELECT * FROM EmployeeRecords where CompanyId = ?";
        HashMap<String, String[]> Records = new HashMap<>();
        try {

            // Read and print all values in table
            PreparedStatement stmt  = c.prepareStatement(SelectUserstmt);
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                int quantity = rs.getInt("Quantity");
                String type = EmployeeDetails.get(rs.getInt("EmployeeId"))[0];
                String pay =  EmployeeDetails.get(rs.getInt("EmployeeId"))[1];
                String[] details = {pay, Integer.toString(quantity)};
                Records.put(type, details);
            }

            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }

        return new Gson().toJson(Records);

    }

    public void IncrementEmployeeQuantity(int companyId, String Employeetype) throws SQLException {
        connectToDb();
        String IncrementQuantitystmt = "INSERT INTO EmployeeRecords(CompanyId, EmployeeId, Quantity) SELECT ?, ?, Quantity = Quantity + 1 WHERE NOT EXISTS(SELECT * FROM EmployeeRecords WHERE CompanyId = ? AND EmployeeId = ?)";
        String GetEmployeeId = "SELECT id FROM Employees WHERE EmployeeType = ?";
        try {
            int EmployeeId = 0;
            PreparedStatement Employeestmt = c.prepareStatement(GetEmployeeId);
            Employeestmt.setString(1, Employeetype);
            ResultSet ResultSet = Employeestmt.executeQuery();
            while(ResultSet.next()){
                EmployeeId = ResultSet.getInt("id");
            }
            // Read and print all values in table
            PreparedStatement stmt  = c.prepareStatement(IncrementQuantitystmt);
            stmt.setInt(1, companyId);
            stmt.setInt(2, EmployeeId);
            stmt.setInt(3, companyId);
            stmt.setInt(4, EmployeeId);
            stmt.setInt(5, companyId);
            stmt.setInt(6, EmployeeId);
            stmt.executeUpdate();

            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
    }

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
}
