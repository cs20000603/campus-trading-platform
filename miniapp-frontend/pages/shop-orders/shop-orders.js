const { get, put } = require('../../utils/request');
const { onMessage, offMessage } = require('../../utils/websocket');

Page({
  data: {
    orders: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    statusFilter: '',
    activeTab: 0,
    tabs: [
      { label: '全部', value: '' },
      { label: '待接单', value: '待接单' },
      { label: '已出货', value: '已出货' },
      { label: '已配送', value: '已配送' },
      { label: '已完成', value: '已完成' }
    ]
  },

  onShow() {
    this.load();
    onMessage(this._wsHandler);
  },

  onHide() {
    offMessage(this._wsHandler);
  },

  _wsHandler(msg) {
    if (msg.eventType === 'ORDER_NEW') {
      wx.showToast({ title: msg.message || '您有新的订单', icon: 'none' });
      this.load();
    }
  },

  load() {
    const { pageNum, pageSize, statusFilter } = this.data;
    get('/orders/shopOrders', {
      status: statusFilter || undefined,
      pageNum,
      pageSize
    }).then(res => {
      this.setData({
        orders: res.list || [],
        total: res.total || 0
      });
    }).catch(() => {});
  },

  onTabChange(e) {
    const idx = e.detail.index;
    const tab = this.data.tabs[idx];
    this.setData({
      activeTab: idx,
      statusFilter: tab.value,
      pageNum: 1
    });
    this.load();
  },

  onPageChange(e) {
    this.setData({ pageNum: e.detail });
    this.load();
  },

  handleAccept(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认接单',
      content: '接单后将更新订单状态为已出货',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { id, status: '已出货' }).then(() => {
            wx.showToast({ title: '已接单', icon: 'success' });
            this.load();
          }).catch(() => {});
        }
      }
    });
  },

  handleDeliver(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认配送',
      content: '确认开始配送该订单吗？',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { id, status: '已配送' }).then(() => {
            wx.showToast({ title: '配送中', icon: 'success' });
            this.load();
          }).catch(() => {});
        }
      }
    });
  },

  handleComplete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认完成',
      content: '确认该订单已完成吗？',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { id, status: '已完成' }).then(() => {
            wx.showToast({ title: '订单已完成', icon: 'success' });
            this.load();
          }).catch(() => {});
        }
      }
    });
  }
});
