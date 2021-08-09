package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"server.controller","server.config"})
public class MoonMain {

    //private static double currentLong = -118.03113; //el monte
    //private static double currentLat = 34.071205; //el monte
    public static void main (String[] args){
        SpringApplication.run(MoonMain.class, args);
        //get current date
        //LocalDate date = java.time.LocalDate.now();
        //LocalTime time = java.time.LocalTime.now();
        //double hour = time.getHour();
        //double mins = time.getMinute();
        //double seconds = time.getSecond();
        //double toDec = HMStoDecimal(hour, mins, seconds);
        //System.out.println("hr: " + hour + "min: " + mins + "sec: " + seconds);
        //int year = date.getYear();
        //int month = date.getMonthValue();
        //int day = date.getDayOfMonth();
        //int year = 2021;
        //int month = 8;
        //double day = 6;
        //Domain.MoonCalculator test = new Domain.MoonCalculator(38, -78, false);
        //test.calcMoon(1, 1, 2015, 0);
        //Domain.MoonCalculator.MoonPhase w = test.moonPhase();
        //System.out.println("moon phase: " + w);

        //Domain.MoonCalculator m = new Domain.MoonCalculator(currentLat, currentLong, true);

        //for (int i = 0; i < 24; i++) {
            //m.calcMoon(month, day, year, i);
            //if (i == 0){
              //  Domain.MoonCalculator.MoonPhase phase = m.moonPhase();
               // System.out.println("phase: " + phase);
            //}
        //}

        /*
        if (m.moonsetLCT >= 0){
            System.out.println("rise: " + m.moonriseLCT + ", set: " + m.moonsetLCT);
            DMS r = m.decimalToDMS(m.moonriseLCT);
            System.out.println("moonrise time: " + r.degrees + ":" + r.minutes);
            DMS s = m.decimalToDMS(m.moonsetLCT);
            System.out.println("moonset time: " + s.degrees + ":" + s.minutes);
        } else {
            double prevDay = day;
            while (m.moonsetLCT < 0) {
                prevDay--;
                m.calcMoon(month, prevDay, year);

            }
            System.out.println("prev day: " + m.moonriseLCT + ", " + m.moonsetLCT);
            double riseLCTprev = m.moonriseLCT;
            double setLCTprev = m.moonsetLCT;
            m.calcMoon(month, day, year);
            double nextDay = day;
            while (m.moonsetLCT < 0) {
                nextDay++;
                m.calcMoon(month, nextDay, year);
            }
            double riseLCTnext = m.moonriseLCT;
            double setLCTnext = m.moonsetLCT;
            System.out.println("next day: " + m.moonriseLCT + ", " + m.moonsetLCT);
            //System.out.println(nextDay - prevDay);

            double avgRise = (riseLCTnext - riseLCTprev) / (nextDay - prevDay);
            double avgSet = (setLCTnext - setLCTprev) / (nextDay - prevDay);
            //System.out.println("avg rise: " + avgRise + ", avg set: " + avgSet);
            double correctedRise = riseLCTprev + 2 * 0.667155;
            System.out.println("moon real rise time: " + correctedRise);
            double correctedSet = setLCTprev + avgSet;
            System.out.println("moon real set time: " + correctedSet);

            DMS r = m.decimalToDMS(correctedRise);
            System.out.println("moonrise time: " + r.degrees + ":" + r.minutes);
            DMS s = m.decimalToDMS(correctedSet);
            System.out.println("moonset time: " + s.degrees + ":" + s.minutes);
        }

         */


        //Domain.MoonCalculator c = new Domain.MoonCalculator(11, 4, 2000, 3, currentLat, currentLong, false);

        //for (int i = 0; i < 24; i++){
            //System.out.println("time (UT): " + i);
            //Domain.MoonCalculator test = new Domain.MoonCalculator(8, 5, 2021, i, currentLat, currentLong, false);
        //}
    }
}
