const { get, del, put } = require('../../utils/request');

Page({
  data: {
    activeTab: 0,
    list: [],
    loading: false,
  },

  onShow() {
    this.loadList();
  },

  onTabChange(e) {
    this.setData({ activeTab: e.detail.index });
    this.loadList();
  },

  loadList() {
    const statusMap = ['在售', '已售出', '已下架'];
    const status = statusMap[this.data.activeTab];
    this.setData({ loading: true });
    get('/idleGoods/selectPage', { status, pageSize: 50 }).then(data => {
      this.setData({ list: data.list || [], loading: false });
    }).catch(() => { this.setData({ loading: false }); });
  },

  onItemTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: '/pages/idle-detail/idle-detail?id=' + id });
  },

  onTakeDown(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '确认下架',
      content: '确定要下架这件商品吗？',
      success: (res) => {
        if (res.confirm) {
          put('/idleGoods/takeDown/' + id).then(() => {
            wx.showToast({ title: '已下架', icon: 'success' });
            this.loadList();
          }).catch(() => {});
        }
      }
    });
  },

  onRelist(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '重新发布',
      content: '确定要重新发布这件商品吗？',
      success: (res) => {
        if (res.confirm) {
          put('/idleGoods/update', { id, status: '在售' }).then(() => {
            wx.showToast({ title: '已重新发布', icon: 'success' });
            this.loadList();
          }).catch(() => {});
        }
      }
    });
  },

  onDelete(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这件商品吗？此操作不可恢复。',
      success: (res) => {
        if (res.confirm) {
          del('/idleGoods/delete/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadList();
          }).catch(() => {});
        }
      }
    });
  },
});
