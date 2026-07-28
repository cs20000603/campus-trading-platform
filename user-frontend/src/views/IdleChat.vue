<template>
  <!-- 闲置商品聊天页面：与卖家/买家在线沟通，支持WebSocket实时消息 -->
<div class="front-container">
    <div class="card" style="padding: 20px; display: flex; flex-direction: column; height: 70vh">
      <!-- 顶部标题栏 -->
      <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center">
        <h4>{{ data.title }} — 与卖家的对话</h4>
        <el-button @click="$router.back()">返回</el-button>
      </div>
      <!-- 消息列表区域：自己的消息靠右显示，对方消息靠左显示 -->
      <div class="chat-messages" ref="msgContainer">
        <div v-for="(msg, i) in data.messages" :key="i" :class="['msg-row', msg.senderId === data.userId ? 'msg-mine' : 'msg-other']">
          <div class="msg-bubble">{{ msg.content }}</div>
          <div class="msg-time">{{ msg.createTime }}</div>
        </div>
        <div v-if="data.messages.length === 0" style="text-align: center; color: #999; padding: 40px">暂无消息，发送第一条消息吧</div>
      </div>
      <!-- 底部输入区域 -->
      <div class="chat-input">
        <el-input v-model="data.text" placeholder="输入消息..." @keyup.enter="send" />
        <el-button type="primary" @click="send" style="margin-left: 10px">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, nextTick, ref, onMounted, onUnmounted } from "vue";
import request from "@/utils/request";
import { useRoute } from "vue-router";
import { onMessage, offMessage } from "@/utils/websocket";

const route = useRoute();
const user = JSON.parse(localStorage.getItem("system-user") || "{}");
const msgContainer = ref(null);

// 响应式数据对象
const data = reactive({
  idleId: route.query.idleId,                               // 闲置商品ID
  otherId: parseInt(route.query.otherId),                    // 聊天对方用户ID
  title: decodeURIComponent(route.query.title || "闲置商品"),  // 商品标题（URL解码）
  userId: user.id,          // 当前登录用户ID
  messages: [],             // 聊天消息列表
  text: "",                 // 输入框中的文本
});

// 加载对话消息历史
const loadMessages = () => {
  if (!data.idleId || !data.otherId) return;
  request.get("/idleMessage/conversation", { params: { idleId: data.idleId, otherId: data.otherId } }).then(res => {
    data.messages = res.data || [];
    scrollToBottom();  // 新消息加载后滚动到底部
  });
};

// 发送消息
const send = () => {
  if (!data.text.trim()) return;
  request.post("/idleMessage/send", {
    idleId: parseInt(data.idleId),
    receiverId: data.otherId,
    content: data.text,
  }).then(() => {
    data.text = "";  // 清空输入框
    loadMessages();  // 重新加载消息列表
  });
};

// 滚动消息容器到底部（显示最新消息）
const scrollToBottom = () => {
  nextTick(() => {
    const el = msgContainer.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
};

// 处理WebSocket推送：收到新消息时自动刷新
const handleWsMessage = (msg) => {
  if (msg.eventType === "IDLE_MESSAGE") {
    loadMessages();
  }
};

// 组件挂载时注册WebSocket监听，卸载时注销
onMounted(() => { onMessage(handleWsMessage); });
onUnmounted(() => { offMessage(handleWsMessage); });

loadMessages();
// 每5秒轮询拉取新消息（WebSocket之外的补充方案）
const timer = setInterval(loadMessages, 5000);
onUnmounted(() => { clearInterval(timer); });
</script>

<style scoped>
.chat-messages {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
}
.msg-row { margin-bottom: 12px; }
.msg-mine { text-align: right; }
.msg-mine .msg-bubble { background: #409eff; color: white; display: inline-block; }
.msg-other .msg-bubble { background: #f0f0f0; display: inline-block; }
.msg-bubble {
  padding: 8px 14px;
  border-radius: 16px;
  max-width: 70%;
  word-break: break-word;
}
.msg-time { font-size: 11px; color: #999; margin-top: 3px; }
.chat-input { display: flex; }
</style>
