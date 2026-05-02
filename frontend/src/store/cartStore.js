import { create } from 'zustand'
import { cartService } from '../services'
import toast from 'react-hot-toast'

export const useCartStore = create((set, get) => ({
  cart:      null,
  isLoading: false,

  fetchCart: async () => {
    set({ isLoading: true })
    try {
      const { data } = await cartService.getCart()
      set({ cart: data.data, isLoading: false })
    } catch {
      set({ isLoading: false })
    }
  },

  addItem: async (productId, quantity = 1) => {
    try {
      const { data } = await cartService.addItem({ productId, quantity })
      set({ cart: data.data })
      toast.success('Added to cart!')
      return true
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add item')
      return false
    }
  },

  removeItem: async (productId) => {
    try {
      const { data } = await cartService.removeItem(productId)
      set({ cart: data.data })
      toast.success('Item removed')
    } catch {
      toast.error('Failed to remove item')
    }
  },

  updateQuantity: async (productId, quantity) => {
    try {
      const { data } = await cartService.updateQuantity(productId, quantity)
      set({ cart: data.data })
    } catch {
      toast.error('Failed to update quantity')
    }
  },

  clearCart: async () => {
    try {
      await cartService.clearCart()
      set({ cart: null })
    } catch {
      toast.error('Failed to clear cart')
    }
  },

  totalItems: () => get().cart?.totalItems ?? 0,
  totalAmount: () => get().cart?.totalAmount ?? 0,
}))
