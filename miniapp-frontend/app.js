const { connect } = require('./utils/websocket');

App({
  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      connect();
    }
  },
  globalData: {
    userInfo: null
  }
});
