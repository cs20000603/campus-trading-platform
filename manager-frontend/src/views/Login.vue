<!-- 管理员登录页面 -->
<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 30px; text-align: center; margin-bottom: 30px; color: #1967e3">管理后台登录</div>
      <!-- 登录表单：用户名、密码、验证码（从后端控制台获取） -->
      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <!-- 验证码：输入框 + 获取验证码按钮（60秒冷却） -->
        <el-form-item prop="captcha">
          <el-input :prefix-icon="Key" size="large" v-model="data.form.captcha" placeholder="请输入验证码" style="width: 60%; float: left; margin-right: 10px" />
          <el-button size="large" type="info" style="width: 35%; float: right" @click="getCaptcha" :disabled="data.countdown > 0">
            {{ data.countdown > 0 ? data.countdown + 's' : '获取验证码' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="login">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
  import { reactive, ref } from "vue";
  import { User, Lock, Key } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import { ElMessage } from "element-plus";
  import router from "@/router";

  // 响应式数据对象
  const data = reactive({
    countdown: 0,  // 验证码按钮冷却倒计时（秒）
    form: {},      // 登录表单数据（username, password, captcha）
    rules: {       // 表单校验规则
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
      ],
    }
  })

  const formRef = ref()  // 表单引用

  // 获取验证码：后端生成验证码并打印在控制台，前端起60秒冷却
  const getCaptcha = () => {
    if (!data.form.username) {
      ElMessage.warning('请先输入用户名')
      return
    }
    request.get('/captcha/admin').then(res => {
      if (res.code === '200') {
        ElMessage.success('验证码已生成，请查看后端控制台')
        data.countdown = 60
        const timer = setInterval(() => {
          data.countdown--
          if (data.countdown <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      } else {
        ElMessage.error(res.msg)
      }
    })
  }

  // 管理员登录：调用后端登录接口，成功后存储用户信息并跳转
  const login = () => {
    formRef.value.validate((valid => {
      if (valid) {
        request.post('/login/admin', data.form).then(res => {
          if (res.code === '200') {
            ElMessage.success("登录成功")
            localStorage.setItem('system-user', JSON.stringify(res.data))
            router.push('/manager/home')
          } else {
            ElMessage.error(res.msg)
          }
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