const { get } = require('../../utils/request');
const { logout } = require('../../utils/auth');

Page({
  data: {
    userInfo: null,
    accountBalance: 0
  },

  onShow() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({ userInfo });
    }
    // Try to get latest from server
    if (userInfo && userInfo.id) {
      get(`/user/info`)
        .then(data => {
          if (data) {
            wx.setStorageSync('userInfo', data);
            this.setData({
              userInfo: data,
              accountBalance: data.account || 0
            });
          }
        })
        .catch(() => {});
    }
  },

  onOrdersTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/orders/orders' });
  },

  onCollectTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/collect/collect' });
  },

  onCommentListTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/comment-list/comment-list' });
  },

  onRechargeTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/recharge/recharge' });
  },

  onUserProfileTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/user-profile/user-profile' });
  },

  onPasswordTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/password/password' });
  },

  onShopTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/shop-manage/shop-manage' });
  },

  onIdleTap() {
    if (!this.checkLogin()) return;
    wx.navigateTo({ url: '/pages/idle-square/idle-square' });
  },

  onAboutTap() {
    wx.showModal({
      title: '关于',
      content: '校园小卖部 v1.0\n校园外卖配送小程序',
      showCancel: false
    });
  },

  onLogoutTap() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          logout();
        }
      }
    });
  },

  checkLogin() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return false;
    }
    return true;
  }
});
