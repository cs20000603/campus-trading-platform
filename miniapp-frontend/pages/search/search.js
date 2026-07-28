const { get } = require('../../utils/request');

Page({
  data: {
    searchValue: '',
    list: [],
    loading: false,
    searched: false,
    suggestions: [],
    showSuggestions: false
  },

  onSearch(e) {
    const value = e.detail || '';
    this.setData({ searchValue: value });
    if (value.trim()) {
      // 联想建议
      if (this._suggestTimer) clearTimeout(this._suggestTimer);
      this._suggestTimer = setTimeout(() => {
        this.fetchSuggestions(value.trim());
      }, 150);
    } else {
      if (this._suggestTimer) clearTimeout(this._suggestTimer);
      this.setData({ list: [], searched: false, suggestions: [], showSuggestions: false });
    }
  },

  onSearchConfirm() {
    const value = this.data.searchValue;
    if (!value || !value.trim()) {
      wx.showToast({ title: '请输入搜索关键词', icon: 'none' });
      return;
    }
    this.setData({ showSuggestions: false });
    this.doSearch(value.trim());
  },

  onSearchClear() {
    if (this._timer) clearTimeout(this._timer);
    if (this._suggestTimer) clearTimeout(this._suggestTimer);
    this.setData({
      searchValue: '',
      list: [],
      searched: false,
      suggestions: [],
      showSuggestions: false
    });
  },

  fetchSuggestions(keyword) {
    get('/goods/suggest', { keyword })
      .then(data => {
        const arr = Array.isArray(data) ? data : [];
        this.setData({ suggestions: arr, showSuggestions: arr.length > 0 });
      })
      .catch(() => {
        this.setData({ suggestions: [], showSuggestions: false });
      });
  },

  onTapSuggestion(e) {
    const keyword = e.currentTarget.dataset.keyword;
    if (this._timer) clearTimeout(this._timer);
    if (this._suggestTimer) clearTimeout(this._suggestTimer);
    this.setData({
      searchValue: keyword,
      suggestions: [],
      showSuggestions: false
    });
    this.doSearch(keyword);
  },

  doSearch(keyword) {
    this.setData({ loading: true, searched: true, showSuggestions: false });
    get('/goods/selectPage', {
      name: keyword,
      pageNum: 1,
      pageSize: 50
    })
      .then(data => {
        const records = data.list || data.records || [];
        this.setData({ list: records });
      })
      .catch(err => {
        console.error('搜索请求失败:', err);
        wx.showToast({ title: '请求失败，请检查后端是否启动', icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  onTapGoods(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({
      url: '/pages/goods-detail/goods-detail?id=' + id
    });
  },

  onAiSearchTap() {
    wx.navigateTo({ url: '/pages/ai-chat/ai-chat?mode=search' });
  }
});
