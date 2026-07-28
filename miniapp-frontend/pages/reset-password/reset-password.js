const { resetPassword } = require('../../utils/auth');

Page({
  data: {
    phone: '',
    password: '',
    confirmPwd: '',
    loading: false
  },

  onLoad(options) {
    if (options.phone) {
      this.setData({ phone: options.phone });
    }
  },

  onPwdInput(e) {
    this.setData({ password: e.detail });
  },

  onConfirmInput(e) {
    this.setData({ confirmPwd: e.detail });
  },

  handleReset() {
    const { phone, password, confirmPwd } = this.data;
    if (!phone) {
      wx.showToast({ title: '手机号不能为空', icon: 'none' });
      return;
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '新密码至少6位', icon: 'none' });
      return;
    }
    if (password !== confirmPwd) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    resetPassword(phone, password)
      .then(() => {
        wx.showToast({ title: '重置成功，请登录', icon: 'success' });
        setTimeout(() => wx.navigateBack(), 1500);
      })
      .catch(err => {
        wx.showToast({ title: err.message || '重置失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  }
});
