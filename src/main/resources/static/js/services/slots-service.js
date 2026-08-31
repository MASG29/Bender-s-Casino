import { API_BASE_URL } from "../constants/utils.js";

export async function roll(playerId, betAmount) {
    const response = await fetch(`${API_BASE_URL}slots/roll`, {
        method: "POST",
        headers: {
            "Content-type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
            playerId: playerId,
            betAmount: betAmount,
        }),
    });

    if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.message || `Spin failed (${response.status})`);
    }

    return response.json();
}
