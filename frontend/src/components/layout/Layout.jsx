import { Outlet, Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, User, Search, Package, LogOut, Shield } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'
import { useCartStore }  from '../../store/cartStore'
import { useEffect, useState } from 'react'
import { useDebounce } from '../../hooks/useDebounce'

export default function Layout() {
  const navigate = useNavigate()
  const { user, isAuthenticated, isAdmin, logout } = useAuthStore()
  const { fetchCart, totalItems } = useCartStore()
  const [searchQ, setSearchQ] = useState('')
  const debouncedQ = useDebounce(searchQ, 400)

  useEffect(() => {
    if (isAuthenticated()) fetchCart()
  }, [isAuthenticated()])

  useEffect(() => {
    if (debouncedQ.trim()) navigate(`/products?q=${encodeURIComponent(debouncedQ)}`)
  }, [debouncedQ])

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ── Navbar ── */}
      <nav className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">

            {/* Logo */}
            <Link to="/" className="flex items-center gap-2 font-bold text-xl text-indigo-600">
              <Package className="w-6 h-6" />
              ShopEasy
            </Link>

            {/* Search */}
            <div className="flex-1 max-w-lg mx-6">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchQ}
                  onChange={(e) => setSearchQ(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-full text-sm
                             focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                />
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-4">
              <Link to="/products"
                className="text-sm text-gray-600 hover:text-indigo-600 font-medium hidden sm:block">
                Products
              </Link>

              {isAuthenticated() ? (
                <>
                  {/* Cart */}
                  <Link to="/cart" className="relative p-2 hover:text-indigo-600">
                    <ShoppingCart className="w-5 h-5 text-gray-600" />
                    {totalItems() > 0 && (
                      <span className="absolute -top-1 -right-1 bg-indigo-600 text-white text-xs
                                       rounded-full w-5 h-5 flex items-center justify-center font-bold">
                        {totalItems()}
                      </span>
                    )}
                  </Link>

                  {/* Orders */}
                  <Link to="/orders" className="text-sm text-gray-600 hover:text-indigo-600 font-medium hidden sm:block">
                    Orders
                  </Link>

                  {/* Admin */}
                  {isAdmin() && (
                    <Link to="/admin" className="text-sm text-gray-600 hover:text-indigo-600 flex items-center gap-1">
                      <Shield className="w-4 h-4" />
                      <span className="hidden sm:inline">Admin</span>
                    </Link>
                  )}

                  {/* User dropdown */}
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-700 hidden sm:block">
                      Hi, {user?.firstName}
                    </span>
                    <button onClick={handleLogout}
                      className="flex items-center gap-1 text-sm text-gray-500 hover:text-red-500">
                      <LogOut className="w-4 h-4" />
                    </button>
                  </div>
                </>
              ) : (
                <div className="flex items-center gap-3">
                  <Link to="/login"
                    className="text-sm text-gray-600 hover:text-indigo-600 font-medium">
                    Login
                  </Link>
                  <Link to="/register"
                    className="text-sm bg-indigo-600 text-white px-4 py-2 rounded-lg
                               hover:bg-indigo-700 font-medium transition-colors">
                    Sign Up
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* ── Main content ── */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>

      {/* ── Footer ── */}
      <footer className="bg-white border-t border-gray-200 mt-16">
        <div className="max-w-7xl mx-auto px-4 py-8 text-center text-sm text-gray-500">
          © 2024 ShopEasy · Distributed E-Commerce Platform · Built with Spring Boot + React
        </div>
      </footer>
    </div>
  )
}
