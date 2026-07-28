const { post } = require('./request');

function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) {
          post('/auth/login', { code: res.code })
            .then(data => {
              wx.setStorageSync('token', data.token);
              wx.setStorageSync('userInfo', data.user);
              resolve(data);
            })
            .catch(reject);
        } else {
          reject(new Error('wx.login 失败'));
        }
      },
      fail: reject
    });
  });
}

function accountLogin(account, password) {
  return post('/auth/accountLogin', { account, password })
    .then(data => {
      wx.setStorageSync('token', data.token);
      wx.setStorageSync('userInfo', data.user);
      return data;
    });
}

function phoneLogin(phone, password) {
  return post('/auth/phoneLogin', { phone, password })
    .then(data => {
      wx.setStorageSync('token', data.token);
      wx.setStorageSync('userInfo', data.user);
      return data;
    });
}

function register(phone, password) {
  return post('/auth/register', { phone, password })
    .then(data => {
      wx.setStorageSync('token', data.token);
      wx.setStorageSync('userInfo', data.user);
      return data;
    });
}

function bindPhone(phone, password) {
  return post('/auth/bindPhone', { phone, password });
}

function checkLogin() {
  const token = wx.getStorageSync('token');
  if (!token) {
    wx.navigateTo({ url: '/pages/login/login' });
    return false;
  }
  return true;
}

function getUserInfo() {
  return wx.getStorageSync('userInfo') || null;
}

function logout() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');
  wx.reLaunch({ url: '/pages/login/login' });
}

function resetPassword(phone, newPassword) {
  return post('/auth/resetPassword', { phone, newPassword });
}

module.exports = { wxLogin, accountLogin, phoneLogin, register, bindPhone, resetPassword, checkLogin, getUserInfo, logout };
