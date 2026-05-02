import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { productService } from '../services'
import { useCartStore } from '../store/cartStore'
import { ShoppingCart, Star, ArrowLeft, Package } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useState } from 'react'

export default function ProductPage() {
  const { id } = useParams()
  const { addItem } = useCartStore()
  const [qty, setQty] = useState(1)
  const [adding, setAdding] = useState(false)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['product', id],
    queryFn: () => productService.getById(id),
  })

  const product = data?.data?.data

  const handleAddToCart = async () => {
    setAdding(true)
    await addItem(product.id, qty)
    setAdding(false)
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-10 animate-pulse">
        <div className="aspect-square bg-gray-200 rounded-2xl" />
        <div className="space-y-4">
          <div className="h-6 bg-gray-200 rounded w-3/4" />
          <div className="h-4 bg-gray-200 rounded w-1/2" />
          <div className="h-8 bg-gray-200 rounded w-1/3" />
        </div>
      </div>
    )
  }

  if (isError || !product) {
    return (
      <div className="text-center py-20">
        <Package className="w-16 h-16 text-gray-200 mx-auto mb-4" />
        <h2 className="text-xl font-bold text-gray-900">Product not found</h2>
        <Link to="/products" className="text-indigo-600 hover:underline mt-2 block">
          Back to products
        </Link>
      </div>
    )
  }

  return (
    <div>
      <Link to="/products" className="flex items-center gap-1 text-sm text-gray-500 hover:text-indigo-600 mb-6">
        <ArrowLeft className="w-4 h-4" /> Back to products
      </Link>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
        {/* Image */}
        <div className="aspect-square bg-gray-50 rounded-2xl overflow-hidden border border-gray-100">
          <img
            src={product.imageUrl || `https://placehold.co/500x500/e0e7ff/4f46e5?text=${encodeURIComponent(product.name.substring(0,10))}`}
            alt={product.name}
            className="w-full h-full object-cover"
          />
        </div>

        {/* Details */}
        <div className="space-y-4">
          <div>
            <p className="text-sm text-indigo-500 font-medium">{product.categoryName}</p>
            <h1 className="text-2xl font-bold text-gray-900 mt-1">{product.name}</h1>
            {product.brand && (
              <p className="text-sm text-gray-500 mt-1">by {product.brand}</p>
            )}
          </div>

          {product.averageRating > 0 && (
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-0.5">
                {[...Array(5)].map((_, i) => (
                  <Star key={i}
                    className={`w-4 h-4 ${i < Math.round(product.averageRating) ? 'fill-amber-400 text-amber-400' : 'text-gray-200'}`} />
                ))}
              </div>
              <span className="text-sm text-gray-500">
                {Number(product.averageRating).toFixed(1)} ({product.totalReviews} reviews)
              </span>
            </div>
          )}

          <div className="flex items-baseline gap-3">
            <span className="text-3xl font-bold text-gray-900">
              ₹{Number(product.price).toLocaleString('en-IN')}
            </span>
            {product.originalPrice && product.originalPrice > product.price && (
              <>
                <span className="text-lg text-gray-400 line-through">
                  ₹{Number(product.originalPrice).toLocaleString('en-IN')}
                </span>
                <span className="text-sm bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">
                  {product.discountPercent}% off
                </span>
              </>
            )}
          </div>

          {product.description && (
            <p className="text-gray-600 text-sm leading-relaxed">{product.description}</p>
          )}

          {/* Quantity selector */}
          <div className="flex items-center gap-4">
            <label className="text-sm font-medium text-gray-700">Quantity:</label>
            <div className="flex items-center gap-2 border border-gray-200 rounded-lg px-3 py-1.5">
              <button onClick={() => setQty(q => Math.max(1, q - 1))}
                className="text-gray-500 hover:text-gray-700 font-bold">−</button>
              <span className="w-8 text-center text-sm font-semibold">{qty}</span>
              <button onClick={() => setQty(q => Math.min(99, q + 1))}
                className="text-gray-500 hover:text-gray-700 font-bold">+</button>
            </div>
          </div>

          <button onClick={handleAddToCart} disabled={adding}
            className="flex items-center justify-center gap-2 w-full bg-indigo-600 text-white
                       py-3.5 rounded-xl font-semibold hover:bg-indigo-700 disabled:opacity-60
                       transition-colors text-base">
            <ShoppingCart className="w-5 h-5" />
            {adding ? 'Adding…' : 'Add to Cart'}
          </button>

          {/* Tags */}
          {product.tags?.length > 0 && (
            <div className="flex flex-wrap gap-2 pt-2">
              {product.tags.map(tag => (
                <span key={tag} className="text-xs bg-gray-100 text-gray-600 px-2.5 py-1 rounded-full">
                  #{tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
