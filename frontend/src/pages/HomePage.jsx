import { useQuery } from '@tanstack/react-query'
import { productService, categoryService } from '../services'
import { Link } from 'react-router-dom'
import { ArrowRight, Zap, Shield, Truck, Package } from 'lucide-react'

export default function HomePage() {
  const { data: productsData } = useQuery({
    queryKey: ['featured-products'],
    queryFn: () => productService.getAll({ page: 0, size: 8 }),
  })

  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryService.getAll(),
  })

  const products   = productsData?.data?.data?.content ?? []
  const categories = (categoriesData?.data?.data ?? []).slice(0, 6)

  return (
    <div className="space-y-12">
      {/* Hero */}
      <div className="bg-gradient-to-br from-indigo-600 to-purple-700 rounded-2xl p-8 md:p-12 text-white relative overflow-hidden">
        <div className="relative z-10">
          <h1 className="text-3xl md:text-4xl font-bold mb-3">
            Everything you need,<br />delivered fast
          </h1>
          <p className="text-indigo-200 mb-6 max-w-lg">
            Shop from thousands of products with secure payments and real-time order tracking.
          </p>
          <Link to="/products"
            className="inline-flex items-center gap-2 bg-white text-indigo-700 font-semibold
                       px-6 py-3 rounded-xl hover:bg-indigo-50 transition-colors">
            Shop Now <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
        <div className="absolute right-0 bottom-0 w-64 h-64 bg-white/5 rounded-full -mr-16 -mb-16" />
      </div>

      {/* Features */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { icon: Truck,  title: 'Fast Delivery',     desc: 'Free shipping over ₹500' },
          { icon: Shield, title: 'Secure Payments',   desc: 'Encrypted & safe checkout' },
          { icon: Zap,    title: 'Live Tracking',     desc: 'Know where your order is' },
        ].map(({ icon: Icon, title, desc }) => (
          <div key={title} className="bg-white rounded-xl border border-gray-100 p-4 text-center">
            <Icon className="w-7 h-7 text-indigo-600 mx-auto mb-2" />
            <h3 className="font-semibold text-sm text-gray-900">{title}</h3>
            <p className="text-xs text-gray-500 mt-0.5">{desc}</p>
          </div>
        ))}
      </div>

      {/* Categories */}
      {categories.length > 0 && (
        <div>
          <h2 className="text-xl font-bold text-gray-900 mb-4">Shop by Category</h2>
          <div className="grid grid-cols-3 sm:grid-cols-6 gap-3">
            {categories.map((cat) => (
              <Link key={cat.id} to={`/products?categoryId=${cat.id}`}
                className="bg-white rounded-xl border border-gray-100 p-4 text-center
                           hover:shadow-md hover:border-indigo-200 transition-all">
                <div className="w-10 h-10 bg-indigo-50 rounded-xl mx-auto mb-2 flex items-center justify-center">
                  <Package className="w-5 h-5 text-indigo-600" />
                </div>
                <p className="text-xs font-medium text-gray-700 leading-tight">{cat.name}</p>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Featured Products */}
      {products.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-gray-900">Featured Products</h2>
            <Link to="/products" className="text-sm text-indigo-600 hover:underline font-medium flex items-center gap-1">
              View all <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {products.map((p) => (
              <Link key={p.id} to={`/products/${p.id}`}
                className="bg-white rounded-xl border border-gray-100 overflow-hidden
                           hover:shadow-md transition-shadow group">
                <div className="aspect-square bg-gray-50 overflow-hidden">
                  <img
                    src={p.imageUrl || `https://placehold.co/200x200/e0e7ff/4f46e5?text=${encodeURIComponent(p.name.substring(0,8))}`}
                    alt={p.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                </div>
                <div className="p-3">
                  <p className="text-xs font-medium text-gray-900 line-clamp-1">{p.name}</p>
                  <p className="text-indigo-600 font-bold text-sm mt-1">
                    ₹{Number(p.price).toLocaleString('en-IN')}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
