(function () {
  var box = document.getElementById("chatBox");
  var form = document.getElementById("chatForm");
  var input = document.getElementById("chatInput");
  if (!box || !form || !input || !window.SockJS || !window.Stomp) return;

  var roomId = box.dataset.roomId;
  var currentMemberId = Number(box.dataset.currentMemberId);
  var reportPrefix = box.dataset.reportPrefix || "/reports/chat_message/";
  var lastDateKey = initLastDateKey();

  function initLastDateKey() {
    var nodes = box.querySelectorAll(".chat-message[data-message-date]");
    if (!nodes.length) return null;
    return nodes[nodes.length - 1].getAttribute("data-message-date");
  }

  var stomp = Stomp.over(new SockJS("/ws"));
  stomp.debug = null;

  function scrollToBottom() {
    box.scrollTop = box.scrollHeight;
  }

  function scrollToBottomSoon() {
    scrollToBottom();
    requestAnimationFrame(function () {
      scrollToBottom();
      requestAnimationFrame(scrollToBottom);
    });
  }

  scrollToBottomSoon();
  window.addEventListener("load", scrollToBottomSoon);
  if (document.fonts && document.fonts.ready) {
    document.fonts.ready.then(scrollToBottom);
  }

  stomp.connect({}, function () {
    stomp.subscribe("/topic/chat/" + roomId, function (frame) {
      var message = JSON.parse(frame.body);
      append(message);
    });
  });

  form.addEventListener("submit", function (event) {
    event.preventDefault();
    var content = input.value.trim();
    if (!content) return;
    stomp.send("/app/chat/" + roomId, {}, JSON.stringify({ content: content }));
    input.value = "";
  });

  function parseInstant(iso) {
    if (!iso) return new Date();
    var normalized = String(iso).replace(" ", "T");
    var d = new Date(normalized);
    return isNaN(d.getTime()) ? new Date() : d;
  }

  function dateKey(d) {
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, "0");
    var day = String(d.getDate()).padStart(2, "0");
    return y + "-" + m + "-" + day;
  }

  function formatKoreanDate(d) {
    return d.getFullYear() + "년 " + (d.getMonth() + 1) + "월 " + d.getDate() + "일";
  }

  function formatTime(d) {
    var h = String(d.getHours()).padStart(2, "0");
    var min = String(d.getMinutes()).padStart(2, "0");
    return h + ":" + min;
  }

  function appendDateDivider(label) {
    var wrap = document.createElement("div");
    wrap.className = "chat-date-divider";
    var span = document.createElement("span");
    span.textContent = label;
    wrap.appendChild(span);
    box.appendChild(wrap);
  }

  function append(message) {
    var created = parseInstant(message.createdAt);
    var dk = dateKey(created);
    if (lastDateKey !== dk) {
      appendDateDivider(formatKoreanDate(created));
      lastDateKey = dk;
    }

    var row = document.createElement("div");
    row.className = "chat-message" + (message.senderId === currentMemberId ? " mine" : "");
    row.setAttribute("data-message-date", dk);

    var inner = document.createElement("div");
    inner.className = "chat-message-inner";

    var bubble = document.createElement("div");
    bubble.className = "chat-bubble";

    if (message.senderId !== currentMemberId) {
      var sender = document.createElement("span");
      sender.className = "chat-sender";
      sender.textContent = message.senderNickname || "";
      bubble.appendChild(sender);
    }

    var text = document.createElement("span");
    text.className = "chat-text";
    text.textContent = message.content || "";
    bubble.appendChild(text);

    var meta = document.createElement("div");
    meta.className = "chat-meta";

    var timeEl = document.createElement("time");
    timeEl.textContent = formatTime(created);
    meta.appendChild(timeEl);

    var report = document.createElement("a");
    report.className = "muted chat-report";
    report.href = reportPrefix + message.id;
    report.textContent = "신고";
    meta.appendChild(report);

    inner.appendChild(bubble);
    inner.appendChild(meta);
    row.appendChild(inner);
    box.appendChild(row);
    scrollToBottom();
  }
})();
