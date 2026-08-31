import routes, { gamesList } from "./routes.js";
import state from "/js/state.js";

const PROTECTED = ["/lobby", "/profile", ...gamesList.map((game) => `/${game.id}`)];

function navigate(path, firstLoad = false) {
    if (path == routes.currentPath.path) return;

    if (PROTECTED.includes(path) && !state.isLoggedIn()) {
        path = "/";
    }

    const routeKey = Object.keys(routes).find(
        (key) => routes[key].path === path,
    );

    const route = routes[routeKey] || routes.home;

    setCurrentRoute(route);

    firstLoad
        ? history.replaceState(route, "", route.path)
        : history.pushState(route, "", route.path);

    launchController(route.controller);
}

function handlePopState(event) {
    
    const { state: routeState } = event;
    const route = routeState || routes.home;
    setCurrentRoute(route);
    launchController(route.controller);
}

function setAnchorEventListener() {
    const anchors = document.querySelectorAll("nav a");

    anchors.forEach((anchor) => {
        anchor.addEventListener("click", (event) => {
            event.preventDefault();
            navigate(anchor.pathname);
        });
    });
}

async function launchController(controllerName) {
    try {
        const controllerModule = await import(
            `./views/pages/${controllerName}.js`
            );
        controllerModule.init();
    } catch (error) {
        console.error(error);
    }
}

function setCurrentRoute(route) {
    routes.currentPath.path = route.path;
    routes.currentPath.controller = route.controller;
    updateTableLink(route.path);
}

function updateTableLink(path) {
    const tableLink = document.getElementById("table-link");
    if (!tableLink) return;
    const isGameRoute = gamesList.some((game) => `/${game.id}` === path);
    tableLink.setAttribute("href", isGameRoute ? path : "/lobby");
}

function start() {
    setAnchorEventListener();
    addEventListener("popstate", handlePopState);
    navigate(window.location.pathname, true);
}

export default { start, navigate };
