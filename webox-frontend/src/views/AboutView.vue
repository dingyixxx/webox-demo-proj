<script setup lang="ts">
</script>

<template>
  <div class="about">
    <van-nav-bar title="关于" fixed placeholder />

    <div class="content">
      <van-cell-group inset>
        <van-cell title="技术栈" value="Vue 3" />
        <van-cell title="UI 组件库" value="Vant 4" />
        <van-cell title="构建工具" value="Vite" />
        <van-cell title="状态管理" value="Pinia" />
        <van-cell title="路由" value="Vue Router" />
      </van-cell-group>
    </div>

    <van-tabbar route>
      <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/about" icon="info-o">关于</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped lang="scss">
.about {
  min-height: 100vh;
  background: #f7f8fa;
}

.content {
  padding: 16px 0 60px;
}
</style>
