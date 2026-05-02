import { useQuery } from '@tanstack/react-query'
import { useSearchParams, Link } from 'react-router-dom'
import { productService, categoryService } from '../services'
import { useCartStore } from '../store/cartStore'
import { ShoppingCart, Star, SlidersHorizontal, Search } from 'lucide-react'
import { useState } from 'react'

function ProductCard({ product }) {
  const { addItem } = useCartStore()

  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow overflow-hidden group">
      <Link to={`/products/${product.id}`}>
        <div className="aspect-square bg-gray-50 overflow-hidden">
          <img
            src={product.imageUrl || `https://placehold.co/300x300/e0e7ff/4f46e5?text=${encodeURIComponent(product.name)}`}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        </div>
      </Link>

      <div className="p-4">
        <p className="text-xs text-indigo-500 font-medium mb-1">{product.categoryName}</p>
        <Link to={`/products/${product.id}`}>
          <h3 className="font-semibold text-gray-900 text-sm line-clamp-2 hover:text-indigo-600">{product.name}</h3>
        </Link>

        {product.averageRating > 0 && (
          <div className="flex items-center gap-1 mt-1">
            <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
            <span className="text-xs text-gray-500">{Number(product.averageRating).toFixed(1)} ({product.totalReviews})</span>
          </div>
        )}

        <div className="flex items-center justify-between mt-3">
          <div>
            <span className="font-bold text-gray-900">₹{Number(product.price).toLocaleString('en-IN')}</span>
            {product.originalPrice && product.originalPrice > product.price && (
              <span className="text-xs text-gray-400 line-through ml-2">
                ₹{Number(product.originalPrice).toLocaleString('en-IN')}
              </span>
            )}
          </div>
          {product.discountPercent > 0 && (
            <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">
              {product.discountPercent}% off
            </span>
          )}
        </div>

        <button
          onClick={() => addItem(product.id, 1)}
          className="w-full mt-3 flex items-center justify-center gap-2 bg-indigo-600 text-white
                     text-sm py-2 rounded-lg hover:bg-indigo-700 transition-colors font-medium">
          <ShoppingCart className="w-4 h-4" />
          Add to Cart
        </button>
      </div>
    </div>
  )
}

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [showFilters, setShowFilters] = useState(false)

  const q          = searchParams.get('q') || ''
  const categoryId = searchParams.get('categoryId') || ''
  const minPrice   = searchParams.get('minPrice') || ''
  const maxPrice   = searchParams.get('maxPrice') || ''
  const page       = parseInt(searchParams.get('page') || '0')

  const { data: productsData, isLoading } = useQuery({
    queryKey: ['products', q, categoryId, minPrice, maxPrice, page],
    queryFn: () => {
      const params = { page, size: 20 }
      if (q)          params.q          = q
      if (categoryId) params.categoryId = categoryId
      if (minPrice)   params.minPrice   = minPrice
      if (maxPrice)   params.maxPrice   = maxPrice
      return q ? productService.search(q, params) : productService.getAll(params)
    },
  })

  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryService.getAll(),
  })

  const products   = productsData?.data?.data?.content ?? []
  const totalPages = productsData?.data?.data?.totalPages ?? 1
  const categories = categoriesData?.data?.data ?? []

  const updateFilter = (key, value) => {
    const next = new URLSearchParams(searchParams)
    value ? next.set(key, value) : next.delete(key)
    next.set('page', '0')
    setSearchParams(next)
  }

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {q ? `Results for "${q}"` : 'All Products'}
          </h1>
          {productsData && (
            <p className="text-sm text-gray-500 mt-1">
              {productsData.data?.data?.totalElements ?? 0} products found
            </p>
          )}
        </div>
        <button onClick={() => setShowFilters(!showFilters)}
          className="flex items-center gap-2 text-sm border border-gray-300 px-3 py-2 rounded-lg hover:bg-gray-50">
          <SlidersHorizontal className="w-4 h-4" />
          Filters
        </button>
      </div>

      <div className="flex gap-6">
        {/* Filters sidebar */}
        {showFilters && (
          <aside className="w-56 shrink-0">
            <div className="bg-white rounded-xl border border-gray-100 p-4 space-y-5">
              <div>
                <h3 className="font-semibold text-sm text-gray-900 mb-2">Category</h3>
                <div className="space-y-1">
                  <button onClick={() => updateFilter('categoryId', '')}
                    className={`block w-full text-left text-sm px-2 py-1.5 rounded-lg ${!categoryId ? 'bg-indigo-50 text-indigo-600 font-medium' : 'text-gray-600 hover:bg-gray-50'}`}>
                    All Categories
                  </button>
                  {categories.map((cat) => (
                    <button key={cat.id} onClick={() => updateFilter('categoryId', cat.id)}
                      className={`block w-full text-left text-sm px-2 py-1.5 rounded-lg ${categoryId == cat.id ? 'bg-indigo-50 text-indigo-600 font-medium' : 'text-gray-600 hover:bg-gray-50'}`}>
                      {cat.name}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <h3 className="font-semibold text-sm text-gray-900 mb-2">Price Range</h3>
                <div className="space-y-2">
                  <input type="number" placeholder="Min ₹"
                    defaultValue={minPrice}
                    onBlur={(e) => updateFilter('minPrice', e.target.value)}
                    className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  <input type="number" placeholder="Max ₹"
                    defaultValue={maxPrice}
                    onBlur={(e) => updateFilter('maxPrice', e.target.value)}
                    className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
              </div>
            </div>
          </aside>
        )}

        {/* Products grid */}
        <div className="flex-1">
          {isLoading ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
              {[...Array(8)].map((_, i) => (
                <div key={i} className="bg-white rounded-xl border border-gray-100 overflow-hidden animate-pulse">
                  <div className="aspect-square bg-gray-200" />
                  <div className="p-4 space-y-2">
                    <div className="h-3 bg-gray-200 rounded w-1/2" />
                    <div className="h-4 bg-gray-200 rounded" />
                    <div className="h-4 bg-gray-200 rounded w-3/4" />
                  </div>
                </div>
              ))}
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-16">
              <Search className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <h3 className="text-lg font-semibold text-gray-900">No products found</h3>
              <p className="text-sm text-gray-500 mt-1">Try adjusting your search or filters</p>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
                {products.map((p) => <ProductCard key={p.id} product={p} />)}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-8">
                  {[...Array(totalPages)].map((_, i) => (
                    <button key={i} onClick={() => updateFilter('page', i)}
                      className={`w-9 h-9 rounded-lg text-sm font-medium transition-colors
                        ${page === i ? 'bg-indigo-600 text-white' : 'border border-gray-200 hover:bg-gray-50 text-gray-600'}`}>
                      {i + 1}
                    </button>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}
