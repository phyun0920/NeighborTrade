(function () {
  var shell = document.getElementById("admin-shell");
  var toggle = document.getElementById("admin-menu-toggle");
  var sidebar = document.getElementById("admin-sidebar");
  var backdrop = document.getElementById("admin-sidebar-backdrop");

  if (!shell || !toggle || !sidebar || !backdrop) {
    return;
  }

  var mq = window.matchMedia("(max-width: 767px)");

  function openSidebar() {
    shell.classList.add("admin-shell--sidebar-open");
    backdrop.hidden = false;
    toggle.setAttribute("aria-expanded", "true");
    toggle.setAttribute("aria-label", "관리자 메뉴 닫기");
  }

  function closeSidebar() {
    shell.classList.remove("admin-shell--sidebar-open");
    backdrop.hidden = true;
    toggle.setAttribute("aria-expanded", "false");
    toggle.setAttribute("aria-label", "관리자 메뉴 열기");
  }

  function syncForViewport() {
    if (!mq.matches) {
      closeSidebar();
    }
  }

  toggle.addEventListener("click", function () {
    if (shell.classList.contains("admin-shell--sidebar-open")) {
      closeSidebar();
    } else {
      openSidebar();
    }
  });

  backdrop.addEventListener("click", closeSidebar);

  sidebar.querySelectorAll("a").forEach(function (link) {
    link.addEventListener("click", function () {
      if (mq.matches) {
        closeSidebar();
      }
    });
  });

  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && mq.matches) {
      closeSidebar();
    }
  });

  mq.addEventListener("change", syncForViewport);
  syncForViewport();
})();
