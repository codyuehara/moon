package domain;

public class MoonCalculator {
    //private final double sun's ecliptic longitude at the epoch
    private final double moonIncl = 5.1453964; //
    private final double w = 282.938346; //sun's ecliptic longitude at perigee at the epoch
    //private int year, month;
    //private double day, time;
    private double latitude, longitude;
    private static final double e = 0.016708; // eccentricity of earth sun orbit
    private int month, year;
    private double day, time;
    //private double LST;
    //private double jd, elapsedDays;
    //private double sunMA, sunTA, sunEclLong;
    private double sunEclLong, moonTEL, moonEclLat, moonEclLong;
    private Coordinates eqCoords;
    private DMS altitude, azimuth;
    private boolean DST;
    private double ra, dec;
    public double moonriseLCT, moonsetLCT;

    enum MoonPhase {
        NEW, FIRST_QUARTER, THIRD_QUARTER, FULL,
        WAXING_CRESCENT, WAXING_GIBBOUS, WANING_GIBBOUS, WANING_CRESCENT;
    }

    public MoonCalculator(double lat, double longg, boolean DST){
        this.latitude = lat;
        this.longitude = longg;
        this.DST = DST;
    }

    public void calcMoon(int m, double d, int yr, double time){
        month = m;
        day = d;
        year = yr;
        this.time = time;
        double ut = LCTtoUT(time);
        double jd = julianDate(year, month, day); //works!
        double GST = UTtoGST(jd, ut, year);
        //System.out.println("GST: " + GST);
        double LST = GSTtoLST(GST);
        //System.out.println("LST: " + LST);
        double tt = ut + (63.8 / 60 / 60);
        double fracDay = tt / 24 + day;
        double jdAdjusted = julianDate(year, month, fracDay); //works!
        //System.out.println("jd adjusted " + jdAdjusted);
        double jd0 = julianDate(2000, 1, 1.5); //julian date for standard epoch
        double elapsedDays = jdAdjusted - jd0;
        //System.out.println("elapsed days: " + elapsedDays);

        //suns ecliptic coordinates -- works
        double sunMA = 360 * elapsedDays / 365.242191 + 280.466069 - w; //deg
        if (sunMA > 360) sunMA = sunMA % 360;
        double sunEA = keplersEquation(sunMA); //rad
        double sunTA = Math.atan((Math.sqrt((1 + e) / (1 - e)) * Math.tan(sunEA / 2))) * 2; //rad
        if (sunTA > 2 * Math.PI) sunTA = sunTA % 2 * Math.PI;
        while (sunTA < 0) sunTA += 2 * Math.PI;
        sunEclLong = Math.toDegrees(sunTA) + w; //deg
        if (sunEclLong > 360) sunEclLong = sunEclLong - 360;
        //System.out.println("MA:" + sunMA);
        //System.out.println("EclLong: " + sunEclLong);

        //Moon's uncorrected mean ecliptic longitude
        double moonMEL = 13.176339686 * elapsedDays + 218.316433; //deg
        if (moonMEL > 360) moonMEL = moonMEL % 360;

        //Moon's uncorrected mean ecliptic longitude of the ascending node
        double moonMELAN = 125.044522 - 0.0529539 * elapsedDays; //deg
        if (moonMELAN > 360) moonMELAN = moonMELAN % 360;
        while (moonMELAN < 0) moonMELAN = moonMELAN + 360;
        //System.out.println("moon MEL: " + moonMEL);

        //Moon's uncorrected mean anomaly
        double moonMA = moonMEL - 0.1114041 * elapsedDays - 83.353451; //deg
        if (moonMA > 360) moonMA = moonMA % 360;
        while (moonMA < 0) moonMA = moonMA + 360;
        //System.out.println("ma: " + moonMA);

        //annual equation correction
        double ae = 0.1858 * Math.sin(Math.toRadians(sunMA));  //deg
        //System.out.println("ae: " + ae);

        //evection
        double ev = 1.2739 * Math.sin(Math.toRadians(2 * (moonMEL - sunEclLong) - moonMA)); //change to radians

        //mean anomaly corrections
        double ca = moonMA + ev - ae - 0.37 * Math.sin(Math.toRadians(sunMA)); //deg
        //System.out.println("Ca:" + ca);

        //moons true anomaly
        double moonTA = 6.2886 * Math.sin(Math.toRadians(ca)) + 0.214 * Math.sin(2 * Math.toRadians(ca)); //deg

        //corrected ecliptic longitude
        double moonCEL = moonMEL + ev + moonTA - ae; //deg

        //variation correction
        double v = 0.6583 * Math.sin(Math.toRadians(2 * (moonCEL - sunEclLong))); //deg

        //moons true ecliptic longitude
        moonTEL = moonCEL + v; //Math.toRadians(moonCEL + v); //deg
        //System.out.println("moon true ecliptic longitude: " + moonTEL);

        //corrected ecliptic longitude of the ascending node
        double moonCELAN = moonMELAN - 0.16 * Math.sin(Math.toRadians(sunMA)); //deg
        //System.out.println("corrected omega: " + moonCELAN);

        //moons ecliptic longitude
        double y = Math.sin(Math.toRadians(moonTEL) - Math.toRadians(moonCELAN)) * Math.cos(Math.toRadians(moonIncl));
        double x = Math.cos(Math.toRadians(moonTEL) - Math.toRadians(moonCELAN));
        double t = Math.atan(y / x); //rad
        moonEclLong = moonCELAN + quadraticAdjust(y, x, Math.toDegrees(t));    //deg
        if (moonEclLong > 360) {
            moonEclLong = moonEclLong - 360;
        }
        //System.out.println("moons ecliptic longitude: " + moonEclLong);

        //moons ecliptic latitude
        moonEclLat = Math.toDegrees(Math.asin(Math.sin((Math.toRadians(moonTEL) - Math.toRadians(moonCELAN))) * Math.sin(Math.toRadians(moonIncl))));
        //System.out.println("Moons ecliptic latitude: " + moonEclLat);

        eqCoords = eclipticToEqCoords(moonEclLat, moonEclLong);
        //System.out.println("dec:" + eqCoords.getLat() + " ra:" + eqCoords.getLongg());

        ra = eqCoords.getLongg();
        dec = eqCoords.getLat();

        //moonrise and moonset
        double st1r = calcRiseLST(ra, latitude, dec);
        //System.out.println("st1r: " + st1r);
        double st1s = calcSetLST(ra, latitude, dec);
        //System.out.println("st1s: " + st1s);

        //moon ecliptic lat 12 hrs later
        double newLat = moonEclLat + 0.05 * Math.cos(Math.toRadians(moonTEL - moonCELAN)) * 12;
        //System.out.println("new lat: " + newLat);
        //moon ecliptic long 12 hrs later
        double newLong = moonEclLong + (0.55 + 0.06 * Math.cos(Math.toRadians(ca))) * 12;
        if (newLong > 360) { newLong = newLong - 360; }
        //System.out.println("new long: " + newLong);

        //new eq coords
        Coordinates newEqCoords = eclipticToEqCoords(newLat, newLong);
        //System.out.println("new ra: "  + newEqCoords.getLongg() + " new dec: " + newEqCoords.getLat());

        double st2r = calcRiseLST(newEqCoords.getLongg(), latitude, newEqCoords.getLat());
        //System.out.println("st2r:" + st2r);
        double st2s = calcSetLST(newEqCoords.getLongg(), latitude, newEqCoords.getLat());
        //System.out.println("st2s: " + st2s);

        double moonrise = interpolation(st1r, st2r);
        //System.out.println("Moonrise: " + moonrise);

        double moonset = interpolation(st1s, st2s);
        //System.out.println("Moonset: " + moonset);

        double moonriseGST = LSTtoGST(moonrise);
        double moonsetGST = LSTtoGST(moonset);
        double moonriseUT = GSTtoUT(jd, moonriseGST, year);
        double moonsetUT = GSTtoUT(jd, moonsetGST, year);
        moonriseLCT = UTtoLCT(moonriseUT);
        //System.out.println("LCT of rise: " + moonriseLCT);
        moonsetLCT = UTtoLCT(moonsetUT);

        //System.out.println("LCT of set: " + moonsetLCT);
        //Domain.DMS mrHMS = decimalToDMS(moonriseLCT);
        //System.out.println("moonrise time: " + mrHMS.degrees + ":" + mrHMS.minutes + ":" + mrHMS.seconds);
        //Domain.DMS msHMS = decimalToDMS(moonsetLCT);
        //System.out.println("moonset time: " + msHMS.degrees + ":" + msHMS.minutes + ":" + msHMS.seconds);

        Coordinates horizonCoords = eqToHorizonCoords(eqCoords.getLat(), eqCoords.getLongg(), latitude, LST);
        //Domain.DMS h = decimalToDMS(horizonCoords.getLat());
        double h = decimalToDeg(horizonCoords.getLat());
        //altitude = h;
        System.out.print("altitude: " + h);
        //Domain.DMS A = decimalToDMS(horizonCoords.getLongg());
        double a = decimalToDeg(horizonCoords.getLongg());
        //azimuth = A;
        System.out.println(" || azimuth: " + a);

    }

