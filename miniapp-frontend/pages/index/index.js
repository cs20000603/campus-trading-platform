const { get } = require('../../utils/request');

Page({
  data: {
    carousels: [],
    categories: [],
    hotGoods: [],
    newGoods: [],
    searchValue: ''
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    // Reload data when tab is shown
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadData() {
    return Promise.all([
      this.loadCarousels(),
      this.loadCategories(),
      this.loadGoods()
    ]);
  },

  loadCarousels() {
    return get('/carousel/selectAll')
      .then(data => {
        this.setData({ carousels: data || [] });
      })
      .catch(() => {});
  },

  loadCategories() {
    return get('/category/selectAll')
      .then(data => {
        // Limit to 8 categories for the grid row
        const categories = (data || []).slice(0, 8);
        this.setData({ categories });
      })
      .catch(() => {});
  },

  loadGoods() {
    return get('/goods/selectAll', { status: '上架' })
      .then(data => {
        const goodsList = data || [];
        // Hot goods: sorted by saleCount desc, top 6
        const hotGoods = [...goodsList]
          .sort((a, b) => (b.saleCount || 0) - (a.saleCount || 0))
          .slice(0, 6);
        // New goods: sorted by id desc (latest), top 6
        const newGoods = [...goodsList]
          .sort((a, b) => (b.id || 0) - (a.id || 0))
          .slice(0, 6);
        this.setData({ hotGoods, newGoods });
      })
      .catch(() => {});
  },

  onSearchTap() {
    wx.navigateTo({ url: '/pages/search/search' });
  },

  onCarouselTap(e) {
    const { goodsid } = e.currentTarget.dataset;
    if (goodsid) {
      wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${goodsid}` });
    }
  },

  onCategoryTap(e) {
    const { id, name } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/goods-list/goods-list?categoryId=${id}&categoryName=${name}` });
  },

  onGoodsTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${id}` });
  },

  onMoreHotTap() {
    wx.switchTab({ url: '/pages/goods-list/goods-list' });
  },

  onMoreNewTap() {
    wx.switchTab({ url: '/pages/goods-list/goods-list' });
  },

  onAiTap() {
    wx.navigateTo({ url: '/pages/ai-chat/ai-chat' });
  }
});
