/** 菜系偏好（多选 → cuisinePreferences） */
export const CUISINE_OPTIONS = [
  { value: 'chinese', text: '中餐' },
  { value: 'western', text: '西餐' },
  { value: 'japanese', text: '日料' },
  { value: 'salad', text: '轻食' },
  { value: 'korean', text: '韩餐' },
  { value: 'southeast_asian', text: '东南亚' },
]

/** 过敏原（多选 → allergens） */
export const ALLERGEN_OPTIONS = [
  { value: 'peanut', text: '花生' },
  { value: 'seafood', text: '海鲜' },
  { value: 'dairy', text: '乳制品' },
  { value: 'egg', text: '鸡蛋' },
  { value: 'gluten', text: '麸质' },
  { value: 'soy', text: '大豆' },
]

/** 辣度：不辣 / 微辣 / 辣 → 10 / 50 / 90 */
export const SPICINESS_OPTIONS = [
  { value: 10, text: '不辣' },
  { value: 50, text: '微辣' },
  { value: 90, text: '辣' },
]

/** 口味：清淡 / 适中 / 重口 → 10 / 50 / 90 */
export const TASTE_OPTIONS = [
  { value: 10, text: '清淡' },
  { value: 50, text: '适中' },
  { value: 90, text: '重口' },
]

/** 与后端 GET/PUT 字段对齐 */
export function createEmptyPreferences() {
  return {
    allergens: [],
    cuisinePreferences: [],
    spicinessLevel: 0,
    tasteLevel: 0,
    proteinLevel: 0,
    fatLevel: 0,
    preferredMinPrice: null,
    preferredMaxPrice: null,
  }
}

export function normalizePreferences(data) {
  const empty = createEmptyPreferences()
  if (!data || typeof data !== 'object') return empty

  return {
    allergens: Array.isArray(data.allergens) ? [...data.allergens] : [],
    cuisinePreferences: Array.isArray(data.cuisinePreferences)
      ? [...data.cuisinePreferences]
      : [],
    spicinessLevel: Number(data.spicinessLevel) || 0,
    tasteLevel: Number(data.tasteLevel) || 0,
    proteinLevel: Number(data.proteinLevel) || 0,
    fatLevel: Number(data.fatLevel) || 0,
    preferredMinPrice:
      data.preferredMinPrice != null ? Number(data.preferredMinPrice) : null,
    preferredMaxPrice:
      data.preferredMaxPrice != null ? Number(data.preferredMaxPrice) : null,
  }
}

/** 提交给后端 PUT 的完整入参 */
export function toPreferencesPayload(prefs) {
  return {
    allergens: prefs.allergens || [],
    cuisinePreferences: prefs.cuisinePreferences || [],
    spicinessLevel: Number(prefs.spicinessLevel) || 0,
    tasteLevel: Number(prefs.tasteLevel) || 0,
    proteinLevel: Number(prefs.proteinLevel) || 0,
    fatLevel: Number(prefs.fatLevel) || 0,
    preferredMinPrice:
      prefs.preferredMinPrice != null && prefs.preferredMinPrice !== ''
        ? Number(prefs.preferredMinPrice)
        : null,
    preferredMaxPrice:
      prefs.preferredMaxPrice != null && prefs.preferredMaxPrice !== ''
        ? Number(prefs.preferredMaxPrice)
        : null,
  }
}

/** 存储价（元×100）→ 表单展示元 */
export function priceToYuan(stored) {
  if (stored == null || stored === '') return ''
  const n = Number(stored)
  if (Number.isNaN(n)) return ''
  return String(n / 100)
}

/** 表单元 → 存储价（元×100） */
export function yuanToPrice(yuan) {
  if (yuan == null || yuan === '') return null
  const n = Number(yuan)
  if (Number.isNaN(n)) return null
  return Math.round(n * 100)
}

/** 用户过敏原是否与菜品冲突（海鲜覆盖 fish / shellfish） */
export function normalizeAllergenList(allergens) {
  if (Array.isArray(allergens)) return allergens.filter(Boolean)
  if (typeof allergens === 'string') {
    try {
      const parsed = JSON.parse(allergens)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : []
    } catch {
      return allergens ? [allergens] : []
    }
  }
  return []
}

