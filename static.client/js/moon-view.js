import {MoonCalculator} from "./moon-calculator.js";

export class MoonView {
    m;
    month;
    day;
    year;

    constructor() {
        //this.m = new MoonCalculator(34, -118, true);
        //this.m = new MoonCalculator(30, -95, true);
        let count = 0;
        this.submitBtnEl = document.getElementById('submit-btn');
        this.submitBtnEl.addEventListener('click', (e) => {
            console.log("Click");
            const latEl = document.getElementById('lat');
            const lat = latEl.value;
            const longEl = document.getElementById('long');
            const long = longEl.value;
            const dstEl = document.getElementById('dst');
            const dst = dstEl.value;
            this.m = new MoonCalculator(lat, long, dst);
            this.getDate();
            this.drawChart();
            this.moonPhase();
            count++;

            //this.calculateMoon();
        });

    }

    moonPhase() {
        this.m.calcMoon(this.month, this.day, this.year, 12);
        const phase = document.getElementById('moon-phase');
        phase.innerHTML = "Moon Phase: " + this.m.moonPhase();
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

        for (let i = 0; i < 1440; i++) {
            const ut = i / 60;
            this.m.calcMoon(this.month, this.day, this.year, ut);
            time.push(ut);
            altitude.push(this.m.altitude);
            azimuth.push(this.m.azimuth);
        }
        //CONVERT UT TO LCT
        console.log(altitude);
        var ctx = document.getElementById('myChart').getContext('2d');
        var myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: time,
                datasets: [{
                    label: 'altitude',
                    data: altitude,
                    fill: false,
                    borderColor: 'rgb(255, 255, 255)'
                }]
            },
            options: {
                labels: {

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
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });

    }

    resetCanvas(){
        $('myChart').remove();

    }
}