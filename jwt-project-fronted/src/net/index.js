import axios from "axios";
import {ElMessage} from "element-plus";

const authItemName = "access_token"

//包装头部token
function accessHeader() {
    const token = takeAccessToken()
    return token ? {
        'Authorization': `Bearer ${takeAccessToken()}`
    } : {}
}
//获得token
function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    if(!str)
        return null
    const authObj = JSON.parse(str)
    if(authObj.expire <= new Date()) {
        deleteAccessToken()
        ElMessage.warning('登录状态过期')
        return null
    }
    return authObj.token
}
//删除token
function deleteAccessToken() {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}
//保存token
function storeAccessToken(token, remember, expire) {
    const authObj = {token: token , expire: expire}
    const str = JSON.stringify(authObj)
    if(remember)
        localStorage.setItem(authItemName,str)
    else
        sessionStorage.setItem(authItemName,str)

}

const defaultFailure = (message, code, url) => {
    console.warn(`请求地址: ${url},状态码: ${code},错误信息: ${message}`)
    ElMessage.warning(message)
}

const defaultError = (error) => {
    console.error(error)
    ElMessage.error('发生系统错误，请联系管理员')
}
function internalPost(url, data, header, success,failure,error = defaultError) {
    axios.post(url,data,{headers: header})
        .then(({data}) => {
            if(data.code === 200) {
                success(data.data)
            } else {
                failure(data.message)
            }
        }).catch(err => error(err))
}

function internalGet(url, header, success,failure,error = defaultError) {
    axios.get(url,{headers: header})
        .then(({data}) => {
            if(data.code === 200) {
                success(data.data)
            } else {
                failure(data.message)
            }
        }).catch(err => error(err))
}

function get(url,success,failure = defaultFailure) {
    internalGet(url,accessHeader(),success,failure)
}
function post(url,data,success,failure=defaultFailure) {
    internalPost(url,data,accessHeader(),success,failure)
}
function login(username,password,remember,success,failure = defaultFailure) {
    internalPost('/api/auth/login',{
        username: username,
        password: password
    }, {
        //默认发送的是原生(raw)的json格式
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(data.token,remember,data.expire)
        ElMessage.success(`登陆成功,欢迎${data.username}进入系统`)
        success(data)
    },failure)
}

function logout(success,failure = defaultFailure) {
    get('/api/auth/logout',() => {
        deleteAccessToken()
        ElMessage.success('退出登录成功,欢迎您再次使用')
        success()
    },failure)
}

//判断是否登录
//false:已登录 true:未登录
function unauthorized(){
    return !takeAccessToken()
}
export {login, logout, get, post, unauthorized}