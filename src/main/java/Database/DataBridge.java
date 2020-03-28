package Database;

import IBM.ToneAnalyser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            String q_createTableCompanyInfo = "CREATE TABLE IF NOT EXISTS CompanyInfo (id INTEGER PRIMARY KEY, Company VARCHAR, ConsumerConfidence INTEGER, Funds INTEGER, LoanAmount INTEGER, Revenue INTEGER, Costs INTEGER, CarsSold INTEGER, CompanyLevel INTEGER, XP INTEGER, Aluminium INTEGER, Glass INTEGER, Steel INTEGER, Rubber INTEGER, LastCarSold VARCHAR, LastNewsEvent VARCHAR, LastLoanInterest VARCHAR )";
            String q_createTablePlayerDetails = "CREATE TABLE IF NOT EXISTS PlayerDetails (id INTEGER PRIMARY KEY, CompanyId INTEGER, Email VARCHAR, Password CHAR(128), Salt VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createTableEmployees = "CREATE TABLE IF NOT EXISTS Employees (id INTEGER PRIMARY KEY, JobType VARCHAR, HourlyPay INTEGER)";
            String q_createTableEmployeeRecords = "CREATE TABLE IF NOT EXISTS EmployeeRecords(id INTEGER PRIMARY KEY, CompanyId INTEGER, EmployeeId INTEGER, Quantity INTEGER, MechsInUse INTEGER, LastPaid VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id), FOREIGN KEY(EmployeeId) REFERENCES Employees(id))";
            String q_createRawMaterialRecords = "CREATE TABLE IF NOT EXISTS MaterialRecords (id INTEGER PRIMARY KEY, CompanyId INTEGER, MaterialName VARCHAR, Price INTEGER, Quantity INTEGER, SaleDate TEXT, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createCurrentlyBuildingCars = "CREATE TABLE IF NOT EXISTS CarsBuilding (id INTEGER PRIMARY KEY, CompanyId INTEGER, StartTime VARCHAR, SellPrice INTEGER, TimeToComplete INTEGER, MechsUsed INTEGER, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createCarStock = "CREATE TABLE IF NOT EXISTS CarsInventory (id INTEGER PRIMARY KEY, CompanyId INTEGER, CarValue INTEGER, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createCarSales = "CREATE TABLE IF NOT EXISTS CarSales (id INTEGER PRIMARY KEY, CompanyId INTEGER, CarValue INTEGER, SaleDate VARCHAR, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";
            String q_createTableMessages = "CREATE TABLE IF NOT EXISTS Messages (id INTEGER PRIMARY KEY, CompanyId INTEGER, Message VARCHAR, DateSent TEXT, FOREIGN KEY(CompanyId) REFERENCES CompanyInfo(id))";

            cmnd.execute(q_createTableCompanyInfo);
            cmnd.execute(q_createTablePlayerDetails);
            cmnd.execute(q_createTableEmployees);
            cmnd.execute(q_createTableEmployeeRecords);
            cmnd.execute(q_createCurrentlyBuildingCars);
            cmnd.execute(q_createCarStock);
            cmnd.execute(q_createCarSales);
            cmnd.execute(q_createTableMessages);

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
            System.out.println(e.getMessage());
            c.close();
        }
    }
    public boolean WritePlayer(String emailstr, String companystr, String password, String salt) throws SQLException {
        connectToDb();
        String InitialiseCompanyDetailsstmt = "INSERT INTO CompanyInfo (Company, ConsumerConfidence, Funds, LoanAmount, Revenue, Costs, CarsSold, CompanyLevel, XP, Aluminium, Glass, Steel, Rubber, LastCarSold, LastNewsEvent, LastLoanInterest ) VALUES (?, 5, 10000, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, ?, ?, ?)";
        String GetCompanyIdstmt = "SELECT id FROM CompanyInfo WHERE Company = ?";
        String InitialisePlayerDetailsstmt = "INSERT INTO PlayerDetails (CompanyId, Email, Password, Salt) VALUES (?, ?, ?, ?)";
        String InitialiseEmployeesstmt = "INSERT INTO EmployeeRecords (CompanyId, EmployeeId, Quantity, MechsInUse, LastPaid) VALUES (?, ?, 0, 0, ?)";
        String GetEmployeeId = "SELECT * FROM Employees";

        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = myDateObj.format(myFormatObj);
        try {
            PreparedStatement pstmt = c.prepareStatement(InitialiseCompanyDetailsstmt);
            pstmt.setString(1, companystr);
            pstmt.setString(2, formattedDate);
            pstmt.setString(3, formattedDate);
            pstmt.setString(4, formattedDate);
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
                createEmployees.setString(3, formattedDate);
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

    public void DecrementEmployees(int CompanyId, String EmployeeType) throws SQLException {
        connectToDb();
        String IncrementQuantitystmt = "UPDATE EmployeeRecords SET Quantity = Quantity - 1 WHERE CompanyId = ? AND EmployeeId = ?";
        String GetEmployeeId = "SELECT id FROM Employees WHERE JobType = ?";

        try {
            int EmployeeId = 0;
            PreparedStatement Employeestmt = c.prepareStatement(GetEmployeeId);
            Employeestmt.setString(1, EmployeeType);
            ResultSet ResultSet = Employeestmt.executeQuery();
            while(ResultSet.next()){
                EmployeeId = ResultSet.getInt("id");
            }
            // Read and print all values in table
            PreparedStatement stmt  = c.prepareStatement(IncrementQuantitystmt);
            stmt.setInt(1, CompanyId);
            stmt.setInt(2, EmployeeId);
            stmt.executeUpdate();
            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
    }
    public String CheckInputExists(String email, String company) throws SQLException {
        connectToDb();
        String result = "True";
        String checkEmailExists = "SELECT * FROM PlayerDetails WHERE Email = ?";
        String checkCompanyExists = "SELECT * FROM CompanyInfo WHERE Company = ?";
        try{
            PreparedStatement checkExists = c.prepareStatement(checkEmailExists);
            checkExists.setString(1, email);
            ResultSet resultSet = checkExists.executeQuery();
            if (resultSet.next()){
                result = "Email";
            }

            PreparedStatement checkCompanyExist = c.prepareStatement(checkCompanyExists);
            checkCompanyExist.setString(1, company);
            ResultSet resultSetCompany = checkCompanyExist.executeQuery();
            if (resultSetCompany.next()){
                result = "Company";
            }

            c.close();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            c.close();
        }
        HashMap<String, String> resultOutput = new HashMap<>();
        resultOutput.put("result", result);
        return new Gson().toJson(resultOutput);
    }
    public void incrementXP(int compID, int xpPoints) throws SQLException {
        connectToDb();
        String getCurrentLevelAndXP = "SELECT CompanyLevel, XP FROM CompanyInfo WHERE id = ?";
        String updateLevelAndXP = "UPDATE CompanyInfo SET CompanyLevel = ?, XP = ? WHERE id = ?";

        try {
            PreparedStatement cLevelSt = c.prepareStatement(getCurrentLevelAndXP);
            cLevelSt.setInt(1, compID);
            ResultSet levXP = cLevelSt.executeQuery();
            int lev = 0;
            int xp = 0;
            while (levXP.next()){
                lev = levXP.getInt("CompanyLevel");
                xp = levXP.getInt("XP");
            }
            int maxXP = lev*25 + 500;
            PreparedStatement updateLevXpSt = c.prepareStatement(updateLevelAndXP);
            if((xp + xpPoints)>=maxXP){
                int levelsToIncrement = (xp + xpPoints)/maxXP;
                int remainderXp = (xp + xpPoints)%maxXP;
                updateLevXpSt.setInt(1, lev+levelsToIncrement);
                updateLevXpSt.setInt(2, remainderXp);
                updateLevXpSt.setInt(3, compID);
            } else {
                updateLevXpSt.setInt(1, lev);
                updateLevXpSt.setInt(2, xp+xpPoints);
                updateLevXpSt.setInt(3, compID);
            }
            updateLevXpSt.executeUpdate();
            c.close();
        } catch (Exception e){
            c.close();
            System.out.println(e.getMessage());
        }
    }
    public String UpdatePassword(String email, String oldPassword, String password, String salt) throws SQLException {
        String result = "True";
        if (AuthPlayer(email, oldPassword)){
            connectToDb();
            String updatePassword = "UPDATE PlayerDetails SET Password = ?, Salt = ? WHERE Email = ?";
            try{
                PreparedStatement updatestmt = c.prepareStatement(updatePassword);
                updatestmt.setString(1, password);
                updatestmt.setString(2, salt);
                updatestmt.setString(3, email);
                updatestmt.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            finally {
                c.close();
            }
        }
        else{
            result = "False";
        }

        HashMap<String, String> resultOutput = new HashMap<>();
        resultOutput.put("result", result);
        return new Gson().toJson(resultOutput);
    }
    public void startCarBuild(int companyID, String time, int sellPrice, int timeNeeded, int mechs) throws SQLException {
        connectToDb();
        String makeNewCarRow = "INSERT INTO CarsBuilding (CompanyId, StartTime, SellPrice, TimeToComplete, MechsUsed) VALUES (?, ?, ?, ?, ?)";
        String decrementMats = "UPDATE CompanyInfo SET Aluminium = Aluminium - 2, Steel = Steel - 1, Glass = Glass - 3, Rubber = Rubber - 3 WHERE id = ?";
        try {
            PreparedStatement carBuildCommand = c.prepareStatement(makeNewCarRow);
            carBuildCommand.setInt(1, companyID);
            carBuildCommand.setString(2, time);
            carBuildCommand.setInt(3, sellPrice);
            carBuildCommand.setInt(4, timeNeeded);
            carBuildCommand.setInt(5, mechs);
            carBuildCommand.executeUpdate();

            PreparedStatement decremMatsSt = c.prepareStatement(decrementMats);
            decremMatsSt.setInt(1, companyID);
            decremMatsSt.executeUpdate();

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

        String updateMech = "UPDATE EmployeeRecords SET MechsInUse = MechsInUse + ? WHERE CompanyId = ? AND EmployeeId = 1";
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
        String updateMech = "UPDATE EmployeeRecords SET MechsInUse = MechsInUse - ? WHERE CompanyId = ? AND EmployeeId = 1";
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
    public String getEmployeeCounts(int compID) throws SQLException {
        connectToDb();
        String getEmployees = "SELECT EmployeeId, Quantity, MechsInUse FROM EmployeeRecords WHERE CompanyId = ?";
        try {
            PreparedStatement getEmployeeNumbersStatement = c.prepareStatement(getEmployees);
            getEmployeeNumbersStatement.setInt(1, compID);
            ResultSet rs = getEmployeeNumbersStatement.executeQuery();
            HashMap<Integer, String[]> employeeCountMap = new HashMap<>();
            while (rs.next()){
                employeeCountMap.put(rs.getInt("EmployeeId"), new String[]{Integer.toString(rs.getInt("Quantity")), Integer.toString(rs.getInt("MechsInUse"))});
            }
            c.close();
            return new Gson().toJson(employeeCountMap);
        } catch (Exception e){
            c.close();
            System.out.println(e.getMessage());
            return "Failed to get employee Count";
        }
    }
    public void payThePeople(int compID, int payAmount) throws SQLException {
        connectToDb();
        String updateLastPaid = "UPDATE EmployeeRecords SET LastPaid = ? WHERE CompanyId = ?";
        try {
            PreparedStatement updateLastPaidStatement = c.prepareStatement(updateLastPaid);
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = myDateObj.format(myFormatObj);

            updateLastPaidStatement.setString(1, formattedDate);
            updateLastPaidStatement.setInt(2, compID);
            updateLastPaidStatement.executeUpdate();

            decrementFunds(compID, payAmount);
            AddPaymentRecord(compID, payAmount);
        } catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            c.close();
        }
    }
    public void sellCars(int compID, int numberOfCars) throws SQLException {
        connectToDb();
        String numberOfAvailableCars = "SELECT count(*) FROM CarsInventory WHERE CompanyId = ?";
        String getAvailableCarsFromStock = "SELECT * FROM CarsInventory WHERE CompanyId = ? ORDER BY id ASC LIMIT ?";
        String insertIntoSoldCars = "INSERT INTO CarSales (CompanyId, CarValue, SaleDate) VALUES (?, ?, ?)";
        String updateLastSold = "UPDATE CompanyInfo SET LastCarSold = ? WHERE id = ?";
        String deleteRowsFromInventory = "DELETE FROM CarsInventory WHERE id = ?";
        String increaseCarsSold = "UPDATE CompanyInfo SET CarsSold = CarsSold + ? WHERE id = ?";

        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter formatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);
        String formatCorrectDate = myDateObj.format(formatObj);
        try {
            PreparedStatement availCarsStatement = c.prepareStatement(numberOfAvailableCars);
            availCarsStatement.setInt(1, compID);
            ResultSet availCarSet = availCarsStatement.executeQuery();
            int availCarInt = 0;
            while (availCarSet.next()) {
                availCarInt = availCarSet.getInt(1);
            }
            int carsToRemove = Math.min(numberOfCars, availCarInt);
            if(carsToRemove>0){

                PreparedStatement removeFromStockStatement = c.prepareStatement(getAvailableCarsFromStock);
                PreparedStatement insertIntoSoldCarsStatement = c.prepareStatement(insertIntoSoldCars);

                removeFromStockStatement.setInt(1, compID);
                removeFromStockStatement.setInt(2, carsToRemove);
                ResultSet soldCarsSet = removeFromStockStatement.executeQuery();
                int[] idsToDelete = new int[carsToRemove];
                int index = 0;
                while (soldCarsSet.next()){
                    insertIntoSoldCarsStatement.setInt(1, compID);
                    insertIntoSoldCarsStatement.setInt(2, soldCarsSet.getInt("CarValue"));
                    insertIntoSoldCarsStatement.setString(3, formatCorrectDate);
                    insertIntoSoldCarsStatement.executeUpdate();
                    incrementFunds(compID, soldCarsSet.getInt("CarValue"));
                    idsToDelete[index] = soldCarsSet.getInt("id");
                    index++;
                }

                PreparedStatement deleteFromInventoryStatement = c.prepareStatement(deleteRowsFromInventory);
                for(int i : idsToDelete){
                    deleteFromInventoryStatement.setInt(1, i);
                    deleteFromInventoryStatement.executeUpdate();
                }

                PreparedStatement updatecrsSoldStmnt = c.prepareStatement(increaseCarsSold);
                updatecrsSoldStmnt.setInt(1, carsToRemove);
                updatecrsSoldStmnt.setInt(2, compID);
                updatecrsSoldStmnt.executeUpdate();
            }
            PreparedStatement updateLastSoldStatement = c.prepareStatement(updateLastSold);
            updateLastSoldStatement.setString(1, formattedDate);
            updateLastSoldStatement.setInt(2, compID);
            updateLastSoldStatement.executeUpdate();
            c.close();
            incrementXP(compID, 200 * carsToRemove);
        } catch (Exception e){
            c.close();
            System.out.println(e.getMessage());
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
            String brandLoyalty = companyResultSet.getString(3); // confidence
            String funds = companyResultSet.getString(4); // funds
            String loans = companyResultSet.getString(5); // loans
            String revenue = companyResultSet.getString(6); // revenue
            String costs = companyResultSet.getString(7); // costs
            String carsSold = companyResultSet.getString(8); // cars sold
            String currentLevel = companyResultSet.getString((9)); // level
            String xp = companyResultSet.getString(10); // xp
            String aluminium = companyResultSet.getString(11); // steel stock
            String glass = companyResultSet.getString(12); // glass stock
            String steel = companyResultSet.getString(13); // steel stock
            String rubber = companyResultSet.getString(14); // rubber stock
            String LastCarSold = companyResultSet.getString(15); // lcs;
            String LastNewsEvent = companyResultSet.getString(16); // last news event
            String LastInterestPayment = companyResultSet.getString(17); // last time ineterest acrewed

            HashMap<String, String> companyDetailsMap = new HashMap<>();
            companyDetailsMap.put("Company Name", companyName);
            companyDetailsMap.put("Company ID", companyID);
            companyDetailsMap.put("Confidence", brandLoyalty);
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
            companyDetailsMap.put("Last Car Sold", LastCarSold);
            companyDetailsMap.put("Last News Event", LastNewsEvent);
            companyDetailsMap.put("Last Interest Payment", LastInterestPayment);

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
            AddPaymentRecord(companyId, employeePay);
            c.close();


        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
    }
    public String getLastPaid(int compID) throws SQLException {
        connectToDb();
        String getEmployeePaidLog = "SELECT Employees.HourlyPay, EmployeeRecords.Quantity, EmployeeRecords.LastPaid FROM EmployeeRecords  INNER JOIN Employees ON EmployeeRecords.EmployeeId=Employees.id AND EmployeeRecords.CompanyId=?";
        try{
            PreparedStatement getEmployeeLogStatement = c.prepareStatement(getEmployeePaidLog);
            getEmployeeLogStatement.setInt(1, compID);
            ResultSet rs = getEmployeeLogStatement.executeQuery();
            HashMap<Integer, String[]> employeeLogMap= new HashMap();
            while (rs.next()){
                employeeLogMap.put(rs.getInt("HourlyPay"), new String[] {Integer.toString(rs.getInt("Quantity")), rs.getString("LastPaid")});
            }
            c.close();
            return new Gson().toJson(employeeLogMap);
        } catch (Exception e){
            System.out.println(e.getMessage());
            c.close();
            return "Failed to get employee last paid";
        }
    }

    private void AddPaymentRecord(int companyId, int amount) throws SQLException {
        String insertRecord = "INSERT INTO MaterialRecords(CompanyId, MaterialName, Price, Quantity, SaleDate) Values(?, 'Salary Payments', ?, 1, ?)";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        String saleDate = formatter.format(today);
        connectToDb();
        try{
            PreparedStatement stmt = c.prepareStatement(insertRecord);
            stmt.setInt(1, companyId);
            stmt.setInt(2, amount);
            stmt.setString(3, saleDate);
            stmt.executeUpdate();
            c.close();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            c.close();
        }
    }
    public void applyInterest(int compID, int amount) throws SQLException {
        connectToDb();
        try {
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = myDateObj.format(myFormatObj);

            String updateInterestPaidDate = "UPDATE CompanyInfo SET LastLoanInterest = ? WHERE id = ?";
            PreparedStatement updateStatem = c.prepareStatement(updateInterestPaidDate);
            updateStatem.setString(1, formattedDate);
            updateStatem.setInt(2, compID);
            updateStatem.executeUpdate();
            decrementFunds(compID, amount);
            c.close();
        } catch (Exception e){
            c.close();
            System.out.println(e.getMessage());
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

    private void incrementFundsLoan(int companyID, int amount){
        String updateFunds = "UPDATE CompanyInfo SET Funds = Funds + ?, Costs = Costs + ? WHERE id = ?";
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
    public String GetPlayerRankings() throws SQLException {
        connectToDb();
        HashMap<String, String[]> playerInfo = new HashMap<>();
        String GetPlayerRankingsstmt = "SELECT Company, Revenue, Costs FROM CompanyInfo";
        try{
            Statement stmt = c.createStatement();
            ResultSet ResultSet = stmt.executeQuery(GetPlayerRankingsstmt);
            while(ResultSet.next()){
                String name = ResultSet.getString("Company");
                String revenue = ResultSet.getString("Revenue"); // revenue
                String costs = ResultSet.getString("Costs"); // costs
                String[] details = {revenue, costs};
                playerInfo.put(name, details);
            }
            c.close();
        }
        catch (Exception ex){
            c.close();
            System.out.println(ex.getMessage());
        }

        return new Gson().toJson(playerInfo);
    }
    public String changeConfidence(int compID) throws SQLException {
        connectToDb();
        String getConsumerConfidence = "SELECT ConsumerConfidence FROM CompanyInfo WHERE id = ?";
        String incrementConfidence = "UPDATE CompanyInfo SET ConsumerConfidence = ConsumerConfidence + 1 WHERE id = ?";
        String decrementConfidence = "UPDATE CompanyInfo SET ConsumerConfidence = ConsumerConfidence - 1 WHERE id = ?";
        String updateLastGameEvent = "UPDATE CompanyInfo SET LastNewsEvent = ? WHERE id = ?";
        String toneString = new ToneAnalyser().confidenceScore();

        HashMap<String, String> toneMap = new Gson().fromJson(toneString, HashMap.class);
        int toneChange = Integer.parseInt(toneMap.get("change"));
        System.out.println(toneChange);

        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = myDateObj.format(myFormatObj);

        try{
            PreparedStatement updateLasteventst = c.prepareStatement(updateLastGameEvent);
            updateLasteventst.setString(1, formattedDate);
            updateLasteventst.setInt(2, compID);

            if(toneChange==0){
                updateLasteventst.executeUpdate();
                return toneString;
            }

            PreparedStatement getConsumerConfidenceStatement = c.prepareStatement(getConsumerConfidence);
            getConsumerConfidenceStatement.setInt(1, compID);
            ResultSet consumerConfInt = getConsumerConfidenceStatement.executeQuery();
            int conf = 0;
            while (consumerConfInt.next()){
                conf = consumerConfInt.getInt("ConsumerConfidence");
            }
            if(toneChange==1){
                if(conf < 10){
                    PreparedStatement increseConfidence = c.prepareStatement(incrementConfidence);
                    increseConfidence.setInt(1, compID);
                    increseConfidence.executeUpdate();
                }
            }
            if(toneChange==-1){
                if(conf>0){
                    PreparedStatement decreaseCOnf = c.prepareStatement(decrementConfidence);
                    decreaseCOnf.setInt(1, compID);
                    decreaseCOnf.executeUpdate();
                }
            }
            updateLasteventst.executeUpdate();
            c.close();
            return toneString;
        } catch (Exception e){
            c.close();
            System.out.println(e.getMessage());
            return "Failed to get tone or something";
        }

    }
    private int getMonthEndings(int month, int year){
        int day = 0;
        if(month == 2){
            if (year % 4 == 0){
                day = 29;
            }
            else{
                day = 28;
            }
        }
        else{
            switch(month % 2){
                case 1:
                    if (month <= 7){
                        day = 31;
                    }
                    else{
                        day = 30;
                    }
                    break;
                case 0:
                    if (month <= 7){
                        day = 30;
                    }
                    else{
                        day = 31;
                    }
                    break;
            }
        }

        return day;
    }
    private String GetDate(String date, int i){
        String[] splitDate = date.split("-");
        int year = Integer.parseInt(splitDate[0]);
        int month = Integer.parseInt(splitDate[1]);
        int day = Integer.parseInt(splitDate[2]);
        day = day - i;
        if (day <= 0){
            month -= 1;
            if (month <= 0){
                year -= 1;
                month = 12;
            }
            int monthEnding = getMonthEndings(month, year);
            day = monthEnding - (day * -1);
        }
        String dayFormat = Integer.toString(day);
        String monthFormat = Integer.toString(month);
        if (dayFormat.length() == 1){
            dayFormat = "0" + dayFormat;
        }
        if (monthFormat.length() == 1){
            monthFormat = "0" + monthFormat;
        }

        String checkDate = Integer.toString(year) + "-" + monthFormat + "-" + dayFormat;
        return checkDate;
    }
    public String GetSoldRecords(String CompanyId, String chosenDate) throws SQLException {
        connectToDb();
        HashMap<String, HashMap<String, String>> graphInfo = new HashMap<>();
        try {
            int NoOfDays = 10;
            for (int i = 0; i < NoOfDays; i++){
                String SelectUserstmt = "SELECT CarValue FROM CarSales where SaleDate = ? AND CompanyId = ?";
                String date = GetDate(chosenDate, i);
                PreparedStatement stmt  = c.prepareStatement(SelectUserstmt);
                stmt.setString(1, date);
                stmt.setInt(2, Integer.parseInt(CompanyId));
                ResultSet rs = stmt.executeQuery();
                HashMap<String, String> details = new HashMap<>();
                int TotalValue = 0;
                int NoOfCars = 0;
                // loop through the result set
                while (rs.next()) {
                    TotalValue += rs.getInt("CarValue");
                    NoOfCars += 1;
                }
                details.put("totalValue", Integer.toString(TotalValue));
                details.put("carsSold", Integer.toString(NoOfCars));
                graphInfo.put(date, details);
            }
            c.close();
        } catch (Exception ex) {
            c.close();
            System.out.println(ex.getMessage());
        }
        return new Gson().toJson(graphInfo);
    }
    public void AddLoan(int CompanyId, int loanAmount) throws SQLException {
        String addLoan = "UPDATE CompanyInfo SET LoanAmount = LoanAmount + ? WHERE id = ?";
        connectToDb();
        try{
            PreparedStatement stmt = c.prepareStatement(addLoan);
            stmt.setInt(1, loanAmount);
            stmt.setInt(2, CompanyId);
            stmt.executeUpdate();
            incrementFundsLoan(CompanyId, loanAmount);
            c.close();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            c.close();
        }

    }
    public void EndGame(String CompanyId) throws SQLException {
        String deleteCarSales = "DELETE FROM CarSales WHERE CompanyId = ?";
        String deleteCarBuilding = "DELETE FROM CarsBuilding WHERE CompanyId = ?";
        String deleteCarInventory = "DELETE FROM CarsInventory WHERE CompanyId = ?";
        String deleteEmployeeRecords = "DELETE FROM EmployeeRecords WHERE CompanyId = ?";
        String deleteMaterialRecords = "DELETE FROM MaterialRecords WHERE CompanyId = ?";
        String deletePlayerDetails = "DELETE FROM PlayerDetails WHERE CompanyId = ?";
        String deleteCompanyInfo = "DELETE FROM CompanyInfo WHERE CompanyId = ?";
        int id = Integer.parseInt(CompanyId);
        connectToDb();
        try{
            PreparedStatement stmt = c.prepareStatement(deleteCarSales);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt = c.prepareStatement(deleteCarBuilding);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt = c.prepareStatement(deleteCarInventory);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt = c.prepareStatement(deleteEmployeeRecords);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt = c.prepareStatement(deleteMaterialRecords);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt = c.prepareStatement(deletePlayerDetails);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt = c.prepareStatement(deleteCompanyInfo);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            c.close();
        }
        catch (Exception ex){
            c.close();
            System.out.println(ex.getMessage());
        }

    }


    public void SendMessage(int CompanyId, String Message) throws SQLException {
        String sqlstmt = "INSERT INTO Messages(CompanyId, Message, DateSent) Values(?, ?, ?)";
        connectToDb();
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");
            LocalDate today = LocalDate.now();
            String dateSent = formatter.format(today);
            PreparedStatement stmt = c.prepareStatement(sqlstmt);
            stmt.setInt(1, CompanyId);
            stmt.setString(2, Message);
            stmt.setString(3, dateSent);
            stmt.executeUpdate();
            c.close();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            c.close();
        }
    }

    public String GetMessages() throws SQLException {
        String sqlstmt = "SELECT Messages.id as id, CompanyInfo.Company as CompanyName, Messages.Message as Message, Messages.DateSent as DateSent FROM Messages INNER JOIN CompanyInfo ON Messages.CompanyId = CompanyInfo.id";
        connectToDb();
        HashMap<String, String[]> messages = new HashMap<>();
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sqlstmt);
            while(rs.next()){
                String id = Integer.toString(rs.getInt("id"));
                String company = rs.getString("CompanyName");
                String message = rs.getString("Message");
                String dateSent = rs.getString("DateSent");
                String[] details = {company, message, dateSent};
                messages.put(id, details);
            }
            c.close();
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            c.close();
        }
        return new Gson().toJson(messages);
    }

    public static String Hash(String password, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String input = password + salt;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte [] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] digest = md.digest(inputBytes);
        return Base64.getEncoder().encodeToString(digest);
    }

}
