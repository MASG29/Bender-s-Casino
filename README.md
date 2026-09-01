# Bender's Casino

A Futurama-themed online casino built as a single-page application, hosted by Bender — the wisecracking, chrome-plated dealer who runs the floor, cracks jokes and never lets you forget you're playing against the house. Create an account, sit at any of five tables, and try to leave with more chips than you came with.

## Games

- **Blackjack** — hit, stand, dealer plays to 17, natural blackjacks and pushes handled, with Bender reacting in character to streaks, busts and going broke.
- **Roulette** — straight, split and outside bets (odd/even, red/black, high/low), full payout table.
- **Peixinho** — a "books" card game where you ask other players for cards to complete sets, playable against Bender's bots.
- **Slots** — three-reel slot machine with its own symbol/payout table.
- **Video Poker** — five-card draw with hold/discard and a standard paytable.

## What it does

- Register and log in with an account (nickname or email + password), persisted in a database.
- Sit at any live table and play with real chips tied to your account balance.
- Track wins, losses, pushes and blackjacks across hands from your profile page.
- Reset your profile back to the starting balance at any time.
- React to the game in real time with Bender's in-character remarks.

## Languages and technologies

- **Back-end:** Java 21, Spring Boot, Maven, Spring Security (BCrypt-hashed passwords), Spring Data JPA
- **Database:** H2 (file-based), accessed through JPA/Hibernate
- **Front-end:** HTML, CSS, JavaScript (vanilla SPA using the History API — no framework)
- **Architecture:** REST API consumed by the SPA, MVCS-style layering (Model / Service / Controller + DTOs). Each game plugs into a generic core (`GameSession` + `GameService`) behind `/api/games/{game}/...`, and lives in its own subpackage (`model/blackjack`, `service/roleta`, `controller/slots`, etc.) alongside the classes shared by every game.

## External API

- [Deck of Cards API](https://deckofcardsapi.com) — used to shuffle decks and draw cards for Blackjack, Peixinho and Video Poker.

## Project structure

The application is a single Maven project. The back-end exposes a REST API under `/api`, and the front-end SPA is served as static resources from the same origin (no CORS needed):

```
src/main/java/...          → back-end (models, services, controllers, DTOs), one subpackage per game
src/main/resources/static  → front-end SPA (HTML/CSS/JS, one page + service per game)
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

## Project management and collaboration tools

- **Linear:** used for task management and sprint coordination.
- **Discord:** used for team communication and meetings.
- **Git / GitHub:** used for version control and collaboration on this repository.

## Contributors

- Marco Guimarães — [@MASG29](https://github.com/MASG29)
- Diogo Pinto — [@diogompintoo](https://github.com/diogompintoo)
- Eddie — [@Eddie-PL](https://github.com/Eddie-PL)
- Tiago Paulos — [@AquaTPPT](https://github.com/AquaTPPT)
