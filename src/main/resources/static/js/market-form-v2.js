(function () {
  var MAX_FILES = 10;
  var input = document.getElementById("market-form-v2-file-input");
  var previews = document.getElementById("market-form-v2-previews");
  var status = document.getElementById("market-form-v2-file-status");
  var urlInput = document.getElementById("market-form-v2-rep-url");
  var fileZone = document.getElementById("market-form-v2-file-zone");
  var form = input ? input.closest("form") : null;

  if (!input || !previews) {
    return;
  }

  var objectUrls = [];
  var pendingFiles = [];
  var removedExistingIds = new Set();

  function serverExistingCount() {
    if (!form) {
      return 0;
    }
    return parseInt(form.getAttribute("data-existing-image-count") || "0", 10) || 0;
  }

  function effectiveExistingCount() {
    return Math.max(0, serverExistingCount() - removedExistingIds.size);
  }

  function maxNewFiles() {
    return Math.max(0, MAX_FILES - effectiveExistingCount());
  }

  function revokeAll() {
    objectUrls.forEach(function (url) {
      URL.revokeObjectURL(url);
    });
    objectUrls = [];
  }

  function isDuplicate(file) {
    return pendingFiles.some(function (existing) {
      return (
        existing.name === file.name &&
        existing.size === file.size &&
        existing.lastModified === file.lastModified
      );
    });
  }

  function syncInputFiles() {
    var dt = new DataTransfer();
    pendingFiles.forEach(function (file) {
      dt.items.add(file);
    });
    input.files = dt.files;
  }

  function createRemoveButton(label, onClick) {
    var button = document.createElement("button");
    button.type = "button";
    button.className = "market-form-v2__remove-btn";
    button.setAttribute("aria-label", label);
    button.textContent = "×";
    button.addEventListener("click", onClick);
    return button;
  }

  function updateStatus() {
    var pendingCount = pendingFiles.length;
    var existingCount = effectiveExistingCount();
    var isEdit = serverExistingCount() > 0;

    if (pendingCount > 0 || removedExistingIds.size > 0) {
      status.hidden = false;
      if (isEdit) {
        status.textContent =
          pendingCount +
          "장 추가 예정 · 등록 " +
          existingCount +
          "장 · 합계 최대 " +
          MAX_FILES +
          "장";
      } else {
        status.textContent =
          pendingCount + "장 선택됨 · 첫 번째 사진이 대표 이미지로 등록됩니다.";
      }
    } else {
      status.hidden = true;
      status.textContent = "";
    }

    if (pendingCount > 0) {
      if (urlInput) {
        urlInput.value = "";
        urlInput.readOnly = true;
        urlInput.placeholder = "파일 업로드 사용 중 (URL 입력 불필요)";
      }
    } else if (removedExistingIds.size === 0) {
      if (urlInput) {
        urlInput.readOnly = false;
        urlInput.placeholder = "대표 이미지 URL (선택, 파일 업로드 우선)";
      }
    }

    if (fileZone) {
      fileZone.hidden = existingCount + pendingCount >= MAX_FILES;
    }
  }

  function renderPreviews() {
    revokeAll();
    previews.innerHTML = "";

    var files = pendingFiles.slice(0, maxNewFiles());
    var isEdit = serverExistingCount() > 0;

    files.forEach(function (file, index) {
      var url = URL.createObjectURL(file);
      objectUrls.push(url);

      var item = document.createElement("div");
      item.className = "market-form-v2__preview-item";

      var img = document.createElement("img");
      img.src = url;
      img.alt = file.name;
      item.appendChild(img);

      if (!isEdit && index === 0) {
        item.classList.add("is-cover");
        var badge = document.createElement("span");
        badge.className = "market-form-v2__preview-badge";
        badge.textContent = "대표";
        item.appendChild(badge);
      }

      item.appendChild(
        createRemoveButton("추가 예정 사진 삭제", function () {
          removePendingFile(file);
        })
      );

      previews.appendChild(item);
    });

    updateStatus();
  }

  function removePendingFile(file) {
    pendingFiles = pendingFiles.filter(function (item) {
      return item !== file;
    });
    syncInputFiles();
    renderPreviews();
  }

  function removeExistingImage(imageId) {
    removedExistingIds.add(String(imageId));
    var item = document.querySelector(
      '.market-form-v2__existing-item[data-image-id="' + imageId + '"]'
    );
    if (item) {
      item.classList.add("is-removed");
      item.hidden = true;
    }
    updateStatus();
  }

  function appendSelectedFiles(newFiles) {
    var limit = maxNewFiles();
    if (limit === 0) {
      window.alert("이미지는 최대 " + MAX_FILES + "장까지 등록할 수 있습니다.");
      input.value = "";
      return;
    }

    var skippedDuplicate = false;
    Array.from(newFiles || []).forEach(function (file) {
      if (pendingFiles.length >= limit) {
        return;
      }
      if (isDuplicate(file)) {
        skippedDuplicate = true;
        return;
      }
      pendingFiles.push(file);
    });

    if (pendingFiles.length > limit) {
      pendingFiles = pendingFiles.slice(0, limit);
      window.alert("추가 가능한 사진은 최대 " + limit + "장입니다.");
    }

    if (skippedDuplicate) {
      window.alert("이미 선택한 사진은 제외되었습니다.");
    }

    syncInputFiles();
    renderPreviews();
    input.value = "";
  }

  input.addEventListener("change", function () {
    appendSelectedFiles(input.files);
  });

  document.querySelectorAll(".js-remove-existing-image").forEach(function (button) {
    button.addEventListener("click", function () {
      removeExistingImage(button.getAttribute("data-image-id"));
    });
  });

  if (form) {
    form.addEventListener("submit", function () {
      syncInputFiles();
      form.querySelectorAll('input[name="deleteImageIds"]').forEach(function (node) {
        node.remove();
      });
      removedExistingIds.forEach(function (id) {
        var hidden = document.createElement("input");
        hidden.type = "hidden";
        hidden.name = "deleteImageIds";
        hidden.value = id;
        form.appendChild(hidden);
      });
    });
  }

  if (fileZone && effectiveExistingCount() >= MAX_FILES) {
    fileZone.hidden = true;
  }

  window.addEventListener("pagehide", revokeAll);
})();
