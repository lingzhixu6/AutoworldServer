//package GameLogic;
//
//public class Factory
//{
//    private Thread ManufactureSimulationThread;
//
//
//    private void ManufactureAutoCars(String userIndex)
//    {
//        //consume certain amount of raw material and labour here
//        //When the manufacture is done, update db about the latest user
//        if (CheckIfEnoughMaterial(userIndex) && CheckIfEnoughEmployee(userIndex))
//        {
//            RemoveRequiredMaterialAmountFromDb(userIndex);
//            ChangeEmployeeStateAtDb(userIndex);
//            //produce car here, set completion time --2h-- here
//            ManufactureSimulationThread = new FactoryThread();
//            ManufactureSimulationThread.start();
//        }
//        else
//        {
//            //Unity display a pop up window
//        }
//    }
//
//
//    private void RemoveRequiredMaterialAmountFromDb(String userIndex)
//    {
//
//    }
//
//    private void ChangeEmployeeStateAtDb(String userIndex)
//    {
//        //change the state of employee in db, from "spare" to "working"
//    }
//
//    private boolean CheckIfEnoughMaterial(String userIndex)
//    {
//        return false;
//    }
//
//    private boolean CheckIfEnoughEmployee(String userIndex)
//    {
//        return false;
//    }
//
//    private String CheckManufactureState()
//    {
//        //query the db and return remaining time
//        //this method should be called in Update() in Unity
//
//    }
//
//}
//
//class FactoryThread extends Thread {
//    public void run() {
//        int oneMinInMs = 60000;
//        int twoHoursInMin = 120;
//        while(twoHoursInMin > 0) {
//            try {
//                Thread.sleep(oneMinInMs);
//            } catch (InterruptedException e) {
//                //Tell unity that factory cannot manufacture now.
//            }
//            twoHoursInMin--;
//            changeRemainingCarManuTime();
//        }
//        changeCarNumAtDb();
//    }
//
//
//    private void changeCarNumAtDb(String userIndex) {
//
//    }
//
//    private void changeRemainingCarManuTime(String userIndex) {
//
//    }
//
//}
//
