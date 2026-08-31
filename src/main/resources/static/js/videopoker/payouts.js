export const PAYOUTS = [
  { name: "Royal Flush", example: ["AS", "KS", "QS", "JS", "0S"] },
  { name: "Straight Flush", example: ["5H", "6H", "7H", "8H", "9H"] },
  { name: "Four of a Kind", example: ["7S", "7H", "7D", "7C", "2S"] },
  { name: "Full House", example: ["KH", "KS", "KD", "4C", "4H"] },
  { name: "Flush", example: ["2D", "5D", "9D", "JD", "KD"] },
  { name: "Straight", example: ["4S", "5H", "6D", "7C", "8S"] },
  { name: "Three of a Kind", example: ["9S", "9H", "9D", "3C", "5D"] },
  { name: "Two Pairs", example: ["JS", "JH", "8D", "8C", "2S"] },
  { name: "Jacks or Better", example: ["JS", "JH", "4D", "6C", "9S"] },
];

export function cardImageUrl(code) {
  return `https://deckofcardsapi.com/static/img/${code}.png`;
}
