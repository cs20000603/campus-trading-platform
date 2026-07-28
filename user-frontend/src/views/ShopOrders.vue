<template>
  <div class="front-container">
    <div style="display: flex; align-items: center; margin-bottom: 20px">
      <!-- 店铺订单管理页面：查看本店铺订单、接单、配送、确认完成，支持WebSocket实时推送 -->
<h2 style="flex: 1">店铺订单</h2>
      <el-select v-model="data.statusFilter" placeholder="订单状态" style="width: 140px; margin-right: 10px" clearable @change="load">
        <el-option label="全部" value=""></el-option>
        <el-option label="待接单" value="待接单"></el-option>
        <el-option label="已出货" value="已出货"></el-option>
        <el-option label="已配送" value="已配送"></el-option>
        <el-option label="已完成" value="已完成"></el-option>
        <el-option label="已取消" value="已取消"></el-option>
      </el-select>
    </div>

    <div class="card" style="padding: 20px" v-if="!data.shop">
      <el-empty description="您还没有店铺，请先申请开店">
        <el-button type="primary" @click="router.push('/front/shopRegister')">申请开店</el-button>
      </el-empty>
    </div>

    <template v-else>
      <div v-if="data.orders.length === 0" class="card" style="padding: 60px 20px">
        <el-empty description="暂无订单" />
      </div>

      <div v-for="order in data.orders" :key="order.id" class="card order-card">
        <div class="order-header">
          <span>订单号：{{ order.orderNo }}</span>
          <span>时间：{{ order.time }}</span>
          <el-tag :type="order.status === '待接单' ? 'warning' : order.status === '已出货' ? 'primary' : order.status === '已配送' ? '' : order.status === '已完成' ? 'success' : 'danger'">
            {{ order.status }}
          </el-tag>
        </div>

        <div class="order-body">
          <div class="order-items">
            <div v-for="detail in order.orderDetailList" :key="detail.id" class="order-item">
              <el-image :src="detail.goodsImg" style="width: 60px; height: 60px; border-radius: 4px" fit="cover" />
              <div class="item-info">
                <div class="item-name">{{ detail.goodsName }}</div>
                <div class="item-price">¥{{ detail.goodsPrice }} x {{ detail.num }}</div>
              </div>
            </div>
          </div>
          <div class="order-summary">
            <div>下单用户：{{ order.userName || '用户' + order.userId }}</div>
            <div>配送方式：{{ order.deliverType || '--' }}</div>
            <div v-if="order.address">地址：{{ order.address }}</div>
            <div class="order-total">合计：<span class="price">¥{{ order.total }}</span></div>
          </div>
        </div>

        <div class="order-actions">
          <!-- 待接单：统一操作 -->
          <el-button type="primary" size="small" v-if="order.status === '待接单'" @click="handleAccept(order.id)">接单</el-button>
          <!-- 自提：已出货 → 确认取货 → 已完成 -->
          <el-button type="success" size="small" v-if="order.deliverType === '自提' && order.status === '已出货'" @click="handleComplete(order.id)">确认取货</el-button>
          <!-- 外送：已出货 → 配送 → 已配送 → 送达 → 已完成 -->
          <el-button type="success" size="small" v-if="order.deliverType === '外送' && order.status === '已出货'" @click="handleDeliver(order.id)">开始配送</el-button>
          <el-button type="primary" size="small" v-if="order.deliverType === '外送' && order.status === '已配送'" @click="handleComplete(order.id)">确认送达</el-button>
        </div>
      </div>

      <div class="card" style="margin-top: 10px" v-if="data.total > data.pageSize">
        <el-pagination @current-change="load" background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </template>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox, ElNotification } from "element-plus";
import { onMessage, offMessage } from "@/utils/websocket";
import router from "@/router";

const user = JSON.parse(localStorage.getItem('system-user') || '{}');

// 响应式数据对象
const data = reactive({
  shop: null,        // 当前用户的店铺信息
  orders: [],        // 店铺订单列表
  pageNum: 1,        // 当前页码
  pageSize: 10,      // 每页条数
  total: 0,          // 订单总数
  statusFilter: ''   // 订单状态筛选条件
})

// 加载当前用户的店铺信息，有店铺则加载订单
const loadShop = () => {
  request.get('/userShop/my', { params: { userId: user.id } }).then(res => {
    data.shop = res.data
    if (res.data) {
      load()
    }
  }).catch(() => {
    data.shop = null
  })
}

// 分页查询店铺订单
const load = () => {
  if (!data.shop) return
  request.get('/orders/shopOrders', {
    params: {
      shopId: data.shop.id,
      status: data.statusFilter || undefined,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    data.orders = res.data?.list || []
    data.total = res.data?.total || 0
  })
}

// 接单：将待接单状态更新为已出货
const handleAccept = (id) => {
  ElMessageBox.confirm('确认接单吗？', '提示', { type: 'info' }).then(() => {
    request.put('/orders/update', { id, status: '已出货' }).then(res => {
      if (res.code === '200') {
        ElMessage.success('已接单，开始备货')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

// 开始配送（仅外送订单）
const handleDeliver = (id) => {
  ElMessageBox.confirm('确认开始配送吗？', '提示', { type: 'info' }).then(() => {
    request.put('/orders/update', { id, status: '已配送' }).then(res => {
      if (res.code === '200') {
        ElMessage.success('配送中')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

// 确认完成：自提/外送订单的最终完成操作
const handleComplete = (id) => {
  ElMessageBox.confirm('确认该订单已完成吗？', '提示', { type: 'info' }).then(() => {
    request.put('/orders/update', { id, status: '已完成' }).then(res => {
      if (res.code === '200') {
        ElMessage.success('订单已完成')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

loadShop()

// 处理WebSocket消息：收到新订单通知时自动刷新列表
const handleWsMessage = (msg) => {
  if (msg.eventType === 'ORDER_NEW') {
    ElNotification({ title: '新订单', message: msg.message, type: 'info' })
    load()
  }
}

onMounted(() => onMessage(handleWsMessage))
onUnmounted(() => offMessage(handleWsMessage))
</script>

<style scoped>
.order-card {
  padding: 0;
  margin-bottom: 12px;
  overflow: hidden;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fafafa;
  font-size: 13px;
  color: #666;
  border-bottom: 1px solid #eee;
}
.order-body {
  display: flex;
  padding: 16px 20px;
}
.order-items {
  flex: 1;
}
.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.item-info {
  margin-left: 12px;
}
.item-name {
  font-size: 14px;
  font-weight: 500;
}
.item-price {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.order-summary {
  width: 200px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  border-left: 1px solid #eee;
  padding-left: 16px;
}
.order-total {
  font-size: 15px;
  font-weight: bold;
  color: #333;
  margin-top: 4px;
}
.price {
  color: #f56c6c;
}
.order-actions {
  padding: 12px 20px;
  border-top: 1px solid #eee;
  text-align: right;
}
</style>
