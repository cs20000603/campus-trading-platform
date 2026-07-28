const { get, put, del } = require('../../utils/request');

Page({
  data: {
    list: [],
    searchText: '',
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false,
    refreshing: false,
    // Edit popup
    showEditPopup: false,
    editingComment: null,
    editScore: 0,
    editContent: '',
    editSubmitting: false
  },

  onShow() {
    this.loadData(true);
  },

  loadData(refresh) {
    if (this.data.loading) return Promise.resolve();
    const pageNum = refresh ? 1 : this.data.pageNum;
    this.setData({ loading: true });

    const params = { pageNum, pageSize: this.data.pageSize };
    if (this.data.searchText && this.data.searchText.trim()) {
      params.content = this.data.searchText.trim();
    }

    return get('/comment/selectPage', params)
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

  onSearch(e) {
    this.setData({ searchText: e.detail || '' });
  },

  onSearchConfirm() {
    this.setData({ pageNum: 1, finished: false, list: [] });
    this.loadData(true);
  },

  onSearchClear() {
    this.setData({ searchText: '', pageNum: 1, finished: false, list: [] });
    this.loadData(true);
  },

  onTapGoods(e) {
    const { goodsId } = e.currentTarget.dataset;
    wx.navigateTo({
      url: '/pages/goods-detail/goods-detail?id=' + goodsId
    });
  },

  // Edit comment
  onEdit(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showEditPopup: true,
      editingComment: item,
      editScore: item.score || 0,
      editContent: item.content || ''
    });
  },

  onEditPopupClose() {
    this.setData({ showEditPopup: false });
  },

  onEditScoreChange(e) {
    this.setData({ editScore: e.detail });
  },

  onEditContentInput(e) {
    this.setData({ editContent: e.detail });
  },

  onEditSubmit() {
    const { editingComment, editScore, editContent } = this.data;
    if (!editScore) {
      wx.showToast({ title: '请选择评分', icon: 'none' });
      return;
    }
    if (!editContent || !editContent.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'none' });
      return;
    }
    this.setData({ editSubmitting: true });
    put('/comment/update', {
      id: editingComment.id,
      score: editScore,
      content: editContent.trim()
    })
      .then(() => {
        wx.showToast({ title: '修改成功', icon: 'success' });
        this.setData({ showEditPopup: false });
        // Update local item
        const list = this.data.list.map(item => {
          if (item.id === editingComment.id) {
            return { ...item, score: editScore, content: editContent.trim() };
          }
          return item;
        });
        this.setData({ list });
      })
      .catch(() => {})
      .finally(() => {
        this.setData({ editSubmitting: false });
      });
  },

  // Delete comment
  onDelete(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '提示',
      content: '确定要删除这条评价吗？',
      success: (res) => {
        if (res.confirm) {
          del('/comment/delete/' + id)
            .then(() => {
              wx.showToast({ title: '已删除', icon: 'success' });
              const list = this.data.list.filter(item => item.id !== id);
              this.setData({ list });
            })
            .catch(() => {});
        }
      }
    });
  }
});
