const { get, post, put, del, upload } = require('../../utils/request');

Page({
  data: {
    shopId: '',
    shopType: '',
    goodsList: [],
    categories: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,

    // Form
    formVisible: false,
    formTitle: '新增商品',
    formId: null,
    formName: '',
    formPrice: 0,
    formDiscountPrice: '',
    formDiscountEnd: '',
    formStore: 1,
    formCategoryId: '',
    formCategoryIndex: -1,
    formStatus: '上架',
    formDescription: '',
    formImg: '',
    formImgList: [],
    saving: false,
    aiGenerating: false,

    // Delete
    deleteId: null,
    deleteVisible: false,

    // Category picker
    categoryPickerVisible: false,
    categoryNames: []
  },

  onLoad(options) {
    this.setData({
      shopId: options.shopId || '',
      shopType: options.shopType || ''
    });
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
          this.setData({
            categories,
            categoryNames: categories.map(c => c.name)
          });
        })
        .catch(() => {});
    } else {
      get('/category/selectAll')
        .then(data => {
          const categories = data || [];
          this.setData({
            categories,
            categoryNames: categories.map(c => c.name)
          });
        })
        .catch(() => {});
    }
  },

  onCategoryPickerTap() {
    this.setData({ categoryPickerVisible: true });
  },
  onCategoryConfirm(e) {
    const index = e.detail.index;
    this.setData({
      formCategoryIndex: index,
      formCategoryId: this.data.categories[index] ? this.data.categories[index].id : '',
      categoryPickerVisible: false
    });
  },
  onCategoryCancel() {
    this.setData({ categoryPickerVisible: false });
  },

  loadGoods(refresh) {
    if (this.data.loading) return;
    this.setData({ loading: true });
    const pageNum = refresh ? 1 : this.data.pageNum + 1;
    get('/goods/selectPage', {
      pageNum,
      pageSize: this.data.pageSize,
      shopId: this.data.shopId,
      status: ''  // show all including 下架
    }).then(data => {
      const list = data.list || data || [];
      const goodsList = refresh ? list : [...this.data.goodsList, ...list];
      const hasMore = list.length >= this.data.pageSize;
      this.setData({ goodsList, pageNum, hasMore, loading: false });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  // Add
  onAddTap() {
    this.setData({
      formVisible: true,
      formTitle: '新增商品',
      formId: null,
      formName: '',
      formPrice: 0,
      formDiscountPrice: '',
      formDiscountEnd: '',
      formStore: 1,
      formCategoryId: '',
      formCategoryIndex: -1,
      formStatus: '上架',
      formDescription: '',
      formImg: '',
      formImgList: [],
    });
  },

  // Edit
  onEditTap(e) {
    const { item } = e.currentTarget.dataset;
    const catIndex = this.data.categories.findIndex(c => c.id === item.categoryId);
    this.setData({
      formVisible: true,
      formTitle: '编辑商品',
      formId: item.id,
      formName: item.name || '',
      formPrice: item.price || 0,
      formDiscountPrice: item.discountPrice || '',
      formDiscountEnd: item.discountEnd || '',
      formStore: item.store || 1,
      formCategoryId: item.categoryId || '',
      formCategoryIndex: catIndex,
      formStatus: item.status || '上架',
      formDescription: item.description || '',
      formImg: item.img || '',
      formImgList: item.img ? [{ url: item.img }] : [],
    });
  },

  // AI generate description
  onAiGenerate() {
    const name = this.data.formName;
    if (!name) { wx.showToast({ title: '请先输入商品名称', icon: 'none' }); return; }
    const cat = this.data.categories[this.data.formCategoryIndex];
    this.setData({ aiGenerating: true });
    post('/ai/generateDesc', { name, category: cat ? cat.name : '' }).then(reply => {
      const desc = typeof reply === 'string' ? reply : String(reply || '');
      if (desc) {
        this.setData({ formDescription: desc });
        wx.showToast({ title: '已生成简介', icon: 'success' });
      }
    }).finally(() => {
      this.setData({ aiGenerating: false });
    });
  },

  // Delete confirm
  onDeleteTap(e) {
    const { id } = e.currentTarget.dataset;
    this.setData({ deleteId: id, deleteVisible: true });
  },

  onDeleteConfirm() {
    del('/goods/delete/' + this.data.deleteId).then(() => {
      wx.showToast({ title: '删除成功', icon: 'success' });
      this.setData({ deleteVisible: false, goodsList: [], pageNum: 1, hasMore: true });
      this.loadGoods(true);
    }).catch(() => {});
  },

  onDeleteCancel() {
    this.setData({ deleteVisible: false, deleteId: null });
  },

  // Form handlers
  onFormNameInput(e) { this.setData({ formName: e.detail }); },
  onFormPriceInput(e) { this.setData({ formPrice: Number(e.detail) || 0 }); },
  onFormDiscountPriceInput(e) { this.setData({ formDiscountPrice: e.detail }); },
  onFormDiscountEndInput(e) { this.setData({ formDiscountEnd: e.detail }); },
  onFormStoreInput(e) { this.setData({ formStore: Number(e.detail) || 1 }); },
  onFormDescInput(e) { this.setData({ formDescription: e.detail }); },

  onCategoryChange(e) {
    const index = e.detail.value;
    this.setData({
      formCategoryIndex: index,
      formCategoryId: this.data.categories[index] ? this.data.categories[index].id : ''
    });
  },

  onStatusChange(e) {
    this.setData({ formStatus: e.detail.value });
  },

  onUpload(e) {
    const { file } = e.detail;
    upload('/files/upload', file.url).then(url => {
      this.setData({ formImg: url });
    }).catch(() => {
      wx.showToast({ title: '上传失败', icon: 'none' });
      this.setData({ formImgList: [] });
    });
  },

  onDeleteImg() {
    this.setData({ formImg: '', formImgList: [] });
  },

  onFormCancel() {
    this.setData({ formVisible: false });
  },

  onFormSave() {
    const { formName, formPrice, formStore, formCategoryId, formImg } = this.data;
    if (!formName) { wx.showToast({ title: '请输入名称', icon: 'none' }); return; }
    if (!formImg) { wx.showToast({ title: '请上传图片', icon: 'none' }); return; }
    if (!formCategoryId) { wx.showToast({ title: '请选择分类', icon: 'none' }); return; }

    this.setData({ saving: true });
    const body = {
      name: formName,
      img: formImg,
      price: formPrice,
      discountPrice: this.data.formDiscountPrice || null,
      discountEnd: this.data.formDiscountEnd || null,
      store: formStore,
      categoryId: formCategoryId,
      status: this.data.formStatus,
      description: this.data.formDescription,
      shopId: this.data.shopId
    };

    const request = this.data.formId ? put('/goods/update', { ...body, id: this.data.formId }) : post('/goods/add', body);

    request.then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ formVisible: false, saving: false, goodsList: [], pageNum: 1, hasMore: true });
      this.loadGoods(true);
    }).catch(() => {
      this.setData({ saving: false });
    });
  }
});
