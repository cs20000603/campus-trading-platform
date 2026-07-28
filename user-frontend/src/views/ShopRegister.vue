<template>
  <!-- 申请开店页面：填写店铺信息、上传资质文件、提交管理员审核 -->
<div class="front-container">
    <div style="margin-bottom: 20px">
      <h2>申请开店</h2>
      <p style="color: #999; font-size: 14px">填写店铺信息，提交后将由管理员审核</p>
    </div>

    <!-- 重要提示：仅校外商户可申请，审核分为线上初审和线下核查两个阶段 -->
    <el-alert
      title="重要提示：本平台仅供校外合法商户申请入驻，在校学生不得申请开店。申请需提交经营许可证等证件，审核分为线上初审和线下实地核查两个阶段，线下核查包括店铺类型、商品质量、经营许可证等证件核验。"
      type="error"
      :closable="false"
      show-icon
      style="margin-bottom: 20px; line-height: 1.8"
    />

    <!-- 已有店铺提示 -->
    <el-alert
      v-if="data.existingShop"
      :title="'您已有一家店铺「' + data.existingShop.name + '」，状态：' + data.existingShop.status"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />

    <!-- 开店表单 -->
    <div class="card" style="padding: 30px; max-width: 600px">
      <el-form :model="data.form" ref="formRef" :rules="data.rules" label-width="80px">
        <!-- 经营类型：预定义类型 + 自定义输入 -->
        <el-form-item label="经营类型" prop="type">
          <el-select v-model="data.typeSelect" placeholder="请选择经营类型" style="width: 100%" @change="onTypeChange">
            <el-option v-for="t in data.typeList" :key="t" :label="t" :value="t"></el-option>
            <el-option label="其他（自定义输入）" value="__custom__"></el-option>
          </el-select>
          <el-input v-if="data.showCustomType" v-model="data.form.type" placeholder="请输入经营类型，如：文具店、打印店等" style="margin-top: 10px" />
        </el-form-item>
        <el-form-item label="店铺名称" prop="name">
          <el-input v-model="data.form.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="店铺简介" prop="description">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input v-model="data.form.description" type="textarea" :rows="3" placeholder="简单介绍一下你的店铺" style="flex: 1" />
            <el-button type="warning" size="small" @click="aiGenerateDesc" :loading="data.aiLoading" style="align-self: flex-start; white-space: nowrap">AI 生成</el-button>
          </div>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="data.form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="data.form.address" placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item label="店铺Logo">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleLogoSuccess" :limit="1">
            <el-button type="primary">上传Logo</el-button>
          </el-upload>
        </el-form-item>
        <!-- 经营许可证上传 -->
        <el-form-item label="经营许可证">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleLicenseSuccess" :limit="1">
            <el-button type="primary">上传许可证</el-button>
          </el-upload>
        </el-form-item>
        <!-- 已有店铺时禁用提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="data.loading" :disabled="!!data.existingShop">提交申请</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const user = JSON.parse(localStorage.getItem('system-user') || '{}');
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'  // 文件上传接口地址

// 响应式数据对象
const data = reactive({
  loading: false,       // 提交加载状态
  existingShop: null,   // 用户已有的店铺（若无则为null，有则禁用提交）
  showCustomType: false,// 是否显示自定义类型输入框
  typeSelect: '',       // 经营类型下拉框选中值
  typeList: ['超市', '水果店', '服装店', '蛋糕店', '奶茶店'],  // 预设经营类型列表
  aiLoading: false,     // AI生成简介加载状态
  form: {               // 开店表单数据
    ownerId: user.id
  },
  rules: {              // 表单校验规则
    type: [{ required: true, message: '请选择经营类型', trigger: 'change' }],
    name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  }
})

// 经营类型下拉变化：选择"自定义"时显示输入框，否则使用选中值
const onTypeChange = (val) => {
  if (val === '__custom__') {
    data.showCustomType = true
    data.form.type = ''
  } else {
    data.showCustomType = false
    data.form.type = val
  }
}

// Logo上传成功回调
const handleLogoSuccess = (res) => {
  data.form.logo = res.data
}
// 经营许可证上传成功回调
const handleLicenseSuccess = (res) => {
  data.form.license = res.data
}

const formRef = ref()  // 表单引用

// 从后端加载已审核通过的店铺类型列表
request.get('/shop/types').then(res => {
  if (res.data && res.data.length > 0) {
    data.typeList = res.data
  }
}).catch(() => {})

// 检查当前用户是否已有店铺
request.get('/userShop/my', { params: { userId: user.id } }).then(res => {
  if (res.data) {
    data.existingShop = res.data
  }
}).catch(() => {})

// AI生成店铺简介：根据店铺名称自动生成描述文案
const aiGenerateDesc = () => {
  if (!data.form.name) {
    ElMessage.warning('请先输入店铺名称')
    return
  }
  data.aiLoading = true
  const finalType = data.form.type || data.typeSelect
  request.post('/ai/generateDesc', {
    name: data.form.name + (finalType && finalType !== '__custom__' ? '（' + finalType + '）' : '')
  }).then(res => {
    if (res.data) {
      data.form.description = res.data
      ElMessage.success('已生成简介')
    } else {
      ElMessage.error(res.msg || '生成失败')
    }
  }).finally(() => {
    data.aiLoading = false
  })
}

// 提交开店申请
const submit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      data.loading = true
      request.post('/userShop/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('申请已提交，请等待审核')
          router.push('/front/shopManage')
        } else {
          ElMessage.error(res.msg)
        }
      }).finally(() => {
        data.loading = false
      })
    }
  })
}
</script>
