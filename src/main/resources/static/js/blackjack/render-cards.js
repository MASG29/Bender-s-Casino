export function renderPlayerCards() {
  playerCardsContainer.innerHTML = "";
  gameState.playerHand.cards.forEach((c) => {
    const cardEl = element("div", playerCardsContainer);
    element("p", cardEl).textContent = c.value + " of " + c.suit;
  });
}

export function renderDealerCards() {
  dealerCardsContainer.innerHTML = "";
  gameState.dealerHand.cards.forEach((c) => {
    const cardEl = element("div", dealerCardsContainer);
    element("p", cardEl).textContent = c.value + " of " + c.suit;
  });
}
