<!-- 用户前端主布局：顶部导航栏（菜单、搜索、用户下拉）+ 路由视图 + 底部页脚 + AI助手 -->
<template>
  <div>
    <!-- 顶部导航栏：Logo + 菜单 + 搜索框 + 用户下拉 -->
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center;">
      <!-- 左侧Logo -->
      <div style="width: 20%">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">校园小卖部</div>
        </div>
      </div>
      <!-- 中间导航菜单和搜索框 -->
      <div style="width: 60%; height: 60px; display: flex; align-items: center">
        <div style="flex: 1">
          <el-menu router :default-active="router.currentRoute.value.path" style="background-color: #2e3143;" ellipsis mode="horizontal">
            <el-menu-item index="/front/home">首页</el-menu-item>
            <el-menu-item index="/front/goods">精选商品</el-menu-item>
            <el-menu-item index="/front/shop">校园店铺</el-menu-item>
            <el-menu-item index="/front/cart">购物车</el-menu-item>
            <el-menu-item index="/front/userOrders">商品订单</el-menu-item>
            <el-menu-item index="/front/idleSquare">闲置广场</el-menu-item>
          </el-menu>
        </div>
        <!-- 搜索框（商品页不显示以避免重复） -->
        <div style="width: fit-content" v-if="router.currentRoute.value.path !== '/front/goods'">
          <el-autocomplete
            v-model="data.goodsName"
            :fetch-suggestions="querySearch"
            :trigger-on-focus="false"
            placeholder="请输入商品名称查询"
            @select="handleSelect"
            @keyup.enter="search"
            style="width: 300px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-autocomplete>
        </div>
      </div>
      <!-- 右侧用户头像和下拉菜单 -->
      <div style="width: 20%; text-align: right; padding-right: 10px;">
        <el-dropdown>
          <div style="display: flex; align-items: center;">
            <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
            <span style="color: #fff; margin-left: 5px">{{ data.user.name || '代码小王' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click.native="router.push('/front/userRecharge')">我的充值</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/userCollect')">我的收藏</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/userComment')">我的评价</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/shopManage')">我的店铺</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click.native="router.push('/front/password')">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

      </div>
    </div>

    <!-- 路由视图容器：所有子页面在此渲染 -->
    <div style="background-color: #f0f2ff">
      <router-view @updateUser="updateUser" />
    </div>

    <!-- 底部页脚组件 -->
    <Footer />

    <!-- AI助手悬浮组件 -->
    <AiAssistant />

  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";
import { Search } from "@element-plus/icons-vue";
import Footer from "@/components/Footer.vue";
import AiAssistant from "@/components/AiAssistant.vue";
import request from "@/utils/request";

// 响应式数据对象
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),  // 当前登录用户
  goodsName: null  // 搜索框关键词
})

// 搜索自动补全：根据输入从后端获取商品名称建议
const querySearch = (queryString, cb) => {
  if (!queryString) {
    cb([])
    return
  }
  request.get('/goods/suggest', { params: { keyword: queryString } }).then(res => {
    const list = (res.data || []).map(name => ({ value: name }))
    cb(list)
  }).catch(() => cb([]))
}

// 选中搜索建议时跳转到商品列表页
const handleSelect = (item) => {
  router.push('/front/goods?name=' + item.value)
  data.goodsName = null
}

// 回车搜索：跳转到商品列表页
const search = () => {
  if (data.goodsName) {
    router.push('/front/goods?name=' + data.goodsName)
    data.goodsName = null
  }
}

// 未登录则跳转到登录页
if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

// 退出登录：清除本地用户信息并跳转到登录页
const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
  ElMessage.success('退出成功')
}

// 更新Front里面的user对象为最新值（子组件修改个人信息后回调）
const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
</script>

<style>
.el-tooltip__trigger {
  cursor: pointer;
  outline: none !important;
}
.el-menu--horizontal .el-menu-item{
  color: white;
}
.el-menu--horizontal {
  border: none !important;
}
.el-menu--horizontal .el-menu-item {
  border: none;
  height: 60px;
}
.el-menu--horizontal .el-menu-item.is-active {
  border: none;
  color: white !important;
  background-color: #0c9c7a !important;
}
.el-menu--horizontal .el-menu-item:not(.is-active):hover {
  color: white;
  background-color: #8db6ab !important;
}
</style>