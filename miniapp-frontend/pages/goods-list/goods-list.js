const { get } = require('../../utils/request');

Page({
  data: {
    categories: [],
    activeCategoryId: '',
    activeCategoryName: '全部',
    shopId: '',
    shopType: '',
    searchName: '',
    goodsList: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    if (options.categoryId) {
      this.setData({
        activeCategoryId: options.categoryId,
        activeCategoryName: options.categoryName || ''
      });
    }
    if (options.shopId) {
      this.setData({ shopId: options.shopId, shopType: options.shopType || '' });
    } else if (options.shopType) {
      this.setData({ shopType: options.shopType });
    }
    this.loadCategories();
    this.loadGoods(true);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadGoods(false);
    }
  },

  loadCategories() {
    if (this.data.shopType) {
      get('/category/selectByShopType', { shopType: this.data.shopType })
        .then(data => {
          const categories = data || [];
          categories.unshift({ id: '', name: '全部' });
          this.setData({ categories });
        })
        .catch(() => {});
    } else {
      get('/category/selectAll')
        .then(data => {
          const categories = data || [];
          categories.unshift({ id: '', name: '全部' });
          this.setData({ categories });
        })
        .catch(() => {});
    }
  },

  loadGoods(refresh) {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const pageNum = refresh ? 1 : this.data.pageNum + 1;
    const params = {
      status: '上架',
      pageNum: pageNum,
      pageSize: this.data.pageSize
    };
    if (this.data.activeCategoryId) {
      params.categoryId = this.data.activeCategoryId;
    }
    if (this.data.searchName) {
      params.name = this.data.searchName;
    }
    if (this.data.shopId) {
      params.shopId = this.data.shopId;
    }

    return get('/goods/selectPage', params)
      .then(data => {
        const list = data.list || data || [];
        const goodsList = refresh ? list : [...this.data.goodsList, ...list];
        const hasMore = list.length >= this.data.pageSize;
        this.setData({
          goodsList,
          pageNum,
          hasMore,
          loading: false
        });
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  onCategoryTap(e) {
    const { id, name } = e.currentTarget.dataset;
    if (id === this.data.activeCategoryId) return;
    this.setData({
      activeCategoryId: id,
      activeCategoryName: name,
      goodsList: [],
      pageNum: 1,
      hasMore: true
    });
    this.loadGoods(true);
  },

  onSearchConfirm(e) {
    this.setData({
      searchName: e.detail.value || e.detail || '',
      goodsList: [],
      pageNum: 1,
      hasMore: true
    });
    this.loadGoods(true);
  },

  onSearchClear() {
    this.setData({
      searchName: '',
      goodsList: [],
      pageNum: 1,
      hasMore: true
    });
    this.loadGoods(true);
  },

  onGoodsTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${id}` });
  }
});
