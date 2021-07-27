import java.math.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MoonCalculator {
    //private final double sun's ecliptic longitude at the epoch
    private final double w = 282.938346; //sun's ecliptic longitude at perigee at the epoch
    private double currentLong = -157.85776; //honolulu
    private double currentLat = 21.30485; //honolulu
    private double ecl;
    private double RA;
    private double dec;
    private int year, month;
    private double day;
    private double eccentricity = 0.016708; // of what?

    enum MoonPhase {
        NEW, FIRST_QUARTER, THIRD_QUARTER, FULL,
        WAXING_CRESCENT, WAXING_GIBBOUS, WANING_GIBBOUS, WANING_CRESCENT;
    }

    public MoonCalculator(){
        //get current date
        LocalDate date = java.time.LocalDate.now();
        LocalTime time = java.time.LocalTime.now();
        System.out.println(time);
        double hour = time.getHour();
        double mins = time.getMinute();
        double seconds = time.getSecond();
        //double toDec = HMStoDecimal(hour, mins, seconds);
        //double ut = LCTtoUT(toDec, currentLong);
        //System.out.println("hr: " + hour + "min: " + mins + "sec: " + seconds);
        //int year = date.getYear();
        //int month = date.getMonthValue();
        //int day = date.getDayOfMonth();
        year = 2015;
        month = 1;
        day = 1;
        double ut = LCTtoUT(22, -78);
        double fracDay = ut / 24 + day;
        double jd = julianDate(year, month, fracDay + 1); //works!
        double jd0 = julianDate(2000, 1, 1.5); //julian date for standard epoch
        double elapsedDays = jd - jd0;
        System.out.println("elapsed days: " + elapsedDays);
        double tt = ut + (63.8 / 60 / 60);
        System.out.println("TT: " + tt);

        //double testSun = calcSun(ut, elapsedDays);
        //using epoch J2000
        double meanAnomaly = 360 * elapsedDays / 365.242191 + 280.466069 - w;
        if (meanAnomaly > 360 || meanAnomaly < 0) meanAnomaly = meanAnomaly % 360;
        System.out.println("mean anomaly: " + meanAnomaly);
        double eccentricAnomaly = keplersEquation(meanAnomaly);
        double sunTrueAnomaly = Math.atan((Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * Math.tan(eccentricAnomaly / 2))) * 2;
        if (sunTrueAnomaly > 360) sunTrueAnomaly = sunTrueAnomaly % 360;
        if (sunTrueAnomaly < 0) sunTrueAnomaly = -sunTrueAnomaly % 360;
        System.out.println("true anomaly: " + Math.toDegrees(sunTrueAnomaly)); // works
        double eclLongitude = Math.toDegrees(sunTrueAnomaly) + w;
        if (eclLongitude > 360) eclLongitude = eclLongitude - 360;
        System.out.println("ecliptic longitude: " + eclLongitude);

        //Moon's uncorrected mean ecliptic longitude
        double moonMEL = 13.176339686 * elapsedDays + 218.316433;
        if (moonMEL > 360) moonMEL = moonMEL % 360;
        System.out.println("Moons uncorrected mean ecliptic longitude: " + moonMEL);

        //Moon's uncorrected mean ecliptic longitude of the ascending node
        double omega = 125.044522 - 0.0529539 * elapsedDays;
        if (omega > 360) omega = omega % 360;
        while (omega < 0) omega = omega + 360;
        System.out.println("omega: " + omega);

        //Moon's uncorrected mean anomaly
        double moonMA = moonMEL - 0.1114041 * elapsedDays - 83.353451;
        if (moonMA > 360) moonMA = moonMA % 360;
        while (moonMA < 0) moonMA = moonMA + 360;
        System.out.println("Moon mean anomaly: " + moonMA);

        //annual equation correction
        double ae = 0.1858 * Math.sin(Math.toRadians(meanAnomaly));
        System.out.println("annual equation: " + Math.toDegrees(ae));

        //evection
        double ev = 1.2739 * Math.sin(2 * (moonMEL - eclLongitude) - moonMA); //change to radians
        System.out.println("evection: " + ev);

        //mean anomaly corrections
        double ca = moonMA + ev - ae - 0.37 * Math.sin(meanAnomaly);
        System.out.println("mean anomaly correction: " + ca);

        //moons true anomaly
        double moonTrueAnomaly = 6.2886 * Math.sin(ca) + 0.214 * Math.sin(2 * ca); //change to radians

        //corrected ecliptic longitude
        double moonEclLong = moonMEL + ev + moonTrueAnomaly - ae;
        System.out.println("moons corrected ecliptic longitude: " + Math.toDegrees(moonEclLong));

        //variation correction
        double v = 0.6583 * Math.sin(2 * (moonEclLong - eclLongitude)); //change to radians

        //

        //MoonPhase phase = this.calculateMoonPhase(year, month, day);
        //System.out.println("Moon Phase: " + phase + " day: " + day);
    }

    public double HMStoDecimal(double hours, double mins, double secs){
        double decimal = hours + ((mins + (secs / 60)) / 60);
        System.out.println("decimal: " + decimal);
        return decimal;
    }

    public double julianDate(int year, int month, double day){
        if (month <= 2){
            month = month + 12;
            year = year - 1;
        }
        double t = 0;
        if (year < 0) {
            t = 0.75;
        }
        int a = year / 100;
        int b = 2 - a + a / 4;
        double jd = b + (int)(365.25 * year - t) + (int)(30.6001 * (month + 1)) + day + 1720994.5;
        System.out.println("jd = " + jd);
        return jd;
    }

    /**
     *
     * @param time given in decimal format
     * @param longitude of observer's location
     * @return universal time in decimal format
     */
    public double LCTtoUT(double time, double longitude){
        double adjust = Math.round(longitude / 15);
        double ut = time - adjust;
        if (ut < 0){
            ut = ut + 24;
        }
        if (ut > 24) {
            ut = ut - 24;
        }
        return ut;
    }

    /**
     *
     * @param currentJD of current date
     * @param ut in decimal format
     * @return greenwich sidereal time in decimal format
     */
    public double UTtoGST(double currentJD, double ut){
        double JD0 = julianDate(this.year, 1, 0);
        double daysIntoYear = currentJD - JD0;
        double t = (JD0 - 2415020) / 36525;
        double r = 6.6460656 + 2400.051262 * t + 0.00002581 * Math.pow(t, 2);
        double b = 24 - r + 24 * (year - 1900);
        double t0 = 0.0657098 * daysIntoYear - b;
        double gst = t0 + 1.002738 * ut;
        if (gst < 0 ){ gst = gst + 24; }
        if (gst > 24) gst = gst - 24;
        return gst;
    }

    /**
     *
     * @param gst
     * @param longitude
     * @return local sidereal time in decimal format
     */
    public double GSTtoLST(double gst, double longitude){
        double adjust = longitude / 15;
        double lst = gst + adjust;
        if (lst < 0 ) lst = lst + 24;
        if (lst > 24) lst = lst - 24;
        System.out.println("LST: " + lst);
        return lst;
    }

    /**
     *
     * @param ut
     * @param elapsedDays since the standard epoch
     * @return
     */
    public double calcSun(double ut, double elapsedDays) {

        return 0;
    }

    /**
     *
     * @param meanAnomaly
     * @return eccentric anomaly in radians(currently)
     */
    public double keplersEquation(double meanAnomaly){
        meanAnomaly = Math.toRadians(meanAnomaly);
        double E = meanAnomaly;
        double delta = 1;
        while (delta > 0.000002) {
            double nextE = meanAnomaly + eccentricity * Math.sin(E);
            delta = Math.abs(nextE - E);
            E = nextE;
        }
        return E;
    }

    public void testMoon() {

    }

    public MoonPhase calculateMoonPhase(int year, int month, int day){
        if (month == 1 || month == 2) {
            year--;
            month = month + 12;
        }
        int a = year / 100;
        int b = a / 4;
        int c = 2 - a + b;
        int e = (int)(365.25 * (year + 4716));
        int f = (int)(30.6001 * (month + 1));
        double JD = c + day + e + f - 1524.5;
        double daysSinceNew = (JD - 2451549.5);
        double newMoons = daysSinceNew / 29.53058770576;
        BigDecimal bigDecimal = new BigDecimal(newMoons);
        double decimal = bigDecimal.subtract(new BigDecimal(bigDecimal.intValue())).doubleValue();
        double daysIntoCycle = (decimal * 29.53058770576);
        //System.out.println("days into cycle: " + daysIntoCycle);

        if (daysIntoCycle < 1){
            return MoonPhase.NEW;
        } else if (daysIntoCycle >= 1 && daysIntoCycle < 6.38) {
            return MoonPhase.WAXING_CRESCENT;
        } else if (daysIntoCycle >= 6.38 && daysIntoCycle < 8.38){
            return MoonPhase.FIRST_QUARTER;
        } else if (daysIntoCycle >= 8.38 && daysIntoCycle < 13.765){
            return MoonPhase.WAXING_GIBBOUS;
        } else if (daysIntoCycle >= 13.765 && daysIntoCycle < 15.765) {
            return MoonPhase.FULL;
        } else if (daysIntoCycle >= 15.765 && daysIntoCycle < 21.15){
            return MoonPhase.WANING_GIBBOUS;
        } else if (daysIntoCycle >= 21.15 && daysIntoCycle < 23.15) {
            return MoonPhase.THIRD_QUARTER;
        } else if (daysIntoCycle >= 23.15 && daysIntoCycle < 28.53) {
            return MoonPhase.WANING_CRESCENT;
        } else {
            return MoonPhase.NEW;
        }
    }

    public double moonRiseTime(double RA){
        //calculate moon rise and moon set

        //RA = Math.atan2(ye, xe);
        System.out.println("RA: " + RA);
        //dec = Math.atan2(ze, Math.sqrt(xe * xe + ye * ye));
        System.out.println("Dec: " + dec);
        return 0.0;
    }

    public double moonSetTime(double RA){
        return 0.0;
    }
/*
    public double atan2(double y, double x){
        double temp = 0.0;
        if (x > 0) {
            temp = Math.atan()
        }
        return 0.0;
    }

 */

    public double moonTopocentric(double r){
        return 0.0;
    }

    public static void main(String[] args){
        MoonCalculator test = new MoonCalculator();
    }
}
