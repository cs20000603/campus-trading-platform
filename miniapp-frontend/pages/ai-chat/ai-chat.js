const { post } = require('../../utils/request');

Page({
  data: {
    messages: [],
    input: '',
    loading: false,
    mode: 'chat',   // chat | desc | search
    modeLabel: '智能问答'
  },

  onLoad(options) {
    const mode = options.mode || 'chat';
    const labels = { chat: '智能问答', desc: '商品描述', search: '智能搜索' };
    const hints = {
      chat: '解答购物、订单等问题',
      desc: '输入商品名称，生成吸睛文案',
      search: '描述你想要的商品，智能提取关键词'
    };
    this.setData({
      mode,
      modeLabel: labels[mode] || '智能问答',
      messages: [{
        role: 'ai',
        content: '你好！当前模式：【' + (labels[mode] || mode) + '】\n' + (hints[mode] || '') + '\n\n也可以切换其他模式使用~'
      }]
    });
  },

  onInput(e) {
    this.setData({ input: e.detail.value });
  },

  onModeChange(e) {
    const mode = e.currentTarget.dataset.mode;
    const labels = { chat: '智能问答', desc: '商品描述', search: '智能搜索' };
    this.setData({ mode, modeLabel: labels[mode] || mode });
  },

  onSend() {
    const text = this.data.input.trim();
    if (!text || this.data.loading) return;

    const messages = [...this.data.messages, { role: 'user', content: text }];
    this.setData({ messages, input: '', loading: true }, () => {
      this.scrollToBottom();
    });

    let url, params;
    if (this.data.mode === 'chat') {
      url = '/ai/chat';
      params = { message: text };
    } else if (this.data.mode === 'desc') {
      url = '/ai/generateDesc';
      params = { name: text };
    } else {
      url = '/ai/smartSearch';
      params = { query: text };
    }

    post(url, params).then(reply => {
      const newMessages = [...this.data.messages, { role: 'ai', content: typeof reply === 'string' ? reply : (reply || '无结果') }];
      this.setData({ messages: newMessages, loading: false }, () => {
        this.scrollToBottom();
      });
    }).catch(() => {
      const newMessages = [...this.data.messages, { role: 'ai', content: '请求失败，请稍后再试' }];
      this.setData({ messages: newMessages, loading: false });
    });
  },

  scrollToBottom() {
    wx.createSelectorQuery().select('#msg-list').boundingClientRect().exec(res => {
      if (res[0]) {
        wx.pageScrollTo({ scrollTop: res[0].height + 200, duration: 200 });
      }
    });
  }
});
