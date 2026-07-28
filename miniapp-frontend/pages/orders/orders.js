const { get, put } = require('../../utils/request');
const { onMessage, offMessage } = require('../../utils/websocket');

Page({
  data: {
    activeTab: 0,
    tabs: ['全部', '待接单', '已出货', '已完成'],
    statusMap: ['', '待接单', '已出货', '已完成'],
    orders: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadOrders(true);
  },

  onShow() {
    onMessage(this._wsHandler);
  },

  onHide() {
    offMessage(this._wsHandler);
  },

  _wsHandler(msg) {
    if (msg.eventType === 'ORDER_STATUS') {
      wx.showToast({ title: msg.message || '订单状态已更新', icon: 'none' });
      this.loadOrders(true);
    }
  },

  onTabChange(e) {
    const activeTab = e.detail.index;
    this.setData({
      activeTab,
      orders: [],
      pageNum: 1,
      hasMore: true
    });
    this.loadOrders(true);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadOrders(false);
    }
  },

  loadOrders(refresh) {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      this.setData({ orders: [], loading: false, hasMore: false });
      return;
    }

    if (this.data.loading) return;
    this.setData({ loading: true });

    const pageNum = refresh ? 1 : this.data.pageNum + 1;
    const params = {
      userId: userInfo.id,
      pageNum: pageNum,
      pageSize: this.data.pageSize
    };

    return get('/orders/selectPage', params)
      .then(data => {
        let list = data.list || data || [];
        // Filter by status tab
        const status = this.data.statusMap[this.data.activeTab];
        if (status) {
          list = list.filter(order => order.status === status);
        }

        const orders = refresh ? list : [...this.data.orders, ...list];
        const hasMore = list.length >= this.data.pageSize;

        this.setData({
          orders,
          pageNum,
          hasMore,
          loading: false
        });
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  onCancelOrder(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { id: id, status: '已取消' })
            .then(() => {
              wx.showToast({ title: '已取消', icon: 'none' });
              this.loadOrders(true);
            })
            .catch(() => {});
        }
      }
    });
  },

  onConfirmReceive(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '提示',
      content: '确认已收到商品？',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { id: id, status: '已完成' })
            .then(() => {
              wx.showToast({ title: '已确认收货', icon: 'success' });
              this.loadOrders(true);
            })
            .catch(() => {});
        }
      }
    });
  },

  onCommentOrder(e) {
    const { id, index } = e.currentTarget.dataset;
    const order = this.data.orders[index];
    const app = getApp();
    app.globalData.commentOrder = order;
    wx.navigateTo({ url: `/pages/order-comment/order-comment?orderId=${id}` });
  },

  getStatusTag(status) {
    const map = {
      '待接单': { text: '待接单', color: '#ff976a' },
      '已出货': { text: '已出货', color: '#1989fa' },
      '已配送': { text: '配送中', color: '#1989fa' },
      '已完成': { text: '已完成', color: '#07c160' },
      '已取消': { text: '已取消', color: '#999' }
    };
    return map[status] || { text: status, color: '#999' };
  }
});
