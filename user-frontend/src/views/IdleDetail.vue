<template>
  <!-- 闲置商品详情页：查看闲置商品信息、购买、联系卖家 -->
<div class="front-container">
    <div class="card" style="padding: 20px">
      <div style="display: flex; gap: 20px; flex-wrap: wrap">
        <!-- 左侧：图片轮播展示 -->
        <div style="flex: 1; min-width: 300px; max-width: 500px">
          <el-carousel v-if="data.images.length > 0" height="400px">
            <el-carousel-item v-for="(img, i) in data.images" :key="i">
              <el-image :src="img" style="width: 100%; height: 400px" fit="contain" :preview-src-list="data.images" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div style="flex: 1; min-width: 280px">
          <h2>{{ data.goods.title }}</h2>
          <div style="margin: 15px 0">
            <span style="color: red; font-size: 28px; font-weight: bold">￥{{ data.goods.price }}</span>
            <span v-if="data.goods.originalPrice" style="color: #999; text-decoration: line-through; margin-left: 12px; font-size: 16px">￥{{ data.goods.originalPrice }}</span>
          </div>
          <div style="display: flex; gap: 8px; margin: 12px 0">
            <el-tag type="warning">{{ data.goods.condition }}</el-tag>
            <el-tag>{{ data.goods.deliveryType }}</el-tag>
            <el-tag type="info" v-if="data.goods.campusArea">{{ data.goods.campusArea }}</el-tag>
            <el-tag v-if="data.goods.category">{{ data.goods.category }}</el-tag>
          </div>
          <div style="margin: 15px 0; color: #666; line-height: 1.8; white-space: pre-wrap">{{ data.goods.description }}</div>

          <el-divider />
          <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 15px">
            <el-avatar :size="40" :src="data.goods.sellerAvatar" />
            <div>
              <div style="font-weight: bold">{{ data.goods.sellerName }}
                <el-tag v-if="data.goods.shopName" size="small" type="success" style="margin-left: 6px">{{ data.goods.shopName }}</el-tag>
              </div>
              <div style="font-size: 12px; color: #999">浏览量: {{ data.goods.views }}</div>
            </div>
          </div>

          <div style="display: flex; gap: 10px">
            <el-button type="danger" size="large" @click="buy" :disabled="!data.canBuy">立即购买</el-button>
            <el-button type="primary" size="large" @click="chat">联系卖家</el-button>
            <el-button size="large" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import router from "@/router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRoute } from "vue-router";

const route = useRoute();
const id = route.query.id;
const user = JSON.parse(localStorage.getItem("system-user") || "{}");

// 响应式数据对象
const data = reactive({
  goods: {},      // 闲置商品详情
  images: [],     // 商品图片列表（由逗号分隔的字符串拆分）
  canBuy: false,  // 当前用户是否可以购买（在售且不是自己的商品）
});

// 加载闲置商品详情
const load = () => {
  if (!id) return;
  request.get("/idleGoods/selectById/" + id).then(res => {
    data.goods = res.data || {};
    data.images = (data.goods.images || "").split(",").filter(img => img);
    data.canBuy = data.goods.status === "在售" && data.goods.sellerId !== user.id;
  });
};

// 立即购买：弹出确认框后调用购买接口
const buy = () => {
  ElMessageBox.confirm("确认以 ￥" + data.goods.price + " 购买「" + data.goods.title + "」吗？", "确认购买", { confirmButtonText: "确认购买", cancelButtonText: "取消", type: "warning" }).then(() => {
    request.post("/idleGoods/buy/" + id).then(res => {
      if (res.code === "200") { ElMessage.success("购买成功！"); load(); }
      else { ElMessage.error(res.msg); }
    });
  }).catch(() => {});
};

// 联系卖家：跳转到聊天页面
const chat = () => {
  router.push("/front/idleChat?idleId=" + id + "&otherId=" + data.goods.sellerId + "&title=" + encodeURIComponent(data.goods.title));
};

load();
</script>
