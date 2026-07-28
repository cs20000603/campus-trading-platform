<template>
  <div class="front-container">
    <div style="margin-bottom: 20px">
      <!-- 校园店铺列表页：按类型筛选、浏览营业中店铺、点击进入店铺商品 -->
<h2 style="color: #333; margin-bottom: 5px">校园店铺</h2>
      <p style="color: #999; font-size: 14px">发现校园周边好店</p>
    </div>

    <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap">
      <el-button :type="data.typeFilter === '' ? 'primary' : ''" @click="filterByType('')">全部</el-button>
      <el-button :type="data.typeFilter === '超市' ? 'primary' : ''" @click="filterByType('超市')">超市</el-button>
      <el-button :type="data.typeFilter === '水果店' ? 'primary' : ''" @click="filterByType('水果店')">水果店</el-button>
      <el-button :type="data.typeFilter === '服装店' ? 'primary' : ''" @click="filterByType('服装店')">服装店</el-button>
      <el-button :type="data.typeFilter === '蛋糕店' ? 'primary' : ''" @click="filterByType('蛋糕店')">蛋糕店</el-button>
      <el-button :type="data.typeFilter === '奶茶店' ? 'primary' : ''" @click="filterByType('奶茶店')">奶茶店</el-button>
    </div>

    <!-- 店铺卡片网格：4列布局展示店铺Logo、名称、简介、状态 -->
    <div v-if="data.tableData.length > 0">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in data.tableData" :key="item.id">
          <div class="card shop-item" @click="router.push('/front/goods?shopId=' + item.id + '&shopType=' + (item.type || ''))" style="padding: 0; border-radius: 8px; margin-bottom: 20px; cursor: pointer; overflow: hidden">
            <img :src="item.logo || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="" style="width: 100%; height: 180px; object-fit: cover">
            <div style="padding: 15px">
              <div style="font-size: 18px; font-weight: bold; margin-bottom: 5px">{{ item.name }}</div>
              <div style="color: #666; font-size: 13px; margin-bottom: 8px" class="line2">{{ item.description || '暂无简介' }}</div>
              <el-tag :type="item.status === '营业中' ? 'success' : item.status === '审核中' ? 'warning' : 'info'" size="small">{{ item.status }}</el-tag>
              <span style="margin-left: 10px; font-size: 13px; color: #999" v-if="item.address">{{ item.address }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <div style="text-align: center; margin-top: 20px" v-if="data.total > data.pageSize">
        <el-pagination background layout="prev, pager, next" v-model:current-page="data.pageNum" :page-size="data.pageSize" :total="data.total" @current-change="load" />
      </div>
    </div>
    <div v-else style="padding: 80px; text-align: center; color: #999">
      <el-empty description="暂无店铺" />
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router";
import request from "@/utils/request";

// 响应式数据对象
const data = reactive({
  tableData: [],    // 店铺列表数据
  pageNum: 1,       // 当前页码
  pageSize: 12,     // 每页条数
  total: 0,         // 店铺总数
  typeFilter: '',   // 店铺类型筛选条件
})

// 按店铺类型筛选：清空页码后重新加载
const filterByType = (type) => {
  data.typeFilter = type
  data.pageNum = 1
  load()
}

// 分页查询营业中的店铺
const load = () => {
  request.get('/shop/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      status: '营业中',
      type: data.typeFilter || null
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}
load()
</script>

<style scoped>
.shop-item {
  transition: transform 0.3s;
}
.shop-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
</style>
