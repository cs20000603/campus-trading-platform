<template>
  <div class="ai-assistant">
    <div class="ai-float-btn" @click="toggle">
      <el-icon :size="24"><ChatDotRound /></el-icon>
      <span class="ai-label">AI助手</span>
    </div>

    <el-dialog title="AI 智能助手" v-model="visible" width="480px" :close-on-click-modal="false" destroy-on-close>
      <div class="ai-chat-wrap">
        <div class="ai-messages" ref="msgRef">
          <div v-for="(msg, i) in messages" :key="i" :class="['ai-msg', msg.role === 'user' ? 'ai-msg-user' : 'ai-msg-ai']">
            <div class="ai-msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="loading" class="ai-msg ai-msg-ai">
            <div class="ai-msg-content"><el-icon class="is-loading"><Loading /></el-icon> 思考中...</div>
          </div>
        </div>
        <div class="ai-tabs">
          <el-radio-group v-model="mode" size="small">
            <el-radio-button value="chat">智能问答</el-radio-button>
            <el-radio-button value="desc">商品描述</el-radio-button>
            <el-radio-button value="search">智能搜索</el-radio-button>
          </el-radio-group>
        </div>
        <div class="ai-input-row">
          <el-input v-model="input" placeholder="输入你的问题..." @keyup.enter="send" :disabled="loading"></el-input>
          <el-button type="primary" @click="send" :loading="loading">发送</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const mode = ref('chat')
const messages = ref([
  { role: 'ai', content: '你好！我是校园小卖部智能助手，可以帮你：\n1. 解答购物、订单等问题\n2. 生成商品描述文案\n3. 智能搜索商品\n\n切换到对应模式试试吧~' }
])
const msgRef = ref(null)

const scrollBottom = () => {
  nextTick(() => {
    if (msgRef.value) msgRef.value.scrollTop = msgRef.value.scrollHeight
  })
}

const toggle = () => {
  visible.value = !visible.value
}

const send = () => {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollBottom()

  let url, params
  if (mode.value === 'chat') {
    url = '/ai/chat'
    params = { message: text }
  } else if (mode.value === 'desc') {
    url = '/ai/generateDesc'
    params = { name: text }
  } else {
    url = '/ai/smartSearch'
    params = { query: text }
  }

  request.post(url, params).then(res => {
    if (res.code === '200') {
      messages.value.push({ role: 'ai', content: res.data })
    } else {
      messages.value.push({ role: 'ai', content: res.msg || '请求失败，请稍后再试' })
    }
    scrollBottom()
  }).catch(() => {
    messages.value.push({ role: 'ai', content: '网络异常，请稍后再试' })
    scrollBottom()
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 80px;
  z-index: 999;
}
.ai-float-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0c9c7a, #13ce66);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(12, 156, 122, 0.4);
  transition: transform 0.2s;
}
.ai-float-btn:hover {
  transform: scale(1.1);
}
.ai-label {
  font-size: 10px;
  margin-top: 2px;
}
.ai-chat-wrap {
  display: flex;
  flex-direction: column;
  height: 420px;
}
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}
.ai-msg {
  margin-bottom: 10px;
  display: flex;
}
.ai-msg-user {
  justify-content: flex-end;
}
.ai-msg-user .ai-msg-content {
  background: #0c9c7a;
  color: #fff;
  border-radius: 12px 12px 4px 12px;
}
.ai-msg-ai .ai-msg-content {
  background: #fff;
  color: #333;
  border-radius: 12px 12px 12px 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}
.ai-msg-content {
  max-width: 80%;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-tabs {
  margin-bottom: 10px;
  text-align: center;
}
.ai-input-row {
  display: flex;
  gap: 8px;
}
</style>
