SafeHarbor.requireAuth();
SafeHarbor.bindChrome("inventory");

const inventoryForm = document.getElementById("inventoryForm");
let inventoryItems = [];

async function loadInventory() {
  SafeHarbor.setStatus("inventoryStatus", "Loading inventory...");
  try {
    const page = await SafeHarbor.apiFetch("/api/inventory?size=100&sort=itemName,asc");
    inventoryItems = SafeHarbor.pageContent(page);
    renderInventory();
    SafeHarbor.setStatus("inventoryStatus", "Inventory loaded.", "success");
  } catch (error) {
    SafeHarbor.setStatus("inventoryStatus", error.message, "error");
  }
}

function renderInventory() {
  document.getElementById("inventoryRows").innerHTML = inventoryItems.length ? inventoryItems.map((item) => {
    const low = item.availableQuantity <= item.criticalThreshold;
    return `
      <tr>
        <td><strong>${item.itemName}</strong><br><span class="muted">${item.unit}</span></td>
        <td>${item.category}</td>
        <td>${low ? SafeHarbor.badge("CRITICAL") : SafeHarbor.badge("OK")} ${item.availableQuantity}</td>
        <td>${item.reservedQuantity}</td>
        <td>${item.criticalThreshold}</td>
        <td>
          <div class="button-row">
            <button class="secondary" data-edit="${item.id}" type="button">Edit</button>
            <button class="danger" data-delete="${item.id}" type="button">Delete</button>
          </div>
        </td>
      </tr>`;
  }).join("") : `<tr><td colspan="6" class="empty-state">No inventory items found.</td></tr>`;
}

inventoryForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = SafeHarbor.formJson(inventoryForm, ["availableQuantity", "criticalThreshold"]);
  const id = payload.id;
  delete payload.id;
  SafeHarbor.setStatus("inventoryStatus", "Saving item...");
  try {
    await SafeHarbor.apiFetch(id ? `/api/inventory/${id}` : "/api/inventory", {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    inventoryForm.reset();
    await loadInventory();
    SafeHarbor.setStatus("inventoryStatus", "Item saved.", "success");
  } catch (error) {
    SafeHarbor.setStatus("inventoryStatus", error.message, "error");
  }
});

document.getElementById("inventoryRows").addEventListener("click", async (event) => {
  const editId = event.target.dataset.edit;
  const deleteId = event.target.dataset.delete;
  if (editId) {
    const item = inventoryItems.find((entry) => String(entry.id) === editId);
    Object.entries(item).forEach(([key, value]) => {
      if (inventoryForm.elements[key]) inventoryForm.elements[key].value = value ?? "";
    });
  }
  if (deleteId && confirm("Delete this inventory item?")) {
    await SafeHarbor.apiFetch(`/api/inventory/${deleteId}`, { method: "DELETE" });
    await loadInventory();
  }
});

document.getElementById("resetInventory").addEventListener("click", () => inventoryForm.reset());
document.getElementById("refreshInventory").addEventListener("click", loadInventory);
loadInventory();
