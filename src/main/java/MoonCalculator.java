import java.math.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MoonCalculator {
    private double currentLong = -157.85776;
    private double currentLat = 21.30485;
    private double ecl;
    private double RA;
    private double dec;
    private int d;
    private double LST;
    private double eclipticInclination;
    private double longitude;
    private double perihelion;
    private double meanDistFromSun;
    private double eccentricity;
    private double meanAnomaly;
    private double h; // Moon's altitude

    enum MoonPhase {
        NEW, FIRST_QUARTER, HALF, THIRD_QUARTER, FULL,
        WAXING_CRESCENT, WAXING_GIBBOUS, WANING_GIBBOUS, WANING_CRESCENT;
    }

    public MoonCalculator(){
        //get current date
        LocalDate date = java.time.LocalDate.now();
        System.out.println(date);
        int year = date.getYear();
        //int month = date.getMonthValue();
        //int day = date.getDayOfMonth();
        int month = 1;
        int day = 20;
        System.out.println("day: " + day);
        MoonPhase phase = this.getMoonPhase(year, month, day);
        System.out.println("Moon Phase: " + phase);

        //calculate ecl
        ecl = 23.4393 - 3.563E-7 * d;

        //calculate moon rise and moon set
        double E = meanAnomaly + eccentricity * (180/Math.PI) * Math.sin(meanAnomaly) * (1.0 + eccentricity * Math.cos(meanAnomaly));
        double xv = Math.cos(E) - eccentricity;
        double yv = Math.sqrt(1.0 - eccentricity * eccentricity) * Math.sin(E);
        double v = Math.atan2(yv, xv);
        double r = Math.sqrt(xv * xv + yv * yv);
        double lonmoon = v + perihelion;
        double xs = r * Math.cos(lonmoon);
        double ys = r * Math.sin(lonmoon);
        double xe = xs;
        double ye = ys * Math.cos(ecl);
        double ze = ys * Math.sin(ecl);
        RA = Math.atan2(ye, xe);
        System.out.println("RA: " + RA);
        dec = Math.atan2(ze, Math.sqrt(xe * xe + ye * ye));
        System.out.println("Dec: " + dec);


        //calculate position of the moon
        longitude = 125.1228 - 0.0529538083 * d;
        eclipticInclination = 5.1454;
        perihelion = 318.0634 + 0.1643573223 * d;
        meanDistFromSun = 60.2666;
        eccentricity = 0.054900;
        meanAnomaly = 115.3654 + 13.0649929509 * d;

    }

    public MoonPhase getMoonPhase(int year, int month, int day){
        int a = year / 100;
        int b = a / 4;
        int c = 2 - a + b;
        int e = (int)(365.25 * (year + 4716));
        int f = (int)(30.6001 * (month + 1));
        double JD = c + day + e + f - 1524.5;
        double daysSinceNew = (JD - 2451549.5);
        double newMoons = daysSinceNew / 29.53058770576;
        System.out.println(newMoons);
        BigDecimal bigDecimal = new BigDecimal(newMoons);
        double decimal = bigDecimal.subtract(new BigDecimal(bigDecimal.intValue())).doubleValue();
        System.out.println(decimal);
        double daysIntoCycle = (decimal * 29.53058770576);
        System.out.println("days into cycle: " + daysIntoCycle);

        if (daysIntoCycle <= 1){
            return MoonPhase.NEW;
        } else if (daysIntoCycle > 1 && daysIntoCycle <= 6.38) {
            return MoonPhase.WAXING_CRESCENT;
        } else if (daysIntoCycle > 6.38 && daysIntoCycle <= 8.38){
            return MoonPhase.FIRST_QUARTER;
        } else if (daysIntoCycle > 8.38 && daysIntoCycle <= 13.765){
            return MoonPhase.WAXING_GIBBOUS;
        } else if (daysIntoCycle > 13.77 && daysIntoCycle <= 15.765) {
            return MoonPhase.FULL;
        } else if (daysIntoCycle > 15.765 && daysIntoCycle <= 21.15){
            return MoonPhase.WANING_GIBBOUS;
        } else if (daysIntoCycle > 21.15 && daysIntoCycle <= 23.15) {
            return MoonPhase.THIRD_QUARTER;
        } else if (daysIntoCycle > 23.15 && daysIntoCycle <= 29.53) {
            return MoonPhase.WANING_CRESCENT;
        } else {
            return MoonPhase.NEW;
        }
    }

    public double moonRiseTime(double RA){
        return 0.0;
    }

    //public double atan2(double x, double y){
        //return 0.0;
    //}

    public double calculateLST(){
        return 0.0;
    }

    public double moonTopocentric(double r){
        return 0.0;
    }

    public static void main(String[] args){
        MoonCalculator test = new MoonCalculator();
    }
}
