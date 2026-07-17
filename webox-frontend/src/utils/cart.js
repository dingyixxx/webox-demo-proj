import { getDishId } from '@/utils/menu'

/** 兼容数组或 { items: [] } */
export function normalizeCartItems(data) {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.items)) return data.items
  if (Array.isArray(data?.list)) return data.list
  return []
}

export function getCartItemMenuId(item) {
  return item?.menuItemId ?? item?.menu_item_id ?? item?.menuId
}

export function getCartLineId(item) {
  return item?.id ?? item?.cartItemId ?? item?.cart_item_id
}

/**
 * 用当日菜单补齐购物车行的名称 / 图片 / 当日价格
 */
export function mergeCartWithMenu(cartItems, menuList) {
  const menuMap = new Map()
  ;(menuList || []).forEach((dish) => {
    const id = String(getDishId(dish))
    if (id) menuMap.set(id, dish)
  })

  return (cartItems || []).map((item) => {
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
      lineAmount: price * quantity,
    }
  })
}

export function calcCartTotal(items) {
  return (items || []).reduce((sum, item) => {
    const price = Number(item.price) || 0
    const quantity = Number(item.quantity) || 0
    return sum + price * quantity
  }, 0)
}
