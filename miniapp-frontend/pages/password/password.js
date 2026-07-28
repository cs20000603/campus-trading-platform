const { put } = require('../../utils/request');
const { logout } = require('../../utils/auth');

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    submitting: false
  },

  onOldPwdInput(e) {
    this.setData({ oldPassword: e.detail });
  },

  onNewPwdInput(e) {
    this.setData({ newPassword: e.detail });
  },

  onConfirmPwdInput(e) {
    this.setData({ confirmPassword: e.detail });
  },

  onSubmit() {
    const { oldPassword, newPassword, confirmPassword } = this.data;

    if (!newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'none' });
      return;
    }
    if (newPassword.length < 6) {
      wx.showToast({ title: '新密码至少6位', icon: 'none' });
      return;
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的新密码不一致', icon: 'none' });
      return;
    }
    if (oldPassword === newPassword) {
      wx.showToast({ title: '新密码不能与旧密码相同', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    put('/updatePassword', {
      password: oldPassword,
      newPassword: newPassword
    })
      .then(() => {
        wx.showToast({ title: '密码修改成功，请重新登录', icon: 'success' });
        setTimeout(() => {
          logout();
        }, 1500);
      })
      .catch(() => {})
      .finally(() => {
        this.setData({ submitting: false });
      });
  }
});
