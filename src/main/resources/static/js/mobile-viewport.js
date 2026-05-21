(function () {
  function applyMobileRoot() {
    var mobile =
      window.matchMedia("(max-width: 767px)").matches ||
      window.innerWidth <= 767;
    document.documentElement.classList.toggle("is-mobile", mobile);
  }

  applyMobileRoot();
  window.addEventListener("resize", applyMobileRoot);
  window.addEventListener("orientationchange", applyMobileRoot);
})();
