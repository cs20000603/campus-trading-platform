<!-- 个人信息页面：查看和编辑用户个人资料（头像、账号、姓名、余额） -->
<template>
  <div class="front-container" style="width: 40%">
    <div class="card" style="padding: 20px">
      <div style="font-size: 20px; margin-bottom: 40px; text-align: center">个人信息页</div>

      <!-- 个人信息表单：头像上传、账号（禁用编辑）、姓名、账户余额（只读） -->
      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="avatar" label="头像">
          <el-upload
              class="avatar-uploader"
              :action="baseUrl + '/files/upload'"
              :show-file-list="false"
              :on-success="handleFileUpload"
          >
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="username" label="账号">
          <!-- 已有ID时不允许修改账号 -->
          <el-input :disabled="data.user.id !== undefined" v-model="data.user.username" placeholder="请输入账号" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.user.name" placeholder="请输入姓名" autocomplete="off"></el-input>
        </el-form-item>
        <!-- 账户余额仅显示，不可编辑 -->
        <el-form-item prop="account" label="账户余额">
          <div style="color: red; font-weight: bold">￥{{ data.user.account }}</div>
        </el-form-item>
        <div style="text-align: center">
          <el-button type="primary" size="large" @click="update">保 存</el-button>
        </div>

      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive,ref } from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

const emit = defineEmits(['updateUser'])  // 用于通知父组件更新用户信息

const baseUrl = import.meta.env.VITE_BASE_URL  // 后端接口基础地址（从环境变量读取）
const formRef = ref()  // 表单引用
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),  // 当前用户信息
  rules: {              // 表单校验规则
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ]
  }
})

// 从后端重新加载最新用户信息并同步到localStorage
const loadUser = () => {
  request.get('/user/selectById/' + data.user.id).then(res => {
    data.user = res.data
    // 存储最新的用户信息到本地
    localStorage.setItem('system-user', JSON.stringify(res.data))
    emit('updateUser')  // 通知父组件（Front.vue）更新用户数据
  })
}
loadUser()

// 头像上传成功回调：将返回的文件路径赋值给avatar
const handleFileUpload = (res) => {
  data.user.avatar = res.data
}

// 保存个人信息到后端
const update = () => {
  request.put('/user/update', data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success('更新成功')
      loadUser()  // 更新后重新加载
    } else {
      ElMessage.error(res.msg)
    }
  })
}

</script>

<style scoped>
.avatar-uploader .avatar {
  width: 130px;
  height: 130px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 130px;
  height: 130px;
  text-align: center;
}
</style>