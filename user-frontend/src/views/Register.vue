<!-- 用户注册页面 -->
<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 30px; text-align: center; margin-bottom: 30px; color: #0c9c7a">欢 迎 注 册</div>
      <!-- 注册表单：账号、密码、确认密码 -->
      <el-form :model="data.form"  ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.newPassword" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button size="large" style="width: 100%; background-color: #0c9c7a; border-color: #0c9c7a; color: white" @click="register">注 册</el-button>
        </el-form-item>
      </el-form>
      <!-- 底部链接：跳转登录页面 -->
      <div style="text-align: right;">
        已有账号？请 <a href="/login">登录</a>
      </div>
    </div>

  </div>
</template>

<script setup>
  import { reactive, ref } from "vue";
  import { User, Lock } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import {ElMessage} from "element-plus";
  import router from "@/router";

  // 响应式数据对象
  const data = reactive({
    form: { },    // 注册表单数据（username, password, newPassword）
    rules: {      // 表单校验规则
      username: [
        { required: true, message: '请输入账号', trigger: 'blur' },
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
      newPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
      ],
    }
  })

  const formRef = ref()  // 表单引用，用于触发表单校验

  // 注册处理：校验表单后调用后端注册接口
  const register = () => {
    formRef.value.validate((valid => {
      if (valid) {
        request.post('/register', data.form).then(res => {
          if (res.code === '200') {
            ElMessage.success("恭喜您！注册成功")
            // 注册成功后跳转到登录页面
            router.push('/login')
          } else {
            ElMessage.error(res.msg)
          }
        }).catch(err => {
          ElMessage.error('注册失败：' + (err.message || err))
        })
      } else {
        ElMessage.warning('请填写完整信息')
      }
    })).catch(() => {
      ElMessage.warning('请填写完整信息')
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
  background: #0c9c7a;
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