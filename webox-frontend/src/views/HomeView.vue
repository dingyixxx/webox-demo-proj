<script setup>
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { useRouter } from 'vue-router'

const router = useRouter()

const handleClick = () => {
  showToast('欢迎使用 Webox')
}

const goAbout = () => {
  router.push('/about')
}
</script>

<template>
  <div class="home">
    <van-nav-bar title="Webox" fixed placeholder />

    <div class="content">
      <van-cell-group inset>
        <van-cell title="Vue 3 + Vant 4" label="移动端 H5 项目已就绪" is-link @click="handleClick" />
        <van-cell title="关于项目" is-link @click="goAbout" />
      </van-cell-group>

      <div class="actions">
        <van-button type="primary" block round @click="handleClick">
          点我试试
        </van-button>
      </div>
    </div>

    <van-tabbar route>
      <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/about" icon="info-o">关于</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped lang="scss">
.home {
  min-height: 100vh;
  background: #f7f8fa;
}

.content {
  padding: 16px 0 60px;
}

.actions {
  margin: 24px 16px 0;
}
</style>
