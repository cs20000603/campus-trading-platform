const { get, post, put } = require('../../utils/request');
const { onMessage } = require('../../utils/websocket');

Page({
  data: {
    title: '',
    condition: '',
    category: '',
    deliveryType: '',
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    idleList: [],
    conditionOptions: ['全新', '几乎全新', '轻微使用', '明显痕迹'],
    categoryOptions: ['数码', '书籍', '生活用品', '服饰', '美妆', '运动', '乐器', '其他'],
    deliveryOptions: ['自提', '可送', '均可'],
    showFilter: false,
  },

  onLoad() {
    this.loadData(true);
    onMessage(this.handleWsMessage);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadData(false);
    }
  },

  handleWsMessage(msg) {
    if (msg.eventType === 'IDLE_NEW') {
      wx.showToast({ title: '有新宝贝上架！', icon: 'none' });
    }
  },

  loadData(refresh) {
    if (this.data.loading) return;
    this.setData({ loading: true });
    const pageNum = refresh ? 1 : this.data.pageNum + 1;
    get('/idleGoods/selectPage', {
      pageNum, pageSize: this.data.pageSize,
      title: this.data.title || undefined,
      condition: this.data.condition || undefined,
      category: this.data.category || undefined,
      deliveryType: this.data.deliveryType || undefined,
      status: '在售',
    }).then(data => {
      const list = data.list || [];
      const idleList = refresh ? list : [...this.data.idleList, ...list];
      this.setData({ idleList, pageNum, hasMore: list.length >= this.data.pageSize, loading: false });
    }).catch(() => { this.setData({ loading: false }); });
  },

  onSearch(e) {
    this.setData({ title: e.detail });
    this.loadData(true);
  },

  onPublishTap() {
    wx.navigateTo({ url: '/pages/idle-publish/idle-publish' });
  },

  onItemTap(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: '/pages/idle-detail/idle-detail?id=' + id });
  },

  onFilterChange(e) {
    const { field, value } = e.currentTarget.dataset;
    this.setData({ [field]: this.data[field] === value ? '' : value });
    this.loadData(true);
  },
});
