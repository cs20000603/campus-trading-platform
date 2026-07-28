<!-- 重置密码页面：通过用户名修改密码 -->
<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 30px; text-align: center; margin-bottom: 30px; color: #1967e3">重置密码</div>
      <!-- 重置密码表单：账号、新密码、确认新密码 -->
      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="reset" :loading="data.loading">确认重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 底部链接：跳转登录页面 -->
      <div style="text-align: right;">
        想起密码了？<a href="/login">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

// 响应式数据对象
const data = reactive({
  loading: false,  // 按钮加载状态，防止重复提交
  form: {},        // 表单数据（username, newPassword, confirmPassword）
  rules: {         // 表单校验规则
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码至少6位', trigger: 'blur' },
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
    ],
  }
})

const formRef = ref()  // 表单引用

// 重置密码处理：校验两次密码一致后调用后端接口
const reset = () => {
  formRef.value.validate((valid => {
    if (valid) {
      // 前端校验两次密码是否一致
      if (data.form.newPassword !== data.form.confirmPassword) {
        ElMessage.error('两次密码不一致')
        return
      }
      data.loading = true
      request.post('/resetPassword', {
        username: data.form.username,
        newPassword: data.form.newPassword
      }).then(res => {
        if (res.code === '200') {
          ElMessage.success('重置成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      }).finally(() => {
        data.loading = false  // 无论成功失败，关闭加载状态
      })
    }
  })).catch(error => {
    console.error(error)
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow:hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #2e3143;
  background-size: cover;
}
.login-box {
  width: 350px;
  padding: 50px 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
  background-color: #fff;
}
</style>
