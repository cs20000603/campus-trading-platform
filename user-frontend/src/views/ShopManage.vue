<template>
  <div class="front-container">
<!-- 店铺管理页面：查看店铺状态、编辑店铺信息、管理商品和订单，支持WebSocket实时审核通知 -->
    <!-- 有店铺时的展示 -->
    <div v-if="data.shop">
      <!-- 店铺标题行：名称 + 状态标签 + 类型标签 -->
      <div style="display: flex; align-items: center; margin-bottom: 20px">
        <h2 style="flex: 1">我的店铺</h2>
        <el-tag :type="data.shop.status === '营业中' ? 'success' : data.shop.status === '线上审核中' ? 'warning' : data.shop.status === '线下审核中' ? '' : data.shop.status === '审核拒绝' ? 'danger' : 'danger'" size="large">
          {{ data.shop.status }}
        </el-tag>
        <el-tag v-if="data.shop.type" type="primary" size="large" style="margin-left: 10px">{{ data.shop.type }}</el-tag>
      </div>

      <!-- 店铺基本信息卡片 -->
      <div class="card" style="padding: 30px; margin-bottom: 20px">
        <div style="display: flex; align-items: flex-start">
          <img :src="data.shop.logo || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" style="width: 100px; height: 100px; border-radius: 8px; object-fit: cover; margin-right: 20px" />
          <div style="flex: 1">
            <div style="font-size: 24px; font-weight: bold; margin-bottom: 10px">{{ data.shop.name }}</div>
            <div style="color: #666; margin-bottom: 10px">{{ data.shop.description || '暂无简介' }}</div>
            <div style="color: #999; font-size: 14px">
              <span style="margin-right: 20px">电话：{{ data.shop.phone || '--' }}</span>
              <span>地址：{{ data.shop.address || '--' }}</span>
            </div>
            <div style="color: #999; font-size: 13px; margin-top: 5px">创建时间：{{ data.shop.createTime || '--' }}</div>
          </div>
        </div>
      </div>

      <!-- 审核被驳回时的提示和重新提交入口 -->
      <div class="card" style="padding: 20px; margin-bottom: 20px; background: #fef0f0" v-if="data.shop.status === '审核拒绝'">
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="color: #f56c6c">
            <strong>申请被驳回</strong>
            <span v-if="data.shop.rejectReason"> — {{ data.shop.rejectReason }}</span>
            <div style="font-size: 13px; margin-top: 5px">请根据修改意见完善信息后重新提交</div>
          </div>
          <el-button type="primary" @click="openEdit">修改并重新提交</el-button>
        </div>
      </div>

      <!-- 营业中店铺的操作入口：编辑信息、管理商品、店铺订单 -->
      <div class="card" style="padding: 20px; margin-bottom: 20px" v-if="data.shop.status === '营业中'">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-button type="primary" size="large" style="width: 100%; height: 80px" @click="openEdit">
              <el-icon style="margin-right: 5px"><Edit /></el-icon>编辑店铺信息
            </el-button>
          </el-col>
          <el-col :span="8">
            <el-button type="success" size="large" style="width: 100%; height: 80px" @click="router.push('/front/shopGoods?shopId=' + data.shop.id + '&shopType=' + (data.shop.type || ''))">
              <el-icon style="margin-right: 5px"><Goods /></el-icon>管理商品
            </el-button>
          </el-col>
          <el-col :span="8">
            <el-button type="warning" size="large" style="width: 100%; height: 80px" @click="router.push('/front/shopOrders')">
              <el-icon style="margin-right: 5px"><List /></el-icon>店铺订单
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 编辑店铺信息弹窗 -->
      <el-dialog v-model="data.editVisible" title="编辑店铺信息" width="500px">
        <el-form :model="data.editForm" label-width="100px">
          <el-form-item label="经营类型">
            <el-select v-model="data.editForm.type" placeholder="请选择经营类型" style="width: 100%" @change="onEditTypeChange">
              <el-option label="超市" value="超市"></el-option>
              <el-option label="水果店" value="水果店"></el-option>
              <el-option label="服装店" value="服装店"></el-option>
              <el-option label="蛋糕店" value="蛋糕店"></el-option>
              <el-option label="奶茶店" value="奶茶店"></el-option>
              <el-option label="其他（自定义输入）" value="__custom__"></el-option>
            </el-select>
            <el-input v-if="data.editShowCustomType" v-model="data.editCustomType" placeholder="请输入经营类型" style="margin-top: 10px" />
          </el-form-item>
          <el-form-item label="店铺Logo">
            <el-upload :action="uploadUrl" list-type="picture" :on-success="handleEditLogoSuccess" :limit="1">
              <el-button type="primary" size="small">更换Logo</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="经营许可证">
            <el-upload :action="uploadUrl" list-type="picture" :on-success="handleEditLicenseSuccess" :limit="1">
              <el-button type="primary" size="small">更换许可证</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="店铺名称">
            <el-input v-model="data.editForm.name" />
          </el-form-item>
          <el-form-item label="店铺简介">
            <el-input v-model="data.editForm.description" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="data.editForm.phone" />
          </el-form-item>
          <el-form-item label="店铺地址">
            <el-input v-model="data.editForm.address" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="data.editVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit" :loading="data.saving">保存</el-button>
        </template>
      </el-dialog>

      <!-- 审核被驳回的提示信息 -->
      <el-alert
        v-if="data.shop.status === '审核拒绝'"
        :title="'审核被驳回' + (data.shop.rejectReason ? '：' + data.shop.rejectReason : '')"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <p v-if="data.shop.rejectReason" style="margin-top: 5px">修改意见：{{ data.shop.rejectReason }}</p>
          <el-button type="primary" size="small" style="margin-top: 10px" @click="openEdit">修改后重新提交</el-button>
        </template>
      </el-alert>

      <!-- 审核中的提示 -->
      <el-alert
        v-if="data.shop.status === '线上审核中' || data.shop.status === '线下审核中'"
        title="您的店铺正在审核中，审核分为线上初审和线下核查两个阶段，审核通过后即可管理商品"
        type="warning"
        show-icon
        :closable="false"
      />
    </div>

    <!-- 无店铺时的空状态展示 -->
    <div v-else style="text-align: center; padding: 80px 0">
      <el-empty description="您还没有店铺">
        <el-button type="primary" @click="router.push('/front/shopRegister')">申请开店</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted } from "vue";
