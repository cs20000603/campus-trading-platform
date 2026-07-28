<!-- 用户登录页面 -->
<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 30px; text-align: center; margin-bottom: 30px; color: #1967e3">用户登录</div>
      <!-- 登录表单：包含账号、密码、验证码三个输入项 -->
      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <!-- 验证码：左侧输入框 + 右侧随机生成的验证码文本，点击可刷新 -->
        <el-form-item prop="captcha">
          <el-input :prefix-icon="Key" size="large" v-model="data.form.captcha" placeholder="请输入验证码" style="width: 60%; float: left; margin-right: 10px" />
          <div style="width: 35%; float: right; text-align: center; line-height: 40px; background: #f0f0f0; border-radius: 4px; cursor: pointer; font-size: 16px; letter-spacing: 2px;" @click="refreshCaptcha">{{ data.captchaText }}</div>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="login">登 录</el-button>
        </el-form-item>
      </el-form>
      <!-- 底部链接：跳转注册页面和忘记密码页面 -->
      <div style="text-align: right;">
        还没有账号？请 <a href="/register">注册</a>
        &nbsp;|&nbsp;<a href="#" @click.prevent="goResetPassword">忘记密码？</a>
      </div>
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
    captchaText: '',  // 当前显示的验证码文本
    form: {},         // 登录表单数据（username, password, captcha）
    rules: {          // 表单校验规则
      username: [
        { required: true, message: '请输入账号', trigger: 'blur' },
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
      ],
    }
  })

  const formRef = ref()  // 表单引用，用于触发表单校验

  // 生成随机四位验证码（数字+大写字母）
  const generateCaptcha = () => {
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    let result = '';
    for (let i = 0; i < 4; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  }

  // 刷新验证码：重新生成验证码并清空输入框
  const refreshCaptcha = () => {
    data.captchaText = generateCaptcha();
    data.form.captcha = '';
  }

  // 页面加载时生成验证码
  refreshCaptcha();

  // 跳转到忘记密码页面
  const goResetPassword = () => {
    router.push('/resetPassword')
  }

  // 登录处理：先校验验证码，再调用后端接口进行账号密码验证
  const login = () => {
    formRef.value.validate((valid => {
      if (valid) {
        // 先验证前端验证码（大小写不敏感）
        if (!data.form.captcha || data.form.captcha.toUpperCase() !== data.captchaText.toUpperCase()) {
          ElMessage.error('验证码错误');
          refreshCaptcha();
          return;
        }
        // 调用后端登录接口
        request.post('/login/user', data.form).then(res => {
          if (res.code === '200') {
            ElMessage.success("登录成功")
            // 将用户信息和token存入localStorage
            localStorage.setItem('system-user', JSON.stringify(res.data))
            router.push('/front/home')
          } else {
            ElMessage.error(res.msg)
            refreshCaptcha();
          }
        }).catch(() => {
          refreshCaptcha();
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