    public double HMStoDecimal(double hours, double mins, double secs){
        double decimal = hours + ((mins + (secs / 60)) / 60);
        return decimal;
    }

    /**
     *
     * @param val
     * @return
     */
    public double decimalToDeg(double val) {
        int sign = 1;
        if (val < 0){
            sign = -1;
        }
        val = Math.abs(val);
        int degrees = (int)(val);
        double frac = val - (int)(val);
        int minutes = (int)(60 * frac);
        int seconds = (int)(60 * ((60 * frac) - minutes));
        if (seconds > 30){
            minutes += 1;
        }
        if (minutes > 30) {
            degrees += 1;
        }
        degrees = degrees * sign;
        return degrees;
        //return new Domain.DMS(degrees, minutes, seconds);
    }

    /**
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
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
        return jd;
    }

    /**
     *
     * @param time given in decimal format
     * @return universal time in decimal format
     */
    public double LCTtoUT(double time){
        if (DST) { time = time - 1; }
        double adjust = Math.round(longitude / 15);
        double ut = time - adjust;
        if (ut < 0){
            ut = ut + 24;
            this.day--;
        }
        if (ut > 24) {
            ut = ut - 24;
            this.day++;
        }
        return ut;
    }

    /**
     *
     * @param currentJD of current date
     * @param ut in decimal format
     * @return greenwich sidereal time in decimal format
     */
    public double UTtoGST(double currentJD, double ut, int year){
        double jd0 = julianDate(this.year, 1, 0);
        double daysIntoYear = currentJD - jd0;
        double t = (jd0 - 2415020) / 36525;
        double r = 6.6460656 + 2400.051262 * t + 0.00002581 * Math.pow(t, 2);
        double b = 24 - r + 24 * (year - 1900);
        double t0 = 0.0657098 * daysIntoYear - b;
        double gst = t0 + 1.002738 * ut;
        if (gst < 0 ){
            gst = gst + 24;
        }
        if (gst > 24) {
            gst = gst - 24;
        }
        return gst;
    }

