const { get, post } = require('../../utils/request');

Page({
  data: {
    balance: 0,
    // Recharge popup
    showRechargePopup: false,
    amount: '',
    payType: '微信支付',
    rechargeSubmitting: false,
    // History list
    list: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false,
    refreshing: false
  },

  onShow() {
    this.loadBalance();
    this.loadData(true);
  },

  loadBalance() {
    get('/user/info')
      .then(data => {
        const user = data.user || data;
        if (user) {
          this.setData({ balance: user.account || 0 });
          // Update local cache
          const stored = wx.getStorageSync('userInfo') || {};
          stored.account = user.account || 0;
          wx.setStorageSync('userInfo', stored);
        }
      })
      .catch(() => {});
  },

  loadData(refresh) {
    if (this.data.loading) return Promise.resolve();
    const pageNum = refresh ? 1 : this.data.pageNum;
    this.setData({ loading: true });

    return get('/recharge/selectPage', { pageNum, pageSize: this.data.pageSize })
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

  onShowRecharge() {
    this.setData({ showRechargePopup: true, amount: '', payType: '微信支付' });
  },

  onRechargePopupClose() {
    this.setData({ showRechargePopup: false });
  },

  onAmountInput(e) {
    this.setData({ amount: e.detail });
  },

  onPayTypeChange(e) {
    this.setData({ payType: e.detail });
  },

  onRechargeSubmit() {
    const { amount, payType } = this.data;
    const userInfo = wx.getStorageSync('userInfo');

    if (!amount || parseFloat(amount) <= 0) {
      wx.showToast({ title: '请输入有效充值金额', icon: 'none' });
      return;
    }

    this.setData({ rechargeSubmitting: true });
    post('/recharge/add', {
      money: amount,
      type: payType,
      userId: userInfo ? userInfo.id : ''
    })
      .then(() => {
        wx.showToast({ title: '充值成功', icon: 'success' });
        this.setData({ showRechargePopup: false });
        // Refresh balance and history
        this.loadBalance();
        this.loadData(true);
      })
      .catch(() => {})
      .finally(() => {
        this.setData({ rechargeSubmitting: false });
      });
  }
});
