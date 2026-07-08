SafeHarbor.requireAuth();
SafeHarbor.bindChrome("incidents");

const incidentForm = document.getElementById("incidentForm");
let incidents = [];

async function loadIncidents() {
  SafeHarbor.setStatus("incidentStatus", "Loading incidents...");
  try {
    const page = await SafeHarbor.apiFetch("/api/incidents?size=100&sort=reportedAt,desc");
    incidents = SafeHarbor.pageContent(page);
    renderIncidents();
    SafeHarbor.setStatus("incidentStatus", "Incidents loaded.", "success");
  } catch (error) {
    SafeHarbor.setStatus("incidentStatus", error.message, "error");
  }
}

function renderIncidents() {
  const rows = document.getElementById("incidentRows");
  rows.innerHTML = incidents.length ? incidents.map((incident) => `
    <tr>
      <td><strong>${incident.title}</strong><br><span class="muted">${incident.description}</span></td>
      <td>${SafeHarbor.badge(incident.severityLevel)}</td>
      <td>${SafeHarbor.badge(incident.status)}</td>
      <td>${incident.latitude}, ${incident.longitude}</td>
      <td>${incident.assignedResponderUsername || "-"}</td>
      <td>
        <div class="button-row">
          <button class="secondary" data-edit="${incident.id}" type="button">Edit</button>
          <select data-status="${incident.id}">
            <option value="">Set status</option>
            <option value="REPORTED">Reported</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          <button class="danger" data-delete="${incident.id}" type="button">Delete</button>
        </div>
      </td>
    </tr>
  `).join("") : `<tr><td colspan="6" class="empty-state">No incidents found.</td></tr>`;
}

incidentForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = SafeHarbor.formJson(incidentForm, ["latitude", "longitude"]);
  const id = payload.id;
  delete payload.id;
  SafeHarbor.setStatus("incidentStatus", "Saving incident...");
  try {
    await SafeHarbor.apiFetch(id ? `/api/incidents/${id}` : "/api/incidents", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    incidentForm.reset();
    await loadIncidents();
    SafeHarbor.setStatus("incidentStatus", "Incident saved.", "success");
  } catch (error) {
    SafeHarbor.setStatus("incidentStatus", error.message, "error");
  }
});

document.getElementById("incidentRows").addEventListener("click", async (event) => {
  const editId = event.target.dataset.edit;
  const deleteId = event.target.dataset.delete;
  if (editId) {
    const incident = incidents.find((item) => String(item.id) === editId);
    Object.entries(incident).forEach(([key, value]) => {
      if (incidentForm.elements[key]) incidentForm.elements[key].value = value ?? "";
    });
  }
  if (deleteId && confirm("Delete this incident?")) {
    await SafeHarbor.apiFetch(`/api/incidents/${deleteId}`, { method: "DELETE" });
    await loadIncidents();
  }
});

document.getElementById("incidentRows").addEventListener("change", async (event) => {
  const id = event.target.dataset.status;
  if (!id || !event.target.value) return;
  try {
    await SafeHarbor.apiFetch(`/api/incidents/${id}/status?status=${encodeURIComponent(event.target.value)}`, { method: "PATCH" });
    await loadIncidents();
  } catch (error) {
    SafeHarbor.setStatus("incidentStatus", error.message, "error");
  }
});

document.getElementById("resetIncident").addEventListener("click", () => incidentForm.reset());
document.getElementById("refreshIncidents").addEventListener("click", loadIncidents);
loadIncidents();
