const games = [
    { id: "blackjack", label: "Blackjack", live: true },
    { id: "roulette", label: "Roulette", live: true },
    { id: "peixinho", label: "Peixinho", live: true },
];

const gameRoutes = Object.fromEntries(
    games.map((game) => [
        game.id,
        {
            path: `/${game.id}`,
            controller: game.id,
            label: game.label,
            live: game.live
        },
    ]),
);

export const gamesList = games;

export default {
    home: {
        path: "/",
        controller: "home",
    },

    lobby: {
        path: "/lobby",
        controller: "lobby",
    },

    ...gameRoutes,

    profile: {
        path: "/profile",
        controller: "profile",
    },

    currentPath: {
        path: "",
        controller: "",
    },
};