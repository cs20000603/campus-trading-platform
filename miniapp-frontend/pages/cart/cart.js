const { get, put, del } = require('../../utils/request');

Page({
  data: {
    cartList: [],
    checkedAll: false,
    totalPrice: 0,
    checkedCount: 0,
    loading: false
  },

  onShow() {
    this.loadCart();
  },

  loadCart() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      this.setData({ cartList: [] });
      return;
    }

    this.setData({ loading: true });
    return get('/cart/selectAll', { userId: userInfo.id })
      .then(data => {
        const cartList = (data || []).map(item => ({
          ...item,
          checked: item.checked !== undefined ? item.checked : false
        }));
        this.setData({ cartList, loading: false });
        this.calcTotal();
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  onCheckChange(e) {
    const { index } = e.currentTarget.dataset;
    const cartList = this.data.cartList;
    cartList[index].checked = !cartList[index].checked;
    this.setData({ cartList });
    this.calcTotal();
  },

  onCheckAllChange() {
    const checkedAll = !this.data.checkedAll;
    const cartList = this.data.cartList.map(item => ({
      ...item,
      checked: checkedAll
    }));
    this.setData({ cartList, checkedAll });
    this.calcTotal();
  },

  calcTotal() {
    const cartList = this.data.cartList;
    let totalPrice = 0;
    let checkedCount = 0;
    let checkedAll = cartList.length > 0;
    cartList.forEach(item => {
      if (item.checked) {
        totalPrice += (item.goodsPrice || 0) * (item.num || 0);
        checkedCount++;
      } else {
        checkedAll = false;
      }
    });
    if (cartList.length === 0) checkedAll = false;

    this.setData({
      totalPrice: Math.round(totalPrice * 100) / 100,
      checkedCount,
      checkedAll
    });
  },

  onStepperChange(e) {
    const { index } = e.currentTarget.dataset;
    const value = e.detail;
    const cartList = this.data.cartList;
    const item = cartList[index];
    const userInfo = wx.getStorageSync('userInfo');

    if (value <= 0) {
      // Delete item if quantity is 0
      this.onDelete(e);
      return;
    }

    item.num = value;
    cartList[index] = item;
    this.setData({ cartList });
    this.calcTotal();

    // Auto-save to server
    put('/cart/update', {
      id: item.id,
      num: value,
      goodsId: item.goodsId,
      userId: userInfo ? userInfo.id : null
    }).catch(() => {
      wx.showToast({ title: '更新失败', icon: 'none' });
    });
  },

  onDelete(e) {
    const { index } = e.currentTarget.dataset;
    const item = this.data.cartList[index];

    wx.showModal({
      title: '提示',
      content: '确定要删除该商品吗？',
      success: (res) => {
        if (res.confirm) {
          del(`/cart/delete/${item.id}`)
            .then(() => {
              const cartList = this.data.cartList;
              cartList.splice(index, 1);
              this.setData({ cartList });
              this.calcTotal();
              wx.showToast({ title: '已删除', icon: 'none' });
            })
            .catch(() => {});
        }
      }
    });
  },

  onCheckout() {
    const checkedItems = this.data.cartList.filter(item => item.checked);
    if (checkedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' });
      return;
    }

    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const orderItems = checkedItems.map(item => ({
      goodsId: item.goodsId,
      num: item.num,
      goodsName: item.goodsName,
      goodsImg: item.goodsImg,
      goodsPrice: item.goodsPrice
    }));

    const app = getApp();
    app.globalData.orderItems = orderItems;

    wx.navigateTo({ url: '/pages/order-create/order-create' });
  }
});
