const { BASE_URL } = require('./constants');

const request = (url, options = {}) => {
  const token = wx.getStorageSync('token');
  const header = {
    'Content-Type': 'application/json',
    ...options.header
  };
  if (token) {
    header['Authorization'] = 'Bearer ' + token;
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.reLaunch({ url: '/pages/login/login' });
          reject(new Error('未登录'));
          return;
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data;
          if (data.code === '200') {
            resolve(data.data);
          } else {
            wx.showToast({ title: data.msg || '请求失败', icon: 'none' });
            reject(new Error(data.msg || '请求失败'));
          }
        } else {
          wx.showToast({ title: '网络请求失败', icon: 'none' });
          reject(new Error('网络请求失败'));
        }
      },
      fail(err) {
        wx.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

const get = (url, params = {}) => {
  const queryString = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => key + '=' + encodeURIComponent(params[key]))
    .join('&');
  const fullUrl = queryString ? url + '?' + queryString : url;
  return request(fullUrl, { method: 'GET' });
};

const post = (url, data = {}) => {
  return request(url, { method: 'POST', data });
};

const put = (url, data = {}) => {
  return request(url, { method: 'PUT', data });
};

const del = (url) => {
  return request(url, { method: 'DELETE' });
};

const upload = (url, filePath, formData = {}) => {
  const token = wx.getStorageSync('token');
  const header = {};
  if (token) {
    header['Authorization'] = 'Bearer ' + token;
  }

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + url,
      filePath,
      name: 'file',
      formData,
      header,
      success(res) {
        try {
          const data = JSON.parse(res.data);
          if (data.code === '200') {
            resolve(data.data);
          } else {
            wx.showToast({ title: data.msg || '上传失败', icon: 'none' });
            reject(new Error(data.msg));
          }
        } catch (e) {
          reject(new Error('解析响应失败'));
        }
      },
      fail(err) {
        wx.showToast({ title: '上传失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

module.exports = { get, post, put, del, upload };
