const SafeHarbor = (() => {
  const BASE_URL = "https://safeharbor-api-kyao.onrender.com";
  const tokenKey = "safeharbor.token";
  const userKey = "safeharbor.user";

  function getToken() {
    return localStorage.getItem(tokenKey);
  }

  function getUser() {
    const raw = localStorage.getItem(userKey);
    return raw ? JSON.parse(raw) : null;
  }

  function saveSession(auth) {
    localStorage.setItem(tokenKey, auth.accessToken);
    localStorage.setItem(userKey, JSON.stringify({
      username: auth.username,
      role: auth.role,
      fullName: auth.fullName
    }));
  }

  function clearSession() {
    localStorage.removeItem(tokenKey);
    localStorage.removeItem(userKey);
    window.location.href = "login.html";
  }

  function requireAuth() {
    if (!getToken()) {
      window.location.href = "login.html";
    }
  }

  async function apiFetch(path, options = {}) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;

    const response = await fetch(`${BASE_URL}${path}`, { ...options, headers });
    if (response.status === 401 || response.status === 403) {
      clearSession();
      throw new Error("Please sign in again.");
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
      throw new Error(data?.message || data?.error || "Request failed.");
    }
    return data;
  }

  function pageContent(page) {
    if (Array.isArray(page)) return page;
    return page?.content || [];
  }

  function setStatus(id, message, type = "") {
    const node = document.getElementById(id);
    if (!node) return;
    node.textContent = message;
    node.className = `status ${type}`.trim();
  }

  function formJson(form, numberFields = []) {
    const payload = Object.fromEntries(new FormData(form).entries());
    numberFields.forEach((field) => {
      if (payload[field] !== undefined && payload[field] !== "") {
        payload[field] = Number(payload[field]);
      }
    });
    Object.keys(payload).forEach((key) => {
      if (payload[key] === "") payload[key] = null;
    });
    return payload;
  }

  function badge(value) {
    const text = value || "UNKNOWN";
    const good = ["DELIVERED", "RESOLVED", "ACTIVE"].includes(text);
    const bad = ["CANCELLED", "CRITICAL", "INACTIVE"].includes(text);
    const warn = ["REPORTED", "ASSIGNED", "IN_TRANSIT", "PENDING_APPROVAL"].includes(text);
    const cls = good ? "good" : bad ? "bad" : warn ? "warn" : "";
    return `<span class="badge ${cls}">${text}</span>`;
  }

  function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString();
  }

  function isActive(record) {
    return record?.active ?? record?.isActive ?? false;
  }

  function bindChrome(activePage) {
    document.querySelectorAll("[data-nav]").forEach((link) => {
      if (link.dataset.nav === activePage) link.classList.add("active");
    });
    document.querySelectorAll("[data-logout]").forEach((button) => {
      button.addEventListener("click", clearSession);
    });
    const user = getUser();
    const userNode = document.querySelector("[data-user]");
    if (userNode && user) {
      userNode.textContent = `${user.fullName || user.username} (${user.role})`;
    }
  }

  return {
    apiFetch,
    badge,
    bindChrome,
    clearSession,
    formatDate,
    formJson,
    getUser,
    isActive,
    pageContent,
    requireAuth,
    saveSession,
    setStatus
  };
})();
