const STORAGE_PREFIX = "tutorialSeen:";

/**
 * Mounts a "how to play" modal + floating "?" button for a game, and
 * auto-opens it the first time the player visits that game.
 */
export function initTutorial({ game, title, body }) {
  document.querySelectorAll(".tutorial-btn, .tutorial-modal").forEach((el) => el.remove());

  const modalId = `tutorial-modal-${game}`;
  document.body.insertAdjacentHTML(
    "beforeend",
    `
    <div class="modal-backdrop tutorial-modal" id="${modalId}">
      <div class="modal-box">
        <button type="button" class="modal-close" data-tutorial-close aria-label="Close">✕</button>
        <h2 class="modal-title">${title}</h2>
        <div class="modal-hint tutorial-body">${body}</div>
      </div>
    </div>
    <button type="button" class="tutorial-btn" aria-label="How to play">?</button>
    `,
  );

  const modal = document.getElementById(modalId);
  const button = document.querySelector(".tutorial-btn");

  function open() {
    modal.classList.add("open");
  }

  function close() {
    modal.classList.remove("open");
  }

  button.addEventListener("click", open);
  modal.querySelector("[data-tutorial-close]").addEventListener("click", close);
  modal.addEventListener("click", (e) => {
    if (e.target === modal) close();
  });

  const key = STORAGE_PREFIX + game;
  if (!localStorage.getItem(key)) {
    open();
    localStorage.setItem(key, "true");
  }

  return { open, close };
}
