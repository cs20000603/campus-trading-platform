const { put, upload } = require('../../utils/request');

Page({
  data: {
    userInfo: null,
    avatar: '',
    name: '',
    phone: '',
    account: 0,
    submitting: false
  },

  onShow() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        userInfo,
        avatar: userInfo.avatar || '',
        name: userInfo.name || '',
        phone: userInfo.phone || '',
        account: userInfo.account || 0
      });
    }
  },

  onChooseAvatar() {
    const that = this;
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFilePaths[0];
        wx.showLoading({ title: '上传中...' });
        upload('/files/upload', tempFilePath)
          .then(url => {
            wx.hideLoading();
            that.setData({ avatar: url });
            wx.showToast({ title: '头像上传成功', icon: 'success' });
          })
          .catch(() => {
            wx.hideLoading();
            wx.showToast({ title: '头像上传失败', icon: 'none' });
          });
      }
    });
  },

  onNameInput(e) {
    this.setData({ name: e.detail });
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail });
  },

  onSubmit() {
    const { name, phone, avatar, userInfo } = this.data;
    if (!name || !name.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    put('/user/update', {
      id: userInfo.id,
      name: name.trim(),
      avatar,
      phone: phone || ''
    })
      .then(() => {
        wx.showToast({ title: '保存成功', icon: 'success' });
        // 更新本地缓存
        const updatedUser = {
          ...userInfo,
          name: name.trim(),
          avatar,
          phone: phone || ''
        };
        wx.setStorageSync('userInfo', updatedUser);
        this.setData({ userInfo: updatedUser });
        setTimeout(() => {
          wx.navigateBack();
        }, 1000);
      })
      .catch(() => {})
      .finally(() => {
        this.setData({ submitting: false });
      });
  }
});
