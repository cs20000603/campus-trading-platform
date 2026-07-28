const { get, post, del } = require('../../utils/request');

Page({
  data: {
    goodsId: '',
    goods: {},
    comments: [],
    commentPageNum: 1,
    commentHasMore: true,
    commentLoading: false,
    isCollected: false,
    collectId: null,
    num: 1,
    activeTab: 'detail',
    previewImages: []
  },

  onLoad(options) {
    const goodsId = options.id;
    if (!goodsId) {
      wx.showToast({ title: '商品不存在', icon: 'none' });
      setTimeout(() => { wx.navigateBack(); }, 1000);
      return;
    }
    this.setData({ goodsId });
    this.loadGoodsDetail();
    this.checkCollect();
    this.loadComments(true);
  },

  loadGoodsDetail() {
    return get(`/goods/selectAll`, { status: '上架' })
      .then(data => {
        const goodsList = data || [];
        const goods = goodsList.find(g => String(g.id) === String(this.data.goodsId));
        if (goods) {
          const previewImages = goods.img ? [goods.img] : [];
          this.setData({ goods, previewImages });
        } else {
          // Try direct load
          return get(`/goods/selectPage`, {
            pageNum: 1,
            pageSize: 1,
            id: this.data.goodsId
          }).then(pageData => {
            const list = pageData.list || pageData || [];
            if (list.length > 0) {
              const goods = list[0];
              const previewImages = goods.img ? [goods.img] : [];
              this.setData({ goods, previewImages });
            }
          });
        }
      })
      .catch(() => {
        wx.showToast({ title: '加载商品失败', icon: 'none' });
      });
  },

  checkCollect() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) return;
    get('/collect/selectAll', {
      goodsId: this.data.goodsId,
      userId: userInfo.id
    })
      .then(data => {
        const collects = data || [];
        if (collects.length > 0) {
          this.setData({
            isCollected: true,
            collectId: collects[0].id
          });
        } else {
          this.setData({ isCollected: false, collectId: null });
        }
      })
      .catch(() => {});
  },

  loadComments(refresh) {
    if (this.data.commentLoading) return;
    this.setData({ commentLoading: true });

    const pageNum = refresh ? 1 : this.data.commentPageNum + 1;
    return get('/comment/selectPage', {
      goodsId: this.data.goodsId,
      pageNum: pageNum,
      pageSize: 10
    })
      .then(data => {
        const list = data.list || data || [];
        const comments = refresh ? list : [...this.data.comments, ...list];
        const hasMore = list.length >= 10;
        this.setData({
          comments,
          commentPageNum: pageNum,
          commentHasMore: hasMore,
          commentLoading: false
        });
      })
      .catch(() => {
        this.setData({ commentLoading: false });
      });
  },

  onReachBottom() {
    if (this.data.activeTab === 'comment' && this.data.commentHasMore) {
      this.loadComments(false);
    }
  },

  onPreviewImage() {
    if (this.data.goods.img) {
      wx.previewImage({
        urls: [this.data.goods.img],
        current: this.data.goods.img
      });
    }
  },

  onStepperChange(e) {
    this.setData({ num: e.detail });
  },

  onCollectToggle() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    if (this.data.isCollected) {
      // Delete collect
      del(`/collect/delete/${this.data.collectId}`)
        .then(() => {
          this.setData({ isCollected: false, collectId: null });
          wx.showToast({ title: '已取消收藏', icon: 'none' });
        })
        .catch(() => {});
    } else {
      // Add collect
      post('/collect/add', {
        goodsId: this.data.goodsId,
        goodsName: this.data.goods.name,
        goodsImg: this.data.goods.img,
        goodsPrice: this.data.goods.price,
        userId: userInfo.id
      })
        .then(data => {
          this.setData({
            isCollected: true,
            collectId: data.id || data
          });
          wx.showToast({ title: '已收藏', icon: 'success' });
        })
        .catch(() => {});
    }
  },

  onAddToCart() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { goodsId, goods, num } = this.data;
    post('/cart/add', {
      goodsId: goodsId,
      goodsName: goods.name,
      goodsImg: goods.img,
      goodsPrice: goods.price,
      num: num,
      userId: userInfo.id
    })
      .then(() => {
        wx.showToast({ title: '已加入购物车', icon: 'success' });
      })
      .catch(() => {});
  },

  onBuyNow() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { goodsId, goods, num } = this.data;
    const orderItems = [{
      goodsId: goodsId,
      num: num,
      goodsName: goods.name,
      goodsImg: goods.img,
      goodsPrice: goods.price
    }];

    const app = getApp();
    app.globalData.orderItems = orderItems;

    wx.navigateTo({ url: '/pages/order-create/order-create' });
  },

  onTabChange(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  }
});
