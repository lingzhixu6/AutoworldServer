//
//
//public class DataBridge {
//    //private static bool _dataExists;
//    private static DatabaseReference _rootReference = FirebaseDatabase.DefaultInstance.RootReference;
//    private static readonly DataBridge _dataBridge;
//    public static string playerEmail;  //playerID should be fetched from db first, then it can be used to retrieve other data
//    public static Player currentPlayer;
//    static string connection = "URI=file:" + "Assets/Scripts/Database/AutoWorldDb.db";
//    private static SqliteConnection dbcon = new SqliteConnection(connection);
//
//    private DataBridge() {
//        //FirebaseApp.DefaultInstance.SetEditorDatabaseUrl("https://autoworld-2.firebaseio.com/");
//
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
//    }
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
//    // public static void InitPlayer()
//    // {
//    //     try
//    //     {
//    //         string id = "";
//    //         string company = "";
//    //         string email = "";
//    //         dbcon.Open();
//    //         // Read and print all values in table
//    //         IDataReader reader;
//    //         var cmnd = new SqliteCommand(dbcon);
//    //         cmnd.CommandText = query;
//    //         cmnd.Parameters.AddWithValue("@email", emailstr);
//    //         cmnd.Prepare();
//    //         reader = cmnd.ExecuteReader();
//    //         while (reader.Read())
//    //         {
//    //             id = reader[0].ToString();
//    //             company = reader[1].ToString();
//    //             email = reader[2].ToString();
//    //         }
//    //
//    //         currentPlayer = new Player(id, company, email);
//    //         dbcon.Close();
//    //     }
//    //     catch (Exception ex)
//    //     {
//    //         EditorUtility.DisplayDialog("Error", ex.Message, "Close");
//    //     }
//    // }
//
//    public int GetPlayerBalance() {
//        return -1;
//    }
//
//    public void UpdatePlayerMaterialQty()        //Update = Get and Post
//    {
//    }
//
//    public void CreatePlayerWithEmailAndPassword(string email, string password) {
//    }
//
//    public static string CreateSalt() {
//        string salt = "";
//        string saltset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+=][}{<>"; //Choose random letters
//        Debug.Log(saltset.Length);
//        System.Random rnd = new System.Random();
//        for (int i = 1; i <= 100; i++) {
//            int random = rnd.Next(0, 81);
//            salt += saltset[random];  //Create Salt
//        }
//
//        return salt;
//    }
//
//    public static string Hash(string password, string salt) {
//        Byte[] toBytes = Encoding.UTF8.GetBytes(password + salt);
//        using(HashAlgorithm TypeOfHash = new SHA512Managed())
//        {
//            Byte[] hashbytes = TypeOfHash.ComputeHash(toBytes);
//            string HashedPassword = Convert.ToBase64String(hashbytes);
//            return HashedPassword;
//        }
//
//    }
//}
