const { get, post, del } = require('../../utils/request');

Page({
  data: {
    list: [],
    loading: false,
    pageNum: 1,
    hasMore: true,
    showPublish: false,
    wantedTitle: '',
    wantedDesc: '',
    wantedBudget: '',
    wantedCategory: '',
    wantedArea: '',
    categoryOptions: ['数码', '书籍', '生活用品', '服饰', '美妆', '运动', '乐器', '其他'],
    showCategory: false,
    submitting: false,
  },

  onLoad() {
    this.loadList(true);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadList(false);
    }
  },

  loadList(refresh) {
    if (this.data.loading) return;
    this.setData({ loading: true });
    const pageNum = refresh ? 1 : this.data.pageNum + 1;
    get('/idleWanted/selectPage', { pageNum, pageSize: 10 }).then(data => {
      const list = data.list || [];
      this.setData({
        list: refresh ? list : [...this.data.list, ...list],
        pageNum,
        hasMore: list.length >= 10,
        loading: false,
      });
    }).catch(() => { this.setData({ loading: false }); });
  },

  onPublishTap() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.id) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    this.setData({ showPublish: true });
  },

  onPublishClose() {
    this.setData({ showPublish: false });
  },

  onTitleInput(e) { this.setData({ wantedTitle: e.detail }); },
  onDescInput(e) { this.setData({ wantedDesc: e.detail }); },
  onBudgetInput(e) { this.setData({ wantedBudget: e.detail }); },
  onAreaInput(e) { this.setData({ wantedArea: e.detail }); },
  onCategoryTap() { this.setData({ showCategory: true }); },
  onCategoryClose() { this.setData({ showCategory: false }); },
  onCategoryChange(e) {
    this.setData({ wantedCategory: this.data.categoryOptions[e.detail.value] });
  },

  onSubmitWanted() {
    const { wantedTitle, wantedBudget, submitting } = this.data;
    if (submitting) return;
    if (!wantedTitle.trim()) { wx.showToast({ title: '请输入求购标题', icon: 'none' }); return; }
    if (!wantedBudget || parseFloat(wantedBudget) <= 0) { wx.showToast({ title: '请输入有效预算', icon: 'none' }); return; }

    this.setData({ submitting: true });
    post('/idleWanted/add', {
      title: wantedTitle.trim(),
      description: this.data.wantedDesc.trim(),
      budget: parseFloat(wantedBudget),
      category: this.data.wantedCategory || '其他',
      campusArea: this.data.wantedArea.trim(),
    }).then(() => {
      wx.showToast({ title: '发布成功！', icon: 'success' });
      this.setData({ showPublish: false, wantedTitle: '', wantedDesc: '', wantedBudget: '', wantedCategory: '', wantedArea: '', submitting: false });
      this.loadList(true);
    }).catch(() => { this.setData({ submitting: false }); });
  },

  onDeleteWanted(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条求购信息吗？',
      success: (res) => {
        if (res.confirm) {
          del('/idleWanted/delete/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadList(true);
          }).catch(() => {});
        }
      }
    });
  },
});
