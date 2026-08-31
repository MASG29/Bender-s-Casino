import { API_BASE_URL } from "../constants/utils.js";

export async function deal(playerId, bet) {
  const response = await fetch(`${API_BASE_URL}videopoker/deal`, {
    method: "POST",
    headers: {
      "Content-type": "application/json",
    },
    body: JSON.stringify({
      playerId: playerId,
      bet: bet,
    }),
  });

  if (!response.ok) {
    const errMessage = await response.text();
    throw new Error(errMessage);
  }

  return response.json();
}

export async function draw(handId, held) {
  const response = await fetch(
    `${API_BASE_URL}videopoker/${handId}/draw`,
    {
      method: "POST",
      headers: {
        "Content-type": "application/json",
      },
      body: JSON.stringify({
        held: held,
      }),
    },
  );

  if (!response.ok) {
    const errMessage = await response.text();
    throw new Error(errMessage);
  }

  return response.json();
}
