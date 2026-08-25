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

export function move(el, aPos, bPos) {
  console.log(aPos.left);
  const deltaX = aPos.left - bPos.left + 40;
  const deltaY = aPos.top - bPos.top + 23;
  console.log(deltaX);
  console.log(deltaY);

  el.style.transform = `translate(0px, 0px)`;
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      el.style.transform = `translate(${deltaX}px, ${deltaY}px)`;
    });
  });
}
