import {MoonCalculator} from "./moon-calculator.js";

export class MoonView {
    m;
    month;
    day;
    year;
    myChart;

    constructor() {
        let count = 0;
        this.submitBtnEl = document.getElementById('submit-btn');
        this.submitBtnEl.addEventListener('click', (e) => {
            console.log("Click");
            if (this.myChart != null){
                this.myChart.destroy();
            }
            const latEl = document.getElementById('lat');
            const lat = latEl.value;
            const longEl = document.getElementById('long');
            const long = longEl.value;
            const dstEl = document.getElementById('dst');
            const dst = dstEl.value;
            const error = document.getElementById('error');
            if (lat < -180 || lat > 180 || long < -180 || long > 180){
                error.innerHTML = "Input Error";
            } else {
                error.innerHTML = "";
                this.m = new MoonCalculator(lat, long, dst);
                this.getDate();
                this.drawChart();
                this.moonPhase();
            }

            count++;
        });

    }

    moonPhase() {
        this.m.calcMoon(this.month, this.day, this.year, 12);
        const phase = document.getElementById('moon-phase');
        phase.innerHTML = "Moon Phase: " + this.m.moonPhase();
        const illumination = document.getElementById('illumination');
        illumination.innerHTML = "Illumination: " + this.m.illumination + "%";
    }


    getDate(){
        let currentDate = new Date();
        this.day = currentDate.getDate();
        this.month = currentDate.getMonth() + 1;
        this.year = currentDate.getFullYear();
        const date = document.getElementById('date');
        date.innerHTML = "Date: " + this.month + "/" + this.day + "/" + this.year;
    }

    drawChart() {
        const altitude = [];
        const azimuth = [];
        const time = [];
        let hr = 12;
        let timeOfDay = "am";
        let mins = -1;

        for (let i = 0; i < 1440; i++) {
            const ut = i / 60;
            this.m.calcMoon(this.month, this.day, this.year, ut);
            if (hr == 13){ hr = 1;}
            mins++;
            if (mins == 60){
                mins = 0;
                hr += 1;
            }
            if (i === 719){ timeOfDay = "pm"; }
            if (mins < 10){
                let singleDigit = "0" + mins;
                time.push(hr + ":" + singleDigit + " " + timeOfDay);
            } else {
                time.push(hr + ":" + mins + " " + timeOfDay);
            }
            altitude.push(this.m.altitude);
            azimuth.push(this.m.azimuth);
        }
        //CONVERT UT TO LCT
        var ctx = document.getElementById('myChart').getContext('2d');
        this.myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: time,
                datasets: [{
                    label: 'altitude',
                    data: altitude,
                    fill: false,
                    backgroundColor: 'rgb(255, 255, 255)',
                    borderColor: 'rgb(255, 255, 255)',
                    borderWidth: 0
                }],
            },
            options: {
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index'
                    }
                },
                scales: {
                    yAxis: {
                        ticks: {
                            callback: function(value, index, values){
                                return value + '\xB0'
                            }
                        }
                    },
                    xAxis: {
                        display: false,
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

}