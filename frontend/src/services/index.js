import api from './api'

// ─────────────────────── AUTH ───────────────────────

export const authService = {
  register: (data)        => api.post('/auth/register', data),
  login:    (data)        => api.post('/auth/login', data),
  refresh:  (token)       => api.post('/auth/refresh-token', { refreshToken: token }),
  logout:   (token)       => api.post('/auth/logout', { refreshToken: token }),
}

// ─────────────────────── PRODUCTS ───────────────────────

export const productService = {
  getAll: (params) => api.get('/products', { params }),
  getById: (id)    => api.get(`/products/${id}`),
  search:  (q, params) => api.get('/products/search', { params: { q, ...params } }),
  getByCategory: (categoryId, params) => api.get(`/products/category/${categoryId}`, { params }),
  create:  (data) => api.post('/products', data),
  update:  (id, data) => api.put(`/products/${id}`, data),
  delete:  (id)   => api.delete(`/products/${id}`),
}

// ─────────────────────── CATEGORIES ───────────────────────

export const categoryService = {
  getAll:  () =>    api.get('/categories'),
  getById: (id) =>  api.get(`/categories/${id}`),
  create:  (data) => api.post('/categories', data),
}

// ─────────────────────── CART ───────────────────────

export const cartService = {
  getCart:        ()               => api.get('/cart'),
  addItem:        (data)           => api.post('/cart/items', data),
  removeItem:     (productId)      => api.delete(`/cart/items/${productId}`),
  updateQuantity: (productId, qty) => api.patch(`/cart/items/${productId}?quantity=${qty}`),
  clearCart:      ()               => api.delete('/cart'),
}

// ─────────────────────── ORDERS ───────────────────────

export const orderService = {
  placeOrder:   (data)      => api.post('/orders', data),
  getMyOrders:  (params)    => api.get('/orders', { params }),
  getOrder:     (id)        => api.get(`/orders/${id}`),
  cancelOrder:  (id, reason) => api.post(`/orders/${id}/cancel?reason=${encodeURIComponent(reason || '')}`),
}

// ─────────────────────── PAYMENTS ───────────────────────

export const paymentService = {
  initiate:       (data, idempotencyKey) => api.post('/payments', data, {
    headers: { 'Idempotency-Key': idempotencyKey || crypto.randomUUID() }
  }),
  getByOrder:     (orderId) => api.get(`/payments/order/${orderId}`),
}

// ─────────────────────── INVENTORY ───────────────────────

export const inventoryService = {
  getByProduct:     (productId)        => api.get(`/inventory/product/${productId}`),
  checkAvailability:(productId, qty)   => api.get(`/inventory/product/${productId}/check?quantity=${qty}`),
  upsert:           (data)             => api.post('/inventory', data),
  restock:          (productId, qty)   => api.post(`/inventory/product/${productId}/restock?quantity=${qty}`),
}
