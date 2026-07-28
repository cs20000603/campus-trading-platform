<!-- 我的收藏页面：以卡片网格展示用户收藏的商品，支持取消收藏操作 -->
<template>
  <div class="front-container">
    <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px">我收藏的商品（{{ data.total }}）</div>
    <!-- 商品卡片网格：点击跳转商品详情，可取消收藏 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in data.tableData" :key="item.id">
        <div @click="router.push('/front/goodsDetail?id=' + item.goodsId)" class="card"
             style="cursor: pointer; width: 100%; padding: 0; border-radius: 5px; margin-bottom: 20px">
          <img :src="item.goodsImg" alt="" style="width: 100%; height: 260px; border-radius: 5px 5px 0 0">
          <div style="padding: 5px">
            <div class="line1" style="font-size: 18px; margin-bottom: 10px">{{ item.goodsName }}</div>
            <div style="display: flex; align-items: center">
              <div style="flex: 1; color: red">￥<b style="font-size: 20px">{{ item.goodsPrice }}</b></div>
              <!-- @click.stop 阻止冒泡，防止触发卡片点击跳转 -->
              <el-button type="danger" @click.stop="cancel(item.id)">取消收藏</el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <div v-if="data.total > 0">
      <el-pagination style="background-color: white; width: fit-content; padding: 5px 10px; border-radius: 5px" @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request";
import router from "@/router";
import {ElMessage} from "element-plus";

// 响应式数据对象
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),  // 当前登录用户
  pageNum: 1,    // 当前页码
  pageSize: 10,  // 每页条数
  total: 0,      // 收藏商品总数
  tableData: [], // 收藏列表数据
})

// 分页查询当前用户的收藏记录
const load = () => {
  request.get('/collect/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 取消收藏：根据收藏记录ID删除
const cancel = (collectId) => {
  request.delete('/collect/delete/' + collectId).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新列表
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>