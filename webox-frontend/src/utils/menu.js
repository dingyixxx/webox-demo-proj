/** 分类：接口值 -> 展示文案 */
export const CATEGORY_OPTIONS = [
  { value: 'all', text: '全部' },
  { value: 'chinese', text: '中餐' },
  { value: 'western', text: '西餐' },
  { value: 'japanese', text: '日料' },
  { value: 'salad', text: '轻食' },
  { value: 'korean', text: '韩餐' },
  { value: 'southeast_asian', text: '东南亚' },
]

const CATEGORY_LABEL_MAP = {
  chinese: '中餐',
  western: '西餐',
  japanese: '日料',
  salad: '轻食',
  korean: '韩餐',
  southeast_asian: '东南亚',
}

const ALLERGEN_LABEL_MAP = {
  peanut: '花生',
  dairy: '乳制品',
  egg: '鸡蛋',
  fish: '鱼类',
  shellfish: '甲壳类',
  seafood: '海鲜',
  gluten: '麸质',
  soy: '大豆',
}

/** 金额按「元×100」整数存储，展示为元 */
export function formatPrice(price) {
  const n = Number(price)
  if (Number.isNaN(n)) return '—'
  return (n / 100).toFixed(2)
}

export function getCategoryLabel(category) {
  if (!category) return '未分类'
  return CATEGORY_LABEL_MAP[category] || category
}

export function getAllergenLabel(code) {
  if (!code) return ''
  return ALLERGEN_LABEL_MAP[code] || code
}

/** 列表/详情统一取菜品 ID（优先 menuItemId） */
export function getDishId(item) {
  return item?.menuItemId ?? item?.id
}
