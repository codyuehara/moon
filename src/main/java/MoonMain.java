public class MoonMain {
    private static double currentLong = -118.03113; //el monte
    private static double currentLat = 34.071205; //el monte
    public static void main (String[] args){
        //MoonCalculator m = new MoonCalculator(1,1,  2015, 22, 38, -78, false);
        //m.keplersEquation(24.742896);
        //MoonCalculator time1 = new MoonCalculator(12, 12, 2014, 20, 0, -77, false);
        //MoonCalculator sun1 = new MoonCalculator(8, 9, 2000, 12, 30, -95, true);
        //MoonCalculator moon2 = new MoonCalculator(5, 15, 2010, 14.5, -20, -30, true);
        //MoonCalculator moon1 = new MoonCalculator(8, 9, 2000, 12, 30, -95, true);
        //MoonCalculator jan1 = new MoonCalculator(1, 1, 2021, 3, currentLat, currentLong, false);
        //MoonCalculator jan1 = new MoonCalculator(1, 1, 2021, 3, currentLat, currentLong, false);

        for (int i = 0; i < 24; i++){
            System.out.println("time (UT): " + i);
            MoonCalculator test = new MoonCalculator(1, 1, 2021, i, currentLat, currentLong, false);
        }
    }
}
