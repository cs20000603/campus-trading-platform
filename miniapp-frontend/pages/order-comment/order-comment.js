const { post } = require('../../utils/request');

Page({
  data: {
    orderId: '',
    orderItems: [],
    commentMap: {},
    submitting: false
  },

  onLoad(options) {
    const orderId = options.orderId || '';
    this.setData({ orderId });

    const app = getApp();
    const order = app.globalData.commentOrder;
    if (order && order.orderDetailList && order.orderDetailList.length > 0) {
      const orderItems = order.orderDetailList;
      const commentMap = {};
      orderItems.forEach(item => {
        commentMap[item.goodsId] = { score: 0, content: '' };
      });
      this.setData({ orderItems, commentMap });
    } else {
      wx.showToast({ title: '订单数据异常', icon: 'none' });
      setTimeout(() => { wx.navigateBack(); }, 1000);
    }
  },

  onScoreChange(e) {
    const { goodsid } = e.currentTarget.dataset;
    const value = e.detail;
    const commentMap = { ...this.data.commentMap };
    if (!commentMap[goodsid]) commentMap[goodsid] = { score: 0, content: '' };
    commentMap[goodsid].score = value;
    this.setData({ commentMap });
  },

  onContentInput(e) {
    const { goodsid } = e.currentTarget.dataset;
    const value = e.detail.value || e.detail;
    const commentMap = { ...this.data.commentMap };
    if (!commentMap[goodsid]) commentMap[goodsid] = { score: 0, content: '' };
    commentMap[goodsid].content = value;
    this.setData({ commentMap });
  },

  onSubmit() {
    const { orderId, orderItems, commentMap } = this.data;
    const userInfo = wx.getStorageSync('userInfo');

    // Validate all comments have been filled
    const comments = [];
    for (const item of orderItems) {
      const c = commentMap[item.goodsId];
      if (!c || !c.score) {
        wx.showToast({ title: `请为"${item.goodsName}"评分`, icon: 'none' });
        return;
      }
      if (!c.content || !c.content.trim()) {
        wx.showToast({ title: `请填写"${item.goodsName}"的评价`, icon: 'none' });
        return;
      }
      comments.push({
        score: c.score,
        content: c.content.trim(),
        orderId: orderId,
        goodsId: item.goodsId,
        goodsName: item.goodsName,
        goodsImg: item.goodsImg,
        userId: userInfo ? userInfo.id : '',
        userName: userInfo ? (userInfo.name || userInfo.username || '') : ''
      });
    }

    this.setData({ submitting: true });

    // Submit each comment sequentially
    const submitAll = (index) => {
      if (index >= comments.length) {
        wx.showToast({ title: '评价成功', icon: 'success' });
        setTimeout(() => { wx.navigateBack(); }, 1500);
        return;
      }

      post('/comment/add', comments[index])
        .then(() => {
          submitAll(index + 1);
        })
        .catch(() => {
          this.setData({ submitting: false });
        });
    };

    submitAll(0);
  }
});
