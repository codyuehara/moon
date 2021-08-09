export class MoonView {

    constructor() {
        this.submitBtnEl = document.getElementById('submit-btn');
        this.submitBtnEl.addEventListener('click', (e) => {
            console.log("Click");
            this.calculateMoon();
        })


    }

    calculateMoon() {
        const date = document.getElementById('date');
        date.innerHTML = "";
    }
}