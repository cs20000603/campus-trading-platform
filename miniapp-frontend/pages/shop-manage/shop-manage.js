const { get, put, upload } = require('../../utils/request');
const { onMessage, offMessage } = require('../../utils/websocket');

Page({
  data: {
    shop: null,
    editing: false,
    saving: false,
    editName: '',
    editDesc: '',
    editPhone: '',
    editAddress: '',
    editType: '',
    editLogo: '',
    editLogoList: []
  },

  onShow() {
    this.loadShop();
    onMessage(this._wsHandler);
  },

  onHide() {
    offMessage(this._wsHandler);
  },

  _wsHandler(msg) {
    if (msg.eventType === 'SHOP_APPROVE' || msg.eventType === 'SHOP_REJECT') {
      wx.showToast({ title: msg.message || '店铺审核状态已更新', icon: 'none' });
      this.loadShop();
    }
  },

  loadShop() {
    get('/shop/my').then(shop => {
      this.setData({ shop });
    }).catch(() => {
      this.setData({ shop: null });
    });
  },

  onEditTap() {
    const { shop } = this.data;
    this.setData({
      editing: true,
      editName: shop.name || '',
      editDesc: shop.description || '',
      editPhone: shop.phone || '',
      editAddress: shop.address || '',
      editType: shop.type || '',
      editLogo: shop.logo || '',
      editLogoList: shop.logo ? [{ url: shop.logo }] : []
    });
  },

  onEditLogoUpload(e) {
    const { file } = e.detail;
    upload('/files/upload', file.url).then(url => {
      this.setData({ editLogo: url });
    }).catch(() => {
      wx.showToast({ title: '上传失败', icon: 'none' });
      this.setData({ editLogoList: [] });
    });
  },
  onDeleteEditLogo() {
    this.setData({ editLogo: '', editLogoList: [] });
  },

  onEditTypeInput(e) { this.setData({ editType: e.detail }); },
  onEditNameInput(e) { this.setData({ editName: e.detail }); },
  onEditDescInput(e) { this.setData({ editDesc: e.detail }); },
  onEditPhoneInput(e) { this.setData({ editPhone: e.detail }); },
  onEditAddressInput(e) { this.setData({ editAddress: e.detail }); },

  onCancelEdit() {
    this.setData({ editing: false });
  },

  onSaveEdit() {
    const { shop, editType, editName, editDesc, editPhone, editAddress, editLogo } = this.data;
    if (!editName) {
      wx.showToast({ title: '请输入店铺名称', icon: 'none' });
      return;
    }
    this.setData({ saving: true });
    put('/shop/update', {
      id: shop.id,
      type: editType,
      name: editName,
      description: editDesc,
      phone: editPhone,
      address: editAddress,
      logo: editLogo
    }).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ editing: false, saving: false });
      this.loadShop();
    }).catch(() => {
      this.setData({ saving: false });
    });
  },

  onManageGoodsTap() {
    const { shop } = this.data;
    wx.navigateTo({ url: '/pages/goods-manage/goods-manage?shopId=' + shop.id + '&shopType=' + (shop.type || '') });
  },

  onShopOrdersTap() {
    wx.navigateTo({ url: '/pages/shop-orders/shop-orders' });
  },

  onRegisterTap() {
    wx.navigateTo({ url: '/pages/shop-register/shop-register' });
  }
});
