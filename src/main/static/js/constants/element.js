
export function element(type, parent, classes) {
    const el = document.createElement(type);
    if (classes != undefined) {
        for (const style of classes) {
            el.classList.add(style);
        }
    }
    parent.appendChild(el);
    return el
}