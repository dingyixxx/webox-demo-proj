import { getDishId } from '@/utils/menu'
import { sortDishesByFlavorPreference } from '@/utils/preferences'

/** 兼容数组或 { items: [] } */
export function normalizeCartItems(data) {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.items)) return data.items
  if (Array.isArray(data?.list)) return data.list
  return []
}

export function getCartItemMenuId(item) {
  return item?.menuItemId
}

export function getCartLineId(item) {
  return item?.id
}

/**
 * 用当日菜单补齐购物车行的名称 / 图片 / 当日价格 / 风味字段
 * @param {object} [prefs] 传入时按辣度 → 口味贴近度排序
 */
export function mergeCartWithMenu(cartItems, menuList, prefs) {
  const menuMap = new Map()
  ;(menuList || []).forEach((dish) => {
    const id = String(getDishId(dish))
    if (id) menuMap.set(id, dish)
  })

  const merged = (cartItems || []).map((item) => {
    const menuId = getCartItemMenuId(item)
    const dish = menuMap.get(String(menuId)) || {}
    const price =
      item.price != null ? item.price : dish.price != null ? dish.price : 0
    const quantity = Number(item.quantity) || 1

    return {
      ...item,
      id: getCartLineId(item),
      menuItemId: menuId,
      quantity,
      price,
      name: item.name || dish.name || '未知菜品',
      image: item.image || dish.image || '',
      category: item.category || dish.category || '',
      flavorSpicinessLevel:
        item.flavorSpicinessLevel ?? dish.flavorSpicinessLevel,
      flavorTasteLevel: item.flavorTasteLevel ?? dish.flavorTasteLevel,
      lineAmount: price * quantity,
    }
  })

  return prefs ? sortDishesByFlavorPreference(merged, prefs) : merged
}

export function calcCartTotal(items) {
  return (items || []).reduce((sum, item) => {
    const price = Number(item.price) || 0
    const quantity = Number(item.quantity) || 0
    return sum + price * quantity
  }, 0)
}
