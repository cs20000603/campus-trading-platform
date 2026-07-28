const { get, post } = require('../../utils/request');
const { onMessage, offMessage } = require('../../utils/websocket');

Page({
  data: {
    idleId: null,
    otherId: null,
    otherName: '',
    myId: null,
    messages: [],
    inputText: '',
    scrollToView: '',
  },

  onLoad(options) {
    const { idleId, otherId, otherName } = options;
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      idleId: parseInt(idleId) || 0,
      otherId: parseInt(otherId) || 0,
      otherName: otherName || '用户',
      myId: userInfo ? userInfo.id : 0,
    });
    wx.setNavigationBarTitle({ title: otherName || '聊天' });
    this.loadMessages();
    onMessage(this.handleWsMessage);
  },

  onUnload() {
    offMessage(this.handleWsMessage);
  },

  handleWsMessage(msg) {
    if (msg.eventType === 'IDLE_MESSAGE' && msg.senderId === this.data.otherId) {
      this.loadMessages();
    }
  },

  loadMessages() {
    const { idleId, otherId } = this.data;
    if (!idleId || !otherId) return;
    get('/idleMessage/conversation', { idleId, otherId }).then(data => {
      const list = data || [];
      // Mark messages from other as read
      list.forEach(m => {
        if (m.receiverId === this.data.myId && !m.isRead) {
          post('/idleMessage/read', { idleId, senderId: m.senderId, receiverId: m.receiverId });
        }
      });
      this.setData({
        messages: list,
        scrollToView: list.length > 0 ? 'msg-' + (list.length - 1) : '',
      });
    }).catch(() => {});
  },

  onInputChange(e) {
    this.setData({ inputText: e.detail });
  },

  onSend() {
    const text = this.data.inputText.trim();
    if (!text) return;
    const { idleId, myId, otherId } = this.data;

    post('/idleMessage/send', {
      idleId,
      senderId: myId,
      receiverId: otherId,
      content: text,
    }).then(() => {
      this.setData({ inputText: '' });
      this.loadMessages();
    }).catch(() => {});
  },

  // Polling fallback
  onShow() {
    if (this.data.idleId) {
      this.pollTimer = setInterval(() => {
        this.loadMessages();
      }, 5000);
    }
  },

  onHide() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  },
});
