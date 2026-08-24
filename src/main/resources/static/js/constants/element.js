export function element(type, parent, classes, text = "") {
  const el = document.createElement(type);
  if (classes != undefined) {
    for (const style of classes) {
      el.classList.add(style);
    }
  }
  el.textContent = text;
  parent.appendChild(el);
  return el;
}

export function button(parent, text, classes) {
  const button = element("button", parent, classes, text);
  button.textContent = text;
  return button;
}

export function stylizedButton(parent, text) {
  const but = button(parent);
  const span = element("span", but, ["button_top"], text);
  return but;
}
