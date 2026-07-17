import http from './http'

/** POST /api/recommendations — AI 智能推荐（需登录） */
export function getRecommendations(prompt) {
  return http.post('/api/recommendations', { prompt })
}
