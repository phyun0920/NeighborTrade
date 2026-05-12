(function () {
  var map;
  var neighborhoodCircle;
  var centerMarker;
  var gpsMarker;
  var neighborhoodSelect;
  var radiusLegendEl;

  function domReady(fn) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", fn);
    } else {
      fn();
    }
  }

  function parseCoord(value) {
    var n = parseFloat(value);
    return Number.isFinite(n) ? n : null;
  }

  function getSelectedOption() {
    if (!neighborhoodSelect || neighborhoodSelect.selectedIndex < 0) return null;
    return neighborhoodSelect.options[neighborhoodSelect.selectedIndex];
  }

  function readNeighborhoodFromSelection() {
    var opt = getSelectedOption();
    if (!opt || !opt.value) return null;
    var lat = parseFloat(opt.getAttribute("data-center-lat"));
    var lng = parseFloat(opt.getAttribute("data-center-lng"));
    var radius = parseInt(opt.getAttribute("data-radius-m"), 10);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null;
    if (!Number.isFinite(radius) || radius <= 0) radius = 1000;
    return { lat: lat, lng: lng, radiusM: radius };
  }

  function removeNeighborhoodLayers() {
    if (neighborhoodCircle && map) {
      map.removeLayer(neighborhoodCircle);
      neighborhoodCircle = null;
    }
    if (centerMarker && map) {
      map.removeLayer(centerMarker);
      centerMarker = null;
    }
  }

  function drawNeighborhood(nb) {
    removeNeighborhoodLayers();
    if (!map || !nb) {
      if (radiusLegendEl) radiusLegendEl.textContent = "동네를 선택하면 중심과 인증 반경이 표시됩니다.";
      return;
    }
    centerMarker = L.marker([nb.lat, nb.lng]).addTo(map);
    centerMarker.bindPopup("동네 기준점(중심 좌표)");
    neighborhoodCircle = L.circle([nb.lat, nb.lng], {
      radius: nb.radiusM,
      color: "#ff6f0f",
      weight: 2,
      fillColor: "#ff6f0f",
      fillOpacity: 0.12,
    }).addTo(map);
    neighborhoodCircle.bindPopup("인증 허용 거리: 약 " + nb.radiusM + "m (반경)");
    if (radiusLegendEl) {
      radiusLegendEl.textContent =
        "선택한 동네 인증 반경: 약 " +
        nb.radiusM.toLocaleString("ko-KR") +
        "m (중심에서 반경). GPS 마커가 이 안에 있으면 반경 검증에서는 통과에 가깝습니다.";
    }
  }

  function ensureGpsMarker(lat, lng) {
    if (!map) return;
    if (gpsMarker) {
      gpsMarker.setLatLng([lat, lng]);
    } else {
      gpsMarker = L.circleMarker([lat, lng], {
        radius: 11,
        color: "#b91c1c",
        weight: 2,
        fillColor: "#dc2626",
        fillOpacity: 0.9,
      }).addTo(map);
      gpsMarker.bindPopup("현재 GPS (" + lat.toFixed(6) + ", " + lng.toFixed(6) + ")");
    }
  }

  function fitMap() {
    if (!map) return;
    var bounds = L.latLngBounds([]);
    if (neighborhoodCircle) {
      bounds.extend(neighborhoodCircle.getBounds());
    } else if (centerMarker) {
      bounds.extend(centerMarker.getLatLng());
    }
    var latIn = document.getElementById("latitude");
    var lngIn = document.getElementById("longitude");
    var glat = latIn ? parseCoord(latIn.value) : null;
    var glng = lngIn ? parseCoord(lngIn.value) : null;
    if (glat != null && glng != null) {
      bounds.extend([glat, glng]);
    }
    if (bounds.isValid()) {
      map.fitBounds(bounds.pad(0.12));
    } else {
      map.setView([37.5665, 126.9784], 11);
    }
  }

  // 서버에서 동네별 경계여부 내려주기 : postgis 켜짐 그리고 선택 동네에 경계 있을 때만 문구 표시 추가(20260512)
  function updatePostgisBoundaryNote() {
    var note = document.getElementById("locationPostgisBoundaryNote");
    var form = document.getElementById("locationForm");
    if (!note || !form) return;
    var postgisOn = form.getAttribute("data-postgis-enabled") === "true";
    var opt = getSelectedOption();
    var hasBoundary =
      opt &&
      opt.value &&
      opt.getAttribute("data-has-boundary") === "true";
    note.style.display = postgisOn && hasBoundary ? "block" : "none";
  }

  function onNeighborhoodChange() {
    var nb = readNeighborhoodFromSelection();
    drawNeighborhood(nb);
    updatePostgisBoundaryNote();        // 서버에서 동네별 경계여부 내려주기 : postgis 켜짐 그리고 선택 동네에 경계 있을 때만 문구 표시 추가(20260512)
    fitMap();
  }

  function syncGpsFromInputs() {
    var latIn = document.getElementById("latitude");
    var lngIn = document.getElementById("longitude");
    if (!latIn || !lngIn || !map) return;
    var lat = parseCoord(latIn.value);
    var lng = parseCoord(lngIn.value);
    if (lat == null || lng == null) {
      if (gpsMarker) {
        map.removeLayer(gpsMarker);
        gpsMarker = null;
      }
      return;
    }
    ensureGpsMarker(lat, lng);
    gpsMarker.setPopupContent(
      "현재 GPS (" + lat.toFixed(6) + ", " + lng.toFixed(6) + ")",
    );
    fitMap();
  }

  function initMap() {
    var el = document.getElementById("locationMap");
    neighborhoodSelect = document.getElementById("neighborhoodId");
    radiusLegendEl = document.getElementById("locationRadiusLegend");
    if (!el || typeof L === "undefined") return;
    map = L.map("locationMap", { scrollWheelZoom: true }).setView([37.5665, 126.9784], 11);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(map);
    setTimeout(function () {
      map.invalidateSize();
    }, 100);

    if (neighborhoodSelect) {
      neighborhoodSelect.addEventListener("change", onNeighborhoodChange);
    }
    var latIn = document.getElementById("latitude");
    var lngIn = document.getElementById("longitude");
    if (latIn)
      latIn.addEventListener("input", syncGpsFromInputs);
    if (lngIn)
      lngIn.addEventListener("input", syncGpsFromInputs);

    onNeighborhoodChange();
    syncGpsFromInputs();
  }

  window.fillCurrentLocation = function () {
    if (!navigator.geolocation) {
      alert("브라우저가 위치 기능을 지원하지 않습니다.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      function (pos) {
        var lat = pos.coords.latitude;
        var lng = pos.coords.longitude;
        var latEl = document.getElementById("latitude");
        var lngEl = document.getElementById("longitude");
        if (latEl) latEl.value = lat;
        if (lngEl) lngEl.value = lng;
        if (map) {
          ensureGpsMarker(lat, lng);
          fitMap();
        }
      },
      function () {
        alert("위치 권한을 허용해주세요.");
      },
    );
  };

  domReady(initMap);
})();