export function hasAllergenConflict(dishAllergens, userAllergens) {
  const dish = normalizeAllergenList(dishAllergens)
  const user = normalizeAllergenList(userAllergens)
  if (!dish.length || !user.length) return false

  return user.some((code) => {
    if (code === 'seafood') {
      return (
        dish.includes('seafood') ||
        dish.includes('fish') ||
        dish.includes('shellfish')
      )
    }
    return dish.includes(code)
  })
}

/** 菜品上的某个过敏原编码，是否命中用户偏好 */
export function isDishAllergenMatched(dishCode, userAllergens) {
  const user = normalizeAllergenList(userAllergens)
  if (!dishCode || !user.length) return false
  if (user.includes(dishCode)) return true
  if (
    user.includes('seafood') &&
    ['fish', 'shellfish', 'seafood'].includes(dishCode)
  ) {
    return true
  }
  return false
}

/** 命中的用户过敏原编码（用于展示） */
export function getMatchedUserAllergens(dishAllergens, userAllergens) {
  const dish = normalizeAllergenList(dishAllergens)
  const user = normalizeAllergenList(userAllergens)
  if (!dish.length || !user.length) return []

  return user.filter((code) => {
    if (code === 'seafood') {
      return (
        dish.includes('seafood') ||
        dish.includes('fish') ||
        dish.includes('shellfish')
      )
    }
    return dish.includes(code)
  })
}

/**
 * 推荐模式：先按价格区间过滤，再按「菜系 → 辣度 → 口味」优先级排序
 */
export function applyRecommendSort(list, prefs) {
  const items = Array.isArray(list) ? [...list] : []
  const priceMin =
    prefs?.preferredMinPrice != null ? Number(prefs.preferredMinPrice) : null
  const priceMax =
    prefs?.preferredMaxPrice != null ? Number(prefs.preferredMaxPrice) : null

  let filtered = items
  if (priceMin != null || priceMax != null) {
    filtered = items.filter((item) => {
      const price = Number(item.price) || 0
      if (priceMin != null && price < priceMin) return false
      if (priceMax != null && price > priceMax) return false
      return true
    })
  }

  return sortDishesByFlavorPreference(filtered, prefs)
}

/**
 * 全部 / 分类浏览：按「菜系 → 辣度 → 口味」优先级排序
 */
export function applyFlavorSort(list, prefs) {
  return sortDishesByFlavorPreference(list, prefs)
}

/**
 * 排序优先级：
 * 1. 命中用户偏好菜系（分类）靠前
 * 2. 辣度更接近用户偏好靠前
 * 3. 口味更接近用户偏好靠前
 */
export function sortDishesByFlavorPreference(list, prefs) {
  const items = Array.isArray(list) ? [...list] : []
  const cuisines = prefs?.cuisinePreferences || []

  return items.sort((a, b) => {
    if (cuisines.length) {
      const aHit = cuisines.includes(a.category) ? 0 : 1
      const bHit = cuisines.includes(b.category) ? 0 : 1
      if (aHit !== bHit) return aHit - bHit
    }

    const spicinessCmp =
      levelDistance(readSpiciness(a), prefs?.spicinessLevel) -
      levelDistance(readSpiciness(b), prefs?.spicinessLevel)
    if (spicinessCmp !== 0) return spicinessCmp

    return (
      levelDistance(readTaste(a), prefs?.tasteLevel) -
      levelDistance(readTaste(b), prefs?.tasteLevel)
    )
  })
}

function readSpiciness(dish) {
  const v =
    dish?.flavorSpicinessLevel ??
    dish?.flavor_spiciness_level ??
    dish?.spicinessLevel
  return v === undefined || v === null || v === '' ? null : Number(v)
}

function readTaste(dish) {
  const v =
    dish?.flavorTasteLevel ?? dish?.flavor_taste_level ?? dish?.tasteLevel
  return v === undefined || v === null || v === '' ? null : Number(v)
}

/** 与用户偏好档位的距离；未设置偏好时视为相等（返回 0） */
function levelDistance(dishLevel, prefLevel) {
  if (prefLevel == null || prefLevel === '' || Number(prefLevel) === 0) return 0
  const dish =
    dishLevel == null || Number.isNaN(Number(dishLevel)) ? 0 : Number(dishLevel)
  return Math.abs(dish - Number(prefLevel))
}
