import {Client} from "./client.js";

// call the main function when the DOM is loaded
window.addEventListener('load', main);

export let client = null;

export function main() {
    client = new Client();
}