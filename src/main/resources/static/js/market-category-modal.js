(function () {
  var modal = document.getElementById("market-category-modal");
  var openBtn = document.getElementById("market-category-open");
  var closeBtn = document.getElementById("market-category-modal-close");
  var backdrop = document.getElementById("market-category-modal-backdrop");
  var doneBtn = document.getElementById("market-category-modal-done");
  var hiddenInput = document.getElementById("market-mobile-category");
  var labelEl = document.getElementById("market-category-open-label");
  var form = document.getElementById("market-filters-mobile-form");
  var list = document.getElementById("market-category-modal-list");

  if (!modal || !openBtn || !hiddenInput || !labelEl || !list) {
    return;
  }

  var pendingValue = hiddenInput.value || "";
  var pendingLabel = labelEl.textContent.trim() || "전체";

  function syncActive(value) {
    list.querySelectorAll(".market-category-modal__option").forEach(function (btn) {
      var selected = (btn.getAttribute("data-value") || "") === value;
      btn.classList.toggle("is-active", selected);
      btn.setAttribute("aria-checked", selected ? "true" : "false");
    });
  }

  function openModal() {
    pendingValue = hiddenInput.value || "";
    pendingLabel = labelEl.textContent.trim() || "전체";
    syncActive(pendingValue);
    modal.hidden = false;
    document.body.classList.add("market-category-modal-open");
  }

  function closeModal() {
    modal.hidden = true;
    document.body.classList.remove("market-category-modal-open");
  }

  function applySelection(submit) {
    hiddenInput.value = pendingValue;
    labelEl.textContent = pendingLabel || "전체";
    closeModal();
    if (submit && form) {
      form.submit();
    }
  }

  openBtn.addEventListener("click", openModal);

  if (closeBtn) {
    closeBtn.addEventListener("click", closeModal);
  }
  if (backdrop) {
    backdrop.addEventListener("click", closeModal);
  }

  list.addEventListener("click", function (event) {
    var btn = event.target.closest(".market-category-modal__option");
    if (!btn) {
      return;
    }
    pendingValue = btn.getAttribute("data-value") || "";
    pendingLabel = btn.getAttribute("data-label") || "전체";
    syncActive(pendingValue);
  });

  if (doneBtn) {
    doneBtn.addEventListener("click", function () {
      applySelection(true);
    });
  }

  if (form) {
    form.addEventListener("submit", function () {
      if (!hiddenInput.value) {
        hiddenInput.disabled = true;
      }
    });
  }

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && !modal.hidden) {
      closeModal();
    }
  });
})();