    /**
     *
     * @param gst
     * @return local sidereal time in decimal format
     */
    public double GSTtoLST(double gst){
        double adjust = longitude / 15;
        double lst = gst + adjust;
        if (lst < 0 ) lst = lst + 24;
        if (lst > 24) lst = lst - 24;
        //System.out.println("LST: " + lst);
        return lst;
    }

    public double LSTtoGST(double lst){
        double adjust = longitude / 15;
        double gst = lst - adjust;
        //if (gst < 0 ) {
            //gst = gst + 24;
            //System.out.println("next day - LSTtoGST" + gst);
        //}
        //if (gst > 24) {
            //gst = gst - 24;
            //System.out.println("prev day - LSTtoGST" + gst);
        //}
        return gst;
    }

    public double GSTtoUT(double currentJD, double gst, int year){
        double jd0 = julianDate(year, 1, 0);
        double daysIntoYear = currentJD - jd0;
        double t = (jd0 - 2415020) / 36525;
        double r = 6.6460656 + 2400.051262 * t + 0.00002581 * Math.pow(t, 2);
        double b = 24 - r + 24 * (year - 1900);
        double t0 = 0.0657098 * daysIntoYear - b;
        if (t0 < 0) { t0 = t0 + 24; }
        if (t0 > 24) { t0 = t0 - 24; }
        double a = gst - t0;
        if (a < 0) { a +=24; }
        double ut = 0.997270 * a;
        return ut;
    }

    public double UTtoLCT(double ut) {
        double adjust = Math.round(longitude / 15);
        double lct = ut + adjust;
        //if (lct < 0) {
            //lct += 24;
            //System.out.println("next day - UTtoLCT " + lct);
        //}
        //if (lct > 24) {
            //lct -= 24;
            //System.out.println("prev day - UTtoLCT" + lct);
        //}
        if (DST){ lct = lct + 1; }
        return lct;
    }

    /**
     * calculates the angle of the ecliptic plane from the celestial equator
     * @return angle in degrees
     */
    public double obliquityEcliptic(){
        double jd = julianDate(2010, 1, 0);
        double centuries = (jd - 2451545) / 36525;
        double daysElapsed = 46.815 * centuries + 0.0006 * Math.pow(centuries, 2) - 0.00181 * Math.pow(centuries, 3);
        double obliquity = 23.439292 - daysElapsed / 3600;
        return obliquity;
    }

