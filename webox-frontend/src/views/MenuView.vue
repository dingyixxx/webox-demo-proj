<script setup>
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { showToast } from 'vant'
import 'vant/es/toast/style'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { email, isLoggedIn } = storeToRefs(authStore)

async function handleLogout() {
  try {
    await authStore.logout()
  } catch {
    // 即使登出接口失败，本地态也会清理
  }
  showToast('已退出登录')
  await router.replace('/login')
}

function goLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="menu-page">
    <van-nav-bar title="菜单" fixed placeholder />

    <div class="content">
      <van-cell-group inset>
        <van-cell v-if="isLoggedIn" title="当前用户" :value="email || '已登录'" />
        <van-cell title="菜单列表" label="后续在此展示菜品、分类筛选与 AI 推荐" />
      </van-cell-group>

      <div class="actions">
        <van-button v-if="isLoggedIn" round block type="danger" plain hairline @click="handleLogout">
          退出登录
        </van-button>
        <van-button v-else round block type="primary" @click="goLogin">
          去登录
        </van-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.menu-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.content {
  padding: 16px 0;
}

.actions {
  margin: 24px 16px 0;
}
</style>
