SafeHarbor.requireAuth();
SafeHarbor.bindChrome("shelters");

const shelterForm = document.getElementById("shelterForm");
let shelters = [];

async function loadShelters() {
  SafeHarbor.setStatus("shelterStatus", "Loading shelters...");
  try {
    const page = await SafeHarbor.apiFetch("/api/shelters?size=100&sort=shelterName,asc");
    shelters = SafeHarbor.pageContent(page);
    renderShelters();
    SafeHarbor.setStatus("shelterStatus", "Shelters loaded.", "success");
  } catch (error) {
    SafeHarbor.setStatus("shelterStatus", error.message, "error");
  }
}

function renderShelters() {
  document.getElementById("shelterRows").innerHTML = shelters.length ? shelters.map((shelter) => `
    <tr>
      <td><strong>${shelter.shelterName}</strong></td>
      <td>${shelter.locationAddress}</td>
      <td>${shelter.currentOccupancy} / ${shelter.capacity}</td>
      <td>${shelter.managerName || "-"}</td>
      <td>${SafeHarbor.badge(SafeHarbor.isActive(shelter) ? "ACTIVE" : "INACTIVE")}</td>
      <td>
        <div class="button-row">
          <button class="secondary" data-edit="${shelter.id}" type="button">Edit</button>
          <button class="secondary" data-intake="${shelter.id}" data-count="1" type="button">+1</button>
          <button class="secondary" data-intake="${shelter.id}" data-count="-1" type="button">-1</button>
          <button class="danger" data-delete="${shelter.id}" type="button">Deactivate</button>
        </div>
      </td>
    </tr>
  `).join("") : `<tr><td colspan="6" class="empty-state">No shelters found.</td></tr>`;
}

shelterForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = SafeHarbor.formJson(shelterForm, ["capacity", "currentOccupancy"]);
  const id = payload.id;
  delete payload.id;
  SafeHarbor.setStatus("shelterStatus", "Saving shelter...");
  try {
    await SafeHarbor.apiFetch(id ? `/api/shelters/${id}` : "/api/shelters", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    shelterForm.reset();
    await loadShelters();
    SafeHarbor.setStatus("shelterStatus", "Shelter saved.", "success");
  } catch (error) {
    SafeHarbor.setStatus("shelterStatus", error.message, "error");
  }
});

document.getElementById("shelterRows").addEventListener("click", async (event) => {
  const editId = event.target.dataset.edit;
  const deleteId = event.target.dataset.delete;
  const intakeId = event.target.dataset.intake;
  if (editId) {
    const shelter = shelters.find((entry) => String(entry.id) === editId);
    Object.entries(shelter).forEach(([key, value]) => {
      if (shelterForm.elements[key]) shelterForm.elements[key].value = value ?? "";
    });
  }
  if (intakeId) {
    const count = event.target.dataset.count;
    await SafeHarbor.apiFetch(`/api/shelters/${intakeId}/occupancy?intakeCount=${count}`, { method: "PATCH" });
    await loadShelters();
  }
  if (deleteId && confirm("Deactivate this shelter?")) {
    await SafeHarbor.apiFetch(`/api/shelters/${deleteId}`, { method: "DELETE" });
    await loadShelters();
  }
});

document.getElementById("resetShelter").addEventListener("click", () => shelterForm.reset());
document.getElementById("refreshShelters").addEventListener("click", loadShelters);
loadShelters();
