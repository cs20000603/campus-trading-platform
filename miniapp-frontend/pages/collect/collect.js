const { get, del } = require('../../utils/request');

Page({
  data: {
    list: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false,
    refreshing: false
  },

  onShow() {
    this.loadData(true);
  },

  loadData(refresh) {
    if (this.data.loading) return Promise.resolve();
    const pageNum = refresh ? 1 : this.data.pageNum;
    this.setData({ loading: true });

    return get('/collect/selectPage', { pageNum, pageSize: this.data.pageSize })
      .then(data => {
        const records = data.list || data.records || [];
        const total = data.total || 0;
        const list = refresh ? records : [...this.data.list, ...records];
        this.setData({
          list,
          pageNum: pageNum + 1,
          total,
          finished: list.length >= total,
          refreshing: false
        });
      })
      .catch(() => {
        this.setData({ refreshing: false });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  onPullDownRefresh() {
    this.onRefresh().then(() => wx.stopPullDownRefresh());
  },

  onRefresh() {
    this.setData({ refreshing: true, pageNum: 1, finished: false });
    return this.loadData(true);
  },

  onLoadMore() {
    if (this.data.finished || this.data.loading) return;
    this.loadData(false);
  },

  onUncollect(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '提示',
      content: '确定要取消收藏吗？',
      success: (res) => {
        if (res.confirm) {
          del('/collect/delete/' + id)
            .then(() => {
              wx.showToast({ title: '已取消收藏', icon: 'success' });
              const list = this.data.list.filter(item => item.id !== id);
              this.setData({ list });
            })
            .catch(() => {});
        }
      }
    });
  },

  onTapCard(e) {
    const { goodsId } = e.currentTarget.dataset;
    wx.navigateTo({
      url: '/pages/goods-detail/goods-detail?id=' + goodsId
    });
  }
});
