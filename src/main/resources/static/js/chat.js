(function () {
  const box = document.getElementById("chatBox");
  const form = document.getElementById("chatForm");
  const input = document.getElementById("chatInput");
  if (!box || !form || !input || !window.SockJS || !window.Stomp) return;

  const roomId = box.dataset.roomId;
  const currentMemberId = Number(box.dataset.currentMemberId);
  const stomp = Stomp.over(new SockJS("/ws"));
  stomp.debug = null;

  stomp.connect({}, function () {
    stomp.subscribe("/topic/chat/" + roomId, function (frame) {
      const message = JSON.parse(frame.body);
      append(message);
    });
  });

  form.addEventListener("submit", function (event) {
    event.preventDefault();
    const content = input.value.trim();
    if (!content) return;
    stomp.send("/app/chat/" + roomId, {}, JSON.stringify({ content: content }));
    input.value = "";
  });

  function append(message) {
    const row = document.createElement("div");
    row.className = "chat-message" + (message.senderId === currentMemberId ? " mine" : "");
    const name = document.createElement("b");
    name.textContent = message.senderNickname + " ";
    const text = document.createElement("span");
    text.textContent = message.content;
    row.appendChild(name);
    row.appendChild(text);
    box.appendChild(row);
    box.scrollTop = box.scrollHeight;
  }
})();
