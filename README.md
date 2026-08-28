# Bender's Casino

A Blackjack-themed single-page application inspired by Bender, the wisecracking robot from *Futurama*. Players create a profile, sit at the table, bet chips and play hands of Blackjack against the dealer — while Bender comments, mocks and cheers (mostly mocks) after every move.

## What it does

- Create a player and start with a fixed amount of chips.
- Place a bet and play a full hand of Blackjack: hit, stand, dealer turn, payout.
- Track a player's balance and stats (wins, losses, pushes, blackjacks) across hands.
- Reset a player's profile back to the starting balance at any time.
- React to the game in real time with Bender's in-character remarks, triggered by streaks, blackjacks, busts and going broke.

## Languages and technologies

- **Back-end:** Java 21, Spring Boot, Maven
- **Front-end:** HTML, CSS, JavaScript (vanilla SPA using the History API — no framework)
- **Architecture:** REST API consumed by the SPA, MVCS-style layering (Model / Service / Controller + DTOs). The game core is generic (`GameSession` + `GameService`) behind `/api/games/{game}/...`, with `/api/blackjack` kept as a stable alias for the current front end.
- **Data:** in-memory storage on the service layer (no database yet — login/persistence is in progress, see below)

## External API

- [Deck of Cards API](https://deckofcardsapi.com) — used to shuffle decks and draw cards for each hand.

## Project structure

The application is a single Maven project. The back-end exposes a REST API under `/api`, and the front-end SPA is served as static resources from the same origin (no CORS needed):

```
src/main/java/...          → back-end (models, services, controllers, DTOs)
src/main/resources/static  → front-end SPA (HTML/CSS/JS)
src/test/java/...          → back-end tests
```

## Running the project

Requirements: JDK 21.

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` — open it in a browser to use the SPA.

To run the back-end tests:

```bash
./mvnw test
```

## Status

Blackjack is the finished MVP. The project is now in Fase 2: extracting a game-agnostic core so
Roleta, Peixinho, Slots, Video Poker and a login system can be built on top of it without each
one duplicating the table/session code. See [docs/AGORA.md](docs/AGORA.md) for what's currently
being worked on and by whom, and [docs/Fase2-Jogos.md](docs/Fase2-Jogos.md) for the full breakdown.

## Project management and collaboration tools

- **Trello:** used for task management and sprint coordination.
- **Discord:** used for team communication and meetings.
- **Git / GitHub:** used for version control and collaboration on this repository.

## Contributors

- Marco Guimarães — [@MASG29](https://github.com/MASG29)
- Diogo Pinto — [@diogompintoo](https://github.com/diogompintoo)
- Eddie — [@Eddie-PL](https://github.com/Eddie-PL)
- Tiago Paulos — [@AquaTPPT](https://github.com/AquaTPPT)
