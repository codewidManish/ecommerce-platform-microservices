import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import { useAuthStore } from './store/authStore'

import Layout       from './components/layout/Layout'
import HomePage     from './pages/HomePage'
import LoginPage    from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProductsPage from './pages/ProductsPage'
import ProductPage  from './pages/ProductPage'
import CartPage     from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrdersPage   from './pages/OrdersPage'
import OrderPage    from './pages/OrderPage'
import AdminPage    from './pages/AdminPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 2,
      staleTime: 1000 * 60 * 5,  // 5 minutes
      refetchOnWindowFocus: false,
    },
  },
})

function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated())
  return isAuthenticated ? children : <Navigate to="/login" replace />
}

function AdminRoute({ children }) {
  const isAdmin = useAuthStore((s) => s.isAdmin())
  return isAdmin ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Toaster position="top-right" />
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index                   element={<HomePage />} />
            <Route path="login"            element={<LoginPage />} />
            <Route path="register"         element={<RegisterPage />} />
            <Route path="products"         element={<ProductsPage />} />
            <Route path="products/:id"     element={<ProductPage />} />

            {/* Protected routes */}
            <Route path="cart"     element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
            <Route path="checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
            <Route path="orders"   element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
            <Route path="orders/:id" element={<ProtectedRoute><OrderPage /></ProtectedRoute>} />

            {/* Admin routes */}
            <Route path="admin"    element={<AdminRoute><AdminPage /></AdminRoute>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
