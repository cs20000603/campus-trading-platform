const { get, post, upload } = require('../../utils/request');
const { BASE_URL } = require('../../utils/constants');

Page({
  data: {
    name: '',
    description: '',
    phone: '',
    address: '',
    logo: '',
    logoList: [],
    license: '',
    licenseList: [],
    loading: false,
    existingShop: null,
    type: '',
    typeColumns: ['超市', '水果店', '服装店', '蛋糕店', '奶茶店'],
    typePickerVisible: false,
    showCustomType: false,
    customType: '',
    aiLoading: false
  },

  onLoad() {
    this.checkExistingShop();
    this.loadTypes();
  },

  loadTypes() {
    get('/shop/types').then(types => {
      if (types && types.length > 0) {
        const columns = [...types, '其他（自定义输入）'];
        this.setData({ typeColumns: columns });
      }
    }).catch(() => {});
  },

  checkExistingShop() {
    const { get } = require('../../utils/request');
    get('/shop/my').then(shop => {
      if (shop) {
        this.setData({ existingShop: shop });
      }
    }).catch(() => {});
  },

  onNameInput(e) {
    this.setData({ name: e.detail });
  },
  onDescInput(e) {
    this.setData({ description: e.detail });
  },
  onPhoneInput(e) {
    this.setData({ phone: e.detail });
  },
  onAddressInput(e) {
    this.setData({ address: e.detail });
  },

  onUpload(e) {
    const { file } = e.detail;
    upload('/file/upload', file.url).then(url => {
      this.setData({ logo: url });
    }).catch(() => {
      wx.showToast({ title: '上传失败', icon: 'none' });
      this.setData({ logoList: [] });
    });
  },

  onDeleteLogo() {
    this.setData({ logo: '', logoList: [] });
  },
  onLicenseUpload(e) {
    const { file } = e.detail;
    upload('/file/upload', file.url).then(url => {
      this.setData({ license: url });
    }).catch(() => {
      wx.showToast({ title: '上传失败', icon: 'none' });
      this.setData({ licenseList: [] });
    });
  },
  onDeleteLicense() {
    this.setData({ license: '', licenseList: [] });
  },

  onTypeConfirm(e) {
    const val = e.detail.value;
    if (val === '其他（自定义输入）') {
      this.setData({ showCustomType: true, type: '', typePickerVisible: false });
    } else {
      this.setData({ type: val, showCustomType: false, customType: '', typePickerVisible: false });
    }
  },
  onCustomTypeInput(e) {
    this.setData({ customType: e.detail });
  },
  onTypeCancel() {
    this.setData({ typePickerVisible: false });
  },
  openTypePicker() {
    this.setData({ typePickerVisible: true });
  },
  // AI生成店铺简介：根据店铺名称自动生成描述文案
  aiGenerateDesc() {
    const { name, type, showCustomType, customType } = this.data;
    const finalType = showCustomType ? customType : type;
    if (!name) {
      wx.showToast({ title: '请先输入店铺名称', icon: 'none' });
      return;
    }
    this.setData({ aiLoading: true });
    const { post } = require('../../utils/request');
    post('/ai/generateDesc', {
      name: name + (finalType ? '（' + finalType + '）' : '')
    }).then(res => {
      if (res) {
        this.setData({ description: res });
        wx.showToast({ title: '已生成简介', icon: 'success' });
      }
    }).catch(() => {
      wx.showToast({ title: 'AI生成失败，请重试', icon: 'none' });
    }).finally(() => {
      this.setData({ aiLoading: false });
    });
  },

  handleSubmit() {
    const { name, description, phone, address, logo, type, existingShop, showCustomType, customType } = this.data;
    const finalType = showCustomType ? customType : type;
    if (!finalType || (showCustomType && !customType.trim())) {
      wx.showToast({ title: '请选择或输入经营类型', icon: 'none' });
      return;
    }
    if (!name) {
      wx.showToast({ title: '请输入店铺名称', icon: 'none' });
      return;
    }
    if (!phone) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' });
      return;
    }
    if (existingShop) {
      wx.showToast({ title: '您已经申请过店铺', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    post('/shop/register', {
      name, description, phone, address, logo, type: finalType, license
    }).then(() => {
      wx.showToast({ title: '申请已提交，请等待审核', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 1500);
    }).catch(() => {
      this.setData({ loading: false });
    });
  }
});
