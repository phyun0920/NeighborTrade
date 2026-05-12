function fillCurrentLocation() {
  if (!navigator.geolocation) {
    alert("브라우저가 위치 기능을 지원하지 않습니다.");
    return;
  }
  navigator.geolocation.getCurrentPosition(
    function (pos) {
      document.getElementById("latitude").value = pos.coords.latitude;
      document.getElementById("longitude").value = pos.coords.longitude;
    },
    function () {
      alert("위치 권한을 허용해주세요.");
    },
  );
}
