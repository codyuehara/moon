
export class MoonCalculator {
    altitude;
    azimuth;
    day;
    moonTEL;
    sunEclLong;

    constructor(latitude, longitude, DST) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.DST = DST;
        //sun's ecliptic longitude at the epoch
    }

    calcMoon(m, d, yr, time) {
        const moonIncl = 5.1453964; //
        const w = 282.938346; //sun's ecliptic longitude at perigee at the epoch
        const e = 0.016708; // eccentricity of earth sun orbit
        let month = m;
        this.day = d;
        let year = yr;
        this.time = time;
        let ut = this.LCTtoUT(time);
        let jd = this.julianDate(year, month, this.day); //works!
        let GST = this.UTtoGST(jd, ut, year);
        let LST = this.GSTtoLST(GST);
        let tt = ut + (63.8 / 60 / 60);
        let fracDay = tt / 24 + this.day;
        let jdAdjusted = this.julianDate(year, month, fracDay); //works!
        let jd0 = this.julianDate(2000, 1, 1.5); //julian date for standard epoch
        let elapsedDays = jdAdjusted - jd0;

        //console.log("elapsed days: " + elapsedDays);

        //suns ecliptic coordinates -- works
        let sunMA = 360 * elapsedDays / 365.242191 + 280.466069 - w; //deg
        if (sunMA > 360) sunMA = sunMA % 360;
        let sunEA = this.keplersEquation(sunMA, e); //rad
        //console.log(sunEA);
        let sunTA = Math.atan((Math.sqrt((1 + e) / (1 - e)) * Math.tan(sunEA / 2))) * 2; //rad
        if (sunTA > 2 * Math.PI) sunTA = sunTA % 2 * Math.PI;
        while (sunTA < 0) sunTA += 2 * Math.PI;
        let sunEclLong = this.toDegrees(sunTA) + w; //deg
        if (sunEclLong > 360) sunEclLong = sunEclLong - 360;
        //console.log("sun ecl long: " + sunEclLong);

        //Moon's uncorrected mean ecliptic longitude
        let moonMEL = 13.176339686 * elapsedDays + 218.316433; //deg
        if (moonMEL > 360) moonMEL = moonMEL % 360;

        //Moon's uncorrected mean ecliptic longitude of the ascending node
        let moonMELAN = 125.044522 - 0.0529539 * elapsedDays; //deg
        if (moonMELAN > 360) moonMELAN = moonMELAN % 360;
        while (moonMELAN < 0) moonMELAN = moonMELAN + 360;

        //Moon's uncorrected mean anomaly
        let moonMA = moonMEL - 0.1114041 * elapsedDays - 83.353451; //deg
        if (moonMA > 360) moonMA = moonMA % 360;
        while (moonMA < 0) moonMA = moonMA + 360;

        //annual equation correction
        let ae = 0.1858 * Math.sin(this.toRadians(sunMA));  //deg

        //evection
        let ev = 1.2739 * Math.sin(this.toRadians(2 * (moonMEL - sunEclLong) - moonMA)); //change to radians

        //mean anomaly corrections
        let ca = moonMA + ev - ae - 0.37 * Math.sin(this.toRadians(sunMA)); //deg

        //moons true anomaly
        let moonTA = 6.2886 * Math.sin(this.toRadians(ca)) + 0.214 * Math.sin(2 * this.toRadians(ca)); //deg

        //corrected ecliptic longitude
        let moonCEL = moonMEL + ev + moonTA - ae; //deg

        //variation correction
        let v = 0.6583 * Math.sin(this.toRadians(2 * (moonCEL - sunEclLong))); //deg

        //moons true ecliptic longitude
        let moonTEL = moonCEL + v; //Math.toRadians(moonCEL + v); //deg

        //corrected ecliptic longitude of the ascending node
        let moonCELAN = moonMELAN - 0.16 * Math.sin(this.toRadians(sunMA)); //deg

        //moons ecliptic longitude
        let y = Math.sin(this.toRadians(moonTEL) - this.toRadians(moonCELAN)) * Math.cos(this.toRadians(moonIncl));
        let x = Math.cos(this.toRadians(moonTEL) - this.toRadians(moonCELAN));
        let t = Math.atan(y / x); //rad
        let moonEclLong = moonCELAN + this.quadraticAdjust(y, x, this.toDegrees(t));    //deg
        if (moonEclLong > 360) {
            moonEclLong = moonEclLong - 360;
        }

        //moons ecliptic latitude
        let moonEclLat = this.toDegrees(Math.asin(Math.sin((this.toRadians(moonTEL) - this.toRadians(moonCELAN))) * Math.sin(this.toRadians(moonIncl))));
        let eqCoords = this.eclipticToEqCoords(moonEclLat, moonEclLong);
        let dec = eqCoords[0];
        let ra = eqCoords[1];
        //console.log("ra: " + ra + " || dec: " + dec);

        let horizonCoords = this.eqToHorizonCoords(dec, ra, this.latitude, LST);
        let h = this.decimalToDeg(horizonCoords[0]);
        this.altitude = h;
        let a = this.decimalToDeg(horizonCoords[1]);
        this.azimuth = a;
        //console.log("altitude: " + h + " || azimuth: " + a);

    }

    HMStoDecimal(hours, mins, secs) {
        return hours + ((mins + (secs / 60)) / 60);
    }

    /**
     *
     * @param val
     * @return
     **/
    decimalToDeg(val) {
        let sign = 1;
        if (val < 0) {
            sign = -1;
        }
        val = Math.abs(val);
        let degrees = Math.trunc(val);
        let frac = val - Math.trunc(val);
        let minutes = Math.trunc(60 * frac);
        let seconds = Math.trunc(60 * ((60 * frac) - minutes));
        if (seconds > 30) {
            minutes += 1;
        }
        if (minutes > 30) {
            degrees += 1;
        }
        degrees = degrees * sign;
        return degrees;
    }

    /**
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    julianDate(year, month, day) {
        if (month <= 2) {
            month = month + 12;
            year = year - 1;
        }
        let t = 0;
        if (year < 0) {
            t = 0.75;
        }
        let a = year / 100;
        let b = 2 - a + a / 4;
        return b + Math.trunc(365.25 * year - t) + Math.trunc(30.6001 * (month + 1)) + day + 1720994.5;
    }

    /**
     *
     * @param time given in decimal format
     * @return universal time in decimal format
     */

    LCTtoUT(time) {
        if (this.DST) {
            time = time - 1;
        }
        let adjust = Math.round(this.longitude / 15);
        let ut = time - adjust;
        if (ut < 0) {
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
     * @param year
     * @return greenwich sidereal time in decimal format
     */

    UTtoGST(currentJD, ut, year) {
        let jd0 = this.julianDate(year, 1, 0);
        let daysIntoYear = currentJD - jd0;
        let t = (jd0 - 2415020) / 36525;
        let r = 6.6460656 + 2400.051262 * t + 0.00002581 * Math.pow(t, 2);
        let b = 24 - r + 24 * (year - 1900);
        let t0 = 0.0657098 * daysIntoYear - b;
        let gst = t0 + 1.002738 * ut;
        if (gst < 0) {
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

    GSTtoLST(gst) {
        let adjust = this.longitude / 15;
        let lst = gst + adjust;
        if (lst < 0) lst = lst + 24;
        if (lst > 24) lst = lst - 24;
        return lst;
    }

    UTtoLCT(ut){

    }


    /**
     * calculates the angle of the ecliptic plane from the celestial equator
     * @return angle in degrees
     */
    obliquityEcliptic() {
        let jd = this.julianDate(2010, 1, 0);
        let centuries = (jd - 2451545) / 36525;
        let daysElapsed = 46.815 * centuries + 0.0006 * Math.pow(centuries, 2) - 0.00181 * Math.pow(centuries, 3);
        return 23.439292 - daysElapsed / 3600;
    }

    /**
     *
     * @param lat
     * @param longg
     * @return right ascension and declination in degrees
     */
    eclipticToEqCoords(lat, longg) {
        let o = this.toRadians(this.obliquityEcliptic());
        lat = this.toRadians(lat);
        longg = this.toRadians(longg);
        let t = Math.sin(lat) * Math.cos(o) + Math.cos(lat) * Math.sin(o) * Math.sin(longg);
        let dec = this.toDegrees(Math.asin(t));
        let y = Math.sin(longg) * Math.cos(o) - Math.tan(lat) * Math.sin(o);
        let x = Math.cos(longg);
        let r = this.toDegrees(Math.atan(y / x));
        let ra = this.quadraticAdjust(y, x, r);
        return [dec, ra];
    }

    /**
     *
     * @param dec
     * @param ra
     * @param observerLat
     * @param LST
     * @return altitude and azimuth in degrees
     */
    eqToHorizonCoords(dec, ra, observerLat, LST) {
        let ha = LST - ra / 15;
        if (ha < 0) {
            ha += 24;
        }
        ha = this.toRadians(ha * 15); // convert to degrees
        dec = this.toRadians(dec);
        observerLat = this.toRadians(observerLat);
        let t0 = Math.sin(dec) * Math.sin(observerLat) + Math.cos(dec) * Math.cos(observerLat) * Math.cos(ha);
        let h = Math.asin(t0);
        let t1 = Math.sin(dec) - Math.sin(observerLat) * Math.sin(h);
        let t2 = t1 / (Math.cos(observerLat) * Math.cos(h));
        let azimuth = Math.acos(t2);
        if (Math.sin(ha) > 0) {
            azimuth = 2 * Math.PI - azimuth;
        }
        return [this.toDegrees(h), this.toDegrees(azimuth)];
    }

    /**
     *
     * @param meanAnomaly
     * @param e eccentricity of sun earth orbit
     * @return eccentric anomaly in radians currently
     */
    keplersEquation(meanAnomaly, e) {
        meanAnomaly = this.toRadians(meanAnomaly);
        let E = meanAnomaly;
        let delta = 1;
        while (delta > 0.000002) {
            let nextE = meanAnomaly + e * Math.sin(E);
            delta = Math.abs(nextE - E);
            E = nextE;
        }
        return E;
    }

    moonPhase() {
        let a = this.moonTEL - this.sunEclLong;
        if (a > 360) {
            a = a % 360;
        }
        while (a < 0) {
            a += 360;
        }
        console.log(a);
        let moonAge = a / 12.1907;
        if (moonAge > 360) {
            moonAge = moonAge % 360;
        }
        while (moonAge < 0) {
            moonAge += 360;
        }
        //let phase = (1 - Math.cos(this.toRadians(a))) / 2;
        if (a < 22.5) {
            return "New";
        } else if (a >= 22.5 && a < 67.5) {
            return "Waxing Crescent";
        } else if (a >= 67.5 && a < 112.5) {
            return "First Quarter";
        } else if (a >= 112.5 && a < 157.5) {
            return "Waxing Gibbous";
        } else if (a >= 157.5 && a < 202.5) {
            return "Full";
        } else if (a >= 202.5 && a < 247.5) {
            return "Waning Gibbous";
        } else if (a >= 247.5 && a < 292.5) {
            return "Third Quarter";
        } else if (a >= 292.5 && a < 337.5) {
            return "Waning Crescent";
        } else {
            return "New";
        }
    }

    quadraticAdjust(y, x, val) {
        if (y >= 0 && x < 0) {
            val = val + 180;
        } else if (y < 0 && x >= 0) {
            val = val + 360;
        } else if (y < 0 && x < 0) {
            val = val + 180;
        } else return val;
        return val;
    }

    toDegrees(val) {
        let pi = Math.PI;
        return val * (180 / pi);
    }

    toRadians(val) {
        return val * (Math.PI / 180);
    }
}


