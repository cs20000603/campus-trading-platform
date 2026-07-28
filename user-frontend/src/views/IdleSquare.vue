<template>
  <!-- 闲置广场页面：浏览在售闲置商品、按条件筛选、发布闲置、跳转求购板 -->
<div class="front-container">
    <div class="card" style="padding: 20px">
      <!-- 筛选栏：标题搜索、成色、分类、配送方式筛选 + 操作按钮 -->
      <div style="display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; align-items: center">
        <el-input v-model="data.title" placeholder="搜索闲置商品..." clearable style="width: 220px" @change="load" />
        <el-select v-model="data.condition" placeholder="成色" clearable style="width: 120px" @change="load">
          <el-option label="全新" value="全新" />
          <el-option label="几乎全新" value="几乎全新" />
          <el-option label="轻微使用" value="轻微使用" />
          <el-option label="明显痕迹" value="明显痕迹" />
        </el-select>
        <el-select v-model="data.category" placeholder="分类" clearable style="width: 120px" @change="load">
          <el-option label="数码" value="数码" />
          <el-option label="书籍" value="书籍" />
          <el-option label="生活用品" value="生活用品" />
          <el-option label="服饰" value="服饰" />
          <el-option label="美妆" value="美妆" />
          <el-option label="运动" value="运动" />
          <el-option label="乐器" value="乐器" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-model="data.deliveryType" placeholder="配送方式" clearable style="width: 120px" @change="load">
          <el-option label="自提" value="自提" />
          <el-option label="可送" value="可送" />
          <el-option label="均可" value="均可" />
        </el-select>
        <el-button type="primary" @click="$router.push('/front/idlePublish')">发布闲置</el-button>
        <el-button @click="$router.push('/front/idleWanted')">求购板</el-button>
        <el-button @click="$router.push('/front/myIdle')">我的闲置</el-button>
      </div>

      <div v-if="data.tableData.length === 0" style="text-align: center; padding: 80px 0; color: #999">
        <el-empty description="暂无闲置商品" />
      </div>

      <div v-else class="idle-grid">
        <div class="idle-card" v-for="item in data.tableData" :key="item.id" @click="$router.push('/front/idleDetail?id=' + item.id)">
          <img :src="item.images?.split(',')[0]" class="idle-card-img" />
          <div class="idle-card-body">
            <div class="idle-card-title">{{ item.title }}</div>
            <div class="idle-card-price">￥{{ item.price }} <span class="idle-card-original" v-if="item.originalPrice">￥{{ item.originalPrice }}</span></div>
            <div class="idle-card-tags">
              <el-tag size="small" type="warning">{{ item.condition }}</el-tag>
              <el-tag size="small">{{ item.deliveryType }}</el-tag>
              <el-tag size="small" type="info" v-if="item.campusArea">{{ item.campusArea }}</el-tag>
            </div>
            <div class="idle-card-seller">
              <span>{{ item.sellerName }} {{ item.shopName ? '· ' + item.shopName : '' }}</span>
              <span>{{ item.createTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <div style="text-align: center; margin-top: 20px">
        <el-pagination background layout="prev, pager, next" :total="data.total" :page-size="data.pageSize" v-model:current-page="data.pageNum" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import { onMessage, offMessage } from "@/utils/websocket";
import { ElNotification } from "element-plus";

// 响应式数据对象
const data = reactive({
  title: "",         // 标题搜索关键词
  condition: "",     // 成色筛选条件
  category: "",      // 分类筛选条件
  deliveryType: "",  // 配送方式筛选条件
  pageNum: 1,        // 当前页码
  pageSize: 10,      // 每页条数
  total: 0,          // 总记录数
  tableData: [],     // 在售闲置商品列表
});

// 分页查询在售状态的闲置商品
const load = () => {
  request.get("/idleGoods/selectPage", {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title || undefined,
      condition: data.condition || undefined,
      category: data.category || undefined,
      deliveryType: data.deliveryType || undefined,
      status: "在售",
    },
  }).then(res => {
    data.tableData = res.data?.list || [];
    data.total = res.data?.total || 0;
  });
};

// 处理WebSocket消息：接收新闲置商品通知
const handleWsMessage = (msg) => {
  if (msg.eventType === "IDLE_NEW") {
    ElNotification({ title: "闲置广场", message: msg.message, type: "info", duration: 5000 });
  }
};

onMessage(handleWsMessage);
load();
</script>

<style scoped>
.idle-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
}
.idle-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.idle-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.idle-card-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
.idle-card-body { padding: 10px; }
.idle-card-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}
.idle-card-price { color: red; font-size: 18px; font-weight: bold; }
.idle-card-original { color: #999; font-size: 12px; text-decoration: line-through; margin-left: 8px; }
.idle-card-tags { display: flex; gap: 4px; margin: 8px 0; }
.idle-card-seller {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}
</style>
