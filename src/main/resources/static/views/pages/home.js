export function init() {
    document.querySelector("main").innerHTML = `
    <section class="join" id="join">
      <h2>Membership Has Its Dents</h2>
      <p>Sign up for a starting stack and a dealer who remembers every bet you've lost.</p>
      <a href="#" class="btn">Pick A Name</a>
    </section>

    <section class="hero">
      <div class="hero-image">
        <img class="bender-img" src="/assets/img_1.png" alt="Robot casino dealer mascot">
      </div>
      <h1>The House Always Bends The Rules</h1>
      <div class="hero-text">
        <p>A casino floor run by a chrome-plated dealer with no interest in your winning streak.</p>
      </div>
    </section>
    `;
}
