
function setAnchorEventListener() {
  const anchors = document.querySelectorAll("nav");
  anchors.forEach ((anchor) => {
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
    });
  });
}



function start() {
    console.log(window.location.pathname);
    setAnchorEventListener();
}

export default {start}
