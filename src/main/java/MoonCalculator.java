import java.math.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MoonCalculator {
    private double ecl;
    private double RA;
    private double decl;
    private int d;
    private double LST;
    private double eclipticInclination;
    private double longitude;
    private double perihelion;
    private double meanDistFromSun;
    private double eccentricity;
    private double meanAnomaly;

    enum MoonPhase {
        NEW, FIRST_QUARTER, HALF, THIRD_QUARTER, FULL,
        WAXING_CRESCENT, WAXING_GIBBOUS, WANING_GIBBOUS, WANING_CRESCENT;
    }

    public MoonCalculator(){
        //get current date
        LocalDate date = java.time.LocalDate.now();
        System.out.println(date);

        //calculate ecl
        ecl = 23.4393 - 3.563E-7 * d;

        //calculate moon rise and moon set
        double E = meanAnomaly + eccentricity * (180/Math.PI) * Math.sin(meanAnomaly) * (1.0 + eccentricity * Math.cos(meanAnomaly));
        double xv = Math.cos(E) - eccentricity;
        double yv = Math.sqrt(1.0 - eccentricity * eccentricity) * Math.sin(E);
        double v = atan2(yv, xv);
        double r = Math.sqrt(xv * xv + yv * yv);
        double lonmoon = v + perihelion;
        double xs = r * Math.cos(lonmoon);
        double ys = r * Math.sin(lonmoon);
        double xe = xs;
        double ye = ys * Math.cos(ecl);
        double ze = ys * Math.sin(ecl);
        RA = atan2(ye, xe);
        decl = atan2(ze, Math.sqrt(xe * xe + ye * ye));


        //calculate position of the moon
        longitude = 125.1228 - 0.0529538083 * d;
        eclipticInclination = 5.1454;
        perihelion = 318.0634 + 0.1643573223 * d;
        meanDistFromSun = 60.2666;
        eccentricity = 0.054900;
        meanAnomaly = 115.3654 + 13.0649929509 * d;

    }

    public MoonPhase getMoonPhase(){
        MoonPhase phase = MoonPhase.FULL;
        return phase;
    }

    public double moonRiseTime(double RA){
        return 0.0;
    }

    public double atan2(double x, double y){
        return 0.0;
    }

    public double sidereal(){
        return 0.0;
    }

    public static void main(String[] args){
        MoonCalculator test = new MoonCalculator();
    }
}
