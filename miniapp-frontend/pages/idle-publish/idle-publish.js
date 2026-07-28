const { post, upload } = require('../../utils/request');

Page({
  data: {
    title: '',
    description: '',
    images: [],
    price: '',
    originalPrice: '',
    condition: '',
    deliveryType: '自提',
    campusArea: '',
    category: '',
    uploading: false,
    submitting: false,
    showCondition: false,
    showCategory: false,
    conditionOptions: ['全新', '几乎全新', '轻微使用', '明显痕迹'],
    categoryOptions: ['数码', '书籍', '生活用品', '服饰', '美妆', '运动', '乐器', '其他'],
    deliveryOptions: ['自提', '可送', '均可'],
  },

  onTitleInput(e) { this.setData({ title: e.detail }); },
  onDescInput(e) { this.setData({ description: e.detail }); },
  onPriceInput(e) { this.setData({ price: e.detail }); },
  onOriginalPriceInput(e) { this.setData({ originalPrice: e.detail }); },
  onAreaInput(e) { this.setData({ campusArea: e.detail }); },

  onConditionChange(e) {
    this.setData({ condition: this.data.conditionOptions[e.detail.value] });
  },
  onCategoryChange(e) {
    this.setData({ category: this.data.categoryOptions[e.detail.value] });
  },
  onDeliveryChange(e) {
    this.setData({ deliveryType: e.detail });
  },
  onConditionTap() { this.setData({ showCondition: true }); },
  onConditionClose() { this.setData({ showCondition: false }); },
  onCategoryTap() { this.setData({ showCategory: true }); },
  onCategoryClose() { this.setData({ showCategory: false }); },

  onChooseImage() {
    const remaining = 9 - this.data.images.length;
    if (remaining <= 0) {
      wx.showToast({ title: '最多上传9张图片', icon: 'none' });
      return;
    }
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sizeType: ['compressed'],
      success: (res) => {
        this.setData({ uploading: true });
        const promises = res.tempFiles.map(file => {
          return upload('/files/upload', file.tempFilePath);
        });
        Promise.all(promises).then(urls => {
          this.setData({
            images: [...this.data.images, ...urls],
            uploading: false,
          });
        }).catch(() => {
          this.setData({ uploading: false });
          wx.showToast({ title: '图片上传失败', icon: 'none' });
        });
      }
    });
  },

  onDeleteImage(e) {
    const { index } = e.currentTarget.dataset;
    const images = [...this.data.images];
    images.splice(index, 1);
    this.setData({ images });
  },

  onSubmit() {
    const { title, price, images, submitting } = this.data;
    if (submitting) return;
    if (!title.trim()) { wx.showToast({ title: '请输入标题', icon: 'none' }); return; }
    if (!price || parseFloat(price) <= 0) { wx.showToast({ title: '请输入有效价格', icon: 'none' }); return; }
    if (images.length === 0) { wx.showToast({ title: '请上传至少一张图片', icon: 'none' }); return; }

    this.setData({ submitting: true });
    const data = {
      title: title.trim(),
      description: this.data.description.trim(),
      images: images.join(','),
      price: parseFloat(price),
      originalPrice: this.data.originalPrice ? parseFloat(this.data.originalPrice) : undefined,
      condition: this.data.condition || '几乎全新',
      deliveryType: this.data.deliveryType || '自提',
      campusArea: this.data.campusArea.trim(),
      category: this.data.category || '其他',
    };

    post('/idleGoods/add', data).then(() => {
      wx.showToast({ title: '发布成功！', icon: 'success' });
      setTimeout(() => { wx.navigateBack(); }, 1500);
    }).catch(() => {
      this.setData({ submitting: false });
    });
  },
});
