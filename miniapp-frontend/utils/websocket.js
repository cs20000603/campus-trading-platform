const BASE_URL = 'ws://localhost:9091';
const RECONNECT_DELAY = 5000;

let socketTask = null;
let listeners = [];
let isManualClose = false;
let reconnectTimer = null;

function connect() {
  const token = wx.getStorageSync('token');
  if (!token) return;

  if (socketTask) {
    socketTask.close({ code: 1000, reason: 'reconnect' });
  }

  socketTask = wx.connectSocket({
    url: BASE_URL + '/ws/miniapp',
    header: {
      'Authorization': 'Bearer ' + token
    }
  });

  socketTask.onOpen(() => {
    console.log('[WS] 小程序 WebSocket 已连接');
    isManualClose = false;
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
  });

  socketTask.onMessage(res => {
    try {
      const data = JSON.parse(res.data);
      notifyListeners(data);
    } catch (e) {
      // ignore
    }
  });

  socketTask.onClose(res => {
    console.log('[WS] WebSocket 断开, code:', res.code);
    if (!isManualClose) {
      reconnectTimer = setTimeout(connect, RECONNECT_DELAY);
    }
  });

  socketTask.onError(err => {
    console.error('[WS] WebSocket 错误:', err);
    // onClose will fire after onError, reconnect handled there
  });
}

function disconnect() {
  isManualClose = true;
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  if (socketTask) {
    socketTask.close({ code: 1000, reason: 'user disconnect' });
    socketTask = null;
  }
}

function onMessage(callback) {
  listeners.push(callback);
}

function offMessage(callback) {
  listeners = listeners.filter(fn => fn !== callback);
}

function notifyListeners(data) {
  listeners.forEach(fn => {
    try { fn(data); } catch (e) { /* ignore */ }
  });
}

module.exports = { connect, disconnect, onMessage, offMessage };
