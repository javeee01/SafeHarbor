SafeHarbor.requireAuth();
SafeHarbor.bindChrome("admin");

const personnelForm = document.getElementById("personnelForm");
let personnel = [];

async function loadPersonnel() {
  SafeHarbor.setStatus("adminStatus", "Loading personnel...");
  try {
    const page = await SafeHarbor.apiFetch("/api/auth/personnel?size=100&sort=fullName,asc");
    personnel = SafeHarbor.pageContent(page);
    renderPersonnel();
    SafeHarbor.setStatus("adminStatus", "Personnel loaded.", "success");
  } catch (error) {
    SafeHarbor.setStatus("adminStatus", `${error.message} Agency Director role is required.`, "error");
  }
}

function renderPersonnel() {
  document.getElementById("personnelRows").innerHTML = personnel.length ? personnel.map((account) => `
    <tr>
      <td><strong>${account.fullName}</strong><br><span class="muted">${account.contactNumber || "-"}</span></td>
      <td>${account.username}</td>
      <td><span class="role-chip">${account.role}</span></td>
      <td>${account.assignedRegion || "-"}</td>
      <td>${SafeHarbor.badge(SafeHarbor.isActive(account) ? "ACTIVE" : "INACTIVE")}</td>
      <td>
        <div class="button-row">
          <button class="secondary" data-edit="${account.id}" type="button">Edit</button>
          <button class="danger" data-delete="${account.id}" type="button">Deactivate</button>
        </div>
      </td>
    </tr>
  `).join("") : `<tr><td colspan="6" class="empty-state">No personnel records found.</td></tr>`;
}

personnelForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = SafeHarbor.formJson(personnelForm);
  const id = payload.id;
  delete payload.id;
  SafeHarbor.setStatus("adminStatus", "Saving account...");
  try {
    const path = id ? `/api/auth/personnel/${id}` : "/api/auth/register";
    await SafeHarbor.apiFetch(path, { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
    personnelForm.reset();
    await loadPersonnel();
    SafeHarbor.setStatus("adminStatus", "Account saved.", "success");
  } catch (error) {
    SafeHarbor.setStatus("adminStatus", error.message, "error");
  }
});

document.getElementById("personnelRows").addEventListener("click", async (event) => {
  const editId = event.target.dataset.edit;
  const deleteId = event.target.dataset.delete;
  if (editId) {
    const account = personnel.find((entry) => String(entry.id) === editId);
    Object.entries(account).forEach(([key, value]) => {
      if (personnelForm.elements[key]) personnelForm.elements[key].value = value ?? "";
    });
    personnelForm.elements.password.value = "";
    personnelForm.elements.password.placeholder = "Enter a password to save changes";
  }
  if (deleteId && confirm("Deactivate this account?")) {
    await SafeHarbor.apiFetch(`/api/auth/personnel/${deleteId}`, { method: "DELETE" });
    await loadPersonnel();
  }
});

document.getElementById("resetPersonnel").addEventListener("click", () => {
  personnelForm.reset();
  personnelForm.elements.password.required = true;
  personnelForm.elements.password.placeholder = "";
});
document.getElementById("refreshPersonnel").addEventListener("click", loadPersonnel);
loadPersonnel();