    /**
     *
     * @param lat
     * @param longg
     * @return right ascension and declination in degrees
     */
    public Coordinates eclipticToEqCoords(double lat, double longg){
        double o = Math.toRadians(obliquityEcliptic());
        lat = Math.toRadians(lat);
        longg = Math.toRadians(longg);
        double t = Math.sin(lat) * Math.cos(o) + Math.cos(lat) * Math.sin(o) * Math.sin(longg);
        double dec = Math.toDegrees(Math.asin(t));
        double y = Math.sin(longg) * Math.cos(o) - Math.tan(lat) * Math.sin(o);
        double x = Math.cos(longg);
        double r = Math.toDegrees(Math.atan(y / x));
        double ra = quadraticAdjust(y, x, r);
        Coordinates c = new Coordinates(dec, ra);
        return c;
    }

    /**
     *
     * @param dec
     * @param ra
     * @param observerLat
     * @param LST
     * @return altitude and azimuth in degrees
     */
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
     * @param meanAnomaly
     * @return eccentric anomaly in radians currently
     */
    public double keplersEquation(double meanAnomaly){
        meanAnomaly = Math.toRadians(meanAnomaly);
        double E = meanAnomaly;
        double delta = 1;
        while (delta > 0.000002) {
            double nextE = meanAnomaly + e * Math.sin(E);
            delta = Math.abs(nextE - E);
            E = nextE;
        }
        return E;
    }

    public double calcRiseLST(double ra, double observerLat, double dec){
        ra = ra / 15; //convert from degrees to hours
        double temp = Math.tan(Math.toRadians(observerLat)) * Math.tan(Math.toRadians(dec));
        //System.out.println("temp: " + temp);
        double temp2 = Math.toDegrees(Math.acos(-temp) / 15);
        //System.out.println("temp2: " + temp2);
        double riseTime = 24 + ra - temp2;
        //System.out.println("rise time: " + riseTime);
        if (riseTime > 24) { riseTime = riseTime - 24; }
        return riseTime;
    }

    public double calcSetLST(double ra, double observerLat, double dec){
        ra = ra / 15; //convert from degrees to hours
        double temp = Math.tan(Math.toRadians(observerLat)) * Math.tan(Math.toRadians(dec));
        double temp2 = Math.toDegrees(Math.acos(-temp) / 15);
        double setTime = ra + temp2;
        if (setTime > 24) { setTime = setTime - 24; }
        return setTime;
    }

    public double interpolation(double st1, double st2){
        return 12.03 * st1 / (12.03 + st1 - st2);
    }


    public MoonPhase moonPhase(){
        double a = moonTEL - sunEclLong;
        if (a > 360) { a = a % 360; }
        while (a < 0) { a += 360; }
        double moonAge = a / 12.1907;
        if (moonAge > 360) { moonAge = moonAge % 360; }
        while (moonAge < 0) { moonAge += 360; }
        System.out.println("moon age: " + a);
        double phase = (1 - Math.cos(Math.toRadians(a))) / 2;
        System.out.println("phase: " + phase);
        if (a < 22.5){
            return MoonPhase.NEW;
        } else if (a >= 22.5 && a < 67.5) {
            return MoonPhase.WAXING_CRESCENT;
        } else if (a >= 67.5 && a < 112.5){
            return MoonPhase.FIRST_QUARTER;
        } else if (a >= 112.5 && a < 157.5){
            return MoonPhase.WAXING_GIBBOUS;
        } else if (a >= 157.5 && a < 202.5) {
            return MoonPhase.FULL;
        } else if (a >= 202.5 && a < 247.5){
            return MoonPhase.WANING_GIBBOUS;
        } else if (a >= 247.5 && a < 292.5) {
            return MoonPhase.THIRD_QUARTER;
        } else if (a >= 292.5 && a < 337.5) {
            return MoonPhase.WANING_CRESCENT;
        } else {
            return MoonPhase.NEW;
        }
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

    public DMS getAltitude(){ return altitude; }

    public void printAltitude(){
        System.out.println("altitude: " + altitude.degrees + "deg " + altitude.minutes + " " + altitude.seconds);
    }

    public DMS getAzimuth(){ return azimuth; }

    public void printAzimuth(){
        System.out.println("azimuth: " + azimuth.degrees + "deg " + azimuth.minutes + " " + azimuth.seconds);
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
