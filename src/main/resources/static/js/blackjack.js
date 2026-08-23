import { element, button, stylizedButton } from "./constants/element.js";

const body = document.querySelector("#App");

export function render() {
  const ui = element("div", body, ["blackjack-ui"]);
  createUiButtons(ui);
}

function createUiButtons(parent) {
  const hit = stylizedButton(parent, "Hit");
  const stand = stylizedButton(parent, "Stand");
}
