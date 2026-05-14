(function () {
  var openBtn = document.getElementById("region-chip-open");
  var modal = document.getElementById("region-modal");
  var closeBtn = document.getElementById("region-modal-close");
  var backdrop = document.getElementById("region-modal-backdrop");
  var search = document.getElementById("region-search");
  var form = document.getElementById("region-select-form");
  var hiddenId = document.getElementById("region-selected-id");

  /** 모달 내부 리스트만 사용(문서 어디에 두든 동일 요소를 가리키도록). */
  function listRoot() {
    return modal ? modal.querySelector("#region-list") : null;
  }

  if (!openBtn || !modal || !listRoot() || !form || !hiddenId) {
    return;
  }

  var items = [];
  var loadPromise = null;
  var loadError = false;
  /** fetch 완료 전 input/composition이 render()를 호출하면 빈 목록으로 덮여 빈 화면처럼 보일 수 있음 */
  var loadingList = false;

  function ctxPath() {
    var h = document.querySelector(".site-header[data-context-path]");
    return h ? h.getAttribute("data-context-path") || "" : "";
  }

  /** Thymeleaf @{/api/neighborhoods} 로 생성된 경로(컨텍스트 경로 포함). 조합 실수 방지. */
  function neighborhoodsEndpoint() {
    var el = document.querySelector(".site-header[data-neighborhoods-api]");
    var fromServer = el && el.getAttribute("data-neighborhoods-api");
    if (fromServer && fromServer.length > 0) {
      return fromServer;
    }
    var cp = ctxPath() || "";
    if (cp.endsWith("/")) {
      cp = cp.slice(0, -1);
    }
    return cp + "/api/neighborhoods";
  }

  function normStr(s) {
    if (s == null) return "";
    try {
      return String(s).normalize("NFC").trim().toLowerCase();
    } catch (e) {
      return String(s).trim().toLowerCase();
    }
  }

  function normalizePayload(data) {
    if (Array.isArray(data)) return data;
    if (data && typeof data === "object") {
      if (Array.isArray(data.data)) return data.data;
      if (Array.isArray(data.content)) return data.content;
      if (Array.isArray(data.neighborhoods)) return data.neighborhoods;
    }
    return [];
  }

  function neighborhoodLabel(n) {
    if (!n || typeof n !== "object") return "";
    var direct =
      n.displayName ||
      n.display_name ||
      n.DisplayName ||
      n.townName ||
      n.town_name ||
      n.TownName ||
      n.emdName ||
      n.emd_name ||
      n.EmdName;
    if (direct != null && String(direct).trim() !== "") {
      return String(direct).trim();
    }
    var parts = [];
    for (var k in n) {
      if (!Object.prototype.hasOwnProperty.call(n, k)) continue;
      if (k === "id") continue;
      var v = n[k];
      if (typeof v === "string" && v.trim().length > 0) {
        parts.push(v.trim());
      }
    }
    return parts.join(" ");
  }

  function neighborhoodId(n) {
    if (!n || typeof n !== "object") return null;
    return n.id != null ? n.id : n.ID;
  }

  function haystack(n) {
    var label = neighborhoodLabel(n);
    var tn = n.townName || n.town_name || n.TownName || "";
    var sd =
      n.sido || n.Sido || n.siDo || n.SiDo || "";
    var sg =
      n.sigungu || n.Sigungu || n.siGunGu || n.SiGunGu || "";
    return normStr(label + " " + tn + " " + sd + " " + sg);
  }

  var renderFrame = null;
  function scheduleRender() {
    if (renderFrame != null) {
      cancelAnimationFrame(renderFrame);
    }
    renderFrame = requestAnimationFrame(function () {
      renderFrame = null;
      render();
    });
  }

  function loadNeighborhoods() {
    if (loadPromise) {
      return loadPromise;
    }
    loadError = false;
    loadingList = true;
    var lr = listRoot();
    if (!lr) {
      loadingList = false;
      return Promise.resolve();
    }
    lr.innerHTML =
      '<li class="region-modal-msg">목록을 불러오는 중…</li>';

    loadPromise = fetch(neighborhoodsEndpoint(), {
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    })
      .then(function (r) {
        if (!r.ok) {
          throw new Error("HTTP " + r.status);
        }
        return r.json();
      })
      .then(function (data) {
        items = normalizePayload(data);
      })
      .catch(function () {
        loadError = true;
        items = [];
      })
      .then(function () {
        loadingList = false;
        loadPromise = null;
      });

    return loadPromise;
  }

  function render() {
    var lr = listRoot();
    if (!lr) {
      return;
    }
    if (loadingList) {
      return;
    }
    if (loadError) {
      lr.innerHTML =
        '<li class="region-modal-msg region-modal-msg--error">목록을 불러오지 못했습니다.</li>';
      return;
    }

    var q = normStr(search ? search.value : "");
    lr.innerHTML = "";
    var filtered =
      q.length === 0
        ? items
        : items.filter(function (n) {
            return haystack(n).indexOf(q) !== -1;
          });
    if (filtered.length === 0) {
      lr.innerHTML =
        '<li class="region-modal-msg">' +
        (items.length === 0 ? "등록된 동네가 없습니다." : "검색 결과가 없습니다.") +
        "</li>";
      return;
    }
    filtered.forEach(function (n) {
      var label = neighborhoodLabel(n);
      var nid = neighborhoodId(n);
      var li = document.createElement("li");
      var btn = document.createElement("button");
      btn.type = "button";
      btn.className = "region-modal-item";
      btn.setAttribute("aria-label", label + " 선택");
      btn.textContent = label;
      btn.addEventListener("click", function () {
        hiddenId.value = String(nid);
        form.submit();
      });
      li.appendChild(btn);
      lr.appendChild(li);
    });
  }

  function openModal() {
    modal.hidden = false;
    modal.removeAttribute("hidden");
    document.body.classList.add("region-modal-open");
    /** 일부 구형 브라우저는 Promise.finally 미지원 → render 미실행으로 빈 목록이 될 수 있음 */
    loadNeighborhoods().then(function () {
      render();
      if (search) {
        search.focus();
      }
    });
  }

  function closeModal() {
    modal.hidden = true;
    modal.setAttribute("hidden", "");
    document.body.classList.remove("region-modal-open");
  }

  openBtn.addEventListener("click", openModal);
  if (closeBtn) closeBtn.addEventListener("click", closeModal);
  if (backdrop) backdrop.addEventListener("click", closeModal);

  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && !modal.hidden) {
      closeModal();
    }
  });

  if (search) {
    /** 한글 IME: compositionend 직후에도 input.value가 아직 갱신 안 된 경우가 있어 다음 프레임에 렌더 */
    search.addEventListener("input", scheduleRender);
    search.addEventListener("compositionend", scheduleRender);
  }
})();
