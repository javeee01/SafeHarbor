SafeHarbor.requireAuth();
SafeHarbor.bindChrome("dashboard");

async function loadDashboard() {
  SafeHarbor.setStatus("dashboardStatus", "Loading dashboard...");
  try {
    const [incidentsPage, inventoryPage, sheltersPage, shortages] = await Promise.all([
      SafeHarbor.apiFetch("/api/incidents?size=5&sort=reportedAt,desc"),
      SafeHarbor.apiFetch("/api/inventory?size=100"),
      SafeHarbor.apiFetch("/api/shelters?size=100"),
      SafeHarbor.apiFetch("/api/inventory/shortages")
    ]);

    const incidents = SafeHarbor.pageContent(incidentsPage);
    const inventory = SafeHarbor.pageContent(inventoryPage);
    const shelters = SafeHarbor.pageContent(sheltersPage);

    document.getElementById("incidentCount").textContent = incidentsPage.totalElements ?? incidents.length;
    document.getElementById("inventoryCount").textContent = inventoryPage.totalElements ?? inventory.length;
    document.getElementById("shelterCount").textContent = shelters.filter(SafeHarbor.isActive).length;

    document.getElementById("recentIncidents").innerHTML = incidents.length
      ? incidents.map((incident) => `
          <div class="priority-item">
            <div><strong>${incident.title}</strong><span class="muted">${incident.incidentType} at ${incident.latitude}, ${incident.longitude}</span></div>
            ${SafeHarbor.badge(incident.status)}
          </div>
        `).join("")
      : `<div class="empty-state">No incidents reported.</div>`;

    document.getElementById("shortages").innerHTML = shortages.length
      ? shortages.map((item) => `
          <div class="priority-item">
            <div><strong>${item.itemName}</strong><span class="muted">${item.availableQuantity} ${item.unit} available</span></div>
            ${SafeHarbor.badge("CRITICAL")}
          </div>
        `).join("")
      : `<div class="empty-state">No critical shortages.</div>`;

    SafeHarbor.setStatus("dashboardStatus", "Dashboard updated.", "success");
  } catch (error) {
    SafeHarbor.setStatus("dashboardStatus", error.message, "error");
  }
}

loadDashboard();
