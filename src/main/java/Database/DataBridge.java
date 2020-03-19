package Database;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;

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
            String q_createTableCompanyInfo = "CREATE TABLE IF NOT EXISTS CompanyInfo (id INTEGER PRIMARY KEY, Company VARCHAR, EmployeeHappiness INTEGER, BrandLoyalty INTEGER, Funds INTEGER, LoanAmount INTEGER, Revenue INTEGER, Costs INTEGER, CarsSold INTEGER, CompanyLevel INTEGER, XP INTEGER, Aluminium INTEGER, Glass INTEGER, Steel INTEGER, Rubber INTEGER, Cars INTEGER)";
            String q_createTablePlayerDetails = "CREATE TABLE IF NOT EXISTS PlayerDetails (id INTEGER PRIMARY KEY, CompanyId INTEGER, Email VARCHAR, Password CHAR(128), Salt VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createTableEmployees = "CREATE TABLE IF NOT EXISTS Employees (id INTEGER PRIMARY KEY, JobType VARCHAR, HourlyPay INTEGER)";
            String q_createTableEmployeeRecords = "CREATE TABLE IF NOT EXISTS EmployeeRecords(id INTEGER PRIMARY KEY, CompanyId INTEGER, EmployeeId INTEGER, Quantity INTEGER, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id), FOREIGN KEY(EmployeeId) REFERENCES Employees(id))";
            String q_createRawMaterialRecords = "CREATE TABLE IF NOT EXISTS MaterialRecords (id INTEGER PRIMARY KEY, CompanyId INTEGER, MaterialName VARCHAR, Price INTEGER, Quantity INTEGER, SaleDate TEXT, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createCurrentlyBuildingCars = "CREATE TABLE IF NOT EXISTS CarsBuilding (id INTEGER PRIMARY KEY, CompanyId INTEGER, StartTime VARCHAR, SellPrice INTEGER, TimeToComplete INTEGER, MechsUsed INTEGER, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createCarStock = "CREATE TABLE IF NOT EXISTS CarsInventory (id INTEGER PRIMARY KEY, CompanyId INTEGER, CarValue INTEGER, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";

            cmnd.execute(q_createTableCompanyInfo);
            cmnd.execute(q_createTablePlayerDetails);
            cmnd.execute(q_createTableEmployees);
            cmnd.execute(q_createTableEmployeeRecords);
            cmnd.execute(q_createCurrentlyBuildingCars);
            cmnd.execute(q_createCarStock);
            String q_insertMechanic = "INSERT INTO Employees(JobType, HourlyPay) SELECT 'Mechanic', 50 WHERE NOT EXISTS(SELECT * FROM Employees WHERE JobType = 'Mechanic')";
            String q_insertEngineer = "INSERT INTO Employees(JobType, HourlyPay) SELECT 'Engineer', 200 WHERE NOT EXISTS(SELECT * FROM Employees WHERE JobType = 'Engineer')";
            String q_insertSalesperson = "INSERT INTO Employees(JobType, HourlyPay) SELECT 'Salesperson', 70 WHERE NOT EXISTS(SELECT * FROM Employees WHERE JobType = 'Salesperson')";
            Statement stmt = c.createStatement();
            stmt.executeUpdate(q_insertMechanic);
            stmt.executeUpdate(q_insertEngineer);
            stmt.executeUpdate( q_insertSalesperson);
            cmnd.execute(q_createRawMaterialRecords);

            c.close();
        }
        catch (Exception e){
            c.close();
        }
    }

    public boolean WritePlayer(String emailstr, String companystr, String password, String salt) throws SQLException {
        connectToDb();
        String InitialiseCompanyDetailsstmt = "INSERT INTO CompanyInfo (Company, EmployeeHappiness, BrandLoyalty, Funds, LoanAmount, Revenue, Costs, CarsSold, CompanyLevel, XP, Aluminium, Glass, Steel, Rubber, Cars) VALUES (?, 0, 0, 10000, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0)";
        String GetCompanyIdstmt = "SELECT id FROM CompanyInfo WHERE Company = ?";
        String InitialisePlayerDetailsstmt = "INSERT INTO PlayerDetails (CompanyId, Email, Password, Salt) VALUES (?, ?, ?, ?)";
        String InitialiseEmployeesstmt = "INSERT INTO EmployeeRecords (CompanyId, EmployeeId, Quantity) VALUES (?, ?, 0)";
        String GetEmployeeId = "SELECT * FROM Employees";
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

            Statement getIdstmt = c.createStatement();
            ResultSet ResultSet = getIdstmt.executeQuery(GetEmployeeId);
            while (ResultSet.next()){
                PreparedStatement createEmployees = c.prepareStatement(InitialiseEmployeesstmt);
                createEmployees.setInt(1, companyId);
                createEmployees.setInt(2, ResultSet.getInt("id"));
                createEmployees.executeUpdate();
            }
            c.close();
            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            c.close();
            return false;
        }


    }

    public void startCarBuild(int companyID, String time, int sellPrice, int timeNeeded, int mechs) throws SQLException {
        connectToDb();
        String makeNewCarRow = "INSERT INTO CarsBuilding (CompanyId, StartTime, SellPrice, TimeToComplete, MechsUsed) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement carBuildCommand = c.prepareStatement(makeNewCarRow);
            carBuildCommand.setInt(1, companyID);
            carBuildCommand.setString(2, time);
            carBuildCommand.setInt(3, sellPrice);
            carBuildCommand.setInt(4, timeNeeded);
            carBuildCommand.setInt(5, mechs);
            carBuildCommand.executeUpdate();

            decrementMechanic(companyID, mechs);
        } catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            c.close();
        }
    }

    public String getBuildingCars(int compID) throws SQLException {
        connectToDb();
        String getCurrentCars = "SELECT id, StartTime, SellPrice, TimeToComplete, MechsUsed FROM CarsBuilding where CompanyId = ?";
        try {
            HashMap<Integer, String[]> currCars = new HashMap<>();
            PreparedStatement getCurrentlyBuildingCarsStatement = c.prepareStatement(getCurrentCars);
            getCurrentlyBuildingCarsStatement.setInt(1, compID);
            ResultSet resultSet = getCurrentlyBuildingCarsStatement.executeQuery();
            while (resultSet.next()){
                currCars.put(resultSet.getInt("id"), new String[]{resultSet.getString("StartTime"), Integer.toString(resultSet.getInt("SellPrice")), Integer.toString(resultSet.getInt("TimeToComplete")), Integer.toString(resultSet.getInt("MechsUsed"))});
            }
            c.close();
            return new Gson().toJson(currCars);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            c.close();
            return "failed retrieving building cars";
        }
    }

    public void finishCar(int rowID, int carPrice, int mechs) throws SQLException {
        connectToDb();
        String getCompanyIDForRow = "SELECT CompanyId FROM CarsBuilding WHERE id = ?";
        String deleteRowFromCarsBuilding = "DELETE FROM CarsBuilding WHERE id = ?";
        String addStockRow = "INSERT INTO CarsInventory (CompanyId, CarValue) VALUES (?, ?)";

        try{
            PreparedStatement companyRowStatment = c.prepareStatement(getCompanyIDForRow);
            companyRowStatment.setInt(1, rowID);
            ResultSet compIdRS = companyRowStatment.executeQuery();
            int companyId = 0;
            while (compIdRS.next()){
                companyId = compIdRS.getInt("CompanyId");
            }
            PreparedStatement deleteRowStatement = c.prepareStatement(deleteRowFromCarsBuilding);
            deleteRowStatement.setInt(1, rowID);
            deleteRowStatement.executeUpdate();

            PreparedStatement makeNewRowStatement = c.prepareStatement(addStockRow);
            makeNewRowStatement.setInt(1, companyId);
            makeNewRowStatement.setInt(2, carPrice);
            makeNewRowStatement.executeUpdate();

            incrementMechanic(companyId, mechs);
        } catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            c.close();
        }
    }

    public void decrementMechanic(int compID, int mechs) throws SQLException {

        String updateMech = "UPDATE EmployeeRecords SET Quantity = Quantity - ? WHERE CompanyId = ? AND EmployeeId = 1";
        try{
            PreparedStatement decrementMechanicStatement = c.prepareStatement(updateMech);
            decrementMechanicStatement.setInt(1, mechs);
            decrementMechanicStatement.setInt(2, compID);
            decrementMechanicStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void incrementMechanic(int compID, int mechs) throws SQLException{
        connectToDb();
        String updateMech = "UPDATE EmployeeRecords SET Quantity = Quantity + ? WHERE CompanyId = ? AND EmployeeId = 1";
        try{
            PreparedStatement decrementMechanicStatement = c.prepareStatement(updateMech);
            decrementMechanicStatement.setInt(1, mechs);
            decrementMechanicStatement.setInt(2, compID);
            decrementMechanicStatement.executeUpdate();
        } catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            c.close();
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
    public String companyDetails(String email) throws SQLException {
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
            String aluminium = companyResultSet.getString(12); // steel stock
            String glass = companyResultSet.getString(13); // glass stock
            String steel = companyResultSet.getString(14); // steel stock
            String rubber = companyResultSet.getString(15); // rubber stock
            String cars = companyResultSet.getString(16); // car stock;

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
            companyDetailsMap.put("Aluminium", aluminium);
            companyDetailsMap.put("Glass", glass);
            companyDetailsMap.put("Steel", steel);
            companyDetailsMap.put("Rubber", rubber);
            companyDetailsMap.put("Cars", cars);

            c.close();
            return new Gson().toJson(companyDetailsMap);
        } catch (Exception e) {
            c.close();
            System.out.println(e.getMessage());
            return "";
        }
    }


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
        String IncrementQuantitystmt = "UPDATE EmployeeRecords SET Quantity = Quantity + 1 WHERE CompanyId = ? AND EmployeeId = ?";
        String GetEmployeeId = "SELECT id, HourlyPay FROM Employees WHERE JobType = ?";

        try {
            int EmployeeId = 0;
            int employeePay = 0;
            PreparedStatement Employeestmt = c.prepareStatement(GetEmployeeId);
            Employeestmt.setString(1, Employeetype);
            ResultSet ResultSet = Employeestmt.executeQuery();
            while(ResultSet.next()){
                EmployeeId = ResultSet.getInt("id");
                employeePay = ResultSet.getInt("HourlyPay");
            }
            // Read and print all values in table
            PreparedStatement stmt  = c.prepareStatement(IncrementQuantitystmt);
            stmt.setInt(1, companyId);
            stmt.setInt(2, EmployeeId);
            stmt.executeUpdate();

            decrementFunds(companyId, employeePay);

            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
    }

    private void incrementFunds(int companyID, int amount){
        String updateFunds = "UPDATE CompanyInfo SET Funds = Funds + ?, Revenue = Revenue + ? WHERE id = ?";
        try {
            PreparedStatement statement = c.prepareStatement(updateFunds);
            statement.setInt(1, amount); statement.setInt(2, amount); statement.setInt(3, companyID);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decrementFunds(int companyID, int amount){
        String updateFunds = "UPDATE CompanyInfo SET Funds = Funds - ?, Costs = Costs + ? WHERE id = ?";
        try {
            PreparedStatement statement = c.prepareStatement(updateFunds);
            statement.setInt(1, amount); statement.setInt(2, amount); statement.setInt(3, companyID);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void BuyRawMaterials(int companyId, String name, int price, int quantity) throws SQLException {
        connectToDb();
        String insertRecord = "INSERT INTO MaterialRecords(CompanyId, MaterialName, Price, Quantity, SaleDate) Values(?, ?, ?, ?, ?)";
        String updateMaterialInventory = "UPDATE CompanyInfo SET / = / + ? WHERE id = ?".replace("/",name);
        System.out.println(updateMaterialInventory);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        String saleDate = formatter.format(today);
        try {
            PreparedStatement stmt  = c.prepareStatement(insertRecord);
            stmt.setInt(1, companyId);
            stmt.setString(2, name);
            stmt.setInt(3, price);
            stmt.setInt(4, quantity);
            stmt.setString(5, saleDate);
            stmt.executeUpdate();

            PreparedStatement inventoryStatement = c.prepareStatement(updateMaterialInventory);
            //inventoryStatement.setString(1, name);
            //inventoryStatement.setString(2, name);
            inventoryStatement.setInt(1, quantity);
            inventoryStatement.setInt(2, companyId);
            inventoryStatement.executeUpdate();

            decrementFunds(companyId, price*quantity);

            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
    }

    public void RepayLoan(int companyId, int loanAmount) throws SQLException {
        connectToDb();
        String IncrementQuantitystmt = "UPDATE CompanyInfo SET LoanAmount = LoanAmount - ?, Funds = Funds - ? WHERE id = ?";

        try{
            PreparedStatement stmt  = c.prepareStatement(IncrementQuantitystmt);
            stmt.setInt(1, loanAmount);
            stmt.setInt(2, loanAmount);
            stmt.setInt(3, companyId);
            stmt.executeUpdate();
            c.close();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            c.close();
        }
    }
    public String GetMaterialRecords(int companyId) throws SQLException {
        StringBuilder input = new StringBuilder();
        connectToDb();

        String SelectUserstmt = "SELECT * FROM MaterialRecords where CompanyId = ? ORDER BY date(SaleDate) desc";
        try {
            // Read and print all values in table
            PreparedStatement stmt  = c.prepareStatement(SelectUserstmt);
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                input.append(rs.getString("MaterialName"));
                input.append("%");
                input.append(rs.getInt("Quantity"));
                input.append("%");
                input.append(rs.getInt("Price"));
                input.append("%");
                input.append(rs.getString("SaleDate"));
                input.append(",");
            }
            input.deleteCharAt(input.length() - 1);
            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
        return input.toString();
    }
    public static String Hash(String password, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String input = password + salt;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte [] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] digest = md.digest(inputBytes);
        return Base64.getEncoder().encodeToString(digest);
    }
}
