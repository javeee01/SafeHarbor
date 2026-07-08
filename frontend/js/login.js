const loginTab = document.getElementById("loginTab");
const registerTab = document.getElementById("registerTab");
const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

function showForm(name) {
  const registering = name === "register";
  loginForm.hidden = registering;
  registerForm.hidden = !registering;
  loginTab.classList.toggle("active", !registering);
  registerTab.classList.toggle("active", registering);
  SafeHarbor.setStatus("authStatus", "");
}

loginTab.addEventListener("click", () => showForm("login"));
registerTab.addEventListener("click", () => showForm("register"));

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  SafeHarbor.setStatus("authStatus", "Signing in...");
  try {
    const auth = await SafeHarbor.apiFetch("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(SafeHarbor.formJson(loginForm))
    });
    SafeHarbor.saveSession(auth);
    window.location.href = "dashboard.html";
  } catch (error) {
    SafeHarbor.setStatus("authStatus", error.message, "error");
  }
});

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  SafeHarbor.setStatus("authStatus", "Creating account...");
  try {
    const auth = await SafeHarbor.apiFetch("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(SafeHarbor.formJson(registerForm))
    });
    SafeHarbor.saveSession(auth);
    window.location.href = "dashboard.html";
  } catch (error) {
    SafeHarbor.setStatus("authStatus", error.message, "error");
  }
});
