SafeHarbor.requireAuth();
SafeHarbor.bindChrome("dispatch");

const dispatchForm = document.getElementById("dispatchForm");
let dispatches = [];

async function loadSelects() {
  const [incidentPage, inventoryPage] = await Promise.all([
    SafeHarbor.apiFetch("/api/incidents?size=100"),
    SafeHarbor.apiFetch("/api/inventory?size=100")
  ]);
  const incidents = SafeHarbor.pageContent(incidentPage);
  const inventory = SafeHarbor.pageContent(inventoryPage);
  document.getElementById("incidentSelect").innerHTML = incidents
    .map((incident) => `<option value="${incident.id}">${incident.title} (${incident.status})</option>`)
    .join("");
  document.getElementById("inventorySelect").innerHTML = inventory
    .map((item) => `<option value="${item.id}">${item.itemName} - ${item.availableQuantity} ${item.unit}</option>`)
    .join("");
}

async function loadDispatches() {
  SafeHarbor.setStatus("dispatchStatus", "Loading dispatch data...");
  try {
    await loadSelects();
    const page = await SafeHarbor.apiFetch("/api/dispatches?size=100&sort=initiatedAt,desc");
    dispatches = SafeHarbor.pageContent(page);
    renderDispatches();
    SafeHarbor.setStatus("dispatchStatus", "Dispatch data loaded.", "success");
  } catch (error) {
    SafeHarbor.setStatus("dispatchStatus", error.message, "error");
  }
}

function renderDispatches() {
  document.getElementById("dispatchRows").innerHTML = dispatches.length ? dispatches.map((dispatch) => `
    <tr>
      <td><strong>${dispatch.incidentTitle}</strong></td>
      <td>${dispatch.itemName}</td>
      <td>${dispatch.dispatchedQuantity}</td>
      <td>${SafeHarbor.badge(dispatch.dispatchStatus)}</td>
      <td>${SafeHarbor.formatDate(dispatch.initiatedAt)}</td>
      <td>
        <div class="button-row">
          <button class="secondary" data-status="${dispatch.id}" data-value="DELIVERED" type="button">Deliver</button>
          <button class="secondary" data-status="${dispatch.id}" data-value="CANCELLED" type="button">Cancel</button>
          <button class="danger" data-delete="${dispatch.id}" type="button">Delete</button>
        </div>
      </td>
    </tr>
  `).join("") : `<tr><td colspan="6" class="empty-state">No dispatches found.</td></tr>`;
}

dispatchForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = SafeHarbor.formJson(dispatchForm, ["targetIncidentId", "inventoryItemId", "dispatchedQuantity"]);
  const id = payload.id;
  delete payload.id;
  SafeHarbor.setStatus("dispatchStatus", "Saving dispatch...");
  try {
    await SafeHarbor.apiFetch(id ? `/api/dispatches/${id}` : "/api/dispatches/request", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    dispatchForm.reset();
    await loadDispatches();
    SafeHarbor.setStatus("dispatchStatus", "Dispatch saved.", "success");
  } catch (error) {
    SafeHarbor.setStatus("dispatchStatus", error.message, "error");
  }
});

document.getElementById("dispatchRows").addEventListener("click", async (event) => {
  const statusId = event.target.dataset.status;
  const deleteId = event.target.dataset.delete;
  try {
    if (statusId) {
      const value = event.target.dataset.value;
      const path = value === "DELIVERED"
        ? `/api/dispatches/${statusId}/fulfill`
        : `/api/dispatches/${statusId}/status?status=${value}`;
      await SafeHarbor.apiFetch(path, { method: value === "DELIVERED" ? "POST" : "PATCH" });
      await loadDispatches();
    }
    if (deleteId && confirm("Cancel this dispatch?")) {
      await SafeHarbor.apiFetch(`/api/dispatches/${deleteId}`, { method: "DELETE" });
      await loadDispatches();
    }
  } catch (error) {
    SafeHarbor.setStatus("dispatchStatus", error.message, "error");
  }
});

document.getElementById("resetDispatch").addEventListener("click", () => dispatchForm.reset());
document.getElementById("refreshDispatches").addEventListener("click", loadDispatches);
loadDispatches();
