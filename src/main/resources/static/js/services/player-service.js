import { API_BASE_URL } from "../constants/utils.js";

export async function createPlayer(name) {
  const response = await fetch(`${API_BASE_URL}players`, {
    method: "POST",
    headers: {
      "Content-type": "application/json",
    },
    body: JSON.stringify({
      name: name,
    }),
  });

  if (!response.ok) {
    const err = await response.text();
    throw new Error(err);
  }

  const text = await response.text();
  console.log(text);
  return response;
}
