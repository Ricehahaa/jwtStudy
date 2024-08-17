import {createRouter, createWebHistory} from "vue-router";
import {unauthorized} from "@/net/index.js";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children:[
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                },{
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                },
                {
                    path: 'reset',
                    name: 'welcome-reset',
                    component: () => import('@/views/welcome/ResetPage.vue')
                }
            ]
        },{
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue')
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    //这个其实就很像一个递归
    //比如我访问/a,而我没有登录
    //这时候的流程就是,/a没有一个匹配到的,放到/index上去
    //然后/index也回到这里,看哪个匹配,发现我没有登录又访问/index,就把我扔回/
    //接着/又回到起点这里开始匹配,匹配到最后一个next(),就放行

    //注意第一行的顺序,因为如果 to.name.startsWith('welcome-')放在第一个,如果是/a这种没有被路由的,他的to.name则是undefined,将报错
    if(!isUnauthorized && to.name.startsWith('welcome-')){
        next('/index')
    } else if (to.fullPath.startsWith('/index') && isUnauthorized){
        next('/')
    } else if(to.matched.length === 0) {
        next('/index')
    } else {
        next()
    }


})
export default router