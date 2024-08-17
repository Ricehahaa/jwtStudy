<script setup>

import {reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net/index.js";
import router from "@/router/index.js";

const active = ref(0)
const formRef = ref()
const form = reactive({
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})
const timerId = ref(0)
function askCode() {
  if(!isEmailValid()){
    ElMessage.warning('请输入正确的电子邮件')
    return
  }
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=register`,() => {
    ElMessage.success(`验证码已发送到${form.email}`)
    timerId.value = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0){
        clearInterval(timerId.value)
      }
    },1000)
  },(message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}
const validatePassword  = (rule,value,callback) => {
  if(value === '')
    callback(new Error('请再次输入密码'))
  else if (value !== form.password)
    callback(new Error("密码不一致"))
  else
    callback()
}
const rule = {
  email: [
    {request: true, message: '请输入电子邮件', trigger: 'blur'},
    {type: "email", message: '请输入合法电子邮件', trigger: ['blur', 'change']}
  ],
  code: [
    {request: true, message: '请输入验证码',trigger: 'blur'}
  ],
  password: [
    {request: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 20, message: '密码长度为6到20', trigger: ['blur', 'change']}
  ],
  password_repeat: [
    {validator: validatePassword, trigger: ['blur', 'change']}
  ]
}
const coldTime = ref(0)
function isEmailValid(){
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email)
}

function confirmReset() {
  formRef.value.validate((valid) => {
    if(valid){
      post('/api/auth/reset-confirm',{
        email: form.email,
        code: form.code
      },() => active.value++)
    }
  })
}
function doReset() {
  formRef.value.validate((valid) => {
    if(valid){
      post('/api/auth/reset-password',{...form}, () => {
        ElMessage.success('密码重置成功')
        router.push('/')
      })
    }
  })
}
</script>

<template>
  <div style="margin-top: 30px">
    <el-steps  :active="active" finish-status="success" align-center>
      <el-step title="验证电子邮件"/>
      <el-step title="重新设置密码"/>
    </el-steps>
  </div>

  <div v-if="active===0" style="margin: 0 20px;text-align: center">
    <div style="margin-top: 80px">
      <div style="font-size: 25px;font-weight: bold">重置密码</div>
      <div style="font-size: 14px;color: grey">请输入电子邮件地址</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="email">
          <el-input v-model="form.email" type="text" placeholder="电子邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="code">
          <el-row :gutter="10" style="width: 100%;">
            <el-col :span="17" style="text-align: left;">
              <el-input v-model="form.code" type="text" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="7" style="text-align: right">
              <el-button @click="askCode" :disabled="!isEmailValid() || coldTime > 0" type="success">
                {{ coldTime > 0 ? `请稍后${coldTime}秒` : '获取验证码' }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 80px">
      <el-button @click="confirmReset" style="width: 270px" type="warning" plain>开始重置密码</el-button>
    </div>
  </div>

  <div v-if="active===1" style="margin: 0 20px;text-align: center">
    <div style="margin-top: 80px">
      <div style="font-size: 25px;font-weight: bold">重置密码</div>
      <div style="font-size: 14px;color: grey">输入新密码</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password_repeat">
          <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="重复密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 80px">
      <el-button @click="doReset" style="width: 270px" type="danger" plain>立即重置重置密码</el-button>
    </div>
  </div>
</template>

<style scoped>

</style>