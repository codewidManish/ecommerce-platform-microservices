import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { authService } from '../services'

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user:         null,
      accessToken:  null,
      refreshToken: null,
      isLoading:    false,
      error:        null,

      login: async (credentials) => {
        set({ isLoading: true, error: null })
        try {
          const { data } = await authService.login(credentials)
          const { accessToken, refreshToken, user } = data.data
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', refreshToken)
          set({ user, accessToken, refreshToken, isLoading: false })
          return { success: true }
        } catch (err) {
          const msg = err.response?.data?.message || 'Login failed'
          set({ error: msg, isLoading: false })
          return { success: false, error: msg }
        }
      },

      register: async (formData) => {
        set({ isLoading: true, error: null })
        try {
          const { data } = await authService.register(formData)
          const { accessToken, refreshToken, user } = data.data
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', refreshToken)
          set({ user, accessToken, refreshToken, isLoading: false })
          return { success: true }
        } catch (err) {
          const msg = err.response?.data?.message || 'Registration failed'
          set({ error: msg, isLoading: false })
          return { success: false, error: msg }
        }
      },

      logout: async () => {
        try {
          const token = get().refreshToken
          if (token) await authService.logout(token)
        } finally {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          set({ user: null, accessToken: null, refreshToken: null })
        }
      },

      isAuthenticated: () => !!get().accessToken,
      isAdmin: () => get().user?.roles?.includes('ROLE_ADMIN') ?? false,
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
      }),
    }
  )
)
