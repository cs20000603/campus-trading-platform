Component({
  properties: {
    goods: {
      type: Object,
      value: {}
    }
  },

  methods: {
    onTap() {
      const goods = this.properties.goods;
      if (goods && goods.id) {
        wx.navigateTo({
          url: '/pages/goods-detail/goods-detail?id=' + goods.id
        });
      }
    }
  }
});
