const STATUS_LABEL_MAP = {
  pending: '待确认',
  confirmed: '已确认',
  completed: '已完成',
  cancelled: '已取消',
}

const MEAL_PERIOD_LABEL_MAP = {
  lunch: '午餐 Lunch',
  dinner: '晚餐 Dinner',
}

export function formatDateYmd(date = new Date()) {
  const d = date instanceof Date ? date : new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function getStatusLabel(status) {
  if (!status) return '未知'
  return STATUS_LABEL_MAP[status] || status
}

export function getStatusType(status) {
  if (status === 'completed') return 'success'
  if (status === 'cancelled') return 'danger'
  if (status === 'confirmed') return 'primary'
  return 'warning'
}

export function getMealPeriodLabel(period) {
  if (!period) return '—'
  return MEAL_PERIOD_LABEL_MAP[period] || period
}

/** 列表接口 data 可能是数组，或单个订单对象（详情） */
export function normalizeOrderList(data) {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.list)) return data.list
  return []
}

export function normalizeOrderItems(order) {
  if (!order) return []
  return Array.isArray(order.items) ? order.items : []
}

export function getOrderId(order) {
  return order?.id
}

export function formatDeliveryDate(value) {
  if (!value) return '—'
  if (typeof value === 'string') return value.slice(0, 10)
  return String(value)
}

/** 明细行金额：优先用后端 subtotal */
export function getItemSubtotal(item) {
  if (item?.subtotal != null) return Number(item.subtotal) || 0
  return (Number(item?.price) || 0) * (Number(item?.quantity) || 0)
}
