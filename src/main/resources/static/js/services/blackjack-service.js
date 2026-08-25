import { API_BASE_URL } from "../constants/utils.js";

export async function startGame(playerId, betAmount) {
  const response = await fetch(`${API_BASE_URL}blackjack/start`, {
    method: "POST",
    headers: {
      "Content-type": "application/json",
    },
    body: JSON.stringify({
      playerId: playerId,
      bet: betAmount,
    }),
  });

  if (!response.ok) {
    const errMessage = await response.text();
    throw new Error(errMessage);
  }

  return response.json();
}

export async function playerHit(playerId) {
  const response = await fetch(`${API_BASE_URL}blackjack/hit`, {
    method: "POST",
    headers: {
      "Content-type": "application/json",
    },
    body: JSON.stringify({
      playerId: playerId,
    }),
  });

  if (!response.ok) {
    const errMessage = await response.text();
    throw new Error(errMessage);
  }

  return response.json();
}

export async function playerStand(playerId) {
  const response = await fetch(`${API_BASE_URL}blackjack/stand`, {
    method: "POST",
    headers: {
      "Content-type": "application/json",
    },
    body: JSON.stringify({
      playerId: playerId,
    }),
  });

  if (!response.ok) {
    const errMessage = await response.text();
    throw new Error(errMessage);
  }

  return response.json();
}

export async function getState(playerId) {
  const response = await fetch(`${API_BASE_URL}blackjack/state/${playerId}`);

  if (!response.ok) {
    const errMessage = await response.text();
    throw new Error(errMessage);
  }

  return response.json();
}
