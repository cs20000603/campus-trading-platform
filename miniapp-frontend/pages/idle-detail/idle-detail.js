const { get, post } = require('../../utils/request');

Page({
  data: {
    id: null,
    goods: null,
    images: [],
    currentImage: 0,
    userInfo: null,
    contactVisible: false,
  },

  onLoad(options) {
    const { id } = options;
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    this.setData({ id: parseInt(id) });
    this.loadDetail();
  },

  loadDetail() {
    get('/idleGoods/selectById/' + this.data.id).then(data => {
      const images = (data.images || '').split(',').filter(Boolean);
      this.setData({ goods: data, images });
    }).catch(() => {});
  },

  onSwiperChange(e) {
    this.setData({ currentImage: e.detail.current });
  },

  onPreviewImage(e) {
    const { index } = e.currentTarget.dataset;
    wx.previewImage({
      urls: this.data.images,
      current: this.data.images[index],
    });
  },

  onBuyTap() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    const goods = this.data.goods;
    if (goods.sellerId === userInfo.id) {
      wx.showToast({ title: '不能购买自己的商品', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '确认购买',
      content: '确定以 ￥' + goods.price + ' 购买「' + goods.title + '」吗？',
      success: (res) => {
        if (res.confirm) {
          post('/idleGoods/buy/' + this.data.id).then(() => {
            wx.showToast({ title: '购买成功！', icon: 'success' });
            setTimeout(() => { wx.navigateBack(); }, 1500);
          }).catch(() => {});
        }
      }
    });
  },

  onContactTap() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    const goods = this.data.goods;
    wx.navigateTo({
      url: '/pages/idle-chat/idle-chat?idleId=' + this.data.id + '&otherId=' + goods.sellerId + '&otherName=' + (goods.sellerName || '卖家'),
    });
  },

  onShareAppMessage() {
    const goods = this.data.goods;
    return {
      title: goods ? goods.title : '闲置商品',
      path: '/pages/idle-detail/idle-detail?id=' + this.data.id,
    };
  },
});
