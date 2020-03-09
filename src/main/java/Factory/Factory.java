//using System.Threading;
//
//
//public class Factory    //This game logic should be deployed in servers. In case data are corrupted by users.
//{
//    private Thread ManufactureMonitoringThread;
//    void ReceiveClientRequest()
//    {
//        //Get user identifier here
//    }
//
//
//    void ManufactureAutoCars(string userIdentifier)
//    {
//        //consume certain amount of raw material and labour here
//        if (CheckIfEnoughMaterial(userIdentifier) && CheckIfEnoughEmployee(userIdentifier))
//        {
//            RemoveRequiredMaterialAmountFromDb(userIdentifier);
//            ChangeEmployeeStateAtDb(userIdentifier);
//            //produce car here, set completion time --2h-- here
//            ManufactureMonitoringThread = new Thread(ManufactureSimulation);
//            ManufactureMonitoringThread.Start();
//        }
//        else
//        {
//            //Unity display a pop up window
//        }
//
//    }
//
//    void ManufactureSimulation()
//    {
//        //Completion time is fixed to 2h for now
//        int twoHoursInMs = 7200000;
//        Thread.Sleep(twoHoursInMs);
//    }
//
//    void RemoveRequiredMaterialAmountFromDb(string userIdentifier)
//    {
//
//    }
//
//    void ChangeEmployeeStateAtDb(string userIdentifier)
//    {
//        //change the state of employee in db, from "spare" to "working"
//    }
//
//    bool CheckIfEnoughMaterial(string userIdentifier)
//    {
//        return false;
//    }
//
//    bool CheckIfEnoughEmployee(string userIdentifier)
//    {
//        return false;
//    }
//
//    string CheckManufactureState()
//    {
//        string threadStatus = ManufactureMonitoringThread.ThreadState.ToString();
//        if (threadStatus.Equals("terminated"))
//            return "Finished";
//        return "Manufacturing";
//    }
//}
//
//
