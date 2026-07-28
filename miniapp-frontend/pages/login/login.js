const { accountLogin, wxLogin } = require('../../utils/auth');

Page({
  data: {
    account: '',
    password: '',
    loading: false
  },

  onAccountInput(e) {
    this.setData({ account: e.detail });
  },

  onPwdInput(e) {
    this.setData({ password: e.detail });
  },

  handleAccountLogin() {
    const { account, password } = this.data;
    if (!account || account.trim().length === 0) {
      wx.showToast({ title: '请输入手机号或用户名', icon: 'none' });
      return;
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    accountLogin(account.trim(), password)
      .then(() => {
        wx.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' });
        }, 1000);
      })
      .catch(err => {
        wx.showToast({ title: err.message || '登录失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  onForgotPassword() {
    wx.navigateTo({ url: '/pages/reset-password/reset-password' });
  },

  onRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  },

  handleWxLogin() {
    this.setData({ loading: true });
    wxLogin()
      .then(data => {
        if (data.needBindPhone) {
          wx.showToast({ title: '请先绑定手机号', icon: 'none' });
          setTimeout(() => {
            wx.navigateTo({ url: '/pages/register/register' });
          }, 800);
        } else {
          wx.showToast({ title: '登录成功', icon: 'success' });
          setTimeout(() => {
            wx.switchTab({ url: '/pages/index/index' });
          }, 1000);
        }
      })
      .catch(err => {
        wx.showToast({ title: err.message || '登录失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  }
});
