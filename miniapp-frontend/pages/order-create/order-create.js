const { post } = require('../../utils/request');

Page({
  data: {
    orderItems: [],
    deliverType: '自提',
    address: '',
    totalPrice: 0,
    submitting: false,
    showAddress: false
  },

  onLoad() {
    const app = getApp();
    const orderItems = app.globalData.orderItems || [];
    if (orderItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' });
      setTimeout(() => { wx.navigateBack(); }, 1000);
      return;
    }

    let totalPrice = 0;
    orderItems.forEach(item => {
      totalPrice += (item.goodsPrice || 0) * (item.num || 0);
    });
    totalPrice = Math.round(totalPrice * 100) / 100;

    this.setData({
      orderItems,
      totalPrice,
      showAddress: false
    });
  },

  onDeliverTypeChange(e) {
    const deliverType = e.detail;
    this.setData({
      deliverType,
      showAddress: deliverType === '外送'
    });
  },

  onAddressInput(e) {
    this.setData({ address: e.detail });
  },

  onSubmitOrder() {
    if (this.data.submitting) return;

    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { deliverType, address, orderItems } = this.data;

    if (deliverType === '外送' && !address.trim()) {
      wx.showToast({ title: '请输入收货地址', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    const cartList = orderItems.map(item => ({
      goodsId: item.goodsId,
      num: item.num
    }));

    const params = {
      userId: userInfo.id,
      userName: userInfo.name || userInfo.username || '',
      cartList: cartList,
      deliverType: deliverType,
      address: deliverType === '外送' ? address : ''
    };

    post('/orders/add', params)
      .then(() => {
        wx.showToast({ title: '下单成功', icon: 'success' });
        // Clear global order items
        const app = getApp();
        app.globalData.orderItems = [];
        setTimeout(() => {
          wx.switchTab({ url: '/pages/cart/cart' });
        }, 1500);
      })
      .catch(() => {
        this.setData({ submitting: false });
      });
  }
});