import request from "@/utils/request";
import { ElMessage, ElNotification } from "element-plus";
import { onMessage, offMessage } from "@/utils/websocket";
import { Edit, Goods, List } from "@element-plus/icons-vue";
import router from "@/router";

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'  // 文件上传地址
const user = JSON.parse(localStorage.getItem('system-user') || '{}');

// 响应式数据对象
const data = reactive({
  shop: null,              // 店铺信息（无店铺时为null）
  editVisible: false,      // 编辑弹窗是否显示
  saving: false,           // 保存中的加载状态
  editForm: {},            // 编辑表单数据
  editShowCustomType: false, // 编辑时是否显示自定义类型输入框
  editCustomType: ''       // 编辑时的自定义类型值
})

// 编辑弹窗中经营类型下拉变化：选择"自定义"时显示输入框
const onEditTypeChange = (val) => {
  if (val === '__custom__') {
    data.editShowCustomType = true
    data.editForm.type = ''
  } else {
    data.editShowCustomType = false
    data.editCustomType = ''
  }
}

// 加载当前用户的店铺信息
const loadShop = () => {
  request.get('/userShop/my', { params: { userId: user.id } }).then(res => {
    data.shop = res.data
  }).catch(() => {
    data.shop = null
  })
}
loadShop()

// 编辑弹窗中Logo上传成功回调
const handleEditLogoSuccess = (res) => {
  data.editForm.logo = res.data
}
// 编辑弹窗中经营许可证上传成功回调
const handleEditLicenseSuccess = (res) => {
  data.editForm.license = res.data
}

// 打开编辑弹窗：将当前店铺信息填充到表单中
const openEdit = () => {
  if (data.shop) {
    data.editForm = {
      name: data.shop.name,
      description: data.shop.description,
      phone: data.shop.phone,
      address: data.shop.address,
      logo: data.shop.logo || '',
      type: data.shop.type || '',
      license: data.shop.license || ''
    }
    data.editShowCustomType = false
    data.editCustomType = ''
    data.editVisible = true
  }
}

// 保存编辑后的店铺信息
const saveEdit = () => {
  data.saving = true
  const payload = {
    id: data.shop.id,
    ownerId: user.id,
    name: data.editForm.name,
    description: data.editForm.description,
    phone: data.editForm.phone,
    address: data.editForm.address,
    logo: data.editForm.logo,
    type: data.editForm.type,
    license: data.editForm.license
  }
  // 如果选择了自定义类型，使用自定义输入的值
  if (data.editShowCustomType && data.editCustomType.trim()) {
    payload.type = data.editCustomType.trim()
  }
  request.put('/userShop/update', payload).then(res => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      data.editVisible = false
      data.saving = false
      loadShop()  // 刷新店铺信息
    } else {
      ElMessage.error(res.msg)
      data.saving = false
    }
  }).catch(() => {
    data.saving = false
  })
}

// 处理WebSocket消息：审核通过或拒绝时刷新店铺状态
const handleWsMessage = (msg) => {
  if (msg.eventType === 'SHOP_APPROVE' || msg.eventType === 'SHOP_REJECT') {
    ElNotification({ title: '审核通知', message: msg.message, type: msg.eventType === 'SHOP_APPROVE' ? 'success' : 'warning' })
    loadShop()
  }
}

// 组件挂载时注册WebSocket监听，卸载时注销
onMounted(() => onMessage(handleWsMessage))
onUnmounted(() => offMessage(handleWsMessage))
</script>
