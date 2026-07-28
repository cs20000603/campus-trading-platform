const { register } = require('../../utils/auth');

Page({
  data: {
    phone: '',
    password: '',
    confirmPassword: '',
    submitting: false
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail });
  },

  onPwdInput(e) {
    this.setData({ password: e.detail });
  },

  onConfirmPwdInput(e) {
    this.setData({ confirmPassword: e.detail });
  },

  onSubmit() {
    const { phone, password, confirmPassword } = this.data;

    if (!phone || phone.length !== 11) {
      wx.showToast({ title: '请输入11位手机号', icon: 'none' });
      return;
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    register(phone, password)
      .then(() => {
        wx.showToast({ title: '注册成功', icon: 'success' });
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' });
        }, 1000);
      })
      .catch(err => {
        wx.showToast({ title: err.message || '注册失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ submitting: false });
      });
  }
});
