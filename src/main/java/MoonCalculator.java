import java.awt.*;
import java.math.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MoonCalculator {
    //private final double sun's ecliptic longitude at the epoch
    private final double moonIncl = 5.1453964;
    private final double w = 282.938346; //sun's ecliptic longitude at perigee at the epoch
    private double currentLong = -118.03113; //el monte
    private double currentLat = 34.071205; //el monte
    private double ecl;
    private double RA;
    private double dec;
    private int year, month;
    private double day, time;
    private double latitude, longitude;
    private double sunEccentricity = 0.016708; // of what?
    private DMS altitude, azimuth;
    private boolean DST;

    enum MoonPhase {
        NEW, FIRST_QUARTER, THIRD_QUARTER, FULL,
        WAXING_CRESCENT, WAXING_GIBBOUS, WANING_GIBBOUS, WANING_CRESCENT;
    }

    public MoonCalculator(int month, double day, int year, double time, double lat, double longg, boolean DST){
        //get current date
        //LocalDate date = java.time.LocalDate.now();
        //LocalTime time = java.time.LocalTime.now();
        //double hour = time.getHour();
        //double mins = time.getMinute();
        //double seconds = time.getSecond();
        //double toDec = HMStoDecimal(hour, mins, seconds);
        //double ut = LCTtoUT(toDec, currentLong);
        //System.out.println("hr: " + hour + "min: " + mins + "sec: " + seconds);
        //int year = date.getYear();
        //int month = date.getMonthValue();
        //int day = date.getDayOfMonth();
        //year = 2015;
        this.year = year;
        this.month = month;
        this.day = day;
        this.time = time;
        this.latitude = lat;
        this.longitude = longg;
        this.DST = DST;
        double ut = LCTtoUT(this.time, longitude, DST);
        //double fracDay = ut / 24 + day;
        double jd = julianDate(this.year, this.month, this.day); //works!
        double GST = UTtoGST(jd, ut);
        double LST = GSTtoLST(GST, longitude);
        //double LST = 4.562547;
        double tt = ut + (63.8 / 60 / 60);
        double fracDay = tt / 24 + this.day;
        double jdAdjusted = julianDate(this.year, this.month, fracDay); //works!
        double jd0 = julianDate(2000, 1, 1.5); //julian date for standard epoch
        double elapsedDays = jdAdjusted - jd0;
        //System.out.println("elapsed days: " + elapsedDays);

        //suns ecliptic coordinates -- works
        double sunMA = 360 * elapsedDays / 365.242191 + 280.466069 - w; //deg
        if (sunMA > 360) sunMA = sunMA % 360;
        //System.out.println("sun mean anomaly: " + sunMA);
        double sunEA = keplersEquation(sunMA); //rad
        double sunTA = Math.atan((Math.sqrt((1 + sunEccentricity) / (1 - sunEccentricity)) * Math.tan(sunEA / 2))) * 2; //rad
        if (sunTA > 2 * Math.PI) sunTA = sunTA % 2 * Math.PI;
        while (sunTA < 0) sunTA += 2 * Math.PI;
        //System.out.println("sun true anomaly: " + Math.toDegrees(sunTA));
        //System.out.println("sun TA in rad: " + sunTA);
        double sunEclLong = Math.toDegrees(sunTA) + w; //deg
        if (sunEclLong > 360) sunEclLong = sunEclLong - 360;
        //System.out.println("sun ecliptic longitude: " + sunEclLong);

        //Moon's uncorrected mean ecliptic longitude
        double moonMEL = 13.176339686 * elapsedDays + 218.316433; //deg
        if (moonMEL > 360) moonMEL = moonMEL % 360;
        //System.out.println("Moons uncorrected mean ecliptic longitude: " + moonMEL);

        //Moon's uncorrected mean ecliptic longitude of the ascending node
        double omega = 125.044522 - 0.0529539 * elapsedDays;
        if (omega > 360) omega = omega % 360;
        while (omega < 0) omega = omega + 360;
        //System.out.println("omega: " + omega);

        //Moon's uncorrected mean anomaly
        double moonMA = moonMEL - 0.1114041 * elapsedDays - 83.353451;
        if (moonMA > 360) moonMA = moonMA % 360;
        while (moonMA < 0) moonMA = moonMA + 360;
        //System.out.println("Moon mean anomaly: " + moonMA);

        //annual equation correction
        double ae = 0.1858 * Math.sin(Math.toRadians(sunMA));
        //System.out.println("annual equation: " + ae);

        //evection
        double ev = 1.2739 * Math.sin(Math.toRadians(2 * (moonMEL - sunEclLong) - moonMA)); //change to radians
        //System.out.println("evection: " + ev);

        //mean anomaly corrections
        double ca = moonMA + ev - ae - 0.37 * Math.sin(sunMA); //deg
        //System.out.println("mean anomaly correction: " + ca);

        //moons true anomaly
        double moonTrueAnomaly = 6.2886 * Math.sin(Math.toRadians(ca)) + 0.214 * Math.sin(2 * Math.toRadians(ca)); //deg
        //System.out.println("moon true anomaly: " + moonTrueAnomaly);

        //corrected ecliptic longitude
        double moonCEL = moonMEL + ev + moonTrueAnomaly - ae; //deg
        //System.out.println("moons corrected ecliptic longitude: " + moonCEL);

        //variation correction
        double v = 0.6583 * Math.sin(Math.toRadians(2 * (moonCEL - sunEclLong))); //deg
        //System.out.println("V: " + v);

        //moons true ecliptic longitude
        double moonTEL = Math.toRadians(moonCEL + v); //rad
        //System.out.println("moon true ecliptic longitude: " + moonTEL);

        //corrected ecliptic longitude of the ascending node
        double correctOmega = omega - 0.16 * Math.sin(sunMA); //deg
        //System.out.println("correct omega: " + correctOmega);

        //moons ecliptic longitude
        double y = Math.sin(moonTEL - Math.toRadians(correctOmega)) * Math.cos(Math.toRadians(moonIncl));
        double x = Math.cos(moonTEL - Math.toRadians(correctOmega));
        double t = Math.atan(y / x); //rad
        double moonEclLong = correctOmega + quadraticAdjust(y, x, Math.toDegrees(t));
        if (moonEclLong > 360) {
            moonEclLong = moonEclLong - 360;
        }
        //System.out.println("moon ecliptic longitude: " + moonEclLong);

        //moons ecliptic latitude
        double moonEclLat = Math.toDegrees(Math.asin(Math.sin((moonTEL - Math.toRadians(correctOmega))) * Math.sin(Math.toRadians(moonIncl))));
        //System.out.println("moon ecliptic latitude: " + moonEclLat);

        Coordinates eqCoords = eclipticToEqCoords(moonEclLat, moonEclLong);
        //System.out.println("dec: " + eqCoords.getLat() + "  ra: " + eqCoords.getLongg());

        Coordinates horizonCoords = eqToHorizonCoords(eqCoords.getLat(), eqCoords.getLongg(), latitude, LST);
        DMS h = decimalToDMS(horizonCoords.getLat());
        this.altitude = h;
        System.out.print("altitude: " + h.degrees + "deg " + h.minutes + " " + h.seconds);
        DMS A = decimalToDMS(horizonCoords.getLongg());
        this.azimuth = A;
        System.out.println(" || azimuth: " + A.degrees + "deg " + A.minutes + " " + A.seconds);

        //MoonPhase phase = this.calculateMoonPhase(year, month, day);
        //System.out.println("Moon Phase: " + phase + " day: " + day);
    }


    public double HMStoDecimal(double hours, double mins, double secs){
        double decimal = hours + ((mins + (secs / 60)) / 60);
        //System.out.println("decimal: " + decimal);
        return decimal;
    }

    public DMS decimalToDMS(double val) {
        int sign = 1;
        if (val < 0){
            sign = -1;
        }
        val = Math.abs(val);
        int degrees = (int)(val);
        double frac = val - (int)(val);
        int minutes = (int)(60 * frac);
        int seconds = (int)(60 * ((60 * frac) - minutes));
        degrees = degrees * sign;
        return new DMS(degrees, minutes, seconds);
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
        //System.out.println("jd = " + jd);
        return jd;
    }

    /**
     *
     * @param time given in decimal format
     * @param longitude of observer's location
     * @return universal time in decimal format
     */
    public double LCTtoUT(double time, double longitude, boolean DST){
        if (DST) time = time - 1;
        double adjust = Math.round(longitude / 15);
        double ut = time - adjust;
        if (ut < 0){
            ut = ut + 24;
            this.day--;
            //System.out.println("previous day");
        }
        if (ut > 24) {
            ut = ut - 24;
            this.day++;
            //System.out.println("next day");
        }
        //System.out.println("UT: " + ut);
        return ut;
    }

    /**
     *
     * @param currentJD of current date
     * @param ut in decimal format
     * @return greenwich sidereal time in decimal format
     */
    public double UTtoGST(double currentJD, double ut){
        double jd0 = julianDate(year, 1, 0);
        double daysIntoYear = currentJD - jd0;
        double t = (jd0 - 2415020) / 36525;
        double r = 6.6460656 + 2400.051262 * t + 0.00002581 * Math.pow(t, 2);
        double b = 24 - r + 24 * (year - 1900);
        double t0 = 0.0657098 * daysIntoYear - b;
        double gst = t0 + 1.002738 * ut;
        if (gst < 0 ){
            gst = gst + 24;
            //this.day++;
        }
        if (gst > 24) {
            gst = gst - 24;
            //this.day--;
        }
        //System.out.println("GST: " + gst);
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
        //System.out.println("LST: " + lst);
        return lst;
    }

    public double obliquityEcliptic(){
        double jd = julianDate(2010, 1, 0);
        double centuries = (jd - 2451545) / 36525;
        double daysElapsed = 46.815 * centuries + 0.0006 * Math.pow(centuries, 2) - 0.00181 * Math.pow(centuries, 3);
        double obliquity = 23.439292 - daysElapsed / 3600;
        return obliquity;
    }

    public Coordinates eclipticToEqCoords(double lat, double longg){
        double o = Math.toRadians(obliquityEcliptic());
        lat = Math.toRadians(lat);
        longg = Math.toRadians(longg);
        //lat = Math.toRadians(1.2);
        //longg = Math.toRadians(184.6);
        double t = Math.sin(lat) * Math.cos(o) + Math.cos(lat) * Math.sin(o) * Math.sin(longg);
        double dec = Math.toDegrees(Math.asin(t));
        double y = Math.sin(longg) * Math.cos(o) - Math.tan(lat) * Math.sin(o);
        double x = Math.cos(longg);
        double r = Math.toDegrees(Math.atan(y / x));
        double ra = quadraticAdjust(y, x, r);
        Coordinates c = new Coordinates(dec, ra);
        return c;
    }

    public Coordinates eqToHorizonCoords(double dec, double ra, double observerLat, double LST) {
        double ha = LST - ra / 15;
        if (ha < 0) {
            ha += 24;
        }
        ha = Math.toRadians(ha * 15); // convert to degrees
        dec = Math.toRadians(dec);
        observerLat = Math.toRadians(observerLat);
        double t0 = Math.sin(dec) * Math.sin(observerLat) + Math.cos(dec) * Math.cos(observerLat) * Math.cos(ha);
        double h = Math.asin(t0);
        double t1 = Math.sin(dec) - Math.sin(observerLat) * Math.sin(h);
        double t2 = t1 / (Math.cos(observerLat) * Math.cos(h));
        double azimuth = Math.acos(t2);
        if (Math.sin(ha) > 0){
            azimuth = 2 * Math.PI - azimuth;
        }
        return new Coordinates(Math.toDegrees(h), Math.toDegrees(azimuth));
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
            double nextE = meanAnomaly + sunEccentricity * Math.sin(E);
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

    public double quadraticAdjust(double y, double x, double val){
        if (y >= 0 && x < 0) {
            val = val + 180;
        } else if (y < 0 && x >=0){
            val = val + 360;
        } else if (y < 0 && x < 0){
            val = val + 180;
        } else return val;

        return val;
    }

    public DMS getAltitude(){
        return altitude;
    }

    public void printAltitude(){
        System.out.println("altitude: " + altitude.degrees + "deg " + altitude.minutes + " " + altitude.seconds);
    }

    public DMS getAzimuth(){
        return azimuth;
    }

    public void printAzimuth(){
        System.out.println("azimuth: " + azimuth.degrees + "deg " + azimuth.minutes + " " + azimuth.seconds);
    }

    public double moonTopocentric(double r){
        return 0.0;
    }

    public static void main(String[] args){
        //MoonCalculator test = new MoonCalculator();
    }
}

class Coordinates {
    private double lat;
    private double longg;
    public Coordinates(double lat, double longg){
        this.lat = lat;
        this.longg = longg;
    }
    public double getLat() {
        return lat;
    }
    public double getLongg() {
        return longg;
    }
}

class DMS {
    public int degrees, minutes, seconds;
    public DMS(int d, int m, int s){
        this.degrees = d;
        this.minutes = m;
        this.seconds = s;
    }
}
