# webox-frontend

Vue 3 + Vant 4 移动端 H5 项目。

## 技术栈

- Vue 3 + TypeScript
- Vite
- Vant 4（按需自动引入）
- Vue Router
- Pinia
- postcss-px-to-viewport（375 设计稿转 vw）

## 开始使用

```bash
npm install
npm run dev
```

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `npm run dev` | 本地开发 |
| `npm run build` | 生产构建 |
| `npm run preview` | 预览构建产物 |

## 目录结构

```
src/
  views/        # 页面
  router/       # 路由
  stores/       # Pinia
  styles/       # 全局样式
  components/   # 公共组件
```

## 说明

- Vant 组件通过 `unplugin-vue-components` + `@vant/auto-import-resolver` 自动按需引入，页面中可直接使用，无需手动 `import`
- 样式按 375 设计稿书写 px，构建时自动转为 vw；`vant` 自身样式已排除，不做二次转换
